package net.linkmate.app.ui.nas.group.adapter

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import net.linkmate.app.R
import java.text.FieldPosition

/**
create by: 86136
create time: 2021/6/3 20:35
Function description:
 */

class GroupAddMemberAdapter :
    BaseQuickAdapter<String, BaseViewHolder>(R.layout.item_group_add_member_list) {


    private val mSelectSet = mutableSetOf<String>()


    fun changeSelect(str: String, position: Int) {
        if (mSelectSet.contains(str)) {
            mSelectSet.remove(str)

        } else {
            mSelectSet.add(str)
        }
        notifyItemChanged(position)
    }


    override fun convert(viewHolder: BaseViewHolder, str: String?) {
        viewHolder.setText(R.id.group_name_tv, str)
        viewHolder.addOnClickListener(R.id.rv_list_cb_select)
        viewHolder.setChecked(R.id.rv_list_cb_select, mSelectSet.contains(str))
    }


}