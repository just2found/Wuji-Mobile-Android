package net.sdvn.nascommon.model.oneos.transfer.inter

/**
create by: 86136
create time: 2021/1/11 16:31
Function description:
 */

abstract class AffairRunnable : Runnable, Interruptible, Markable<String> {

    var mMark =""
    var isInterrupt = false;

    override fun setMark(t: String) {
        mMark = t
    }

    override fun getMark(): String {
        return mMark
    }

    override fun interrupt() {
        isInterrupt = true;
    }
}