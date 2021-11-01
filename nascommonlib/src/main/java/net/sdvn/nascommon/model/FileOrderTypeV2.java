package net.sdvn.nascommon.model;

import androidx.annotation.NonNull;

/**
 * Created by todo2088.
 */
public enum FileOrderTypeV2 {
    time_desc,//(时间降序)
    time_asc,//(时间升序)
    name_asc,//(名称升序)
    name_desc,//(名称降序)
    size_desc,//(大小降序)
    size_asc,//(大小升序)
    none;//(不排序（默认）)

    @NonNull
    public static FileOrderTypeV2 getType(int id) {
        return FileOrderTypeV2.values()[id];
    }
}
