package net.sdvn.nascommon.db.objboxkt

import android.os.Parcelable
import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id
import kotlinx.android.parcel.Parcelize

/**
 * Description: 群组
 * @author  admin
 * CreateDate: 2021/6/1
 */
@Keep
@Entity
@Parcelize
data class GroupItem(
    @Id
    var dbid: Long,
    var devId: String,//设备ID
    var dbUserId: String,//db userId 数据属主
    @SerializedName("status") var mgrLevel: Int = 3,//1群主、2普通成员、3未加入群
    @SerializedName("id") var id: Long,
    @SerializedName("name") var name: String,
    @SerializedName("admin") var admin: String,
    @SerializedName("uid") var uid: Int,
    @SerializedName("user_id") var ownerUserId: String,//: "aaaaaaaaa",
    @SerializedName("notice") var text: String?,
    @SerializedName("perm") var perm: Int = 0//请求用户对于此群的权限，未加入的群可忽略此字段
) : Parcelable {
    fun isAdmin(): Boolean {
        return mgrLevel == 1
    }

    fun isNotJoined(): Boolean {
        return mgrLevel == 3
    }

    fun isMember(): Boolean {
        return mgrLevel == 1 || mgrLevel == 2
    }
}