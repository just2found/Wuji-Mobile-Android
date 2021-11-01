package net.sdvn.nascommon.db.objboxkt

import android.os.Parcelable
import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id
import kotlinx.android.parcel.Parcelize

/**
 * Description: 群组成员
 * @author  admin
 * CreateDate: 2021/6/1
 */
@Keep
@Entity
@Parcelize
data class GroupUser(
    @Id
    var dbId: Long,
    var devId: String,//设备ID
    var dbUserId: String,//db userId 数据属主
    @SerializedName("username") var username: String,// "13066867956",
    @SerializedName("uid") var uid: Int, //1002,
    @SerializedName("user_id") var userId: String,// "281621005695251",
    @SerializedName("markname") var mark: String, //"",
    @SerializedName("group_id") var groupId: Long, //1,
    @SerializedName("join_time") var joinTime: Long, //1621477074101530806,
    @SerializedName("perm") var perm: Int, //511,
    @SerializedName("is_admin") var isAdmin: Boolean //true
) : Parcelable {

}