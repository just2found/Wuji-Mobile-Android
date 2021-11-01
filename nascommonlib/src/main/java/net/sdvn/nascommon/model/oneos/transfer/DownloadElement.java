package net.sdvn.nascommon.model.oneos.transfer;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import net.sdvn.nascommon.constant.AppConstants;
import net.sdvn.nascommon.constant.OneOSAPIs;
import net.sdvn.nascommon.model.oneos.OneOSFile;


import java.io.File;

/**
 * Class for download file
 * <p/>
 * Created by gaoyun@eli-tech.com on 2016/2/18.
 */
public class DownloadElement extends TransferElement  {

    private OneOSFile file;
    // needs check phone space
//    private boolean check = true;

    // downloading file temporary name
//    @Nullable
//    private String tmpName = null;
    // downloaded actual name
    @Nullable
    private String toName = null;

    public DownloadElement(Long id) {
        super(id);
    }

    public DownloadElement(@NonNull OneOSFile file, String downloadPath) {
        this(file, downloadPath, 0);
    }

    public DownloadElement(@NonNull OneOSFile file, String downloadPath, long offset) {
        this(file, downloadPath, offset, null);

    }

    public DownloadElement(@NonNull OneOSFile file, String downloadPath, long offset, @Nullable String tmpName) {
        super(0);
        this.file = file;
        this.toPath = downloadPath;
        this.offset = offset;
        this.check = true;
        if (tmpName == null)
            this.tmpName = "." + file.getName() + "_" + file.getTime() + AppConstants.TMP;
        else
            this.tmpName = tmpName;
    }


    /**
     * Whether is download file
     *
     * @return {@code true} if download, {@code false} otherwise.
     */
    @Override
    public boolean isDownload() {
        return true;
    }

    /**
     * Get transmission source file path
     */
    @Override
    public String getSrcPath() {
        //        if (file.getShare_path_type() == io.weline.repo.files.constant.AppConstants.PUBLIC_SHARE_PATH_TYPE) {
//            //V5特殊处理
//            boolean isStartsWithPublic = path.startsWith(OneOSAPIs.ONE_OS_PUBLIC_ROOT_DIR);
//            if (!isStartsWithPublic) {
//                //没有public开头，前面加一个
//                path = OneOSAPIs.ONE_OS_PUBLIC_ROOT_DIR.replace("/", "") + path;
//            }
//        }
        return file.getAllPath();
    }

    /**
     * Get transmission source file name
     */
    @Override
    public String getSrcName() {
        return file.getName();
    }

    /**
     * Get transmission source file size
     */
    @Override
    public long getSize() {
        return file.getSize();
    }

    @NonNull
    @Override
    public String getTag() {
        return getSrcDevId() + getSrcPath() + getToPath();
    }

    @NonNull
    public File getDownloadFile() {
        return new File(toPath + File.separator + getToName());
    }

//    /**
//     * Get downloading file temporary name
//     *
//     * @return temporary file name
//     */
//    @Nullable
//    public String getTmpName() {
//        return tmpName;
//    }
//
//    public void setTmpName(String tmpName) {
//        this.tmpName = tmpName;
//    }

    // ===============getter and setter method======================
    public OneOSFile getFile() {
        return file;
    }

    public void setFile(OneOSFile file) {
        this.file = file;
    }


    @Nullable
    public String getToName() {
        return toName;
    }

    public void setToName(String toName) {
        this.toName = toName;
    }

    public String getSrcDevId() {
        return this.devId;
    }

    public void setSrcDevId(String srcDevId) {
        this.devId = srcDevId;
    }


    // ===============getter and setter method======================
}
