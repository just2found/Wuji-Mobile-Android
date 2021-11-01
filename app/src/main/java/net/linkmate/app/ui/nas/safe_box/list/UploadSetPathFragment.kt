package net.linkmate.app.ui.nas.safe_box.list

import android.os.Bundle
import android.text.TextUtils
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import io.weline.repo.api.ERROR_40207
import io.weline.repo.api.V5HttpErrorNo
import io.weline.repo.api.V5_ERR_SESSION_EXP
import io.weline.repo.files.data.SharePathType
import kotlinx.android.synthetic.main.fragment_nas_upload.*
import net.linkmate.app.R
import net.linkmate.app.ui.function_ui.choicefile.*
import net.linkmate.app.ui.function_ui.choicefile.base.OneOSFilterType
import net.linkmate.app.util.ToastUtils
import net.sdvn.nascommon.BaseActivity
import net.sdvn.nascommon.model.FileListChangeObserver
import net.sdvn.nascommon.model.FileManageAction
import net.sdvn.nascommon.model.oneos.DataFile
import net.sdvn.nascommon.model.oneos.OneOSFile
import net.sdvn.nascommon.model.phone.LocalFile
import net.sdvn.nascommon.model.phone.LocalFileManage
import net.sdvn.nascommon.model.phone.LocalFileType
import kotlin.properties.Delegates

class UploadSetPathFragment : AbsChoiceFileFragment2() {


    private val mSafeBoxFileListModel by viewModels<SafeBoxNasFileModel>({ requireParentFragment() })
    private lateinit var mArgs: UploadSetPathFragmentArgs;

    override fun getNasFileInitData2(): NasFileInitData2 {
        return NasFileInitData2(
                mDeviceId = mArgs.deviceid,
                mFragmentTitle = getString(R.string.set_save_path),
                mInitPathType = SharePathType.SAFE_BOX.type,
                mInitPath = "/",
                mMaxNum = 1,
                mOneOSFilterType = OneOSFilterType.DIR
        )
    }


    override fun onLoadFileError(code: Int?) {
        if (code == ERROR_40207 || code == V5_ERR_SESSION_EXP) {
            mSafeBoxFileListModel.goToLogin(requireActivity(), devId!!)
        }
    }

    override fun onNext(sharePathType: Int, result: List<OneOSFile>) {}

    override fun onEnsureClick(sharePathType: Int, path: String): Boolean {
        uploadFile(path)
        return true
    }

    override fun onDismiss() {
        findNavController().navigateUp()
    }


    private fun uploadFile(path: String) {
        val fileManage = LocalFileManage(requireActivity(), layout_title,
                object : LocalFileManage.OnManageCallback {
                    override fun onComplete(isSuccess: Boolean) {
                        FileListChangeObserver.getInstance().FileListChange()
                        if (activity is BaseActivity) {
                            (activity as? BaseActivity)?.dismissLoading()
                        }
                        findNavController().navigate(R.id.action_upload_to_index, SafeBoxQuickCloudNavFragmentArgs(mArgs.deviceid).toBundle())
                    }

                    override fun onStart(resStrId: Int) {
                        if (activity is BaseActivity) {
                            (activity as? BaseActivity)?.showLoading(resStrId)
                        }
                    }
                })
        fileManage.setUploadPath(mArgs.deviceid,  if(TextUtils.isEmpty(path)) "safe/" else "safe$path")
        val selected: MutableList<LocalFile> = mutableListOf()
        if (mSafeBoxFileListModel.paths.isNullOrEmpty()) {
            findNavController().navigateUp()
        }
        for (dataFile in mSafeBoxFileListModel.paths!!) {
            if (dataFile is LocalFile) {
                selected.add(dataFile)
            }
        }
        fileManage.manage(LocalFileType.PRIVATE, FileManageAction.UPLOAD, selected)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            mArgs = UploadSetPathFragmentArgs.fromBundle(it)
        }
    }
}