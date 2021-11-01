package net.linkmate.app.view;


import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;

import net.linkmate.app.R;
import net.linkmate.app.util.Dp2PxUtils;


public class ProgressView extends View {
    private int mDotCount = 5; // 圆点个数
    private int mDotColor = 0xFFFF9966;// 圆点颜色

    private Paint mPaint;

    private int mRingRadius = 14;// 圆环半径，单位dp
    private int mOriginalRingRadius;// 保存的原始圆环半径，单位dp
    private int mDotRadius = 3; // 小点半径，单位dp
    private int mOriginalDotRadius; // 保存的原始小点半径，单位dp

    private int mCurrentAngle = 0; // 当前旋转的角度

    private ValueAnimator mAnimator;// 旋转动画
    private int mMinRingRadius;

    public ProgressView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.ProgressView);
        mDotColor = ta.getColor(R.styleable.ProgressView_dot_color, mDotColor);
        mDotCount = ta.getInt(R.styleable.ProgressView_dot_count, mDotCount);
        ta.recycle();

        init();
    }

    public ProgressView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ProgressView(Context context) {
        this(context, null);
    }

    private void init() {
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        mPaint.setColor(mDotColor);

        // 屏幕适配，转化圆环半径，小点半径
        mOriginalRingRadius = Dp2PxUtils.dp2px(getContext(), mRingRadius);
        mOriginalDotRadius = Dp2PxUtils.dp2px(getContext(), mDotRadius);

        initAnimator();
    }

    private void initAnimator() {
        mAnimator = ValueAnimator.ofInt(0, 359);
        mAnimator.setDuration(6000);
        mAnimator.setRepeatCount(-1);
        mAnimator.setRepeatMode(ValueAnimator.RESTART);
        mAnimator.setInterpolator(new LinearInterpolator());

        mAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mCurrentAngle = (int) animation.getAnimatedValue();
//				LogUtils.d( ProgressView.this, "mCurrentAngle: "+ mCurrentAngle );
                invalidate();
            }
        });
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        // 重设圆环半径，防止超出视图大小
        int effectiveWidth = getWidth() - getPaddingLeft() - getPaddingRight();
        int effectiveHeight = getHeight() - getPaddingBottom() - getPaddingTop();
        int maxRadius = Math.min(effectiveWidth / 2, effectiveHeight / 2) - mDotRadius;

        mRingRadius = mRingRadius > maxRadius ? maxRadius : mOriginalRingRadius;
//		LogUtils.d( this, "mRingRadius: 前 "+ mRingRadius );
        //限制最小圆环大小
        mMinRingRadius = (int) (mDotCount * mDotRadius / Math.PI + 0.5f);
        if (mRingRadius < mMinRingRadius) mRingRadius = mMinRingRadius * 4 / 5;
        mOriginalRingRadius = mRingRadius;
//		LogUtils.d( this, "mRingRadius: "+ mRingRadius );
//		LogUtils.d( this, "effectiveWidth: "+ effectiveWidth );
//		LogUtils.d( this, "effectiveHeight: "+ effectiveHeight );
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
//		LogUtils.d( this, "widthMeasureSpec: "+ getMeasuredWidth() );
//		LogUtils.d( this, "heightMeasureSpec: "+ getMeasuredHeight() );
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // 根据小球总数平均分配整个圆，得到每个小球的间隔角度
        double cellAngle = 360 / mDotCount;

        for (int i = 0; i < mDotCount; i++) {
            double ange = i * cellAngle + mCurrentAngle;

            // 根据当前角度计算小球到圆心的距离
            calculateRadiusFromProgress();

            // 根据角度绘制单个小球
            drawDot(canvas, ange * 2 * Math.PI / 360);
        }
    }

    /**
     * 根据当前旋转角度计算mRingRadius、mDotRadius的值
     * mCurrentAngle:   0 - 180 - 360
     * mRingRadius:     最小 - 最大 - 最小
     */
    private void calculateRadiusFromProgress() {
        float fraction = 1.0f * mCurrentAngle / 180 - 1;
        fraction = Math.abs(fraction);

        mRingRadius = evaluate(fraction, mOriginalRingRadius, mMinRingRadius);
        mDotRadius = evaluate(fraction, mOriginalDotRadius, mOriginalDotRadius * 4 / 5);
    }

    // fraction：当前的估值器计算值,startValue:起始值,endValue:终点值
    private Integer evaluate(float fraction, Integer startValue, Integer endValue) {
        return (int) (startValue + fraction * (endValue - startValue));
    }


    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        startAnimation();
    }

    private void drawDot(Canvas canvas, double angle) {
        // 根据当前角度获取x、y坐标点
        float x = (float) (getWidth() / 2 + mRingRadius * Math.sin(angle));
        float y = (float) (getHeight() / 2 - mRingRadius * Math.cos(angle));

        // 绘制圆
        canvas.drawCircle(x, y, mDotRadius, mPaint);
    }

    public void startAnimation() {
        mAnimator.start();
    }

    public void stopAnimation() {
        mAnimator.end();
    }

    //销毁页面时停止动画
    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        stopAnimation();
    }
}

