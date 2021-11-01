package net.linkmate.app.data.model.dynamic

import androidx.annotation.Keep
import net.linkmate.app.data.model.Base
import net.sdvn.common.vo.DynamicComment
import net.sdvn.common.vo.DynamicLike
import net.sdvn.common.vo.DynamicRelated

/**
 * @author Raleigh.Luo
 * date：21/1/7 10
 * describe：
 */
@Keep
data class RelatedList(val data: List<DynamicRelated>?): Base()