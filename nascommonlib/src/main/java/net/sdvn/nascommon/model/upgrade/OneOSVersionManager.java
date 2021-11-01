package net.sdvn.nascommon.model.upgrade;

import androidx.annotation.NonNull;

import net.sdvn.nascommon.utils.EmptyUtils;
import net.sdvn.nascommon.utils.log.Logger;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by gaoyun@eli-tech.com on 2016/4/6.
 */
public class OneOSVersionManager {
    private static final String TAG = OneOSVersionManager.class.getSimpleName();
    // Android OneSpace V3.0.9 beta 需要 OneOS 最低版本为3.0.12，适配情况：
    // 1、备份文件时需要使用新的重命名接口；
    public static final String MIN_ONEOS_VERSION = "5.0.0";

    /**
     * check version string, version must be format in xx.xx.xx
     *
     * @param version oneos current version
     * @return true if match success, otherwise false
     */
    public static boolean check(String version) {
        return compare(version, MIN_ONEOS_VERSION);
    }

    public static boolean compare(String version, @NonNull String minVersion) {
        Logger.p(Logger.Level.DEBUG, Logger.Logd.DEBUG, TAG, "version:  " + version);
        if (!EmptyUtils.isEmpty(version)) {
            if (version.equalsIgnoreCase(minVersion)) {
                return true;
            }
            String regEx = "[^.0-9]";
            Pattern pattern = Pattern.compile(regEx);
            Matcher matcher = pattern.matcher(version);
            version = matcher.replaceAll("").trim();

            String[] cur = version.split("\\.");
            String[] min = minVersion.split("\\.");
            Logger.p(Logger.Level.DEBUG, Logger.Logd.DEBUG, TAG, "version:  " + version);
            Logger.p(Logger.Level.DEBUG, Logger.Logd.DEBUG, TAG, "version:  " + minVersion);
            for (int i = 0; i < 3; i++) {
                int c;
                int m;
                try {
                    c = Integer.valueOf(cur[i]);
                } catch (Exception e) {
                    e.printStackTrace();
                    c = 0;
                }
                try {
                    m = Integer.valueOf(min[i]);
                } catch (Exception e) {
                    e.printStackTrace();
                    m = 0;
                }

                if (c > m) {
                    return true;
                }
            }
        }

        return false;
    }
}
