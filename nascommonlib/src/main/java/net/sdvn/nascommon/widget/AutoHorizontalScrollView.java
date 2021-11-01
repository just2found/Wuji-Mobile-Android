package net.sdvn.nascommon.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.HorizontalScrollView;

public class AutoHorizontalScrollView extends HorizontalScrollView {

    public AutoHorizontalScrollView(Context context) {
        super(context);
    }

    public AutoHorizontalScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AutoHorizontalScrollView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        postDelayed(new Runnable() {

            @Override
            public void run() {
                fullScroll(HorizontalScrollView.FOCUS_RIGHT);
            }
        }, 100);
    }
}
