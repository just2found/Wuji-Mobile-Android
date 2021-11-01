package net.sdvn.nascommon.model.oneos.transfer_r.interfaces

/**
 * 这个类是想将具体的数据加载过程外接出去
 *
 * @param <T>
</T> */
interface RepositoryOperation<T : RepositoryData?> {
    fun loadData(callBack: CallBack<List<T>>?) //刚开始时候的加载数据
    fun loadMoreData(offset: Long, limit: Long, callBack: CallBack<List<T>>?) //加载更多数据
    fun getMaxCount(): Long //获取最大数量
}