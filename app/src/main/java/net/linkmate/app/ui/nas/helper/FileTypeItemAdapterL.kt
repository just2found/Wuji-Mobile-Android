package net.linkmate.app.ui.nas.helper

import android.view.ViewGroup
import androidx.core.view.updateLayoutParams
import androidx.recyclerview.widget.GridLayoutManager
import com.chad.library.adapter.base.BaseSectionQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.chad.library.adapter.base.entity.SectionEntity
import net.linkmate.app.R
import net.linkmate.app.util.Dp2PxUtils
import net.sdvn.nascommon.model.FileTypeItem

class FileTypeItemAdapterL : BaseSectionQuickAdapter<SectionEntity<FileTypeItem>, BaseViewHolder>
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
            val margins = Dp2PxUtils.dp2px(holder.itemView.context, 10)
            if (tag != it.flag) {
                holder.itemView.setTag(keyItem, it.flag)
                val lm = recyclerView.layoutManager
                if (lm is GridLayoutManager) {
                    val spanSize = lm.spanSizeLookup.getSpanSize(position)
                    val spanCount = lm.spanCount
                    val spanIndex = lm.spanSizeLookup.getSpanIndex(position, spanCount)
                    val isRight = spanIndex + 1 == spanCount
                    val isStart = spanIndex == 0
                    val isBottom = position + spanCount >= itemCount
                    holder.setGone(R.id.line_right, false)
                    holder.setGone(R.id.line_bottom, false)
                    holder.itemView.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                        when {
                            isRight -> {
                                setMargins(margins, 0, 0, 0)
                            }
                            isStart -> {
                                setMargins(0, 0, margins, 0)
                            }
                            else -> {
                                setMargins(margins/2, 0, margins/2, 0)
                            }
                        }
                    }
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