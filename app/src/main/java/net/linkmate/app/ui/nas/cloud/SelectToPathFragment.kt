package net.linkmate.app.ui.nas.cloud

import android.app.Dialog
import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.setFragmentResult
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import io.weline.repo.api.V5_ERR_DENIED_PERMISSION
import kotlinx.android.synthetic.main.fragment_choice_nas.*
import net.linkmate.app.R
import net.linkmate.app.ui.function_ui.choicefile.AbsChoiceFileFragment2
import net.linkmate.app.ui.function_ui.choicefile.NasFileInitData2
import net.linkmate.app.ui.function_ui.choicefile.base.OneOSFilterType
import net.sdvn.nascommon.constant.AppConstants
import net.sdvn.nascommon.model.oneos.OneOSFile
import net.sdvn.nascommon.utils.DialogUtils


class SelectToPathFragment : AbsChoiceFileFragment2() {
    private val navArgs by navArgs<SelectToPathFragmentArgs>()

    private fun onSelectPath(view: View, path: String, sharePathType: Int) {
        setFragmentResult(navArgs.requestKey, Bundle().apply {
            putString("deviceid", navArgs.deviceid)
            putString("path", path)
            putInt("sharePathType", sharePathType)
        })
        findNavController().popBackStack()
    }

    override fun getNasFileInitData2(): NasFileInitData2 {
        return NasFileInitData2(
            mDeviceId = navArgs.deviceid,
            mFragmentTitle = getString(R.string.set_save_path),
            mMaxNum = 1,
            mOneOSFilterType = OneOSFilterType.DIR,
            mRootPathType = navArgs.rootPathType,
            mOptionalFolderAble = true,
            mAddFolderAble =  false
        )
    }

    override fun onEnsureClick(sharePathType: Int, path: String): Boolean {
        if (sharePathType >= 0 && path.isNotEmpty()) {

            onSelectPath(title_bar, path, sharePathType)
            return true
        }
        return false

    }

    private var notifyDialog: Dialog? = null
    override fun onLoadFileError(code: Int?) {
        if (code == V5_ERR_DENIED_PERMISSION) {
            lifecycleScope.launchWhenResumed {
                if (notifyDialog == null || notifyDialog?.isShowing == false) {
                    notifyDialog = DialogUtils.showNotifyDialog(
                        requireContext(), 0,
                        R.string.ec_no_permission, R.string.confirm
                    ) { _, isPositiveBtn ->
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
        add_folder_btn.isVisible = !isNotEnable
    }

    override fun onDismiss() {
        setFragmentResult(navArgs.requestKey, Bundle().apply {
            putString("path", "")
        })
        findNavController().popBackStack()
    }

}