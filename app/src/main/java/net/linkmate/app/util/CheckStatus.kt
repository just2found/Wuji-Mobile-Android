package net.linkmate.app.util

import android.app.Activity
import android.content.Intent
import android.view.View
import androidx.arch.core.util.Function
import androidx.fragment.app.FragmentManager
import io.weline.repo.files.constant.AppConstants
import net.linkmate.app.R
import net.linkmate.app.bean.DeviceBean
import net.linkmate.app.ui.activity.circle.circleDetail.CircleDetialActivity
import net.linkmate.app.ui.activity.nasApp.deviceDetial.DevicelDetailActivity
import net.linkmate.app.ui.activity.nasApp.deviceDetial.FunctionHelper
import net.linkmate.app.ui.simplestyle.dynamic.CircleStatus
import net.linkmate.app.view.HintDialog
import net.sdvn.cmapi.Device
import net.sdvn.common.repo.InNetDevRepo
import net.sdvn.common.repo.NetsRepo
import net.sdvn.common.vo.NetworkModel

/**
 * @author Raleigh.Luo
 * date：20/10/22 10
 * describe：
 */
object CheckStatus {
    /**
     * 检查圈子状态
     * @param statusCallback true ：状态正常，按原逻辑走，false ：状态异常
     * @param nextCallback 下一步回调，点击了弹框按钮 是／否
     */
    fun checkCircleStatus(activity: Activity, fragmentManager: FragmentManager, currentNetwork: NetworkModel?,
                          statusCallback: Function<Boolean, Void?>?, nextCallback: Function<Boolean, Void?>? = null) {
        /*---免费旧圈子,直接返回-----------------------------------------------*/
        if (currentNetwork?.isCharge ?: false == false) {
            statusCallback?.apply(true)
            return
        }
        when {
            /*---网络状态异常 需购买流量 //流量状态 0-正常 1-已到期 -1-未订购-----------------------------------------------*/
            currentNetwork?.isDevSepCharge ?: false == false &&
                    (currentNetwork?.flowStatus == 1 || currentNetwork?.flowStatus == -1) -> {
                statusCallback?.apply(false)
                val hintDialog: HintDialog = HintDialog.newInstance(if (currentNetwork.flowStatus == 1) activity.getString(R.string.circle_flow_is_expired_hint) else activity.getString(R.string.not_purchase_circle_flow_hint),
                        null, null, activity.getString(R.string.yes), activity.getString(R.string.no))
                hintDialog.setOnClickListener(View.OnClickListener { view ->
                    if (view.id == R.id.positive) {
                        CircleDetialActivity.startActivityForResult(activity, Intent(activity, CircleDetialActivity::class.java)
                                .putExtra(net.linkmate.app.ui.activity.circle.circleDetail.FunctionHelper.NETWORK_ID, currentNetwork.netId)
                                .putExtra(net.linkmate.app.ui.activity.circle.circleDetail.FunctionHelper.FUNCTION, net.linkmate.app.ui.activity.circle.circleDetail.FunctionHelper.CIRCLE_PURCHASE_FLOW), net.linkmate.app.ui.activity.circle.circleDetail.FunctionHelper.CIRCLE_PURCHASE_FLOW
                        )
                        nextCallback?.apply(true)
                    } else {
                        //回调
                        nextCallback?.apply(false)
                    }
                })
                hintDialog.show(fragmentManager, "hint")
            }
            /*---圈子状态正常-----------------------------------------------*/
            else -> {
                //回调
                statusCallback?.apply(true)
            }
        }
    }

