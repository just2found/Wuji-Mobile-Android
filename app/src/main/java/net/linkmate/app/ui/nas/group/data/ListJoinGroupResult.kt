package net.linkmate.app.ui.nas.group.data

import androidx.annotation.Keep

/**
create by: 86136
create time: 2021/6/1 13:46
Function description:
 */
@Keep
data class ListJoinGroupResult(
    val admin: List<GroupItem>?,
    val member: List<GroupItem>?
)