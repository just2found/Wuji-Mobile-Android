package net.linkmate.app.ui.simplestyle.device.remove_duplicate.adapter

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import net.linkmate.app.R
import net.sdvn.nascommon.model.oneos.OneOSFile
import net.sdvn.nascommon.widget.CheckableImageButton

/**
create by: 86136
create time: 2021/1/31 19:47
Function description:
 */

class RdAddFileAdapter(layoutId: Int = R.layout.item_rd_select_folder) :
    BaseQuickAdapter<OneOSFile, BaseViewHolder>(layoutId) {

val mSelectList = mutableListOf<OneOSFile>()


fun addSelectList(list: List<OneOSFile>) {
    mSelectList.clear()
    mSelectList.addAll(list)
}

    fun changeSelect(position: Int) {
        if (mData.size > position) {
            val item = mData[position]
            if (mSelectList.contains(item)) {
                mSelectList.remove(item)
            } else {
                mSelectList.add(item)
            }
        }
        notifyItemChanged(position)
    }

    override fun convert(viewHolder: BaseViewHolder, oneOSFile: OneOSFile?) {
        oneOSFile?.let {
            viewHolder.setText(R.id.file_name, it.getName())
            viewHolder.setTag(R.id.file_select, it)
            if (mSelectList.contains(it)) {
                viewHolder.setChecked(R.id.file_select, true)
            }
        }

    }
}