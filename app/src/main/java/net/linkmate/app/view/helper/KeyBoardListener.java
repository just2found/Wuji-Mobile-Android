package net.linkmate.app.view.helper;

import android.app.Activity;
import android.graphics.Rect;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;

import timber.log.Timber;

public class KeyBoardListener {
    private Activity activity;

    private View mChildOfContent;
    private int usableHeightPrevious;
    private FrameLayout.LayoutParams frameLayoutParams;


    public static KeyBoardListener getInstance(Activity activity) {
        return new KeyBoardListener(activity);
    }

    public KeyBoardListener(Activity activity) {
        this.activity = activity;
    }


    public void init() {
        ViewGroup content = activity.findViewById(android.R.id.content);
        mChildOfContent = content.getChildAt(0);
        mChildOfContent.getViewTreeObserver().addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {
                    public void onGlobalLayout() {
                        Timber.d("onGlobalLayout 1");
                        possiblyResizeChildOfContent();
                    }
                });
        frameLayoutParams = (FrameLayout.LayoutParams) mChildOfContent
                .getLayoutParams();

    }


    private void possiblyResizeChildOfContent() {
        int usableHeightNow = computeUsableHeight();
        if (usableHeightNow != usableHeightPrevious) {
            Timber.d("onGlobalLayout 2");
            int usableHeightSansKeyboard = mChildOfContent.getRootView().getHeight();
            int heightDifference = usableHeightSansKeyboard - usableHeightNow;
            if (Math.abs(heightDifference) > (usableHeightSansKeyboard / 6)) {
                Timber.d("onGlobalLayout keyboard probably just became visible ");
                // keyboard probably just became visible
//                frameLayoutParams.height = usableHeightSansKeyboard - heightDifference;
            } else {
                Timber.d("onGlobalLayout keyboard probably just became hidden");
                // keyboard probably just became hidden
//                frameLayoutParams.height = usableHeightSansKeyboard + heightDifference;
            }
            mChildOfContent.requestLayout();
            mChildOfContent.invalidate();
            usableHeightPrevious = usableHeightNow;
        }
    }


    private int computeUsableHeight() {
        Rect r = new Rect();
        mChildOfContent.getWindowVisibleDisplayFrame(r);
        return (r.bottom - r.top);
    }


// private void showLog(String title, String msg) {
// Log.d("Unity", title + "------------>" + msg);
// }

}