package net.linkmate.app.ui.nas.group.adapter

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import net.linkmate.app.R
import net.sdvn.common.internet.protocol.entity.MGR_LEVEL
import net.sdvn.nascommon.SessionManager
import net.sdvn.nascommon.constant.AppConstants
import net.sdvn.nascommon.db.objboxkt.GroupUser
import java.util.*

/**
 *
 * @Description: 用户列表
 * @Author: todo2088
 * @CreateDate: 2021/3/8 18:01
 */
class GroupUserAdapter : BaseQuickAdapter<GroupUser, BaseViewHolder>
    (R.layout.item_simple_user) {
    override fun convert(helper: BaseViewHolder, item: GroupUser?) {
        item?.let {
            val charSequence: CharSequence? = item.username
            helper.setText(R.id.tv_name, charSequence)
            helper.setGone(R.id.iv_user_level, (item.isAdmin).also {
                if (it) {
                    helper.setImageResource(R.id.iv_user_level, R.drawable.icon_user_admin)
                }
            })
            helper.setVisible(R.id.tv_is_me, item.userId == SessionManager.getInstance().userId)
            helper.setText(R.id.tv_time, AppConstants.sdf.format(Date(item.joinTime * 1000)))
        }
    }

}