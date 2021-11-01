package net.linkmate.app.ui.activity.nasApp.deviceDetial.adapter

import android.app.Dialog
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.arch.core.util.Function
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.dialog_device_item_member.view.*
import net.linkmate.app.R
import net.linkmate.app.ui.activity.nasApp.deviceDetial.DeviceDetailViewModel
import net.linkmate.app.ui.activity.nasApp.deviceDetial.DeviceViewModel
import net.linkmate.app.ui.activity.nasApp.deviceDetial.repository.DeviceMemberRepository
import net.linkmate.app.view.DataItemLayout
import net.linkmate.app.view.HintEditDialog
import net.sdvn.cmapi.CMAPI
import net.sdvn.common.internet.protocol.entity.ShareUser
import net.sdvn.nascommon.utils.DialogUtils

/**成员管理
 * @author Raleigh.Luo
 * date：20/7/24 14
 * describe：
 */
class DeviceMemberAdapter(context: Fragment, fragmentViewModel: DeviceDetailViewModel,
                          viewModel: DeviceViewModel)
    : DeviceBaseAdapter<DeviceDetailViewModel>(context, fragmentViewModel, viewModel) {
    private val repository: DeviceMemberRepository = DeviceMemberRepository()
    private var sources: List<ShareUser>? = null

    init {
        getItemSources()
    }

    private fun getItemSources() {
        repository.getShareUsers(viewModel.device.id, viewModel.mStateListener, Function {
            sources = it
            notifyDataSetChanged()
            null
        })
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
                LayoutInflater.from(context.requireContext()).inflate(R.layout.dialog_device_item_member, null, false)
        view.layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return sources?.size ?: 0
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        with(holder.itemView) {
            sources?.let {
                val username = it[position].username ?: ""
                tvUser.setText(it[position].fullName.trim() + "(" + username + ")")
                if (it[position].isAdmin || it[position].isOwner) {
                    var resId = if (it[position].isOwner) R.drawable.icon_user_admin else R.drawable.icon_user_master
                    ivAdmin.visibility = View.VISIBLE
                    ivAdmin.setImageDrawable(context.getDrawable(resId))
                } else {
                    ivAdmin.visibility = View.GONE
                }
                setOnClickListener {
                    //是否已经被拦截处理
                    val isInterceptor = onItemClickListener?.onClick(it, position) ?: false
                    //没有拦截，则可以内部处理
                    if (!isInterceptor) internalItemClick(it, position)
                }
            }

        }
    }

    override fun internalItemClick(view: View, position: Int) {
        sources?.let {
            if (position >= 0 && position < it.size) showUserMngDialog(it[position])
        }

    }

    override fun internalItemLongClick(view: View, position: Int) {
    }

    private fun showUserMngDialog(user: ShareUser) {
        val view = LayoutInflater.from(context.requireContext()).inflate(R.layout.layout_dialog_nas_user_manage, null)
        val dataItemAccount: DataItemLayout = view.findViewById(R.id.nmg_des_dil_account)
        val dataItemName: DataItemLayout = view.findViewById(R.id.nmg_des_dil_name)
        val dataItemSpace: DataItemLayout = view.findViewById(R.id.nmg_des_dil_space)
        val tvTransOwner = view.findViewById<View>(R.id.nmg_des_btn_transfer_owner)
        val tvMdfLevel = view.findViewById<TextView>(R.id.nmg_des_btn_up)
        val tvDelete = view.findViewById<View>(R.id.nmg_des_btn_delete)
        val ivBack = view.findViewById<View>(R.id.iv_back)
        val dialog = Dialog(context.requireContext(), R.style.DialogTheme)
        dialog.setContentView(view)
        dialog.setCancelable(false)
        dialog.show()
        dataItemAccount.setText(user.username)
        dataItemName.visibility = View.GONE
        dataItemSpace.visibility = View.GONE
        if (viewModel.device.getMnglevel() == 0
                && CMAPI.getInstance().baseInfo.userId != user.userid) {
            //如果是所有者，并且选的不是自己
            tvMdfLevel.visibility = View.VISIBLE
            if (user.mgrlevel == 1) {
                tvMdfLevel.text = context.getString(R.string.Downgrad_to_a_common_user)
            }
            tvMdfLevel.setOnClickListener {
                if (user.mgrlevel == 1) {
                    showModifyUserLevelDialog(user, 2)
                } else {
                    showModifyUserLevelDialog(user, 1)
                }
                dialog.dismiss()
            }
            tvTransOwner.visibility = View.VISIBLE
            tvTransOwner.setOnClickListener {
                showModifyUserLevelDialog(user, 0)
                dialog.dismiss()
            }
        }
        ivBack.setOnClickListener { dialog.dismiss() }
        tvDelete.setOnClickListener {
            deleteUser(user)
            dialog.dismiss()
        }
    }


    private fun showModifyUserLevelDialog(user: ShareUser, mgrlevel: Int) {
        val titleResId: Int
        titleResId = when (mgrlevel) {
            0 -> R.string.transfer_ownership_of_this_device
            1 -> R.string.upgrade_to_administrator
            else -> R.string.Downgrad_to_a_common_user
        }
        DialogUtils.showWarningDialog(context.requireContext(),
                titleResId,
                -1,
                R.string.confirm, R.string.cancel) { dialog, isPositiveBtn ->
            dialog.dismiss()
            if (isPositiveBtn) {
                repository.modifyUserLevel(user, mgrlevel, viewModel.device.id, Function {
                    if (it) {//刷新
//                        getItemSources()
                        viewModel.toFinishActivity()
                    }
                    null
                })
            }
        }
    }

    private fun deleteUser(user: ShareUser) {
        val curUid = CMAPI.getInstance().baseInfo.userId
        if (viewModel.device.getMnglevel() == 0) {
            if (curUid == user.userid) { //所有者删除自己，要先转移所有者权限
                DialogUtils.showNotifyDialog(context.requireContext(),
                        R.string.permission_denied, R.string.tip_please_change_admin, R.string.ok, null)
            } else { //所有者删除其他用户
                showDeleteUserDialog(user)
            }
        } else if (curUid == user.userid) { //用户删除自己账号
            showDeleteSelfDialog(user)
        } else if (viewModel.device.getMnglevel() == 1 && user.mgrlevel != 0 && user.mgrlevel != 1) { //管理员删除其他用户
            showDeleteUserDialog(user)
        } else { //非所有者用户删除其他普通用户
            DialogUtils.showNotifyDialog(context.requireContext(),
                    R.string.permission_denied, R.string.please_login_onespace_with_admin, R.string.ok, null)
        }
    }

    private var hintEditDialog: HintEditDialog? = null
        get() {
            if (field == null) {
                field = HintEditDialog.newInstance()
            }
            return field
        }

    private fun showDeleteSelfDialog(user: ShareUser) {
        if (TextUtils.isEmpty(user.userid)) return
        //本地用户  勾选清除用户数据，且不可取消
        hintEditDialog?.update(
                title = context.getString(R.string.remove_device_title),
                content = context.getString(R.string.manager_remove_device_content),
                contentTextColor = R.color.red,
                editHint = context.getString(R.string.manager_remove_device_hint),
                editContent = "",
                matchEditToHint = true,
                checkBoxText =  null,
                isCheckedBox = false,
                checkboxEnable = false,
                confrimText = context.getString(R.string.confirm),
                cancelText = context.getString(R.string.cancel)
        )
        hintEditDialog?.setOnClickListener(object : View.OnClickListener {
            override fun onClick(view: View?) {
                if (view?.id == R.id.positive) {
                    repository.unbind(user.userid, viewModel.device.id, Function {
                        if (CMAPI.getInstance().baseInfo.userId == user.userid) {//用户删除自己账号
                            viewModel.sendRemoveDevBroadcast(view.context.applicationContext)
                            viewModel.toFinishActivity()
                        } else {
                            viewModel.toFinishActivity()
                        }
                        null
                    })
                }
            }

        })
        hintEditDialog?.show(context.requireActivity().getSupportFragmentManager(), "hintEditDialog")
    }

    private fun showDeleteUserDialog(user: ShareUser) {
        //本地用户  勾选清除用户数据，且不可取消
        hintEditDialog?.update(
                title = context.getString(R.string.remove_device_title),
                content = context.getString(R.string.manager_remove_device_content),
                contentTextColor = R.color.red,
                editHint = context.getString(R.string.manager_remove_device_hint),
                editContent = "",
                matchEditToHint = true,
                checkBoxText =  null,
                isCheckedBox = false,
                checkboxEnable = false,
                confrimText = context.getString(R.string.confirm),
                cancelText = context.getString(R.string.cancel)
        )
        hintEditDialog?.setOnClickListener(object : View.OnClickListener {
            override fun onClick(view: View?) {
                if (view?.id == R.id.positive) {
                    repository.unbind(user.userid, viewModel.device.id, Function {
                        if (CMAPI.getInstance().baseInfo.userId == user.userid) {//用户删除自己账号
                            viewModel.sendRemoveDevBroadcast(view.context.applicationContext)
                            viewModel.toFinishActivity()
                        } else {
                            viewModel.toFinishActivity()
                        }
                        null
                    })
                }
            }

        })
        hintEditDialog?.show(context.requireActivity().getSupportFragmentManager(), "hintEditDialog")
    }


}