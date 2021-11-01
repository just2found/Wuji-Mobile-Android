package io.weline.repo.data.model

import androidx.annotation.Keep

/**
 * @author Raleigh.Luo
 * date：21/6/5 19
 * describe：
 */
@Keep
data class LanScanVisibleResult(val network_discovery: Int? = null, val virtual_network: Int? = null)