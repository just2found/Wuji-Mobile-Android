package net.linkmate.app.view.adapter

import android.text.TextUtils
import android.util.Log
import android.view.View
import com.bumptech.glide.Glide
import com.chad.library.adapter.base.BaseMultiItemQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.rxjava.rxlife.RxLife
import kotlinx.android.synthetic.main.item_home_device.view.*
import kotlinx.android.synthetic.main.item_home_device_title.view.*
import kotlinx.android.synthetic.main.item_home_simplestyle.view.*
import net.linkmate.app.R
import net.linkmate.app.base.DevBoundType
import net.linkmate.app.base.MyConstants
import net.linkmate.app.bean.DeviceBean
import net.linkmate.app.ui.simplestyle.home.HomeViewModel
import net.linkmate.app.ui.viewmodel.DevCommonViewModel
import net.linkmate.app.util.Dp2PxUtils
import net.sdvn.cmapi.CMAPI
import net.sdvn.cmapi.global.Constants
import net.sdvn.common.ErrorCode
import net.sdvn.nascommon.SessionManager
import net.sdvn.nascommon.model.UiUtils
import net.sdvn.nascommon.utils.SPUtils

class HomeDevRVAdapter(data: MutableList<DeviceBean>?, private val viewModel: HomeViewModel,
                       private val mDevCommonViewModel: DevCommonViewModel, private var devBoundType: Int) : BaseMultiItemQuickAdapter<DeviceBean, BaseViewHolder>(data) {

    protected override fun convert(helper: BaseViewHolder, data: DeviceBean) {
        if (data == null) {
            return
        }
        with(helper.itemView) {
            if (data.itemType == 1) {
                var typeName: String? = ""
                when (data.mnglevel) {
                    0 -> typeName = mContext.getString(R.string.edge_node)
                    1 -> typeName = mContext.getString(R.string.devices)
                    2 -> typeName = mContext.getString(R.string.clients)
                    3 -> typeName = mContext.getString(R.string.cloud_device)
                }
                ihdt_tv_title.setText(typeName)
            } else if (data.itemType == 2) {
                val isSelect = (data.selectable || data.isVNode) &&
                        CMAPI.getInstance().baseInfo.hadSelectedSn(data.id)
                val hasSubnet = data.feature and Constants.DF_ACCESS_SUBNET != 0 && data.subNets != null && data.subNets.size > 0
                helper.setGone(R.id.ihd_iv_trans, false)
                        .setGone(R.id.ihd_iv_message, false)
                        .setGone(R.id.ihd_iv_internet, data.selectable || data.isVNode)
                        .setGone(R.id.ihd_iv_subnet, hasSubnet)
                        .setGone(R.id.ihd_iv_high, data.dlt != null && data.dlt.clazz > 0)
                        .setImageResource(R.id.ihd_iv_internet, if (isSelect) R.drawable.icon_net_selected else R.drawable.icon_net_selectable)
                        .setVisible(R.id.ihd_tv_local, data.hardData != null && !TextUtils.isEmpty(data.hardData!!.location) && UiUtils.isCN())
                        .setGone(R.id.ihd_iv_dig, data.hardData != null && data.hardData!!.isEN)

                var hasError = false
                var isNeedsUp = false
                if (getTag() != data.id) {
                    ihd_img_icon.setTag(null)
                    ihd_tv_name.setTag(null)
                    setTag(data.id)
                }
                //待同意的统一显示为设备名
                //是否等待同意，需使用hardData
                val isPendingAccept = data.isPendingAccept
                if (data.isNas && !isPendingAccept && SPUtils.getBoolean(MyConstants.SP_SHOW_REMARK_NAME, true)) {
                    val deviceModel = SessionManager.getInstance()
                            .getDeviceModel(data.id)
                    if (deviceModel != null) {
                        if (ihd_tv_name.getTag() == null) ihd_tv_name.setText(deviceModel.devName)
                        deviceModel.devNameFromDB
                                .`as`(RxLife.`as`(helper.itemView))
                                .subscribe({ s: String? ->
                                    if (ihd_tv_name.getTag() != s) {
                                        ihd_tv_name.setText(s)
                                    }
                                    ihd_tv_name.setTag(ihd_tv_name.text.toString())
                                }) { throwable: Throwable? -> }
                        val loginSession = deviceModel.loginSession
                        if (loginSession != null) {
                            val oneOSInfo = loginSession.oneOSInfo
                            if (loginSession.isLogin) hasError = !loginSession.isHDStatusEnable
                            if (deviceModel.isOwner && deviceModel.isOnline) {
                                isNeedsUp = oneOSInfo != null && oneOSInfo.isNeedsUp
                                hasError = loginSession.hdCount == 0 || loginSession.hdError > 0
                            }
                        }
                    } else {
                        val text = data.name.trim { it <= ' ' }
                        if (ihd_tv_name.getTag() != text) {
                            ihd_tv_name.setText(text)
                            ihd_tv_name.setTag(text)
                        }
                    }
                } else {
                    val text = data.name.trim { it <= ' ' }
                    if (ihd_tv_name.getTag() != text) {
                        ihd_tv_name.setText(text)
                        ihd_tv_name.setTag(text)
                    }
                }
                helper.setGone(R.id.ihd_iv_upgrade, isNeedsUp)
                helper.setGone(R.id.iv_icon_status, hasError && !UiUtils.isAndroidTV(data.devClass))
                if (data.hardData != null && !TextUtils.isEmpty(data.hardData!!.location)) {
                    helper.setText(R.id.ihd_tv_local, data.hardData!!.location)
                }
                var name = data.hardData?.nickname
                if (TextUtils.isEmpty(name)) name = data.ownerName

                if (devBoundType == DevBoundType.ALL_BOUND_DEVICES) {//设备管理
                    helper.setGone(R.id.ihd_iv_more, false)
                    helper.setGone(R.id.ihd_ll_status, false)
                    helper.setText(R.id.ihd_tv_owner, name)
                            .setText(R.id.ihd_tv_vip, if (devBoundType == DevBoundType.ALL_BOUND_DEVICES) "" else data.vip)
                            .setTextColor(R.id.ihd_tv_name, mContext.resources.getColor(R.color.text_dark))
                            .setTextColor(R.id.ihd_tv_vip, mContext.resources.getColor(R.color.text_gray))
                            .setGone(R.id.ihd_iv_more, true)

                } else {
                    when (data.type) {
                        0 -> {
                            if (data.isVNode) {
                                helper.setBackgroundRes(R.id.ihd_content, R.drawable.bg_device_white)
                            } else {
                                helper.setBackgroundRes(R.id.ihd_content, R.drawable.bg_device_white)
                            }
                            helper.setImageResource(R.id.ihd_iv_more, R.drawable.icon_blue_details)
                        }
                        1 -> helper.setBackgroundRes(R.id.ihd_content,  /*data.isOnline() ?
                            R.drawable.bg_device_green_stroke :*/R.drawable.bg_device_white)
                                .setImageResource(R.id.ihd_iv_more, R.drawable.icon_green_details)
                        2 -> helper.setBackgroundRes(R.id.ihd_content, R.drawable.bg_device_white)
                                .setImageResource(R.id.ihd_iv_more, R.drawable.icon_yellow_details)
                    }
                    if (devBoundType == DevBoundType.LOCAL_DEVICES) {
                        helper.setGone(R.id.ihd_iv_more, false)
                        helper.setGone(R.id.ihd_ll_status, false)
                    } else {
                        helper.setVisible(R.id.ihd_iv_more, true)
                        helper.setVisible(R.id.ihd_ll_status, true)
                    }
                    helper.setText(R.id.ihd_tv_owner, name)
                            .setText(R.id.ihd_tv_vip, if (devBoundType == DevBoundType.ALL_BOUND_DEVICES) "" else data.vip)
                            .setTextColor(R.id.ihd_tv_name, mContext.resources.getColor(R.color.text_dark_gray))
                            .setTextColor(R.id.ihd_tv_vip, mContext.resources.getColor(R.color.text_dark_gray))
                            .setTextColor(R.id.ihd_tv_owner, mContext.resources.getColor(R.color.text_dark_gray))
                            .setTextColor(R.id.ihd_tv_local, mContext.resources.getColor(R.color.text_dark_gray))
                            .setGone(R.id.ihd_iv_more, true)
                }


                val defaultIcon = if (devBoundType == DevBoundType.ALL_BOUND_DEVICES) DeviceBean.getIconSimple(data) else DeviceBean.getIcon(data)
                if (ihd_img_icon.getTag() == null) Glide.with(ihd_img_icon).load(defaultIcon).into(ihd_img_icon)
                data.let {
                    if (it.isNas()) {
                        val brief = viewModel.getBrief(it.id)
                        viewModel.loadBrief(it.id, brief, ihd_img_icon, if (it.isDevDisable || !it.isOnline || isPendingAccept) null else tvContent, defalutImage = defaultIcon)
                    } else {
                        ihd_img_icon.setOnClickListener(null)
                    }
                }
                helper.getView<View>(R.id.ihd_content).elevation = if (devBoundType == DevBoundType.ALL_BOUND_DEVICES) 0f else Dp2PxUtils.dp2px(mContext, 4).toFloat()
                if (devBoundType == DevBoundType.MY_DEVICES || devBoundType == DevBoundType.SHARED_DEVICES || devBoundType == DevBoundType.ALL_BOUND_DEVICES) {
                    val netId = CMAPI.getInstance().baseInfo.netid
                    if (!data.isOnline) {
                        helper.setText(R.id.ihd_tv_vip, if (data.hardData == null || data.hardData!!.networkId == null || data.inNetwork(netId)) R.string.offline else R.string.in_other_networks)
                                .setTextColor(R.id.ihd_tv_name, mContext.resources.getColor(R.color.text_light_gray))
                                .setTextColor(R.id.ihd_tv_vip, mContext.resources.getColor(R.color.text_light_gray))
                                .setTextColor(R.id.ihd_tv_owner, mContext.resources.getColor(R.color.text_light_gray))
                                .setTextColor(R.id.ihd_tv_local, mContext.resources.getColor(R.color.text_light_gray))
                                .setGone(R.id.ihd_iv_more, false)
                                .setGone(R.id.ihd_iv_dig, false)
                        helper.getView<View>(R.id.ihd_content).elevation = 0f
                        if (isPendingAccept) {
                            helper.setText(R.id.ihd_tv_vip, R.string.wait_for_consent)
                        }
                    }
                } else if (devBoundType == DevBoundType.LOCAL_DEVICES) {
                    helper.setText(R.id.ihd_tv_vip, data.priIp)
                            .setVisible(R.id.middle_line, false)
                }
                if (data.isOnline) { //显示错误提示
                    if (data.isDevDisable) {
                        if (devBoundType != DevBoundType.ALL_BOUND_DEVICES) helper.setTextColor(R.id.ihd_tv_vip, mContext.resources.getColor(R.color.text_red))
                        helper.setText(R.id.ihd_tv_vip, ErrorCode.ec2String(data.devDisableReason))
                        //设备无法联通，处于不可用状态：当前网络下，且非免费旧圈子，加入状态异常
                        if (data.enServer != null) {
                            if (data.enServer!!.joinStatus == -1) {
                                helper.setText(R.id.ihd_tv_vip, R.string.not_subscribed)
                            } else if (data.enServer!!.flowStatus == 1) {
                                helper.setText(R.id.ihd_tv_vip, R.string.flow_is_expired)
                            } else if (data.enServer!!.flowStatus != 0) {
                                helper.setText(R.id.ihd_tv_vip, R.string.not_purchase_circle_flow)
                            }
                        }
                    }
                }
                if (data.isVNode) {
                    helper.setText(R.id.ihd_tv_vip, mContext.resources.getString(R.string.virtual_node))
                            .setTextColor(R.id.ihd_tv_vip, mContext.resources.getColor(R.color.text_dark_gray))
                            .setText(R.id.ihd_tv_owner, "")
                            .setGone(R.id.ihd_iv_high, false)
                }
                if (isPendingAccept) {//待同意
                    helper.setText(R.id.ihd_tv_vip, R.string.wait_for_consent)
                }
                if (data.typeValue == 3) {
                    //设备状态 1-正常 2-停用 5-解绑停用 6-欠费停用
                    val devicestatus = data.hardData?.devicestatus ?: 0
                    var text = ""
                    when (devicestatus) {
                        2 -> {//停用
                            helper.setTextColor(R.id.ihd_tv_name, mContext.resources.getColor(R.color.text_light_gray2))
                            text = mContext.getString(R.string.cloud_device_status_stop)
                        }
                        4 -> {//已到期
                            helper.setTextColor(R.id.ihd_tv_name, mContext.resources.getColor(R.color.text_light_gray2))
                            text = mContext.getString(R.string.cloud_device_expired)
                        }
                        5 -> {//解绑停用
                            helper.setTextColor(R.id.ihd_tv_name, mContext.resources.getColor(R.color.text_light_gray2))
                            text = mContext.getString(R.string.cloud_device_status_unbounded)
                        }
                        6 -> {//欠费停用
                            helper.setTextColor(R.id.ihd_tv_name, mContext.resources.getColor(R.color.text_light_gray2))
                            text = mContext.getString(R.string.ec_insufficient_score)
                        }
                        7 -> {//停用
                            helper.setTextColor(R.id.ihd_tv_name, mContext.resources.getColor(R.color.text_light_gray2))
                            text = mContext.getString(R.string.forbidden)
                        }
                        else -> {
                            helper.setTextColor(R.id.ihd_tv_name, mContext.resources.getColor(R.color.text_dark))
                        }
                    }
                    helper.setText(R.id.ihd_tv_vip, text)
                }
                helper.addOnClickListener(R.id.ihd_iv_more)
            }
        }
    }

    override fun convertPayloads(helper: BaseViewHolder, item: DeviceBean, payloads: MutableList<Any>) {
        convert(helper, item)
    }

    override fun setNewData(data: MutableList<DeviceBean>?) {
        this.mData = if (data == null) arrayListOf() else data
        this.notifyItemRangeChanged(0, itemCount, arrayListOf(1))
    }

    fun setFragmentType(type: Int) {
        devBoundType = type
    }

    init {
        addItemType(1, R.layout.item_home_device_title)
        addItemType(2, if (devBoundType == DevBoundType.ALL_BOUND_DEVICES) R.layout.item_device_manager else R.layout.item_home_device)
        setSpanSizeLookup { gridLayoutManager, position ->
            if (getDefItemViewType(position) == 1) {
                gridLayoutManager.spanCount
            } else 1
        }
    }
}