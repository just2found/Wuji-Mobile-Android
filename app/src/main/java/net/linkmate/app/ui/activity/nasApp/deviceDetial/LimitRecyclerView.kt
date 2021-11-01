package net.linkmate.app.ui.activity.nasApp.deviceDetial

import android.content.Context
import android.graphics.Point
import android.util.AttributeSet
import android.view.Display
import android.view.WindowManager
import androidx.recyclerview.widget.RecyclerView


/**
 * @author Raleigh.Luo
 * date：20/9/23 19
 * describe：
 */
class LimitRecyclerView : RecyclerView {
    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    override fun onMeasure(widthSpec: Int, heightSpec: Int) {
        super.onMeasure(widthSpec, heightSpec)

        val measuredHeight1 =
                mHeight?.let {
                    Math.min(measuredHeight, it)
                } ?: let {
                    val wm = context
                            .getSystemService(Context.WINDOW_SERVICE) as WindowManager
                    val defaultDisplay: Display = wm.getDefaultDisplay()
                    val point = Point()
                    defaultDisplay.getSize(point)
                    Math.min(measuredHeight, point.y * 2 / 3)
                }

        setMeasuredDimension(measuredWidth, measuredHeight1)
    }

    var mHeight: Int? = null
}