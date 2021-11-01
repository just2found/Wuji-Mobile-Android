package net.sdvn.common.vo

import androidx.annotation.Keep
import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id

/**
 *  
 *
 *
 * Created by admin on 2020/10/22,19:35
 */
@Keep
@Entity
data class InNetDeviceModel(
        @Id
        var id: Long = 0,
        var userId: String,//用户Id
        var netId: String,//所在网络ID
        var deviceId: String,//服务提供EN设备ID
        var deviceName: String? = null, //EN设备名称
        var deviceSn: String? = null, //EN设备SN
        var status: Int? = null,//用户在网络中的状态 0-正常 1-待确认 2-审核不通过 3-系统锁定 4-owner锁定 5-已过期
        var addTime: Long? = null,//加入网络的时间戳,毫秒
        var ownerId: String? = null, //EN设备拥有者ID
        var firstName: String? = null,
        var lastName: String? = null,
        var loginName: String? = null,
        var nickname: String? = null,
        var deviceType: Int? = null,
        var deviceClass: Int? = null,
        var joinStatus: Int? = null,
        var flowStatus: Int? = null,
        var isEn: Boolean? = null,//是否为EN
        var srvMain: Boolean? = null,//是否是主EN
        var enable: Boolean? = null, //启用禁用
        var srvProvide: Boolean? = null,//是否在网络中提供服务
        var mbpointratio: String? = null//设备实时流量单价
)