package io.weline.repo.data.model

import androidx.annotation.Keep

/**
 * @author Raleigh.Luo
 * date：21/5/10 15
 * describe：
 */
@Keep
data class Brief(var bg: BriefDetail? = null,
                 var avatar: BriefDetail? = null,
                 var text: BriefDetail? = null)
@Keep
data class BriefDetail(var data: String? = null,
                       var update_at: Long? = null)
@Keep
data class BriefTimeStamp(var update_at: BriefTimeStampDetail? = null)

@Keep
data class BriefTimeStampDetail(var bg: Long? = null,
                          var avatar: Long? = null,
                          var text: Long? = null)