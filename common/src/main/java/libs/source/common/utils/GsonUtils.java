package libs.source.common.utils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.Map;

import timber.log.Timber;

/**
 * Created by admin on 2016/1/14.
 */
public class GsonUtils {
    static Gson gson = new Gson();

    public static String encodeJSON(Object src) throws JsonIOException {

        return gson.toJson(src);
    }

    public static String encodeJSONCatchEx(Object src) {
        try {
            return gson.toJson(src);
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }


    @Nullable
    public static <T> T decodeJSON(String jsonString, @NonNull Class<T> cls) {
        T model = null;
        try {
            model = gson.fromJson(jsonString, cls);
        } catch (Exception e) {
            e.printStackTrace();
            Timber.d(e, jsonString);
        }
        return model;
    }

    @Nullable
    public static <T> T decodeJSONWithoutCatchException(String jsonString, @NonNull Class<T> cls) throws Exception {
        T model = null;
        model = gson.fromJson(jsonString, cls);
        return model;
    }

    @Nullable
    public static <T> T decodeJSONCatchException(String jsonString, @NonNull Type typeOfT) {
        T model = null;
        try {
            model = decodeJSON(jsonString, typeOfT);
        } catch (Exception e) {
            Timber.d(e, jsonString);
        }
        return model;
    }

    /* kotlin val type = object : TypeToken<Progress<Content>>() {}.type
     */
    public static <T> T decodeJSON(String jsonString, Type typeOfT) throws JsonSyntaxException {
        return gson.fromJson(jsonString, typeOfT);
    }

    public static Map<String, Object> decodeJSONToMap(String jsonString) throws JsonSyntaxException {
        return gson.fromJson(jsonString, new TypeToken<Map<String, Object>>() {
        }.getType());
    }

}
