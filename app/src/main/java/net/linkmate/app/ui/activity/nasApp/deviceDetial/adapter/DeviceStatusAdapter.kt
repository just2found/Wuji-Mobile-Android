package net.linkmate.app.ui.activity.nasApp.deviceDetial.adapter

import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.functions.Consumer
import kotlinx.android.synthetic.main.dialog_device_item_status.view.*
import net.linkmate.app.R
import net.linkmate.app.base.MyConstants
import net.linkmate.app.manager.DevManager
import net.linkmate.app.ui.activity.mine.IdentifyCodeActivity
import net.linkmate.app.ui.activity.nasApp.deviceDetial.DeviceViewModel
import net.linkmate.app.util.FormatUtils
import net.linkmate.app.util.ToastUtils
import net.linkmate.app.util.business.PrivilegeUtil
import net.linkmate.app.util.business.VIPDialogUtil
import net.sdvn.cmapi.CMAPI
import net.sdvn.cmapi.util.ClipboardUtils
import net.sdvn.common.internet.core.GsonBaseProtocol
import net.sdvn.common.internet.core.HttpLoader
import net.sdvn.common.internet.listener.ResultListener
import net.sdvn.common.internet.protocol.AccountPrivilegeInfo
import net.sdvn.nascommon.SessionManager
import net.sdvn.nascommon.utils.EmptyUtils
import java.math.BigDecimal


/**设备状态
 * @author Raleigh.Luo
 * date：20/7/23 19
 * describe：
 */
