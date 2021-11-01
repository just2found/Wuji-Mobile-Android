package io.weline.repo.data.model
import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName


/** 

Created by admin on 2020/12/10,10:21

 */
@Keep
data class DataDevMark(
    @SerializedName("desc")
    var desc: String,
    @SerializedName("name")
    var name: String
)