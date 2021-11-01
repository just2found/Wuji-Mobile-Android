package net.sdvn.nascommon.utils;

import android.app.Service;
import android.content.Context;
import android.os.Vibrator;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;

import androidx.annotation.Nullable;

import net.sdvn.nascommonlib.R;


public class AnimUtils {
    private static final int VIBRATOR_SHORT = 40;
    @Nullable
    private static Vibrator vibrator = null;

    /**
     * EditText 晃动效果
     *
     * @param activity
     * @param view
     */
    public static void sharkEditText(@Nullable Context activity, @Nullable View view) {
        if (null == view || null == activity) {
            return;
        }

        Animation shake = AnimationUtils.loadAnimation(activity, R.anim.anim_edittext_shark);
        view.startAnimation(shake);
    }

    public static void sharkEditText(@Nullable View view) {
        if (null == view) {
            return;
        }

        Animation shake = AnimationUtils.loadAnimation(view.getContext(), R.anim.anim_edittext_shark);
        view.startAnimation(shake);
    }

    /**
     * 手机震动50ms
     */
    public static void shortVibrator() {
        if (null == vibrator) {
            vibrator = (Vibrator) Utils.getApp().getSystemService(
                    Service.VIBRATOR_SERVICE);
        }

        if (vibrator != null) {
            vibrator.vibrate(VIBRATOR_SHORT);
        }
    }

    public static void focusToEnd(@Nullable EditText mEditText) {
        if (null != mEditText) {
            mEditText.requestFocus();
            mEditText.setSelection(mEditText.getText().length());
        }
    }
}
