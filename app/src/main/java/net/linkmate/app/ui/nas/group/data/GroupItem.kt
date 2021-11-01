package net.linkmate.app.ui.nas.group.data

import androidx.annotation.Keep
import com.chad.library.adapter.base.entity.MultiItemEntity
import net.linkmate.app.ui.nas.group.GroupSpaceModel
import net.sdvn.common.internet.protocol.entity.MGR_LEVEL
import net.sdvn.nascommon.db.objboxkt.GroupItem

/**
create by: 86136
create time: 2021/6/1 13:42
Function description:
 */
@Keep
data class GroupItem(
    val dbId: Long,
    val id: Long,
    val name: String?,
    val admin: String?,
    val uid: Int,
    val user_id: String,
    val text: String?,
    val perm: Int,
    val mgrLevel: Int
) : MultiItemEntity {


    override fun getItemType(): Int {
        return GroupSpaceModel.ITEM
    }

    fun isAdmin(): Boolean {
        return mgrLevel == 1
    }

    fun isEnable(): Boolean {
        return mgrLevel == 1 || mgrLevel == 2
    }


    companion object {
        fun convert(groupItem: GroupItem): net.linkmate.app.ui.nas.group.data.GroupItem {
            return net.linkmate.app.ui.nas.group.data.GroupItem(
                groupItem.dbid,
                groupItem.id,
                groupItem.name,
                groupItem.admin,
                groupItem.uid,
                groupItem.ownerUserId,
                groupItem.text
                , groupItem.perm
                , groupItem.mgrLevel
            )
        }
    }

}