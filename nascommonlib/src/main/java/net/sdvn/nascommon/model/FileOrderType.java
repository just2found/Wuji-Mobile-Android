package net.sdvn.nascommon.model;

import androidx.annotation.NonNull;

/**
 * Created by gaoyun@eli-tech.com on 2016/1/15.
 */
public enum FileOrderType {
    /**
     * order by file toPath
     */
    NAME,
    /**
     * order by file time
     */
    TIME;

    public static boolean isName(FileOrderType type) {
        return type == NAME;
    }

    public static boolean isName(int id) {
        return id == 0;
    }

    @NonNull
    public static FileOrderType getType(int id) {
        if (isName(id)) {
            return FileOrderType.NAME;
        }

        return FileOrderType.TIME;
    }
}
