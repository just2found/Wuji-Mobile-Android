package net.linkmate.app.ui.activity.nasApp.deviceDetial.adapter

import android.content.Intent
import android.text.TextUtils
import android.util.Pair
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.arch.core.util.Function
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.NavController
import kotlinx.android.synthetic.main.dialog_device_item_detail.view.*
import kotlinx.android.synthetic.main.dialog_device_item_switch.view.*
import libs.source.common.livedata.Resource
import libs.source.common.livedata.Status
import net.linkmate.app.R
import net.linkmate.app.base.BaseActivity
import net.linkmate.app.ui.activity.dev.DevBriefActivity
import net.linkmate.app.ui.activity.mine.DevFlowDetailsActivity
import net.linkmate.app.ui.activity.nasApp.deviceDetial.*
import net.linkmate.app.ui.viewmodel.M8CheckUpdateViewModel
import net.linkmate.app.util.AccessDeviceTool
import net.linkmate.app.util.CheckStatus
import net.linkmate.app.util.ToastUtils
import net.linkmate.app.util.business.ReceiveScoreUtil
import net.sdvn.cmapi.CMAPI
import net.sdvn.common.repo.NetsRepo
import net.sdvn.nascommon.SessionManager
import net.sdvn.nascommon.constant.HttpErrorNo
import net.sdvn.nascommon.model.oneos.UpdateInfo
import net.sdvn.nascommon.model.oneos.api.sys.OneOSPowerAPI
import net.sdvn.nascommon.model.oneos.event.UpgradeProgress
import net.sdvn.nascommon.model.oneos.user.LoginSession
import net.sdvn.nascommon.utils.ToastHelper
import net.sdvn.nascommon.utils.Utils
import net.sdvn.nascommon.viewmodel.M3UpdateViewModel

/**设备详情
 * @author Raleigh.Luo
 * date：20/7/23 15
 * describe：
 */
