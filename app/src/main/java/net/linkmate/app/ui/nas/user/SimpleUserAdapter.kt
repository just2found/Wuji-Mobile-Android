package net.linkmate.app.ui.nas.user

import android.text.TextUtils
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import net.linkmate.app.R
import net.sdvn.common.internet.protocol.entity.MGR_LEVEL
import net.sdvn.common.internet.protocol.entity.ShareUser

/**
 *
 * @Description: 用户列表
 * @Author: todo2088
 * @CreateDate: 2021/3/8 18:01
 */
class SimpleUserAdapter : BaseQuickAdapter<ShareUser, BaseViewHolder>
(R.layout.item_simple_user) {
    override fun convert(helper: BaseViewHolder, item: ShareUser?) {
        item?.let {
            val charSequence: CharSequence? = (item.devMarkName.takeUnless { it.isNullOrEmpty() }
                    ?: item.fullName.takeUnless { it.isEmpty() } ?: item.username)
            helper.setText(R.id.tv_name, charSequence)

            helper.setVisible(R.id.iv_user_level, (item.mgrlevel<=MGR_LEVEL.ADMIN.toInt()).also {
                if (it) {
                    helper.setImageResource(R.id.iv_user_level, getIconByLevel(item.mgrlevel))
                }
            })


            helper.setVisible(R.id.tv_is_me, item.isCurrent)
            item.datetime?.let {helper.setText(R.id.tv_time, it)  }
            helper.setVisible(R.id.tv_time,!TextUtils.isEmpty(item.datetime))
        }
    }

    private fun getIconByLevel(level: Int): Int {
        return when ("$level") {
            MGR_LEVEL.OWNER -> {
                R.drawable.icon_user_admin
            }
            MGR_LEVEL.UNBOUND -> {
                R.drawable.ic_unbind_user
            }
            else -> {
                R.drawable.icon_user_master
            }
        }
    }


}