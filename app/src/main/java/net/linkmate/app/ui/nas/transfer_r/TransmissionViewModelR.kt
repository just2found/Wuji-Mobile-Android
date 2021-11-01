package net.linkmate.app.ui.nas.transfer_r

import androidx.lifecycle.ViewModel
import com.chad.library.adapter.base.entity.MultiItemEntity
import libs.source.common.AppExecutors
import net.sdvn.nascommon.db.TransferHistoryKeeper
import net.sdvn.nascommon.model.oneos.transfer.DownloadElement
import net.sdvn.nascommon.model.oneos.transfer.TransferElement
import net.sdvn.nascommon.model.oneos.transfer.TransferState
import net.sdvn.nascommon.model.oneos.transfer.UploadElement
import net.sdvn.nascommon.model.oneos.transfer_r.DataRefreshEvent
import net.sdvn.nascommon.model.oneos.transfer_r.DownloadManagerR
import net.sdvn.nascommon.model.oneos.transfer_r.TransferManagerR.Companion.TRANSFER_CANCELED
import net.sdvn.nascommon.model.oneos.transfer_r.TransferManagerR.Companion.TRANSFER_COMPLETE
import net.sdvn.nascommon.model.oneos.transfer_r.TransferManagerR.Companion.TRANSFER_FAIL
import net.sdvn.nascommon.model.oneos.transfer_r.TransferManagerR.Companion.TRANSFER_PAUSE
import net.sdvn.nascommon.model.oneos.transfer_r.TransferManagerR.Companion.TRANSFER_START
import net.sdvn.nascommon.model.oneos.transfer_r.TransferManagerR.Companion.TRANSFER_TRANSMISSION
import net.sdvn.nascommon.model.oneos.transfer_r.TransferRefreshEvent
import net.sdvn.nascommon.model.oneos.transfer_r.UploadManagerR
import net.sdvn.nascommon.model.oneos.transfer_r.interfaces.CallBack
import net.sdvn.nascommon.model.oneos.transfer_r.interfaces.Repository
import net.sdvn.nascommon.model.oneos.transfer_r.interfaces.Repository.Companion.DELETE_DATA
import net.sdvn.nascommon.model.oneos.transfer_r.interfaces.Repository.Companion.INSERT_DATA
import net.sdvn.nascommon.model.oneos.transfer_r.interfaces.Repository.Companion.NO_MORE_DATA
import net.sdvn.nascommon.model.oneos.transfer_r.interfaces.Repository.Companion.SOURCE_DATA
import net.sdvn.nascommon.model.oneos.transfer_r.interfaces.Repository.Companion.UPDATE_DATA
import net.sdvn.nascommon.model.oneos.transfer_r.thread.TransferThreadExecutor
import org.view.libwidget.handler.DelayedUnit
import org.view.libwidget.handler.DelayedUtils
import org.view.libwidget.log.L

/**
create by: 86136
create time: 2021/3/12 13:18
Function description:
 */

class TransmissionViewModelR : ViewModel() {

    companion object {
        const val ALL_DEVICE = ""
    }

    val mMultiItemEntityList = mutableListOf<MultiItemEntity>()//只要操作这个类就需要进行加锁同步

    var callBack: CallBack<DataRefreshEvent>? = null //加这个东西主要是可以避免错误刷新
    private var mDeviceID = ALL_DEVICE
    private var mIsDownLoad = false
    private var isNoProgressData = false
    private var isNoCompleteData = false
    private var mProgressItemSupple = 0 //这个是当添加了标题投后需要计算的
    private var mCompleteItemSupple = 0 //这个是当添加了标题投后需要计算的

    private fun wakeUp() {
        if (mIsDownLoad) {
            DownloadManagerR.wakeUpTask()
        } else {
            UploadManagerR.wakeUpTask()
        }
    }


