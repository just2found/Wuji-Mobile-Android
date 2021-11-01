package net.linkmate.app.view;

import android.animation.IntEvaluator;
import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.FrameLayout;

import androidx.core.view.ViewCompat;
import androidx.customview.widget.ViewDragHelper;

/**
 * Created by JarryLeo on 2017/5/6.
 */

public class SlideExit extends FrameLayout {
    //支持往边滑动关闭Activity


    public static final int SLIDE_LEFT_EXIT = 1 << 0;
    public static final int SLIDE_RIGHT_EXIT = 1 << 1;
    public static final int SLIDE_UP_EXIT = 1 << 2;
    public static final int SLIDE_DOWN_EXIT = 1 << 3;
    private ViewDragHelper mDragHelper;
    private View mContentView;
    private int mSide;
    private Paint mPaint;
    private Activity mActivity;
    private IntEvaluator mEvaluator;


    /**
     * Minimum velocity that will be detected as a fling
     */
    private static final int MIN_FLING_VELOCITY = 400; // dips per second

    private static final int DEFAULT_SCRIM_COLOR = 0x99000000;

    private static final int FULL_ALPHA = 255;

    /**
     * Edge flag indicating that the left edge should be affected.
     */
    public static final int EDGE_LEFT = ViewDragHelper.EDGE_LEFT;

    /**
     * Edge flag indicating that the right edge should be affected.
     */
    public static final int EDGE_RIGHT = ViewDragHelper.EDGE_RIGHT;

    /**
     * Edge flag indicating that the bottom edge should be affected.
     */
    public static final int EDGE_BOTTOM = ViewDragHelper.EDGE_BOTTOM;

    /**
     * Edge flag set indicating all edges should be affected.
     */
    public static final int EDGE_ALL = EDGE_LEFT | EDGE_RIGHT | EDGE_BOTTOM;


    /**
     * Default threshold of scroll
     */
    private static final float DEFAULT_SCROLL_THRESHOLD = 0.3f;

    private static final int OVERSCROLL_DISTANCE = 10;

    private static final int[] EDGE_FLAGS = {
            EDGE_LEFT, EDGE_RIGHT, EDGE_BOTTOM, EDGE_ALL
    };

    private int mEdgeFlag;

    /**
     * Threshold of scroll, we will close the activity, when scrollPercent over
     * this value;
     */
    private float mScrollThreshold = DEFAULT_SCROLL_THRESHOLD;

    private boolean mEnable = true;


    private float mScrollPercent;

    private int mContentLeft;

    private int mContentTop;

    /**
     * The set of listeners to be sent events through.
     */

    private Drawable mShadowLeft;

    private Drawable mShadowRight;

    private Drawable mShadowBottom;

    private float mScrimOpacity;

    private int mScrimColor = DEFAULT_SCRIM_COLOR;

    private boolean mInLayout;

    private Rect mTmpRect = new Rect();

    /**
     * Edge being dragged
     */
    private int mTrackingEdge;


    public SlideExit(Context context) {
        this(context, null);
    }

