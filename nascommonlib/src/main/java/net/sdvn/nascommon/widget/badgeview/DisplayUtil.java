package net.sdvn.nascommon.widget.badgeview;

import android.content.Context;

/**
 * @author chqiu
 * Email:qstumn@163.com
 */

public class DisplayUtil {
    public static float dp2px(Context context, float dp) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return dp * scale;
    }

    public static float px2dp(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return pxValue / scale;
    }
}