package net.linkmate.app.ui.activity.nasApp.deviceDetial.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import kotlinx.android.synthetic.main.dialog_device_item_detail.view.*
import net.linkmate.app.R
import net.linkmate.app.ui.activity.nasApp.deviceDetial.DeviceDetailFragmentDirections
import net.linkmate.app.ui.activity.nasApp.deviceDetial.DeviceDetailViewModel
import net.linkmate.app.ui.activity.nasApp.deviceDetial.DeviceViewModel
import net.linkmate.app.ui.activity.nasApp.deviceDetial.FunctionHelper

/**远程管理
 * @author Raleigh.Luo
 * date：20/7/27 14
 * describe：
 */
class DeviceRemoteManagerAdapter(context: Fragment, fragmentViewModel: DeviceDetailViewModel,
                                 viewModel: DeviceViewModel, val navController: NavController)
    : DeviceBaseAdapter<DeviceDetailViewModel>(context, fragmentViewModel, viewModel) {
    private val menus: ArrayList<FunctionHelper.DetailMenu>

    init {
        menus = getItemSources()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layout = R.layout.dialog_device_item_detail
        val view =
                LayoutInflater.from(context.requireContext()).inflate(layout, null, false)
        view.layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return menus.size
    }


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        with(holder.itemView) {
            val menu = menus[position]
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


    /**
     * 没有被外部监听器拦截，内部处理点击事件
     * @param position 位置
     */
    override fun internalItemClick(view: View, position: Int) {
//        0->//选择节点
        val function = view.getTag() as Int
        navController.navigate(DeviceDetailFragmentDirections.enterDetial(function))

    }


    override fun internalItemLongClick(view: View, position: Int) {
    }

    fun getItemSources(): ArrayList<FunctionHelper.DetailMenu> {
        val array = ArrayList<FunctionHelper.DetailMenu>()
        //切换节点
        array.add(FunctionHelper.getDeviceMenu(FunctionHelper.DEVICE_CHANGE_NODE))
        //切换网络
        array.add(FunctionHelper.getDeviceMenu(FunctionHelper.DEVICE_CHANGE_NETWORK))
        return array
    }
}