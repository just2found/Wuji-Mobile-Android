package net.linkmate.app.poster.model
import com.google.gson.annotations.SerializedName
import java.io.Serializable


data class RegisterCrmModel(
  @SerializedName("token")
  val token: String
) : Serializable