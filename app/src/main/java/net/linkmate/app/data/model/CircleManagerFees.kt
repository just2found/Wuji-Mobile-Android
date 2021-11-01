package net.linkmate.app.data.model

import androidx.annotation.Keep
import com.google.gson.annotations.Expose
import net.linkmate.app.R
import net.linkmate.app.base.MyApplication
import net.sdvn.common.internet.core.GsonBaseProtocol
import java.io.Serializable

/**
 * @author Raleigh.Luo
 * date：20/10/16 11
 * describe：
 */
@Keep
data class CircleManagerFees(val data: FeeList?) : GsonBaseProtocol() {
    @Keep
    data class FeeList(val list: List<FeeTypes>?)

    @Keep
    data class FeeTypes(val fees: List<Fee>?,
                        var feetype: String?,//费用类型
                        var title: String?,//费用类型国际化标题
                        var share: Any?,//未知
                        var owner_share: Double? = null,//网络拥有者抽成
                        var netdevs: List<NetDevs>?//独立收费
    )

    @Keep
    data class Fee(var title: String?,//收费项目国际化标题
                   var feeperiod: String?,//费用类型 flow流量 trial试用 full终身 cycle周期
                   var timeunit: String?,//时间单位 minute/hour/day/month/year
                   var duration: Int?,//周期时长
                   var vaddable: Boolean?,//管理员能否禁用此费用的权限,为false时不能设置enable
                   var enable: Boolean?,////管理员设置为禁用后,此项对用户不可见此
                   var editable: Boolean?,//此收费项能否设置增值费,例如免费/试用类型 不允许设置
                   var value: Double?,//真实费用 =基础费+增值费
                   var basic: FeeDetail?,//基础费内容
                   var deposit: Double?,//质押积分
                   var vadd: FeeDetail?////增值费用内容,如果没有则为空
    ) : Serializable {
        @Expose
        var feetype: String? = null//费用类型 和上层feetype一致

        @Expose
        var titleUpper: String? = null//费用类型 和上层title一致

        @Expose
        var owner_share: Double? = null//网络拥有者抽成 和上层owner_share一致

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
                return if (it == 0.toDouble()) "0" else String.format("%.2f",it)
            } ?: let {
                return "0"
            }
        }

        fun getOwnShareText(): String {
            owner_share?.let {
                return if (it == 0.toDouble()) "0" else String.format("%.2f",it)
            } ?: let {
                return "0"
            }
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
        fun isFilterFlow(): Boolean {
//            return feetype in arrayOf("flow-net", "flow-dev", "flow-netdev") && feeperiod == "flow"
            return feeperiod == "flow"
        }

    }

    @Keep
    data class FeeDetail(var feeid: String?,//费用id
                         var value: Float?,//费用值
                         var plans: Plans? //费用计划
    ) : Serializable {
        fun getValueText(): String {
            value?.let {
                return if (it == 0f) "0" else String.format("%.2f",it)
            } ?: let {
                return "0"
            }
        }
    }

    @Keep
    data class Plans(var planname: String?,//计划名称
                     var starttime: Long?,//开始时间戳
                     var endtime: Long?, //结束时间戳
                     var discounttype: Int?, //优惠方式 1-特价2-折扣
                     var discountvalue: Float? //优惠值 特价时积分使用此值,折扣时积分乘以此值
    ) : Serializable

    @Keep
    data class NetDevs(var deviceid: String?, var fees: List<Fee>?) : Serializable
}