package net.sdvn.nascommon.model.oneos.backup.file;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;

import net.sdvn.nascommon.constant.AppConstants;
import net.sdvn.nascommon.db.objecbox.BackupFile;
import net.sdvn.nascommon.model.oneos.backup.BackupType;
import net.sdvn.nascommon.model.oneos.transfer.UploadElement;
import net.sdvn.nascommon.model.oneos.transfer.thread.Priority;
import net.sdvn.nascommon.utils.FileUtils;

import java.io.File;

@Keep
public class BackupElement extends UploadElement {
    private BackupFile backupInfo;

    //    public BackupElement(BackupFile backupInfo, File file, String uploadPath, boolean overwrite) {
//        super(file, uploadPath, overwrite);
//        this.backupInfo = backupInfo;
//    }

    public BackupElement(BackupFile info, @NonNull File file, boolean check) {
        super(file, "", check);
        this.backupInfo = info;
        setPriority(Priority.BG_LOW);
        File backupDir = new File(info.getPath());
        // 相对路径
        if (info.getType() == BackupType.ALBUM) {  // 相册备份
            String relativeDir = file.getParent().replaceFirst(backupDir.getAbsolutePath(), "");
            String cameraDate;
            if (FileUtils.isPictureFile(file.getName())) {
                cameraDate = FileUtils.getPhotoDate(file);
            } else {
                cameraDate = FileUtils.getVideoDate(file);
            }
            // 相册路径： /来自：MI4/Album/RelativeDir/2015-09/xxx.png
            String toPath = AppConstants.BACKUP_FILE_ONEOS_ROOT_DIR_NAME_ALBUM + relativeDir + File.separator + cameraDate + File.separator;
            setToPath(toPath);
        } else {
            String relativeDir = file.getParent().replaceFirst(backupDir.getParent(), "");
            // 文件路径： /来自：MI4/Files/RelativeDir/xxx.txt
            String toPath = AppConstants.BACKUP_FILE_ONEOS_ROOT_DIR_NAME_FILES + relativeDir + File.separator;
            setToPath(toPath);
        }
        setToDevId(info.getDevUUID());
    }

    public BackupFile getBackupInfo() {
        return backupInfo;
    }

    public void setBackupInfo(BackupFile backupInfo) {
        this.backupInfo = backupInfo;
    }


}