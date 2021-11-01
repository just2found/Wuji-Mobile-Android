package net.sdvn.nascommon.model.oneos.transfer_r

import android.util.Log
import androidx.lifecycle.MutableLiveData
import net.sdvn.nascommon.model.oneos.transfer_r.interfaces.CallBack
import net.sdvn.nascommon.model.oneos.transfer_r.interfaces.Repository
import net.sdvn.nascommon.model.oneos.transfer_r.interfaces.Repository.Companion.DELETE_DATA
import net.sdvn.nascommon.model.oneos.transfer_r.interfaces.Repository.Companion.INSERT_DATA
import net.sdvn.nascommon.model.oneos.transfer_r.interfaces.Repository.Companion.NO_MORE_DATA
import net.sdvn.nascommon.model.oneos.transfer_r.interfaces.Repository.Companion.PAGE_SIZE
import net.sdvn.nascommon.model.oneos.transfer_r.interfaces.Repository.Companion.SOURCE_DATA
import net.sdvn.nascommon.model.oneos.transfer_r.interfaces.Repository.Companion.UPDATE_DATA
import net.sdvn.nascommon.model.oneos.transfer_r.interfaces.RepositoryData
import net.sdvn.nascommon.model.oneos.transfer_r.interfaces.RepositoryOperation
import java.util.concurrent.locks.ReentrantLock


//mKeyStr这个用来存设备ID
abstract class BasicRepository<T : RepositoryData>(var mKey: String?) : Repository<T> {

    override var version = Int.MIN_VALUE //是否需要同步数据的版本号
    override var isInit = false //是否进行了初始化

    override val mLiveDataRefreshEvent = MutableLiveData<DataRefreshEvent>()//可以带生命周期的回调
    private var mCallBack: CallBack<DataRefreshEvent>? = null//直接回调监听 一般不推荐使用

    override fun setCallBack(callBack: CallBack<DataRefreshEvent>) {
        mCallBack = callBack
    }

    override var mDataSource: MutableList<T> = mutableListOf() //数据的存储类
    protected var mRepositoryOperation: RepositoryOperation<T>? = null//数据的实际操作类

    abstract fun getLock(): ReentrantLock //为了安全所以操作进行加锁

    override fun deleteDataList(list: List<T>?) {
        list?.forEach {
            deleteData(it)
        }
    }

    override fun reLoadData() {
        mRepositoryOperation?.loadData(object : CallBack<List<T>> {
            override fun onCallBack(t: List<T>) {
                mDataSource.clear()
                mDataSource.addAll(t)
                notifyDataSource()
            }
        })
    }

    override fun releaseData() {

    }

    override fun loadMoreData() {
        mRepositoryOperation?.loadMoreData(mDataSource.size.toLong(), PAGE_SIZE, object : CallBack<List<T>> {
            override fun onCallBack(t: List<T>) {
                if (t.isNullOrEmpty()) {
                    notifyNoMoreData()
                } else {
                    addDataList(t)
                }
            }
        })
    }

    override fun addOrUpdateList(list: List<T>?) {
        if (list == null)
            return
        var itemCount = 0
        for (t in list) {
            val i = findDataPositionS(t)
            if (i == -1) {
                addOrUpdate(t)
            } else {
                addDataS(t)
                itemCount++
            }
        }
        notifyDataObserver(INSERT_DATA, mDataSource.size - itemCount, itemCount)
    }


    override fun updateData(t: T?): Boolean {
        var b = false
        if (t != null) {
            val i = findDataPositionS(t)
            if (i != -1) {
                mDataSource.removeAt(i)
                mDataSource.add(i, t)
                notifyDataObserver(UPDATE_DATA, i)
                b = true
            }
        }
        return b
    }

    override fun addDataList(list: List<T>?) {
        //插入列表的时候即便是空也会进行通知，让接收者结束更多加载状态
        if (list == null) {
            return
        }
        var itemCount = 0
        for (t in list) {
            val i = findDataPositionS(t)
            if (i == -1) {
                addDataS(t)
                itemCount++
            }
        }
        notifyDataObserver(INSERT_DATA, mDataSource.size - itemCount, itemCount)
    }

    override fun addDataList(list: List<T>?, position: Int) {
        //插入列表的时候即便是空也会进行通知，让接收者结束更多加载状态
        if (list == null) {
            return
        }
        var insertPosition = position
        if (mDataSource!!.size < position) return
        for (t in list) {
            val i = findDataPositionS(t)
            if (i == -1) {
                addDataS(t, insertPosition)
                insertPosition++
            }
        }
        notifyDataObserver(INSERT_DATA, position, insertPosition - position)
    }

