package net.sdvn.nascommon.model.oneos.transfer;

import android.text.TextUtils;

import androidx.annotation.Keep;
import androidx.annotation.Nullable;

import io.objectbox.converter.PropertyConverter;

/**
 * 按state 排序,请勿更新顺序
 * 2021/03/13 @todo2088
 */
@Keep
public enum TransferState {
    NONE,
    START,
    WAIT,
    PAUSE,
    FAILED,
    COMPLETE,
    CANCELED;

    public static class TransferStateConverter implements PropertyConverter<TransferState, String> {

        @Override
        public TransferState convertToEntityProperty(String databaseValue) {
            if (!TextUtils.isEmpty(databaseValue))
                return TransferState.valueOf(databaseValue);
            return TransferState.NONE;
        }

        @Nullable
        @Override
        public String convertToDatabaseValue(@Nullable TransferState entityProperty) {
            if (entityProperty == null) return null;
            return entityProperty.name();
        }


    }
}
