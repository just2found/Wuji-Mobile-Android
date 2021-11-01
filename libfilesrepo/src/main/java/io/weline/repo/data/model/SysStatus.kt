package io.weline.repo.data.model

import androidx.annotation.Keep

/**
"data":{
"cpu_use":1.5138126349032042,
"cpu_temp":45,
"cpu_speed":"1100 MHZ"
"mem_use":3.3311656657951336,
"sys_runtime":86400
"fan_rpm":[300,600],
"hd_temp":[45,45],
"rx_speed":500,
"tx_speed":600
}
 */
@Keep
data class SysStatus(
    val cpu_use: Double?,
    val cpu_temp: Int?,
    val cpu_speed: String?,
    val mem_use: Double?,
    val sys_runtime: Long?,//sys_runtime单位是秒
    val fan_rpm: List<Int>?,
    val hd_temp: List<Int>?,
    val rx_speed: Long?,
    val tx_speed: Long?
)
