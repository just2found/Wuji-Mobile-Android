package net.sdvn.nascommon.model.oneos.transfer_r

import androidx.lifecycle.MutableLiveData
import io.objectbox.query.Query
import libs.source.common.AppExecutors
import net.sdvn.nascommon.SessionManager
import net.sdvn.nascommon.db.TransferHistoryKeeper
import net.sdvn.nascommon.db.objecbox.TransferHistory
import net.sdvn.nascommon.db.objecbox.TransferHistory_
import net.sdvn.nascommon.iface.GetSessionListener
import net.sdvn.nascommon.model.oneos.transfer.DownloadElement
import net.sdvn.nascommon.model.oneos.transfer.OnTransferFileListener
import net.sdvn.nascommon.model.oneos.transfer.TransferElement
import net.sdvn.nascommon.model.oneos.transfer.TransferManager.OnTransferCompleteListener
import net.sdvn.nascommon.model.oneos.transfer.TransferManager.OnTransferCountListener
import net.sdvn.nascommon.model.oneos.transfer.TransferState
import net.sdvn.nascommon.model.oneos.transfer_r.interfaces.CallBack
import net.sdvn.nascommon.model.oneos.transfer_r.thread.TaskRunnable
import net.sdvn.nascommon.model.oneos.transfer_r.thread.TransferThreadExecutor
import net.sdvn.nascommon.model.oneos.user.LoginSession
import net.sdvn.nascommon.utils.MediaScanner
import org.view.libwidget.handler.DelayedUnit
import org.view.libwidget.handler.DelayedUtils
import timber.log.Timber
import java.io.File
import java.lang.ref.WeakReference
import java.util.*
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.ReentrantLock

/**
create by: 86136
create time: 2021/2/18 13:41
Function description:
 */

abstract class TransferManagerR<T : TransferElement>(val isDownload: Boolean) {

    companion object {
        const val FILE_IS_EXIST = -1
        const val TRANSFER_START = 1
        const val TRANSFER_TRANSMISSION = 2
        const val TRANSFER_COMPLETE = 4
        const val TRANSFER_CANCELED = 8
        const val TRANSFER_PAUSE = 16
        const val TRANSFER_FAIL = 32

        const val TASK_NUMBER_MIN = 10 //当任务低于这个时候则开始添加数量
        const val TASK_NUMBER_MAX = 20 //这个是将任务数添加到数量
        private const val ALL_INTERCEPT = "AllIntercept"
    }


    //传输计数相关
    private var count: Int? = null
    private val countListeners = mutableListOf<WeakReference<OnTransferCountListener>>()
    private val mTransferHistoryList = mutableListOf<TransferHistory>()


    fun addOnTransferCountListener(onTransferCountListener: OnTransferCountListener): Boolean {
        val iterator = countListeners.iterator()
        while (iterator.hasNext()) {
            val data = iterator.next()
            if (data.get() == onTransferCountListener) {
                return false
            }
        }
        notifyTransferReCount()
        countListeners.add(WeakReference(onTransferCountListener))
        return true
    }

    fun removeOnTransferCountListener(onTransferCountListener: OnTransferCountListener) {
        val iterator = countListeners.iterator()
        while (iterator.hasNext()) {
            val data = iterator.next()
            if (data.get() == null || data.get() == onTransferCountListener)
                iterator.remove()
        }
    }


    private val mReCountRunnable = Runnable {
        val count = TransferHistoryKeeper.allCount(null, isDownload, false).toInt()
        val iterator = countListeners.iterator()
        while (iterator.hasNext()) {
            val data = iterator.next()
            if (data.get() == null) {
                iterator.remove()
            } else {
                data.get()?.let {
                    it.onChanged(isDownload, count)
                }
            }
        }
    }

    //通知全部从新计数 00
    fun notifyTransferReCount() {
        DelayedUtils.addDelayedUnit(if (isDownload) DelayedUtils.NOTIFY_TRANSFER_COUNT_DOWNLOAD else DelayedUtils.NOTIFY_TRANSFER_COUNT_UPLOAD, DelayedUnit(mReCountRunnable, delayTime = 500, maxIntervalTime = 1000))
    }


    private val mCompleteListenerList = mutableListOf<WeakReference<OnTransferCompleteListener<T>>>()

    /**
     * Add a [OnTransferCountListener] to `completeListeners`
     *
     * @param listener
     * @return true if succeed, false otherwise.
     */
    open fun addTransferCompleteListener(listener: OnTransferCompleteListener<T>): Boolean {
        val iterator = mCompleteListenerList.iterator()
        while (iterator.hasNext()) {
            val data = iterator.next()
            if (data.get() == listener) {
                return false
            }
        }
        mCompleteListenerList.add(WeakReference(listener))
        return true
    }

