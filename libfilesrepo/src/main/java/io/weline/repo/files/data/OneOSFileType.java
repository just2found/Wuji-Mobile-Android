package io.weline.repo.files.data;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.Serializable;

import io.weline.repo.files.constant.OneOSAPIs;

@Keep
public enum OneOSFileType implements Serializable {
    /**
     * 文件目录
     */
    PRIVATE(OneOSAPIs.ONE_OS_PRIVATE_ROOT_DIR),
    /**
     * 公共目录
     */
    PUBLIC(OneOSAPIs.ONE_OS_PUBLIC_ROOT_DIR),
    /**
     * 回收站
     */
    RECYCLE(OneOSAPIs.ONE_OS_RECYCLE_ROOT_DIR),
    /**
     * 文档
     */
    DOC("doc"),
    /**
     * 图片
     */
    PICTURE("pic"),
    /**
     * 视频
     */
    VIDEO("video"),
    /**
     * 音频
     */
    AUDIO("audio"),
    /**
     * 文件夹
     */
    DIR("dir"),
    /**
     * 图片时间轴
     */
    IMAGE_TIMELINE("pic"),
    /**
     * 智能相框
     */
    PHOTOS_FRAME("all"),
    /**
     * 所有
     */
    ALL("all");

    private String serverTypeName;

    OneOSFileType(String name) {
        this.serverTypeName = name;

    }

    public static String getServerTypeName(OneOSFileType type) {
        if (type == ALL) {
            return "all";
        } else if (type == DOC) {
            return "doc";
        } else if (type == VIDEO) {
            return "video";
        } else if (type == AUDIO) {
            return "audio";
        } else {
            return "pic";
        }
    }


    @Nullable
    public static String getRootPath(@Nullable OneOSFileType type) {
        String path = null;
        if (type == OneOSFileType.PRIVATE) {
            path = OneOSAPIs.ONE_OS_PRIVATE_ROOT_DIR;
        } else if (type == OneOSFileType.PUBLIC) {
            path = OneOSAPIs.ONE_OS_PUBLIC_ROOT_DIR;
        } else if (type == OneOSFileType.RECYCLE) {
            path = OneOSAPIs.ONE_OS_RECYCLE_ROOT_DIR;
        }

        return path;
    }


    @NonNull
    public static OneOSFileType getTypeByPath(@NonNull String path) {
        if (path.startsWith(OneOSAPIs.ONE_OS_PUBLIC_ROOT_DIR)) {
            return PUBLIC;
        } else if (path.startsWith(OneOSAPIs.ONE_OS_RECYCLE_ROOT_DIR)) {
            return RECYCLE;
        }
        return PRIVATE;
    }


    public static boolean isDB(@NonNull OneOSFileType mFileType) {
        return mFileType.ordinal() >= DOC.ordinal()
                && mFileType.ordinal() <= AUDIO.ordinal();
    }

    public static boolean isDir(@NonNull OneOSFileType fileType) {
        return fileType.ordinal() <= RECYCLE.ordinal();
    }

    public String getServerTypeName() {
        return serverTypeName;
    }
}
