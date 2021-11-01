package net.linkmate.app.view

import android.content.DialogInterface
import android.graphics.Point
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import kotlinx.android.synthetic.main.dialog_circle_edit.*
import net.linkmate.app.R
import net.sdvn.nascommon.utils.AnimUtils
import net.sdvn.nascommon.utils.InputMethodUtils

/** 编辑框
 * @author Raleigh.Luo
 * date：20/8/25 09
 * describe：
 */
class EditDialog : DialogFragment() {
    var onDismissListener: DialogInterface.OnDismissListener? = null
    var onShowListener: DialogInterface.OnShowListener? = null
    var onClickListener: View.OnClickListener? = null

    companion object {
        private const val TITLE = "title"
        private const val HINT = "hint"
        private const val BOTTOM_HINT = "bottom_hint"
        private const val UNIT = "unit"
        private const val MAX_VALUE = "max_value"

        //输入文本最大字符
        private const val MAX_EMS = "max_ems"
        private const val ORIGINAL_TEXT = "original_text"
        private const val CONFRIM_TEXT = "confrim_text"
        private const val CANCEL_TEXT = "cancel_text"
        private const val INPUT_TYPE = "input_type"

        @JvmStatic
        fun newInstance(title: String? = null,
                        hint: String? = null,
                        originalText: String? = null,
                        confrimText: String? = null,
                        cancelText: String? = null,
                        inputType: Int? = null,
                        unit: String? = null,
                        bottomHint: String? = null,
                        maxValue: Float? = null,
                        maxEms: Int? = null
        ): EditDialog = EditDialog().apply {
            update(title, hint, originalText, confrimText, cancelText, inputType,
                    unit, bottomHint, maxValue, maxEms)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.dialog_circle_edit, null)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //必须在onCreate方法中设置
        setStyle(STYLE_NO_FRAME, R.style.DialogTheme2)
    }

    /*
     * 更新UI
     */
    fun update(title: String? = null,
               hint: String? = null,
               originalText: String? = null,
               confrimText: String? = null,
               cancelText: String? = null,
               inputType: Int? = null,
               unit: String? = null,
               bottomHint: String? = null,
               maxValue: Float? = null, maxEms: Int? = null) {
        arguments = bundleOf(TITLE to title, HINT to hint, ORIGINAL_TEXT to originalText,
                CONFRIM_TEXT to confrimText, CANCEL_TEXT to cancelText, INPUT_TYPE to inputType,
                UNIT to unit, BOTTOM_HINT to bottomHint, MAX_VALUE to maxValue, MAX_EMS to maxEms
        )
    }

    private var tvNegative: TextView? = null
    private var tvPositive: TextView? = null
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initEvent()
        tvNegative = view?.findViewById<TextView>(R.id.negative)
        tvPositive = view?.findViewById<TextView>(R.id.positive)
        tvPositive?.setBackgroundResource(R.drawable.bg_ripple_trans_gray)
        tvNegative?.setBackgroundResource(R.drawable.bg_ripple_trans_gray)
        tvNegative?.setOnClickListener {
            dismiss()
            onClickListener?.onClick(it)
        }
        tvPositive?.setOnClickListener {
            if (getContent()?.length?:0 == 0) {
                //输入不能为空
                AnimUtils.sharkEditText(requireContext(), etContent)
            } else {
                dismiss()
            }
            onClickListener?.onClick(it)
        }
        arguments?.getString(TITLE)?.let {
            tvTitle.text = it

        }
        arguments?.getString(HINT)?.let {
            tvContent.text = it
        }
        arguments?.getString(ORIGINAL_TEXT)?.let {
            etContent.setText(it)
            etContent.setSelection(it.length)
        }
        arguments?.getInt(MAX_EMS)?.let {
            etContent.maxEms = it
        }

        arguments?.getString(BOTTOM_HINT)?.let {
            tvBottomHint.visibility = View.VISIBLE
            tvBottomHint.setText(it)
        } ?: let {
            tvBottomHint.visibility = View.GONE
        }

        arguments?.getString(UNIT)?.let {
            tvUnit.visibility = View.VISIBLE
            tvUnit.setText(it)
        } ?: let {
            tvUnit.visibility = View.GONE
        }

        arguments?.getString(CANCEL_TEXT)?.let {
            tvNegative?.text = it
        }
        arguments?.getString(CONFRIM_TEXT)?.let {
            tvPositive?.text = it
        }
    }

    private fun initEvent() {
        arguments?.getInt(INPUT_TYPE)?.let {
            if (it != 0)
                etContent.inputType = arguments?.getInt(INPUT_TYPE) ?: 0
        }
        etContent.requestFocus()
        context?.let {
            InputMethodUtils.showKeyboard(it, etContent, 200)
        }
        etContent.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                //不能超过最大值 数字
                arguments?.getFloat(MAX_VALUE)?.let {
                    //maxvalue 不能为0,无意义
                    if (it != 0f && !TextUtils.isEmpty(charSequence)) {

                        val s1 = String.format("%.0f", it)
                        try {
                            val value = charSequence.toString().toFloat()
                            if (value > it) {
                                updateEdit(s1)
                            }
                        } catch (e: Exception) {
                        }
                    }
                }
                tvContent.visibility = if (etContent.text.length > 0) View.GONE else View.VISIBLE
            }

            private fun updateEdit(s1: String) {
                etContent.setText(s1)
                etContent.setSelection(s1.length)
                AnimUtils.sharkEditText(requireContext(), etContent)
            }

            override fun afterTextChanged(editable: Editable) {}
        })
    }

    fun getContent(): String? {
        //不输入值或删除已存在的值时，默认设置为0
        arguments?.getFloat(MAX_VALUE)?.let {
            if (etContent.text.length == 0) {
                etContent.setText("0")
            }
        }

        if (TextUtils.isEmpty(etContent.text.toString())) {
            //输入不能为空
            return null
        } else {
            return etContent.text.toString()
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