    // 只有传输状态的才会有改变状态  这个是改变单条的
    fun changeTransferStatus(entity: TransferEntityR, position: Int) {
        diskIOExecutor().execute {
            TransferHistoryKeeper.updateStateById(entity.id, entity.state)
        }
        if (entity.state == TransferState.WAIT || entity.state == TransferState.START) {
            entity.state = TransferState.PAUSE
            L.i(entity.tag, "changeTransferStatus", "TransmissionViewModelR", "nwq", "2021/3/22");
            if (mIsDownLoad) {
                DownloadManagerR.pause(entity.tag)
            } else {
                UploadManagerR.pause(entity.tag)
            }
        } else {
            entity.state = TransferState.WAIT
            wakeUp()
        }
        refreshProcessTitleDelay()
        //通知UI刷新数据
        callBack?.onCallBack(DataRefreshEvent(refreshType = UPDATE_DATA, startPosition = position))
    }

    // 全部开始 全部暂停这里直接改
    fun changeTransferStatus(entity: HeaderEntityR) {
        val flag = entity.isAllStart
        if (entity.isAllStart) {
            mProgressRepository.pause()
            if (mIsDownLoad) {
                DownloadManagerR.pauseDeviceId(mDeviceID)
            } else {
                UploadManagerR.pauseDeviceId(mDeviceID)
            }
        } else {
            mProgressRepository.resume()
        }
        entity.isAllStart = !entity.isAllStart
        diskIOExecutor().execute {
            if (flag) {
                TransferHistoryKeeper.updateStateByDevId(mDeviceID, TransferState.PAUSE, mIsDownLoad)
            } else {
                mProgressRepository.resume()
                TransferHistoryKeeper.updateStateByDevId(mDeviceID, TransferState.WAIT, mIsDownLoad)
                wakeUp()
            }
        }


    }

    /**
     * 根据状态 清楚正在下载的任务
     * 需要同步操作
     */
    fun removeEntity(position: Int) {
        val entity = mMultiItemEntityList.removeAt(position)
        if (entity is TransferEntityR) {
            diskIOExecutor().execute {
                TransferHistoryKeeper.delete(entity.id)
                if (entity.isDownload) {
                    if (entity.state == TransferState.WAIT || entity.state == TransferState.START) {
                        DownloadManagerR.cancelTask(entity.tag, entity.id)
                    }
                    if (entity.state != TransferState.START) {
                        DownloadManagerR.notifyTransferReCount()
                    }
                } else {
                    UploadManagerR.notifyTransferReCount()
                    if (entity.state == TransferState.WAIT || entity.state == TransferState.START) {
                        UploadManagerR.cancelTask(entity.tag, entity.id)
                    }
                    if (entity.state != TransferState.START) {
                        UploadManagerR.notifyTransferReCount()
                    }
                }
            }
            callBack?.onCallBack(DataRefreshEvent(refreshType = DELETE_DATA, startPosition = position))
            if (entity.isComplete) {
                mCompleteRepository.onlyDelete(entity)
                refreshCompleteTitle(-1)
            } else {
                mProgressRepository.onlyDelete(entity)
                refreshProcessTitleDelay()
            }
        }

    }


    /**
     * 删除完成的数据
     * 需要同步操作
     */
    fun deleteComplete() {
        diskIOExecutor().execute {
            TransferHistoryKeeper.deleteComplete(mDeviceID, mIsDownLoad)
            AppExecutors.instance.mainThread().execute {
                val startPosition = getProgressSize()
                val countItem = mMultiItemEntityList.size - getProgressSize()
                for (x in getProgressSize() until mMultiItemEntityList.size) {
                    mMultiItemEntityList.removeAt(getProgressSize())
                }
                mCompleteRepository.mDataSource.clear()
                mCompleteItemSupple = 0
                callBack?.onCallBack(DataRefreshEvent(refreshType = DELETE_DATA, startPosition = startPosition, itemCount = countItem))
            }
        }
    }


    //清除失败的的任务
    fun clearTransferFailed() {
        if (mProgressRepository.mDataSource.size <= 0)
            return
        diskIOExecutor().execute {
            val hasDeleteData = TransferHistoryKeeper.deleteByState(mDeviceID, TransferState.FAILED, mIsDownLoad)
            if (hasDeleteData) {
                if (mIsDownLoad) {
                    DownloadManagerR.notifyTransferReCount()
                } else {
                    UploadManagerR.notifyTransferReCount()
                }
                AppExecutors.instance.mainThread().execute {
                    reLoadData()
                }
            }
        }
    }

