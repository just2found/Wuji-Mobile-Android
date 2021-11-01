package net.linkmate.app.data.model

import androidx.annotation.Keep
import com.google.gson.annotations.Expose
import net.linkmate.app.R
import net.linkmate.app.base.MyApplication
import net.sdvn.common.internet.core.GsonBaseProtocol
import java.io.Serializable

/**
 * @author Raleigh.Luo
 * date：20/10/13 13
 * describe：
 */
@Keep
data class CircleJoinWay(var data: JoinWayList?) : GsonBaseProtocol() {
    @Keep
    data class JoinWayList(var list: List<FeeType>?)

    @Keep
    data class FeeType(var feetype: String?,
                       var title: String?,
                       var fees: List<Fee>?,
                       var isfree: Boolean?, //能否免费使用  如果为是,允许不传费用ID和值免费订购
                       var resstatus: Int?, ///费用订购状态  1-有生效中订单 0-从未订购过 -1-订购已过期
                       var expireable: Boolean?,
                       var owner_share: Float? //网络拥有者分成

    ) : Serializable

    @Keep
    data class Fee(var feeid: String? = null,
                   var title: String?,
                   var feeperiod: String? = null,//费用类型 flow流量 trial试用 full终身
                   var timeunit: String? = null,//时间单位 minute/hour/day/month/year
                   var duration: Int? = 0,//周期时长
                   var value: Float? = 0f,//须缴费用
                   var deposit: Float? = null, //质押积分
                   var current: Boolean? = null////是否为当前订购的费用
    ) : Serializable {
        @Expose
        var owner_share: Float? = null//费用类型 和上层owner_share一致

        fun getDurationText(): String {
            var default = MyApplication.getContext().getString(R.string.no_limit)
            var unit = ""
            timeunit?.let {
                when (timeunit) {
                    "minute" -> {
                        unit = MyApplication.getContext().getString(R.string.minutes)
                    }
                    "hour" -> {
                        unit = MyApplication.getContext().getString(R.string.hours)
                    }
                    "day" -> {
                        unit = MyApplication.getContext().getString(R.string.day)
                    }
                    "month" -> {
                        unit = MyApplication.getContext().getString(R.string.counts) + MyApplication.getContext().getString(R.string.circle_month)
                    }
                    "year" -> {
                        unit = MyApplication.getContext().getString(R.string.circle_year)
                    }
                }
            }
            return if ((duration ?: 0) == 0) default else (duration ?: 0).toString() + unit
        }

        fun getValueText(): String {
            value?.let {
                return if (it == 0f) "0" else String.format("%.2f", it)
            } ?: let {
                return "0"
            }
        }

        fun getOwnShareText(): String {
            owner_share?.let {
                return if (it == 0f) "0%" else (String.format("%.2f", it) + "%")
            } ?: let {
                return "0%"
            }
        }

        fun getDepositText(): String {
            deposit?.let {
                return if (it == 0f) "0" else String.format("%.2f", it)
            } ?: let {
                return "0"
            }
        }

        /**
         * 总计费用 押金＋费用
         */
        fun getTotalText(): String {
            val total = (deposit ?: 0f) + (value ?: 0f)
            return if (total == 0f) "0" else String.format("%.2f", total)
        }

        /**
         * 对
        feettype为流量费类型flow-net或flow-dev或flow-netdev,
        且
        feeperiod为实时流量flow
        的收费项:
        1.在选购流量、我的收费项中，不显示其所需积分；
        2.在设置收费项中，列表上不显示其所需积分；数据中的value值分别对应其基本费、增值费的流量单价，在选择收费项设置的弹窗内为基本费、增值费的value加上 GB / 积分 的单位
         */
        fun isFilterFlow(feetype: String): Boolean {
//            return feetype in arrayOf("flow-net", "flow-dev", "flow-netdev") && feeperiod == "flow"
            return feeperiod == "flow"
        }
    }
}
