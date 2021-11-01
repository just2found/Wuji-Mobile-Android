package net.linkmate.app.ui.simplestyle.device.download_offline.adapter

import android.text.TextUtils
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.chad.library.adapter.base.BaseMultiItemQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.chad.library.adapter.base.entity.MultiItemEntity
import net.linkmate.app.R
import net.linkmate.app.ui.simplestyle.device.download_offline.data.OfflineDownLoadTask
import net.linkmate.app.ui.simplestyle.device.download_offline.data.TaskListTitle
import net.sdvn.nascommon.model.oneos.OneOSFileType
import net.sdvn.nascommon.utils.FileUtils

/**
create by: 86136
create time: 2021/2/26 15:23
Function description:
 */

class OdTaskListAdapter(data: List<MultiItemEntity>?) : BaseMultiItemQuickAdapter<MultiItemEntity, BaseViewHolder>(data) {

    companion object {
        const val PROGRESS_TITLE = 0 //总计
        const val NORMAL_ITEM = 1 //头部
        const val COMPLETE_TITLE = 2 //条目
    }

    init {
        addItemType(PROGRESS_TITLE, R.layout.item_line_string)
        addItemType(NORMAL_ITEM, R.layout.item_offline_transfer)
        addItemType(COMPLETE_TITLE, R.layout.item_line_string)
    }

    var mProgressTitle: TaskListTitle? = null
    var mCompleteTitle: TaskListTitle? = null

    fun findCompleteTitlePosition(): Int {
        mData?.let { list ->
            for ((index, item) in list.withIndex()) {
                item.itemType == COMPLETE_TITLE
                return index
            }
        }
        return -1
    }

    fun removeCompleteUi() {
        val startPosition = findCompleteTitlePosition()
        if (startPosition == -1)//没有找到直接结束
            return
        var flag = false
        val iterator = mData.iterator()
        var count = 0
        if (iterator.hasNext()) {
            val item = iterator.next()
            if (item.itemType == COMPLETE_TITLE) {
                flag = true
            }
            if (flag) {
                iterator.remove()
                count++
            }

        }
        notifyItemRangeRemoved(startPosition, count)

    }


    fun cleanTitle() {
        if (!isHasTrans()) {
            cleanProgressTitle()
        }
        if (!isHasFinish()) {
            cleanFinishTitle()
        }
    }


    private fun isHasTrans(): Boolean {
        mData.forEach {
            if (it.itemType == NORMAL_ITEM && it is OfflineDownLoadTask) {
                if (it.status == OfflineDownLoadTask.START ||
                        it.status == OfflineDownLoadTask.LOAD ||
                        it.status == OfflineDownLoadTask.SUSPEND ||
                        it.status == OfflineDownLoadTask.ERROR)
                    return true
            }
        }
        return false
    }


    private fun cleanProgressTitle() {
        if (mData.size > 0 && mData[0].itemType == PROGRESS_TITLE) {
            remove(0)
        }
    }

    private fun isHasFinish(): Boolean {
        mData.forEach {
            if (it.itemType == NORMAL_ITEM && it is OfflineDownLoadTask) {
                if (it.status == OfflineDownLoadTask.FINISH)
                    return true
            }
        }
        return false
    }

    private fun cleanFinishTitle() {
        if (mData.size > 0 && mData[mData.size - 1].itemType == COMPLETE_TITLE) {
            remove(mData.size - 1)
        }
    }

    override fun convert(holder: BaseViewHolder, item: MultiItemEntity?) {
        when (holder.itemViewType) {
            PROGRESS_TITLE -> {
                if (item is TaskListTitle) {
                    mProgressTitle = item
                    holder.setText(R.id.header, item.title)
                    holder.setText(R.id.more, item.optName)
                }
                holder.addOnClickListener(R.id.more)
            }
            NORMAL_ITEM -> {
                if (item is OfflineDownLoadTask) {
                    holder.setText(R.id.fileName, item.filename)
                    if (TextUtils.isEmpty(item.imgUrl)) {
                        holder.setImageResource(R.id.fileImage, item.defImg)
                    } else {
                        val view = holder.getView<ImageView>(R.id.fileImage)
                        Glide.with(view)
                                .asBitmap()
                                .centerCrop()
                                .placeholder(item.defImg)
                                .load(item.imgUrl)
                                .diskCacheStrategy(DiskCacheStrategy.DATA)
                                .into(view)
                    }
                    var status: String? = null
                    when (item.status) {
                        OfflineDownLoadTask.START -> {
                            if (item.speed > 0) {
                                status = "${FileUtils.fmtFileSize(item.speed)}/s"
                            } else {
                                status = "0B/s"
                            }
                        }
                        OfflineDownLoadTask.LOAD -> {
                            status = holder.itemView.context.getString(R.string.dl_waiting)
                        }
                        OfflineDownLoadTask.SUSPEND -> {
                            status = holder.itemView.context.getString(R.string.paused)
                        }
                        OfflineDownLoadTask.ERROR -> {
                            status = holder.itemView.context.getString(R.string.error)
                        }
                        OfflineDownLoadTask.FINISH -> {
                            val s = if (item.sharePathType == OneOSFileType.PRIVATE.ordinal) holder.itemView.context.getString(R.string.root_dir_name_private)
                            else holder.itemView.context.getString(R.string.root_dir_name_public)
                            if (!TextUtils.isEmpty(item.savePath) && !item.savePath.startsWith("/")) {
                                status = "${s}/${item.savePath}"
                            } else {
                                status = "${s}${item.savePath}"
                            }
                        }
                    }
                    holder.setText(R.id.statues_tv, status)
                    if (item.status != OfflineDownLoadTask.FINISH && item.cursize > 0 && item.filesize > 0) {
                        holder.setVisible(R.id.progress, true)
                        holder.setProgress(R.id.progress, ((item.cursize * 100) / item.filesize).toInt())
                        holder.setText(R.id.file_size_tv, "${FileUtils.fmtFileSize(item.cursize)}/${FileUtils.fmtFileSize(item.filesize)}")
                    } else {
                        holder.setVisible(R.id.progress, false)
                        holder.setProgress(R.id.progress, 0)
                        holder.setText(R.id.file_size_tv, "0/${FileUtils.fmtFileSize(item.filesize)}")
                    }
                    if (item.status == OfflineDownLoadTask.FINISH) {
                        holder.setVisible(R.id.progress, false)
                        holder.setText(R.id.file_size_tv, FileUtils.fmtFileSize(item.filesize))
                    }
                }
            }
            COMPLETE_TITLE -> {
                if (item is TaskListTitle) {
                    mCompleteTitle = item
                    holder.setText(R.id.header, item.title)
                    holder.setText(R.id.more, item.optName)
                    holder.addOnClickListener(R.id.more)
                }
            }
        }
    }
}