    //删除全部进行中的任务
    fun clearTransferProgress() {
        if (mProgressRepository.mDataSource.size <= 0)
            return
        diskIOExecutor().execute {
            if (TransferHistoryKeeper.deleteByRun(mDeviceID, mIsDownLoad)) {
                if (mIsDownLoad) {
                    DownloadManagerR.cancelDeviceId(mDeviceID)
                    DownloadManagerR.notifyTransferReCount()
                } else {
                    UploadManagerR.cancelDeviceId(mDeviceID)
                    UploadManagerR.notifyTransferReCount()
                }
                AppExecutors.instance.mainThread().execute {
                    reLoadData()
                }
            }
        }
    }

    private fun diskIOExecutor() = TransferThreadExecutor.diskIOExecutor

    //设置参数
    fun setRepositoryOperationParameter(deviceID: String = mDeviceID, isDownLoad: Boolean = mIsDownLoad) {
        if (mDeviceID == deviceID && isDownLoad == mIsDownLoad) {
            return
        }
        mMultiItemEntityList.clear();
        callBack?.onCallBack(DataRefreshEvent(refreshType = SOURCE_DATA, startPosition = 0))
        isNoProgressData = false
        mDeviceID = deviceID
        mIsDownLoad = isDownLoad
        mProgressRepository.getRepositoryOperation()?.let {
            if (it is TransmissionRepositoryOperation) {
                it.deviceID = mDeviceID
                it.isDownLoad = mIsDownLoad
            }
        }
        mCompleteRepository.getRepositoryOperation()?.let {
            if (it is TransmissionRepositoryOperation) {
                it.deviceID = mDeviceID
                it.isDownLoad = mIsDownLoad
            }
        }
        return
    }

    fun reLoadData() {
        isNoProgressData = false
        mProgressItemSupple = 0
        mCompleteItemSupple = 0
        mProgressRepository.mDataSource.clear()
        mCompleteRepository.mDataSource.clear()
        mProgressRepository.reLoadData()
    }

    fun loadMoreData() {
        if (isNoProgressData) {
            mCompleteRepository.loadMoreData()
        } else {
            L.i("mProgressRepository", "loadMoreData", "TransmissionViewModelR", "nwq", "2021/3/17");
            mProgressRepository.loadMoreData()
        }
    }


