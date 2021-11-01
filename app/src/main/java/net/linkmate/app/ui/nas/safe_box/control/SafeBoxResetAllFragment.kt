package net.linkmate.app.ui.nas.safe_box.control

import android.text.TextUtils
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import io.reactivex.disposables.Disposable
import io.weline.repo.api.V5HttpErrorNo
import kotlinx.android.synthetic.main.fragment_safe_box_reset_all.*
import libs.source.common.livedata.Status
import net.linkmate.app.R
import net.linkmate.app.base.MyOkHttpListener
import net.linkmate.app.ui.simplestyle.device.download_offline.DownloadOfflineIndexFragment
import net.linkmate.app.util.ToastUtils
import net.linkmate.app.view.ProgressDialog
import net.sdvn.common.data.remote.UserRemoteDataSource
import net.sdvn.common.internet.core.GsonBaseProtocol
import net.sdvn.common.internet.core.HttpLoader
import net.sdvn.nascommon.BaseFragment
import net.sdvn.nascommon.db.SafeBoxTransferHistoryKeeper
import net.sdvn.nascommon.model.oneos.transfer.TransferState

/**
 * 重置保险箱
 */
class SafeBoxResetAllFragment : BaseFragment(), HttpLoader.HttpLoaderStateListener {

    private val viewModel by viewModels<SafeBoxModel>({
        requireActivity()
    })


    val loader by lazy { UserRemoteDataSource() }
    private val mLoadingDialogFragment by lazy {
        ProgressDialog()
    }

    override fun initView(view: View) {
        iv_close.setOnClickListener { findNavController().navigateUp() }
        determine_btn.setOnClickListener {
            val pswStr = psw_it.checkValue
            if (TextUtils.isEmpty(pswStr)) {
                ToastUtils.showToast(R.string.input_weline_psw)
                return@setOnClickListener
            }
            loader.passwordConfirm(pswStr!!, object : MyOkHttpListener<GsonBaseProtocol>() {
                override fun success(tag: Any?, data: GsonBaseProtocol) {
                    resetAll()
                }

                override fun error(tag: Any?, baseProtocol: GsonBaseProtocol?) {
                    ToastUtils.showToast(V5HttpErrorNo.getResourcesId(baseProtocol?.result));
                    //  super.error(tag, baseProtocol)
                }

            }).setHttpLoaderStateListener(this)
        }
    }


    override fun getLayoutResId(): Int {
        return R.layout.fragment_safe_box_reset_all
    }

    override fun getTopView(): View? {
        return title_layout
    }

    fun resetAll() {
        val title = getString(R.string.reset)
        val description = getString(R.string.reset_safe_box_info)
        val inputStr = getString(R.string.confirm_reset)
        FormatEnsureDialog(title, description, inputStr) {
            devId?.let { it1 ->
                viewModel.resetSafeBoxAll(devId!!).observe(this, Observer {
                    if (it.status == Status.SUCCESS) {
                        ToastUtils.showToast(R.string.reset_succeed)
                        SafeBoxTransferHistoryKeeper.clear(it1)
                        requireActivity().setResult(SafeBoxModel.CLOSE_ACTIVITY)
                        requireActivity().finish()
                    } else {
                        ToastUtils.showToast(it.code?.let { it1 -> V5HttpErrorNo.getResourcesId(it1) })
                    }
                })

            }
        }.show(childFragmentManager, FormatEnsureDialog::class.java.simpleName)
    }

    override fun onLoadStart(disposable: Disposable) {
        if (mLoadingDialogFragment.dialog?.isShowing != true) {
            mLoadingDialogFragment.show(childFragmentManager, DownloadOfflineIndexFragment::javaClass.name)
        }
    }

    override fun onLoadComplete() {
        if (mLoadingDialogFragment.dialog?.isShowing == true) {
            mLoadingDialogFragment.dismiss()
        }
    }

    override fun onLoadError() {
        if (mLoadingDialogFragment.dialog?.isShowing == true) {
            mLoadingDialogFragment.dismiss()
        }
        ToastUtils.showToast(R.string.tip_password_error)
    }


}