package net.sdvn.nascommon.model.oneos.backup.info;

/**
 * Created by gaoyun@eli-tech.com on 2016/2/25.
 */
public interface OnBackupInfoListener {
    void onStart(BackupInfoType type);

    void onBackup(BackupInfoType type, BackupInfoStep step, int progress);

    void onComplete(BackupInfoType type, BackupInfoException exception);
}
