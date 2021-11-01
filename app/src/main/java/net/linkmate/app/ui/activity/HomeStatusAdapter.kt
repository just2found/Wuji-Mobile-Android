package net.linkmate.app.ui.activity

import android.content.Context
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.dialog_device_item_status.view.*
import net.linkmate.app.BuildConfig
import net.linkmate.app.R
import net.linkmate.app.base.MyConstants
import net.linkmate.app.ui.activity.nasApp.deviceDetial.adapter.DeviceBaseAdapter
import net.linkmate.app.ui.activity.nasApp.deviceDetial.adapter.DeviceStatusAdapter
import net.linkmate.app.util.FormatUtils
import net.linkmate.app.util.MySPUtils
import net.linkmate.app.util.ToastUtils
import net.sdvn.cmapi.CMAPI
import net.sdvn.cmapi.Network
import net.sdvn.cmapi.RealtimeInfo
import net.sdvn.cmapi.global.Constants
import net.sdvn.cmapi.util.ClipboardUtils

/**
 * @author Raleigh.Luo
 * date：20/8/3 21
 * describe：
 */
class HomeStatusAdapter(val context: Context): RecyclerView.Adapter<DeviceBaseAdapter.ViewHolder>() {
    private val sources:ArrayList<DeviceStatusAdapter.StatusMenu> = ArrayList()
    //必须在init前／否则后赋值初始值均为0
    private val MACHINE_NAME=0
    private val CURRENT_NET=1
    private val NODES=2
    private val VIP=3
    private val LANIP=4
    private val DOMAIN=5
    private val UPTIME=6
    private val DELAY=7
    init {
        getItemSources()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeviceBaseAdapter.ViewHolder {
        val view =
                LayoutInflater.from(context).inflate(R.layout.dialog_device_item_status, null, false)
        view.layoutParams =
                ConstraintLayout.LayoutParams(
                        ConstraintLayout.LayoutParams.MATCH_PARENT,
                        ConstraintLayout.LayoutParams.WRAP_CONTENT
                )
        return DeviceBaseAdapter.ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return sources.size
    }

    override fun onBindViewHolder(holder: DeviceBaseAdapter.ViewHolder, position: Int) {
        with(holder.itemView) {
            tvStatusTitle.text = sources[position].title
            tvStatusContent.text = sources[position].content

            setOnClickListener(View.OnClickListener {
                //没有拦截，则可以内部处理
                internalItemClick(it, position)
            })
        }
    }
    fun internalItemClick(view: View, position: Int){
        clipString(sources[position].content?:"")
    }
    private fun clipString(content: String) {
        if (context != null) {
            ClipboardUtils.copyToClipboard(context, content)
            ToastUtils.showToast(context.getString(R.string.Copied).toString() + content)
        }
    }

    /**
     * 赋值项
     */
    fun getItemSources(){
        sources.clear()
        sources.add(getMenuByFunction(MACHINE_NAME))
        sources.add(getMenuByFunction(CURRENT_NET))
        sources.add(getMenuByFunction(NODES))
        sources.add(getMenuByFunction(VIP))
        sources.add(getMenuByFunction(LANIP))
        sources.add(getMenuByFunction(DOMAIN))
        sources.add(getMenuByFunction(UPTIME))
        sources.add(getMenuByFunction(DELAY))
    }


    private fun getMenuByFunction(function:Int): DeviceStatusAdapter.StatusMenu {
        val menu= DeviceStatusAdapter.StatusMenu(function = function)
        when(function){
            MACHINE_NAME ->{
                menu.title = context.getString(R.string.machine_name)
                menu.content = getContent(CMAPI.getInstance().getBaseInfo().getDeviceName())
            }
            CURRENT_NET ->{
                menu.title = context.getString(R.string.current_net)
                menu.content =  "-"
                val network: Network? = getCurrentNetwork()
                network?.let {
                    menu.content = getContent(network.name)
                }
            }
            NODES ->{
                menu.title = context.getString(R.string.nodes)
                val snName: String? = getCurrentSNName()
                menu.content = getContent(if (TextUtils.isEmpty(snName)) "N/A" else snName)
            }
            VIP ->{
                menu.title = context.getString(R.string.vip)
                menu.content =  getContent(CMAPI.getInstance().getBaseInfo().getVip())
            }
            LANIP ->{
                menu.title = context.getString(R.string.lanIp)
                menu.content =  getContent(CMAPI.getInstance().getBaseInfo().getPriIp())
            }
            DOMAIN ->{
                menu.title = context.getString(R.string.domain)
                menu.content =  getContent(if(BuildConfig.DEBUG)CMAPI.getInstance().getBaseInfo().getTicket()
                else CMAPI.getInstance().getBaseInfo().getDomain())
            }
            UPTIME ->{
                menu.title = context.getString(R.string.uptime)
                menu.content =  "-"
            }
            DELAY ->{
                menu.title = context.getString(R.string.delay)
                menu.content =  "-"
            }
        }
        return menu
    }
    private fun getContent(content: String?):String{
        return if(TextUtils.isEmpty(content)) "-" else content!!
    }
    fun refreshRealTimeInfo(info: RealtimeInfo) {
        val network = getCurrentNetwork()
        if (network != null){
            if(sources.size>=CURRENT_NET+1) sources.get(CURRENT_NET).content = network.name
        }
        val snName = getCurrentSNName()
        if(sources.size>=NODES+1) sources.get(NODES).content = getContent(if (TextUtils.isEmpty(snName)) "N/A" else snName)
        if(sources.size>=DELAY+1) sources.get(DELAY).content = getContent(FormatUtils.getLatencyText(info.netLatency))
        if(sources.size>=UPTIME+1) sources.get(UPTIME).content = getContent(FormatUtils.getUptime(info.onlineTime))
        if (!MySPUtils.getBoolean(MyConstants.IS_LOGINED)) {
            if(sources.size>=DELAY+1) sources.get(DELAY).content = "-"
            if(sources.size>=UPTIME+1) sources.get(UPTIME).content = "-"
        }
        notifyDataSetChanged()
    }

    private fun getCurrentNetwork(): Network? {
        for (network in CMAPI.getInstance().networkList) {
            if (network.isCurrent) return network
        }
        return null
    }
    private fun getCurrentSNName(): String? {
        val currentSmartNode = CMAPI.getInstance().realtimeInfo?.currentSmartNode
        val sb = StringBuilder()
        currentSmartNode?.let {
            if (it.size >0) {
                for (i in currentSmartNode.indices) {
                    val device = currentSmartNode[i]
                    if (device.selectable || device.deviceType == Constants.DT_V_NODE) {
                        if (!TextUtils.isEmpty(sb.toString())) sb.append(", ")
                        sb.append(device.name)
                    }
                }
            }
        }

        return sb.toString()
    }
}