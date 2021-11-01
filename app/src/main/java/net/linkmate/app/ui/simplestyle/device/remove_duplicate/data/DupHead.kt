package net.linkmate.app.ui.simplestyle.device.remove_duplicate.data

import com.chad.library.adapter.base.entity.MultiItemEntity
import net.linkmate.app.ui.simplestyle.device.remove_duplicate.adapter.RdSelectAdapter

/**
create by: 86136
create time: 2021/1/31 23:02
Function description:
 */

data class DupHead(val defImg: Int, val imgUrl: String?, val count: String, val size: String) : MultiItemEntity {

    override fun getItemType(): Int {
        return RdSelectAdapter.HEAD
    }
}