    public SlideExit(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SlideExit(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }


    private void init() {
        mPaint = new Paint();
        mEvaluator = new IntEvaluator();
        mDragHelper = ViewDragHelper.create(this, mCallback);

        //上滑
        if ((mSide & SLIDE_UP_EXIT) == SLIDE_UP_EXIT) {
            mDragHelper.setEdgeTrackingEnabled(ViewDragHelper.EDGE_BOTTOM);
            mEdgeFlag = EDGE_BOTTOM;

        }
        //下滑
        if ((mSide & SLIDE_DOWN_EXIT) == SLIDE_DOWN_EXIT) {
            mDragHelper.setEdgeTrackingEnabled(ViewDragHelper.EDGE_TOP);
        }


        //左滑
        if ((mSide & SLIDE_LEFT_EXIT) == SLIDE_LEFT_EXIT) {
            mDragHelper.setEdgeTrackingEnabled(ViewDragHelper.EDGE_RIGHT);
            mEdgeFlag = EDGE_RIGHT;
        }
        //右滑
        if ((mSide & SLIDE_RIGHT_EXIT) == SLIDE_RIGHT_EXIT) {
            mDragHelper.setEdgeTrackingEnabled(ViewDragHelper.EDGE_LEFT);
            mEdgeFlag = EDGE_LEFT;
        }
        mDragHelper.setEdgeTrackingEnabled(mEdgeFlag);


    }


    ViewDragHelper.Callback mCallback = new ViewDragHelper.Callback() {

        private boolean mIsScrollOverValid;

        @Override
        public boolean tryCaptureView(View child, int pointerId) {


            //捕获子容器
            return child == mContentView;
        }

        @Override
        public void onViewReleased(View releasedChild, float xvel, float yvel) {
            //滑动边界1/4即关闭Activity
            float xDistance = getMeasuredWidth() / 3;
            float yDistance = getMeasuredHeight() / 4;
            //左右超过边界处理
            if (mContentView.getLeft() != 0) {
                if (mContentView.getLeft() < -xDistance) {
                    mDragHelper.smoothSlideViewTo(mContentView, -getMeasuredWidth(), 0);
                } else if (mContentView.getLeft() > xDistance) {
                    mDragHelper.smoothSlideViewTo(mContentView, getMeasuredWidth(), 0);
                } else {
                    //未超过则回弹
                    mDragHelper.smoothSlideViewTo(mContentView, 0, 0);
                }
            }
            //上下超过边界
            if (mContentView.getTop() != 0) {
                if (mContentView.getTop() < -yDistance) {
                    mDragHelper.smoothSlideViewTo(mContentView, 0, -getMeasuredHeight());
                } else if (mContentView.getTop() > yDistance) {
                    mDragHelper.smoothSlideViewTo(mContentView, 0, getMeasuredHeight());
                } else {
                    //未超过则回弹
                    mDragHelper.smoothSlideViewTo(mContentView, 0, 0);
                }
            }

            //刷新动画
            ViewCompat.postInvalidateOnAnimation(SlideExit.this);
        }

        @Override
        public int clampViewPositionVertical(View child, int top, int dy) {
            //上滑
            if ((mSide & SLIDE_UP_EXIT) == SLIDE_UP_EXIT && top < 0) {
                return top;
            }
            //下滑
            if ((mSide & SLIDE_DOWN_EXIT) == SLIDE_DOWN_EXIT && top > 0) {
                return top;
            }
            return 0;
        }

        @Override
        public int clampViewPositionHorizontal(View child, int left, int dx) {
            //左滑
            if ((mSide & SLIDE_LEFT_EXIT) == SLIDE_LEFT_EXIT && left < 0) {
                return left;
            }
            //右滑
            if ((mSide & SLIDE_RIGHT_EXIT) == SLIDE_RIGHT_EXIT && left > 0) {
                return left;
            }
            return 0;
        }

        @Override
        public void onViewPositionChanged(View changedView, int left, int top, int dx, int dy) {
            //绘制阴影刷新显示
            ViewCompat.postInvalidateOnAnimation(SlideExit.this);
        }


        @Override
        public int getViewHorizontalDragRange(View child) {
            return 1;
        }

        @Override
        public int getViewVerticalDragRange(View child) {
            return 1;
        }

        @Override
        public void onEdgeDragStarted(int edgeFlags, int pointerId) {
            mDragHelper.captureChildView(mContentView, pointerId);
        }
    };

    @Override
    public void computeScroll() {
        //滑动动画处理
        if (mDragHelper.continueSettling(true)) {
            //刷新显示
            ViewCompat.postInvalidateOnAnimation(this);
        } else {
            //滑动结束,关闭Activty
            if (Math.abs(mContentView.getLeft()) == getMeasuredWidth()
                    || Math.abs(mContentView.getTop()) == getMeasuredHeight()) {
                mActivity.finish();
            }
        }
    }

    public void setEnableGesture(boolean enable) {
        mEnable = enable;
    }

    /**
     * Enable edge tracking for the selected edges of the parent view. The
     * callback's
     * {@link ViewDragHelper.Callback#onEdgeTouched(int, int)}
     * and
     * {@link ViewDragHelper.Callback#onEdgeDragStarted(int, int)}
     * methods will only be invoked for edges for which edge tracking has been
     * enabled.
     *
     * @param edgeFlags Combination of edge flags describing the edges to watch
     * @see #EDGE_LEFT
     * @see #EDGE_RIGHT
     * @see #EDGE_BOTTOM
     */
    public void setEdgeTrackingEnabled(int edgeFlags) {
        mEdgeFlag = edgeFlags;
        mDragHelper.setEdgeTrackingEnabled(mEdgeFlag);
    }

    /**
     * Set a color to use for the scrim that obscures primary content while a
     * drawer is open.
     *
     * @param color Color to use in 0xAARRGGBB format.
     */
    public void setScrimColor(int color) {
        mScrimColor = color;
        invalidate();
    }

    /**
     * Set scroll threshold, we will close the activity, when scrollPercent over
     * this value
     *
     * @param threshold
     */
    public void setScrollThresHold(float threshold) {
        if (threshold >= 1.0f || threshold <= 0) {
            throw new IllegalArgumentException("Threshold value should be between 0 and 1.0");
        }
        mScrollThreshold = threshold;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        boolean handled = false;

        if (isEnabled()) {

            if (ev.getX() < 100 && mEdgeFlag == EDGE_LEFT
                    || ev.getX() > getMeasuredWidth() - 100 && mEdgeFlag == EDGE_RIGHT
                    || ev.getY() > getMeasuredHeight() - 120 && mEdgeFlag == EDGE_BOTTOM)//当x<100,即左侧边缘才起作用
                handled = mDragHelper.shouldInterceptTouchEvent(ev);
        } else {
            mDragHelper.cancel();
        }
        return !handled ? super.onInterceptTouchEvent(ev) : handled;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!mEnable) {
            return false;
        }
        //交给ViewDragHelper处理滑动事件
        mDragHelper.processTouchEvent(event);
        return true;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        mInLayout = true;
        if (mContentView != null)
            mContentView.layout(mContentLeft, mContentTop,
                    mContentLeft + mContentView.getMeasuredWidth(),
                    mContentTop + mContentView.getMeasuredHeight());
        mInLayout = false;
    }

