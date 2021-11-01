package net.linkmate.app.ui.activity.circle.circleDetail.adapter

import android.view.View
import android.widget.CompoundButton
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import net.linkmate.app.ui.activity.circle.circleDetail.CircleFragmentViewModel

/**
 * @author Raleigh.Luo
 * date：20/8/14 11
 * describe：
 * Context 定义out协变：保留子类型化
 */
open abstract class DialogBaseAdapter<out VM:CircleFragmentViewModel>(val context: Fragment, val fragmentViewModel: VM)
    :  RecyclerView.Adapter<DialogBaseAdapter.ViewHolder>(){
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
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
    //EN数量
    protected val TYPE_EN_COUNT=6

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

    interface OnItemClickListener {
        fun onClick(view: View, position: Int):Boolean
    }
    interface OnItemLongClickListener {
        fun onLongClick(view: View, position: Int):Boolean
    }
    open fun onDestory(){
    }
}