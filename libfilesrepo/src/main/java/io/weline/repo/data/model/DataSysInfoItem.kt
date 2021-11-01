package io.weline.repo.data.model
import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName


/** 

Created by admin on 2020/10/26,09:54

 */
@Keep
data class DataSysInfoItem(
    @field:SerializedName("create_at")
    var createAt: Long,
    @field:SerializedName("level")
    var level: Int,
    @field:SerializedName("name")
    var name: String,
    @field:SerializedName("update_at")
    var updateAt: Long,
    @field:SerializedName("value")
    var value: String
)