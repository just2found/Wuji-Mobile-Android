package net.linkmate.app.view

import android.content.DialogInterface
import android.graphics.Point
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import kotlinx.android.synthetic.main.dialog_hint.*
import net.linkmate.app.R

/**
 * @author Raleigh.Luo
 * date：20/8/18 13
 * describe：
 */
class HintDialog : DialogFragment() {
    private var onClickListener: View.OnClickListener? = null
    var onDismissListener: DialogInterface.OnDismissListener? = null
    var onShowListener: DialogInterface.OnShowListener? = null

    companion object {
        private const val TITLE = "title"
        private const val CONTENT = "content"
        private const val CONFRIM_TEXT = "confrim_text"
        private const val CANCEL_TEXT = "cancel_text"

        //提示文字颜色
        private const val HINT_COLOR = "hint_color"
        private const val CANCEL_COLOR = "cancel_color"
        private const val CONFIRM_COLOR = "confirm_color"

        @JvmStatic
        fun newInstance(title: String? = null, content: String? = null, hintColor: Int? = null, confrimText: String? = null, cancelText: String? = null,
                        confirmColor: Int? = null, cancelColor: Int? = null) = HintDialog().apply {
            arguments = bundleOf(TITLE to title, CONTENT to content, HINT_COLOR to hintColor,
                    CONFRIM_TEXT to confrimText, CANCEL_TEXT to cancelText,CONFIRM_COLOR to confirmColor,CANCEL_COLOR to cancelColor)
        }
    }

    /*
     * 更新UI
     */
    fun update(title: String? = null, content: String? = null, hintColor: Int? = null, confrimText: String? = null, cancelText: String? = null,
               confirmColor: Int? = null, cancelColor: Int? = null) {
        arguments = bundleOf(TITLE to title, CONTENT to content, HINT_COLOR to hintColor,
                CONFRIM_TEXT to confrimText, CANCEL_TEXT to cancelText, CANCEL_TEXT to cancelText,CONFIRM_COLOR to confirmColor,CANCEL_COLOR to cancelColor)
    }

    fun setOnClickListener(onClickListener: View.OnClickListener): HintDialog {
        this.onClickListener = onClickListener
        return this
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.dialog_hint, null);
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //必须在onCreate方法中设置
        setStyle(STYLE_NO_FRAME, R.style.DialogTheme2)
    }

    private var tvNegative: TextView? = null
    private var tvPositive: TextView? = null
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        tvNegative = view?.findViewById<TextView>(R.id.negative)
        tvPositive = view?.findViewById<TextView>(R.id.positive)
        tvPositive?.setBackgroundResource(R.drawable.bg_ripple_trans_gray)
        tvNegative?.setBackgroundResource(R.drawable.bg_ripple_trans_gray)
        tvNegative?.setOnClickListener {
            dismiss()
            onClickListener?.onClick(it)
        }
        tvPositive?.setOnClickListener {
            dismiss()
            onClickListener?.onClick(it)
        }
        arguments?.getString(TITLE)?.let {
            tvTitle.setText(it)
        }
        arguments?.getString(CONTENT)?.let {
            tvConfirmHint.visibility = View.VISIBLE
            tvConfirmHint.setText(it)
        }?:let{
            tvConfirmHint.visibility = View.GONE
        }

        arguments?.getInt(HINT_COLOR)?.let {
            if (it != 0) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    tvConfirmHint.setTextColor(resources.getColor(it, null))
                } else {
                    tvConfirmHint.setTextColor(resources.getColor(it))
                }
            }
        }
        arguments?.getInt(CONFIRM_COLOR)?.let {
            if (it != 0) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    tvPositive?.setTextColor(resources.getColor(it, null))
                } else {
                    tvPositive?.setTextColor(resources.getColor(it))
                }
            }
        }
        arguments?.getInt(CANCEL_COLOR)?.let {
            if (it != 0) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    tvNegative?.setTextColor(resources.getColor(it, null))
                } else {
                    tvNegative?.setTextColor(resources.getColor(it))
                }
            }
        }
        arguments?.getString(CANCEL_TEXT)?.let {
            tvNegative?.setText(it)
            tvNegative?.visibility = View.VISIBLE
        }?:let{
            tvNegative?.visibility = View.GONE
        }
        arguments?.getString(CONFRIM_TEXT)?.let {
            tvPositive?.setText(it)
        }
    }


    override fun onResume() {
        val params = dialog?.window?.attributes
        val d = getActivity()?.getWindowManager()?.getDefaultDisplay()
        d?.let {
            val point = Point()
            d.getSize(point)
            //设置宽为屏幕3/4
            params?.width = point.x * 3 / 4
            dialog?.window?.attributes = params
        }
        super.onResume()
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        onDismissListener?.onDismiss(null)
    }

    override fun show(transaction: FragmentTransaction, tag: String?): Int {
        onShowListener?.onShow(null)
        return super.show(transaction, tag)
    }

    override fun show(manager: FragmentManager, tag: String?) {
        onShowListener?.onShow(null)
        super.show(manager, tag)
    }

    override fun showNow(manager: FragmentManager, tag: String?) {
        onShowListener?.onShow(null)
        super.showNow(manager, tag)
    }
}