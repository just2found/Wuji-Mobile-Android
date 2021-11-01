package net.linkmate.app.ui.simplestyle.device.self_check.data

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
@Keep
data class HDSmartInforSystem(@field: SerializedName("hds") val hds: List<Hdinfo>) {

//    data class Vfinfo(
//            @field: SerializedName("blocks") val blocks: Long,
//            @field: SerializedName("bsize") val bsize: Long,
//            @field: SerializedName("frsize") val frsize: Long,
//            @field: SerializedName("bavail") val bavail: Long,
//            @field: SerializedName("bfree") val bfree: Long
//    )

}