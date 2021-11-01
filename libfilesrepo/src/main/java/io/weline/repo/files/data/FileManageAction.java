package io.weline.repo.files.data;

import androidx.annotation.Keep;

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
    SEARCH;
}
