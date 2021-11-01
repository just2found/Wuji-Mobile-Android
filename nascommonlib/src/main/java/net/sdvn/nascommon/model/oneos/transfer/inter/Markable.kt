package net.sdvn.nascommon.model.oneos.transfer.inter

/**
create by: 86136
create time: 2021/1/11 16:28
Function description:
 */
interface Markable<T> {

    fun setMark(t: T)

    fun getMark():T

}