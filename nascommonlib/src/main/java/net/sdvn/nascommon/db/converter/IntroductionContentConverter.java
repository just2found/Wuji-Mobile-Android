package net.sdvn.nascommon.db.converter;

import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import net.sdvn.nascommon.utils.EmptyUtils;

import java.lang.reflect.Type;

import io.objectbox.converter.PropertyConverter;
import io.weline.repo.data.model.DataDevIntroduction;

/**
 * Description:设备简介转换器
 *
 * @author admin
 * CreateDate: 2021/4/9
 */
public class IntroductionContentConverter implements PropertyConverter<DataDevIntroduction, String> {
    @Nullable
    @Override
    public DataDevIntroduction convertToEntityProperty(@Nullable String databaseValue) {
        try {
            if (EmptyUtils.isNotEmpty(databaseValue)) {
                Type typeOfT = new TypeToken<DataDevIntroduction>() {
                }.getType();
                return new Gson().fromJson(databaseValue, typeOfT);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Nullable
    @Override
    public String convertToDatabaseValue(@Nullable DataDevIntroduction entityProperty) {
        if (entityProperty == null) return null;
        try {
            return new Gson().toJson(entityProperty);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
