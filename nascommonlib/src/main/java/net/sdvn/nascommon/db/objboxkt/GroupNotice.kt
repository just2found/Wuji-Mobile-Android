package net.sdvn.nascommon.db.objboxkt

import android.os.Parcelable
import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id
import kotlinx.android.parcel.Parcelize
import java.io.Serializable

/**
 * Description: 群组公告
 * @author  admin
 * CreateDate: 2021/6/1
 */
@Keep
@Entity
@Parcelize
data class GroupNotice(
    @Id
    var dbId: Long,
    var devId: String,//设备ID
    var dbUserId: String,//db userId 数据属主
    @SerializedName("group_id") var groupId: Long, //1,
    @SerializedName("notice_id") var noticeId: Long, //1,
    @SerializedName("notice") var notice: String, //"",
    @SerializedName("ptime") var postTime: Long,//1621477074101530806,
    @SerializedName("p_username") var postUsername: String//1621477074101530806,
//    @SerializedName("p_user_id") var userId: String //1621477074101530806,
) : Parcelable