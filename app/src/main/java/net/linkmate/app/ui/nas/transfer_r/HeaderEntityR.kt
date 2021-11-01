package net.linkmate.app.ui.nas.transfer_r

import com.chad.library.adapter.base.entity.MultiItemEntity

/**
create by: 86136
create time: 2021/3/12 15:47
Function description:
 */
class HeaderEntityR(var size: Int, var isAllStart: Boolean, private val type: Int) : MultiItemEntity {
    override fun getItemType(): Int {
        return type
    }
}