    @Override
    public void requestLayout() {
        if (!mInLayout) {
            super.requestLayout();
        }
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        drawShadow(canvas);


    }

    private void drawShadow(Canvas canvas) {
        //绘制阴影
        if (mContentView.getTop() > 0) {
            Integer evaluate = mEvaluator.evaluate(mContentView.getTop() / mContentView.getMeasuredHeight() * 1.0f,
                    0, 100);
            mPaint.setColor(Color.argb(100 - evaluate, 0, 0, 0));
            //上边阴影
            canvas.drawRect(0, 0, mContentView.getMeasuredWidth(),
                    mContentView.getTop(), mPaint);
        } else if (mContentView.getTop() < 0) {
            Integer evaluate = mEvaluator.evaluate(-mContentView.getTop() / mContentView.getMeasuredHeight() * 1.0f,
                    0, 100);
            mPaint.setColor(Color.argb(100 - evaluate, 0, 0, 0));
            //下边阴影
            canvas.drawRect(0, mContentView.getMeasuredHeight() + mContentView.getTop(),
                    getMeasuredWidth(), getMeasuredHeight(), mPaint);
        }

        if (mContentView.getLeft() > 0) {
            Integer evaluate = mEvaluator.evaluate(mContentView.getLeft() * 1.0f / mContentView.getMeasuredWidth(),
                    0, 100);
            mPaint.setColor(Color.argb(100 - evaluate, 0, 0, 0));
            //左边阴影
            canvas.drawRect(0, 0, mContentView.getLeft(),
                    getMeasuredHeight(), mPaint);
        } else if (mContentView.getLeft() < 0) {
            Integer evaluate = mEvaluator.evaluate(-mContentView.getLeft() / mContentView.getMeasuredWidth() * 1.0f,
                    0, 100);
            mPaint.setColor(Color.argb(100 - evaluate, 0, 0, 0));
            //右边阴影
            canvas.drawRect(mContentView.getLeft() + getMeasuredWidth(), 0,
                    getMeasuredWidth(), getMeasuredHeight(), mPaint);
        }
    }

