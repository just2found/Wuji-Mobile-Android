package net.linkmate.app.ui.nas.helper

import net.linkmate.app.R
import net.sdvn.nascommon.model.FileTypeItem
import net.sdvn.nascommon.model.oneos.OneOSFileType

/** 

Created by admin on 2020/7/31,16:41

 */
object FileTypeHelper {
    //文件类型集

    fun getFileTypes(): List<FileTypeItem> {
        val mFileTypeList = mutableListOf<FileTypeItem>()
        val privateItem = FileTypeItem(R.string.root_dir_name_private,
                R.drawable.icon_file_my_space, 0, OneOSFileType.PRIVATE)
        mFileTypeList.add(privateItem)
        val publicItem = FileTypeItem(R.string.root_dir_name_public,
                R.drawable.icon_file_shared_space, 0, OneOSFileType.PUBLIC)
        mFileTypeList.add(publicItem)
        val recycleItem = FileTypeItem(R.string.file_type_cycle,
                R.drawable.icon_file_recycle, 0, OneOSFileType.RECYCLE)
        mFileTypeList.add(recycleItem)

        val picItem = FileTypeItem(R.string.file_type_pic,
                R.drawable.icon_file_image, 0, OneOSFileType.PICTURE)
        mFileTypeList.add(picItem)
        val videoItem = FileTypeItem(R.string.file_type_video,
                R.drawable.icon_file_video, 0, OneOSFileType.VIDEO)
        mFileTypeList.add(videoItem)
        val audioItem = FileTypeItem(R.string.file_type_audio,
                R.drawable.icon_file_audio, 0, OneOSFileType.AUDIO)
        mFileTypeList.add(audioItem)
        val docItem = FileTypeItem(R.string.file_type_doc,
                R.drawable.icon_file_doc, 0, OneOSFileType.DOCUMENTS)
        mFileTypeList.add(docItem)
        return mFileTypeList
    }

    fun getDirs(): List<FileTypeItem> {
        val dirs = ArrayList<FileTypeItem>()
        val privateItem = FileTypeItem(R.string.root_dir_name_private,
                R.drawable.icon_file_my_space, 0, OneOSFileType.PRIVATE)
        dirs.add(privateItem)
        val publicItem = FileTypeItem(R.string.root_dir_name_public,
                R.drawable.icon_file_shared_space, 0, OneOSFileType.PUBLIC)
        dirs.add(publicItem)
        val recycleItem = FileTypeItem(R.string.file_type_cycle,
                R.drawable.icon_file_recycle, 0, OneOSFileType.RECYCLE)
        dirs.add(recycleItem)
        return dirs
    }

    fun getSpace(): List<FileTypeItem> {
        val dirs = ArrayList<FileTypeItem>()
        val privateItem = FileTypeItem(R.string.root_dir_name_private,
                R.drawable.icon_file_my_space, 0, OneOSFileType.PRIVATE)
        dirs.add(privateItem)
        val publicItem = FileTypeItem(R.string.root_dir_name_public,
                R.drawable.icon_file_shared_space, 0, OneOSFileType.PUBLIC)
        dirs.add(publicItem)
        val recycleItem = FileTypeItem(R.string.file_type_cycle,
                R.drawable.icon_file_recycle, 0, OneOSFileType.RECYCLE)
        dirs.add(recycleItem)
        return dirs
    }

    fun getMedia(): List<FileTypeItem> {
        val media: MutableList<FileTypeItem> = mutableListOf<FileTypeItem>()
        val picItem = FileTypeItem(R.string.file_type_pic,
                R.drawable.icon_file_image_45dp, 0, OneOSFileType.PICTURE)
        media.add(picItem)
        val videoItem = FileTypeItem(R.string.file_type_video,
                R.drawable.icon_file_video_45dp, 0, OneOSFileType.VIDEO)
        media.add(videoItem)
        val audioItem = FileTypeItem(R.string.file_type_audio,
                R.drawable.icon_file_music_45dp, 0, OneOSFileType.AUDIO)
        media.add(audioItem)
        val docItem = FileTypeItem(R.string.file_type_doc,
                R.drawable.icon_file_doc_45dp, 0, OneOSFileType.DOCUMENTS)
        media.add(docItem)
        return media
    }

    fun getDocuments(): List<FileTypeItem> {
        return mutableListOf<FileTypeItem>(
            FileTypeItem(R.string.all, R.drawable.icon_file_doc,  0, OneOSFileType.DOCUMENTS),
            FileTypeItem(R.string.doc, R.drawable.icon_file_doc, 0, OneOSFileType.DOC),
            FileTypeItem(R.string.xls, R.drawable.icon_file_doc, 0, OneOSFileType.XLS),
            FileTypeItem(R.string.ppt, R.drawable.icon_file_doc, 0, OneOSFileType.PPT),
            FileTypeItem(R.string.pdf, R.drawable.icon_file_doc, 0, OneOSFileType.PDF)
        ).toList()
    }

}