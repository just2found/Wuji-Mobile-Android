package net.linkmate.app.ui.simplestyle.dynamic.video

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import com.lxj.xpopup.photoview.PhotoView
import com.lxj.xpopup.widget.PhotoViewContainer

/**
 * @author Raleigh.Luo
 * date：21/1/26 17
 * describe：
 */
class PhotoViewContainer2 : PhotoViewContainer {
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context) : super(context)

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        val currentView = viewPager.getChildAt(viewPager.getCurrentItem())
        if (currentView is PhotoView) {
            return super.onInterceptTouchEvent(ev)
        } else {//视频，不拦截
            return false
        }
    }


}