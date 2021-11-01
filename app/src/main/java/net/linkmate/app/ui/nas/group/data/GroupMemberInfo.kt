package net.linkmate.app.ui.nas.group.data

import androidx.annotation.Keep

/**
create by: 86136
create time: 2021/6/3 20:20
Function description:
 */
@Keep
data class GroupMemberInfo(
    val username: String?,
    val uid: Long?,
    val user_id: String?,
    val text: String?,
    val group_id: Long?,
    val join_time: Long?,
    val perm: Int?,
    val is_admin: Boolean
)



