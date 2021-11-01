package net.sdvn.nascommon.model.oneos.transfer;

import android.content.ContentResolver;
import android.net.Uri;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.documentfile.provider.DocumentFile;

import net.sdvn.nascommon.utils.EmptyUtils;
import net.sdvn.nascommon.utils.Utils;

import java.io.File;
import java.util.Objects;

/**
 * Created by gaoyun@eli-tech.com on 2016/2/18.
 */
@Keep
public class UploadElement extends TransferElement {
    private File file;
    // needs check if file exist
//    protected boolean check = false;
    // overwrite if target file exist
    private boolean overwrite = false;
    //    创建下载任务时保存uid，使之不影响切盘时的下载
//    private long uid = -1;
    private String name = "";
    private long size = 0;

    public UploadElement(File file, String uploadPath) {
        this(file, uploadPath, false);
    }

    public UploadElement(File file, String uploadPath, boolean check) {
        this(file.getAbsolutePath(), uploadPath, check);
        this.file = file;
        initElementParams();
    }

    public UploadElement(Long id) {
        super(id);
    }

    public UploadElement(String path, String uploadPath, boolean check) {
        super(0);
        this.srcPath = path;
        this.toPath = uploadPath;
        this.check = check;
        initElementParams();
    }

    private void initElementParams() {
        final Uri uri = Uri.parse(srcPath);
        if (Objects.equals(ContentResolver.SCHEME_CONTENT, uri.getScheme())) {
            final DocumentFile documentFile = DocumentFile.fromSingleUri(Utils.getApp(), uri);
            if (documentFile != null) {
                name = documentFile.getName();
                size = documentFile.length();
            }
        } else if (file == null && Objects.equals(ContentResolver.SCHEME_FILE, uri.getScheme())) {
            this.file = new File(srcPath);
        }
        if (file != null) {
            name = file.getName();
            size = file.length();
        }
        if (EmptyUtils.isEmpty(name)) {
            name = "";
        }
    }

//    public long getUid() {
//        return uid;
//    }
//
//    public void saveUid(long uid) {
//        this.uid = uid;
//    }

//    public boolean isUploadToPrivateDir() {
//        return toPath.startsWith("/");
//    }

    /**
     * Whether is download file
     *
     * @return true or false
     */
    @Override
    public boolean isDownload() {
        return false;
    }

    /**
     * Get transmission source file path
     */
    @NonNull
    @Override
    public String getSrcPath() {
        return srcPath;
    }

    /**
     * Get transmission source file name
     */
    @NonNull
    @Override
    public String getSrcName() {
        return name;
    }

    /**
     * Get transmission source file size
     */
    @Override
    public long getSize() {
        return size;
    }

    @NonNull
    @Override
    public String getTag() {
        return getSrcPath() + getToDevId() + getToPath();
    }

    @NonNull
    @Override
    public String toString() {
        return "{src:" + srcPath + ", target:" + toPath + ", overwrite:" + overwrite + "}";
    }

    // ===============getter and setter method======================
    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
        initElementParams();
    }


    public boolean isOverwrite() {
        return overwrite;
    }

    public void setOverwrite(boolean overwrite) {
        this.overwrite = overwrite;
    }

    public String getToDevId() {
        return this.devId;
    }

    public void setToDevId(String toDevId) {
        this.devId = toDevId;
    }

    // ===============getter and setter method======================

}
