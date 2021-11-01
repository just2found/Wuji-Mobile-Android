package net.linkmate.app.ui.simplestyle.device.download_offline.data

import androidx.annotation.Keep
import com.chad.library.adapter.base.entity.MultiItemEntity
import com.google.gson.annotations.SerializedName
import net.linkmate.app.ui.simplestyle.device.download_offline.adapter.OdTaskDetailAdapter

@Keep
data class OfflineDownLoadFile(
        @field:SerializedName("dirname") val dirname: String,// 目录名(文件夹名)
        @field:SerializedName("filename") val filename: String, // 对应文件夹内的文件
        @field:SerializedName("status") var status: Int,// 0表示暂停下载，1表示正在下载
        @field:SerializedName("totalSize") val totalSize: Long,  // 子文件总大小
        @field:SerializedName("curSize") val curSize: Long,//子文件当前已下载大小
        @field:SerializedName("percent") val percent: String // 子文件当前下载完成百分比
) : MultiItemEntity {


    companion object {
        const val START = 1
        const val SUSPEND = 0
    }

    override fun getItemType(): Int {
        return OdTaskDetailAdapter.ITEM_FILE
    }


}


