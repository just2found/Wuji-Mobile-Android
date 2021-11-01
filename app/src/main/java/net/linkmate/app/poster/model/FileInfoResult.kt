package net.linkmate.app.poster.model
import com.google.gson.annotations.SerializedName
import java.io.Serializable



data class FileInfoResult(
    @SerializedName("dir")
    val dir: String,
    @SerializedName("dirs")
    val dirs: Int,
    @SerializedName("files")
    val files: Int,
    @SerializedName("gid")
    val gid: Int,
    @SerializedName("name")
    val name: String,
    @SerializedName("path")
    val path: String,
    @SerializedName("perm")
    val perm: String,
    @SerializedName("size")
    val size: Long,
    @SerializedName("time")
    val time: Long,
    @SerializedName("type")
    val type: String,
    @SerializedName("uid")
    val uid: Int
) : Serializable