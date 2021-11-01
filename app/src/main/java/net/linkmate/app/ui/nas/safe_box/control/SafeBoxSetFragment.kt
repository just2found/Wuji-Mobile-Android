package net.linkmate.app.ui.nas.safe_box.control

import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import io.weline.repo.api.ERROR_40207
import io.weline.repo.api.V5HttpErrorNo
import io.weline.repo.api.V5_ERR_SESSION_EXP
import kotlinx.android.synthetic.main.fragment_safe_box_set.*
import libs.source.common.livedata.Status
import net.linkmate.app.R
import net.linkmate.app.ui.nas.safe_box.control.SafeBoxModel.Companion.CLOSE_ACTIVITY
import net.linkmate.app.ui.nas.safe_box.control.SafeBoxModel.Companion.RESET_PASSWORD
import net.linkmate.app.ui.nas.safe_box.control.SafeBoxModel.Companion.RESET_QUESTION
import net.linkmate.app.util.ToastUtils
import net.sdvn.nascommon.BaseFragment
import net.sdvn.nascommon.db.SafeBoxTransferHistoryKeeper
import net.sdvn.nascommon.model.oneos.tansfer_safebox.SafeBoxDownloadManager
import net.sdvn.nascommon.model.oneos.tansfer_safebox.SafeBoxUploadManager
import net.sdvn.nascommon.model.oneos.transfer.TransferState
import net.sdvn.nascommon.model.oneos.transfer_r.thread.TransferThreadExecutor


/**
 * A simple [Fragment] subclass.
 * Use the [SafeBoxSetFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class SafeBoxSetFragment : BaseFragment() {

    private val viewModel by viewModels<SafeBoxModel>({ requireActivity() })


    override fun getTopView(): View? {
        return titleBar
    }

    override fun initView(view: View) {
        titleBar.setBackListener {
            requireActivity().finish()
        }
        modify_pwd_tv.setOnClickListener {
            findNavController().navigate(R.id.action_set_to_reset_psw, VerifyPasswordFragmentArgs(devId!!, RESET_PASSWORD).toBundle(), null, null)
        }
        modify_secret_tv.setOnClickListener {
            findNavController().navigate(R.id.action_set_to_reset_question, VerifyQuestionFragmentArgs(devId!!, RESET_QUESTION).toBundle(), null, null)
        }
        reset_secret_all_tv.setOnClickListener {
            findNavController().navigate(R.id.action_set_to_All, SafeBoxResetAllFragmentArgs(devId!!).toBundle(), null, null)
        }

        logout_secret_tv.setOnClickListener {
            val title = getString(R.string.login_out_safe_box)
            val description = getString(R.string.login_out_safe_box_ds)
            val inputStr = null
            FormatEnsureDialog(title, description, inputStr) {
                devId?.let { it1 ->
                    viewModel.lockSafeBoxStatus(it1).observe(this, Observer {
                        if (it.status == Status.SUCCESS) {
                            loginOut(it1)
                        } else if (it.status == Status.ERROR) {
                            if (it.code == ERROR_40207|| it.code == V5_ERR_SESSION_EXP) {
                                loginOut(it1)
                            } else {
                                ToastUtils.showToast(V5HttpErrorNo.getResourcesId(it.code))
                            }
                        }
                    })

                }
            }.show(childFragmentManager, FormatEnsureDialog::class.java.simpleName)
        }
    }


    fun loginOut(devId: String) {
        diskIOExecutor().execute {
            SafeBoxTransferHistoryKeeper.updateStateByDevId(devId, TransferState.PAUSE, false)
            SafeBoxTransferHistoryKeeper.updateStateByDevId(devId, TransferState.PAUSE, true)
        }
        SafeBoxUploadManager.pause()
        SafeBoxDownloadManager.pause()
        requireActivity().setResult(CLOSE_ACTIVITY)
        requireActivity().finish()
    }

    private fun diskIOExecutor() = TransferThreadExecutor.diskIOExecutor

    override fun getLayoutResId(): Int {
        return R.layout.fragment_safe_box_set
    }


}