package net.linkmate.app.ui.nas.safe_box.control

import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import net.linkmate.app.R
import net.linkmate.app.util.ToastUtils
import net.sdvn.nascommon.utils.InputMethodUtils

/**
create by: 86136
create time: 2021/4/20 10:19
Function description:
 */

class FormatEnsureDialog(val title: String, val description: String, private val inputStr: String?, private val ensureClick: () -> Unit) : DialogFragment() {
    private var boolean = false
    private lateinit var edit: EditText
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        val view: View = inflater.inflate(R.layout.dialog_fromat_ensure, null)
        view.findViewById<TextView>(R.id.format_cancel).setOnClickListener {
            dismiss()
        }

        view.findViewById<TextView>(R.id.format_ensure_title).text = title
        view.findViewById<TextView>(R.id.format_ensure_info).text = description
        edit = view.findViewById(R.id.format_ensure_edt)

        if (!TextUtils.isEmpty(inputStr)) {
            edit.hint = inputStr
            view.findViewById<View>(R.id.format_ensure_fl).setOnClickListener {
                edit.requestFocus()
                InputMethodUtils.showKeyboard(requireContext(),edit)
            }
        } else {
            view.findViewById<View>(R.id.format_ensure_fl).visibility = View.GONE
        }

        view.findViewById<TextView>(R.id.format_ensure).setOnClickListener {
            when {
                !inputStr.isNullOrEmpty() && edit.text.toString().trim() != edit.hint.toString() -> {
                    ToastUtils.showToast(description)
                }
                boolean -> {
                    ToastUtils.showToast(getString(R.string.send_request))
                }
                else -> {
                    boolean = true
                    ensureClick.invoke()
                }
            }
        }
        return view
    }



}