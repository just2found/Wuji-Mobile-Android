package net.linkmate.app.view


import android.content.Context
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import net.linkmate.app.R


/**自定义textview 图标样式
 * @author raleigh
 * @date 2015-02-09
 */
class CusTextView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : AppCompatTextView(context, attrs) {
    var leftHeight = -1
    var leftWidth = -1
    var rightHeight = -1
    var rightWidth = -1
    var topHeight = -1
    var topWidth = -1
    var bottomHeight = -1
    var bottomWidth = -1
    var isDrawableCenter=false

    init {
        val a = context.obtainStyledAttributes(attrs, R.styleable.CusTextViewAttrs)
        val count = a.indexCount
        var index = 0
        for (i in 0..count - 1) {
            index = a.getIndex(i)
            if (index == R.styleable.CusTextViewAttrs_bottomDrawableHeight)
                bottomHeight = a.getDimensionPixelSize(index, -1)
            if (index == R.styleable.CusTextViewAttrs_bottomDrawableWidth)
                bottomWidth = a.getDimensionPixelSize(index, -1)
            if (index == R.styleable.CusTextViewAttrs_startDrawableHeight)
                leftHeight = a.getDimensionPixelSize(index, -1)
            if (index == R.styleable.CusTextViewAttrs_startDrawableWidth)
                leftWidth = a.getDimensionPixelSize(index, -1)
            if (index == R.styleable.CusTextViewAttrs_endDrawableHeight)
                rightHeight = a.getDimensionPixelSize(index, -1)
            if (index == R.styleable.CusTextViewAttrs_endDrawableWidth)
                rightWidth = a.getDimensionPixelSize(index, -1)
            if (index == R.styleable.CusTextViewAttrs_topDrawableHeight)
                topHeight = a.getDimensionPixelSize(index, -1)
            if (index == R.styleable.CusTextViewAttrs_topDrawableWidth)
                topWidth = a.getDimensionPixelSize(index, -1)
            if(index == R.styleable.CusTextViewAttrs_drawableCenter)
                isDrawableCenter=a.getBoolean(index,false)
        }

        val drawables = compoundDrawablesRelative
        var dir = 0
        // 0-left; 1-top; 2-right; 3-bottom;
        for (drawable in drawables) {
            setImageSize(drawable, dir++)
        }
        setCompoundDrawablesRelative(drawables[0], drawables[1], drawables[2], drawables[3])
    }
    var bodyWidth:Float=-1f

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        //图片和文字居中
        if(isDrawableCenter) {
            val drawables = compoundDrawablesRelative
            if (drawables != null) {
                for(i in 0..drawables.size-1){
                    drawcenter(drawables[i],i)
                }
            }
        }
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas?.translate((width - bodyWidth) / 2, 0f)
    }
    private fun drawcenter(drawable: Drawable?,position: Int){
        if (drawable != null) {
            val textWidth = paint.measureText(text.toString())
            val drawablePadding = compoundDrawablePadding
            var drawableWidth =  2*drawable.intrinsicWidth
            bodyWidth = textWidth + drawableWidth.toFloat() + drawablePadding.toFloat()
            val padding=((width - bodyWidth)/2).toInt()
            setPadding(if(position==0||position==2)padding else paddingLeft,
                    if(position==1||position==3)padding else paddingTop,
                    if(position==0||position==2)padding else paddingRight,
                    if(position==1||position==3)padding else paddingBottom)
        }
    }

    fun setTopDrawable(drawable: Drawable?) {
        val drawables = compoundDrawablesRelative
        drawable?.setBounds(0, 0, topWidth, topHeight)
        setCompoundDrawablesRelative(drawables[0], drawable, drawables[2], drawables[3])
    }

    fun setStartDrawable(drawable: Drawable?) {
        val drawables = compoundDrawablesRelative
        drawable?.setBounds(0, 0, leftWidth, leftHeight)
        setCompoundDrawablesRelative(drawable, drawables[1], drawables[2], drawables[3])
    }

    fun setEndDrawable(drawable: Drawable?) {
        val drawables = compoundDrawablesRelative
        drawable?.setBounds(0, 0, rightWidth, rightHeight)
        setCompoundDrawablesRelative(drawables[0], drawables[1], drawable, drawables[3])
    }

    fun setBottomDrawable(drawable: Drawable?) {
        val drawables = compoundDrawablesRelative
        drawable?.setBounds(0, 0, bottomWidth, bottomHeight)
        setCompoundDrawablesRelative(drawables[0], drawables[1], drawables[2], drawable)
    }

    private fun setImageSize(d: Drawable?, dir: Int) {
        if (d == null) {
            return
        }

        var height = -1
        var width = -1
        when (dir) {
            0 -> {
                // left
                height = leftHeight
                width = leftWidth
            }
            1 -> {
                // top
                height = topHeight
                width = topWidth
            }
            2 -> {
                // right
                height = rightHeight
                width = rightWidth
            }
            3 -> {
                // bottom
                height = bottomHeight
                width = bottomWidth
            }
        }
        if (width != -1 && height != -1) {
            d.setBounds(0, 0, width, height)
        }
    }


}
