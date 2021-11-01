package net.linkmate.app.ui.activity.nasApp.deviceDetial.adapter

import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.arch.core.util.Function
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.NavController
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.functions.Consumer
import io.weline.devhelper.DevTypeHelper
import io.weline.repo.SessionCache
import io.weline.repo.data.model.BaseProtocol
import io.weline.repo.net.V5Observer
import io.weline.repo.repository.V5Repository
import kotlinx.android.synthetic.main.dialog_device_item_detail.view.*
import kotlinx.android.synthetic.main.dialog_device_item_switch.view.*
import net.linkmate.app.R
import net.linkmate.app.base.MyConstants
import net.linkmate.app.base.MyOkHttpListener
import net.linkmate.app.data.ScoreHelper
import net.linkmate.app.manager.DevManager
import net.linkmate.app.ui.activity.nasApp.NasAppsActivity
import net.linkmate.app.ui.activity.nasApp.deviceDetial.DeviceDetailFragmentDirections
import net.linkmate.app.ui.activity.nasApp.deviceDetial.DeviceSettingFeesActivity
import net.linkmate.app.ui.activity.nasApp.deviceDetial.DeviceViewModel
import net.linkmate.app.ui.activity.nasApp.deviceDetial.FunctionHelper
import net.linkmate.app.ui.activity.nasApp.deviceDetial.repository.DeviceControlRepository
import net.linkmate.app.ui.nas.NasNavActivity
import net.linkmate.app.ui.nas.user.UserManageActivity
import net.linkmate.app.ui.simplestyle.device.disk.DiskSpaceActivity
import net.linkmate.app.ui.viewmodel.TrafficPriceEditViewModel
import net.linkmate.app.util.CheckStatus
import net.linkmate.app.util.DialogUtil
import net.linkmate.app.util.DialogUtil.OnDialogButtonClickListener
import net.linkmate.app.util.ToastUtils
import net.linkmate.app.view.AlterFlowFeePayerDialog
import net.linkmate.app.view.HintDialog
import net.linkmate.app.view.HintEditDialog
import net.sdvn.cmapi.CMAPI
import net.sdvn.common.internet.core.GsonBaseProtocol
import net.sdvn.common.internet.loader.SetDeviceNameHttpLoader
import net.sdvn.common.internet.utils.LoginTokenUtil
import net.sdvn.nascommon.SessionManager
import net.sdvn.nascommon.constant.AppConstants
import net.sdvn.nascommon.db.UserInfoKeeper
import net.sdvn.nascommon.iface.GetSessionListener
import net.sdvn.nascommon.model.UiUtils
import net.sdvn.nascommon.model.oneos.api.OneOSDeviceInfoAPI
import net.sdvn.nascommon.model.oneos.user.LoginSession
import net.sdvn.nascommon.utils.*

/**控制面板
 * @author Raleigh.Luo
 * date：20/7/27 14
 * describe：
 */
