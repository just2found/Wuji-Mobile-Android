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
import net.linkmate.app.ui.function_ui.choicefile.AbsChoiceFileFragment1
import net.linkmate.app.ui.function_ui.choicefile.NasFileInitData1
import net.linkmate.app.ui.function_ui.choicefile.base.NasFileConstant.CONTAIN_PUBLIC
import net.linkmate.app.ui.function_ui.choicefile.base.NasFileConstant.CONTAIN_USER
import net.linkmate.app.util.ToastUtils
import net.sdvn.nascommon.model.oneos.OneOSFile

class GroupSpaceMoveInFragment : AbsChoiceFileFragment1() {

    private lateinit var mArgs: GroupSpaceMoveInFragmentArgs;

    private val mSafeBoxFileListModel by viewModels<GroupSpaceNasFileModel>({ requireParentFragment() })

    override fun getNasFileInitData1(): NasFileInitData1 {
        return NasFileInitData1(
            mArgs.deviceid,
            SharePathType.GROUP.type,
            mArgs.path,
            getString(R.string.hint_select_file),
            mGroupId = mArgs.groupid,
            mRootPathType=CONTAIN_USER or CONTAIN_PUBLIC
        )
    }

    override fun onNext(sharePathType: Int, result: List<OneOSFile>) {
        if (result.isNullOrEmpty()) {
            ToastUtils.showToast(R.string.tip_select_file)
            return
        }
        val list = mutableListOf<String>()
        result.forEach {
            list.add(it.getPath())
        }
        val liveData = mSafeBoxFileListModel.moveInGroupSpace(mArgs.deviceid, mArgs.path, sharePathType, list,mArgs.groupid)
        liveData.observe(this, Observer {
            if (it.status == Status.SUCCESS) {
                ToastUtils.showToast(R.string.move_file_success)
                findNavController().navigateUp()
            } else {
//                if (it.code == ERROR_40207 || it.code == V5_ERR_SESSION_EXP) {
//                    mSafeBoxFileListModel.goToLogin(requireActivity(), devId!!)
//                }
                ToastUtils.showToast(V5HttpErrorNo.getResourcesId(it.code ?: 0))
            }
        })
    }

    override fun onChangeToPath() {
        if (mChoiceNasFileAdapter.mSelectList.isNullOrEmpty()) {
            ToastUtils.showToast(R.string.tip_select_file)
            return
        }
        val list = Array(mChoiceNasFileAdapter.mSelectList.size) { it -> "" }
        for ((index, oneOSFile) in mChoiceNasFileAdapter.mSelectList.withIndex()) {
            list[index] = oneOSFile.getPath()
        }
        mChoiceNasFileAdapter.selectAll(false)
        findNavController().navigate(
            R.id.action_in_to_set_path,
            GroupSpaceSetPathFragmentArgs(mArgs.deviceid, mNowType, list, mArgs.groupid).toBundle()
        )
    }


    override fun onDismiss() {
        findNavController().navigateUp()
    }

    override fun onLoadFileError(code: Int?) {

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            mArgs = GroupSpaceMoveInFragmentArgs.fromBundle(it)
        }
    }
}