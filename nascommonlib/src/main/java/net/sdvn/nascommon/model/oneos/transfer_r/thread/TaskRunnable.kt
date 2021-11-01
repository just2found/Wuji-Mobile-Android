package net.sdvn.nascommon.model.oneos.transfer_r.thread

import net.sdvn.nascommon.db.objecbox.TransferHistory
import net.sdvn.nascommon.model.oneos.transfer.TransferElement

/**
create by: 86136
create time: 2021/3/11 10:51
Function description:
 */
abstract class TaskRunnable<T : TransferElement>(protected val transferElement: T) : Runnable {

    fun getTag(): String {
        return transferElement.tag
    }

    fun getDeviceId(): String {
        return transferElement.devId
    }

    private var isInterrupt = false

    fun isInterrupt(): Boolean {
        return isInterrupt
    }

    fun interruptRunnable() {
        isInterrupt = true
        interrupt()
    }

    protected abstract fun interrupt()


}