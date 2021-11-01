package net.sdvn.common.data.model

import androidx.annotation.Keep
import net.sdvn.common.internet.core.GsonBaseProtocol

@Keep
class CircleDevice(var data: Devices?) : GsonBaseProtocol() {
    @Keep
    data class Devices(var list: List<Device>?)

    @Keep
    data class Device(var deviceid: String,//服务提供EN设备ID
                      var devicename: String?, //EN设备名称
                      var devicesn: String?, //EN设备SN
                      var status: Int?,//用户在网络中的状态 0-正常 1-待确认 2-审核不通过 3-系统锁定 4-owner锁定 5-已过期
                      var addtime: Long?,//加入网络的时间戳,毫秒
                      var ownerid: String?, //EN设备拥有者ID
                      var firstname: String?,
                      var lastname: String?,
                      var loginname: String?,
                      var nickname: String? = null,
                      var devicetype: Int?,
                      var deviceclass: Int?,
                      var joinstatus:Int?,//加入状态 0-正常 1-待确认 -1未订购 (以及其他非0的不正常状态)
                      var flowstatus:Int?,//流量状态 0-正常 1-已到期 -1-未订购
                      var isen: Boolean?,//是否为EN
                      var srvmain: Boolean?,//是否是主EN
                      var enable: Boolean?, //启用禁用
                      var srvprovide: Boolean?,//是否在网络中提供服务
                      var mbpointratio: String?//设备实时流量单价
    )
}