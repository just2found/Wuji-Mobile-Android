package io.weline.repo.data.model

import androidx.annotation.Keep

/**
 * @author Raleigh.Luo
 * date：21/6/5 18
 * describe：
 */
@Keep
data class DLNAPathResult(val share_path_type: Int? = null, val path: String? = null, val abspath: String? = null)

@Keep
data class DLNAOptionResult(val virtual_network: Int? = null, val media_path:List<DLNAPathResult>? = null)