package net.linkmate.app.poster.model
import com.google.gson.annotations.SerializedName
import java.io.Serializable


data class ProductResult(
    @SerializedName("id")
    val id: String,
    @SerializedName("title")
    val title: String?,
    @SerializedName("post_excerpt")
    val post_excerpt: String?,
    @SerializedName("thumbnail")
    val thumbnail: String?,
    @SerializedName("price")
    val price: String?,
    @SerializedName("weline_score")
    val weline_score: String?,
    @SerializedName("expire_num")
    val expire_num: String?,
    @SerializedName("expire_unit")
    val expire_unit: String?
) : Serializable