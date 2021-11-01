package net.linkmate.app.ui.nas.samba

import android.os.Bundle
import android.view.View
import android.widget.EditText
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import kotlinx.android.synthetic.main.fragment_lan_access_setting.*
import libs.source.common.utils.InputMethodUtils
import net.linkmate.app.R
import net.linkmate.app.base.BaseActivity
import net.linkmate.app.base.MyConstants
import net.linkmate.app.ui.fragment.BaseFragment
import net.linkmate.app.util.ToastUtils
import net.linkmate.app.util.UIUtils
import net.sdvn.cmapi.CMAPI
import net.sdvn.nascommon.constant.AppConstants
import net.sdvn.nascommon.utils.AnimUtils
import org.view.libwidget.singleClick
import java.util.regex.Pattern

/**SAMBA设置密码
 * @author Raleigh.Luo
 * date：21/6/5 14
 * describe：
 */
class SAMBASettingPasswordFragment : BaseFragment() {
    private val activityViewModel: SAMBAViewModel by activityViewModels()
    private val viewModel: SAMBASettingPasswordViewModel by viewModels()
    private val compileRegNumLetterAndChar = Pattern.compile(MyConstants.regNumLetter6_20)
    override fun initView(view: View, savedInstanceState: Bundle?) {
        viewModel.deviceId = activityViewModel.deviceId
        tv_account.text = String.format("%s : %s", resources.getString(R.string.account), activityViewModel.getUserName.value)
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
            if (CMAPI.getInstance().isConnected) {//连接成功才请求
                activityViewModel.loading(true)
                viewModel.setPassword(trim)
            } else {
                ToastUtils.showToast(R.string.network_not_available)
            }
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initObserver()
    }

    private fun initObserver() {
        //设置密码结果
        viewModel.setPasswordResult.observe(this, Observer {
            activityViewModel.loading(false)
            if (it) {//设置成功返回
                ToastUtils.showToast(R.string.setting_success)
                requireActivity().onBackPressed()
            }
        })
        //监听用户主动取消进度条事件
        activityViewModel.cancelLoading.observe(this, Observer {
            if (it != null) viewModel.dispose()
        })
    }

    private fun setOnFocusListener(editText: EditText, lineView: View) {
        editText.setOnFocusChangeListener { _, hasFocus ->
            lineView.isSelected = hasFocus
        }
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_samba_setting_password
    }
}