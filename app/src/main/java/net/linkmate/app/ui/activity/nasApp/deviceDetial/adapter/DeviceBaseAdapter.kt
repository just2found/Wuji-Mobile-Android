package net.linkmate.app.ui.activity.nasApp.deviceDetial.adapter

import android.view.View
import android.widget.CompoundButton
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import net.linkmate.app.ui.activity.nasApp.deviceDetial.DeviceDetailViewModel
import net.linkmate.app.ui.activity.nasApp.deviceDetial.DeviceViewModel

/**
 * @author Raleigh.Luo
 * date：20/7/23 17
 * describe：
 */
open abstract class  DeviceBaseAdapter<out VM: DeviceDetailViewModel>
(val context: Fragment, val fragmentViewModel: VM, val viewModel: DeviceViewModel):
        RecyclerView.Adapter<DeviceBaseAdapter.ViewHolder>() {
    //默认布局－每个适配器的默认主布局，非固定，其他为固定
    protected val TYPE_DEFALUT=0
    //二维码布局－固定
    protected val TYPE_QRCODE=1
    //状态布局－固定
    protected val TYPE_STATUS=2
    //开关布局－固定
    protected val TYPE_SWITCH=3
    //dns布局－固定
    protected val TYPE_DNS=4
    //子网ip－固定
    protected val TYPE_SUBNET=5

    /**
     * 外部长按监听器
     */
    var onCheckedChangeListener: CompoundButton.OnCheckedChangeListener? = null
    /**
     * 外部点击监听器
     */
    var onItemClickListener:OnItemClickListener? = null

    /**
     * 外部长按监听器
     */
    var onItemLongClickListener:OnItemLongClickListener? = null

    /**
     * 内部点击事件响应(未被外部监听器OnItemClickListener拦截既可调用)
     * 前提条件：OnItemClick＝false
     */
    abstract fun internalItemClick(view: View, position: Int)
    /**
     * 内部长按事件响应(未被外部监听器OnItemLongClickListener拦截既可调用)
     *  前提条件：onLongClick＝false
     */
    abstract fun internalItemLongClick(view: View, position: Int)

    /**
     * 拦截返回事件 true
     * false表示不拦截
     */
    open fun interceptBackPressed():Boolean{
        return false
    }
    /**
     * 拦截finish事件 true
     * false表示不拦截
     */
    open fun interceptFinishActivity():Boolean{
        return false
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    interface OnItemClickListener {
        fun onClick(view: View, position: Int):Boolean
    }
    interface OnItemLongClickListener {
        fun onLongClick(view: View, position: Int):Boolean
    }
    open fun onDestory(){
    }

}