    /**
     * Remove the [OnTransferCountListener] from `completeListeners`
     *
     * @param listener
     * @return true if succeed, false otherwise.
     */
    open fun removeTransferCompleteListener(listener: OnTransferCompleteListener<T>?): Boolean {
        val iterator = mCompleteListenerList.iterator()
        while (iterator.hasNext()) {
            val data = iterator.next()
            if (data.get() == null || data.get() == listener)
                iterator.remove()
        }
        return true
    }

    protected fun notifyComplete(t: T) {
        val iterator = mCompleteListenerList.iterator()
        while (iterator.hasNext()) {
            val data = iterator.next()
            if (data.get() == null) {
                iterator.remove()
            } else {
                data.get()?.let {
                    it.onComplete(isDownload, t)
                }
            }
        }
    }


    private var mHasMoreData = true//数据存在更多数据的意思  仅在内部唤醒 自循环时候使用
    private var isLoadingMore = false//记录当前是否正在加载更多

    private var interceptDevice: String? = null//是否进行拦截


    val transferRefreshLiveData by lazy { MutableLiveData<TransferRefreshEvent<T>>() }//因为LiveData的传输数据有点问题所以这里只传递完成的

    protected val mTransferThreadExecutor by lazy {//线程任务相关
        TransferThreadExecutor<T>()
    }

    protected var callBack: WeakReference<CallBack<TransferRefreshEvent<T>>>? = null //这里做一个弱引用

    fun setCallBack(callBack: CallBack<TransferRefreshEvent<T>>) {
        this.callBack = WeakReference(callBack)
    }

    private val mOnTransferFileListener by lazy {
        val obj = object : OnTransferFileListener<T> {

            override fun onStart(url: String?, element: T) {
                onStart(element)
            }

            override fun onTransmission(url: String?, element: T) {
                onTransmission(element)
            }

            override fun onComplete(url: String?, element: T) {
                onComplete(element)
            }
        }
        obj
    }


    private val mWakeUpRunnable = Runnable {
        mTransferThreadExecutor.runInIO {
            if (mHasMoreData) {
                val nowTaskCount = mTransferThreadExecutor.getTaskCount()
                if (nowTaskCount < TASK_NUMBER_MIN) {
                    loadMoreTask((TASK_NUMBER_MAX - nowTaskCount).toLong())
                }
            }
        }
    }


    private val mWakeUpDelayedUnit by lazy {
        DelayedUnit()
    }

    //抽出来是为了方便给添加任务调用
    fun wakeUpTask(restFlag: Boolean = true) {
        if (restFlag) {
            interceptDevice = null;
            mHasMoreData = true
        }
        mWakeUpDelayedUnit.runnable = mWakeUpDelayedUnit.runnable ?: mWakeUpRunnable
        DelayedUtils.addDelayedUnit(if (isDownload) DelayedUtils.WAKE_UP_TRANSFER_TASK_DOWNLOAD else DelayedUtils.WAKE_UP_TRANSFER_TASK_UPLOAD, mWakeUpDelayedUnit)
    }


    fun pause(tag: String) = mTransferThreadExecutor.runInIO { mTransferThreadExecutor.interrupt(tag) }

    //因为调用它方法已经在IO 所有这里IO
    fun cancelTask(tag: String, id: Long) = mTransferThreadExecutor.runInIO {
        removeTransferHistory(id)
        mTransferThreadExecutor.interrupt(tag)
    }


    //因为调用它方法已经在IO 所有这里IO
    fun cancelDeviceId(devicId: String?) {
        if (devicId.isNullOrEmpty()) {
            mTransferHistoryList.clear()
            interceptDevice = ALL_INTERCEPT
            mTransferThreadExecutor.interruptAll()
        } else {
            interceptDevice = devicId
            val iterator = mTransferHistoryList.iterator()
            while (iterator.hasNext()) {
                val transferHistory = iterator.next()
                if (transferHistory.srcDevId == devicId)
                    iterator.remove()
            }
            mTransferThreadExecutor.interruptDeviceId(devicId)
        }
    }

    fun pauseDeviceId(devicId: String?) {
        mTransferThreadExecutor.runInIO {
            if (devicId.isNullOrEmpty()) {
                interceptDevice = ALL_INTERCEPT
                mTransferThreadExecutor.interruptAll()
            } else {
                interceptDevice = devicId
                mTransferThreadExecutor.interruptDeviceId(devicId)
            }
        }
    }


    fun pause(tagList: List<String>) {
        mTransferThreadExecutor.runInIO {
            mTransferThreadExecutor.interrupt(tagList)
        }

    }

