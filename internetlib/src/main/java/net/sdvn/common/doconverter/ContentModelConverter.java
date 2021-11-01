package net.sdvn.common.doconverter;

import android.text.TextUtils;

import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import net.sdvn.common.internet.protocol.entity.FlowMbpointRatioModel;

import io.objectbox.converter.PropertyConverter;

public class ContentModelConverter implements PropertyConverter<FlowMbpointRatioModel, String> {

    @Nullable
    @Override
    public FlowMbpointRatioModel convertToEntityProperty(String databaseValue) {
        try {
            if (!TextUtils.isEmpty(databaseValue))
                return new Gson().fromJson(databaseValue, new TypeToken<FlowMbpointRatioModel>() {
                }.getType());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Nullable
    @Override
    public String convertToDatabaseValue(@Nullable FlowMbpointRatioModel entityProperty) {
        if (entityProperty == null) return null;
        try {
            return new Gson().toJson(entityProperty);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}