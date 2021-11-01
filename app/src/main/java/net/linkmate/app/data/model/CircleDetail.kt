package net.linkmate.app.data.model

import android.text.TextUtils
import androidx.annotation.Keep
import net.linkmate.app.R
import net.linkmate.app.base.MyApplication
import net.sdvn.common.internet.core.GsonBaseProtocol
import java.io.Serializable

/** 圈子详情对象
 * @author Raleigh.Luo
 * date：20/8/20 15
 * describe：
 */
@Keep
data class CircleDetail(var data: Circle?) : GsonBaseProtocol() {
    @Keep
    data class Circle(var networkid: String?,//网络ID
                      var networkstatus: Int?, //网络状态 0-正常 1-待审批 2-审批不通过 3-管理员锁定 4-owner锁定
                      var firstname: String?,
                      var loginname: String?,
                      var userstatus: Int?,  //使用状态 0-正常 1-待确认 3-系统锁定 4-owner锁定 5-使用到期
                      var uselevel: Int?,//使用级别 0-owner 1-manager 2-user  未加入网络则为空
                      var ownerid: String?, //网络属主ID
                      var lastname: String?,
                      var networkname: String?,//网络名称
                      var ischargecreate: Boolean?, //网络是否付费创建,以前的网络和注册帐号创建的均为免费创建
                      var provide_confirm: Boolean?, //提供服务是否需要管理员确认
                      var join_confirm: Boolean?, //成员加入是否需要管理员确认
                      var devsepcharge: Boolean?,//网络设备是否单独收费
                      var joined: Boolean?,//当前用户是否已加入此网络
                      var flowed: Boolean?,//当前用户是否能使用网络中设备流量
                      var networkprops: NetworkProps?,
                      var nickname: String?,
                      var ischarge: Boolean?, //网络是否付费，旧的均为免费为false
                      var srvmain: List<String>? //主EN列表

    ) : Serializable {
        fun isOwner(): Boolean {
            return uselevel == 0
        }

        /**
         * 是否是服务提供者，包括owner
         */
        fun isManager(): Boolean {
            return uselevel == 1
        }

        fun isAdmin(): Boolean {
            return isOwner() || isManager()
        }

        fun getFullName(): String? {
//            val owner = StringBuilder()
//            if (!TextUtils.isEmpty(lastname)) owner.append(lastname)
//            if (!TextUtils.isEmpty(firstname)) {
//                owner.append(" ").append(firstname)
//            }
//            return owner.toString()
            return if (TextUtils.isEmpty(nickname)) loginname else nickname
        }

        fun getMainENDeviceId(): String? {
            var id: String? = null
            srvmain?.let {
                if (it.size > 0)
                    id = it.get(0)
            }
            return id
        }

        /**
         * 是否为正常用户
         */
        fun isNormalUser(): Boolean {
            return userstatus == null || userstatus == 0
        }

//        /**
//         * 每个用户加入的设备数限制
//         */
//        fun getAccountDeviceMax() :Int{
//            var accountDeviceMax = 0
//            networkprops?.network_scale?.let {
//                val device_max = it.filter {
//                    it.key == "acctdev_max"
//                }
//                if (device_max != null && device_max.size > 0) {
//                    accountDeviceMax = device_max.get(0).value?.toInt()?:0
//                }
//            }
//            return accountDeviceMax
//        }
    }

    @Keep
    data class NetworkProps(var user_joinmode: ArrayList<Item<String>>?, //用户加入方式
                            var network_approval: ArrayList<Item<String>>?, //网络审批
                            var network_scale: ArrayList<Item<String>>?, //网络规模
                            var network_fee: ArrayList<Item<String>>?, //网络费用
                            var owner_custom: ArrayList<Item<Boolean>>?,
                            var acct_count: Int?,//已加入用户数
                            var device_count: Int?,//已加入设备数
                            var provide_count: Int?//已提供服务EN数
    ) : Serializable

    @Keep
    data class Item<T>(
            var enable: Boolean?,
            var title: String?,
            var modifiable: Boolean?,
            var value: T?,
            var value_modifiable: Boolean?,
            var key: String?,
            var owner_custom: String?,
            var duration_value: String?,
            var duration_unit: String?,
            var duration_modifiable: Boolean?
    ) : Serializable {
        fun getDuration(): String? {
            val duration = String.format("%s%s", duration_value ?: "", duration_unit ?: "")
            return if (TextUtils.isEmpty(duration)) MyApplication.getContext().getString(R.string.no_limit) else duration
        }
    }
}