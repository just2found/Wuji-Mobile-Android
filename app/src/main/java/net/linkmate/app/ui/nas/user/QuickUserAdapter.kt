package net.linkmate.app.ui.nas.user

import android.text.TextUtils
import com.chad.library.adapter.base.BaseSectionQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.chad.library.adapter.base.entity.SectionEntity
import net.linkmate.app.R
import net.sdvn.nascommon.model.oneos.OneOSUser
import net.sdvn.nascommon.utils.FileUtils

class QuickUserAdapter(mOneOSUsers: List<SectionEntity<OneOSUser>>?, private val mIsAdmin: Boolean)
    : BaseSectionQuickAdapter<SectionEntity<OneOSUser>, BaseViewHolder>(R.layout.item_listview_user
        , R.layout.item_user_title, mOneOSUsers) {

    override fun convertHead(p0: BaseViewHolder, p1: SectionEntity<OneOSUser>?) {
        p0.setText(R.id.txt_title, p1?.header)
    }


    //管理员的账号
    private var managerName: String? = null


    override fun convert(holder: BaseViewHolder, sectionEntity: SectionEntity<OneOSUser>?) {
        val context = holder.itemView.context.applicationContext
        sectionEntity?.t?.let { user ->
            val name = user.name
            val isAdmin = user.isAdmin

            if (!TextUtils.isEmpty(managerName) && "admin" == name) {
                //如果当前用户名称为admin，则将其改为管理员账号
                holder.setText(R.id.txt_account, managerName)
            } else {
                holder.setText(R.id.txt_account, name)
            }
            if (!TextUtils.isEmpty(user.markName)) {
                holder.setText(R.id.txt_name, user.markName)
                holder.setTextColor(R.id.txt_name, context.resources.getColor(R.color.black))
            } else {
                holder.setText(R.id.txt_name, R.string.click_to_edit)
                holder.setTextColor(R.id.txt_name, context.resources.getColor(R.color.gray))
            }
            holder.setGone(R.id.iv_admin, isAdmin == 1 || isAdmin == 0)
                    .setImageResource(R.id.iv_admin,
                            if (isAdmin == 0) R.drawable.icon_user_admin else R.drawable.icon_user_master)
            if (user.space == user.used && user.used == -1L) {
                holder.setText(R.id.txt_space, R.string.get_space_failed)
                holder.setProgress(R.id.progressbar, 0)
            } else if (user.space == 0L) {
                holder.setText(R.id.txt_space, String.format("%s / %s", FileUtils.fmtFileSize(user.used), context.resources.getString(R.string.unknown)))
                holder.setProgress(R.id.progressbar, 0)
            } else {
                holder.setText(R.id.txt_space, String.format("%s / %s", FileUtils.fmtFileSize(user.used), FileUtils.fmtFileSize(user.space)))
                holder.setProgress(R.id.progressbar, (user.used * 100 / user.space).toInt())
            }
            holder.setImageResource(R.id.iv_location, if (user.isRemote) R.drawable.ic_internet_blue_32 else R.drawable.ic_subnet_blue_32)
        }
    }

    fun setManagerName(managerName: String) {
        this.managerName = managerName
    }


}
