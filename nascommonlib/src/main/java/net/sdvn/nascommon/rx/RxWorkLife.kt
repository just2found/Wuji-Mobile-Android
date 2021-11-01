package net.sdvn.nascommon.rx

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent

/** 

Created by admin on 2020/11/18,11:00

 */
class RxWorkLife(var life: LifecycleOwner) : RxWork(), LifecycleObserver {
    init {
        life.lifecycle.addObserver(this)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onStop() {
        clear()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun onDestroy() {
        life.lifecycle.removeObserver(this)
        dispose()
    }

}