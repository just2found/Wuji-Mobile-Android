package net.linkmate.app.ui.nas.group.adapter


import android.view.View
import com.chad.library.adapter.base.BaseMultiItemQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.chad.library.adapter.base.entity.MultiItemEntity
import kotlinx.android.synthetic.main.item_home_simplestyle.view.*
import net.linkmate.app.R
import net.linkmate.app.ui.nas.group.GroupSpaceModel
import net.linkmate.app.ui.nas.group.data.GroupItem
import net.linkmate.app.ui.nas.group.data.TextHeadTitle
import net.sdvn.nascommon.SessionManager

class GroupListAdapter(data: List<MultiItemEntity>? = null) :
    BaseMultiItemQuickAdapter<MultiItemEntity, BaseViewHolder>(data) {


    init {
        addItemType(GroupSpaceModel.TITLE, R.layout.item_simple_title)
        addItemType(GroupSpaceModel.ITEM, R.layout.item_group_space_list)
    }

    override fun convert(viewHolder: BaseViewHolder, item: MultiItemEntity) {
        if (item.itemType == GroupSpaceModel.TITLE && item is TextHeadTitle) {
            viewHolder.setText(R.id.tvTitle, item.content)
        } else if (item.itemType == GroupSpaceModel.ITEM && item is GroupItem) {
            viewHolder.setText(R.id.group_name_tv, item.name)
            viewHolder.setText(R.id.group_announcement_tv, item.text ?: "")
            viewHolder.setGone(R.id.group_setting, true)
            viewHolder.addOnClickListener(R.id.group_setting)
            viewHolder.setImageResource(R.id.ivImage, R.drawable.icon_defualt_circle)
            val position = viewHolder.adapterPosition
            val vBottomLineVisibility =
                !(position + 1 == itemCount || (position + 1 != itemCount && getItem(position + 1) is TextHeadTitle))
            viewHolder.setVisible(R.id.vBottomLine, vBottomLineVisibility)
        }
    }

}
