package io.weline.repo.data.model

import androidx.annotation.Keep


@Keep
data class HardInfo(
    val model: String?,
    val sn: String?,
    val cpu: String?,
    val sys_version: String?,
    val ddr_size: Long?,
    val mac: String?
)
