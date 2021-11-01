package io.weline.repo.torrent.data

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

/** 

Created by admin on 2020/7/3,10:04

 */
@Keep
data class BtVersion(
        @SerializedName("version")
        val version: String)