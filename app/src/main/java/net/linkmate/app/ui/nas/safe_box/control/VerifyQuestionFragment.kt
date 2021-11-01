package net.linkmate.app.ui.nas.safe_box.control

import android.os.Bundle
import android.text.TextUtils
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import io.weline.repo.api.V5HttpErrorNo
import kotlinx.android.synthetic.main.fragment_verify_password.*
import kotlinx.android.synthetic.main.fragment_verify_password.title_layout
import kotlinx.android.synthetic.main.fragment_verify_quesition.*
import kotlinx.android.synthetic.main.fragment_verify_quesition.forget_password_tv
import kotlinx.android.synthetic.main.fragment_verify_quesition.iv_close
import kotlinx.android.synthetic.main.fragment_verify_quesition.title_tv
import libs.source.common.livedata.Status
import net.linkmate.app.R
import net.linkmate.app.ui.nas.safe_box.control.SafeBoxModel.Companion.RESET_PASSWORD
import net.linkmate.app.ui.nas.safe_box.control.SafeBoxModel.Companion.RESET_QUESTION
import net.linkmate.app.util.ToastUtils
import net.sdvn.nascommon.BaseFragment


class VerifyQuestionFragment : BaseFragment() {

    private val viewModel by viewModels<SafeBoxModel>({ requireActivity() })
    private lateinit var mArgs: VerifyQuestionFragmentArgs;

    override fun getLayoutResId(): Int {
        return R.layout.fragment_verify_quesition
    }

    override fun getTopView(): View? {
        return title_layout
    }

    override fun initView(view: View) {
        if (mArgs.tpye == RESET_PASSWORD) {
            title_tv.text = getString(R.string.find_the_password)
        } else if (mArgs.tpye == RESET_QUESTION) {
            title_tv.text = getString(R.string.modify_secret_guard)
        }

        iv_close.setOnClickListener { findNavController().navigateUp() }

        viewModel.querySafeBoxStatus(mArgs.deviceid).observe(this, Observer {
            it.data?.question?.let { questionStr ->
                question_tv.text = questionStr
            }
        })


        //验证老的密保答案
        next_btn.setOnClickListener {
            val oldAnswer: String? = old_answer_it.checkValue
            if (TextUtils.isEmpty(oldAnswer)) {
                ToastUtils.showToast(R.string.security_answer)
            } else {
                viewModel.checkOldAnswer(mArgs.deviceid, oldAnswer!!).observe(this, Observer {
                    if (it.status == Status.SUCCESS && it.data != null) {
                        it.data?.ranStr?.let { str ->
                            next(str)
                        }
                    } else {
                        ToastUtils.showToast(V5HttpErrorNo.getResourcesId(it.code ?: 0))
                    }
                })

            }
        }

        forget_password_tv.setOnClickListener {
            findNavController().navigate(R.id.action_question_to_all, SafeBoxResetAllFragmentArgs(mArgs.deviceid).toBundle(), null, null)
        }
    }

    private fun next(ranStr: String) {
        if (mArgs.tpye == RESET_PASSWORD) {
            findNavController().navigate(R.id.action_question_to_set_password, SetPasswordFragmentArgs(devId!!, ranStr).toBundle())
        } else if (mArgs.tpye == RESET_QUESTION) {
            findNavController().navigate(R.id.action_question_to_set_question, SetQuestionFragmentArgs(devId!!, ranStr).toBundle())
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            mArgs = VerifyQuestionFragmentArgs.fromBundle(it)
        }
    }
}