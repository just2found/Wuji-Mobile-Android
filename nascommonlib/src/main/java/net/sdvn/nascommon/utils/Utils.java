package net.sdvn.nascommon.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.SystemClock;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.util.DisplayMetrics;
import android.view.View;

import androidx.annotation.ColorRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.text.DateFormat;
import java.util.Date;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by gaoyun@eli-tech.com on 2016/1/7.
 */
public class Utils {
    private static Context sContext;

    public static void init(Context context) {
        sContext = context;
    }

    public static final int VIEW_TAG_KEY_CLICK = 1766613352;
    public static final int VIEW_DEFAULT_CLICK_INTERVAL = 400;

    //    @NonNull
//    private static WeakHashMap<View, Long> clickMap = new WeakHashMap<>();
    public static boolean isFastClick(View view) {
        return isFastClick(view, VIEW_DEFAULT_CLICK_INTERVAL);
    }

    public static boolean isFastClick(View view, long interval) {
        if (interval <= 0) interval = VIEW_DEFAULT_CLICK_INTERVAL;
        long currentTime = SystemClock.uptimeMillis();
//        Long lastClickTime = clickMap.get(view);
        Long lastClickTime;
        try {
            lastClickTime = (Long) view.getTag(VIEW_TAG_KEY_CLICK);
        } catch (Exception e) {
            lastClickTime = 0L;
        }
        long lagTime = currentTime - (lastClickTime == null ? 0 : lastClickTime);
        if (lagTime > 0 && lagTime < interval)
            return true;
        view.setTag(VIEW_TAG_KEY_CLICK, currentTime);
        return false;
    }

    public static boolean isNotFastClick(View view) {
        return isNotFastClick(view, VIEW_DEFAULT_CLICK_INTERVAL);
    }

    public static boolean isNotFastClick(View view, long interval) {
        return !isFastClick(view, interval);
    }

