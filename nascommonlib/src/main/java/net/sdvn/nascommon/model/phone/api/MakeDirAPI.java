package net.sdvn.nascommon.model.phone.api;

import androidx.annotation.NonNull;

import net.sdvn.nascommon.utils.EmptyUtils;

import java.io.File;

/**
 * Created by gaoyun@eli-tech.com on 2016/3/1.
 */
public class MakeDirAPI {
    private static final String TAG = MakeDirAPI.class.getSimpleName();

    public boolean mkdir(@NonNull String path) {
        if (EmptyUtils.isEmpty(path)) {
            return false;
        }

        File dir = new File(path);
        if (!dir.exists()) {
            return dir.mkdirs();
        }

        return true;
    }

}