    public static void bind(Activity activity, int slide_side) {
        //创建本类对象并绑定Activity
        new SlideExit(activity).attach(activity, slide_side);
    }


    private void attach(Activity activity, int slide_side) {
        //滑动关闭方向
        mActivity = activity;
        mSide = slide_side;

        //获取Activity布局的父容器
        Window window = activity.getWindow();
        ViewGroup decorView = (ViewGroup) window.getDecorView();

        if (decorView.getChildCount() > 0) {
            //拿到Activity的contentView
            mContentView = decorView.getChildAt(0);
            decorView.removeAllViews();
            //把contentView添加到本容器
            this.removeAllViews();
            this.addView(mContentView);
            //把本容器添加到Activity的父容器
            decorView.addView(this);
        }
    }


    @Override
    protected boolean drawChild(Canvas canvas, View child, long drawingTime) {
        final boolean drawContent = child == mContentView;

        boolean ret = super.drawChild(canvas, child, drawingTime);
        if (mScrimOpacity > 0 && drawContent
                && mDragHelper.getViewDragState() != ViewDragHelper.STATE_IDLE) {
            drawShadow(canvas, child);
            drawScrim(canvas, child);
        }
        return ret;
    }

    private void drawScrim(Canvas canvas, View child) {
        final int baseAlpha = (mScrimColor & 0xff000000) >>> 24;
        final int alpha = (int) (baseAlpha * mScrimOpacity);
        final int color = alpha << 24 | (mScrimColor & 0xffffff);

        if ((mTrackingEdge & EDGE_LEFT) != 0) {
            canvas.clipRect(0, 0, child.getLeft(), getHeight());
        } else if ((mTrackingEdge & EDGE_RIGHT) != 0) {
            canvas.clipRect(child.getRight(), 0, getRight(), getHeight());
        } else if ((mTrackingEdge & EDGE_BOTTOM) != 0) {
            canvas.clipRect(child.getLeft(), child.getBottom(), getRight(), getHeight());
        }
        canvas.drawColor(color);
    }

    private void drawShadow(Canvas canvas, View child) {
        final Rect childRect = mTmpRect;
        child.getHitRect(childRect);

        if ((mEdgeFlag & EDGE_LEFT) != 0) {
            mShadowLeft.setBounds(childRect.left - mShadowLeft.getIntrinsicWidth(), childRect.top,
                    childRect.left, childRect.bottom);
            mShadowLeft.setAlpha((int) (mScrimOpacity * FULL_ALPHA));
            mShadowLeft.draw(canvas);
        }

        if ((mEdgeFlag & EDGE_RIGHT) != 0) {
            mShadowRight.setBounds(childRect.right, childRect.top,
                    childRect.right + mShadowRight.getIntrinsicWidth(), childRect.bottom);
            mShadowRight.setAlpha((int) (mScrimOpacity * FULL_ALPHA));
            mShadowRight.draw(canvas);
        }

        if ((mEdgeFlag & EDGE_BOTTOM) != 0) {
            mShadowBottom.setBounds(childRect.left, childRect.bottom, childRect.right,
                    childRect.bottom + mShadowBottom.getIntrinsicHeight());
            mShadowBottom.setAlpha((int) (mScrimOpacity * FULL_ALPHA));
            mShadowBottom.draw(canvas);
        }
    }

}
