package net.sdvn.nascommon.model.phone;


import androidx.annotation.Keep;

import net.sdvn.nascommonlib.R;

import java.io.Serializable;

@Keep
public enum LocalFileType implements Serializable {
    /**
     * 文件目录
     */
    PRIVATE,
    /**
     * 下载目录
     */
    DOWNLOAD,
    /**
     * 文档
     */
    DOC,
    /**
     * 图片
     */
    PICTURE,
    /**
     * 视频
     */
    VIDEO,
    /**
     * 音频
     */
    AUDIO,
    /**
     * 安装包
     */
    APP,
    /**
     * 压缩包
     */
    ZIP,
    NEW_FOLDER,
    OFFLINE_DOWNLOAD,
    MOVE_IN;

    public static int getTypeName(LocalFileType type) {
        int name = R.string.root_dir_name_private;
        if (type == DOWNLOAD) {
            name = R.string.file_type_download;
        } else if (type == DOC) {
            name = R.string.file_type_doc;
        } else if (type == VIDEO) {
            name = R.string.file_type_video;
        } else if (type == AUDIO) {
            name = R.string.file_type_audio;
        } else if (type == PICTURE) {
            name = R.string.file_type_pic;
        } else if (type == APP) {
            name = R.string.file_type_app;
        }

        return name;
    }

    public static int getTypeTitle(LocalFileType type) {
        int name = R.string.title_select_all;
        if (type == PICTURE) {
            name = R.string.title_select_pic;
        } else if (type == VIDEO) {
            name = R.string.title_select_video;
        } else if (type == AUDIO) {
            name = R.string.title_select_audio;
        } else if (type == DOC) {
            name = R.string.title_select_doc;
        } else if (type == APP) {
            name = R.string.title_select_app;
        }
        return name;
    }
}
