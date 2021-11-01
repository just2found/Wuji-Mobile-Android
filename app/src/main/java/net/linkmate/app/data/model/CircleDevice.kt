package net.linkmate.app.data.model

import android.text.TextUtils
import androidx.annotation.Keep
import net.linkmate.app.R
import net.linkmate.app.base.MyApplication
import net.sdvn.common.internet.core.GsonBaseProtocol
import java.io.Serializable

/**
 * @author Raleigh.Luo
 * date：20/10/13 15
 * describe：
 */
@Keep
class CircleDevice(var data: Devices?) : GsonBaseProtocol() {
    @Keep
    data class Devices(var list: List<Device>?)
    @Keep
    data class Device(var deviceid: String?,//服务提供EN设备ID
                        var devicename: String?, //EN设备名称
                        var devicesn: String?, //EN设备SN
                        var status: Int?,//用户在网络中的状态 0-正常 1-待确认 2-审核不通过 3-系统锁定 4-owner锁定 5-已过期
                        var addtime: Long?,//加入网络的时间戳,毫秒
                        var ownerid: String?, //EN设备拥有者ID
                        var firstname: String?,
                        var lastname: String?,
                        var loginname: String?,
                        var devicetype: Int?,
                        var deviceclass: Int?,
                        var isen: Boolean?,//是否为EN
                        var srvmain: Boolean?,//是否是主EN
                        var enable: Boolean?, //启用禁用
                        var srvenable: Boolean?, //启用禁用
                        var srvprovide: Boolean?,//是否在网络中提供服务
                        var nickname: String?
    ) : Serializable {
        fun getFullName(): String? {
            val owner = StringBuilder()
            if (!TextUtils.isEmpty(lastname)) owner.append(lastname)
            if (!TextUtils.isEmpty(firstname)) {
                owner.append(" ").append(firstname)
            }
            return owner.toString()
        }

        /**
         * 设置禁用启用
         */
        fun changeEnable() {
            val temp = enable?:false
            enable = !temp
        }

        fun isEnable():Boolean?{
            var isEnable:Boolean? = null
            enable?.let {
                isEnable = it
            }?:let{
                srvenable?.let {
                    isEnable = it
                }
            }
            return isEnable
        }

        fun getStatusName(): String {
            var result: String = ""
            when (status) {
                0 -> {//正常
                    result = MyApplication.getContext().getString(R.string.normal)
                }
                1 -> {//待确认
                    result = MyApplication.getContext().getString(R.string.pending)
                }
                2 -> {//审核不通过
                    result = MyApplication.getContext().getString(R.string.no_pass)
                }
                3 -> {//系统锁定
                    result = MyApplication.getContext().getString(R.string.system_lock)
                }

                4 -> {//锁定
                    result = MyApplication.getContext().getString(R.string.lock)
                }

                5 -> {//已过期
                    result = MyApplication.getContext().getString(R.string.has_expired)
                }
            }
            isEnable()?.let {
                if(it == false) result = MyApplication.getContext().getString(R.string.forbidden)
            }

            return result
        }
    }
}