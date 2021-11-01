package net.sdvn.nascommon.model.oneos.transfer_r.interfaces

import androidx.lifecycle.MutableLiveData
import net.sdvn.nascommon.model.oneos.transfer_r.DataRefreshEvent

interface Repository<T : RepositoryData?> {
    companion object {
        const val UNABLE_GET_MAX_COUNT = -100L
        const val PAGE_SIZE = 40L

        //这五种是刷新类型
        const val NO_MORE_DATA = -1//无更多数据
        const val SOURCE_DATA = 1//全部数据源进行更新
        const val UPDATE_DATA = 2
        const val INSERT_DATA = 3
        const val DELETE_DATA = 4
        const val REFRESH_DATA = 5 //单纯刷新数据
    }

    var version: Int
    var mDataSource: MutableList<T>
    var isInit: Boolean

    val mLiveDataRefreshEvent: MutableLiveData<DataRefreshEvent>  //这个主要是给UI使用，让UI进行特定的数据刷新
    fun setCallBack(callBack: CallBack<DataRefreshEvent>)
    fun deleteDataList(list: List<T>?)
    fun addOrUpdateList(list: List<T>?)
    fun addDataList(list: List<T>?)
    fun addDataList(list: List<T>?, position: Int)
    fun addData(t: T?, position: Int): Boolean
    fun addOrUpdate(t: T?, insertPosition: Int)
    fun addData(t: T?): Boolean
    fun updateData(t: T?): Boolean
    fun deleteData(t: T?): Boolean
    fun reLoadData()
    fun loadMoreData()
    fun setRepositoryOperation(repositoryOperation: RepositoryOperation<T>?)
    fun getRepositoryOperation(): RepositoryOperation<T>?
    fun releaseData()
}