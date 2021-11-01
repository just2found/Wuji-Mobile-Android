package net.linkmate.app.poster.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class BaseResultCrm<T> (
        @SerializedName("code")
        val code: Int,
        @SerializedName("msg")
        val msg: String,
        @SerializedName("data")
        val data: T?
) : Serializable