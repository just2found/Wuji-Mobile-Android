package net.sdvn.nascommon.model.oneos;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import net.sdvn.nascommon.constant.OneOSAPIs;
import net.sdvn.nascommon.utils.Utils;
import net.sdvn.nascommonlib.R;

import java.io.File;
import java.io.Serializable;

/**
 * 文件分类
 * 请勿随意调整顺序
 */
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
     * 外部存储
     */
    EXTERNAL_STORAGE(OneOSAPIs.ONE_OS_USB_ROOT_DIR),
    /**
     * 群组空间
     */
    GROUP(OneOSAPIs.ONE_OS_GROUP_ROOT_DIR),
    /**
     * 收藏
     */
    FAVORITES("favorites"),

    /**
     * 搜索
     */
    SEARCH("search"),

    /**
     * 保险箱
     */
    SAFE("safebox"),

    /**
     * 文档
     */
    DOCUMENTS("Documents"),
    DOC("doc"),
    XLS("xls"),
    PPT("ppt"),
    PDF("pdf"),
    TXT("txt"),
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

    DIR("dir"),
    /**
     * 离线下载时候需要用到的
     */
    OFFLINE_DOWNLOAD("offline_download"),
    TORRENT("bt"),
    //选择为文件类型
    FOLDER("folder"),
    /**
     * 所有
     */
    ALL("all");

    private String serverTypeName;

    OneOSFileType(String name) {
        this.serverTypeName = name;

    }

    /**
     * @see #getServerTypeName()
     */
    @Deprecated
    public static String getServerTypeName(OneOSFileType type) {
        if (type == ALL) {
            return "all";
        } else if (type == DOCUMENTS) {
            return "doc";
        } else if (type == VIDEO) {
            return "video";
        } else if (type == AUDIO) {
            return "audio";
        } else {
            return "pic";
        }
    }

    public static int getTypeName(OneOSFileType type) {
        int name = R.string.root_dir_name_private;
        if (type == PUBLIC) {
            name = R.string.root_dir_name_public;
        } else if (type == RECYCLE) {
            name = R.string.file_type_cycle;
        } else if (type == DOCUMENTS) {
            name = R.string.file_type_doc;
        } else if (type == VIDEO) {
            name = R.string.file_type_video;
        } else if (type == AUDIO) {
            name = R.string.file_type_audio;
        } else if (type == PICTURE) {
            name = R.string.file_type_pic;
        }

        return name;
    }

    @Nullable
    public static String getRootPath(OneOSFileType type) {
        String path = null;
        if (type == OneOSFileType.PRIVATE) {
            path = OneOSAPIs.ONE_OS_PRIVATE_ROOT_DIR;
        } else if (type == OneOSFileType.PUBLIC) {
            path = OneOSAPIs.ONE_OS_PUBLIC_ROOT_DIR;
        } else if (type == OneOSFileType.RECYCLE) {
            path = OneOSAPIs.ONE_OS_RECYCLE_ROOT_DIR;
        } else if (type == OneOSFileType.SAFE) {
            path = OneOSAPIs.ONE_OS_SAFE_ROOT_DIR;
        } else if (type == OneOSFileType.EXTERNAL_STORAGE) {
            path = OneOSAPIs.ONE_OS_EXT_STORAGE_ROOT_DIR;
        }

        return path;
    }

    @NonNull
    public static String getPathWithTypeName(@NonNull String path) {
//        (R.string.root_dir_name_private);
//        (R.string.root_dir_name_public);
//        (R.string.root_dir_name_recycle);
        String pathWithTypeName = path;
        if (!path.endsWith(File.separator)) {
            path = path + File.separator;
        }
        if (path.startsWith(OneOSAPIs.ONE_OS_RECYCLE_ROOT_DIR)) {
            pathWithTypeName = Utils.getApp().
                    getString(R.string.root_dir_name_recycle)
                    + path.replaceFirst(OneOSAPIs.ONE_OS_RECYCLE_ROOT_DIR
                    , OneOSAPIs.ONE_OS_PRIVATE_ROOT_DIR);
        } else if (path.startsWith(OneOSAPIs.ONE_OS_PRIVATE_ROOT_DIR)) {
            pathWithTypeName = Utils.getApp().
                    getString(R.string.root_dir_name_private) + path;
        } else if (path.startsWith(OneOSAPIs.ONE_OS_PUBLIC_ROOT_DIR)) {
            pathWithTypeName = Utils.getApp().
                    getString(R.string.root_dir_name_public)
                    + path.replaceFirst(OneOSAPIs.ONE_OS_PUBLIC_ROOT_DIR
                    , OneOSAPIs.ONE_OS_PRIVATE_ROOT_DIR);
        } else if (path.startsWith(OneOSAPIs.ONE_OS_SAFE_ROOT_DIR)) {
            pathWithTypeName = Utils.getApp().
                    getString(R.string.root_dir_name_safe_box)
                    + path.replaceFirst(OneOSAPIs.ONE_OS_SAFE_ROOT_DIR
                    , OneOSAPIs.ONE_OS_PRIVATE_ROOT_DIR);
        } else if (path.startsWith(OneOSAPIs.ONE_OS_EXT_STORAGE_ROOT_DIR)) {
            pathWithTypeName = Utils.getApp().
                    getString(R.string.external_storage)
                    + path.replaceFirst(OneOSAPIs.ONE_OS_EXT_STORAGE_ROOT_DIR
                    , OneOSAPIs.ONE_OS_PRIVATE_ROOT_DIR);
        } else if (path.startsWith(OneOSAPIs.ONE_OS_GROUP_ROOT_DIR)) {
            pathWithTypeName = Utils.getApp().
                    getString(R.string.group)
                    + path.replaceFirst(OneOSAPIs.ONE_OS_GROUP_ROOT_DIR
                    , OneOSAPIs.ONE_OS_PRIVATE_ROOT_DIR);
        }
        return pathWithTypeName;
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

    public static int getTypeTitle(OneOSFileType type) {
        int name = R.string.title_select_all;
        if (type == PICTURE) {
            name = R.string.title_select_pic;
        } else if (type == VIDEO) {
            name = R.string.title_select_video;
        } else if (type == AUDIO) {
            name = R.string.title_select_audio;
        } else if (type == DOCUMENTS) {
            name = R.string.title_select_doc;
        }
        return name;
    }

    public static boolean isDB(@NonNull OneOSFileType mFileType) {
        return mFileType.ordinal() >= DOCUMENTS.ordinal()
                && mFileType.ordinal() <= AUDIO.ordinal();
    }

    public static boolean isDir(@NonNull OneOSFileType fileType) {
        return fileType.ordinal() <= EXTERNAL_STORAGE.ordinal();
    }
    public static boolean isSharedDir(@NonNull OneOSFileType fileType) {
        return fileType.ordinal() <= PUBLIC.ordinal();
    }
    public String getServerTypeName() {
        return serverTypeName;
    }
}
