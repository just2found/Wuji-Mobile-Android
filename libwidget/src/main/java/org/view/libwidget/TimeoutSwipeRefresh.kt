package org.view.libwidget

import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout.OnRefreshListener

fun <T : SwipeRefreshLayout> T.setOnRefreshWithTimeoutListener(listener: OnRefreshListener, time: Long = 3000) {
    val mActionStop = Runnable {
//        Timber.d("OnRefreshWithTimeout --> mActionStop done")
        if (isRefreshing) {
            isRefreshing = false
        }
    }
    setOnRefreshListener(object : OnRefreshListener {
        init {
            onRefreshListenerTag = this
        }

        override fun onRefresh() {
//            Timber.d("OnRefreshWithTimeout --> mActionStop remove")
            removeCallbacks(mActionStop)
            listener.onRefresh()
//            Timber.d("OnRefreshWithTimeout --> mActionStop do delay")
            postDelayed(mActionStop, time)
        }
    })
}

fun <T : SwipeRefreshLayout> T.showRefreshAndNotify() {
//    Timber.d("OnRefreshWithTimeout --> showRefreshAndNotify")
    if (!isRefreshing) {
        isRefreshing = true
        onRefreshListenerTag?.onRefresh()
    }
}

var <T : SwipeRefreshLayout> T.onRefreshListenerTag: OnRefreshListener?
    set(value) = setTag(1766616666, value)
    get() = getTag(1766616666) as? OnRefreshListener