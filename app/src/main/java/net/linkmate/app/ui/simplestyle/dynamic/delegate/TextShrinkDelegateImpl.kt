package net.linkmate.app.ui.simplestyle.dynamic.delegate

import android.text.TextUtils
import android.util.SparseArray
import android.view.View
import android.view.ViewTreeObserver
import android.widget.TextView
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.RecyclerView
import net.linkmate.app.R
import net.linkmate.app.base.MyApplication
import net.linkmate.app.util.ToastUtils
import net.sdvn.cmapi.util.ClipboardUtils

/** 动态 文字收缩代理 实现
 * @author Raleigh.Luo
 * date：20/11/24 17
 * describe：
 */
internal open class TextShrinkDelegateImpl(private val tvText: TextView, private val tvShrinkView: TextView,
                                           private val recyclerView: RecyclerView) : TextShrinkDelegate() {
    //轻量级数据 保存文本状态集合
    protected val mTextStateList: SparseArray<Int> = SparseArray()

    private var beforeExpendHeight = 0

    /**
     * key 唯一键
     */
    override fun setText(key: Int, text: String?) {
        //避免重复加载
        if (tvText.getTag() == key) return

        tvText.setTag(key)
        if (TextUtils.isEmpty(text)) {
            tvText.visibility = View.GONE
            tvShrinkView.visibility = View.GONE
            return
        } else {
            tvText.visibility = View.VISIBLE
        }
        //添加监听器
        setShrinkViewClickListener(key)
        setTextViewLongClickListener()
        tvText.setText(text)
        val state = mTextStateList.get(key, STATE_UNKNOW)
        //第一次初始化，未知状态
        if (state == STATE_UNKNOW) {
            tvText.getViewTreeObserver().addOnPreDrawListener(object : ViewTreeObserver.OnPreDrawListener {
                override fun onPreDraw(): Boolean {
                    //这个回掉会调用多次，获取完行数后记得注销监听
                    tvText.getViewTreeObserver().removeOnPreDrawListener(this)
                    //如果内容显示的行数大于最大显示行数
                    if (tvText.getLineCount() > MAX_LINE_COUNT) {
                        tvText.setMaxLines(MAX_LINE_COUNT) //设置最大显示行数
                        tvShrinkView.setVisibility(View.VISIBLE) //显示“全文”
                        tvShrinkView.setText(MyApplication.getContext().getString(R.string.all_text))
                        mTextStateList.put(key, STATE_COLLAPSED) //保存状态
                    } else {
                        tvShrinkView.setVisibility(View.GONE)
                        mTextStateList.put(key, STATE_NOT_OVERFLOW)
                    }
                    return true
                }
            })
        } else {
            //如果之前已经初始化过了，则使用保存的状态。
            when (state) {
                STATE_NOT_OVERFLOW -> tvShrinkView.setVisibility(View.GONE)
                STATE_COLLAPSED -> {
                    tvText.setMaxLines(MAX_LINE_COUNT)
                    tvShrinkView.setVisibility(View.VISIBLE)
                    tvShrinkView.setText(MyApplication.getContext().getString(R.string.all_text))
                }
                STATE_EXPANDED -> {
                    tvText.setMaxLines(Int.MAX_VALUE)
                    tvShrinkView.setVisibility(View.VISIBLE)
                    tvShrinkView.setText(MyApplication.getContext().getString(R.string.shrink_text))
                }
            }
        }
    }

    protected open fun setShrinkViewClickListener(key: Int) {
        tvShrinkView.setOnClickListener {
            val state = mTextStateList[key, STATE_UNKNOW]
            if (state == STATE_COLLAPSED) {//展开事件
                beforeExpendHeight = tvText.measuredHeight
                tvText.setMaxLines(Int.MAX_VALUE)
                tvShrinkView.setText(MyApplication.getContext().getString(R.string.shrink_text))
                mTextStateList.put(key, STATE_EXPANDED)
            } else if (state == STATE_EXPANDED) {//收起事件
                val afterExpendHeight = tvText.measuredHeight
                tvText.setMaxLines(MAX_LINE_COUNT)
                tvShrinkView.setText(MyApplication.getContext().getString(R.string.all_text))
                mTextStateList.put(key, STATE_COLLAPSED)
                //滑动到当前项
                recyclerView.smoothScrollBy(0, beforeExpendHeight - afterExpendHeight)
                //重要，否则无法触发AppBarLayout的behavior联动
                recyclerView.startNestedScroll(ViewCompat.SCROLL_AXIS_VERTICAL, ViewCompat.TYPE_NON_TOUCH)
            }
        }
    }

    private fun clipString(content: String) {
        if (!TextUtils.isEmpty(content)) {
            val context = MyApplication.getContext()
            ClipboardUtils.copyToClipboard(context, content)
            ToastUtils.showToast(context.getString(R.string.Copied))
        }
    }

    private fun setTextViewLongClickListener() {
        tvText.setOnLongClickListener {
            clipString(tvText.text.toString())
            true
        }
    }

    override fun clear() {
        mTextStateList.clear()
    }


}