package net.linkmate.app.ui.nas.helper

import net.linkmate.app.R
import net.sdvn.nascommon.model.FileTypeItem
import net.sdvn.nascommon.model.phone.LocalFileType

/** 

Created by admin on 2020/7/31,16:43

 */
object UpdateFileTypeHelper {
    private val mUploadTypeList = ArrayList<FileTypeItem>()
    init {
        //--------------------UploadType------------------------//
        val pic = FileTypeItem(R.string.file_type_pic,
                R.drawable.icon_device_img_new, 0, LocalFileType.PICTURE)
        mUploadTypeList.add(pic)
        val video = FileTypeItem(R.string.file_type_video,
                R.drawable.icon_device_vedio_new, 0, LocalFileType.VIDEO)
        mUploadTypeList.add(video)
        val audio = FileTypeItem(R.string.file_type_audio,
                R.drawable.icon_device_music_new, 0, LocalFileType.AUDIO)
        mUploadTypeList.add(audio)
        val doc = FileTypeItem(R.string.file_type_doc,
                R.drawable.icon_device_doc_new, 0, LocalFileType.DOC)
        mUploadTypeList.add(doc)
        val all = FileTypeItem(R.string.file_type_all,
                R.drawable.icon_device_folder_new, 0, LocalFileType.PRIVATE)
        mUploadTypeList.add(all)
    }
    val addItem = FileTypeItem(R.string.action_new_folder,
            R.drawable.icon_device_newfolder_new, 0, LocalFileType.NEW_FOLDER)

    val offlineDownloadItem = FileTypeItem(R.string.action_offline_download,
            R.drawable.icon_plug_pt, 0, LocalFileType.OFFLINE_DOWNLOAD)

    fun getFileTypes() = mUploadTypeList.toList()
}