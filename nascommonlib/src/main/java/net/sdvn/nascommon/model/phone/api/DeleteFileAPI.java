package net.sdvn.nascommon.model.phone.api;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import net.sdvn.nascommon.model.phone.LocalFile;
import net.sdvn.nascommon.utils.log.Logger;

import java.io.File;
import java.util.List;

/**
 * Created by gaoyun@eli-tech.com on 2016/3/1.
 */
public class DeleteFileAPI {
    private static final String TAG = DeleteFileAPI.class.getSimpleName();

    public boolean delete(@NonNull List<LocalFile> fileList) {
        boolean result = true;
        for (LocalFile file : fileList) {
            result = result && delete(file.getFile());
        }

        return result;
    }

    private boolean delete(@Nullable File file) {
        if (null == file || !file.exists()) {
            return true;
        }

        boolean result = true;
        if (file.isDirectory()) {
            Logger.LOGI(TAG, "Is directory: " + file);
            File[] subFiles = file.listFiles();
            if (subFiles != null) {
                for (File f : subFiles) {
                    result = result && delete(f);
                }
            }
        }

        return result && file.delete();
    }

}