    /** 检查设备状态
     * @param callback 回调，处理后续逻辑
     * @param statusCallback true ：状态回调 状态正常，按原逻辑走，false ：状态异常
     * @param nextCallback 下一步回调，点击了弹框按钮 是／否
     */
    fun checkDeviceStatus(activity: Activity, fragmentManager: FragmentManager, device: DeviceBean,
                          statusCallback: Function<Boolean, Void?>?, nextCallback: Function<Boolean, Void?>? = null) {
        //状态正常，直接返回
        if (!device.isDevDisable) {
            statusCallback?.apply(true)
            return
        }

        val currentNetwork = NetsRepo.getCurrentNet()
        val isDeivceDisableByFlow = device.isDevDisable && device.devDisableReason == 2
        /*---免费旧圈子,直接返回-----------------------------------------------*/
        if (currentNetwork?.isCharge ?: false == false) {
            statusCallback?.apply(true)
            return
        }

        //采用实时查询，数据有延迟
        InNetDevRepo.getNetDeviceModel(device.id)?.let {
            when {
                /*---未开通设备服务-----------------------------------------------*/
                it.joinStatus == -1 -> {
                    statusCallback?.apply(false)
                    val hintDialog: HintDialog = HintDialog.newInstance(activity.getString(R.string.is_open_device_server),
                            null, null, activity.getString(R.string.yes), activity.getString(R.string.no))
                    hintDialog.setOnClickListener(View.OnClickListener {
                        if (it.id == R.id.positive) {
                            DevicelDetailActivity.startActivityForResult(activity, Intent(activity, DevicelDetailActivity::class.java)
                                    .putExtra(AppConstants.SP_FIELD_DEVICE_ID, device.id)
                                    .putExtra(AppConstants.SP_FIELD_DEVICE_NAME, device.name)
                                    .putExtra(FunctionHelper.FUNCTION, FunctionHelper.DEVICE_PURCHASE_JOIN), FunctionHelper.DEVICE_PURCHASE_JOIN
                            )
                            nextCallback?.apply(true)
                        } else {
                            nextCallback?.apply(false)
                        }
                    })
                    hintDialog.show(fragmentManager, "hint")
                }
                /*---设备状态是否正常，可以进入访问设备-----------------------------------------------*/
                else -> {//设备加入状态正常
                    checkDeviceCircleStatus(activity, fragmentManager, isDeivceDisableByFlow, currentNetwork, Function {
                        if (it ?: false) {
                            if (currentNetwork?.isDevSepCharge
                                            ?: false) {//圈子是isDevSepCharge ＝ true,设备单独收费,
                                checkDeviceFlow(activity, fragmentManager, device, isDeivceDisableByFlow, statusCallback, nextCallback)
                            } else {//圈子是isDevSepCharge = false 统一管理
                                checkDeviceCircleStatus(activity, fragmentManager, isDeivceDisableByFlow, currentNetwork, statusCallback, nextCallback)
                            }
                        } else {
                            statusCallback?.apply(it)
                        }
                        null
                    }, nextCallback)
                }

            }
        } ?: let {
            /*---不是EN服务器，直接返回-----------------------------------------------*/
            statusCallback?.apply(true)
        }

    }

    /**
     * 检查设备流量
     * @param statusCallback true ：状态正常，按原逻辑走，false ：状态异常
     * @param nextCallback 下一步回调，点击了弹框按钮 是／否
     */
    private fun checkDeviceFlow(activity: Activity, fragmentManager: FragmentManager, device: DeviceBean, isDeivceDisableByFlow: Boolean,
                                statusCallback: Function<Boolean, Void?>?, nextCallback: Function<Boolean, Void?>?) {
        when {
            /*---流量异常， 选购流量-----------------------------------------------*/
            isDeivceDisableByFlow || (device.enServer?.flowStatus ?: 0) != 0 -> {//流量异常， 选购流量
                statusCallback?.apply(false)
                val hintDialog: HintDialog = HintDialog.newInstance(activity.getString(R.string.is_purchase_flow),
                        null, null, activity.getString(R.string.yes), activity.getString(R.string.no))
                hintDialog.setOnClickListener(View.OnClickListener {
                    if (it.id == R.id.positive) {
                        DevicelDetailActivity.startActivityForResult(activity, Intent(activity, DevicelDetailActivity::class.java)
                                .putExtra(AppConstants.SP_FIELD_DEVICE_ID, device.id)
                                .putExtra(AppConstants.SP_FIELD_DEVICE_NAME, device.name)
                                .putExtra(FunctionHelper.FUNCTION, FunctionHelper.DEVICE_PURCHASE_FLOW), FunctionHelper.DEVICE_PURCHASE_FLOW
                        )
                        nextCallback?.apply(true)
                    } else {
                        nextCallback?.apply(false)
                    }
                })
                hintDialog.show(fragmentManager, "hint")
            }
            else -> {//设备流量收费正常
                statusCallback?.apply(true)
            }

        }

    }

