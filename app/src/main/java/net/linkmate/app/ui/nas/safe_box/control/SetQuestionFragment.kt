package net.linkmate.app.ui.nas.safe_box.control

import android.os.Bundle
import android.text.TextUtils
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import io.weline.repo.api.ERROR_40212
import io.weline.repo.api.V5HttpErrorNo
import kotlinx.android.synthetic.main.fragment_set_question.*
import libs.source.common.livedata.Status
import net.linkmate.app.R
import net.linkmate.app.util.ToastUtils
import net.sdvn.nascommon.BaseFragment


class SetQuestionFragment : BaseFragment() {

    private val viewModel by viewModels<SafeBoxModel>({requireActivity()})
    private lateinit var mArgs: SetQuestionFragmentArgs;
    override fun initView(view: View) {
        iv_close.setOnClickListener {
           findNavController().navigateUp()
        }
        determine_btn.setOnClickListener {
            val questionStr = question_it.checkValue
            val answerStr = answer_it.checkValue
            when {
                TextUtils.isEmpty(questionStr) -> {
                    ToastUtils.showToast(R.string.security_question)
                }
                TextUtils.isEmpty(answerStr) -> {
                    ToastUtils.showToast(R.string.security_answer)
                }
                else -> {
                    viewModel.resetSafeBoxQuestion(devId!!, mArgs.trans, questionStr!!, answerStr!!).observe(this, Observer {
                        if (it.status == Status.SUCCESS) {
                            ToastUtils.showToast(R.string.modify_succeed)
                            requireActivity().finish()
                        }  else if (it.status == Status.ERROR) {
                            ToastUtils.showToast(V5HttpErrorNo.getResourcesId(it.code ?: 0))
                            if (it.code == ERROR_40212) {
                                findNavController().navigateUp()
                            }
                        }
                    })

                }
            }
        }
    }

    override fun getLayoutResId(): Int {
        return R.layout.fragment_set_question
    }

    override fun getTopView(): View? {
        return title_layout
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            mArgs = SetQuestionFragmentArgs.fromBundle(it)
        }
    }

}