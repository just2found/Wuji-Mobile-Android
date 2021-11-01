package net.sdvn.nascommon.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewpager.widget.ViewPager;

public class EnableScrollViewPager extends ViewPager {
    public EnableScrollViewPager(@NonNull Context context) {
        super(context);
    }

    public EnableScrollViewPager(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public boolean isEnableScroll() {
        return mEnableScroll;
    }

    public void setEnableScroll(boolean enableScroll) {
        mEnableScroll = enableScroll;
    }

    private boolean mEnableScroll = true;

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return mEnableScroll && super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        return mEnableScroll && super.onTouchEvent(ev);
    }
}
