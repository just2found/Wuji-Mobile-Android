package net.linkmate.app.poster.model
import com.google.gson.annotations.SerializedName
import java.io.Serializable


data class UploadScreenshot(
    @SerializedName("filepath")
    val filePath: String?
) : Serializable