class DeviceControlAdapter(context: Fragment, fragmentViewModel: DeviceControlViewModel,
                           viewModel: DeviceViewModel, val navController: NavController)
    : DeviceBaseAdapter<DeviceControlViewModel>(context, fragmentViewModel, viewModel) {
    private val repository: DeviceControlRepository = DeviceControlRepository()
    private val menus: ArrayList<FunctionHelper.DetailMenu> = ArrayList()
    private var hideDialog: Dialog? = null

    init {
        initObserver()
        getItemSources()
    }

    private fun initObserver() {
//        //绑定的设备、管理员权限
//        val isMngr = viewModel.device.isOwner || viewModel.device.isAdmin
//        //请求收费项 数据
//        if (isMngr && viewModel.device.isOnline && NetsRepo.getCurrentNet()?.isCharge ?: false
//                && viewModel.device.enServer != null) {
//            fragmentViewModel.startGetFees()
//        } else if (isMngr && (NetsRepo.getCurrentNet()?.isCharge ?: false == false || NetsRepo.getCurrentNet()?.isDevSepCharge ?: false) && viewModel.device.isEn && viewModel.device.hardData != null) {
//            fragmentViewModel.startGetFees()
//        }
//        //设备：有设置收费项数据，才显示
//        fragmentViewModel.feeType.observe(context, Observer {
//            if (it.fees?.size ?: 0 > 0 && SETTING_FEE_INDEX >= 0) {
//                menus.add(SETTING_FEE_INDEX,FunctionHelper.getDeviceMenu(FunctionHelper.DEVICE_SETTING_FEES))
//                notifyDataSetChanged()
//            }
//
//        })
        fragmentViewModel.alterFlowFeePayerResult.removeObservers(context)
        fragmentViewModel.alterFlowFeePayerResult.observe(context, Observer {
            if (mAlterFlowFeePayerDialog?.dialog?.isShowing ?: false) {
                mAlterFlowFeePayerDialog?.dismiss()
            }
            if (it) {//设置成功
                viewModel.device.hardData?.chargetype = (fragmentViewModel.startAlterFlowFeePayer.value
                        ?: 1)
                //刷新设备列表属性
                DevManager.getInstance().initHardWareList(null)
            }
        })
    }

    override fun getItemViewType(position: Int): Int {
        return if (viewModel.device.getSelectable() && position == 0) TYPE_SWITCH else TYPE_DEFALUT
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layout = if (viewType == TYPE_SWITCH) R.layout.dialog_device_item_switch else R.layout.dialog_device_item_detail
        val view =
                LayoutInflater.from(context.requireContext()).inflate(layout, null, false)
        view.layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        //节点设备,节点开关显示、节点选择的逻辑
        val node: Int = if (viewModel.device.getSelectable()) 1 else 0
        return menus.size + node
    }


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        when (getItemViewType(position)) {
            TYPE_SWITCH -> {//选择节点开关
                with(holder.itemView) {
                    mSwitch.setOnCheckedChangeListener(null)
                    mSwitch.setText(R.string.enable_node)
                    mSwitch.setChecked(CMAPI.getInstance().baseInfo.hadSelectedSn(viewModel.device.getId()))
                    mSwitch.setOnCheckedChangeListener(onCheckedChangeListener)
                }
            }
            TYPE_DEFALUT -> {
                with(holder.itemView) {
                    var index = position
                    if (viewModel.device.getSelectable()) index = position - 1
                    val menu = menus[index]
                    ivDeviceDetailIcon.setImageResource(menu.icon)
                    ivDeviceDetailTitle.setText(menu.title)
                    setTag(menu.function)
                    setOnClickListener {
                        //是否已经被拦截处理
                        val isInterceptor = onItemClickListener?.onClick(it, position) ?: false
                        //没有拦截，则可以内部处理
                        if (!isInterceptor) internalItemClick(it, position)
                    }
                }
            }
        }
    }


    /**
     * 没有被外部监听器拦截，内部处理点击事件
     * @param position 位置
     */
    override fun internalItemClick(view: View, position: Int) {
//        0->//选择节点
        val function = view.getTag() as Int
        when (function) {
            FunctionHelper.ALTER_FLOW_FEE_PAYER -> {//更改流量费
                alterFlowFeePayer()
            }
            FunctionHelper.EDIT_DEVICE_NAME -> {//编辑设备名字
                rename()
            }
            FunctionHelper.EDIT_DEVICE_REMARK -> {//编辑设备备注
                editMarkName()
            }
            FunctionHelper.EDIT_UNIT_PRICE -> {//编辑流量单价
                editPrice()
            }
            FunctionHelper.DEVICE_REMOVE -> {//移除
                remove()
            }
            FunctionHelper.DEVICE_RESTART -> {//重新启动
                restart()
            }
            FunctionHelper.DEVICE_SELF_SHUTDOWN -> {//安全关机
                shutdown()
            }
            FunctionHelper.DEVICE_APP_MANAGER -> {//应用管理
                appManager()
            }
            FunctionHelper.DEVICE_LAN_ACCESS -> {
                val intent = Intent(context.requireContext(), NasNavActivity::class.java)
                intent.putExtra(AppConstants.SP_FIELD_DEVICE_ID, viewModel.device.getId())
                intent.putExtra(AppConstants.FUNCTION_ID, NasNavActivity.DEVICE_LAN_ACCESS)
                context.startActivity(intent)
            }
            FunctionHelper.DEVICE_SPACE -> {//设备空间
                SessionCache.instance.isNasV3OrAsyncReq(viewModel.device.id, viewModel.device.vip, Function {
                    if (it) {
                        context.startActivity(Intent(context.requireContext(), DiskSpaceActivity::class.java)
                                .putExtra(io.weline.repo.files.constant.AppConstants.SP_FIELD_DEVICE_ID, viewModel.device.id) //是否是EN服务器
                        )
                        viewModel.toFinishActivity()
                    } else {
                        CheckStatus.checkDeviceStatus(context.requireActivity(), context.requireActivity().supportFragmentManager, viewModel.device, Function {
                            if (it) {//状态正常
                                navController.navigate(DeviceDetailFragmentDirections.enterDetial(function))
                            }
                            null
                        }, Function {
                            if (it) {//状态异常，且进行了下一步,点击了确定，关闭页面
                                viewModel.toFinishActivity()
                            }
                            null
                        })
                    }
                    null
                })
            }
            FunctionHelper.DEVICE_SETTING_FEES -> {//设置收费项
                var networkid: String? = null
                with(viewModel.device) {
                    val isNotENServer = if (inNetwork(CMAPI.getInstance().baseInfo.netid)) enServer == null else (hardData?.isSrcProvide
                            ?: false) == false
                    //必须是提供服务的EN
                    if (!isNotENServer) networkid = viewModel.device.hardData?.networkId
                }
                context.startActivity(Intent(context.requireContext(), DeviceSettingFeesActivity::class.java)
                        .putExtra(io.weline.repo.files.constant.AppConstants.SP_FIELD_DEVICE_ID, viewModel.device.id)
                        .putExtra(FunctionHelper.EXTRA_IS_ENSERVER, viewModel.device.enServer != null)//是否是EN服务器

                        .putExtra(io.weline.repo.files.constant.AppConstants.SP_FIELD_NETWORK, networkid)
                )
                viewModel.toFinishActivity()
            }
            else -> {
                if (function == FunctionHelper.DEVICE_MEMBER && !viewModel.device.isDevDisable() && viewModel.device.isNas) {
//                    //nas设备成员管理
                    val intent = Intent(context.requireContext(), UserManageActivity::class.java)
                    intent.putExtra(AppConstants.SP_FIELD_DEVICE_ID, viewModel.device.getId())
                    context.startActivity(intent)
                    viewModel.toFinishActivity()
                } else if (function == FunctionHelper.DEVICE_REMOTE_MANAGER && viewModel.device.isDevDisable() && viewModel.device.devDisableReason == 1) {
                    ScoreHelper.showNeedMBPointDialog(context.requireContext())
                } else {
                    CheckStatus.checkDeviceStatus(context.requireActivity(), context.requireActivity().supportFragmentManager, viewModel.device, Function {
                        if (it) {//状态正常
                            navController.navigate(DeviceDetailFragmentDirections.enterDetial(function))
                        }
                        null
                    }, Function {
                        if (it) {//状态异常，且进行了下一步,点击了确定，关闭页面
                            viewModel.toFinishActivity()
                        }
                        null
                    })

                }
            }
        }
    }


    override fun internalItemLongClick(view: View, position: Int) {
    }

    private var mAlterFlowFeePayerDialog: AlterFlowFeePayerDialog? = null
        get() {
            if (field == null) {
                field = AlterFlowFeePayerDialog()
                field?.onDismissListener = DialogInterface.OnDismissListener {
                    viewModel.setSecondaryDialogShow(false)
                }
                field?.onShowListener = DialogInterface.OnShowListener {
                    viewModel.setSecondaryDialogShow(true)
                }
                field?.callBackFunction = Function {
                    fragmentViewModel.startAlterFlowFeePayer(viewModel.device.id, it)
                    null
                }
            }
            return field
        }

    private fun alterFlowFeePayer() {
        if (mAlterFlowFeePayerDialog?.dialog?.isShowing != true) {
            mAlterFlowFeePayerDialog?.update(viewModel.device.hardData?.chargetype ?: 1)
            mAlterFlowFeePayerDialog?.show(context.requireActivity().getSupportFragmentManager(), "mAlterFlowFeePayerDialog")
        }
    }

    /**
     * 应用管理
     */
    private fun appManager() {
        if (viewModel.device.isDevDisable() && viewModel.device.devDisableReason == 1) {//没有积分，需要购买
            ScoreHelper.showNeedMBPointDialog(context.requireContext())
        } else {
            CheckStatus.checkDeviceStatus(context.requireActivity(), context.requireActivity().supportFragmentManager,
                    viewModel.device, Function {
                if (it) {//状态正常
                    val intent = Intent(context.requireContext(), NasAppsActivity::class.java)
                    intent.putExtra(AppConstants.SP_FIELD_DEVICE_ID, viewModel.device.getId())
                    context.startActivity(intent)
                }
                null
            }, Function {
                if (it) {//状态异常，且进行了下一步,点击了确定，关闭页面
                    viewModel.toFinishActivity()
                }
                null
            })
        }
    }

    private var hintEditDialog: HintEditDialog? = null
        get() {
            if (field == null) {
                field = HintEditDialog.newInstance()
            }
            field?.onDismissListener = DialogInterface.OnDismissListener {
                viewModel.setSecondaryDialogShow(false)
            }
            field?.onShowListener = DialogInterface.OnShowListener {
                viewModel.setSecondaryDialogShow(true)
            }
            return field
        }

    /**
     * 所有者移除提示框
     */
    private fun showOwnerRemoveDialog() {
        if (hintEditDialog?.dialog?.isShowing != true) {
            //2021/3/25:“同时清除用户数据”的勾选框在非NAS设备隐藏
            val isNas = viewModel.device.isNas
            hintEditDialog?.update(
                    title = context.getString(R.string.remove_device_title),
                    content = context.getString(R.string.manager_remove_device_content),
                    contentTextColor = R.color.red,
                    editHint = context.getString(R.string.manager_remove_device_hint),
                    editContent = "",
                    matchEditToHint = true,
                    checkBoxText = if (isNas) context.getString(R.string.clear_user_data_hint) else null,
                    isCheckedBox = isNas,
                    confrimText = context.getString(R.string.confirm),
                    cancelText = context.getString(R.string.cancel)

            )
            hintEditDialog?.setOnClickListener(object : View.OnClickListener {
                override fun onClick(view: View?) {
                    if (view?.id == R.id.positive) {
                        val isCheckedBox = hintEditDialog?.isCheckedBox() ?: false
                        if (isCheckedBox) {//勾选了清除全部用户数据
                            if (viewModel.device.isDevDisable() && viewModel.device.devDisableReason == 1) {
                                ScoreHelper.showNeedMBPointDialog(context.requireContext())
                                hintEditDialog?.dismiss()
                            } else {
                                CheckStatus.checkDeviceStatus(context.requireActivity(), context.requireActivity().supportFragmentManager, viewModel.device, Function {
                                    if (it) {
                                        viewModel.setLoadingStatus(true)
                                        repository.clearNasUser(viewModel.device.id, Function {
                                            clearDeviceUser()
                                            null
                                        }, Function {
                                            viewModel.setLoadingStatus(false)
                                            ToastUtils.showToast(R.string.clear_user_data_failed_hint)
//                                            if (context != null) {
//                                                val s: String = context.getString(R.string.remove_device_failed)
//                                                ToastHelper.showToast(String.format("%s! %s", s, it))
//                                            }
                                            null
                                        }, viewModel.mStateListener)
                                    } else {//状态异常
                                        hintEditDialog?.dismiss()
                                    }
                                    null
                                }, Function {
                                    if (it) {//状态异常，且进行了下一步,点击了确定，关闭页面
                                        viewModel.toFinishActivity()
                                    }
                                    null
                                })
                            }
                        } else {
                            CheckStatus.checkDeviceStatus(context.requireActivity(), context.requireActivity().supportFragmentManager, viewModel.device, Function {
                                if (it) {
                                    clearDeviceUser()
                                } else {//状态异常
                                }
                                null
                            }, Function {
                                if (it) {//状态异常，且进行了下一步,点击了确定，关闭页面
                                    viewModel.toFinishActivity()
                                }
                                null
                            })
                        }
                    }
                }
            })
            hintEditDialog?.show(context.requireActivity().getSupportFragmentManager(), "hintEditDialog")
        }
    }

    /**
     * 其它用户移除
     */
    private fun showRemoveDialog() {
        if (hintEditDialog?.dialog?.isShowing != true) {
            val isNas = viewModel.device.isNas
            hintEditDialog?.update(
                    title = context.getString(R.string.remove_device_title),
                    content = context.getString(R.string.manager_remove_device_content),
                    contentTextColor = R.color.red,
                    editHint = context.getString(R.string.manager_remove_device_hint),
                    editContent = "",
                    matchEditToHint = true,
                    checkBoxText = if (isNas) context.getString(R.string.clear_user_data_hint) else null,
                    isCheckedBox = isNas,
                    confrimText = context.getString(R.string.confirm),
                    cancelText = context.getString(R.string.cancel)
            )
            hintEditDialog?.setOnClickListener(object : View.OnClickListener {
                override fun onClick(view: View?) {
                    if (view?.id == R.id.positive) {
                        val isCheckedBox = hintEditDialog?.isCheckedBox() ?: false
                        if (isCheckedBox) {//勾选了清除用户数据
                            if (viewModel.device.isDevDisable() && viewModel.device.devDisableReason == 1) {
                                ScoreHelper.showNeedMBPointDialog(context.requireContext())
                            } else {
                                CheckStatus.checkDeviceStatus(context.requireActivity(), context.requireActivity().supportFragmentManager, viewModel.device, Function {
                                    if (it) {
                                        viewModel.setLoadingStatus(true)
                                        val isOwner = viewModel.device.getMnglevel() == 0
                                        repository.deleteNasUser(viewModel.mStateListener, viewModel.device.id, isOwner, Function {
                                            if (it) {
                                                deleteThisDevice(null)
                                            } else {
                                                viewModel.setLoadingStatus(false)
                                            }
                                            null
                                        }, Function {
                                            if (it == null) {
                                                deleteThisDevice(null)
                                            } else {
                                                viewModel.setLoadingStatus(false)
                                                val s: String = context.getString(R.string.remove_device_failed)
                                                ToastHelper.showToast(String.format("%s! (%s)", s, it))
                                            }
                                            null
                                        })
                                    } else {//状态异常
                                    }
                                    null
                                }, Function {
                                    if (it) {//状态异常，且进行了下一步,点击了确定，关闭页面
                                        viewModel.toFinishActivity()
                                    }
                                    null
                                })

                            }
                        } else {
                            CheckStatus.checkDeviceStatus(context.requireActivity(), context.requireActivity().supportFragmentManager, viewModel.device, Function {
                                if (it) {
                                    deleteThisDevice(null)
                                }
                                null
                            }, Function {
                                if (it) {//状态异常，且进行了下一步,点击了确定，关闭页面
                                    viewModel.toFinishActivity()
                                }
                                null
                            })
                        }
                    }
                }
            })
            hintEditDialog?.show(context.requireActivity().getSupportFragmentManager(), "hintEditDialog")
        }
    }

    /**
     * 创建提示Dialog
     */
    private var hintDialog: HintDialog? = null
        get() {
            if (field == null) {
                field = HintDialog.newInstance()
                field?.onDismissListener = DialogInterface.OnDismissListener {
                    viewModel.setSecondaryDialogShow(false)
                }
                field?.onShowListener = DialogInterface.OnShowListener {
                    viewModel.setSecondaryDialogShow(true)
                }
            }
            return field
        }

    /**
     * 移除
     */
    private fun remove() {
        if (viewModel.device.mnglevel == 3) {
            hintDialog?.update(context.getString(R.string.remove),
                    context.getString(R.string.unbind_device_prompt), hintColor = R.color.red,
                    confrimText = context.getString(R.string.confirm),
                    cancelText = context.getString(R.string.cancel)
            )
            hintDialog?.setOnClickListener(View.OnClickListener {
                if (it.id == R.id.positive) {//确定按钮
                    deleteThisDevice(null)
                }
            })
            if (hintDialog?.dialog?.isShowing != true) {
                hintDialog?.show(context.requireActivity().getSupportFragmentManager(), "hintDialog")
            }
        } else {
            viewModel.device.hardData?.let {
                if (it.isOwner()) {
                    DialogUtil.showExtraSelectDialog(context.requireContext(), context.getString(R.string.msg_admin_remove_device),
                            context.getString(R.string.clear_all), OnDialogButtonClickListener { v, strEdit, dialog, isCheck ->
                        dialog.dismiss()
                        showOwnerRemoveDialog()
                        //如果是nas显示是否删除设备数据.
//                    if (isHasUserData(viewModel.device.getDevClass()))
                    },
                            context.getString(R.string.cancel), null
                            , null, null, null)
                } else if (it.isAdmin || it.isCommon) {
                    showRemoveDialog()
                } else {
                    DialogUtil.showSelectDialog(context.requireContext(), R.string.unbind_device_prompt,
                            R.string.confirm, { v, strEdit, dialog, isCheck ->
                        hideDialog = dialog
                        deleteThisDevice(dialog)
                    },
                            R.string.cancel, { v, strEdit, dialog, isCheck ->
                        dialog.dismiss()
                    })
                }
            }
        }
    }

    private fun deleteThisDevice(dialog: Dialog?) {
        repository.deleteThisDevice(viewModel.device.hardData?.deviceid!!, viewModel.mStateListener, Function {
            try {
                dialog?.let {
                    dialog.dismiss()
                    true
                } ?: let {
                    if (hintEditDialog?.dialog?.isShowing == true) hintEditDialog?.dismiss()
                    if (hintDialog?.dialog?.isShowing == true) hintDialog?.dismiss()
                }
            } catch (e: Exception) {
            }
            viewModel.sendRemoveDevBroadcast(context.requireContext().applicationContext)
            viewModel.toFinishActivity()
            null
        }, Function {
            it?.let {
                ToastUtils.showError(it)
            }
            null
        })
    }

    private fun clearDeviceUser() {
        repository.clearDeviceUser(viewModel.device.id, viewModel.mStateListener, Function {
            if (it) {
                hintEditDialog?.dismiss()
                context.activity?.setResult(FunctionHelper.DEVICE_REMOVE)
                viewModel.sendRemoveDevBroadcast(context.requireContext().applicationContext)
                viewModel.toFinishActivity()
            } else {
//                showOwnerRemoveDialog(false)
            }
            null
        })
    }

    /**
     * 重启
     */
    private fun restart() {
        with(viewModel.device) {
            if (isDevDisable() && devDisableReason == 1) {
                ScoreHelper.showNeedMBPointDialog(context.requireContext())
            } else {
                CheckStatus.checkDeviceStatus(context.requireActivity(), context.requireActivity().supportFragmentManager, this, Function {
                    if (it) {
                        DialogUtils.showConfirmDialog(context.requireContext(), R.string.tips, R.string.confirm_reboot_device, R.string.confirm, R.string.cancel) { dialog, isPositiveBtn ->
                            if (isPositiveBtn) {
                                repository.doPowerOffOrRebootDevice(false, id, vip, Function {
                                    //关闭窗口
                                    viewModel.toFinishActivity()
                                    null
                                })
                            }
                        }
                    }

                    null
                }, Function {
                    if (it) {//状态异常，且进行了下一步,点击了确定，关闭页面
                        viewModel.toFinishActivity()
                    }
                    null
                })

            }
        }

    }

    /**
     * 安全关机
     */
    private fun shutdown() {
        with(viewModel.device) {
            if (isDevDisable() && devDisableReason == 1) {
                ScoreHelper.showNeedMBPointDialog(context.requireContext())
            } else {
                CheckStatus.checkDeviceStatus(context.requireActivity(), context.requireActivity().supportFragmentManager, this, Function {
                    if (it) {
                        DialogUtils.showConfirmDialog(context.requireContext(), R.string.tips, R.string.confirm_power_off_device, R.string.confirm, R.string.cancel) { dialog, isPositiveBtn ->
                            if (isPositiveBtn) {
                                repository.doPowerOffOrRebootDevice(true, id, vip, Function {
                                    //关闭窗口
                                    viewModel.toFinishActivity()
                                    null
                                })
                            }
                        }
                    }
                    null
                }, Function {
                    if (it) {//状态异常，且进行了下一步,点击了确定，关闭页面
                        viewModel.toFinishActivity()
                    }
                    null
                })


            }
        }
    }

    fun editPrice() {
        ///不需要即时更改价格
        TrafficPriceEditViewModel().showEditView(context.requireContext(), viewModel.device.id)
    }

    private fun rename() {
        DialogUtils.showEditDialog(context.requireContext(), R.string.tip_set_device_name,
                R.string.hint_set_device_name, viewModel.device.getName(),
                R.string.max_name_length,
                R.string.confirm, R.string.cancel) { dialog, isPositiveBtn, mContentEditText ->
            if (isPositiveBtn) {
                val newName = mContentEditText.text.toString().trim { it <= ' ' }
                if (EmptyUtils.isEmpty(newName) || newName.length > 16) {
                    AnimUtils.sharkEditText(context.requireContext(), mContentEditText)
                } else {
                    val loader = SetDeviceNameHttpLoader(GsonBaseProtocol::class.java)
                    loader.setHttpLoaderStateListener(viewModel.mStateListener)
                    loader.setParams(viewModel.device.id, newName)
                    loader.executor(object : MyOkHttpListener<GsonBaseProtocol>() {
                        override fun success(tag: Any?, gsonBaseProtocol: GsonBaseProtocol) {
                            viewModel.device.setName(newName)
                            //设置名称
                            SessionManager.getInstance().getDeviceModel(viewModel.device.id)?.setDeviceName(newName)
                            DevManager.getInstance().notifyDeviceStateChanged()
                            viewModel.refreshMarkName()
                            dialog.dismiss()
                        }
                    })
                }
            }
        }
    }

    private fun editMarkName() {
        val content = menus.get(editDeviceRemarkIndex).remark
        if (!viewModel.device.isOnline()) {
            ToastUtils.showToast(R.string.device_offline)
            return
        }
        val deviceId: String = viewModel.device.id
        DialogUtils.showEditDialog(context.requireContext(), R.string.mark_name,
                R.string.mark_name, content,
                R.string.max_name_length,
                R.string.confirm, R.string.cancel) { dialog, isPositiveBtn, mContentEditText ->
            if (isPositiveBtn) {
                val newName = mContentEditText.text.toString().trim { it <= ' ' }
                if (EmptyUtils.isEmpty(newName) || newName.length > 16) {
                    AnimUtils.sharkEditText(context.requireContext(), mContentEditText)
                } else {
                    setDeviceName(newName, deviceId)
                    //设置备注名
                    SessionManager.getInstance().getDeviceModel(viewModel.device.id)?.setMarkName(newName)
                    InputMethodUtils.hideKeyboard(context.requireContext(), mContentEditText)
                    menus.get(editDeviceRemarkIndex).remark = newName
                    DevManager.getInstance().notifyDeviceStateChanged()
                    viewModel.refreshMarkName()
                    dialog.dismiss()
                }
            } else {
                InputMethodUtils.hideKeyboard(context.requireContext(), mContentEditText)
                dialog.dismiss()
            }
        }
    }

    /**
     * 设置设备名称，类似于用户给设备定义的备注
     * 如果能更新到具体设备就更新到设备上,否则的话更新到本地数据库时做好标记,代表下次连接成功设备的时候需要更新
     */
    fun setDeviceName(deviceName: String?, deviceId: String) {
        val user = SessionManager.getInstance().username
        if (user != null) {
            UserInfoKeeper.saveDevMarkInfo(user, deviceId, deviceName, null, true)
        }
        val mDeviceModel = SessionManager.getInstance().getDeviceModel(deviceId);
        if (mDeviceModel != null && mDeviceModel.devId == deviceId) {
            mDeviceModel.devName = deviceName ?: ""
            SessionManager.getInstance().getLoginSession(deviceId, object : GetSessionListener() {
                override fun onSuccess(url: String, data: LoginSession) {
                    val listener = object : OneOSDeviceInfoAPI.OnDeviceInfoListener {
                        override fun onStart(url: String) {}
                        override fun onSuccess(url: String, info: OneOSDeviceInfoAPI.SubInfo?) {
                            if (user != null) {
                                UserInfoKeeper.saveDevMarkInfo(user, deviceId, deviceName, null, false)
                            }
                            //更新UI
//                                liveDevices.postValue(deviceModels)
                        }

                        override fun onFailure(url: String, errorNo: Int, errorMsg: String) {
                            if (user != null) {
                                UserInfoKeeper.saveDevMarkInfo(user, deviceId, deviceName, null, true)
                            }
                        }
                    }

                    val observer = object : V5Observer<Any>(data.ip) {
                        override fun success(result: BaseProtocol<Any>) {
                            listener.onSuccess("", null)
                        }

                        override fun fail(result: BaseProtocol<Any>) {
                            listener.onFailure("", result.error?.code ?: 0, result.error?.msg ?: "")
                        }

                        override fun isNotV5() {
                            val oneOSDeviceInfoAPI = OneOSDeviceInfoAPI(data)
                            oneOSDeviceInfoAPI.setListener(listener)
                            oneOSDeviceInfoAPI.update(deviceName, null)
                        }

                        override fun retry(): Boolean {
                            return false
                        }
                    }
                    V5Repository.INSTANCE().setDeviceMark(data.ip, data.ip, LoginTokenUtil.getToken(), deviceName
                            ?: "", "", observer)

                }

                override fun onFailure(url: String?, errorNo: Int, errorMsg: String?) {
                    super.onFailure(url, errorNo, errorMsg)
                    if (user != null) {
                        UserInfoKeeper.saveDevMarkInfo(user, deviceId, deviceName, null, true)
                    }
                }
            })
        }
    }

    //编辑流量单价的位置，在设备名和备注名之后
    private var editUnitPriceIndex = 0

    //编辑设备备注名，在设备名之后
    private var editDeviceRemarkIndex = 0

    //设置收费项的下标位置
    private var SETTING_FEE_INDEX = -1
    fun getItemSources() {
        menus.clear()
        with(viewModel.device) {
            if (!isPendingAccept) {
                //绑定的设备、管理员权限
                val isMngr = isOwner || isAdmin
                val isSameAccount: Boolean = getUserId() == CMAPI.getInstance().baseInfo.userId

                /***设备名**********************************************************************/
                if (isOwner) {//name可编辑
                    menus.add(FunctionHelper.getDeviceMenu(FunctionHelper.EDIT_DEVICE_NAME))
                    editUnitPriceIndex++
                    editDeviceRemarkIndex++
                }
                /***备注名**********************************************************************/
                if (isOnline && isNas) {
                    val deviceModel = SessionManager.getInstance().getDeviceModel(id)
                    deviceModel?.let {
                        val subscribe: Disposable = it.devNameFromDB.subscribe(object : Consumer<String?> {
                            override fun accept(markname: String?) {
                                val menu = FunctionHelper.getDeviceMenu(FunctionHelper.EDIT_DEVICE_REMARK)
                                menu.remark = deviceModel.getRemakName()
                                menus.add(editDeviceRemarkIndex, menu)
                                editUnitPriceIndex++
                                notifyDataSetChanged()
                            }
                        })
                        addDisposable(subscribe)
                    }
                }

                /***流量单价**9/23 产品需求：暂时停用********************************************************************/
//            if (isOnline  && hardData != null) {
//                hardData?.let {
//                    if (it.isEN() && (it.getGb2cRatio() > 0 || EmptyUtils.isNotEmpty(it.getMbpointratio()))) {
//                        var currentPrice = ""
//                        currentPrice = if (it.getGb2cRatio() > 0) {
//                            it.getGb2cRatio()
//                                    .toString() + context.getString(R.string.fmt_traffic_unit_price2)
//                                    .replace("\$TRAFFIC$", MyConstants.DEFAULT_UNIT)
//                        } else {
//                            val mbpointratio: String = it.getMbpointratio()
//                            context.getString(R.string.fmt_traffic_unit_price)
//                                    .replace("\$TRAFFIC$", mbpointratio)
//                        }
//                        val isEditable = it.isOwner() && it.isChangeRatioAble()
//                        if(isEditable){
//                            val menu = FunctionHelper.getDeviceMenu(FunctionHelper.EDIT_UNIT_PRICE)
//                            menu.remark = currentPrice
//                            menus.add(editUnitPriceIndex,menu)
//                        }
//                    }
//                }
//            }

                /**成员管理**********************************************************************************/
                // //管理员 非终端设备
                if (isOnline() && (isMngr || isSameAccount))
                    menus.add(FunctionHelper.getDeviceMenu(FunctionHelper.DEVICE_MEMBER))

                /**11.9 需求去掉此逻辑
                 * 1.设备所有者或管理员可用
                 * 2.设备在当前切换的网络中
                 * 3.必须是收费的网络
                 * 4. 必须是EN服务器 getnetworkprovide获取到的都是EN服务器  enServer！＝null
                 * 或者绑定的设备(非统一收费)
                 *
                 * 5.有收费项才设置（获取列表数据 获得）
                 */
//            SETTING_FEE_INDEX = menus.size
//            if (fragmentViewModel.feeType.value?.fees?.size ?: 0 > 0) {
//                menus.add(FunctionHelper.getDeviceMenu(FunctionHelper.DEVICE_SETTING_FEES))
//            }
                /**设置收费项**********************************************************************************/
                if (isMngr && viewModel.device.isEn) {
                    menus.add(FunctionHelper.getDeviceMenu(FunctionHelper.DEVICE_SETTING_FEES))
                }
                /**更改流量费支付方 en&&有流量单价**********************************************************************************/
                var mbpointratio: String? = null
                enServer?.let {
                    mbpointratio = it.mbpointratio
                }
                hardData?.let {
                    if (EmptyUtils.isEmpty(mbpointratio)) mbpointratio = it.getMbpointratio()
                }
                if (isOwner && viewModel.device.isEn && EmptyUtils.isNotEmpty(mbpointratio)) {
                    menus.add(FunctionHelper.getDeviceMenu(FunctionHelper.ALTER_FLOW_FEE_PAYER))
                }
                /**设备切圈 owner且真实在线**********************************************************************************/
                if (isOwner && hardData?.isRealOnline == true)
                    menus.add(FunctionHelper.getDeviceMenu(FunctionHelper.DEVICE_CHANGE_CIRCLE))
                /**节点配置**********************************************************************************/
                if (isOnline() && (isMngr || isSameAccount) && isSNConfigurable())
                    menus.add(FunctionHelper.getDeviceMenu(FunctionHelper.DEVICE_NODE_CONFIG))
                /**局域网访问**********************************************************************************/
                if (isOnline() && DevTypeHelper.isOneOSWithAccessByMnet(devClass))
                    menus.add(FunctionHelper.getDeviceMenu(FunctionHelper.DEVICE_LAN_ACCESS))
                //绑定的设备、管理员权限
                if (isOnline() && (isMngr || isSameAccount)) {
                    val newVersion = getVersion().toString()
                    if (newVersion != null && UiUtils.isNewVersion(MyConstants.REMOTE_MANAGER_VERSION, newVersion)
                            && isNas()) {
                        /**远程管理**********************************************************************************/
                        menus.add(FunctionHelper.getDeviceMenu(FunctionHelper.DEVICE_REMOTE_MANAGER))
                    }
                }
                if (isOnline() && isMngr && isNas) {
                    val deviceModel = SessionManager.getInstance().getDeviceModel(getId())
                    deviceModel?.let {
                        if (DevTypeHelper.isOneOSNas(it.devClass)) {
                            /**应用管理**********************************************************************************/
                            menus.add(FunctionHelper.getDeviceMenu(FunctionHelper.DEVICE_APP_MANAGER))
                        }

                        if (!UiUtils.isAndroidTV(getDevClass())) {
                            /**设备空间**********************************************************************************/
                            menus.add(FunctionHelper.getDeviceMenu(FunctionHelper.DEVICE_SPACE))
                        }
                        //重新启动
                        menus.add(FunctionHelper.getDeviceMenu(FunctionHelper.DEVICE_RESTART))
                        if (!UiUtils.isAndroidTV(getDevClass())) {
                            /**安全关机**********************************************************************************/
                            menus.add(FunctionHelper.getDeviceMenu(FunctionHelper.DEVICE_SELF_SHUTDOWN))
                        }
                    }
                }
            }
        }

        //移除
        menus.add(FunctionHelper.getDeviceMenu(FunctionHelper.DEVICE_REMOVE))
    }

    private var compositeDisposable: CompositeDisposable? = null
    fun addDisposable(disposable: Disposable) {
        if (compositeDisposable == null) {
            compositeDisposable = CompositeDisposable()
        }
        compositeDisposable?.add(disposable)
    }

    override fun onDestory() {
        super.onDestory()
        compositeDisposable?.dispose()
        hideDialog?.dismiss()
    }
}