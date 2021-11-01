package net.linkmate.app.poster.model
import com.google.gson.annotations.SerializedName


data class VersionResult(
  @SerializedName("context")
  val context: Version
)
data class Version(
        val version: Int
)