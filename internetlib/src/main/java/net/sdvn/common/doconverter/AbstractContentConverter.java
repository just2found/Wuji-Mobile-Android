package net.sdvn.common.doconverter;

import android.text.TextUtils;

import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import io.objectbox.converter.PropertyConverter;

public abstract class AbstractContentConverter<T> implements PropertyConverter<T, String> {

    @Nullable
    @Override
    public T convertToEntityProperty(String databaseValue) {
        try {
            if (!TextUtils.isEmpty(databaseValue))
                return new Gson().fromJson(databaseValue, new TypeToken<T>() {
                }.getType());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Nullable
    @Override
    public String convertToDatabaseValue(@Nullable T entityProperty) {
        if (entityProperty == null) return null;
        try {
            return new Gson().toJson(entityProperty);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}