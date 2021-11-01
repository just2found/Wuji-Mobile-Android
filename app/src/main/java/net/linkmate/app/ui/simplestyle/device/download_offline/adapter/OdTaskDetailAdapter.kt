package net.linkmate.app.ui.simplestyle.device.download_offline.adapter

import com.chad.library.adapter.base.BaseMultiItemQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.chad.library.adapter.base.entity.MultiItemEntity
import net.linkmate.app.R
import net.linkmate.app.ui.simplestyle.device.download_offline.data.OfflineDownLoadFile
import net.linkmate.app.ui.simplestyle.device.download_offline.data.OfflineDownLoadTask
import net.linkmate.app.ui.simplestyle.device.download_offline.data.TaskDetailFolder
import net.sdvn.nascommon.utils.FileUtils

/**
create by: 86136
create time: 2021/2/26 15:23
Function description:
 */

class OdTaskDetailAdapter(data: List<MultiItemEntity>?) : BaseMultiItemQuickAdapter<MultiItemEntity, BaseViewHolder>(data) {

    companion object {
        const val ITEM_FOLDER = 0 //文件夹类型
        const val ITEM_FILE = 1 //文件类型
    }

    val folderList = mutableListOf<OfflineDownLoadFile>()//用于存放选中状态有改变的文件

    init {
        addItemType(ITEM_FOLDER, R.layout.item_offline_detail_folder)
        addItemType(ITEM_FILE, R.layout.item_offline_detail_file)
    }

    override fun convert(holder: BaseViewHolder, entity: MultiItemEntity) {
        when (entity.itemType) {
            ITEM_FOLDER -> {
                if (entity is TaskDetailFolder) {
                    holder.setText(R.id.file_name, entity.dirName)
                }
            }
            ITEM_FILE -> {
                if (entity is OfflineDownLoadFile) {
                    holder.setImageResource(R.id.fileImage, FileUtils.fmtFileIcon(entity.filename))
                    holder.setText(R.id.file_name, entity.filename)
                    holder.setProgress(R.id.progress, ((entity.curSize * 100) / entity.totalSize).toInt())
                    if (entity.curSize > 0) {
                        holder.setText(R.id.file_size_tv, "${FileUtils.fmtFileSize(entity.curSize)}/${FileUtils.fmtFileSize(entity.totalSize)}")
                    } else {
                        holder.setText(R.id.file_size_tv, "0/${FileUtils.fmtFileSize(entity.totalSize)}")
                    }
                    holder.setChecked(R.id.file_select, entity.status == OfflineDownLoadFile.START)
                    holder.itemView.setOnClickListener {
                        //如果改变则加入，改回来就移除
                        if (folderList.contains(entity)) {
                            folderList.remove(entity)
                        } else {
                            folderList.add(entity)
                        }
                        if (entity.status == OfflineDownLoadFile.START) {
                            entity.status = OfflineDownLoadFile.SUSPEND
                            holder.setChecked(R.id.file_select, false)
                        } else if (entity.status == OfflineDownLoadFile.SUSPEND) {
                            entity.status = OfflineDownLoadFile.START
                            holder.setChecked(R.id.file_select, true)
                        }
                    }
                }
            }
        }
    }


}