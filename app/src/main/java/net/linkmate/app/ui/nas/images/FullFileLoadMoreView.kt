package net.linkmate.app.ui.nas.images

import com.chad.library.adapter.base.loadmore.LoadMoreView

import net.linkmate.app.R

class FullFileLoadMoreView : LoadMoreView() {

    override fun getLayoutId(): Int {
        return R.layout.quick_full_file_view_load_more
    }

    override fun getLoadingViewId(): Int {
        return R.id.full_load_more_loading_view
    }

    override fun getLoadFailViewId(): Int {
        return R.id.full_load_more_load_fail_view
    }

    override fun getLoadEndViewId(): Int {
        return R.id.full_load_more_load_end_view
    }
}
