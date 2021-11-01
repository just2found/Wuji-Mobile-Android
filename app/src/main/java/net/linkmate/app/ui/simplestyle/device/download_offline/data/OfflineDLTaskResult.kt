package net.linkmate.app.ui.simplestyle.device.download_offline.data

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

//这个是
@Keep
data class OfflineDLTaskResult(@field:SerializedName("code")val code: Int?,
                               @field:SerializedName("msg")  val msg: String?,
                               @field:SerializedName("taskinfo")val taskinfo: List<OfflineDownLoadTask>?)


