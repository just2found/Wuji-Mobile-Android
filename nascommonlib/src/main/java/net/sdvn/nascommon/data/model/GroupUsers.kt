package net.sdvn.nascommon.data.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import net.sdvn.nascommon.db.objboxkt.GroupUser


/**
 * Description: 群组成员
 * @author  admin
 * CreateDate: 2021/6/1
 */

@Keep
data class GroupUsers(
    @SerializedName("group_user") var users: List<GroupUser>
)

//@Keep
//data class GroupUser(
//    @SerializedName("username") var username: String,// "13066867956",
//    @SerializedName("uid") var uid: Int, //1002,
//    @SerializedName("user_id") var userId: String,// "281621005695251",
//    @SerializedName("text") var mark: String, //"",
//    @SerializedName("group_id") var groupId: Long, //1,
//    @SerializedName("join_time") var joinTime: Long, //1621477074101530806,
//    @SerializedName("perm") var perm: Int, //511,
//    @SerializedName("is_admin") var isAdmin: Boolean //true
//)