class DeviceDetailAdapter(context: Fragment, fragmentViewModel: DeviceDetailViewModel,
                          viewModel: DeviceViewModel, val navController: NavController)
    : DeviceBaseAdapter<DeviceDetailViewModel>(context, fragmentViewModel, viewModel) {
    private val menus: ArrayList<FunctionHelper.DetailMenu> = ArrayList()

    init {
        getItemSources()
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int {
        return if (isSelectNodeSwitchExit() && position == 0) TYPE_SWITCH else TYPE_DEFALUT
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
        val node: Int = if (isSelectNodeSwitchExit()) 1 else 0
        return menus.size + node
    }

    /**
     * 选择节点开关是否存在
     */
    private fun isSelectNodeSwitchExit(): Boolean {
        return viewModel.device.isVNode || viewModel.device.getSelectable()
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
                    if (isSelectNodeSwitchExit()) index = position - 1
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

    var viewSubnodDialog: ViewSubnodeDialog? = null

    /**
     * 没有被外部监听器拦截，内部处理点击事件
     * @param position 位置
     */
    override fun internalItemClick(view: View, position: Int) {
        if (Utils.isFastClick(view)) return
//        0->//选择节点
        val function = view.getTag() as Int
        when (function) {
            FunctionHelper.DEVICE_SCORE -> {
                //领取积分
                viewModel.device.getHardData()?.getGainmbp_url()?.let {
//                    startScoreActivity(view.context, it)
                    ReceiveScoreUtil.showReceiveScoreDialog(context.requireContext(),
                            viewModel.device.getHardData()?.deviceid,
                            null)
                }
            }
            FunctionHelper.DEVICE_FLOW -> {
                // 设备流量明细
                startFlowDetails()
            }
            FunctionHelper.FORMATE_UPGRADE_FIRMWARE -> {//固件升级
                upgradeFirmware()
            }
            FunctionHelper.VIEW_SUBNODE -> {
                if (viewSubnodDialog == null) {
                    viewSubnodDialog = ViewSubnodeDialog(context.requireContext())
                    viewSubnodDialog?.device = viewModel.device
                }
                viewSubnodDialog?.show()
            }
            FunctionHelper.DEVICE_BRIEF -> {//简介
                //1.检查设备是否在线
                if (!viewModel.device.isOnline) {//设备离线
                    ToastUtils.showToast(R.string.circle_main_en_server_offline)
                } else {//设备在线
                    //2.检查设备状态是否正常
                    CheckStatus.checkDeviceStatus(context.requireActivity(), context.requireActivity().supportFragmentManager,
                            viewModel.device, Function {// true ：状态回调 状态正常，按原逻辑走，false ：状态异常
                        if (it) {//状态正常
                            viewModel.device.getId()?.let {
                                viewModel.checkDeviceFormat(FunctionHelper.DEVICE_BRIEF)
                            }
                        }
                        null
                    }, Function {//下一步回调，点击了弹框按钮 是／否
                        if (it) {//去购买流量，就关闭页面
                            viewModel.toFinishActivity()
                        }
                        null
                    })
                }
            }
            else -> {
                navController.navigate(DeviceDetailFragmentDirections.enterDetial(function))
            }
        }
    }


    override fun internalItemLongClick(view: View, position: Int) {
    }


    /**
     * 设备流量明细
     */
    private fun startFlowDetails() {
        val intent = Intent(context.requireContext(), DevFlowDetailsActivity::class.java)
        if (!TextUtils.isEmpty(viewModel.device.getId())) {
            intent.putExtra("checkedDevId", viewModel.device.getId())
        }
        if (!TextUtils.isEmpty(viewModel.device.getName())) {
            intent.putExtra("checkedDevName", viewModel.device.getName())
        }
        context.startActivity(intent)
    }

    //是否有领取积分项
    private var isDeviceScoreFunctionExit = false
    private var isUpdateFirmwareFunctionExit = false

    fun addUpdateFirmwareItem() {
        if (!isUpdateFirmwareFunctionExit) {
            isUpdateFirmwareFunctionExit = true
            if (menus.size > 0) {
                if (isDeviceScoreFunctionExit) {
                    //有领取积分，固件升级,加到倒数第二位置
                    if (menus.get(menus.size - 2).function != FunctionHelper.FORMATE_UPGRADE_FIRMWARE) {
                        menus.add(menus.size - 2, FunctionHelper.getDeviceMenu(FunctionHelper.FORMATE_UPGRADE_FIRMWARE))
                    }
                } else {//添加到最后
                    if (menus.get(menus.size - 1).function != FunctionHelper.FORMATE_UPGRADE_FIRMWARE) {
                        menus.add(FunctionHelper.getDeviceMenu(FunctionHelper.FORMATE_UPGRADE_FIRMWARE))
                    }
                }
                notifyDataSetChanged()
            }
        }

    }

    private var mUpdateFirmwareDialog: UpdateFirmwareDialog? = null
    private fun upgradeFirmware() {
        fragmentViewModel.mUpdateInfo.value?.data?.let {
            update(it)
        }
        fragmentViewModel.mUpdateInfoM8.value?.let {
            val deviceModel = SessionManager.getInstance().getDeviceModel(viewModel.device.getId())
            if (deviceModel != null) {
                val model = M8CheckUpdateViewModel()
                model.showDeviceItemUpgradeDetail(context.requireContext(), Pair(deviceModel, it))
                model.updateResultsLiveData.observe(context, Observer {
                    viewModel.toFinishActivity()
                })
            }
        }
    }


    fun update(data: UpdateInfo) {
        val mM3UpdateViewModel = M3UpdateViewModel()
        mM3UpdateViewModel.update(context.requireContext(), viewModel.device.id, data,viewModel.mStateListener).observeForever(Observer<Resource<Boolean?>> { (status) ->
            if (status === Status.SUCCESS) {
                viewModel.setLoadingStatus(false)
                if (mUpdateFirmwareDialog == null) {
                    mUpdateFirmwareDialog = UpdateFirmwareDialog(context.requireContext())
                }
                mUpdateFirmwareDialog?.show()
                mM3UpdateViewModel.subUpgradeProgress().observeForever(Observer<Resource<UpgradeProgress?>> { (status1, upgradeProgress) ->
                    if (status1 === Status.SUCCESS) {
                        val deviceModel = SessionManager.getInstance().getDeviceModel(viewModel.device.id)
                        if (upgradeProgress != null && "download".equals(upgradeProgress.name, ignoreCase = true)) {
                            if (upgradeProgress.percent >= 0) mUpdateFirmwareDialog?.setProgress(upgradeProgress.percent)
                            else {
                                mUpdateFirmwareDialog?.dismiss()
                                ToastHelper.showToast(R.string.device_upgrade_failed_by_download)
                            }
                        }
                        if (upgradeProgress != null && "install".equals(upgradeProgress.name, ignoreCase = true)) {
                            if (upgradeProgress.percent >= 0) {
                                mUpdateFirmwareDialog?.setInstallProgress(upgradeProgress.percent)
                                if (upgradeProgress.percent == 100) {
                                    val listener = object : OneOSPowerAPI.OnPowerListener {
                                        override fun onSuccess(url: String?, isPowerOff: Boolean) {
                                            if (isPowerOff)
                                                ToastHelper.showToast(net.sdvn.nascommonlib.R.string.success_power_off_device)
                                            else
                                                ToastHelper.showToast(net.sdvn.nascommonlib.R.string.success_reboot_device)
                                            mUpdateFirmwareDialog?.dismiss()
                                            viewModel.toFinishActivity()
                                        }

                                        override fun onFailure(url: String?, errorNo: Int, errorMsg: String?) {
                                            ToastHelper.showToast(HttpErrorNo.getResultMsg(false, errorNo, errorMsg))
                                        }

                                        override fun onStart(url: String?) {
                                        }
                                    }
                                    mM3UpdateViewModel.doPowerOffOrRebootDevice(false, viewModel.device.id, listener)
                                    if (deviceModel != null) {
                                        val loginSession: LoginSession? = deviceModel?.loginSession
                                        loginSession?.let {
                                            val oneOSInfo = loginSession.oneOSInfo
                                            if (oneOSInfo != null) {
                                                if (deviceModel.isOnline && deviceModel.isOwner) {
                                                    oneOSInfo.isNeedsUp = false
                                                }
                                            }
                                        }
                                    }
                                    viewModel.toFinishActivity()
                                }
                            } else {
                                mUpdateFirmwareDialog?.dismiss()
                                ToastHelper.showToast(R.string.device_upgrade_failed_by_install)
                            }
                        }
                    }
                })
            } else if (status === Status.ERROR) {
                viewModel.setLoadingStatus(false)
            } else if (status === Status.LOADING) {
                viewModel.setLoadingStatus(true)
            }
        })
    }

    fun getItemSources() {
        menus.clear()
        with(viewModel.device) {
            //等待同意的设备显示到资源列表、设备管理列表中，作离线表示，并在简介位置显示“等待同意”
            //点击后允许操作的选项：
            //1.状态
            //2.设置-移除，移除时弹窗询问“你确定要移除此设备吗？/确定/取消”
            /**状态**********************************************************************************/
            menus.add(FunctionHelper.getDeviceMenu(FunctionHelper.DEVICE_STATUS))
            if (isPendingAccept) {//等待同意
                /**控制面板-设置**********************************************************************************/
                menus.add(FunctionHelper.getDeviceMenu(FunctionHelper.DEVICE_CONTROL))
            } else {
                if (isVNode()) {
                    menus.add(FunctionHelper.getDeviceMenu(FunctionHelper.VIEW_SUBNODE))
                } else {
//        } else if (viewModel.deviceBoundType == DevBoundType.ALL_BOUND_DEVICES) {
                    if (isOnline()) {
                        if (isNas) {
                            menus.add(FunctionHelper.getDeviceMenu(FunctionHelper.DEVICE_BRIEF))
                        }
                        //绑定的设备、管理员权限
                        val isMngr = isOwner || isAdmin
                        val isSameAccount: Boolean = getUserId() == CMAPI.getInstance().baseInfo.userId
                        // //非终端设备  2020/12/30 产品需求：且没有提供EN服务
                        val isNotENServer = if (inNetwork(CMAPI.getInstance().baseInfo.netid)) enServer == null else (hardData?.isSrcProvide
                                ?: false) == false
                        if (isMngr && isNotENServer) {
                            /**分享设备**********************************************************************************/
                            menus.add(FunctionHelper.getDeviceMenu(FunctionHelper.DEVICE_SHARE))
                        }
                    }
                    networks?.let {
                        if (it.size > 0) {
                            /**所处网络**********************************************************************************/
                            menus.add(FunctionHelper.getDeviceMenu(FunctionHelper.DEVICE_NETWORK))
                        }
                    }

                    if (viewModel.device.getHardData()?.isEN() ?: false && isOwner) {
                        /**流量明细**********************************************************************************/
                        menus.add(FunctionHelper.getDeviceMenu(FunctionHelper.DEVICE_FLOW))
                    }

                    if (isOnline()) {
                        subNets?.let {
                            if (it.size > 0) {
                                /**子网**********************************************************************************/
                                menus.add(FunctionHelper.getDeviceMenu(FunctionHelper.DEVICE_SUBNET))
                            }
                        }
                    }
                    if (isUpdateFirmwareFunctionExit) {
                        menus.add(FunctionHelper.getDeviceMenu(FunctionHelper.FORMATE_UPGRADE_FIRMWARE))
                    }

                    val currentNetwork = NetsRepo.getCurrentNet()

                    /** 设备购买流量显示
                     * 1.设备在当前切换的网络中
                     * 2.必须是收费的网络
                     * 3.必须设备单独收费
                     * 4.必须是En服务器
                     * 5.是所有者 或者 加入状态正常(非未订阅加入状态，不显示购买流量)
                     */
                    viewModel.device.enServer?.let {
                        if (viewModel.device.isOnline && currentNetwork?.isCharge ?: false
                                && currentNetwork?.isDevSepCharge ?: false == true
                                && (isOwner || viewModel.device.enServer?.joinStatus != -1)) {//
                            //购买流量
                            menus.add(FunctionHelper.getDeviceMenu(FunctionHelper.DEVICE_PURCHASE_FLOW))
                        }
                    }

                    viewModel.device.hardData?.let {
                        /**控制面板-设置**********************************************************************************/
                        menus.add(FunctionHelper.getDeviceMenu(FunctionHelper.DEVICE_CONTROL))
                        if (!TextUtils.isEmpty(it.getGainmbp_url())) {
                            /**领取积分**********************************************************************************/
                            menus.add(FunctionHelper.getDeviceMenu(FunctionHelper.DEVICE_SCORE))
                            isDeviceScoreFunctionExit = true
                        }
                    }
                }
            }
        }
    }

    override fun onDestory() {
        super.onDestory()
        if (mUpdateFirmwareDialog?.isShowing == true) mUpdateFirmwareDialog?.dismiss()
        if (viewSubnodDialog?.isShowing == true) viewSubnodDialog?.dismiss()

    }
}