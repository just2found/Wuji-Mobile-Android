package net.linkmate.app.data.model

import android.text.TextUtils
import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import net.linkmate.app.R
import net.linkmate.app.base.MyApplication
import net.sdvn.common.internet.core.GsonBaseProtocol
import java.io.Serializable
import java.text.SimpleDateFormat
import java.util.*

/**
 * @author Raleigh.Luo
 * date：20/8/18 17
 * describe：
 */
@Keep
data class CircleMember(var data: MemberList? = null) : GsonBaseProtocol() {
    @Keep
    data class MemberList(var members: List<Member>? = null)
    @Keep
    data class Member(var userid: String?,
                      var firstname: String?,
                      var lastname: String?,
                      var loginname: String?,
                      var uselevel: Int?,///用户在网络中的级别 0-owner 1-manager 2-user
                      var status: Int?,//用户在网络中的状态 0-正常 1-待确认 2-审核不通过 3-系统锁定 4-owner锁定 5-已过期
                      var addtime: Long?,//加入网络的时间戳,毫秒
                      var expiretime: Long?,//网络到期时间戳,毫秒
                      var nickname: String?
//                      var addtype: String?  //加入方式 free:免费/trial:试用/fullfee:全额付费/monthfee:按月付费
    ) : Serializable {
        fun getFullName(): String? {
//            val owner = StringBuilder()
//            if (!TextUtils.isEmpty(lastname)) owner.append(lastname)
//            if (!TextUtils.isEmpty(firstname)) {
//                owner.append(" ").append(firstname)
//            }
//            return owner.toString()
            return if(TextUtils.isEmpty(nickname))loginname else nickname
        }

        fun getExpire(): String? {
            var expiretimeStr = MyApplication.getContext().getString(R.string.not_limit)
            if (expiretime != null)
                expiretimeStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(expiretime
                        ?: 0))
            var result = ""
//            when(addtype){
//                "free"->{
//                    result = MyApplication.getContext().getString(R.string.free)
//                }
//                "trial"->{
//                    result = MyApplication.getContext().getString(R.string.on_trial) +" "+ expiretime
//                }
//                "fullfee"->{
//                    result = MyApplication.getContext().getString(R.string.not_limit)
//                }
//                "monthfee"->{
//                    result = expiretime
//                }
//
//            }
            return expiretimeStr
        }

        fun isOwner(): Boolean {
            return uselevel == 0
        }

        fun isManager(): Boolean {
            return uselevel == 1
        }
    }
}