    /**
     * 需要同步操作
     */
    private val mProgressRepository by lazy {
        TransferEntityRepository().apply {
            setRepositoryOperation(TransmissionRepositoryOperation(mDeviceID, mIsDownLoad, false))
            setCallBack(object : CallBack<DataRefreshEvent> {
                override fun onCallBack(t: DataRefreshEvent) {

                    when (t.refreshType) {
                        NO_MORE_DATA -> {
                            isNoProgressData = true
                            isNoCompleteData = false
                            mCompleteRepository.reLoadData()
                        }
                        SOURCE_DATA -> {  //有可能没有数据但是也还是需要通知刷新所以就这样了
                            mMultiItemEntityList.clear()
                            if (mDataSource.size >= Repository.PAGE_SIZE) {
                                mMultiItemEntityList.add(HeaderEntityR(getMaxCount(), isAllStart(), QuickTransmissionAdapterR.PROGRESS_TITLE))
                                mMultiItemEntityList.addAll(mDataSource)
                                mProgressItemSupple = 1
                                if (hasStart()) {
                                    wakeUp()
                                }
                                callBack?.onCallBack(t)
                            } else {
                                if (mDataSource.size > 0) {
                                    mProgressItemSupple = 1
                                    mMultiItemEntityList.add(HeaderEntityR(getMaxCount(), isAllStart(), QuickTransmissionAdapterR.PROGRESS_TITLE))
                                    if (hasStart()) {
                                        wakeUp()
                                    }
                                    mMultiItemEntityList.addAll(mDataSource)
                                } else {
                                    mProgressItemSupple = 0
                                }
                                isNoProgressData = true
                                mCompleteRepository.reLoadData()
                            }


                        }
                        UPDATE_DATA -> {
                            if (mMultiItemEntityList.size >= t.startPosition + t.itemCount + mProgressItemSupple) {
                                t.startPosition += mProgressItemSupple
                                callBack?.onCallBack(t)
                            }
                        }
                        INSERT_DATA -> {
                            if (mMultiItemEntityList.size >= t.startPosition + mProgressItemSupple) {
                                for ((position, transferEntityR) in mDataSource.withIndex()) {
                                    if (position >= t.startPosition && position < (t.startPosition + t.itemCount)) {
                                        mMultiItemEntityList.add(position + mProgressItemSupple, transferEntityR)
                                    }
                                }
                                t.startPosition += mProgressItemSupple
                                callBack?.onCallBack(t)
                                if (t.itemCount < Repository.PAGE_SIZE) {//如果在家的数据少于页数则说明没有更多数据
                                    isNoProgressData = true
                                    mCompleteRepository.reLoadData()
                                }
                                if (hasStart()) {
                                    wakeUp()
                                }
                            }
                        }
                        DELETE_DATA -> {  //这个只有传输线程推过来的完成才会触发这个删除
                            if (t.startPosition + t.itemCount + mProgressItemSupple > mMultiItemEntityList.size)
                                return
                            if (t.itemCount == 1) {//业务逻辑上说应该只有完成了传输才会进入到这里
                                t.startPosition += mProgressItemSupple
                                val data = mMultiItemEntityList.removeAt(t.startPosition)
                                callBack?.onCallBack(t)
                                refreshProcessTitleDelay()
                                if (data is TransferEntityR && data.isComplete && isNoProgressData) {  //这样是为了筛选出由传输推过来的完成事件
                                    if (mCompleteItemSupple == 1) {
                                        mCompleteRepository.insertComplete(data)//这里不能调用 mCompleteRepository.loadMore reload，不然会导致是否存在更多数据的判断失效
                                        mMultiItemEntityList.add(getProgressSize() + mCompleteItemSupple, data)
                                        callBack?.onCallBack(DataRefreshEvent(refreshType = INSERT_DATA, startPosition = getProgressSize() + mCompleteItemSupple))
                                        refreshCompleteTitle(t.itemCount)
                                    } else {
                                        mCompleteRepository.insertComplete(data)
                                        refreshCompleteTitle(0)
                                    }
                                }
                            } else {//业务逻辑上 批量状态删除
                                for (position in (t.startPosition + mProgressItemSupple) until t.startPosition + t.itemCount + mProgressItemSupple) {
                                    mMultiItemEntityList.removeAt(t.startPosition + mProgressItemSupple)
                                }
                                t.startPosition += mProgressItemSupple
                                callBack?.onCallBack(t)
                                refreshProcessTitleDelay()
                            }
                        }
                    }
                }
            })
        }
    }

