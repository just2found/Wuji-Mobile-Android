package net.linkmate.app.ui.simplestyle.dynamic.delegate

import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

/** 动态 文字收缩代理
 * @author Raleigh.Luo
 * date：20/11/24 17
 * describe：
 */
abstract class TextShrinkDelegate {
    companion object {
        @JvmStatic
        fun create(tvText: TextView, tvShrinkView: TextView,recyclerView: RecyclerView): TextShrinkDelegate {
            return TextShrinkDelegateImpl(tvText, tvShrinkView,recyclerView)
        }
    }


    abstract fun setText(key:Int, text: String?)


    abstract fun clear()

    //最大显示行数
    protected val MAX_LINE_COUNT = 6

    protected val STATE_UNKNOW = -1 //未知状态

    protected val STATE_NOT_OVERFLOW = 1 //文本行数小于最大可显示行数

    protected val STATE_COLLAPSED = 2 //折叠状态

    protected val STATE_EXPANDED = 3 //展开状态


}