package net.linkmate.app.poster.model
import com.google.gson.annotations.SerializedName
import java.io.Serializable


data class OrderModel(
    @SerializedName("price")
    val price: String?,
    @SerializedName("end_time")
    val endTime: String?
) : Serializable