    fun pause() {
        mTransferThreadExecutor.runInIO {
            mTransferThreadExecutor.interruptAll()
        }
    }


    private fun findByTransferHistory(id: Long): TransferHistory? {
        for (transferHistory in mTransferHistoryList) {
            if (transferHistory.id == id)
                return transferHistory
        }
        return null
    }

    private fun removeTransferHistory(id: Long) {
        val iterator = mTransferHistoryList.iterator()
        while (iterator.hasNext()) {
            val data = iterator.next()
            if (data.id == id) {
                iterator.remove()
                break
            }
        }
    }

    protected fun addTransferHistoryList(list: List<TransferHistory>) {
        for (transferHistory in list) {
            if (findByTransferHistory(transferHistory.id) == null) {
                mTransferHistoryList.add(transferHistory)
            }
        }
    }

    protected fun addTransferHistory(transferHistory: TransferHistory): Boolean {
        return if (findByTransferHistory(transferHistory.id) == null) {
            mTransferHistoryList.add(transferHistory)
            true
        } else {
            false
        }
    }

    private fun loadMoreTask(limit: Long = TASK_NUMBER_MAX.toLong()) {
        if (isLoadingMore || ALL_INTERCEPT == interceptDevice)
            return
        isLoadingMore = true
        val histories = TransferHistoryKeeper.allToManager(interceptDevice, isDownload, limit)//
        if (histories.isNullOrEmpty()) {
            mHasMoreData = false
        }
        for (transferHistory in histories) {
            if (transferHistory.srcDevId == interceptDevice)
                continue
            val element = convertData(transferHistory)
            //这里只有拿到数据不为空，且没有存TransferHistory和不存在任务再进行添加
            if (element != null) {
                if (findByTransferHistory(element.id) == null) {
                    addTransferHistory(transferHistory)
                }
                if (!mTransferThreadExecutor.isExist(element.tag)) {
                    val runnable = buildTaskRunnable(element, mOnTransferFileListener)
                    mTransferThreadExecutor.execute(runnable)
                }
            }
        }
        isLoadingMore = false
    }


    protected open fun getLoginSession(devID: String?): LoginSession? {
        var loginSession = SessionManager.getInstance().getLoginSession(devID)
        if (loginSession != null && loginSession.isLogin) {
            return loginSession
        }
        val latch = CountDownLatch(1)
        SessionManager.getInstance().getLoginSession(devID!!,
                object : GetSessionListener(false) {
                    override fun onSuccess(url: String, data: LoginSession) {
                        loginSession = data
                        latch.countDown()
                    }

                    override fun onFailure(url: String, errorNo: Int, errorMsg: String) {
                        latch.countDown()
                    }
                })
        try {
            latch.await(10, TimeUnit.SECONDS)
        } catch (e: InterruptedException) {
            loginSession = null
        }
        return loginSession
    }


    open fun onDestroy() {

    }

    open fun enqueue(elements: List<T>): Int {
        if (!elements.isNullOrEmpty()) {
            val element0 = elements[0]
            val arrayList = ArrayList(elements)
            val query: Query<TransferHistory> = getQuery(element0)
            val histories: MutableList<TransferHistory> = findIdsTransferHistories(arrayList, query)
            for (element in arrayList) {
                val transferHistory: TransferHistory = getTransferHistory(element)
                histories.add(transferHistory)
            }
            TransferHistoryKeeper.update(histories)
            Timber.d(" enqueue(List<TransferElement> elements : %s", elements.size)
//            findIdsTransferHistories(arrayList, query)
            wakeUpTask()
            notifyTransferReCount()
        }
        return 0
    }

    private fun findIdsTransferHistories(arrayList: ArrayList<T>, query: Query<TransferHistory>): MutableList<TransferHistory> {
        val count = query.count()
        var offset: Long = 0
        val limit: Long = 800
        val histories: MutableList<TransferHistory> = ArrayList()
        out@ while (offset < count) {
            val transferHistoriesPart = query.find(offset, limit)
            offset += limit
            val iterator = arrayList.iterator()
            inner@ for (transferHistory in transferHistoriesPart) {
                while (iterator.hasNext()) {
                    val next = iterator.next()
                    if (transferHistory.srcPath == next.srcPath) {
                        // 重新检查服务端文件是否存在
                        if (transferHistory.isComplete) {
                            transferHistory.length = 0
                            transferHistory.check = true
                            transferHistory.isComplete = false
                            transferHistory.state = TransferState.WAIT
                            histories.add(transferHistory)
                        }
                        iterator.remove()
                        continue@inner
                    }
                }
                if (arrayList.size == 0) {
                    break@out
                }
            }
        }
        return histories
    }


