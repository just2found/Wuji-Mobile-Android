package net.linkmate.app.data.model

import androidx.annotation.Keep
import net.sdvn.common.internet.core.GsonBaseProtocol
import java.io.Serializable

/**
 * @author Raleigh.Luo
 * date：20/10/15 19
 * describe：
 */
@Keep
data class CircleFeeRecords(val data: Records?) : GsonBaseProtocol() {
    @Keep
    data class Records(val list: List<Record>?)

    @Keep
    data class Record(var resid: String?,//资源ID,传入资源ID进行续费
                      var resname: String?,//标题
                      var restime: Long?,//资源购买时间戳
                      var renewable: Boolean?, //消费能否续费
                      var feeid: String?,//费用id
                      var feetype: String?,//费用类型
                      var feetypename: String?,//费用类型名称
                      var feeperiod: String?,//费用周期
                      var networkid: String?,//网络ID
                      var networkname: String?,//网络名称
                      var deviceid: String?,//设备ID
                      var devicesn: String?,//设备序列号
                      var devicename: String?,//设备名称
                      var mbpoint: Float?,//消费金额
                      var status: String?,//状态 1:生效中 0:未生效 -1:已失效
                      var effectdate: Long?,//生效时间戳
                      var expiredate: Long? //失效时间戳

    ) : Serializable {
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
        fun getMbpointValue():String{
            return mbpoint?.let {
                return if (it == 0f) "0" else String.format("%.2f",it)
            } ?: let {
                return "0"
            }
        }
    }

}