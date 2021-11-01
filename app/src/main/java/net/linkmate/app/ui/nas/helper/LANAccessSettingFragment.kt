package net.linkmate.app.ui.nas.helper

import android.view.View
import android.widget.EditText
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import kotlinx.android.synthetic.main.fragment_lan_access_setting.*
import kotlinx.android.synthetic.main.include_title_bar.*
import libs.source.common.utils.InputMethodUtils
import net.linkmate.app.R
import net.linkmate.app.base.MyConstants
import net.linkmate.app.ui.nas.TipsBackPressedFragment
import net.linkmate.app.ui.nas.cloud.findNav
import net.linkmate.app.util.ToastUtils
import net.linkmate.app.util.UIUtils
import net.sdvn.nascommon.iface.Callback
import net.sdvn.nascommon.utils.AnimUtils
import net.sdvn.nascommon.viewmodel.NasLanAccessViewModel
import org.view.libwidget.singleClick
import java.util.regex.Pattern

/**
 *
 * @Description: 局域网访问设置
 * @Author: todo2088
 * @CreateDate: 2021/3/9 16:01
 */
class LANAccessSettingFragment : TipsBackPressedFragment() {
    private val nasLanAccessViewModel by viewModels<NasLanAccessViewModel>()
    private val compileRegNumLetterAndChar = Pattern.compile(MyConstants.regNumLetter6_20)

    override fun getTopView(): View? {
        return include_title
    }

    override fun initView(view: View) {
        setEnableOnBackPressed(findNav(this) != null)
        mTipsBar = tipsBar
        itb_tv_title.setText(R.string.device_lan_access)
        itb_iv_left.setImageResource(R.drawable.icon_return)
        itb_iv_left.isVisible = true
        itb_iv_left.singleClick {
            onBackPressed()
        }
        tv_account?.text = strAccountPrefix
        nasLanAccessViewModel.liveDataLoginSession(getDeviceId()).observe(this, Observer { loginSession ->
            tv_account?.text = "$strAccountPrefix${loginSession?.userInfo?.username ?: ""}"
        })
        iv_visible.setOnClickListener {
            UIUtils.togglePasswordStatus(it, editTextTextPersonName, editTextTextPersonName2)
        }
        setOnFocusListener(editTextTextPersonName, line27)
        setOnFocusListener(editTextTextPersonName2, line28)
        editTextTextPersonName.addTextChangedListener { text ->
            if (text?.length ?: 0 > 20) {
                AnimUtils.sharkEditText(editTextTextPersonName)
            }
        }
        editTextTextPersonName2.addTextChangedListener { text ->
            if (text?.length ?: 0 > 20) {
                AnimUtils.sharkEditText(editTextTextPersonName2)
            }
        }
        button_confirm.singleClick {
            if (editTextTextPersonName.isFocused) {
                InputMethodUtils.hideKeyboard(requireContext(), editTextTextPersonName)
            } else if (editTextTextPersonName2.isFocused) {
                InputMethodUtils.hideKeyboard(requireContext(), editTextTextPersonName2)
            }
            val trim = editTextTextPersonName.text.toString().trim { it <= ' ' }
            if (!compileRegNumLetterAndChar.matcher(trim).matches()) {
                ToastUtils.showToast(R.string.password_must_contains_num_letter_6_20)
                editTextTextPersonName.requestFocus()
                return@singleClick
            }
            val trim1 = editTextTextPersonName2.text.toString().trim { it <= ' ' }
            if (!compileRegNumLetterAndChar.matcher(trim1).matches()) {
                editTextTextPersonName2.requestFocus()
                ToastUtils.showToast(R.string.password_must_contains_num_letter_6_20)
                return@singleClick
            }
            if (trim != trim1) {
                editTextTextPersonName2.requestFocus()
                ToastUtils.showToast(getString(R.string.error_string_pwd_confirm))
                return@singleClick
            }
            commit(trim)
        }
    }

    private fun setOnFocusListener(editText: EditText, lineView: View) {
        editText.setOnFocusChangeListener { _, hasFocus ->
            lineView.isSelected = hasFocus
        }
    }

    private fun commit(trim: String) {
        nasLanAccessViewModel.setLanAccess(getDeviceId(), trim, Callback {
            if (it) {
                onBackPressed()
            }
        })
    }

    private val strAccountPrefix: String by lazy {
        "${resources.getString(R.string.account)} : "
    }

    fun getDeviceId(): String {
        return devId ?: kotlin.run {
            onBackPressed()
            ""
        }
    }

    override fun onBackPressed(): Boolean {
        if (findNav(this)?.popBackStack() == true) {
            return true
        }
        requireActivity().onBackPressed()
        return true
    }

    override fun getLayoutResId(): Int {
        return R.layout.fragment_lan_access_setting
    }

}