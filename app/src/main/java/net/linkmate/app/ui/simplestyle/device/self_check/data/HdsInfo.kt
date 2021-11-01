package net.linkmate.app.ui.simplestyle.device.self_check.data

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
@Keep
data class HdsInfo(@field: SerializedName("name") val name: String?,
                   @field: SerializedName("smartinfo") val smartinfo: SmartInfo?,
                   var slot: Int = -1//这个是添加的用于拼接数据的
)
