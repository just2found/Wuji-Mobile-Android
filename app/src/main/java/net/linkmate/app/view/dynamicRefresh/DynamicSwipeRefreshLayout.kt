package net.linkmate.app.view.dynamicRefresh

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.animation.DecelerateInterpolator
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import net.linkmate.app.R

/**
 * @author Raleigh.Luo
 * date：21/1/21 15
 * describe：
 */
class DynamicSwipeRefreshLayout(context: Context, attrs: AttributeSet?) : SwipeRefreshLayout(context, attrs) {
    private var mTouchY = 0f
    private var mCurrentY = 0f
    var isLoadEnable = false //是否可下拉加载功能
    var isLoading = false
    private var onLoadListener: OnLoadListener? = null

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        val superIntercept = super.onInterceptTouchEvent(ev)
        //非正在刷新，且父类不拦截
        if (!superIntercept) {
            when (ev.action) {
                MotionEvent.ACTION_DOWN -> {
                    mTouchY = ev.y
                    mCurrentY = mTouchY
                }
                MotionEvent.ACTION_MOVE -> {
                    val currentY = ev.y
                    val dy = currentY - mTouchY
                    when {
                        dy > 0 && !canChildScrollUp() -> {

                        }
                        dy < 0 && !canChildScrollDown() -> {//下拉刷新
                            if (isLoadEnable) {
                                onLoadListener?.let {
                                    if (!isLoading) {
                                        isLoading = true
                                        onLoadListener?.onLoad()
                                    }
                                }
                            }
                        }
                    }
                }

            }
        }
        return superIntercept
    }

    private var decelerateInterpolator: DecelerateInterpolator = DecelerateInterpolator(1000f)
    override fun onTouchEvent(ev: MotionEvent): Boolean {
        val superTouch = super.onTouchEvent(ev)
        if (ev.action == MotionEvent.ACTION_MOVE) {
            if (mChildView != null) {
                //滑动下拉位移动最大
                val mWaveHeight = 2 * context.resources.getDimension(R.dimen.common_icon56)
                val currentY = ev.y

                var dy = currentY - mTouchY
                dy = Math.min(mWaveHeight * 2f, dy)
                dy = Math.max(0f, dy)
                val offsetY: Float = decelerateInterpolator.getInterpolation(dy / mWaveHeight / 2) * (dy / 2f)
                mChildView?.setTranslationY(offsetY)
            }
        }

        return superTouch
    }

    private var mNestedScrollView: View? = null
    private var mChildView: View? = null
    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        //滑动的view
        mNestedScrollView = findViewById(R.id.recyclerView)
        mChildView = getChildAt(0)
    }

    fun canChildScrollDown(): Boolean {
        if (mNestedScrollView == null) {
            return false
        }
        return mNestedScrollView?.canScrollVertically(1) ?: false
    }

    fun setOnLoadListener(onLoadListener: OnLoadListener?) {
        this.onLoadListener = onLoadListener
    }

    interface OnLoadListener {
        /**
         * Called when a swipe gesture triggers a refresh.
         */
        fun onLoad()
    }
}