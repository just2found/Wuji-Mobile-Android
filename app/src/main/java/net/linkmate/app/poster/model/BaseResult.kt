package net.linkmate.app.poster.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class BaseResult<T> (
        @SerializedName("result")
        val result: Boolean,
        @SerializedName("error")
        val error: Error?,
        @SerializedName("data")
        val data: T?
) : Serializable

data class Error(
        @SerializedName("code")
        val code: Int,
        @SerializedName("msg")
        val msg: String
) : Serializable