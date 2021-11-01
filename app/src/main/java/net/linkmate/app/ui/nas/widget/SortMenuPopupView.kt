package net.linkmate.app.ui.nas.widget

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.PopupWindow
import com.chad.library.adapter.base.BaseViewHolder
import net.linkmate.app.R

class SortMenuPopupView(context: Context) : PopupWindow() {

    var baseViewHolder: BaseViewHolder

    init {
        val view = LayoutInflater.from(context).inflate(R.layout.sort_menu_popup, null, false)
        contentView = view
        width = ViewGroup.LayoutParams.WRAP_CONTENT
        height = ViewGroup.LayoutParams.WRAP_CONTENT
        baseViewHolder = BaseViewHolder(view)
        baseViewHolder.addOnClickListener(R.id.sort_by_name)
        baseViewHolder.addOnClickListener(R.id.sort_by_time)
        baseViewHolder.addOnClickListener(R.id.display_by_grid)
        baseViewHolder.addOnClickListener(R.id.display_by_list)
    }

}