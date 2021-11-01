package net.linkmate.app.ui.nas.helper

import androidx.recyclerview.widget.GridLayoutManager
import com.chad.library.adapter.base.BaseSectionQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.chad.library.adapter.base.entity.SectionEntity
import net.linkmate.app.R
import net.sdvn.nascommon.model.FileTypeItem

class FileTypeItemAdapter : BaseSectionQuickAdapter<SectionEntity<FileTypeItem>, BaseViewHolder>
(R.layout.item_gridview_card_lenovo, R.layout.item_title, null) {
    override fun convertHead(holder: BaseViewHolder, entity: SectionEntity<FileTypeItem>?) {
        holder.setText(R.id.tv_title, entity?.header)
    }

    override fun convertPayloads(helper: BaseViewHolder, item: SectionEntity<FileTypeItem>?, payloads: MutableList<Any>) {
        convert(helper, item)
    }

    override fun convert(holder: BaseViewHolder, entity: SectionEntity<FileTypeItem>?) {
        entity?.t?.let {
            val position = holder.adapterPosition
            val keyItem = 77581234
            val tag = holder.itemView.getTag(keyItem)
            if (tag != it.flag) {
                holder.itemView.setTag(keyItem, it.flag)
                val lm = recyclerView.layoutManager
                if (lm is GridLayoutManager) {
                    val spanSize = lm.spanSizeLookup.getSpanSize(position)
                    val spanCount = lm.spanCount
                    val spanIndex = lm.spanSizeLookup.getSpanIndex(position, spanCount)
                    val isRight = spanIndex + 1 == spanCount
                    val isBottom = position + spanCount >= itemCount
                    holder.setGone(R.id.line_right, !isRight)
                    holder.setGone(R.id.line_bottom, !isBottom)

                }
                holder.setText(R.id.txt_type, it.title)
                holder.setTextColor(R.id.txt_type, holder.itemView.context.resources.getColor(R.color.text_black))
                holder.setImageResource(R.id.iv_icon, it.normalIcon)
            }
            val value = if (it.ext2 is Int) {
                if (it.ext2 as Int <= 0) {
                    null
                } else {
                    if (it.ext2 as Int <= 99) {
                        it.ext2.toString()
                    } else {
                        "99+"
                    }
                }
            } else {
                null
            }
            holder.setText(R.id.tv_tips, value)
        }
    }

}