package net.linkmate.app.ui.nas.group.data

import com.chad.library.adapter.base.entity.MultiItemEntity
import net.linkmate.app.ui.nas.group.GroupSpaceModel

/**
create by: 86136
create time: 2021/6/1 14:05
Function description:
 */

data class TextHeadTitle(val content: String) : MultiItemEntity {
    override fun getItemType(): Int {
        return GroupSpaceModel.TITLE
    }
}