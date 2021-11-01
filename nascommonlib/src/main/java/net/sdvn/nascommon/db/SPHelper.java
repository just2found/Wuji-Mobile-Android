package net.sdvn.nascommon.db;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Base64;

import androidx.annotation.Nullable;

import net.sdvn.cmapi.CMAPI;
import net.sdvn.nascommon.utils.Utils;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * SharedPreferences Helper
 * <p>
 * Created by gaoyun@eli-tech.com on 2016/4/20.
 */
public class SPHelper {
    private static final String SHARED_PREFERENCES_NAME = "station";

    /**
     * put value in {@link SharedPreferences} by key
     *
     * @param key
     * @param value
     * @return {@code true} if success, otherwise {@code false}
     */

    public static boolean put(String key, String value) {
        value = Base64.encodeToString(value.getBytes(StandardCharsets.UTF_8), Base64.DEFAULT).trim();
        String sp_name = getSPName();
        return put(sp_name, key, value);
    }

    private static String getSPName() {
        String sp_name = SHARED_PREFERENCES_NAME;
        String userId = CMAPI.getInstance().getBaseInfo().getUserId();
        if (!TextUtils.isEmpty(userId)) sp_name = userId;
        return sp_name;
    }


    /**
     * get value from {@link SharedPreferences} by key
     *
     * @param key      value of key
     * @param defValue default value
     * @return value or {@code null}
     */
    @Nullable
    public static String get(String key, String defValue) {
        String sp_name = getSPName();
        String value = get(sp_name, key, defValue);
        if (Objects.equals(defValue, value)) {
            return defValue;
        }
        value = new String(Base64.decode(value, Base64.DEFAULT), StandardCharsets.UTF_8);
        return value;
    }

    /**
     * put value in {@link SharedPreferences} by key
     *
     * @param key
     * @param value
     * @return {@code true} if success, otherwise {@code false}
     */
    public static boolean put(String sp_name, String key, String value) {
        SharedPreferences preferences = Utils.getApp().getSharedPreferences(sp_name, Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = preferences.edit();
        edit.putString(key, value);

        return edit.commit();
    }

    /**
     * get value from {@link SharedPreferences} by key
     *
     * @param key      value of key
     * @param defValue default value
     * @return value or {@code null}
     */
    @Nullable
    public static String get(String sp_name, String key, String defValue) {
        SharedPreferences preferences = Utils.getApp().getSharedPreferences(sp_name, Context.MODE_PRIVATE);
        return preferences.getString(key, defValue);
    }

    /**
     * put value in {@link SharedPreferences} by key
     *
     * @param key
     * @param value
     * @return {@code true} if success, otherwise {@code false}
     */
    public static boolean put(String key, boolean value) {
        SharedPreferences preferences = Utils.getApp().getSharedPreferences(getSPName(), Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = preferences.edit();
        edit.putBoolean(key, value);
        edit.apply();
        return true;
    }

    /**
     * get value from {@link SharedPreferences} by key
     *
     * @param key      value of key
     * @param defValue default value
     * @return value or {@code null}
     */
    public static boolean get(String key, boolean defValue) {
        SharedPreferences preferences = Utils.getApp().getSharedPreferences(getSPName(), Context.MODE_PRIVATE);
        return preferences.getBoolean(key, defValue);
    }

    public static void registerSPChangedListener(String name, SharedPreferences.OnSharedPreferenceChangeListener listener) {
        SharedPreferences preferences = Utils.getApp().getSharedPreferences(name, Context.MODE_PRIVATE);
        preferences.registerOnSharedPreferenceChangeListener(listener);
    }

    public static void unregisterSPChangedListener(String name, SharedPreferences.OnSharedPreferenceChangeListener listener) {
        SharedPreferences preferences = Utils.getApp().getSharedPreferences(name, Context.MODE_PRIVATE);
        preferences.unregisterOnSharedPreferenceChangeListener(listener);
    }

    public static boolean clearSPFile(String spName) {
        SharedPreferences preferences = Utils.getApp().getSharedPreferences(spName, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear();
        return editor.commit();
    }

    /**
     * 清除sp保存的mima
     */
    public static void clearPwd() {
        SharedPreferences preferences = Utils.getApp().getSharedPreferences(getSPName(), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        String password = "password";
        editor.remove(password);
        editor.apply();
    }
}
