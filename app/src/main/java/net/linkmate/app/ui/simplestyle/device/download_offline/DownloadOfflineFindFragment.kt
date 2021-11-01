package net.linkmate.app.ui.simplestyle.device.download_offline

import android.os.Bundle
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import io.weline.repo.api.V5HttpErrorNo
import libs.source.common.livedata.Status
import net.linkmate.app.R
import net.linkmate.app.ui.function_ui.choicefile.AbsChoiceFileFragment2
import net.linkmate.app.ui.function_ui.choicefile.NasFileInitData2
import net.linkmate.app.ui.function_ui.choicefile.base.NasFileConstant
import net.linkmate.app.ui.function_ui.choicefile.base.OneOSFilterType
import net.linkmate.app.ui.nas.helper.SelectTypeFragmentArgs
import net.linkmate.app.util.ToastUtils
import net.linkmate.app.view.ProgressDialog
import net.sdvn.nascommon.model.oneos.OneOSFile
import net.sdvn.nascommon.utils.DialogUtils


class DownloadOfflineFindFragment : AbsChoiceFileFragment2() {

    private lateinit var mArgs: DownloadOfflineSetPathFragmentArgs;
    private val viewModel by viewModels<DownloadOfflineModel>({ requireParentFragment() })

    private val mLoadingDialogFragment by lazy {
        ProgressDialog()
    }

    override fun getNasFileInitData2(): NasFileInitData2 {
        return NasFileInitData2(
                mDeviceId = mArgs.deviceid,
                mFragmentTitle = getString(R.string.add_seeds_manually),
                mMaxNum = 1,
                mShowConfirmTv = false,
                mAddFolderAble = false,
                mOneOSFilterType = OneOSFilterType.BT,
                mRootPathType = NasFileConstant.CONTAIN_USER or NasFileConstant.CONTAIN_PUBLIC
        )
    }

    override fun onLoadFileError(code: Int?) {}

    override fun onNext(sharePathType: Int, result: List<OneOSFile>) {
        if (result.isNotEmpty()) {
            showConfirmDialog(getString(R.string.add_task_confirm)) {
                addTask(result[0])
            }
        }
    }

    private fun showConfirmDialog(contentStr: String, Next: () -> Unit) {
        DialogUtils.showConfirmDialog(requireContext(), "", contentStr, getString(R.string.ok), getString(R.string.cancel),
                DialogUtils.OnDialogClickListener { dialog, isPositive ->
                    if (isPositive) {
                        Next.invoke()
                    }
                })
    }


    private fun addTask(item: OneOSFile) {
        mLoadingDialogFragment.show(childFragmentManager, ProgressDialog::javaClass.name)
        viewModel.addTask(mArgs.deviceid, item).observe(this, Observer { resource ->
            if (resource.status == Status.SUCCESS) {
                resource.data?.let { code ->
                    if (code == 0)//添加任务成功了
                    {
                        ToastUtils.showToast(getString(R.string.bind_success))
                        findNavController().navigate(R.id.action_find_to_index, SelectTypeFragmentArgs(devId!!).toBundle(), null, null)
                    } else//添加任务失败了
                    {
                        ToastUtils.showToast(V5HttpErrorNo.getResourcesId(code))
                    }
                }
            } else {
                ToastUtils.showToast(V5HttpErrorNo.getResourcesId(resource.code))
            }
            mLoadingDialogFragment.dismiss()
        })

    }

    override fun onDismiss() {
        findNavController().navigateUp()
    }

    override fun onEnsureClick(sharePathType: Int, path: String): Boolean {
        return false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            mArgs = DownloadOfflineSetPathFragmentArgs.fromBundle(it)
        }
    }
}