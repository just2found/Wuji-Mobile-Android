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
public class MoveFileAPI {
    private static final String TAG = MoveFileAPI.class.getSimpleName();

    public enum MoveFileException {
        FILE_IS_NULL,
        FILE_NOT_FOUND,
        CAN_NOT_READ,
        CAN_NOT_WRITE,
        TARGET_FILE_EXIST,
        MOVE_ERROR,
        MOVE_FAILED,
        MOVE_ILLEGAL
    }

    @Nullable
    public MoveFileException move(@Nullable List<LocalFile> moveList, @Nullable String toPath) {
        if (null == moveList || null == toPath) {
            return MoveFileException.FILE_IS_NULL;
        }

        MoveFileException ex = null;
        for (LocalFile lf : moveList) {
            File srcFile = lf.getFile();
            ex = checkMove(srcFile, toPath);
            if (ex != null) {
                break;
            }

            ex = move(srcFile, toPath);
        }

        return ex;
    }

    private MoveFileException doMoveFile(@NonNull File srcFile, File toFile) {
        if (toFile.exists()) {
            Logger.LOGE(TAG, "target file is exist");
            return MoveFileException.TARGET_FILE_EXIST; // target file is exist
        }

        if (!srcFile.renameTo(toFile)) {
            return MoveFileException.MOVE_FAILED;
        }

        return null;
    }

    private MoveFileException move(File srcFile, String toPath) {
        if (srcFile.isDirectory()) {
            if (!srcFile.mkdirs()) {
                return MoveFileException.CAN_NOT_WRITE;
            }
            File[] subFiles = srcFile.listFiles();
            if (null != subFiles) {
                for (File f : subFiles) {
                    MoveFileException ex = move(f, toPath);
                    if (ex != null) {
                        return ex;
                    }
                }
            }
        } else {
            return doMoveFile(srcFile, new File(toPath, srcFile.getName()));
        }

        return null;
    }


    private MoveFileException checkMove(@Nullable File srcFile, @Nullable String toPath) {
        if (null == srcFile || null == toPath) {
            Logger.LOGE(TAG, "operate file is null");
            return MoveFileException.FILE_IS_NULL; // file is null
        }
        if (!srcFile.exists()) {
            Logger.LOGE(TAG, "src file is not exist");
            return MoveFileException.FILE_NOT_FOUND;
        }
        if (!srcFile.canWrite()) {
            Logger.LOGE(TAG, "src file can not read");
            return MoveFileException.CAN_NOT_WRITE;
        }

        File toDir = new File(toPath);
        if (!toDir.exists()) {
            if (!toDir.mkdirs()) {
                Logger.LOGE(TAG, "new folder failed");
                return MoveFileException.CAN_NOT_WRITE; // new folder failed
            }
        }
        if (!toDir.canWrite()) {
            Logger.LOGE(TAG, "target dir can not write");
            return MoveFileException.CAN_NOT_WRITE;
        }

        if (toPath.contains(srcFile.getAbsolutePath())) {
            Logger.LOGE(TAG, "copy action illegal");
            return MoveFileException.MOVE_ILLEGAL;
        }

        return null;
    }

}
