package net.linkmate.app.poster.widget

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import net.linkmate.app.R


class CircleIndicatorLayout @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0)
    : ViewGroup(context, attrs, defStyleAttr) {
    private val circleWidth = 20      //圆点宽
    private val circleHeight = 20     //圆点高
    private val circlePadding = 10    //圆点padding
    private var currentIndex: Int = 0 //当前高亮圆点

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        // 计算出所有的childView的宽和高
        measureChildren(widthMeasureSpec, heightMeasureSpec);
        //测量并保存layout的宽高(使用getDefaultSize时，wrap_content和match_perent都是填充屏幕)
        //稍后会重新写这个方法，能达到wrap_content的效果
        setMeasuredDimension(getDefaultSize(suggestedMinimumWidth, widthMeasureSpec),
                getDefaultSize(suggestedMinimumHeight, heightMeasureSpec));
    }

    override fun onLayout(p0: Boolean, p1: Int, p2: Int, p3: Int, p4: Int) {
        val childrenWidth = circleWidth+circlePadding*2
        var left:Int
        var top:Int
        var right:Int
        var bottom:Int
        var layoutWidth = (width-childrenWidth*childCount)/2 // 容器已经占据的宽度

        var layoutHeight = (height-circleHeight)/2 // 容器已经占据的宽度

        var maxChildHeight = 0 //一行中子控件最高的高度，用于决定下一行高度应该在目前基础上累加多少

        for (i in 0 until childCount) {
            val child = getChildAt(i)
            //注意此处不能使用getWidth和getHeight，这两个方法必须在onLayout执行完，才能正确获取宽高
            if (layoutWidth < width) {
                //如果一行没有排满，继续往右排列
                left = layoutWidth+(childrenWidth-circleWidth)/2
                right = left + circleWidth
                top = layoutHeight
                bottom = top + circleHeight
            } else {
                //排满后换行
                layoutWidth = 0
                layoutHeight += maxChildHeight
                maxChildHeight = 0
                left = layoutWidth+(childrenWidth-circleWidth)/2
                right = left + circleWidth
                top = layoutHeight
                bottom = top + circleHeight
            }
            layoutWidth += childrenWidth //宽度累加
            if (circleHeight > maxChildHeight) {
                maxChildHeight = circleHeight
            }

            //确定子控件的位置，四个参数分别代表（左上右下）点的坐标值
            child.layout(left, top, right, bottom)
        }
    }

    fun setCount(count: Int, position: Int){
        if(count <= 1) return
        currentIndex = position
        removeAllViews()
        for (i in 0 until count) {
            val view = View(context)
            view.measure(circleWidth, circleHeight)
            view.setBackgroundResource(R.drawable.bg_circle_indicator)
            view.isEnabled = position==i
            addView(view)
        }
    }

    fun setCurrentIndex(position: Int){
        if(childCount == 0) return
        getChildAt(currentIndex).isEnabled = false
        getChildAt(position).isEnabled = true
        currentIndex = position
    }
}