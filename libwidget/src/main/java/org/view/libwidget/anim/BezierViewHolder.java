package org.view.libwidget.anim;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.TypeEvaluator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.os.Build;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

@SuppressLint("AppCompatCustomView")
public class BezierViewHolder extends TextView implements ValueAnimator.AnimatorUpdateListener {

    public static final int VIEW_SIZE = 20;

    protected Paint mPaint4Circle;
    protected int radius;

    protected Point startPosition;
    protected Point endPosition;


    public BezierViewHolder(@NonNull Context context) {
        this(context, null);
    }

    public BezierViewHolder(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BezierViewHolder(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mPaint4Circle = new Paint();
        mPaint4Circle.setColor(Color.RED);
        mPaint4Circle.setAntiAlias(true);

        setGravity(Gravity.CENTER);
        setTextColor(Color.WHITE);
        setTextSize(12);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            setTranslationZ(1000);
        }
    }


    public void setStartPosition(@NonNull Point startPosition) {
        startPosition.y -= 10;
        this.startPosition = startPosition;
    }

    public void setEndPosition(@NonNull Point endPosition) {
        this.endPosition = endPosition;
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int PX4SIZE = (int) convertDpToPixel(VIEW_SIZE, getContext());
        setMeasuredDimension(PX4SIZE, PX4SIZE);
        radius = PX4SIZE / 2;
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        canvas.drawCircle(getMeasuredWidth() / 2, getMeasuredHeight() / 2, radius, mPaint4Circle);
        super.onDraw(canvas);
    }


    public void startBezierAnimation() {
        if (startPosition == null || endPosition == null) return;
        int pointX = (startPosition.x + endPosition.x) / 3;
        int pointY = (startPosition.y + endPosition.y) / 3;
        Point controlPoint = new Point(pointX, pointY);
        BezierEvaluator bezierEvaluator = new BezierEvaluator(controlPoint);
        ValueAnimator anim = ValueAnimator.ofObject(bezierEvaluator, startPosition, endPosition);
        anim.addUpdateListener(this);
        anim.setDuration(1000);
        anim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                ViewGroup viewGroup = (ViewGroup) getParent();
                viewGroup.removeView(BezierViewHolder.this);
            }
        });
        anim.setInterpolator(new AccelerateDecelerateInterpolator());
        anim.start();
    }

    @Override
    public void onAnimationUpdate(@NonNull ValueAnimator animation) {
        Point point = (Point) animation.getAnimatedValue();
        setX(point.x);
        setY(point.y);
        invalidate();
    }


    public static class BezierEvaluator implements TypeEvaluator<Point> {

        private Point controlPoint;

        public BezierEvaluator(@NonNull Point controlPoint) {
            this.controlPoint = controlPoint;
        }

        @Override
        public Point evaluate(float t, Point startValue, Point endValue) {
            int x = (int) ((1 - t) * (1 - t) * startValue.x + 2 * t * (1 - t) * controlPoint.x + t * t * endValue.x);
            int y = (int) ((1 - t) * (1 - t) * startValue.y + 2 * t * (1 - t) * controlPoint.y + t * t * endValue.y);
            return new Point(x, y);
        }
    }

    public static float convertDpToPixel(float dp, @NonNull Context context) {
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();

        float px = dp * (metrics.densityDpi / 160f);
        return px;
    }
}