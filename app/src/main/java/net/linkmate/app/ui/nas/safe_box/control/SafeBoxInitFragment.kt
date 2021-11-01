package net.linkmate.app.ui.nas.safe_box.control

import android.content.Intent
import android.text.TextUtils
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import io.weline.repo.api.NetworkResponseConstant
import io.weline.repo.api.V5HttpErrorNo
import kotlinx.android.synthetic.main.fragment_safe_box_init.*
import libs.source.common.livedata.Status
import net.linkmate.app.R
import net.linkmate.app.ui.nas.safe_box.list.SafeBoxNasFileActivity
import net.linkmate.app.util.ToastUtils
import net.sdvn.nascommon.BaseFragment

/**
 * A fragment representing a list of Items.
 */
class SafeBoxInitFragment : BaseFragment() {

    private val viewModel by viewModels<SafeBoxModel>({
        requireActivity()
    })

    private var pwdStr: String? = null
    private var rePwdStr: String? = null
    private var questionStr: String? = null
    private var answerStr: String? = null

    override fun getLayoutResId(): Int {
        return R.layout.fragment_safe_box_init
    }

    override fun getTopView(): View? {
        return title_layout
    }

    override fun initView(view: View) {
        iv_close.setOnClickListener {
            this.requireActivity().finish()
        }
        pwd_it.liveData.observe(this, Observer {
            if (it.isPassChecked) {
                re_pwd_it.setSameStr(it.valueStr)
            } else {
                re_pwd_it.setSameStr("")
            }
        })

        next_btn.setOnClickListener {
            pwdStr = pwd_it.checkValue
            if (TextUtils.isEmpty(pwdStr)) {
                ToastUtils.showToast(R.string.password_description_20)
                return@setOnClickListener
            }
            re_pwd_it.setSameStr(pwdStr)
            rePwdStr = re_pwd_it.checkValue
            if (!pwdStr.equals(rePwdStr)) {
                ToastUtils.showToast(R.string.new_pwd_are_different)
            } else {
                pwd_it.visibility = View.GONE
                re_pwd_it.visibility = View.GONE
                next_btn.visibility = View.GONE

                question_it.visibility = View.VISIBLE
                answer_it.visibility = View.VISIBLE
                determine_btn.visibility = View.VISIBLE

                describe_tv.text = getString(R.string.safe_box_answer)
            }
        }
        determine_btn.setOnClickListener {
            questionStr = question_it.checkValue
            answerStr = answer_it.checkValue

            when {
                TextUtils.isEmpty(questionStr) -> {
                    ToastUtils.showToast(R.string.safe_box_answer)
                }
                TextUtils.isEmpty(answerStr) -> {
                    ToastUtils.showToast(R.string.safe_box_answer)
                }
                else -> {
                    viewModel.initSafeBoxStatus(devId!!, questionStr!!, answerStr!!, pwdStr!!)
                        .observe(this, Observer {
                            if (it.status == Status.SUCCESS) {
                                ToastUtils.showToast(R.string.register_succ)
                                check()

                            } else {
                                ToastUtils.showToast(it.code?.let { it1 ->
                                    V5HttpErrorNo.getResourcesId(
                                        it1
                                    )
                                })
                            }
                        })
                }
            }
        }
    }

    fun check() {
        viewModel.querySafeBoxStatus(devId!!).observe(this, Observer {
            if (it.status == Status.SUCCESS) {
                if (it.data?.question.isNullOrEmpty()) {
                    context?.startActivity(
                        Intent(context, SafeBoxControlActivity::class.java)
                            .putExtra(
                                io.weline.repo.files.constant.AppConstants.SP_FIELD_DEVICE_ID,
                                devId
                            )
                            .putExtra(SafeBoxModel.SAFE_BOX_TYPE_KEY, SafeBoxModel.INITIALIZATION)
                    )
                } else if (it.data?.lock == NetworkResponseConstant.SAFE_LOCK_STATUS) {
                    context?.startActivity(
                        Intent(context, SafeBoxControlActivity::class.java)
                            .putExtra(
                                io.weline.repo.files.constant.AppConstants.SP_FIELD_DEVICE_ID,
                                devId
                            )
                            .putExtra(SafeBoxModel.SAFE_BOX_TYPE_KEY, SafeBoxModel.LOGIN_TYPE)
                    )
                } else {
                    context?.startActivity(
                        Intent(context, SafeBoxNasFileActivity::class.java)
                            .putExtra(
                                io.weline.repo.files.constant.AppConstants.SP_FIELD_DEVICE_ID,
                                devId
                            ) //是否是EN服务器
                    )
                }
            } else if (it.status == Status.ERROR) {
                ToastUtils.showToast(V5HttpErrorNo.getResourcesId(it.code ?: 0))
            }
            requireActivity().finish()
        })

    }


}