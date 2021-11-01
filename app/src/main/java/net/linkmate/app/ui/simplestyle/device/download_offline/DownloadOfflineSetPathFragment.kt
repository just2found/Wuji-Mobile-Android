package net.linkmate.app.ui.simplestyle.device.download_offline

import android.os.Bundle
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import net.linkmate.app.R
import net.linkmate.app.ui.function_ui.choicefile.AbsChoiceFileFragment2
import net.linkmate.app.ui.function_ui.choicefile.NasFileInitData2
import net.linkmate.app.ui.function_ui.choicefile.base.NasFileConstant.CONTAIN_PUBLIC
import net.linkmate.app.ui.function_ui.choicefile.base.NasFileConstant.CONTAIN_USER
import net.linkmate.app.ui.function_ui.choicefile.base.OneOSFilterType
import net.sdvn.nascommon.model.oneos.OneOSFile


class DownloadOfflineSetPathFragment : AbsChoiceFileFragment2() {

    private lateinit var mArgs: DownloadOfflineSetPathFragmentArgs;
    private val viewModel by viewModels<DownloadOfflineModel>({ requireParentFragment() })


    override fun getNasFileInitData2(): NasFileInitData2 {
        return NasFileInitData2(
                mDeviceId = mArgs.deviceid!!,
                mFragmentTitle = getString(R.string.set_save_path),
                mMaxNum = 1,
                mOneOSFilterType = OneOSFilterType.DIR,
                mRootPathType = CONTAIN_USER or CONTAIN_PUBLIC
        )
    }

    override fun onLoadFileError(code: Int?) {}
    override fun onNext(sharePathType: Int, result: List<OneOSFile>) {}
    override fun onDismiss() {
        findNavController().navigateUp()
    }
    override fun onEnsureClick(sharePathType: Int, path: String): Boolean {
        viewModel.setSavePath(path, sharePathType, mArgs.deviceid!!)
        findNavController().navigateUp()
        return true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            mArgs = DownloadOfflineSetPathFragmentArgs.fromBundle(it)
        }
    }


}