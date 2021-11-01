package net.linkmate.app.ui.simplestyle.device.download_offline.data

import androidx.annotation.Keep
import com.chad.library.adapter.base.entity.MultiItemEntity
import com.google.gson.annotations.SerializedName
import net.linkmate.app.ui.simplestyle.device.download_offline.adapter.OdTaskListAdapter


@Keep
data class OfflineDownLoadTask(
        @field:SerializedName("gid") val id: String,
        @field:SerializedName("filename") val filename: String,
        @field:SerializedName("type") val type: Int, // 1 http、2 https、3 ftp、4 sftp、5 bt、6 magnet
        @field:SerializedName("createtime") val createtime: Long,// 任务创建时间，unix秒级时间戳
        @field:SerializedName("filesize") val filesize: Long = 0L,
        @field:SerializedName("cursize") val cursize: Long,
        @field:SerializedName("speed") val speed: Long,
        @field:SerializedName("status") var status: String,
        // active 表示当前正在下载/播种下载
        // waiting 表示队列中的下载
        // paused 表示已暂停的下载
        // error 表示已进行的下载出错
        // completed 表示已停止和完成的下载
        @field:SerializedName("savepath") val savePath: String,//保存的路径
        @field:SerializedName("share_path_type") val sharePathType: Int, // 保存的路径对应的类型
        @field:SerializedName("errorMsg") val percent: String,
        @field:SerializedName("error") val error: String,
        var defImg: Int,
        var imgUrl: String? = null
        // , @field:SerializedName("files") val btsubfiles: List<OfflineDownLoadFile>?
) : MultiItemEntity {


    companion object {
        const val LOAD = "waiting"
        const val START = "active"
        const val SUSPEND = "paused"
        const val ERROR = "error"
        const val FINISH = "completed"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        } else if (other is OfflineDownLoadTask) {
            return other.id == id
        }
        return false
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

    override fun getItemType(): Int {
        return OdTaskListAdapter.NORMAL_ITEM
    }
}

