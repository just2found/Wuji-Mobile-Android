package net.linkmate.app.ui.nas.group

import android.content.DialogInterface
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import io.weline.repo.api.ERROR_40504
import io.weline.repo.api.GroupUserPerm
import io.weline.repo.api.V5HttpErrorNo
import kotlinx.android.synthetic.main.fragment_group_space_user_detail.*
import libs.source.common.livedata.Status
import libs.source.common.utils.EmptyUtils
import libs.source.common.utils.InputMethodUtils
import net.linkmate.app.R
import net.linkmate.app.ui.nas.NavTipsBackPressedFragment
import net.linkmate.app.ui.nas.group.list.GroupSpaceNasFileActivity
import net.sdvn.nascommon.SessionManager
import net.sdvn.nascommon.db.objboxkt.GroupUser
import net.sdvn.nascommon.utils.AnimUtils
import net.sdvn.nascommon.utils.DialogUtils
import org.view.libwidget.singleClick


/**
 * A simple [Fragment] subclass.
 * Use the [GroupSpaceUserDetailFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class GroupSpaceUserDetailFragment : NavTipsBackPressedFragment() {

    private val viewModel by viewModels<GroupSpaceModel>({ requireParentFragment() })
    private val navArgs by navArgs<GroupSpaceUserDetailFragmentArgs>()

    override fun initView(view: View) {
        initTitle()
        val data = navArgs.data
        refreshUI(data)
        group_transfer_management_authority.isVisible = !data.isAdmin
        val findNavController = findNavController()
        group_transfer_management_authority.singleClick {
            DialogUtils.showConfirmDialog(
                requireContext(),
                R.string.transfer_management_authority,
                0,
                R.string.confirm,
                R.string.cancel
            ) { dialog, isPositiveBtn ->
                if (isPositiveBtn) {
                    viewModel.transferGroup(data.devId, data.groupId, data.username)
                        .observe(this,
                            Observer {
                                if (it.status == Status.SUCCESS) {
                                    if (activity is GroupSpaceNasFileActivity) {
                                        viewModel.loadGroupUsers(data.devId, data.groupId)
                                        requireActivity().finish()
                                    } else {
                                        viewModel.loadGroupUsers(data.devId, data.groupId)
                                        backStackSettingRoot(findNavController)
                                    }
                                } else if (it.status == Status.ERROR) {
                                    viewModel.optErrorProcessor(it.code)
                                    if (it.code == ERROR_40504) {
                                        findNavController().popBackStack()
                                    }
                                }
                            })
                    dialog.dismiss()
                }
            }

        }
        group_allow_upload_of_group_files.isVisible = !data.isAdmin
        group_allow_management_of_group_files.isVisible = !data.isAdmin
        delete.isVisible = !data.isAdmin
        group_mark_name.singleClick {
            val markName = data.mark
            DialogUtils.showEditDialog(
                context, R.string.tip_set_user_name, R.string.hint_set_user_name, markName,
                net.sdvn.nascommonlib.R.string.max_name_length,
                R.string.confirm, R.string.cancel
            ) { dialog, isPositiveBtn, mContentEditText ->
                val context = mContentEditText.context
                if (isPositiveBtn) {
                    val newName = mContentEditText.text.toString().trim { it <= ' ' }
                    if (EmptyUtils.isEmpty(newName) || newName.length > 16) {
                        AnimUtils.sharkEditText(context, mContentEditText)
                    } else {
                        viewModel.manageGroupUser(data.devId, data.groupId, data, newName)
                            .observe(this,
                                Observer {
                                    if (it.status == Status.SUCCESS) {
                                        group_mark_name.setTips(data.mark)
                                        InputMethodUtils.hideKeyboard(context, mContentEditText)
                                        dialog.dismiss()
                                    } else if (it.status == Status.ERROR) {
                                        viewModel.optErrorProcessor(it.code)
                                        if (it.code == ERROR_40504) {
                                            findNavController().popBackStack()
                                        }
                                    }
                                })
                    }
                } else {
                    InputMethodUtils.hideKeyboard(context, mContentEditText)
                    dialog.dismiss()
                }
            }
        }
        delete.singleClick {
            DialogUtils.showConfirmDialog(
                requireContext(),
                R.string.confirm_delete,
                R.string.confirm,
                R.string.cancel
            ) { dialog: DialogInterface?, isPositiveBtn: Boolean ->
                if (isPositiveBtn) {
                    dialog?.dismiss()
                    viewModel.deleteUser(data.devId, data.groupId, data)
                        .observe(this, Observer {
                            if (it.status == Status.SUCCESS) {
                                if (data.isAdmin || data.userId == SessionManager.getInstance().userId) {
                                    viewModel.getGroupListJoined(data.devId)
                                    backStackSettingRoot(findNavController)
                                } else {
                                    findNavController.popBackStack()
                                }
                            } else if (it.status == Status.ERROR) {
                                viewModel.optErrorProcessor(it.code)
                                if (it.code == ERROR_40504) {
                                    findNavController().popBackStack()
                                }
                            }
                        })
                }
            }
        }
        group_allow_upload_of_group_files.singleClick { _switch ->
            val checked = _switch.isChecked
            val perm = GroupUserPerm.switchUploadEnable(data.perm, checked)
            viewModel.manageGroupUser(data.devId, data.groupId, data, perm).observe(this,
                Observer {
                    if (it.status == Status.SUCCESS) {
                        group_allow_upload_of_group_files.isChecked =
                            GroupUserPerm.isUploadEnable(data.perm)
                    } else if (it.status == Status.ERROR) {
                        viewModel.optErrorProcessor(it.code)
                        if (it.code == ERROR_40504) {
                            findNavController().popBackStack()
                        }
                    }
                })

        }


        group_allow_upload_of_group_files.setOnCheckedChangeListener { _switch, x ->
            val checked = _switch.isChecked
            val perm = GroupUserPerm.switchUploadEnable(data.perm, checked)
            viewModel.manageGroupUser(data.devId, data.groupId, data, perm).observe(this,
                Observer {
                    if (it.status == Status.SUCCESS) {
                        group_allow_upload_of_group_files.isChecked =
                            GroupUserPerm.isUploadEnable(data.perm)
                    } else if (it.status == Status.ERROR) {
                        viewModel.optErrorProcessor(it.code)
                        if (it.code == ERROR_40504) {
                            findNavController().popBackStack()
                        }
                    }
                })
        }


        group_allow_management_of_group_files.singleClick { _switch ->
            val checked = _switch.isChecked
            val perm = GroupUserPerm.switchManagementEnable(data.perm, checked)
            viewModel.manageGroupUser(data.devId, data.groupId, data, perm).observe(this,
                Observer {
                    if (it.status == Status.SUCCESS) {
                        group_allow_management_of_group_files.isChecked =
                            GroupUserPerm.isManageEnable(data.perm)
                    } else if (it.status == Status.ERROR) {
                        viewModel.optErrorProcessor(it.code)
                        if (it.code == ERROR_40504) {
                            findNavController().popBackStack()
                        }
                    }
                })
        }

        group_allow_management_of_group_files.setOnCheckedChangeListener { _switch, x ->
            val checked = _switch.isChecked
            val perm = GroupUserPerm.switchManagementEnable(data.perm, checked)
            viewModel.manageGroupUser(data.devId, data.groupId, data, perm).observe(this,
                Observer {
                    if (it.status == Status.SUCCESS) {
                        group_allow_management_of_group_files.isChecked =
                            GroupUserPerm.isManageEnable(data.perm)
                    } else if (it.status == Status.ERROR) {
                        viewModel.optErrorProcessor(it.code)
                        if (it.code == ERROR_40504) {
                            findNavController().popBackStack()
                        }
                    }
                })
        }

    }

    private fun backStackSettingRoot(findNavController: NavController) {
        try {
            val backStackEntry =
                findNavController.getBackStackEntry(R.id.groupOSSettingFragment)
            findNavController.popBackStack(
                R.id.groupOSSettingFragment,
                true
            )
        } catch (e: Exception) {

        }
    }

    private fun refreshUI(data: GroupUser) {
        group_transfer_management_authority.isVisible = !data.isAdmin
        group_account.setTips(data.username)
        group_mark_name.setTips(data.mark)
        group_allow_upload_of_group_files.isChecked = GroupUserPerm.isUploadEnable(data.perm)
        group_allow_management_of_group_files.isChecked = GroupUserPerm.isManageEnable(data.perm)
    }

    override fun getLayoutResId(): Int {
        return R.layout.fragment_group_space_user_detail
    }

    private fun initTitle() {
        titleBar.setBackListener {
            findNavController().popBackStack()
        }
        mTipsBar = tipsBar
    }

    override fun getTopView(): View? {
        return titleBar
    }
}