    /**
     * @param t        需要插入的数据
     * @param position 位置
     * @return
     */
    override fun addData(t: T?, position: Int): Boolean {
        if (t == null || position > mDataSource!!.size) return false
        val i = findDataPositionS(t)
        if (i == -1) {
            addDataS(t, position)
            notifyDataObserver(INSERT_DATA, position)
            return true
        }
        return false
    }

    fun addOrUpdate(t: T?) {
        if (t == null) return
        val position = findDataPositionS(t)
        if (position == -1) {
            addDataS(t)
            notifyDataObserver(INSERT_DATA, mDataSource!!.size - 1)
        } else {
            deleteDataS(position)
            addDataS(t, position)
            notifyDataObserver(UPDATE_DATA, position)
        }
    }

    override fun addOrUpdate(t: T?, insertPosition: Int) {
        if (t == null || insertPosition > mDataSource!!.size) return
        val position = findDataPositionS(t)
        if (position == -1) {
            addDataS(t, insertPosition)
            notifyDataObserver(INSERT_DATA, insertPosition)
        } else if (position == insertPosition) {
            updateData(t)
        } else {
            deleteData(t)
            addData(t, insertPosition)
        }
    }

    /**
     * @param t 需要插入的数据
     * @return
     */
    override fun addData(t: T?): Boolean {
        if (t == null) return false
        val i = findDataPositionS(t)
        if (i == -1) {
            try {
                addDataS(t)
                notifyDataObserver(INSERT_DATA, mDataSource!!.size - 1)
            } catch (e: Exception) {
                Log.e("addData", e.toString())
            }
            return true
        }
        return false
    }

    /**
     * @param t
     * @return
     */
    override fun deleteData(t: T?): Boolean {
        if (t == null) return false
        val i = findDataPositionS(t)
        if (i != -1) {
            deleteDataS(i)
            notifyDataObserver(DELETE_DATA, i)
            return true
        }
        return false
    }

    override fun setRepositoryOperation(repositoryOperation: RepositoryOperation<T>?) {
        mRepositoryOperation = repositoryOperation
    }

    override fun getRepositoryOperation(): RepositoryOperation<T>? {
        return mRepositoryOperation
    }

    //以S结尾的方法都是不进行校验，直接执行逻辑的
    protected fun findDataPositionS(t: T): Int {
        var position = -1
        for ((p, d) in mDataSource.withIndex()) {
            if (t.isSameData(d)) {
                position = p
            }
        }

        return position
    }


    //以S结尾的方法都是不进行校验，直接执行逻辑的
    protected open fun findDataObjectS(t: T): T? {
        var result: T? = null
        for (d in mDataSource) {
            if (d.isSameData(t)) {
                result = d
            }
        }
        return result
    }

    protected fun findDataPosition(tag: String): Int {
        var position = -1
        for ((p, d) in mDataSource.withIndex()) {
            if (d.equalByTag(tag)) {
                position = p
            }
        }
        return position
    }


    //以S结尾的方法都是不进行校验，直接执行逻辑的
    protected fun findDataObjectS(tag: String): T? {
        var result: T? = null
        for (d in mDataSource) {
            if (d.equalByTag(tag)) {
                result = d
            }
        }
        return result
    }


    protected fun addDataS(t: T, position: Int) {
        mDataSource?.add(position, t)
    }

    protected fun addDataS(t: T): Boolean {
        return mDataSource?.add(t) ?: false
    }

    protected fun deleteDataS(position: Int) {
        mDataSource?.removeAt(position)
    }


    protected fun notifyNoMoreData() {
        notifyDataObserver(NO_MORE_DATA, 0, 0)
    }

    /**
     * 通知数据改变
     */
    protected fun notifyDataSource() {
        notifyDataObserver(SOURCE_DATA, 0, 0)
    }

    /**
     * 通知数据改变
     */
    /**
     * 通知数据改变
     */
    protected fun notifyDataObserver(RefreshType: Int, StartPosition: Int, itemCount: Int = 1) {
        if (version < Int.MAX_VALUE) {
            version++
        } else {
            version = Int.MIN_VALUE
        }
        val dataRefreshEvent = DataRefreshEvent(dataVersion = version, refreshType = RefreshType, startPosition = StartPosition, itemCount = itemCount)
        mLiveDataRefreshEvent.postValue(dataRefreshEvent)
        mCallBack?.onCallBack(dataRefreshEvent)
    }


}