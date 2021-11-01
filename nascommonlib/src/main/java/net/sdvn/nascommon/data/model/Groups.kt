package net.sdvn.nascommon.data.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import net.sdvn.nascommon.db.objboxkt.GroupItem

/**
 * Description:
 * @author  admin
 * CreateDate: 2021/6/2
 */
@Keep
data class Groups(
    @SerializedName("groups") var groups: List<GroupItem>?
)

//@Keep
//data class GroupItem(
//    @SerializedName("id") var id: Int,
//    @SerializedName("name") var name: String,
//    @SerializedName("admin") var admin: String,
//    @SerializedName("uid") var uid: Int,
//    @SerializedName("user_id") var userId: String,//: "aaaaaaaaa",
//    @SerializedName("text") var text: String?
//)


