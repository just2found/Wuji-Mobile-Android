package net.linkmate.app.util;

import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.view.WindowManager;

public class WindowUtil {
    private static final float SHADOW_ALPHA = 0.7f;

    public static void showShadow(final Activity activity) {
        showShadow(activity, null);
    }

    public static void showShadow(final Activity activity, AnimatorListenerAdapter listener) {
        ValueAnimator animator = ValueAnimator.ofFloat(1f, SHADOW_ALPHA);
        animator.setDuration(500);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = (float) animation.getAnimatedValue();
                backgroundAlpha(activity, value);
            }
        });
        if (listener != null)
            animator.addListener(listener);
        animator.start();
    }

    public static void hintShadow(Activity activity) {
        hintShadow(activity, null);
    }

    public static void hintShadow(final Activity activity, AnimatorListenerAdapter listener) {
        ValueAnimator animator = ValueAnimator.ofFloat(SHADOW_ALPHA, 1f);
        animator.setDuration(500);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = (float) animation.getAnimatedValue();
                backgroundAlpha(activity, value);
            }
        });
        if (listener != null)
            animator.addListener(listener);
        animator.start();
    }

    private static void backgroundAlpha(Activity activity, float f) {
        try {
            WindowManager.LayoutParams lp = activity.getWindow().getAttributes();
            lp.alpha = f;
            activity.getWindow().setAttributes(lp);
        } catch (Exception ignore) {
        }
    }
}
