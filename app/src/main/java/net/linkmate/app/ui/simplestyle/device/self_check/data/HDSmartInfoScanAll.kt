package net.linkmate.app.ui.simplestyle.device.self_check.data

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

/**
create by: 86136
create time: 2021/3/17 20:11
Function description:
 */
@Keep
data class HDSmartInfoScanAll(@field: SerializedName("hds") val hds: List<HdsInfo>?)