    /**
     * 需要同步操作
     */
    val mCompleteRepository by lazy {
        TransferEntityRepository().apply {
            setRepositoryOperation(TransmissionRepositoryOperation(mDeviceID, mIsDownLoad, true))
            setCallBack(object : CallBack<DataRefreshEvent> {
                override fun onCallBack(t: DataRefreshEvent) {
                    when (t.refreshType) {
                        NO_MORE_DATA -> {
                            isNoCompleteData = true
                            callBack?.onCallBack(t)
                        }
                        SOURCE_DATA -> {
                            if (mMultiItemEntityList.size < Repository.PAGE_SIZE + mProgressItemSupple) {//这个开始的时候下载中的页面就没加载man
                                if (mDataSource.size > 0) {
                                    mMultiItemEntityList.add(HeaderEntityR(getMaxCount(), false, QuickTransmissionAdapterR.COMPLETED_TITLE))
                                    mMultiItemEntityList.addAll(mDataSource)
                                    mCompleteItemSupple = 1
                                }
                                if (mDataSource.size < Repository.PAGE_SIZE) {
                                    isNoCompleteData = true
                                }
                                callBack?.onCallBack(t)
                            } else {
                                if (mDataSource.size > 0) {
                                    mMultiItemEntityList.add(HeaderEntityR(getMaxCount(), false, QuickTransmissionAdapterR.COMPLETED_TITLE))
                                    mMultiItemEntityList.addAll(mDataSource)
                                    mCompleteItemSupple = 1
                                    t.startPosition = getProgressSize()
                                    t.refreshType = INSERT_DATA
                                    t.itemCount = mDataSource.size + mCompleteItemSupple
                                    callBack?.onCallBack(t)
                                } else { //mDataSource.size==0 就是数据都是空的情况下
                                    t.refreshType = NO_MORE_DATA
                                    callBack?.onCallBack(t)
                                }
                            }
                        }
                        UPDATE_DATA -> {
                            if (mMultiItemEntityList.size >= getProgressSize() + t.startPosition + t.itemCount + mCompleteItemSupple) {
                                t.startPosition = t.startPosition + getProgressSize() + mProgressItemSupple
                                callBack?.onCallBack(t)
                            }
                        }
                        INSERT_DATA -> {
                            if (mMultiItemEntityList.size >= getProgressSize() + t.startPosition + mProgressItemSupple) {
                                if (mProgressItemSupple == 0 && t.startPosition == 0) {
                                    mMultiItemEntityList.add(HeaderEntityR(getMaxCount(), false, QuickTransmissionAdapterR.COMPLETED_TITLE))
                                    mMultiItemEntityList.addAll(mDataSource)
                                    mCompleteItemSupple = 1
                                    t.startPosition = getProgressSize()
                                    t.refreshType = INSERT_DATA
                                    t.itemCount = mDataSource.size + mCompleteItemSupple
                                    callBack?.onCallBack(t)
                                } else {
                                    for ((position, transferEntityR) in mDataSource.withIndex()) {
                                        if (position >= t.startPosition && position < (t.startPosition + t.itemCount)) {
                                            mMultiItemEntityList.add(position + mProgressItemSupple, transferEntityR)
                                        }
                                    }
                                    t.startPosition = t.startPosition + getProgressSize() + mProgressItemSupple
                                    callBack?.onCallBack(t)
                                }
                            }
                            if (t.itemCount < Repository.PAGE_SIZE) {//如果在家的数据少于页数则说明没有更多数据
                                isNoCompleteData = true
                                t.refreshType = NO_MORE_DATA
                                callBack?.onCallBack(t)
                            }
                        }
                        DELETE_DATA -> {
                            if (mMultiItemEntityList.size >= getProgressSize() + t.startPosition + t.itemCount + mProgressItemSupple) {
                                for (position in (t.startPosition + mProgressItemSupple) until t.startPosition + t.itemCount + mProgressItemSupple) {
                                    mMultiItemEntityList.removeAt(position)
                                }
                                t.startPosition = t.startPosition + getProgressSize() + mProgressItemSupple
                                callBack?.onCallBack(t)
                            }
                            refreshCompleteTitle(-t.itemCount)
                        }
                    }
                }
            })
        }
    }


    private fun refreshProcessTitleDelay() {
        if (mProgressRepository.mDataSource.size == 0) {//如果进行中的任务被全部清除则立即刷新
            mDelayedUnit.maxIntervalTime = 1
        } else {
            mDelayedUnit.maxIntervalTime = 1000
        }
        mDelayedUnit.runnable = mDelayedUnit.runnable ?: refreshProcessTitleRunnable
        DelayedUtils.addDelayedUnit(DelayedUtils.REFRESH_PROCESS_TITLE, mDelayedUnit)
    }

    //这个是为了减少延迟消息的创建次数
    private val mDelayedUnit by lazy {
        DelayedUnit(null, delayTime = 1000, maxIntervalTime = 3000)
    }


    private val refreshProcessTitleRunnable by lazy {
        Runnable {
            if (mMultiItemEntityList.isNullOrEmpty() || mProgressRepository == null)//因为是延迟可能导致被释放
                return@Runnable
            val title = mMultiItemEntityList[0]
            if (title is HeaderEntityR && title.itemType == QuickTransmissionAdapterR.PROGRESS_TITLE) {
                title.size = mProgressRepository.getMaxCount()
                if (title.size > 0) {
                    title.isAllStart = mProgressRepository.isAllStart()
                    callBack?.onCallBack(DataRefreshEvent(refreshType = UPDATE_DATA, startPosition = 0))
                } else {
                    mProgressItemSupple = 0
                    mMultiItemEntityList.removeAt(0)
                    callBack?.onCallBack(DataRefreshEvent(refreshType = DELETE_DATA, startPosition = 0))
                }
            }
        }
    }


//    private fun refreshProcessTitle(count: Int, reCount: Boolean = false, countStatus: Boolean = false) {
//        val title = mMultiItemEntityList[0]
//        if (title is HeaderEntityR) {
//            if (reCount) {
//                title.size = mProgressRepository.getMaxCount()
//            } else {
//                title.size = title.size + count
//            }
//            if (title.size > 0) {
//                if (countStatus) {
//                    title.isAllStart = mProgressRepository.isAllStart()
//                }
//                callBack?.onCallBack(DataRefreshEvent(refreshType = UPDATE_DATA, startPosition = 0))
//            } else {
//                mProgressItemSupple = 0
//                mMultiItemEntityList.removeAt(0)
//                callBack?.onCallBack(DataRefreshEvent(refreshType = DELETE_DATA, startPosition = 0))
//            }
//        }
//    }


