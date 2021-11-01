package net.linkmate.app.ui.nas.upload

import android.app.Dialog
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import io.weline.repo.api.V5_ERR_DENIED_PERMISSION
import kotlinx.android.synthetic.main.fragment_choice_nas.*
import libs.source.common.livedata.Status
import net.linkmate.app.R
import net.linkmate.app.ui.function_ui.choicefile.AbsChoiceFileFragment2
import net.linkmate.app.ui.function_ui.choicefile.NasFileInitData2
import net.linkmate.app.ui.function_ui.choicefile.base.OneOSFilterType
import net.sdvn.nascommon.BaseActivity
import net.sdvn.nascommon.model.oneos.OneOSFile
import net.sdvn.nascommon.utils.DialogUtils

class UploadSelectPathFragment : AbsChoiceFileFragment2() {
    private val navAargs by navArgs<UploadSelectPathFragmentArgs>()
    private val localFileViewModel by viewModels<LocalFileViewModel>({ requireParentFragment() })

    private fun uploadFile(view: View, path: String, sharePathType: Int) {
        localFileViewModel.uploadFile(requireActivity(), view, navAargs.deviceid, path, sharePathType)
                .observe(this, Observer {
                    val requireActivity = requireActivity()
                    if (requireActivity is BaseActivity) {
                        when (it.status) {
                            Status.LOADING -> {
                                it.data?.let { it1 -> requireActivity.showLoading(it1) }
                                        ?: requireActivity.showLoading()
                            }
                            else -> {
                                requireActivity.dismissLoading()
                                if (it.status == Status.SUCCESS) {
                                    requireActivity.finish()
                                }
                            }
                        }
                    }
                })
    }

    override fun getNasFileInitData2(): NasFileInitData2 {
        return NasFileInitData2(
                mDeviceId = navAargs.deviceid,
                mFragmentTitle = getString(R.string.set_save_path),
                mMaxNum = 1,
                mOneOSFilterType = OneOSFilterType.DIR,
                mRootPathType = navAargs.rootPathType,
                mOptionalFolderAble = true
        )
    }

    override fun onEnsureClick(sharePathType: Int, path: String): Boolean {
        if (sharePathType >= 0 && path.isNotEmpty()) {

            uploadFile(title_bar, path, sharePathType)
            return true
        }
        return false

    }

    private var notifyDialog: Dialog? = null
    override fun onLoadFileError(code: Int?) {
        if (code == V5_ERR_DENIED_PERMISSION) {
            lifecycleScope.launchWhenResumed {
                if (notifyDialog == null || notifyDialog?.isShowing == false) {
                    notifyDialog = DialogUtils.showNotifyDialog(requireContext(), 0,
                            R.string.ec_no_permission, R.string.confirm) { _, isPositiveBtn ->
                        if (isPositiveBtn) {
                            showAllTypes()
                        }
                    }
                }
            }
        }
    }

    override fun onNext(sharePathType: Int, result: List<OneOSFile>) {

    }

    override fun onNewPathEnable(isNotEnable: Boolean) {
        super.onNewPathEnable(isNotEnable)
        setConfirmTvGone(isNotEnable)
    }

    override fun onDismiss() {
        findNavController().popBackStack()
    }

}