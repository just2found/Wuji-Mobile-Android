package net.linkmate.app.ui.nas.safe_box.control

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import io.weline.repo.api.ERROR_40208
import io.weline.repo.api.V5HttpErrorNo
import kotlinx.android.synthetic.main.fragment_verify_password.*
import libs.source.common.livedata.Status
import net.linkmate.app.R
import net.linkmate.app.ui.nas.safe_box.list.SafeBoxNasFileActivity
import net.linkmate.app.util.ToastUtils
import net.sdvn.nascommon.BaseFragment


class VerifyPasswordFragment : BaseFragment() {

    private val viewModel by viewModels<SafeBoxModel>({ requireActivity() })

    private lateinit var mArgs: VerifyPasswordFragmentArgs;


    override fun initView(view: View) {
        iv_close.setOnClickListener {
            if (mArgs.tpye == SafeBoxModel.LOGIN_TYPE) {
                requireActivity().finish()
            } else {
                findNavController().navigateUp()
            }
        }
        submit_btn.setOnClickListener {
            hide_edt.requestFocus()
            val data = odl_psw_it.textValue;
            if (TextUtils.isEmpty(data)) {
                ToastUtils.showToast(R.string.hint_enter_pwd)
                return@setOnClickListener
            }
            if (mArgs.tpye == SafeBoxModel.LOGIN_TYPE) {
                viewModel.unlockSafeBoxStatus(devId!!, data).observe(this, Observer {
                    if (it.status == Status.SUCCESS || it.code == ERROR_40208) {
                        goShowList()
                    } else {
                        ToastUtils.showToast(V5HttpErrorNo.getResourcesId(it.code ?: 1))
                    }
                })
            } else if (mArgs.tpye == SafeBoxModel.RESET_PASSWORD) {
                viewModel.checkOldPsw(devId!!, data).observe(this, Observer {
                    if (it.status == Status.SUCCESS && it.data != null) {
                        it.data?.ranStr?.let { str ->
                            goRestPsWord(str)
                        }
                    } else {
                        ToastUtils.showToast(V5HttpErrorNo.getResourcesId(it.code ?: 0))
                    }
                })
            }
        }

        forget_password_tv.setOnClickListener {
            findNavController().navigate(R.id.action_verify_to_verify_question, VerifyQuestionFragmentArgs(mArgs.deviceid, SafeBoxModel.RESET_PASSWORD).toBundle(), null, null)
        }
    }


    private fun goShowList() {
        context?.startActivity(Intent(context, SafeBoxNasFileActivity::class.java)
                .putExtra(io.weline.repo.files.constant.AppConstants.SP_FIELD_DEVICE_ID, devId) //是否是EN服务器
        )
        requireActivity().finish()
    }

    private fun goRestPsWord(ranStr: String) {
        findNavController().navigate(R.id.action_verify_to_set_password, SetPasswordFragmentArgs(devId!!, ranStr).toBundle())
    }


    override fun getLayoutResId(): Int {
        return R.layout.fragment_verify_password
    }

    override fun getTopView(): View? {
        return title_layout
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            mArgs = VerifyPasswordFragmentArgs.fromBundle(it)
        }
    }
}