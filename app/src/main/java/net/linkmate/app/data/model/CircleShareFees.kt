package net.linkmate.app.data.model

import androidx.annotation.Keep
import net.sdvn.common.internet.core.GsonBaseProtocol

/** EN服务器抽成费用
 * @author Raleigh.Luo
 * date：20/10/20 21
 * describe：
 */
@Keep
data class CircleShareFees(var data: ShareFees) : GsonBaseProtocol() {
    data class ShareFees(var list: List<Fee> )
    @Keep
    data class Fee(var owner_max:Float?, //拥有者分成最大值
                   var owner_share:Float?,  //拥有者分成,如果可改,范围
                   var provider_share:Float? //服务提供者分成,结算结果,不可改
    )
}