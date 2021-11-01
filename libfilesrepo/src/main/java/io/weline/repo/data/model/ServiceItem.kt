package io.weline.repo.data.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

/**
create by: 86136
create time: 2021/3/27 14:51
Function description:
 */
@Keep
data class ServiceItem(
        @SerializedName("serviceId") val serviceId: Int,//服务ID
        @SerializedName("serviceName") val serviceName: String,//服务名称
        @SerializedName("serviceStatus") val serviceStatus: Boolean,//服务是否支持，true为支持，false不支持
        @SerializedName("serviceType") val serviceType: Int//服务类型：0 内部服务 ，1 外部服务器（可通过10.1 查询当前可用状态）2，配置选项)
)
