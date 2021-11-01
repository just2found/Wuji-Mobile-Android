package net.linkmate.app.view

import android.content.Context
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

/**
 * @author Raleigh.Luo
 * date：21/3/5 10
 * describe：
 */
class GridLayoutManager(context: Context, spanCount:Int ,
                        @RecyclerView.Orientation orientation:Int,  reverseLayout:Boolean)  :
        GridLayoutManager(context, spanCount, orientation, reverseLayout){
    /**
     * 处理列表局部刷新问题
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