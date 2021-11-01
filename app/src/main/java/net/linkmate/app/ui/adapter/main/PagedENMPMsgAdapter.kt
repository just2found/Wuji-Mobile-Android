//package net.linkmate.app.ui.adapter.main
//
//import android.view.LayoutInflater
//import android.view.ViewGroup
//import androidx.paging.PagedListAdapter
//import androidx.recyclerview.widget.DiffUtil
//import com.chad.library.adapter.base.BaseViewHolder
//import io.weline.devhelper.IconHelper
//import io.weline.internetdb.vo.EnMbPointMsgModel
//import net.linkmate.app.R
//import net.linkmate.app.base.MyConstants
//import net.sdvn.common.internet.protocol.entity.FlowMbpointRatioModel
//import net.sdvn.nascommon.SessionManager
//import net.sdvn.nascommon.model.DeviceModel
//import net.sdvn.nascommon.utils.Utils
//import org.view.libwidget.OnItemClickListener
//import java.util.*
//
//class PagedENMPMsgAdapter : PagedListAdapter<EnMbPointMsgModel, BaseViewHolder>(diffCallback) {
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
//        val item = LayoutInflater.from(parent.context).inflate(R.layout.item_en_changed_msg, parent, false)
//        return BaseViewHolder(item)
//    }
//
//    override fun onBindViewHolder(helper: BaseViewHolder, position: Int) {
//        val data = getItem(position)
//        if (data != null) {
//            val contentData = data.content
//            var deviceModel: DeviceModel? = null
//            val content = if (contentData?.defaultContent.isNullOrEmpty()) {
//                deviceModel = SessionManager.getInstance().getDeviceModel(contentData.deviceid)
//                val context = helper.itemView.context
//                if (Objects.equals(contentData.type, FlowMbpointRatioModel.Type.setmbpratio.name)) {
//                    val priceUnit: String = context.getString(R.string.fmt_traffic_unit_price)
//                            .replace("\$TRAFFIC$", contentData.mbpointratio)
////                您绑定的设备[M8X2C2D0003]将于15:47 更改积分计费值为 每2GB流量消耗1积分
//                    val string = context.getString(R.string.set_mbpoint_ratio_msg)
//                    val schemedate = MyConstants.sdfSimple.format(Date(contentData.schemedate * 1000L))
//                    String.format(string, contentData.devicename,
//                            schemedate,
//                            priceUnit)
//                } else {
//                    val string = context.getString(R.string.cancel_mbpoint_ratio_msg)
//                    String.format(string, contentData.devicename)
//                }
//            } else {
//                contentData?.defaultContent
//            }
//            helper.setText(R.id.tv_msg_content, content)
//            helper.setText(R.id.tv_msg_title, contentData?.devicename?.toString())
//            helper.setImageResource(R.id.icon_news, IconHelper.getIconByeDevClass(deviceModel?.devClass
//                    ?: 0))
//            helper.setGone(R.id.iv_msg_selected, !data.isWasRead)
//            if (data.timestamp > 0) {
//                val format = MyConstants.sdf.format(Date(data.timestamp * 1000))
//                helper.setText(R.id.tv_msg_time, format)
//            }
//            helper.itemView.setOnClickListener { view ->
//                if (Utils.isFastClick(view)) {
//                    return@setOnClickListener
//                }
//                listeners?.forEach {
//                    it?.OnItemClick(data, position, view)
//                }
//            }
//        }
//    }
//
//    private var listeners: MutableList<OnItemClickListener<EnMbPointMsgModel>?>? = null
//    fun addItemClickListener(onItemClickListener: OnItemClickListener<EnMbPointMsgModel>) {
//        if (listeners == null) {
//            listeners = mutableListOf()
//        }
//        listeners?.add(onItemClickListener)
//    }
//
//    companion object {
//        private val diffCallback: DiffUtil.ItemCallback<EnMbPointMsgModel> = object : DiffUtil.ItemCallback<EnMbPointMsgModel>() {
//            override fun areItemsTheSame(oldItem: EnMbPointMsgModel, newItem: EnMbPointMsgModel): Boolean {
//                return oldItem === newItem
//            }
//
//            override fun areContentsTheSame(oldItem: EnMbPointMsgModel, newItem: EnMbPointMsgModel): Boolean {
//                return oldItem == newItem
//            }
//        }
//    }
//}