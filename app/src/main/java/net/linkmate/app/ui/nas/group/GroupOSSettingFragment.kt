package net.linkmate.app.ui.nas.group

import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import android.widget.EditText
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import kotlinx.android.synthetic.main.fragment_group_o_s_setting.*
import kotlinx.android.synthetic.main.fragment_group_o_s_setting.titleBar
import libs.source.common.livedata.Status
import net.linkmate.app.R
import net.linkmate.app.ui.nas.NavTipsBackPressedFragment
import net.linkmate.app.ui.nas.group.list.GroupSpaceNasFileActivity
import net.linkmate.app.util.ToastUtils
import net.sdvn.nascommon.db.GroupsKeeper
import net.sdvn.nascommon.fileserver.constants.SharePathType
import net.sdvn.nascommon.utils.DialogUtils
import net.sdvn.nascommon.utils.ToastHelper
import org.view.libwidget.singleClick


/**
 * A simple [Fragment] subclass.
 * Use the [GroupOSSettingFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class GroupOSSettingFragment : NavTipsBackPressedFragment() {
    private val navArgs by navArgs<GroupOSSettingFragmentArgs>()
    private val viewModel by viewModels<GroupSpaceModel>({ requireParentFragment() })
    override fun getLayoutResId(): Int {
        return R.layout.fragment_group_o_s_setting
    }

    override fun getTopView(): View? {
        return titleBar
    }

    private fun isGroupManagement(): Boolean {
        return viewModel.isGroupManagement(navArgs.deviceid, navArgs.groupId)
    }

    private fun isDeviceManagement(): Boolean {
        return viewModel.isDeviceManagement(navArgs.deviceid)
    }

    private fun isGroupMember(): Boolean {
        return viewModel.isGroupMember(navArgs.deviceid, navArgs.groupId)
    }

    private fun hasFileManage(): Boolean {
        return viewModel.hasFileManage(navArgs.deviceid, navArgs.groupId)
    }


    //这些主要是现实直接跳转公告
    private var isFirst = true
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (navArgs.announcement && isFirst) {
            isFirst = false
            findNavController().navigate(
                R.id.action_groupOSSettingFragment_to_groupOSAnnouncementListFragment,
                GroupOSSettingFragmentArgs(navArgs.deviceid, navArgs.groupId).toBundle()
            )
        } else if (!isFirst) {
            findNavController().popBackStack()
        }
    }



    override fun initView(view: View) {
        titleBar.setBackListener {
            findNavController().popBackStack()
        }
        titleBar.setTitleText(viewModel.getGroupItemName(navArgs.deviceid, navArgs.groupId))
        if (isGroupManagement()) {
            titleBar.addRightTextButton(getString(R.string.rename))
                //重命名
                .singleClick {
                    DialogUtils.showEditDialog(
                        it.context,
                        R.string.rename,
                        R.string.group_name_describe,
                        null,
                        -1,
                        R.string.confirm,
                        R.string.cancel
                    ) { dialog: DialogInterface?, isPositiveBtn: Boolean, mEditTextNew: EditText ->
                        if (isPositiveBtn) {
                            val newName = mEditTextNew.text.toString().trim()
                            if (newName.isEmpty()|| newName.length>32) {
                                ToastUtils.showToast(R.string.group_name_describe)
                               // AnimUtils.sharkEditText(mEditTextNew)
                                return@showEditDialog
                            }
                            viewModel.renameGroup(navArgs.deviceid, navArgs.groupId, newName)
                                .observe(this,
                                    Observer {
                                        if (it.status == Status.SUCCESS) {
                                            GroupsKeeper.updateGroupName(navArgs.deviceid, navArgs.groupId,newName)
                                            viewModel.updateGroupItemName(navArgs.deviceid, navArgs.groupId,newName)
                                            titleBar.setTitleText(newName)
                                            ToastHelper.showLongToast(R.string.modify_succ)
                                            dialog?.dismiss()
                                        } else if (it.status == Status.ERROR) {
                                            viewModel.optErrorProcessor(it.code)
                                        }
                                    })
                        }
                    }

                }

        }
        setting_group_notice.isVisible = isGroupMember()
        setting_group_recycle.isVisible = isGroupManagement()||hasFileManage()
        setting_group_member.isVisible = isGroupMember()
        setting_leave_group.isVisible = isGroupMember()
        setting_delete_group.isVisible = isDeviceManagement() || isGroupManagement()

        setting_delete_group.singleClick {
            DialogUtils.showConfirmDialog(
                requireContext(),
                R.string.delete_group,
                R.string.confirm,
                R.string.cancel
            ) { dialog: DialogInterface?, isPositiveBtn: Boolean ->
                if (isPositiveBtn) {
                    dialog?.dismiss()
                    viewModel.deleteGroup(navArgs.deviceid, navArgs.groupId)
                        .observe(this, Observer {
                            if (it.status == Status.SUCCESS) {
                                if(activity is GroupSpaceNasFileActivity)
                                {
                                    requireActivity().finish()
                                }else{
                                    onBackPressed()
                                }
                            } else if (it.status == Status.ERROR) {
                                viewModel.optErrorProcessor(it.code)
                            }
                        })
                }

            }

        }
        setting_leave_group.singleClick {
            DialogUtils.showConfirmDialog(
                requireContext(),
                R.string.leave_group,
                R.string.confirm,
                R.string.cancel
            ) { dialog: DialogInterface?, isPositiveBtn: Boolean ->
                if (isPositiveBtn) {
                    dialog?.dismiss()
                    viewModel.leaveGroup(navArgs.deviceid, navArgs.groupId)
                        .observe(this, Observer {
                            if (it.status == Status.SUCCESS) {
                                if(activity is GroupSpaceNasFileActivity)
                                {
                                    requireActivity().finish()
                                }else{
                                    onBackPressed()
                                }
                            } else if (it.status == Status.ERROR) {
                                viewModel.optErrorProcessor(it.code)
                            }
                        })
                }
            }
        }
        setting_group_member.singleClick {
            findNavController().navigate(
                R.id.action_groupOSSettingFragment_to_groupMemberListFragment,
                GroupOSSettingFragmentArgs(navArgs.deviceid, navArgs.groupId).toBundle()
            )
        }
        setting_group_notice.singleClick {
            findNavController().navigate(
                R.id.action_groupOSSettingFragment_to_groupOSAnnouncementListFragment,
                GroupOSSettingFragmentArgs(navArgs.deviceid, navArgs.groupId).toBundle()
            )
        }

        setting_group_recycle.singleClick {
            findNavController().navigate(
                R.id.action_groupOSSettingFragment_to_groupOSRecycleFragment,
                GroupSpaceRecycleFragmentArgs(
                    navArgs.deviceid,
                    navArgs.groupId,
                    SharePathType.GROUP.type,
                    null
                ).toBundle()
            )
        }
    }

}