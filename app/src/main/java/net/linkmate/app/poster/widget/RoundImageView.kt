package net.linkmate.app.poster.widget

import android.content.Context
import android.graphics.Path
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.widget.ImageView
import net.linkmate.app.R
import net.linkmate.app.poster.utils.BitmapUtils

class RoundImageView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ImageView(context, attrs, defStyleAttr) {
    private val mRoundedRectPath = Path()
    private var width = 0f
    private var height = 0f
    private var isClip = false
    private var typedArray = context.obtainStyledAttributes(attrs, R.styleable.RoundImageView)
    private val mRadius = typedArray.getDimension(R.styleable.RoundImageView_radius, 12f)

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        width = measuredWidth * 1.0f
        height = measuredHeight * 1.0f
    }

    override fun setImageDrawable(drawable: Drawable?) {
        var mDrawable = drawable
        drawable?.let {
            mDrawable = BitmapUtils().roundDrawableByDrawable(
                drawable,
                width.toInt(),
                height.toInt(),
                mRadius,
                resources
            )
        }
        super.setImageDrawable(mDrawable)
    }

    /*override fun onDraw(canvas: Canvas) {
        if (!isClip) {
            isClip = true
            mRoundedRectPath.reset()
            mRoundedRectPath.moveTo(mRadius, 0f)
            mRoundedRectPath.lineTo(width - mRadius, 0f)
            mRoundedRectPath.quadTo(width, 0f, width, mRadius)
            mRoundedRectPath.lineTo(width, height - mRadius)
            mRoundedRectPath.quadTo(width, height, width - mRadius, height)
            mRoundedRectPath.lineTo(mRadius, height)
            mRoundedRectPath.quadTo(0f, height, 0f, height - mRadius)
            mRoundedRectPath.lineTo(0f, mRadius)
            mRoundedRectPath.quadTo(0f, 0f, mRadius, 0f)
        }
        try {
            canvas.clipPath(mRoundedRectPath)
            super.onDraw(canvas)
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }*/
}