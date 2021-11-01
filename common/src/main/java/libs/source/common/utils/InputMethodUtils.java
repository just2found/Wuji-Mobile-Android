package libs.source.common.utils;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Created by Administrator on 2016/1/12.
 */
public class InputMethodUtils {

    /**
     * Hide Soft Keyboard
     */
    public static void hideKeyboard(Activity context) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(context.getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }

    /**
     * Hide Soft Keyboard
     *
     * @param context is current Activity
     */
    public static void hideKeyboard(Context context, @NonNull EditText editText) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(editText.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }

//        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
//        imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);

//        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
//        if (editText != null) {
//            imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
//            editText.clearFocus();
//            // editText.setInputType(0);
//        }
    }

    /**
     * Show Soft Keyboard
     *
     * @param context is current Activity
     */
    public static void showKeyboard(Context context, @Nullable EditText editText) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (editText != null) {
            if (imm != null) {
                imm.showSoftInput(editText, 0);
            }
        }
    }

    /**
     * Show Soft Keyboard
     *
     * @param context is current Activity
     */
    public static void showKeyboard(@NonNull final Context context, @Nullable final EditText editText, int delay) {
        if (editText != null) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                    if (imm != null) {
                        imm.showSoftInput(editText, 0);
                    }
                }
            }, delay);
        }
    }
}
