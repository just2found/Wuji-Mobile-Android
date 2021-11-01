package net.linkmate.app.ui.nas.user

import android.os.Bundle
import android.view.View
import android.widget.Switch
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import io.weline.repo.SessionCache
import io.weline.repo.data.model.PermissionsModel
import io.weline.repo.files.data.SharePathType
import kotlinx.android.synthetic.main.fragment_user_setting.*
import kotlinx.android.synthetic.main.include_title_bar.*
import libs.source.common.livedata.Resource
import libs.source.common.livedata.Status
import net.linkmate.app.R
import net.linkmate.app.ui.nas.TipsBackPressedFragment
import net.linkmate.app.view.HintEditDialog
import net.sdvn.common.internet.protocol.entity.MGR_LEVEL
import net.sdvn.nascommon.BaseActivity
import net.sdvn.nascommon.SessionManager
import net.sdvn.nascommon.constant.AppConstants
import net.sdvn.nascommon.constant.HttpErrorNo
import net.sdvn.nascommon.db.NasServiceKeeper
import net.sdvn.nascommon.db.objecbox.NasServiceItem
import net.sdvn.nascommon.model.oneos.OneOSUser
import net.sdvn.nascommon.utils.FileUtils
import net.sdvn.nascommon.utils.ToastHelper
import net.sdvn.nascommon.viewmodel.UserModel
import org.view.libwidget.singleClick

class UserSettingFragment : TipsBackPressedFragment() {
    private val usersViewModel by viewModels<UsersViewModel>({ requireActivity() })
    private val userModel by viewModels<UserModel>({ requireActivity() })
    private var hintEditDialog: HintEditDialog? = null
        get() {
            if (field == null) {
                field = HintEditDialog.newInstance()
            }
            return field
        }

    //因为设备的用户信息游问题，不是远程用户但是会TYPE_REMOTE 会是6
    private var isUnbind = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val extStorageEnable = NasServiceKeeper.queryNasServiceItem(
            getDeviceId(),
            NasServiceKeeper.SERVICE_TYPE_USBWEBSTORAGE
        )?.isServiceStatus == true
        val groupEnable = NasServiceKeeper.queryNasServiceItem(
            getDeviceId(),
            NasServiceKeeper.SERVICE_TYPE_GROUP
        )?.isServiceStatus == true
        userModel.mUserList.observe(this, Observer {
            val oneOSUser = usersViewModel.liveDataUser.value
            if (oneOSUser != null) {
                it?.filter {
                    it.name == oneOSUser.name
                }?.getOrNull(0)?.let { newUser ->
                    usersViewModel.setUser(oneOSUser.apply {
                        total = newUser.total
                        space = newUser.space
                        markName = newUser.markName
                        used = newUser.used
                        permissions = newUser.permissions
                    })
                }
            }
        })

