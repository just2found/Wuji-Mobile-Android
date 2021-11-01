package net.linkmate.app.ui.simplestyle.device.disk.data

import com.google.gson.annotations.SerializedName

/**
create by: 86136
create time: 2021/1/22 17:34
Function description:
 */

data class OptResult(@field: SerializedName("status") val status: String, @field: SerializedName("processList") val processList: List<Process>) {
    data class Process(@field: SerializedName("totalSpace") val name: String,
                       @field: SerializedName("progress") val progress: Int,
                       @field: SerializedName("create_at") val create_at: Int,
                       @field: SerializedName("update_at") val update_at: Int)
}