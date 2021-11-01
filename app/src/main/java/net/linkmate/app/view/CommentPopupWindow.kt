package net.linkmate.app.view

import android.content.Context
import android.graphics.Rect
import android.util.AttributeSet
import android.view.*
import android.widget.PopupWindow
import androidx.arch.core.util.Function
import androidx.core.widget.addTextChangedListener
import kotlinx.android.synthetic.main.popwindow_comment.view.*
import libs.source.common.utils.InputMethodUtils
import net.linkmate.app.R


/** 评论框
 * @author Raleigh.Luo
 * date：20/11/23 13
 * describe：
 */
class CommentPopupWindow : PopupWindow {
    private val context: Context

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        this.context = context
        init()
    }

    constructor(context: Context) : super(context) {
        this.context = context
        init()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        this.context = context
        init()
    }

    private fun init() {
        contentView = LayoutInflater.from(context).inflate(R.layout.popwindow_comment, null, false)
        setBackgroundDrawable(context.getDrawable(R.color.color_bg_grey250))
        width = ViewGroup.LayoutParams.MATCH_PARENT
        isOutsideTouchable = true
        isTouchable = true
        isFocusable = true
        //设置键盘顶起输入框 必须结合使用 showAtLocation
        setInputMethodMode(INPUT_METHOD_NEEDED);
        setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        animationStyle = R.style.PopupWindowAnim
        with(contentView) {
            etComment.viewTreeObserver.addOnGlobalLayoutListener(onGlobalLayoutListener)
            btnSend.setOnClickListener {
                sendCallBack?.apply(etComment.text.toString())
            }
            etComment.addTextChangedListener {
                btnSend.isEnabled = if (etComment.text.trim().toString().length > 0) true else false
            }
        }
    }

    fun setHint(hint: String) {
        with(contentView) {
            etComment.setHint(hint)
        }
    }

    fun setText(text: String) {
        with(contentView) {
            etComment.setText(text)
        }
    }

    private val onGlobalLayoutListener: ViewTreeObserver.OnGlobalLayoutListener = ViewTreeObserver.OnGlobalLayoutListener {
        with(contentView) {
            val r = Rect()
            getGlobalVisibleRect(r)
            windowHeight = r.bottom - r.top
            windowHeightCallBack?.apply(windowHeight)
        }
    }


    var windowHeightCallBack: Function<Int, Void>? = null

    //发送按钮回调
    var sendCallBack: Function<String, Void>? = null

    //整个window的高度
    var windowHeight = 0
        private set(value) {
            field = value
        }


    override fun showAsDropDown(anchor: View?) {
        super.showAsDropDown(anchor)
    }

    override fun showAsDropDown(anchor: View?, xoff: Int, yoff: Int) {
        super.showAsDropDown(anchor, xoff, yoff)
        showKeyboard()
    }

    override fun showAsDropDown(anchor: View?, xoff: Int, yoff: Int, gravity: Int) {
        super.showAsDropDown(anchor, xoff, yoff, gravity)
        showKeyboard()
    }

    override fun showAtLocation(parent: View?, gravity: Int, x: Int, y: Int) {
        super.showAtLocation(parent, gravity, x, y)
        showKeyboard()
    }

    override fun setOnDismissListener(onDismissListener: OnDismissListener?) {
        super.setOnDismissListener(onDismissListener)
        with(contentView) {
            etComment.viewTreeObserver.removeOnGlobalLayoutListener(onGlobalLayoutListener)
        }

    }

    /**
     *  弹出软键盘
     */
    private fun showKeyboard() {
        with(contentView) {
            etComment.requestFocus()
            InputMethodUtils.showKeyboard(context, etComment, 200)
        }
    }
}