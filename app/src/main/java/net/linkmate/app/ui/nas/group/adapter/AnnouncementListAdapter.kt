package net.linkmate.app.ui.nas.group.adapter

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import net.linkmate.app.R
import net.sdvn.nascommon.constant.AppConstants
import net.sdvn.nascommon.db.objboxkt.GroupNotice
import java.util.*

/**
create by: 86136
create time: 2021/6/3 11:24
Function description:
 */

class AnnouncementListAdapter :
    BaseQuickAdapter<GroupNotice, BaseViewHolder>(R.layout.item_group_announcement_list) {

    override fun convert(viewHolder: BaseViewHolder, item: GroupNotice?) {
        if (item != null) {
            viewHolder.setText(R.id.group_announcement_content_tv, item.notice)
            viewHolder.setText(
                R.id.group_announcement_time_tv,
                AppConstants.sdf.format(Date(item.postTime * 1000))
            )
            viewHolder.setText(R.id.group_announcement_owner, item.postUsername)
        }
    }
}