    open fun getQuery(element0: T): Query<TransferHistory> {
        return if (isDownload) {
            TransferHistoryKeeper.getTransferHistoryQueryBuilder()
                    .equal(TransferHistory_.srcDevId, element0.devId)
                    .equal(TransferHistory_.type, TransferHistoryKeeper.getTransferType(isDownload).toLong())
                    .equal(TransferHistory_.groupId, element0.groupId?:-1)
                    .build()
        } else {
            TransferHistoryKeeper.getTransferHistoryQueryBuilder()
                    .equal(TransferHistory_.srcDevId, element0.devId)
                    .equal(TransferHistory_.type, TransferHistoryKeeper.getTransferType(isDownload).toLong())
                    .equal(TransferHistory_.toPath, element0.toPath)
                    .equal(TransferHistory_.groupId, element0.groupId?:-1)
                    .build()
        }
    }


    open fun onComplete(element: T) {
        DownloadManagerR.mTransferThreadExecutor.runInIO {
            val state = element.state
            val query = findByTransferHistory(element.id)
            query?.let {
                var dataRefreshEvent: TransferRefreshEvent<T>? = null
                when (state) {
                    TransferState.PAUSE -> {
                        query.length = element.length
                        query.state = state
                        TransferHistoryKeeper.update(query)
                        dataRefreshEvent = TransferRefreshEvent(TRANSFER_PAUSE, element)
                    }
                    TransferState.FAILED -> {
                        query.length = element.length
                        query.state = state
                        TransferHistoryKeeper.update(query)
                        dataRefreshEvent = TransferRefreshEvent(TRANSFER_FAIL, element)
                    }
                    TransferState.COMPLETE -> {
                        query.time = System.currentTimeMillis()
                        query.state = state
                        query.isComplete = true
                        query.length = element.length
                        if (element is DownloadElement && element.toName != null) {
                            query.name = element.toName!!
                            MediaScanner.getInstance().scanningFile(element.toPath + File.separator + element.toName)//我现在不知道这一行是用来做什么的
                        }
                        dataRefreshEvent = TransferRefreshEvent(TRANSFER_COMPLETE, element)
                        TransferHistoryKeeper.update(query)
                    }
                    TransferState.CANCELED -> {

                    }//取消不用管 UI已经删除了
                }
                removeTransferHistory(element.id)//移除
                dataRefreshEvent?.let {
                    mTransferThreadExecutor.onlyRemove(element.tag)
                    notifyComplete(element)
                    wakeUpTask(false)
                    callBack?.get()?.onCallBack(it)
                }
            }
            notifyTransferReCount()
        }
    }

    open fun onTransmission(element: T) {
        DownloadManagerR.mTransferThreadExecutor.runInIO {
            val query = findByTransferHistory(element.id)
            query?.let {
                query.state = element.state
                query.length = element.length
                DelayedUtils.addDelayedUnit(if (isDownload) DelayedUtils.DELAY_SAVE_TRANSFER_DOWNLOAD else DelayedUtils.DELAY_SAVE_TRANSFER_UPLOAD, DelayedUnit(saveTransmissionProgressRunnable, delayTime = 2000, maxIntervalTime = 2000))
            }
        }
        val data = TransferRefreshEvent(TRANSFER_TRANSMISSION, element)
        callBack?.get()?.onCallBack(data)
    }


    //因为这里考虑onComplete可能会移除和处理掉一些数据，所以这里只需要存状态是Start
    private val saveTransmissionProgressRunnable = Runnable {
        DownloadManagerR.mTransferThreadExecutor.runInIO {
            val list = mutableListOf<TransferHistory>()
            for (transferHistory in mTransferHistoryList) {
                if (transferHistory.state == TransferState.START) {
                    list.add(transferHistory)
                }
            }
            if (list.size > 0)
                TransferHistoryKeeper.update(list)
        }
    }

    //这个方法先这样保留 暂时没有用上
    open fun onStart(element: T) {}

    /*** **** **** **** **** **** **** **** **** **** **** **** **** **** ****
     *  **** **** **** **** **** **** **** **** **** **** **** **** ****
     *                      需要子类实现的方法
     * **** **** **** **** **** **** **** **** **** **** **** **** **** **** ****
     */
    protected abstract fun convertData(dbList: List<TransferHistory>): List<T>

    protected abstract fun convertData(transferHistory: TransferHistory): T?

    protected abstract fun buildTaskRunnable(t: T, onTransferFileListener: OnTransferFileListener<T>): TaskRunnable<T>

    abstract fun getReentrantLock(): ReentrantLock

    abstract fun enqueue(element: T): Int


    protected abstract fun getTransferHistory(element: T): TransferHistory
}