package net.sdvn.nascommon.model.oneos.event

interface ReadyTask :TaskOps{
    fun isReady():Boolean
}