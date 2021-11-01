package net.linkmate.app.ui.adapter

import net.linkmate.app.R
import org.view.libwidget.LeoRvAdapter

/** 

Created by admin on 2020/10/17,19:15

 */
class EmptyAdapter(resId: Int = R.string.no_data) : LeoRvAdapter<Int>() {
    init {
        data = listOf(resId)
    }

    override fun getItemLayout(position: Int): Int {
        return R.layout.layout_empty_view
    }

    override fun bindData(helper: ItemHelper, data: Int, payloads: MutableList<Any>?) {
        helper.setText(R.id.txt_empty, data)
    }

}