package net.linkmate.app.util;

import android.content.Context;
import android.util.DisplayMetrics;


/**
 * Master LSW on 2017/5/16 17:05 created
 * 像素转换的工具类
 */

public class Dp2PxUtils {

    //dp转换成px
    public static int dp2px(Context context, int dp) {
        return (int) (dp * context.getResources().getDisplayMetrics().density + 0.5);
    }

    private static int dpToPxByXdpi(Context context, int dp) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        return Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
    }

    /**
     * 获取屏幕宽度
     *
     * @return
     */
    public static int getScreenWidth(Context context) {
        int screenWidth = context.getResources().getDisplayMetrics().widthPixels; //得到屏幕宽度
        /*DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        int widthPixels = dm.widthPixels;
        UcLogUtils.d("element", (widthPixels == mScreenWidth) + "");*/
        return screenWidth;
    }

    /**
     * 获取屏幕宽度
     *
     * @return
     */
    public static int getScreenWidthDP(Context context) {
        int screenWidth = context.getResources().getDisplayMetrics().widthPixels; //得到屏幕宽度
        float density = Math.min(2, context.getResources().getDisplayMetrics().density);
        return (int) (screenWidth / density + 0.5);
    }

    /**
     * 获取屏幕高度
     *
     * @return
     */
    public static int getScreenHeight(Context context) {
        int screenHeight = context.getResources().getDisplayMetrics().heightPixels; //得到屏幕宽度
        return screenHeight;
    }

    /**
     * 获取屏幕高度
     *
     * @return
     */
    public static int getScreenHeightDP(Context context) {
        int screenHeight = context.getResources().getDisplayMetrics().heightPixels; //得到屏幕宽度
        float density = Math.min(2, context.getResources().getDisplayMetrics().density);
        return (int) (screenHeight / density + 0.5);
    }
}