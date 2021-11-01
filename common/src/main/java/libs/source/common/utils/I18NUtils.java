package libs.source.common.utils;

import android.content.Context;

import androidx.annotation.NonNull;

import java.util.Locale;
import java.util.TimeZone;

public class I18NUtils {
    /**
     * 获取当前时区 * @return
     */
    public static String getCurrentTimeZone() {
        TimeZone tz = TimeZone.getDefault();
        return tz.getDisplayName(false, TimeZone.SHORT);
    }

    /**
     * 获取当前系统语言格式 * @param mContext * @return
     */
    @NonNull
    public static String getCurrentLanguage(Context mContext) {
        Locale locale = mContext.getResources().getConfiguration().locale;
        String language = locale.getLanguage();
        String country = locale.getCountry();
        String lc = language + "_" + country;
        return lc;
    }
}