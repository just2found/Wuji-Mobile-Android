package net.linkmate.app.poster.model
import com.google.gson.annotations.SerializedName
import java.io.Serializable


data class TxtResult(
  @SerializedName("context")
  val context: String
) : Serializable