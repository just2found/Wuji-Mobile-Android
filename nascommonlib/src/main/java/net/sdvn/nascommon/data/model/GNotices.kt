package net.sdvn.nascommon.data.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import net.sdvn.nascommon.db.objboxkt.GroupNotice

/**
 * Description: 群组公告
 * @author  admin
 * CreateDate: 2021/6/1
 */
@Keep
data class GNotices(@SerializedName("text_history") var notices: List<GroupNotice>)

//@Keep
//data class GroupNotice(
//    @SerializedName("group_id") var groupId: Long, //1,
//    @SerializedName("text") var text: String, //"",
//    @SerializedName("ptime") var postTime: Long //1621477074101530806,
//)