class DeviceStatusAdapter(context: Fragment, fragmentViewModel: DeviceStatusViewModel,
                          viewModel: DeviceViewModel)
    : DeviceBaseAdapter<DeviceStatusViewModel>(context, fragmentViewModel, viewModel) {
    private val sources: ArrayList<StatusMenu> = ArrayList()

    //设备名
    private var DEVICE_NAME = -1

    //备注名
    private var DEVICE_REMARK = -1

    //设备识别码
    private var DEVICE_IDENTIFY_CODE = -1

    //所有者
    private var DEVICE_OWNER = -1

    //流量单价
    private var DEVICE_FLOW_UNIT_PRICE = -1

    //流量费支付方
    private var DEVICE_FLOW_FEE_PAYER = -1

    //虚拟网ip
    private var DEVICE_VIRTUAL_IP = -1

    //局域网ip
    private var DEVICE_LAN_IP = -1

    //域名
    private var DEVICE_DOMAIN = -1
    private var DEVICE_SN = -1

    //版本号
    private var DEVICE_VERSION_CODE = -1

    //添加时间
    private var DEVICE_ADD_TIME = -1

    //云设备宽带
    private var CLOUD_DEVICE_BROADBAND = -1

    //云设备状态
    private var CLOUD_DEVICE_STATUS = -1

    init {
        fragmentViewModel.deviceId = viewModel.device.id
        getItemSources()
        fragmentViewModel.enableCloudDeviceResult.observe(context, Observer {
            if (it) {
                //1-正常 2-管理员停用 4-已到期 5-解绑停用 6-欠费停用 7-用户停用(New)
//                viewModel.device.hardData?.devicestatus =
//                        if (fragmentViewModel.startEnableCloudDevice.value == true) {
//                            1
//                        } else {
//                            7
//                        }
                DevManager.getInstance().refreshCloudDevices(object : HttpLoader.HttpLoaderStateListener {
                    override fun onLoadComplete() {
                        viewModel.setLoadingStatus(false)
                        viewModel.toFinishActivity()
                    }

                    override fun onLoadStart(disposable: Disposable?) {
                    }

                    override fun onLoadError() {
                        viewModel.setLoadingStatus(false)
                        viewModel.toFinishActivity()
                    }
                })
            } else {
                viewModel.setLoadingStatus(false)
                notifyDataSetChanged()
            }
        })
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
                LayoutInflater.from(context.requireContext()).inflate(R.layout.dialog_device_item_status, null, false)
        view.layoutParams =
                ConstraintLayout.LayoutParams(
                        ConstraintLayout.LayoutParams.MATCH_PARENT,
                        ConstraintLayout.LayoutParams.WRAP_CONTENT
                )
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return sources.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        with(holder.itemView) {
            tvStatusTitle.text = sources[position].title
            tvStatusContent.text = sources[position].content
            vRedDot.visibility = View.GONE
            if (sources[position].function == CLOUD_DEVICE_STATUS && (viewModel.device.hardData?.devicestatus == 1
                            || (viewModel.device.hardData?.devicestatus == 7 && viewModel.device.hardData?.isEnable == true))) {
                //必须是1-正常 或7-用户停用(必须enable == true) 状态才显示switch按钮
                mStatusSwitch.visibility = View.VISIBLE
                mStatusSwitch.isChecked = !(viewModel.device.hardData?.devicestatus == 7)
                mStatusSwitch.setOnCheckedChangeListener { buttonView, isChecked ->
                    viewModel.setLoadingStatus(true)
                    fragmentViewModel.startEnableCloudDevice(isChecked)
                }
            } else {
                mStatusSwitch.visibility = View.GONE
                mStatusSwitch.setOnCheckedChangeListener(null)
            }

//            if(sources[position].isEditable){
//                ivStatusEdit.visibility = View.VISIBLE
//                ivStatusEdit.setTag(sources[position].function)
//                ivStatusEdit.setOnClickListener {
//                    //是否已经被拦截处理
//                    val isInterceptor = onItemClickListener?.onClick(it, position) ?: false
//                    //没有拦截，则可以内部处理
//                    if (!isInterceptor) internalItemClick(it, position)
//                }
//
//            }else{
            if (sources[position].function == DEVICE_IDENTIFY_CODE) {
                ivStatusEdit.visibility = View.VISIBLE
                if (sources[position].function == DEVICE_IDENTIFY_CODE) {
                    ivStatusEdit.setImageResource(R.drawable.icon_qr_code)
                    val padding = context.resources.getDimensionPixelSize(R.dimen.common_18)
                    ivStatusEdit.setPaddingRelative(padding, ivStatusEdit.paddingTop, ivStatusEdit.paddingEnd, ivStatusEdit.paddingBottom)
                }
            } else {
                ivStatusEdit.visibility = View.GONE
            }

//            if (sources[position].function == DEVICE_FLOW_UNIT_PRICE) {
//                viewModel.device.hardData?.let {
//                    val hasNewMessage = it.isChangeRatioAble()
//                            || isShouldShowTrafficTips(it.deviceid)
//                    if (hasNewMessage) {
//                        vRedDot.visibility = View.VISIBLE
//                        vRedDot.setTag(sources[position].function)
//                        vRedDot.setOnClickListener {
//                            //是否已经被拦截处理
//                            val isInterceptor = onItemClickListener?.onClick(it, position) ?: false
//                            //没有拦截，则可以内部处理
//                            if (!isInterceptor) internalItemClick(it, position)
//                        }
//                    }
//                }
////                }
//            }

            setOnClickListener(View.OnClickListener {
                //是否已经被拦截处理
                val isInterceptor = onItemClickListener?.onClick(it, position) ?: false
                //没有拦截，则可以内部处理
                if (!isInterceptor) internalItemClick(it, position)
            })

        }
    }

    override fun internalItemClick(view: View, position: Int) {
        if (sources[position].function == DEVICE_IDENTIFY_CODE) {
            val deviceName = fragmentViewModel.viewStatusParams.value?.headerTitle
                    ?: viewModel.device.name
            IdentifyCodeActivity.startDeviceIdCode(context.requireContext(), viewModel.device, deviceName)
        } else {
            clipString(sources[position].content ?: "")
        }
    }

    private fun clipString(content: String) {
        if (context != null) {
            ClipboardUtils.copyToClipboard(context.requireContext(), content)
            ToastUtils.showToast(context.getString(R.string.Copied).toString() + content)
        }
    }

    override fun internalItemLongClick(view: View, position: Int) {
    }


    fun getItemSources() {
        var index = 0
        sources.clear()
        DEVICE_NAME = index++
        val nameMenu = getMenuByFunction(DEVICE_NAME)
        sources.add(nameMenu)
        with(viewModel.device) {
            if (isVNode) {
                initVNodeItem()
            } else if (typeValue == 3) {//云设备
                /***设备识别码,必须是所有者才显示设备识别码**********************************************************************/
                DEVICE_IDENTIFY_CODE = index++
                sources.add(getMenuByFunction(DEVICE_IDENTIFY_CODE))
                /***所有者**********************************************************************/
                DEVICE_OWNER = index++
                sources.add(getMenuByFunction(DEVICE_OWNER))
                /***sn**********************************************************************/
                DEVICE_SN = index++
                val snItem = getMenuByFunction(DEVICE_SN)
                snItem.content = hardData?.devicesn
                sources.add(snItem)
                /***带宽**********************************************************************/
                CLOUD_DEVICE_BROADBAND = index++
                sources.add(getMenuByFunction(CLOUD_DEVICE_BROADBAND))
                /***状态**********************************************************************/
                CLOUD_DEVICE_STATUS = index++
                sources.add(getMenuByFunction(CLOUD_DEVICE_STATUS))
            } else {
                /***设备名**********************************************************************/
                if (isOwner) {//name可编辑
                    nameMenu.isEditable = true
                }
                /***备注名**********************************************************************/
                if (isOnline && isNas) {
                    val deviceModel = SessionManager.getInstance().getDeviceModel(id)
                    deviceModel?.let {
                        DEVICE_REMARK = index++
                        sources.add(getMenuByFunction(DEVICE_REMARK, true))
                        val subscribe: Disposable = it.devNameFromDB.subscribe(object : Consumer<String?> {
                            override fun accept(markname: String?) {
                                sources.get(DEVICE_REMARK).content = deviceModel.getRemakName()
                                notifyItemChanged(DEVICE_REMARK)
                            }
                        })
                        addDisposable(subscribe)
                    }
                }
                /***设备识别码,必须是所有者才显示设备识别码**********************************************************************/
                if (isOwner) {
                    DEVICE_IDENTIFY_CODE = index++
                    sources.add(getMenuByFunction(DEVICE_IDENTIFY_CODE))
                }
                /***所有者**********************************************************************/
                DEVICE_OWNER = index++
                sources.add(getMenuByFunction(DEVICE_OWNER))
                /***流量单价 hardData/enServer 两者有其一**********************************************************************/

                var mbpointratio: String? = null
                enServer?.let {
                    mbpointratio = it.mbpointratio
                }
                hardData?.let {
                    if (EmptyUtils.isEmpty(mbpointratio)) mbpointratio = it.getMbpointratio()
                }

                if (EmptyUtils.isNotEmpty(mbpointratio)) {
                    var currentPrice = ""
                    currentPrice = mbpointratio
                            .toString() + context.getString(R.string.fmt_traffic_unit_price2)
                            .replace("\$TRAFFIC$", MyConstants.DEFAULT_UNIT)
//                        val isEditable = it.isOwner() && it.isChangeRatioAble()
                    val isEditable = false
                    DEVICE_FLOW_UNIT_PRICE = index++
                    val flowMenu = getMenuByFunction(DEVICE_FLOW_UNIT_PRICE, isEditable)
                    flowMenu.content = currentPrice
                    sources.add(flowMenu)


                }

                /***流量费支付方 en&&有流量单价**********************************************************************/
                if (EmptyUtils.isNotEmpty(mbpointratio) && viewModel.device.isEn) {
                    DEVICE_FLOW_FEE_PAYER = index++
                    sources.add(getMenuByFunction(DEVICE_FLOW_FEE_PAYER))
                }

                if (isOnline) {//设备在线
                    //绑定的设备、管理员权限
                    /***虚拟网IP**********************************************************************/
                    DEVICE_VIRTUAL_IP = index++
                    sources.add(getMenuByFunction(DEVICE_VIRTUAL_IP))
                    /***局域网IP**********************************************************************/
                    val isMngr = isOwner || isAdmin
                    val isSameAccount: Boolean = isSameAccount()
                    if (isMngr || isSameAccount) {
                        DEVICE_LAN_IP = index++
                        sources.add(getMenuByFunction(DEVICE_LAN_IP))
                    }

                    /***域名**********************************************************************/
                    DEVICE_DOMAIN = index++
                    sources.add(getMenuByFunction(DEVICE_DOMAIN))
                    /***版本号**********************************************************************/
                    DEVICE_VERSION_CODE = index++
                    sources.add(getMenuByFunction(DEVICE_VERSION_CODE))
                }

                if (hardData != null) {
                    /***sn**********************************************************************/
                    if (isOwner || isAdmin) {
                        DEVICE_SN = index++
                        sources.add(getMenuByFunction(DEVICE_SN))
                    }
                    /***添加时间**********************************************************************/
                    DEVICE_ADD_TIME = index++
                    sources.add(getMenuByFunction(DEVICE_ADD_TIME))
                } else {

                }
            }
        }
    }

    private fun initVNodeItem() {
        val vnodeid: MutableList<String> = java.util.ArrayList()
        vnodeid.add(viewModel.device.getId())
        PrivilegeUtil.getVNodeInfo(vnodeid, viewModel.mStateListener, object : ResultListener<AccountPrivilegeInfo> {
            override fun success(tag: Any?, data: AccountPrivilegeInfo) {
                val vnodesBean = data.data.vnodes[0]
                sources.add(StatusMenu(context.getString(R.string.valid), VIPDialogUtil.getDateString(vnodesBean.expired), function = -1))
                var isBit = false
                if (vnodesBean.units != null) {
                    if (vnodesBean.units.endsWith("b")) {
                        isBit = true
                    }
                    sources.add(StatusMenu(context.getString(R.string.total_traffic), FormatUtils.getSizeFormat(vnodesBean.flowUsable, isBit), function = -1))
                    sources.add(StatusMenu(context.getString(R.string.flow_used), FormatUtils.getSizeFormat(vnodesBean.flowUsed, isBit), function = -1))
                }
                notifyDataSetChanged()
            }

            override fun error(tag: Any?, baseProtocol: GsonBaseProtocol) {}
        })
    }

    private fun isSameAccount(): Boolean {
        return viewModel.device.getUserId() == CMAPI.getInstance().baseInfo.userId
    }

    private var compositeDisposable: CompositeDisposable? = null
    fun addDisposable(disposable: Disposable) {
        if (compositeDisposable == null) {
            compositeDisposable = CompositeDisposable()
        }
        compositeDisposable?.add(disposable)
    }

    private fun getMenuByFunction(function: Int, isEditable: Boolean = false): StatusMenu {
        val menu = StatusMenu(function = function, isEditable = isEditable)
        when (function) {
            DEVICE_NAME -> {
                menu.title = context.getString(R.string.dev_name)
                menu.content = viewModel.device.name
            }
            DEVICE_REMARK -> {
                menu.title = context.getString(R.string.mark_name)
            }
            DEVICE_OWNER -> {
                menu.title = context.getString(R.string.owner)
                var name = viewModel.device.hardData?.nickname
                if (TextUtils.isEmpty(name)) name = viewModel.device.ownerName
                menu.content = name
            }
            DEVICE_FLOW_UNIT_PRICE -> {
                menu.title = context.getString(R.string.flow_unit_price)
                menu.content = viewModel.device.vip
            }
            DEVICE_VIRTUAL_IP -> {
                menu.title = context.getString(R.string.vip)
                menu.content = viewModel.device.vip
            }
            DEVICE_LAN_IP -> {
                menu.title = context.getString(R.string.lanIp)
                menu.content = viewModel.device.priIp
            }
            DEVICE_DOMAIN -> {
                menu.title = context.getString(R.string.domain)
                menu.content = viewModel.device.domain
            }
            DEVICE_SN -> {
                menu.title = "SN"
                menu.content = viewModel.device.hardData?.devicesn
            }
            DEVICE_VERSION_CODE -> {
                menu.title = context.getString(R.string.version_no)
                menu.content = viewModel.device.appVersion
            }
            DEVICE_ADD_TIME -> {
                menu.title = context.getString(R.string.bind_time)
                menu.content = viewModel.device.hardData?.datetime
            }
            DEVICE_IDENTIFY_CODE -> {
                menu.title = context.getString(R.string.identify_code)
                menu.content = context.getString(R.string.strNotificationClickToView)
                menu.isEditable = true
            }
            CLOUD_DEVICE_BROADBAND -> {//云设备带宽
                menu.title = context.getString(R.string.cloud_device_broadband)
                var calcvalue = viewModel.device.hardData?.calcvalue ?: BigDecimal(0)
                // //保留4位小数字
                calcvalue = calcvalue.setScale(4, BigDecimal.ROUND_HALF_DOWN);
                val calcmode = Math.max(viewModel.device.hardData?.calcmode ?: 0, 0)
                //去小数多余的0
                val value = calcvalue.multiply(BigDecimal(calcmode))
                val result = value.stripTrailingZeros().toPlainString()
                menu.content = "$result Mb/s"
            }
            DEVICE_FLOW_FEE_PAYER -> {//流量费付费方
                menu.title = context.getString(R.string.flow_fee_payer)
                val chargetype = viewModel.device.hardData?.chargetype ?: 1
                //收费方式 1-使用者付费 2-拥有者付费
                menu.content = context.getString(if (chargetype == 2) R.string.owner else R.string.user)
            }
            CLOUD_DEVICE_STATUS -> {//云设备状态
                menu.title = context.getString(R.string.status)
                val chargetype = viewModel.device.hardData?.chargetype ?: 1
                //设备状态 1-正常 2-停用 5-解绑停用 6-欠费停用
                val devicestatus = viewModel.device.hardData?.devicestatus ?: 0
                var text = context.getString(R.string.normal)
                when (devicestatus) {
                    2 -> {//停用
                        text = context.getString(R.string.cloud_device_status_stop)
                    }
                    4 -> {//已到期
                        text = context.getString(R.string.cloud_device_expired)
                    }
                    5 -> {//解绑停用
                        text = context.getString(R.string.cloud_device_status_unbounded)
                    }
                    6 -> {//欠费停用
                        text = context.getString(R.string.ec_insufficient_score)
                    }
                    7 -> {//禁用
                        text = context.getString(R.string.forbidden)
                    }
                }
                menu.content = text
            }
        }
        return menu
    }

    class StatusMenu(var title: String? = null, var content: String? = null, var isEditable: Boolean = false
                     , var function: Int = 0)

    override fun onDestory() {
        super.onDestory()
        compositeDisposable?.dispose()
    }


}