        usersViewModel.liveDataUser.observe(this, Observer {
            if (it != null) {
                setting_ail_account.setTips(it.name)
                setting_ail_remark.setTips(it.markName)
                setting_ail_used_space.setTips(FileUtils.fmtFileSize(it.used))
                setting_ail_avilable_space.setTips(FileUtils.fmtFileSize(it.space))
                if (it.isCurrent) {
                    SessionManager.getInstance()
                        .getLoginSession(getDeviceId())?.userInfo?.permissions = it.permissions
                }
                it.permissions?.forEach { permission ->
                    when (permission.sharePathType) {
                        SharePathType.USER.type -> {
                            user_settings_switch_personal.isChecked = permission.isWriteable
                        }
                        SharePathType.PUBLIC.type -> {
                            user_settings_switch_public_read.isChecked = permission.isReadable
                            user_settings_switch_public_write.isEnabled = permission.isReadable
                            user_settings_switch_public_write.isChecked = permission.isWriteable
                        }
                        SharePathType.EXTERNAL_STORAGE.type -> {
                            user_settings_switch_ext_read.isVisible = true && extStorageEnable
                            user_settings_switch_ext_write.isVisible = true && extStorageEnable
                            user_settings_switch_ext_read.isChecked = permission.isReadable
                            user_settings_switch_ext_write.isEnabled = permission.isReadable
                            user_settings_switch_ext_write.isChecked = permission.isWriteable
                        }
                        SharePathType.GROUP.type -> {
                            user_settings_switch_group_read.isVisible = true && groupEnable
                            user_settings_switch_group_write.isVisible = true && groupEnable
                            user_settings_switch_group_read.isChecked = permission.isReadable
                            user_settings_switch_group_write.isEnabled = permission.isReadable
                            user_settings_switch_group_write.isChecked = permission.isWriteable
                        }
                    }
                }
                account_ail_delete.isVisible = it.name != AppConstants.DEFAULT_USERNAME_ADMIN
            }
        })
        usersViewModel.liveDataShareUser.observe(this, Observer {
            if (it.mgrlevel == MGR_LEVEL.UNBOUND.toInt()) {
                isUnbind = true
                return@Observer
            }

            val isCurrentUserNotOwner = it.mgrlevel != MGR_LEVEL.OWNER.toInt()
            setting_ail_change_admin.isVisible = isCurrentUserNotOwner && usersViewModel.isOwner
            setting_ail_change_owner.isVisible = isCurrentUserNotOwner && usersViewModel.isOwner
            setting_line_view.isVisible = isCurrentUserNotOwner && usersViewModel.isOwner
            setting_line_view2.isVisible = isCurrentUserNotOwner && usersViewModel.isOwner
            setting_ail_change_admin.setTitle(
                if (it.mgrlevel == 1) {
                    R.string.Downgrad_to_a_common_user
                } else {
                    R.string.upgrade_to_administrator
                }
            )
            layout_common_user.isVisible = isCurrentUserNotOwner

        })
        usersViewModel.liveDataLoading.observe(this, Observer {
            when (it.status) {
                Status.SUCCESS -> {
                    (requireActivity() as BaseActivity).dismissLoading()
                }
                Status.ERROR -> {
                    (requireActivity() as BaseActivity).dismissLoading()
                }
                Status.LOADING -> {
                    (requireActivity() as BaseActivity).showLoading()
                }
            }
            if (!it.data.isNullOrEmpty())
                ToastHelper.showLongToast(it.data)
        })
        val observer = Observer<Resource<String>> {
            when (it.status) {
                Status.SUCCESS -> {
                    val value = usersViewModel.liveDataShareUser.value
                    if (it.data == value?.userid) {
                        if (value?.isCurrent == true) {
                            //如果是当前账号weline
                            requireActivity().finish()
                        } else {
                            //如果是其他用户
                            findNavController().popBackStack()
                        }
                    }
                }
                Status.ERROR -> {

                }
                Status.LOADING -> {


                }
            }
        }
        usersViewModel.liveDataRemoveUser.observe(this, observer)
        usersViewModel.liveDataUnBind.observe(this, observer)
        usersViewModel.liveDataChangeMgrLevel.observe(this, Observer {
            if (it.status == Status.SUCCESS) {
                if (it.data?.second == 0) {
                    //如果是当前账号weline
                    requireActivity().finish()
                }
            }
        })
    }

    override fun getTopView(): View {
        return itb_rl
    }

    override fun initView(view: View) {
        mTipsBar = tipsBar
        itb_tv_title.setText(R.string.current_user)
        itb_iv_left.setImageResource(R.drawable.icon_return)
        itb_iv_left.isVisible = true
        itb_iv_left.singleClick {
            onBackPressed()
        }
        //nasV3  空间授权才生效
        val nasV3 = SessionCache.instance.isNasV3(getDeviceId())
        user_settings_switch_personal.isChecked = !nasV3
        user_settings_switch_public_read.isChecked = !nasV3
        user_settings_switch_public_write.isChecked = !nasV3

        user_settings_switch_personal.singleClick {
            if (checkNasV3(nasV3)) {
                val pathType = SharePathType.USER
                val oneOSUser = usersViewModel.liveDataUser.value
                    ?: return@singleClick kotlin.run { }
                val perm = PermissionsModel.getAllowPerm(it.isChecked)
                usersViewModel.setPermission(getDeviceId(), oneOSUser, pathType, perm)
            }
        }
        user_settings_switch_public_read.singleClick {
            if (checkNasV3(nasV3)) {
                val pathType = SharePathType.PUBLIC
                if (enableRead(it, pathType)) return@singleClick kotlin.run { }
            }
        }
        user_settings_switch_public_write.singleClick {
            if (checkNasV3(nasV3)) {
                val pathType = SharePathType.PUBLIC
                if (enableWritePerm(it, pathType)) return@singleClick kotlin.run { }
            }
        }
        user_settings_switch_ext_read.singleClick {
            if (checkNasV3(nasV3)) {
                val pathType = SharePathType.EXTERNAL_STORAGE
                if (enableRead(it, pathType)) return@singleClick kotlin.run { }
            }
        }
        user_settings_switch_ext_write.singleClick {
            if (checkNasV3(nasV3)) {
                val pathType = SharePathType.EXTERNAL_STORAGE
                if (enableWritePerm(it, pathType)) return@singleClick kotlin.run { }
            }
        }
        user_settings_switch_group_read.singleClick {
            if (checkNasV3(nasV3)) {
                val pathType = SharePathType.GROUP
                if (enableRead(it, pathType)) return@singleClick kotlin.run { }
            }
        }
        user_settings_switch_group_write.singleClick {
            if (checkNasV3(nasV3)) {
                val pathType = SharePathType.GROUP
                if (enableWritePerm(it, pathType)) return@singleClick kotlin.run { }
            }
        }

        setting_ail_remark.singleClick {
            val oneOSUser = usersViewModel.liveDataUser.value
                ?: return@singleClick kotlin.run { }
            usersViewModel.editMarkName(requireContext(), getDeviceId(), oneOSUser)
        }

        account_ail_delete.singleClick {
            val oneOSUser = usersViewModel.liveDataUser.value
                ?: return@singleClick kotlin.run { }
            val shareUser = usersViewModel.liveDataShareUser.value
                ?: return@singleClick kotlin.run { }
            usersViewModel.deleteUser(
                requireContext(), getDeviceId(), oneOSUser, usersViewModel.isOwner,
                usersViewModel.isAdmin, shareUser.userid, hintEditDialog, isUnbind
            )
                ?.show(requireActivity().supportFragmentManager, "hintEditDialog")
        }
        setting_ail_avilable_space.singleClick {
            val oneOSUser = usersViewModel.liveDataUser.value
                ?: return@singleClick kotlin.run { }

            usersViewModel.showChangedSpaceDialog(
                requireContext(),
                getDeviceId(),
                oneOSUser,
                usersViewModel.total
            )
        }
        setting_ail_change_admin.singleClick {
            val shareUser = usersViewModel.liveDataShareUser.value
                ?: return@singleClick kotlin.run { }
            usersViewModel.showModifyUserLevelDialog(
                requireContext(), getDeviceId(), shareUser, if (shareUser.mgrlevel == 1) {
                    2
                } else {
                    1
                }
            )
        }
        setting_ail_change_owner.singleClick {
            val shareUser = usersViewModel.liveDataShareUser.value
                ?: return@singleClick kotlin.run { }
            usersViewModel.showModifyUserLevelDialog(requireContext(), getDeviceId(), shareUser, 0)
        }
        val oneOSUser = usersViewModel.liveDataUser.value
        oneOSUser?.name.takeIf { !it.isNullOrEmpty() }?.let { username ->
            usersViewModel.getUserInfo(getDeviceId(), username)
            usersViewModel.getUserSpace(getDeviceId(), username)
        }
    }

    private fun enableWritePerm(it: Switch, pathType: SharePathType): Boolean {
        val oneOSUser = usersViewModel.liveDataUser.value
            ?: return true
        val permission = usersViewModel.getPermission(oneOSUser, pathType)
            ?: return true
        val perm = permission.setWriteable(it.isChecked)
        usersViewModel.setPermission(getDeviceId(), oneOSUser, pathType, perm)
        return false
    }

    private fun enableRead(it: Switch, pathType: SharePathType): Boolean {
        val oneOSUser = usersViewModel.liveDataUser.value
            ?: return true
        val perm = PermissionsModel.getReadPerm(it.isChecked)
        usersViewModel.setPermission(getDeviceId(), oneOSUser, pathType, perm)
        return false
    }

    private fun checkNasV3(nasV3: Boolean): Boolean {
        if (!nasV3)
            ToastHelper.showLongToast(HttpErrorNo.getUpgradeVersion())
        return nasV3
    }


    private fun getDeviceId(): String {
        return devId ?: ""
    }

    override fun onBackPressed(): Boolean {
        return findNavController().popBackStack()
    }

    override fun isEnableOnBackPressed(): Boolean {
        return true
    }

    override fun getLayoutResId(): Int {
        return R.layout.fragment_user_setting
    }

    companion object {
        fun newInstance(devId: String?): UserSettingFragment {
            val args = Bundle()
            if (!devId.isNullOrEmpty()) {
                args.putString(AppConstants.SP_FIELD_DEVICE_ID, devId)
            }
            val fragment = UserSettingFragment()
            fragment.arguments = args
            return fragment
        }
    }
}