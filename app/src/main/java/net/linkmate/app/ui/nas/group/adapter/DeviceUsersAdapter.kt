package net.linkmate.app.ui.nas.group.adapter

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import net.linkmate.app.R
import net.linkmate.app.ui.nas.images.ISelection
import net.sdvn.nascommon.widget.CheckableImageButton
import org.view.libwidget.singleClick

/**
 * Description:
 * @author  admin
 * CreateDate: 2021/6/5
 */

class DeviceUsersAdapter : BaseQuickAdapter<String, BaseViewHolder>
    (R.layout.item_selectable_text), ISelection {
    var isSingleSelection = false
    override fun convert(helper: BaseViewHolder, item: String) {
        helper.setText(R.id.tv_name, item)
        val checkableImageButton = helper.getView<CheckableImageButton>(R.id.iv_select)
        helper.setChecked(R.id.iv_select, selection.contains(helper.adapterPosition))
        helper.itemView.singleClick {
            onItemSelectChange(helper, checkableImageButton)
        }
        checkableImageButton.singleClick {
            onItemSelectChange(helper, checkableImageButton)
        }
    }

    private fun onItemSelectChange(
        helper: BaseViewHolder,
        checkableImageButton: CheckableImageButton
    ) {
        if (isSingleSelection) {
            selection.clear()
            select(helper.adapterPosition, !checkableImageButton.isChecked)
            notifyDataSetChanged()
        } else {
            toggleSelection(helper.adapterPosition)
        }
    }

    // ----------------------
    // Selection
    // ----------------------

    var isSetMultiModel: Boolean = false
        set(value) {
            field = value
            if (value) {
                deselectAll()
            }
            notifyDataSetChanged()
        }
    override val selection: HashSet<Int> = hashSetOf()

    fun toggleSelection(pos: Int) {
        if (selection.contains(pos)) selection.remove(pos) else selection.add(pos)
        notifyItemChanged(pos)
    }

    fun select(pos: Int, selected: Boolean) {
        if (selected) selection.add(pos) else selection.remove(pos)
        notifyItemChanged(pos)
    }

    override fun selectRange(start: Int, end: Int, selected: Boolean) {
        for (i in start..end) {
            if (selected) selection.add(i) else selection.remove(i)
        }
        notifyItemRangeChanged(start, end - start + 1)
    }

    fun deselectAll() {
        // this is not beautiful...
        selection.clear()
        notifyDataSetChanged()
    }

    fun selectAll() {
        mData?.forEachIndexed { index, sectionEntity ->
            selection.add(index)
        }
        notifyDataSetChanged()
    }

    fun getSelectedItems(): List<String> {
        return mData.filterIndexed { index, _ ->
            selection.contains(index)
        }
    }

    val countSelected: Int
        get() = selection.size

}