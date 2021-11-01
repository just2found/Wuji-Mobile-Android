package org.view.libwidget

import androidx.recyclerview.widget.RecyclerView
import kotlin.math.abs

abstract class RecyelerViewScrollDetector : RecyclerView.OnScrollListener() {
    private var mLastScrollY = 0
    private var mScrollThreshold = 8
    abstract fun onScrollUp()
    abstract fun onScrollDown()

    /**
     * Callback method to be invoked when RecyclerView's scroll state changes.
     *
     * @param recyclerView The RecyclerView whose scroll state has changed.
     * @param newState     The updated scroll state. One of [RecyclerView.SCROLL_STATE_IDLE],
     * [RecyclerView.SCROLL_STATE_DRAGGING] or [RecyclerView.SCROLL_STATE_SETTLING].
     */
    override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {}

    /**
     * Callback method to be invoked when the RecyclerView has been scrolled. This will be
     * called after the scroll has completed.
     *
     *
     * This callback will also be called if visible item range changes after a layout
     * calculation. In that case, dx and dy will be 0.
     *
     * @param recyclerView The RecyclerView which scrolled.
     * @param dx           The amount of horizontal scroll.
     * @param dy           The amount of vertical scroll.
     */
    private var lastY = 0
    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
//        Timber.d("dy : $dy")
        lastY += dy
        if (abs(mLastScrollY - lastY) > mScrollThreshold) {
            mLastScrollY += lastY
            lastY = 0
            if (dy > 0) {
                onScrollDown()
            } else if (dy < 0) {
                onScrollUp()
            }
        }
    }

    fun setScrollThreshold(scrollThreshold: Int) {
        mScrollThreshold = scrollThreshold
    }

    private fun getTopItemScrollY(recyclerView: RecyclerView?): Int {
        if (recyclerView?.getChildAt(0) == null) return 0
        val topChild = recyclerView.getChildAt(0)
        return topChild.top
    }
}