package net.sdvn.nascommon.model.oneos.event

interface TaskOps :Runnable{
    fun isRunning(): Boolean
    fun stop()
}