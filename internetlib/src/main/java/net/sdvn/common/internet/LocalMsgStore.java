//package net.sdvn.common.internet;
//
//import android.content.Context;
//import android.content.SharedPreferences;
//import android.text.TextUtils;
//
//import androidx.annotation.Nullable;
//
//import com.google.gson.Gson;
//import com.google.gson.reflect.TypeToken;
//
//import net.sdvn.cmapi.CMAPI;
//import net.sdvn.common.internet.protocol.entity.SdvnMessage;
//
//import java.lang.reflect.Type;
//import java.util.ArrayList;
//import java.util.List;
//
//public class LocalMsgStore {
//    public final static String name = "_config";
//    public final static String MESSAGE_COUNT_KEY = "count";
//    private final static int mode = Context.MODE_PRIVATE;
//    private final static String MESSAGE_KEY = "message";
//    private final static String MESSAGE_NEW_DATE_KEY = "message_new_date";
//    private final static String MESSAGE_TIMESTAMP_KEY = "message_timestamp";
//
//    /**
//     * 保存消息
//     */
//    public static void saveMessage(@Nullable List<SdvnMessage> list) {
//        if (list == null) return;
//        Gson gson = new Gson();
//        String json = gson.toJson(list);
//        saveString(OkHttpClientIns.getContext(), MESSAGE_KEY, json);
//
//    }
//
//    /**
//     * 读取消息
//     */
//    public static List<SdvnMessage> loadMessage() {
//        String json = getString(OkHttpClientIns.getContext(), MESSAGE_KEY, "");
//        if (TextUtils.isEmpty(json)) {
//            return new ArrayList<>();
//        } else {
//            Gson gson = new Gson();
//            Type type = new TypeToken<List<SdvnMessage>>() {
//            }.getType();
//            return gson.fromJson(json, type);
//        }
//    }
//
//    public static void saveMessageNewDate(String date) {
//        saveString(OkHttpClientIns.getContext(), MESSAGE_NEW_DATE_KEY, date);
//    }
//
//    @Nullable
//    public static String getMessageNewDate() {
//        return getString(OkHttpClientIns.getContext(), MESSAGE_NEW_DATE_KEY, "");
//    }
//
//    public static void saveMessageTimestamp(long date) {
//        saveString(OkHttpClientIns.getContext(), MESSAGE_TIMESTAMP_KEY, String.valueOf(date));
//    }
//
//    public static long getMessageTimestamp() {
//        return Long.parseLong(getString(OkHttpClientIns.getContext(), MESSAGE_TIMESTAMP_KEY, "-1"));
//    }
//
//
//    public static void saveString(Context appContext, String key, String value) {
//        String account = CMAPI.getInstance().getBaseInfo().getUserId();
//        String sp_name = account + name;
//        SharedPreferences sp = appContext.getSharedPreferences(sp_name, mode);
//        SharedPreferences.Editor edit = sp.edit();
//        edit.putString(key, value);
//        edit.apply();
//
//    }
//
//
//    @Nullable
//    public static String getString(Context appContext, String key, String defValue) {
//        String account = CMAPI.getInstance().getBaseInfo().getUserId();
//        String sp_name = account + name;
//        SharedPreferences sp = appContext.getSharedPreferences(sp_name, mode);
//        return sp.getString(key, defValue);
//    }
//
//    public static boolean clearSPFile(String spName) {
//        SharedPreferences preferences = OkHttpClientIns.getContext().getSharedPreferences(spName, Context.MODE_PRIVATE);
//        SharedPreferences.Editor editor = preferences.edit();
//        editor.clear();
//        return editor.commit();
//    }
//
//
//}
