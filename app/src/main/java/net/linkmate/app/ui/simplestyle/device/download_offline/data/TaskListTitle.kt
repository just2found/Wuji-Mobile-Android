package net.linkmate.app.ui.simplestyle.device.download_offline.data

import com.chad.library.adapter.base.entity.MultiItemEntity

/**
create by: 86136
create time: 2021/2/26 15:49
Function description:
 */

data class TaskListTitle(val title: String, val optName: String,private val type: Int) : MultiItemEntity {

    override fun getItemType(): Int {
        return type
    }
}
