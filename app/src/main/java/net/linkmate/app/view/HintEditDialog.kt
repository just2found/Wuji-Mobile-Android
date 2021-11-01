package net.linkmate.app.view

import android.content.DialogInterface
import android.graphics.Point
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import kotlinx.android.synthetic.main.dialog_hint_edit.*
import kotlinx.android.synthetic.main.dialog_hint_edit.tvConfirmHint
import kotlinx.android.synthetic.main.dialog_hint_edit.tvTitle
import net.linkmate.app.R
import net.sdvn.nascommon.utils.InputMethodUtils

/** 提示输入框
 * @author Raleigh.Luo
 * date：21/1/15 15
 * describe：
 */
class HintEditDialog : DialogFragment() {
    private var onClickListener: View.OnClickListener? = null
    var onDismissListener: DialogInterface.OnDismissListener? = null
    var onShowListener: DialogInterface.OnShowListener? = null

    companion object {
        private const val TITLE = "title"
        private const val CONTENT = "content"
        private const val CONTENT_TEXT_COLOR = "content_text_color"////提示文字颜色 如资源R.color.xxx
        private const val CHECKED_BOX_TEXT = "checked_box_text"//勾选框文本
        private const val IS_CHECKED_BOX = "is_checked_box"//勾选框是否勾选
        private const val CHECKBOX_ENABLE = "checkbox_enable"//是否可更改勾选
        private const val EDIT_HINT = "edit_hint"//输入框提示
        private const val EDIT_CONTENT = "edit_content"//输入框内容
        private const val MATCH_EDIT_TO_HINT = "match_edit_to_hint"//检查输入是否提示匹配,匹配后确定按钮才可用
        private const val CONFRIM_TEXT = "confrim_text"
        private const val CANCEL_TEXT = "cancel_text"


        @JvmStatic
        fun newInstance(title: String? = null, content: String? = null, contentTextColor: Int? = null,
                        editHint: String? = null, editContent: String? = null,
                        matchEditToHint: Boolean = false, checkBoxText: String? = null, isCheckedBox: Boolean = false,
                        checkboxEnable: Boolean = true,
                        confrimText: String? = null, cancelText: String? = null) = HintEditDialog().apply {
            arguments = bundleOf(TITLE to title, CONTENT to content, CONTENT_TEXT_COLOR to contentTextColor,
                    EDIT_HINT to editHint, EDIT_CONTENT to editContent, MATCH_EDIT_TO_HINT to matchEditToHint,
                    CHECKED_BOX_TEXT to checkBoxText, IS_CHECKED_BOX to isCheckedBox, CHECKBOX_ENABLE to checkboxEnable,
                    CONFRIM_TEXT to confrimText, CANCEL_TEXT to cancelText)
        }
    }


    /*
     * 更新UI
     */
    fun update(title: String? = null, content: String? = null, contentTextColor: Int? = null,
               editHint: String? = null, editContent: String? = null, matchEditToHint: Boolean = false,
               checkBoxText: String? = null, isCheckedBox: Boolean = false, checkboxEnable: Boolean = true,
               confrimText: String? = null, cancelText: String? = null) {
        arguments = bundleOf(TITLE to title, CONTENT to content, CONTENT_TEXT_COLOR to contentTextColor,
                EDIT_HINT to editHint, EDIT_CONTENT to editContent, MATCH_EDIT_TO_HINT to matchEditToHint,
                CHECKED_BOX_TEXT to checkBoxText, IS_CHECKED_BOX to isCheckedBox, CHECKBOX_ENABLE to checkboxEnable,
                CONFRIM_TEXT to confrimText, CANCEL_TEXT to cancelText)
    }

    fun setOnClickListener(onClickListener: View.OnClickListener): HintEditDialog {
        this.onClickListener = onClickListener
        return this
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.dialog_hint_edit, null);
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //必须在onCreate方法中设置
        setStyle(STYLE_NO_FRAME, R.style.DialogTheme2)
    }


    private var tvPositive: TextView? = null
    private var tvNegative: TextView? = null

    // 记录编辑位置
    var position: Int = -1
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val matchEditToHint = arguments?.getBoolean(MATCH_EDIT_TO_HINT) ?: false

        etEdit.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                tvEditHint.visibility = if (etEdit.text.trim().length > 0) View.GONE else View.VISIBLE
                if (matchEditToHint) {
                    tvPositive?.isEnabled = (etEdit.text.toString() == tvEditHint.text.toString())
                }
            }

            override fun afterTextChanged(editable: Editable) {
            }
        })
        arguments?.getString(TITLE)?.let {
            tvTitle.setText(it)
        }
        arguments?.getString(CONTENT)?.let {
            tvConfirmHint.setText(it)
        }
        arguments?.getString(EDIT_HINT)?.let {
            flEdit.visibility = View.VISIBLE
            tvEditHint.setText(it)
        }

        arguments?.getString(EDIT_CONTENT)?.let {
            //有值时不设置默认保存  默认自动保存
            flEdit.visibility = View.VISIBLE
            etEdit.setText(it)

        }
        arguments?.getString(CHECKED_BOX_TEXT)?.let {
            cbCheck.visibility = View.VISIBLE
            cbCheck.setText(it)
        } ?: let {
            cbCheck.visibility = View.GONE
        }
        arguments?.getBoolean(IS_CHECKED_BOX)?.let {
            cbCheck.isChecked = it
        }
        arguments?.getBoolean(CHECKBOX_ENABLE)?.let {
            cbCheck.isEnabled = it
        }


        arguments?.getInt(CONTENT_TEXT_COLOR)?.let {
            requireContext().resources?.getColor(it)?.let { it1 -> tvConfirmHint.setTextColor(it1) }
        }
        InputMethodUtils.showKeyboard(requireContext(), etEdit, 200)
        tvPositive = view?.findViewById(R.id.positive)
        tvNegative = view?.findViewById(R.id.negative)
        tvPositive?.isEnabled = !matchEditToHint
        tvPositive?.setBackgroundResource(R.drawable.bg_ripple_trans_gray)
        tvNegative?.setBackgroundResource(R.drawable.bg_ripple_trans_gray)
        tvPositive?.setOnClickListener {
            arguments?.putString(EDIT_CONTENT, getEditText())
            arguments?.putBoolean(IS_CHECKED_BOX, isCheckedBox())
            onClickListener?.onClick(it)
        }
        tvNegative?.setOnClickListener {
            dismiss()
            onClickListener?.onClick(it)
        }
        arguments?.getString(HintEditDialog.CANCEL_TEXT)?.let {
            tvNegative?.setText(it)
            tvNegative?.visibility = View.VISIBLE
        } ?: let {
            tvNegative?.visibility = View.GONE
        }
        arguments?.getString(HintEditDialog.CONFRIM_TEXT)?.let {
            tvPositive?.setText(it)
        }
        setCancelable(true)
//        setCanceledOnTouchOutside(true)
    }

    fun isCheckedBox(): Boolean {
        return cbCheck.isChecked
    }

    fun getEditText(): String {
        return etEdit.text.toString()
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

    override fun onResume() {
        val params = dialog?.window?.attributes
        val d = getActivity()?.getWindowManager()?.getDefaultDisplay()
        d?.let {
            val point = Point()
            d.getSize(point)
            //设置宽为屏幕3/4
            params?.width = point.x * 4 / 5
            dialog?.window?.attributes = params
        }
        super.onResume()
    }

}