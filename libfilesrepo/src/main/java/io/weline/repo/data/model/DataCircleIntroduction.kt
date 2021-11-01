package io.weline.repo.data.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName


/** 

Created by admin on 2020/10/27,15:52

 */
@Keep
data class DataCircleIntroduction(
        @field:SerializedName("id")
        var id: Long,
        @field:SerializedName("iconFile")
        var iconFile: GOsFile? = null,
        @field:SerializedName("bgFile")
        var bgFile: GOsFile? = null,
        @field:SerializedName("title")
        var title: String? = null,
        @field:SerializedName("subtitle")
        var subtitle: String? = null,
        @field:SerializedName("content")
        var content: String? = null,
        @field:SerializedName("mediaResources")
        var mediaResources: List<GOsFile>? = null)