    private fun refreshCompleteTitle(count: Int, reCount: Boolean = false) {
        if (mMultiItemEntityList.size > getProgressSize()) { //1.检测完成的头已经添加 2.防止下标越界
            val title = mMultiItemEntityList[getProgressSize()]
            if (title is HeaderEntityR) {
                if (reCount) {
                    title.size = mCompleteRepository.getMaxCount()
                } else {
                    title.size = title.size + count
                }
                if (title.size > 0) {
                    callBack?.onCallBack(DataRefreshEvent(refreshType = UPDATE_DATA, startPosition = getProgressSize()))
                } else {
                    mCompleteItemSupple = 0
                    mMultiItemEntityList.removeAt(getProgressSize())
                    callBack?.onCallBack(DataRefreshEvent(refreshType = DELETE_DATA, startPosition = getProgressSize()))
                }
            }
        } else if (isNoProgressData && mCompleteRepository.mDataSource.size > 0 && mCompleteItemSupple == 0) {//情况:传输完成后走的可能逻辑 ，需要补充头
            mMultiItemEntityList.add(HeaderEntityR(mCompleteRepository.getMaxCount(), false, QuickTransmissionAdapterR.COMPLETED_TITLE))
            mMultiItemEntityList.addAll(mCompleteRepository.mDataSource)
            mCompleteItemSupple = 1
            callBack?.onCallBack(DataRefreshEvent(startPosition = getProgressSize(), refreshType = INSERT_DATA, itemCount = 1 + mCompleteRepository.mDataSource.size))
        }
    }

    private fun getProgressSize(): Int { //获取传输条目占了多少个
        return mProgressRepository.mDataSource.size + mProgressItemSupple
    }

    //------------------- 这一块是为了接受传输的任务进行刷新的  因为通过Repository 所以不需要额外加锁同步
    //-------------------
    //-------------------
    fun initTransmissionLiveData() {
        DownloadManagerR.setCallBack(mDownloadCallBack)
        UploadManagerR.setCallBack(mUploadCallBack)
    }

    private val mDownloadCallBack = object : CallBack<TransferRefreshEvent<DownloadElement>> {
        override fun onCallBack(t: TransferRefreshEvent<DownloadElement>) {
            dealCallBack(t.refreshType, t.transferElement)
        }
    }

    private val mUploadCallBack = object : CallBack<TransferRefreshEvent<UploadElement>> {
        override fun onCallBack(t: TransferRefreshEvent<UploadElement>) {
            dealCallBack(t.refreshType, t.transferElement)
        }
    }

    private fun dealCallBack(type: Int, transferElement: TransferElement) {
        AppExecutors.instance.mainThread().execute {
            when (type) {
                TRANSFER_TRANSMISSION -> {
                    L.i(transferElement.tag, "TRANSFER_TRANSMISSION", "TransmissionViewModelR", "nwq", "2021/3/19");
                    mProgressRepository.updateTransmission(transferElement)
                }
                TRANSFER_COMPLETE -> {
                    mProgressRepository.deleteComplete(transferElement)
                }
                TRANSFER_FAIL -> {
                    L.i(transferElement.tag, "TRANSFER_FAIL", "TransmissionViewModelR", "nwq", "2021/3/19");
                    mProgressRepository.updateFail(transferElement)
                }
                TRANSFER_START, TRANSFER_CANCELED, TRANSFER_PAUSE -> {
                }//用户的自发事件已经UI已经处理了，所以这里不额外处理了
            }
        }
    }
}