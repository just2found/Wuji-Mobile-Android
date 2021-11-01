package net.linkmate.app.ui.nas.group.list

import android.os.Bundle
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import io.weline.repo.api.ERROR_40207
import io.weline.repo.api.V5HttpErrorNo
import io.weline.repo.api.V5_ERR_SESSION_EXP
import io.weline.repo.files.data.SharePathType
import libs.source.common.livedata.Status
import net.linkmate.app.R
import net.linkmate.app.ui.function_ui.choicefile.AbsChoiceFileFragment2
import net.linkmate.app.ui.function_ui.choicefile.NasFileInitData2
import net.linkmate.app.ui.function_ui.choicefile.base.OneOSFilterType
import net.linkmate.app.util.ToastUtils
import net.sdvn.nascommon.model.oneos.OneOSFile

class GroupSpaceSetPathFragment : AbsChoiceFileFragment2() {

    private lateinit var mArgs: GroupSpaceSetPathFragmentArgs;

    private val mSafeBoxFileListModel by viewModels<GroupSpaceNasFileModel>({ requireParentFragment() })


    override fun getNasFileInitData2(): NasFileInitData2 {
        return NasFileInitData2(
                mDeviceId = mArgs.deviceid,
                mFragmentTitle = getString(R.string.set_save_path),
                mInitPathType = SharePathType.GROUP.type,
                mInitPath = "/",
                mMaxNum = 1,
                mOneOSFilterType = OneOSFilterType.DIR,
                mGroupId=mArgs.groupid
        )
    }


    override fun onLoadFileError(code: Int?) {
//        if (code == ERROR_40207 || code == V5_ERR_SESSION_EXP) {
//            mSafeBoxFileListModel.goToLogin(requireActivity(), devId!!)
//        }
    }

    override fun onNext(sharePathType: Int, result: List<OneOSFile>) {

    }

    override fun onEnsureClick(sharePathType: Int, path: String): Boolean {
        mSafeBoxFileListModel.targetFilePath = path
        val list = mutableListOf<String>()
        list.addAll(mArgs.paths)
        mSafeBoxFileListModel.moveInGroupSpace(mArgs.deviceid, path, mArgs.shareType, list,mArgs.groupid).observe(this, Observer {
            if (it.status == Status.SUCCESS) {
                ToastUtils.showToast(R.string.move_file_success)
                findNavController().navigate(R.id.action_set_to_index, GroupSpaceQuickCloudNavFragmentArgs(mArgs.deviceid,mArgs.groupid,SharePathType.GROUP.type,"/").toBundle())
            } else {
//                if (it.code == ERROR_40207 || it.code == V5_ERR_SESSION_EXP) {
//                    mSafeBoxFileListModel.goToLogin(requireActivity(), devId!!)
//                }
                ToastUtils.showToast(V5HttpErrorNo.getResourcesId(it.code ?: 0))
            }
        })
        return true
    }


    override fun onDismiss() {
        findNavController().navigateUp()
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            mArgs = GroupSpaceSetPathFragmentArgs.fromBundle(it)
        }
    }
}