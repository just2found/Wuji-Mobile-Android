package net.sdvn.nascommon.db.converter;

import androidx.annotation.NonNull;

import com.google.gson.reflect.TypeToken;

import net.sdvn.nascommon.utils.GsonUtils;

import java.util.ArrayList;
import java.util.List;

import io.objectbox.converter.PropertyConverter;

public class ListConverter<E> implements PropertyConverter<List<E>, String> {
    public static final String TAG = ListConverter.class.getSimpleName();

    @NonNull
    @Override
    public List<E> convertToEntityProperty(String databaseValue) {
//        Logger.p(Logger.Level.DEBUG, Logger.Logd.DAO, TAG, "databaseValue :" + databaseValue);
        List<E> strings = new ArrayList<>();
        if (databaseValue == null) return strings;
        try {
            final List<E> decodeJSON = GsonUtils.decodeJSON(databaseValue, new TypeToken<List<E>>() {
            }.getType());
            if (decodeJSON != null) {
                strings.addAll(decodeJSON);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return strings;
    }

    @Override
    public String convertToDatabaseValue(List<E> entityProperty) {
        try {
            return GsonUtils.encodeJSON(entityProperty);
        } catch (Exception e) {
            e.printStackTrace();
            return "[]";
        }
    }
}
