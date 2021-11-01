package net.sdvn.nascommon.model;

import androidx.annotation.Keep;

/**
 * Created by gaoyun@eli-tech.com on 2016/1/20.
 */
@Keep
public enum FileManageAction {
    ATTRIBUTES,
    OPEN,
    COPY,
    MOVE,
    DELETE,
    DELETE_SHIFT,
    RENAME,
    MKDIR,
    DOWNLOAD,
    UPLOAD,
    ENCRYPT,
    DECRYPT,
    EXTRACT,
    CLEAN_RECYCLE,
    SHARE,
    CHMOD,
    MORE,
    BACK,
    //    SHARING,
    READTXT,
    RESTORE_RECYCLE,
    TORRENT_CREATE,
    ADD_PF_TAG,
    SEARCH,
    ARCHIVER,
    FAVORITE,
    UNFAVORITE;
}
