package net.linkmate.app.ui.simplestyle.device.disk.data

import com.google.gson.annotations.SerializedName

//proportion长度必须为3 且放的顺寻必须为系统空间(非V8的就没有这一个)，使用空间 剩余空间
data class DiskSpaceOverview(@field: SerializedName("totalSpace") var totalSpace: String,
                             @field: SerializedName("systemSpace") var systemSpace: String?,
                             @field: SerializedName("usedSpace") var usedSpace: String,
                             @field: SerializedName("freeSpace") var freeSpace: String,
                             @field: SerializedName("proportion") var proportion: List<Int>)
