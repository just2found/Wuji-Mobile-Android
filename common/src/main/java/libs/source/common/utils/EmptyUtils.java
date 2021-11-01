package libs.source.common.utils;

import androidx.annotation.Nullable;

import java.util.List;

/**
 * Created by admin on 2016/1/7.
 */
public class EmptyUtils {

    public static boolean isEmpty(@Nullable List<?> list) {
        return list == null || list.isEmpty();
    }

    public static boolean isEmpty(@Nullable String s) {
        if (null == s) {
            return true;
        }

        return s.trim().length() == 0;
    }

    public static boolean isNotEmpty(@Nullable String s) {
        return !isEmpty(s);
    }

    public static boolean isNotEmpty(@Nullable List<?> list) {
        return !isEmpty(list);
    }
}
