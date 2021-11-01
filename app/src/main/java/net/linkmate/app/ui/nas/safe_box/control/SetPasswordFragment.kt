package net.linkmate.app.ui.nas.safe_box.control

import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import io.weline.repo.api.ERROR_40212
import io.weline.repo.api.V5HttpErrorNo
import kotlinx.android.synthetic.main.fragment_safe_box_init.*
import kotlinx.android.synthetic.main.fragment_set_password.*
import kotlinx.android.synthetic.main.fragment_set_password.iv_close
import kotlinx.android.synthetic.main.fragment_set_password.next_btn
import kotlinx.android.synthetic.main.fragment_set_password.pwd_it
import kotlinx.android.synthetic.main.fragment_set_password.re_pwd_it
import kotlinx.android.synthetic.main.fragment_set_password.title_layout
import libs.source.common.livedata.Status
import net.linkmate.app.R
import net.linkmate.app.util.ToastUtils
import net.sdvn.nascommon.BaseFragment


class SetPasswordFragment : BaseFragment() {


    private val viewModel by viewModels<SafeBoxModel>({ requireActivity() })
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_set_password, container, false)
    }

    private lateinit var mArgs: SetPasswordFragmentArgs;

    override fun initView(view: View) {
        iv_close.setOnClickListener {
            findNavController().navigateUp()
        }
        pwd_it.liveData.observe(this, Observer {
            if (it.isPassChecked) {
                re_pwd_it.setSameStr(it.valueStr)
            } else {
                re_pwd_it.setSameStr("")
            }
        })

        next_btn.setOnClickListener {
            val pwdStr = pwd_it.checkValue
            if (TextUtils.isEmpty(pwdStr)) {
                ToastUtils.showToast(R.string.password_description_20)
                return@setOnClickListener
            }
            re_pwd_it.setSameStr(pwdStr)
            val rePwdStr = re_pwd_it.checkValue
            if (TextUtils.isEmpty(pwdStr)) {
                ToastUtils.showToast(R.string.pls_input_new_pwd)
            } else if (!pwdStr.equals(rePwdStr)) {
                ToastUtils.showToast(R.string.new_pwd_are_different)
            } else {
                viewModel.resetSafeBoxByOldKey(devId!!, mArgs.trans, rePwdStr!!).observe(this, Observer {
                    if (it.status == Status.SUCCESS) {
                        ToastUtils.showToast(R.string.modify_succeed)
                        requireActivity().finish()
                    } else if (it.status == Status.ERROR) {
                        ToastUtils.showToast(V5HttpErrorNo.getResourcesId(it.code ?: 0))
                        if (it.code == ERROR_40212) {
                            findNavController().navigateUp()
                        }
                    }
                })
            }
        }
    }

    override fun getLayoutResId(): Int {
        return R.layout.fragment_set_password
    }

    override fun getTopView(): View? {
        return title_layout
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            mArgs = SetPasswordFragmentArgs.fromBundle(it)
        }
    }
}