package net.linkmate.app.poster.widget

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.widget.ImageView
import net.linkmate.app.R
import net.linkmate.app.poster.utils.BitmapUtils


class RatioImageView : ImageView {

    /* 优先级从大到小：
     mIsWidthFitDrawableSizeRatio mIsHeightFitDrawableSizeRatio
     mWidthRatio mHeightRatio
     即如果设置了mIsWidthFitDrawableSizeRatio为true，则优先级较低的三个值不生效 */

    /* 优先级从大到小：
     mIsWidthFitDrawableSizeRatio mIsHeightFitDrawableSizeRatio
     mWidthRatio mHeightRatio
     即如果设置了mIsWidthFitDrawableSizeRatio为true，则优先级较低的三个值不生效 */
    private var mDrawableSizeRatio = -1f // src图片(前景图)的宽高比例

    // 根据前景图宽高比例测量View,防止图片缩放变形
    private var mIsWidthFitDrawableSizeRatio = false // 宽度是否根据src图片(前景图)的比例来测量（高度已知）
    private var mIsHeightFitDrawableSizeRatio = false // 高度是否根据src图片(前景图)的比例来测量（宽度已知）

    // 宽高比例
    private var mWidthRatio = -1f // 宽度 = 高度*mWidthRatio

    private var mHeightRatio = -1f // 高度 = 宽度*mHeightRatio

    private var width = 0f
    private var height = 0f
    private var mRadius = 12f


    constructor(mContext: Context) : this(mContext, null)

    constructor(mContext: Context, attrs: AttributeSet?) : this(mContext, attrs!!, 0)

    constructor(mContext: Context, attrs: AttributeSet, defStyleAttr: Int) : super(
        mContext,
        attrs,
        defStyleAttr
    ) {
        init(attrs)
        // 一定要有此代码
        if (drawable != null) {
            mDrawableSizeRatio = (1f * drawable.intrinsicWidth
                    / drawable.intrinsicHeight)
        }

    }

    /**
     * 初始化变量
     */
    private fun init(attrs: AttributeSet) {
        val a = context.obtainStyledAttributes(
            attrs,
            R.styleable.RatioImageView
        )
        mIsWidthFitDrawableSizeRatio = a.getBoolean(
            R.styleable.RatioImageView_is_width_fix_drawable_size_ratio,
            mIsWidthFitDrawableSizeRatio
        )
        mIsHeightFitDrawableSizeRatio = a.getBoolean(
            R.styleable.RatioImageView_is_height_fix_drawable_size_ratio,
            mIsHeightFitDrawableSizeRatio
        )
        mHeightRatio = a.getFloat(
            R.styleable.RatioImageView_height_to_width_ratio, mHeightRatio
        )
        mWidthRatio = a.getFloat(
            R.styleable.RatioImageView_width_to_height_ratio, mWidthRatio
        )
        mRadius = a.getDimension(R.styleable.RatioImageView_image_radius, mRadius)
        a.recycle()
    }

    override fun setImageResource(resId: Int) {
        super.setImageResource(resId)
        if (drawable != null) {
            mDrawableSizeRatio = (1f * drawable.intrinsicWidth
                    / drawable.intrinsicHeight)
            if (mDrawableSizeRatio > 0
                && (mIsWidthFitDrawableSizeRatio || mIsHeightFitDrawableSizeRatio)
            ) {
                requestLayout()
            }
        }
    }

    override fun setImageDrawable(drawable: Drawable?) {
        /*var mDrawable = drawable
        drawable?.let {
            mDrawable = BitmapUtils().roundDrawableByDrawable(
                    drawable,
                    width.toInt(),
                    height.toInt(),
                    mRadius,
                    resources
            )
        }*/
        super.setImageDrawable(drawable)
        if (getDrawable() != null) {
            mDrawableSizeRatio = (1f * getDrawable().intrinsicWidth
                    / getDrawable().intrinsicHeight)
            if (mDrawableSizeRatio > 0
                && (mIsWidthFitDrawableSizeRatio || mIsHeightFitDrawableSizeRatio)
            ) {
                requestLayout()
            }
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        // 优先级从大到小：
        // mIsWidthFitDrawableSizeRatio mIsHeightFitDrawableSizeRatio
        // mWidthRatio mHeightRatio

        // 优先级从大到小：
        // mIsWidthFitDrawableSizeRatio mIsHeightFitDrawableSizeRatio
        // mWidthRatio mHeightRatio
        if (mDrawableSizeRatio > 0) {
            // 根据前景图宽高比例来测量view的大小
            if (mIsWidthFitDrawableSizeRatio) {
                mWidthRatio = mDrawableSizeRatio
            } else if (mIsHeightFitDrawableSizeRatio) {
                mHeightRatio = 1 / mDrawableSizeRatio
            }
        }

        if (mHeightRatio > 0 && mWidthRatio > 0) {
            throw RuntimeException("高度和宽度不能同时设置百分比！！")
        }

        if (mWidthRatio > 0) { // 高度已知，根据比例，设置宽度
            val height = MeasureSpec.getSize(heightMeasureSpec)
            super.onMeasure(
                MeasureSpec.makeMeasureSpec(
                    (height * mWidthRatio).toInt(), MeasureSpec.EXACTLY
                ),
                MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY)
            )
        } else if (mHeightRatio > 0) { // 宽度已知，根据比例，设置高度
            val width = MeasureSpec.getSize(widthMeasureSpec)
            super.onMeasure(
                MeasureSpec.makeMeasureSpec(
                    width,
                    MeasureSpec.EXACTLY
                ), MeasureSpec.makeMeasureSpec(
                    (width * mHeightRatio).toInt(), MeasureSpec.EXACTLY
                )
            )
        } else { // 系统默认测量
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        }

        width = measuredWidth * 1.0f
        height = measuredHeight * 1.0f

    }

}