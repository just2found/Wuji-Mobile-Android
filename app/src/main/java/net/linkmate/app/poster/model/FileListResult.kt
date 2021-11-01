package net.linkmate.app.poster.model
import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class FileListResult(
    @SerializedName("files")
    val files: List<MyFile>?,
    @SerializedName("page")
    val page: Int,
    @SerializedName("pages")
    val pages: Int,
    @SerializedName("total")
    val total: Int
) : Serializable

data class MyFile(
    @SerializedName("ftype")
    val ftype: String,
    @SerializedName("gid")
    val gid: Int,
    @SerializedName("name")
    val name: String,
    @SerializedName("path")
    val path: String,
    @SerializedName("size")
    val size: Long,
    @SerializedName("time")
    val time: Long,
    @SerializedName("uid")
    val uid: Int
) : Serializable