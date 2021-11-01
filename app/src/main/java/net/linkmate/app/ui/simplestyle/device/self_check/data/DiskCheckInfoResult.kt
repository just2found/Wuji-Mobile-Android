package net.linkmate.app.ui.simplestyle.device.self_check.data

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

/**
create by: 86136
create time: 2021/2/7 13:49
Function description:  这个是网络请求到的
 */
@Keep
data class DiskCheckInfoResult(@field:SerializedName("checkReport") val checkReportList: List<DiskCheckReport>,
                               @field:SerializedName("createAt") val createAt: String)

@Keep
data class DiskCheckReport(
        @field:SerializedName("Device") val device: String,
        @field:SerializedName("Progress") val progress: Int,
        @field:SerializedName("Msg") val msg: String
)