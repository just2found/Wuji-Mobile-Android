package net.sdvn.nascommon.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.RelativeLayout;

/**
 * Created by LSW on 2018/3/7.
 */

public class InterceptEventRelativeLayout extends RelativeLayout {
    public InterceptEventRelativeLayout(Context context) {
        this(context, null);
    }

    public InterceptEventRelativeLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public InterceptEventRelativeLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return true;
    }
}
