package net.sdvn.nascommon.model.oneos.transfer_r.interfaces

interface RepositoryData {
    fun isSameData(repositoryData: RepositoryData?): Boolean //判断是不是同一个数据的

    fun equalByTag(tag: Any): Boolean //判断是不是同一个数据的
}