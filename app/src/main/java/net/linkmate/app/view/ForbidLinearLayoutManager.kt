package net.linkmate.app.view

import android.content.Context
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import net.linkmate.app.ui.activity.nasApp.deviceDetial.FullyLinearLayoutManager

/** 禁止滑动
 * @author Raleigh.Luo
 * date：20/12/25 16
 * describe：
 */
class ForbidLinearLayoutManager(context: Context) :LinearLayoutManager(context,LinearLayoutManager.VERTICAL,false
){
    override fun canScrollVertically(): Boolean {
        return true
    }

    override fun canScrollHorizontally(): Boolean {
        return false
    }


    override fun isAutoMeasureEnabled(): Boolean {
        return true
    }

    /**
     * 处理列表刷新问题
     * java.lang.IndexOutOfBoundsException: Inconsistency detected. Invalid item position
     */
    override fun onLayoutChildren(recycler: RecyclerView.Recycler?, state: RecyclerView.State?) {
        try {
            super.onLayoutChildren(recycler, state)
        } catch (e: IndexOutOfBoundsException) {
            e.printStackTrace()
        }
    }
}