    /**
     * 检查圈子状态
     * @param statusCallback true ：状态正常，按原逻辑走，false ：状态异常
     * @param nextCallback 下一步回调，点击了弹框按钮 是／否
     * @param isDisableByFlow 因流量不足而设备不可用
     */
    private fun checkDeviceCircleStatus(activity: Activity, fragmentManager: FragmentManager, isDeivceDisableByFlow: Boolean, currentNetwork: NetworkModel?,
                                        statusCallback: Function<Boolean, Void?>?, nextCallback: Function<Boolean, Void?>?) {
        /*---免费旧圈子,直接返回-----------------------------------------------*/
        if (currentNetwork?.isCharge ?: false == false) {
            statusCallback?.apply(true)
            return
        }
        when {
            /*---网络状态异常 需购买流量 //流量状态 0-正常 1-已到期 -1-未订购-----------------------------------------------*/
            currentNetwork?.isDevSepCharge ?: false == false &&
                    (isDeivceDisableByFlow || (currentNetwork?.flowStatus == 1 || currentNetwork?.flowStatus == -1)) -> {
                statusCallback?.apply(false)
                val hintDialog: HintDialog = HintDialog.newInstance(if (currentNetwork?.flowStatus == 1) activity.getString(R.string.circle_flow_is_expired_hint) else activity.getString(R.string.not_purchase_circle_flow_hint),
                        null, null, activity.getString(R.string.yes), activity.getString(R.string.no))
                hintDialog.setOnClickListener(View.OnClickListener { view ->
                    if (view.id == R.id.positive) {
                        CircleDetialActivity.startActivityForResult(activity, Intent(activity, CircleDetialActivity::class.java)
                                .putExtra(net.linkmate.app.ui.activity.circle.circleDetail.FunctionHelper.NETWORK_ID, currentNetwork?.netId)
                                .putExtra(net.linkmate.app.ui.activity.circle.circleDetail.FunctionHelper.FUNCTION, net.linkmate.app.ui.activity.circle.circleDetail.FunctionHelper.CIRCLE_PURCHASE_FLOW), net.linkmate.app.ui.activity.circle.circleDetail.FunctionHelper.CIRCLE_PURCHASE_FLOW
                        )
                        activity.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                        nextCallback?.apply(true)
                    } else {
                        //回调
                        nextCallback?.apply(false)
                    }
                })
                hintDialog.show(fragmentManager, "hint")
            }
            /*---圈子状态正常-----------------------------------------------*/
            else -> {
                //回调
                statusCallback?.apply(true)
            }
        }
    }

    /** 检查设备状态
     * @param callback 回调，处理后续逻辑
     */
    fun checkDeviceStatus(device: Device?): CircleStatus {
        var status = CircleStatus.NONE

        device?.let {
            val currentNetwork = NetsRepo.getCurrentNet()
            //状态正常 或为 免费旧圈子
            if (!device.isDevDisable || (currentNetwork?.isCharge ?: false == false)) {
                status = if (device.isOnline) CircleStatus.NOMARL else CircleStatus.DEVICE_OFFLINE
                return status
            }
            val isDeivceDisableByFlow = device.isDevDisable && device.devDisableReason == 2

            val deviceModel = InNetDevRepo.getNetDeviceModel(device.id)
            //采用实时查询，数据有延迟,检查设备加入状态
            if (deviceModel?.joinStatus == -1) {
                /*---未开通设备服务-----------------------------------------------*/
                status = CircleStatus.UNSUBSCRIBE_DEVICE_SERVER
            } else {//设备加入状态正常
                if (currentNetwork?.isCharge ?: false == true && currentNetwork?.isDevSepCharge ?: false == false &&
                        (isDeivceDisableByFlow || (currentNetwork?.flowStatus == 1 || currentNetwork?.flowStatus == -1))) {
                    /*---网络状态异常 需购买流量 //流量状态 0-正常 1-已到期 -1-未订购-----------------------------------------------*/
                    status = CircleStatus.WITHOUT_PURCHASE_CIRCLE_FLOW
                } else {
                    if (currentNetwork?.isDevSepCharge
                                    ?: false) {//圈子是isDevSepCharge ＝ true,设备单独收费,
                        if (isDeivceDisableByFlow || (deviceModel?.flowStatus ?: 0) != 0) {
                            /*---设备流量异常， 选购流量-----------------------------------------------*/
                            status = CircleStatus.WITHOUT_PURCHASE_DEVICE_FLOW
                        }
                    }
                }
            }
            true
        } ?: let {
            status = CircleStatus.WITHOUT_DEVICE_SERVER
        }
        if (status == CircleStatus.NONE) status = CircleStatus.NOMARL
        return status
    }

}