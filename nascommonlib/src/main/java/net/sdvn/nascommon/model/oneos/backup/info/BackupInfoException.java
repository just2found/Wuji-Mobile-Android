package net.sdvn.nascommon.model.oneos.backup.info;

import androidx.annotation.Keep;

/**
 * Created by gaoyun@eli-tech.com on 2016/2/25.
 */
@Keep
public enum BackupInfoException {
    NO_BACKUP, NO_RECOVERY, ERROR_EXPORT, ERROR_IMPORT, UPLOAD_ERROR, DOWNLOAD_ERROR
}
