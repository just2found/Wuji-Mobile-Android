package net.linkmate.app.ui.simplestyle.device.download_offline.data

import com.chad.library.adapter.base.entity.MultiItemEntity
import net.linkmate.app.ui.simplestyle.device.download_offline.adapter.OdTaskDetailAdapter

class TaskDetailFolder(val dirName: String) : MultiItemEntity  {

    val folderList = mutableListOf<TaskDetailFolder>()//存放子类文件
    val fileList = mutableListOf<OfflineDownLoadFile>()//存放子类文件夹

    override fun getItemType(): Int {
        return OdTaskDetailAdapter.ITEM_FOLDER
    }

    fun getParentDir(): String? {
        return if (!dirName.contains("/")) {
            null
        } else {
            dirName.substring(0, dirName.lastIndexOf("/"))
        }
    }
}
