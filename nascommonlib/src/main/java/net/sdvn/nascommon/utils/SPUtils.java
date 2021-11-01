package net.sdvn.nascommon.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.Nullable;

import static android.content.Context.MODE_APPEND;
import static android.content.Context.MODE_PRIVATE;

/**
 * station
 * Created by Administrator on 2017/12/7.
 */
@SuppressLint("WrongConstant")
public class SPUtils {
    private static final String TAG = "SPUtils";
    private static final String SHARED_PREFERENCES_NAME = "LoginUserInfo";

//    public static void setUserInfo(Context context, String username, String password, String uid) {
//        SharedPreferences userInfoSP = context.getSharedPreferences("LoginUserInfo", MODE_PRIVATE + MODE_APPEND);
//        String account = userInfoSP.getString(username, "none");
//       Logger.LOGD(TAG, "username ===== " + account);
//        if (account.equals("none") || !account.equals(username)) {
//           Logger.LOGD(TAG, "---------------存入用户数据----------------");
//            SharedPreferences.Editor editor = userInfoSP.edit();
//            editor.putString("username", username);
//            editor.putString("password", password);
//            editor.putString("uid", uid);
//            editor.putBoolean("isLogined", true);
//            editor.apply();
//        }
//    }
//
//    public static boolean isUserLogined(Context context, String username) {
//        SharedPreferences userInfoSP = context.getSharedPreferences("LoginUserInfo", MODE_PRIVATE + MODE_APPEND);
//        String account = userInfoSP.getString("username", "none");
//        boolean isLogined = userInfoSP.getBoolean("isLogined", false);
//       Logger.LOGD(TAG, "username ===== " + account);
//       Logger.LOGD(TAG, "isLogined ===== " + isLogined);
//        return !account.equals("none") && isLogined;
//
//    }
//
//    public static void deleteLoginUserInfo(Context context) {
//        SharedPreferences userInfoSP = context.getSharedPreferences("LoginUserInfo", MODE_PRIVATE + MODE_APPEND);
//        SharedPreferences.Editor editor = userInfoSP.edit();
//        editor.clear();
//        editor.apply();
//    }

    @Nullable
    public static String getValue(@Nullable Context context, String param) {
        if (context == null)
            return "";
        SharedPreferences userInfoSP = context.getSharedPreferences(SHARED_PREFERENCES_NAME, MODE_PRIVATE + MODE_APPEND);
        return userInfoSP.getString(param, "");
    }

    @Nullable
    public static String getValue(@Nullable Context context, String param, String defValue) {
        if (context == null)
            return "";
        SharedPreferences userInfoSP = context.getSharedPreferences(SHARED_PREFERENCES_NAME, MODE_PRIVATE + MODE_APPEND);
        return userInfoSP.getString(param, defValue);
    }

//    public static boolean isAdmin(Context context) {
//        if (context == null)
//            return false;
//        SharedPreferences userInfoSP = context.getSharedPreferences("LoginUserInfo", MODE_PRIVATE + MODE_APPEND);
//        return userInfoSP.getBoolean("isAdmin", false);
//    }

    public static void setValue(@Nullable Context context, String key, String value) {
        if (context == null)
            return;
        SharedPreferences userInfoSP = context.getSharedPreferences(SHARED_PREFERENCES_NAME, MODE_PRIVATE + MODE_APPEND);
        SharedPreferences.Editor editor = userInfoSP.edit();
        editor.putString(key, value);
        editor.apply();
    }

    public static void setBoolean(String key, Boolean value) {
        SharedPreferences userInfoSP = Utils.getApp().getSharedPreferences(SHARED_PREFERENCES_NAME, MODE_PRIVATE + MODE_APPEND);
        SharedPreferences.Editor editor = userInfoSP.edit();
        editor.putBoolean(key, value);
        editor.apply();
    }

    public static boolean getBoolean(String param) {
        return getBoolean(param, false);
    }

    public static boolean getBoolean(String param, boolean defValue) {
        SharedPreferences userInfoSP = Utils.getApp().getSharedPreferences(SHARED_PREFERENCES_NAME, MODE_PRIVATE + MODE_APPEND);
        return userInfoSP.getBoolean(param, defValue);
    }
}