    /**
     * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
     */
    public static int dipToPx(float dpValue) {
        final float scale = Utils.getApp().getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    /**
     * check if ip is valid
     */
    public static boolean isAvailableIp(String IP) {
        boolean b = false;
        try {
            if (IP.matches("\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}")) {
                String[] s = IP.split("\\.");
                if (Integer.parseInt(s[0]) < 255)
                    if (Integer.parseInt(s[1]) < 255)
                        if (Integer.parseInt(s[2]) < 255)
                            if (Integer.parseInt(s[3]) < 255)
                                b = true;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return b;
    }

    /**
     * check if port is valid
     *
     * @param port
     * @return result
     */
    public static boolean checkPort(String port) {
        if (EmptyUtils.isEmpty(port)) {
            return false;
        }

        int i = -1;
        try {
            i = Integer.parseInt(port);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            i = -1;
        }

        return i >= 0 && i <= 65535;
    }

    /**
     * check WIFI is available
     *
     * @param context
     * @return if available return true, else return false
     */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public static boolean isWifiAvailable(@Nullable Context context) {
        if (context == null) {
            return false;
        }
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetInfo = manager != null ? manager.getActiveNetworkInfo() : null;
        return activeNetInfo != null && (activeNetInfo.getType() == ConnectivityManager.TYPE_WIFI
                || activeNetInfo.getType() == ConnectivityManager.TYPE_ETHERNET);

    }

    public static boolean isWifi(Context mContext) {
        ConnectivityManager connectivityManager = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetInfo = connectivityManager != null ? connectivityManager.getActiveNetworkInfo() : null;
        return activeNetInfo != null && activeNetInfo.getType() == ConnectivityManager.TYPE_WIFI;
    }

    public static void gotoAppDetailsSettings(@NonNull Context context) {
        Intent localIntent = new Intent();
        localIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (Build.VERSION.SDK_INT >= 9) {
            localIntent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
            localIntent.setData(Uri.fromParts("package", context.getPackageName(), null));
        } else if (Build.VERSION.SDK_INT <= 8) {
            localIntent.setAction(Intent.ACTION_VIEW);
            localIntent.setClassName("com.android.settings", "com.android.settings.InstalledAppDetails");
            localIntent.putExtra("com.android.settings.ApplicationPkgName", context.getPackageName());
        }
        context.startActivity(localIntent);
    }

    public static int getWindowsSize(Activity activity, boolean isWidth) {
        DisplayMetrics dm = new DisplayMetrics();
        // 获取屏幕信息
        activity.getWindowManager().getDefaultDisplay().getMetrics(dm);
        if (isWidth) {
            return dm.widthPixels;
        } else {
            return dm.heightPixels;
        }
    }

    public static int getAppVersion(Context context) {
        try {
            PackageInfo info = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return info.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return 1;
    }

    /**
     * 搜索关键字标
     *
     * @param content
     * @param keyword
     * @return
     */
    @NonNull
    public static SpannableString setKeyWordColor(@NonNull Context context, @ColorRes int id, @NonNull String content, String keyword) {
        SpannableString spannableString = new SpannableString(content);
        String wordReg = "(?i)" + keyword;//用(?i)来忽略大小写
        Matcher matcher = Pattern.compile(wordReg).matcher(content);
        while (matcher.find()) {
            int start = matcher.start();
            int end = matcher.end();
            spannableString.setSpan(new ForegroundColorSpan(context.getResources().getColor(id)),
                    start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        return spannableString;
    }

    @NonNull
    public static String genRandomNum(int count) {
        int counter = 0;
        int i;
        String str = "qwertyupasdfghjkzxcvbnmQWERTYUPASDFGHJKLZXCVBNM0123456789";
        StringBuilder builder = new StringBuilder();
        Random r = new Random();
        while (counter < count) {
            i = Math.abs(r.nextInt(str.length()));
            if (i >= 0 && i < str.length()) {
                builder.append(str.charAt(i));
                counter++;
            }
        }
        return builder.toString();
    }

    /**
     * Returns a user agent string based on the given application name and the library version.
     *
     * @param context A valid context of the calling application.
     * @return A user agent string generated using the applicationName and the library version.
     */
    public static String getUserAgent(Context context) {
        String versionName;
        String applicationName;
        try {
            String packageName = context.getPackageName();
            applicationName = packageName.substring(packageName.lastIndexOf(".") + 1);
            PackageInfo info = context.getPackageManager().getPackageInfo(packageName, 0);
            versionName = info.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            versionName = "?";
            applicationName = "unknown";
        }
        return applicationName + "/" + versionName + " (Linux;Android " + Build.VERSION.RELEASE
                + ") ";
    }

    public static Context getApp() {
        if (sContext == null) {
            throw new NullPointerException("Context is null,pls init Utils");
        }
        return sContext;
    }

    public static String generateDisplayName(Uri uri) {
        final String path = uri.getPath();
        if (!EmptyUtils.isEmpty(path)) {
            final int i = path.lastIndexOf("/");
            if (i > 0 && i < path.length()) {
                return path.substring(i);
            }
        }
        final String toString = uri.toString();
        if (!EmptyUtils.isEmpty(toString)) {
            final int i = toString.lastIndexOf("/");
            if (i > 0 && i < toString.length()) {
                return toString.substring(i);
            }
        }
        return "unknown-" + unixTimeToHumanReadable(System.currentTimeMillis());
    }

    public static String unixTimeToHumanReadable(long milliseconds) {
        Date date = new Date(milliseconds);
        DateFormat df = DateFormat.getDateTimeInstance();
        return df.format(date);
    }

    /**
     * 版本号比较
     *
     * @param v1
     * @param v2
     * @return 0代表相等，1代表左边大，-1代表右边大
     * Utils.compareVersion("1.0.358_20180820090554","1.0.358_20180820090553")=1
     */
    public static int compareVersion(String v1, String v2) {
        if (v1.equals(v2)) {
            return 0;
        }
        String[] version1Array = v1.split("[._]");
        String[] version2Array = v2.split("[._]");
        int index = 0;
        int minLen = Math.min(version1Array.length, version2Array.length);
        long diff = 0;

        while (index < minLen
                && (diff = Long.parseLong(version1Array[index])
                - Long.parseLong(version2Array[index])) == 0) {
            index++;
        }
        if (diff == 0) {
            for (int i = index; i < version1Array.length; i++) {
                if (Long.parseLong(version1Array[i]) > 0) {
                    return 1;
                }
            }

            for (int i = index; i < version2Array.length; i++) {
                if (Long.parseLong(version2Array[i]) > 0) {
                    return -1;
                }
            }
            return 0;
        } else {
            return diff > 0 ? 1 : -1;
        }
    }
}
