package net.linkmate.app.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import net.linkmate.app.base.MyApplication;
import net.sdvn.cmapi.CMAPI;
import net.sdvn.common.internet.protocol.entity.SdvnMessage;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public final class MySPUtils {

    private final static String name = "config";
    private final static int mode = Context.MODE_PRIVATE;
    private final static String FILE_SEND_KEY = "file_send";
    private final static String FILE_RECEIVE_KEY = "file_receive";
    private final static String MESSAGE_KEY = "message";
    private final static String MESSAGE_NEW_DATE_KEY = "message_new_date";//消息最新时间字符串
    private final static String MESSAGE_NEW_TIMESTAMP_KEY = "message_new_timestamp";//消息最新时间戳
    private final static String MESSAGE_COUNT_KEY = "MESSAGE_COUNT";//未查看消息总数
    private final static String MESSAGE_DELETE_IDS_KEY = "message_delete_ids";//已删除消息的id
    public final static String UPDATE_INFO = "update_info";//更新信息
    public final static String APP_TIP_ABLE = "app_tip_able";//应用是否需要提示

    public final static String FIELD_IS_AGREE_PRIVACY_POLICY = "field_is_agree_privacy_policy";//是否同意了隐私政策
    public static void saveBoolean(String key, boolean value) {
        saveBoolean(MyApplication.getContext(), key, value);
    }

    public static void saveBoolean(Context context, String key, boolean value) {
        SharedPreferences sp = context.getSharedPreferences(name, mode);
        Editor edit = sp.edit();
        edit.putBoolean(key, value);
        edit.apply();
    }

    public static void saveInt(Context context, String key, int value) {
        SharedPreferences sp = context.getSharedPreferences(name, mode);
        Editor edit = sp.edit();
        edit.putInt(key, value);
        edit.apply();
    }

    public static void saveString(Context context, String key, String value) {
        SharedPreferences sp = context.getSharedPreferences(name, mode);
        Editor edit = sp.edit();
        edit.putString(key, value);
        edit.apply();
    }

    public static void saveLong(Context context, String key, long value) {
        SharedPreferences sp = context.getSharedPreferences(name, mode);
        Editor edit = sp.edit();
        edit.putLong(key, value);
        edit.apply();
    }


    public static boolean getBoolean(String key) {
        return getBoolean(MyApplication.getContext(), key, false);
    }

    public static boolean getBoolean(Context context, String key, boolean defValue) {
        SharedPreferences sp = context.getSharedPreferences(name, mode);
        return sp.getBoolean(key, defValue);
    }

    public static int getInt(Context context, String key, int defValue) {
        SharedPreferences sp = context.getSharedPreferences(name, mode);
        return sp.getInt(key, defValue);
    }

    public static String getString(Context context, String key, String defValue) {
        SharedPreferences sp = context.getSharedPreferences(name, mode);
        return sp.getString(key, defValue);
    }

    public static long getLong(Context context, String key, long defValue) {
        SharedPreferences sp = context.getSharedPreferences(name, mode);
        return sp.getLong(key, defValue);
    }

//    /**
//     * 保存发送文件的记录
//     */
//    public static void saveFileSend(Context context, List<FileTransRecordBean> list) {
//        if (list == null) return;
//        SharedPreferences sp = context.getSharedPreferences(name, mode);
//        Editor edit = sp.edit();
//        Gson gson = new Gson();
//        String json = gson.toJson(list);
//        edit.putString(FILE_SEND_KEY, json);
//        edit.apply();
//    }
//
//    /**
//     * 读取发送文件的记录
//     */
//    public static List<FileTransRecordBean> getFileSend(Context context) {
//        SharedPreferences sp = context.getSharedPreferences(name, mode);
//        String json = sp.getString(FILE_SEND_KEY, "");
//        if (TextUtils.isEmpty(json)) {
//            return new ArrayList<>();
//        } else {
//            Gson gson = new Gson();
//            Type type = new TypeToken<List<FileTransRecordBean>>() {
//            }.getType();
//            return gson.fromJson(json, type);
//        }
//    }
//
//    /**
//     * 保存接收文件的记录
//     */
//    public static void saveFileRecv(Context context, List<FileTransRecordBean> list) {
//        if (list == null) return;
//        SharedPreferences sp = context.getSharedPreferences(name, mode);
//        Editor edit = sp.edit();
//        Gson gson = new Gson();
//        String json = gson.toJson(list);
//        edit.putString(FILE_RECEIVE_KEY, json);
//        edit.apply();
//    }
//
//    /**
//     * 读取接收文件的记录
//     */
//    public static List<FileTransRecordBean> getFileRecv(Context context) {
//        SharedPreferences sp = context.getSharedPreferences(name, mode);
//        String json = sp.getString(FILE_RECEIVE_KEY, "");
//        if (TextUtils.isEmpty(json)) {
//            return new ArrayList<>();
//        } else {
//            Gson gson = new Gson();
//            Type type = new TypeToken<List<FileTransRecordBean>>() {
//            }.getType();
//            return gson.fromJson(json, type);
//        }
//    }

    public static void saveMessage(List<SdvnMessage> list) {
        if (list == null) return;
        SharedPreferences sp = MyApplication.getContext().getSharedPreferences(name, mode);
        Editor edit = sp.edit();
        Gson gson = new Gson();
        String json = gson.toJson(list);
        edit.putString(MESSAGE_KEY + CMAPI.getInstance().getBaseInfo().getAccount(), json);
        edit.apply();
    }

    public static List<SdvnMessage> loadMessage() {
        SharedPreferences sp = MyApplication.getContext().getSharedPreferences(name, mode);
        String json = sp.getString(MESSAGE_KEY + CMAPI.getInstance().getBaseInfo().getAccount(), "");
        if (TextUtils.isEmpty(json)) {
            return new ArrayList<>();
        } else {
            Gson gson = new Gson();
            Type type = new TypeToken<List<SdvnMessage>>() {
            }.getType();
            return gson.fromJson(json, type);
        }
    }

    public static void saveMessageNewDate(String date) {
        saveString(MyApplication.getContext(), MESSAGE_NEW_DATE_KEY + CMAPI.getInstance().getBaseInfo().getAccount(), date);
    }

    public static String getMessageNewDate() {
        return getString(MyApplication.getContext(), MESSAGE_NEW_DATE_KEY + CMAPI.getInstance().getBaseInfo().getAccount(), "");
    }

    public static void saveMessageNewTimestamp(Long timestamp) {
        if (timestamp > getMessageNewTimestamp())
            saveLong(MyApplication.getContext(), MESSAGE_NEW_TIMESTAMP_KEY + CMAPI.getInstance().getBaseInfo().getAccount(), timestamp);
    }

    public static int getMessageNewCount() {
        return getInt(MyApplication.getContext(), MESSAGE_COUNT_KEY + CMAPI.getInstance().getBaseInfo().getAccount(), 0);
    }

    public static void saveMessageNewCount(int messagesCount) {
        saveInt(MyApplication.getContext(), MESSAGE_COUNT_KEY + CMAPI.getInstance().getBaseInfo().getAccount(), messagesCount);
    }

    public static long getMessageNewTimestamp() {
        return getLong(MyApplication.getContext(), MESSAGE_NEW_TIMESTAMP_KEY + CMAPI.getInstance().getBaseInfo().getAccount(), -1);
    }

    public static void saveMessageDeleteIds(List<String> list) {
        Gson gson = new Gson();
        String userJson = gson.toJson(list);
        saveString(MyApplication.getContext(), MESSAGE_DELETE_IDS_KEY + CMAPI.getInstance().getBaseInfo().getAccount(), userJson);
    }

    public static List<String> getMessageDeleteIdsKey() {
        Gson gson = new Gson();
        String userJson = getString(MyApplication.getContext(), MESSAGE_DELETE_IDS_KEY + CMAPI.getInstance().getBaseInfo().getAccount(), "");
        Type type = new TypeToken<List<String>>() {
        }.getType();
        List<String> ids = gson.fromJson(userJson, type);
        if (ids == null)
            ids = new ArrayList<>();
        return ids;
    }
}
