package net.linkmate.app.ui.simplestyle.device.remove_duplicate.adapter

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import net.linkmate.app.R
import net.sdvn.nascommon.model.oneos.OneOSFile

/**
create by: 86136
create time: 2021/1/31 19:47
Function description:
 */
class RdByFileAdapter(layoutId: Int = R.layout.item_rd_select_folder) :
    BaseQuickAdapter<OneOSFile, BaseViewHolder>(layoutId) {

    override fun convert(viewHolder: BaseViewHolder, oneOSFile: OneOSFile?) {
        oneOSFile?.let {
            viewHolder.setText(R.id.file_name, it.getName())
            viewHolder.setGone(R.id.file_select, false)
        }

    }

}