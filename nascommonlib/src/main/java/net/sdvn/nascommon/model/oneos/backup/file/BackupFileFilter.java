package net.sdvn.nascommon.model.oneos.backup.file;

import androidx.annotation.NonNull;

import net.sdvn.nascommon.utils.FileUtils;

import java.io.File;
import java.io.FileFilter;

/**
 * Created by gaoyun@eli-tech.com on 2016/2/24.
 */
public class BackupFileFilter implements FileFilter {

    private boolean isBackupAlbum;
    private long lastBackupTime;

    public BackupFileFilter(boolean isBackupAlbum, long lastBackupTime) {
        this.lastBackupTime = lastBackupTime;
        this.isBackupAlbum = isBackupAlbum;
    }

    /**
     * Indicating whether a specific file should be included in a pathname list.
     *
     * @param file the abstract file to check.
     * @return {@code true} if the file should be included, {@code false}
     * otherwise.
     */
    @Override
    public boolean accept(@NonNull File file) {
        if (file.isHidden()) {
            return false;
        }
        if (file.isDirectory()) {
            return true;
        }
        if (file.lastModified() <= lastBackupTime) {
            return false;
        }
        return !(isBackupAlbum && !FileUtils.isPictureOrVideo(file));
    }
}
