package net.linkmate.app.ui.adapter.main

import android.text.format.DateUtils
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import net.linkmate.app.R
import net.linkmate.app.base.MyConstants
import net.linkmate.app.ui.viewmodel.getStByConfirmAck
import net.linkmate.app.ui.viewmodel.getStByStatus
import net.sdvn.cmapi.util.DateUtil
import net.sdvn.common.internet.protocol.entity.FlowMbpointRatioModel
import net.sdvn.common.internet.protocol.entity.SdvnMessage
import net.sdvn.common.vo.EnMbPointMsgModel
import net.sdvn.common.vo.MsgCommonModel
import net.sdvn.common.vo.MsgModel
import net.sdvn.common.vo.SdvnMessageModel
import java.util.*

class MsgModelAdapter : BaseQuickAdapter<MsgModel<*>, BaseViewHolder>(R.layout.item_en_changed_msg) {
    override fun convert(helper: BaseViewHolder, data: MsgModel<*>?) {
        if (data != null) {
            when (data) {
                is EnMbPointMsgModel -> {
                    val contentData = data.content
                    //                var deviceModel: DeviceModel? = null
                    val content = if (contentData?.defaultContent.isNullOrEmpty()) {
                        //                    deviceModel = SessionManager.getInstance().getDeviceModel(contentData.deviceid)
                        val context = helper.itemView.context
                        when {
                            Objects.equals(data.type, FlowMbpointRatioModel.Type.setmbpratio.name) -> {
                                //                        val priceUnit: String = context.getString(R.string.fmt_traffic_unit_price)
                                //                                .replace("\$TRAFFIC$", contentData.mbpointratio)
                                //                您绑定的设备[M8X2C2D0003]将于15:47 更改积分计费值为 每2GB流量消耗1积分
                                context.getString(R.string.set_mbpoint_ratio_msg)
                                //                        val schemedate = MyConstants.sdfSimple.format(Date(contentData.schemedate * 1000L))
                                //                        String.format(string, contentData.devicename,
                                //                                schemedate,
                                //                                priceUnit)
                            }
                            Objects.equals(data.type, FlowMbpointRatioModel.Type.cancelmbpratio.name) -> {
                                context.getString(R.string.cancel_mbpoint_ratio_msg)
                                //                        String.format(string, contentData.devicename)
                            }
                            else -> {
                                ""
                            }
                        }
                    } else {
                        contentData?.defaultContent
                    }
                    helper.setText(R.id.tv_msg_content, content)
                    helper.setText(R.id.tv_title, contentData?.devicename?.toString())
                    //                helper.setImageResource(R.id.icon_news, IconHelper.getIconByeDevClass(deviceModel?.devClass
                    //                        ?: 0))
                    helper.setGone(R.id.icon_news, true)

                    //                helper.itemView.setOnClickListener { view ->
                    //                    if (Utils.isFastClick(view)) {
                    //                        return@setOnClickListener
                    //                    }
                    //                    listeners?.forEach {
                    //                        it?.OnItemClick(data, helper.adapterPosition, view)
                    //                    }
                    //                }
                    helper.setText(R.id.tv_msg_status, null)
                    helper.setGone(R.id.iv_red_dot, !data.isWasRead)
                }
                is SdvnMessageModel -> {
                    helper.setText(R.id.tv_msg_content, data.message)
                    helper.setText(R.id.tv_title, data.username)
                    helper.setGone(R.id.icon_news, true)

                    helper.setText(R.id.tv_msg_status, getStByStatus(data.status))
                    helper.setGone(R.id.iv_red_dot, !data.isWasRead || data.status == SdvnMessage.MESSAGE_STATUS_WAIT)
                }
                is MsgCommonModel -> {
                    helper.setText(R.id.tv_msg_content, data.content)
                    helper.setText(R.id.tv_title, data.title)
                    helper.setGone(R.id.icon_news, true)
                    helper.setText(R.id.tv_msg_status, if (data.expired) {
                        R.string.expired
                    } else {
                        if (data.isNeedConfirm()) {
                            getStByConfirmAck(data.confirmAck)
                        } else {
                            R.string.empty
                        }
                    })
                    helper.setGone(R.id.iv_red_dot, !data.isWasRead || (data.isNeedConfirm() && data.isWaitConfirm() && !data.expired))

                }
            }
            helper.setImageResource(R.id.icon_news, getIconByType(data.type))

            if (data.timestamp > 0) {
                val format: String
                val time = data.timestamp
                format = when {
                    DateUtils.isToday(time) -> MyConstants.sdfSimple.format(Date(time))
                    DateUtil.isThisYear(time) -> MyConstants.sdfNoYear.format(Date(time))
                    else -> MyConstants.sdf.format(Date(time))
                }
                helper.setText(R.id.tv_msg_time, format)
            } else {
                helper.setText(R.id.tv_msg_time, "")
            }
        }
    }

//    private var listeners: MutableList<OnItemClickListener<MsgModel<*>>?>? = null
//    fun addItemClickListener(onItemClickListener: OnItemClickListener<MsgModel<*>>) {
//        if (listeners == null) {
//            listeners = mutableListOf()
//        }
//        listeners?.add(onItemClickListener)
//    }

}

fun getIconByType(type: String): Int {
    return when (type) {
        SdvnMessage.APPLY2NET -> R.drawable.icon_msg_joinmynet
        SdvnMessage.INVITE2NET -> R.drawable.icon_msg_joinhisnet
        SdvnMessage.BIND_MGR,
        SdvnMessage.BIND_DEV -> R.drawable.icon_msg_bindingsb
        FlowMbpointRatioModel.Type.cancelmbpratio.name -> R.drawable.icon_msg_flowcancle
        FlowMbpointRatioModel.Type.setmbpratio.name -> R.drawable.icon_msg_flowchangs
        else -> R.drawable.icon_msg_sysmsg
    }
}