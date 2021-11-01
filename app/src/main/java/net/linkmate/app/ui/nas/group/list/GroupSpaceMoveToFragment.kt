package net.linkmate.app.ui.nas.group.list

import android.os.Bundle
import androidx.fragment.app.viewModels
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import io.weline.repo.api.ERROR_40207
import io.weline.repo.api.V5HttpErrorNo
import io.weline.repo.api.V5_ERR_SESSION_EXP
import libs.source.common.livedata.Resource
import libs.source.common.livedata.Status
import net.linkmate.app.R
import net.linkmate.app.ui.function_ui.choicefile.AbsChoiceFileFragment2
import net.linkmate.app.ui.function_ui.choicefile.NasFileInitData2
import net.linkmate.app.ui.function_ui.choicefile.base.NasFileConstant
import net.linkmate.app.ui.function_ui.choicefile.base.OneOSFilterType
import net.linkmate.app.util.ToastUtils
import net.sdvn.nascommon.fileserver.constants.SharePathType
import net.sdvn.nascommon.model.oneos.OneOSFile

class GroupSpaceMoveToFragment : AbsChoiceFileFragment2() {

    private lateinit var mArgs: GroupSpaceMoveToFragmentArgs;

  //  private lateinit var pathList: ArrayList<String> //这个我不知道怎么通过SafeBoxMoveToFragmentArgs传递 只能单独写出来

    companion object {
        const val PATH_LIST_KEY = "path_List"
        const val MOVE = "move"
        const val COPY = "copy"
    }

    private val mSafeBoxFileListModel by viewModels<GroupSpaceNasFileModel>({ requireParentFragment() })


    override fun getNasFileInitData2(): NasFileInitData2 {
        return NasFileInitData2(
            mDeviceId = mArgs.deviceid,
            mFragmentTitle = getString(R.string.set_save_path),
            mOneOSFilterType = OneOSFilterType.DIR,
            mRootPathType= NasFileConstant.CONTAIN_USER or NasFileConstant.CONTAIN_PUBLIC,
            mGroupId = mArgs.groupid
        )
    }


    override fun onLoadFileError(code: Int?) {
//        if (code == ERROR_40207 || code == V5_ERR_SESSION_EXP) {
//            mSafeBoxFileListModel.goToLogin(requireActivity(), devId!!)
//        }
    }

    override fun onNext(sharePathType: Int, result: List<OneOSFile>) {}

    lateinit var liveData: LiveData<Resource<Any>>



    override fun onEnsureClick(sharePathType: Int, path: String): Boolean {
        val list = mutableListOf<String>()
        list.addAll(mArgs.paths)
        if (mArgs.type == MOVE) {
            liveData =
                mSafeBoxFileListModel.moveGroupSpaceTo(mArgs.deviceid, path, sharePathType,mArgs.groupid,list )
            liveData.observe(this, Observer {
                if (it.status == Status.SUCCESS) {
                    ToastUtils.showToast(R.string.move_file_success)
                    findNavController().navigateUp()
                } else {
//                    if (it.code == ERROR_40207 || it.code == V5_ERR_SESSION_EXP) {
//                        mSafeBoxFileListModel.goToLogin(requireActivity(), devId!!)
//                    }
                    ToastUtils.showToast(V5HttpErrorNo.getResourcesId(it.code ?: 0))
                }
            })
        } else if (mArgs.type == COPY) {
            liveData =
                mSafeBoxFileListModel.copyGroupSpaceTo(mArgs.deviceid, path, sharePathType, mArgs.groupid,list)
            liveData.observe(this, Observer {
                if (it.status == Status.SUCCESS) {
                    ToastUtils.showToast(R.string.copy_file_success)
                    findNavController().navigateUp()
                } else {
//                    if (it.code == ERROR_40207 || it.code == V5_ERR_SESSION_EXP) {
//                        mSafeBoxFileListModel.goToLogin(requireActivity(), devId!!)
//                    }
                    ToastUtils.showToast(V5HttpErrorNo.getResourcesId(it.code ?: 0))
                }
            })
        }
        return true
    }


    override fun onDismiss() {
        findNavController().navigateUp()
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            mArgs = GroupSpaceMoveToFragmentArgs.fromBundle(it)
//            pathList = it.getStringArrayList(PATH_LIST_KEY)
//            if (pathList.isNullOrEmpty()) {
//                ToastUtils.showToast(R.string.hint_select_file)
//                findNavController().navigateUp()
//            }
        }
    }


}