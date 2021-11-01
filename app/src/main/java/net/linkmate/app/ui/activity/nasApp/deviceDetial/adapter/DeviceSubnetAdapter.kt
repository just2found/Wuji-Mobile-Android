package net.linkmate.app.ui.activity.nasApp.deviceDetial.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.dialog_device_item_subnet.view.*
import net.linkmate.app.R
import net.linkmate.app.ui.activity.nasApp.deviceDetial.DeviceDetailViewModel
import net.linkmate.app.ui.activity.nasApp.deviceDetial.DeviceViewModel
import net.sdvn.cmapi.Device.SubNet
import net.sdvn.cmapi.util.CommonUtils

/**
 * @author Raleigh.Luo
 * date：20/7/24 15
 * describe：
 */
class DeviceSubnetAdapter(context: Fragment, fragmentViewModel : DeviceDetailViewModel,
                          viewModel: DeviceViewModel)
    :DeviceBaseAdapter<DeviceDetailViewModel>(context,fragmentViewModel,viewModel)  {
    private  val mDatas: ArrayList<SubNet>
    init {
        mDatas=getItemSources()
    }
    override fun internalItemClick(view: View, position: Int) {
    }

    override fun internalItemLongClick(view: View, position: Int) {
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
                LayoutInflater.from(context.requireContext()).inflate(R.layout.dialog_device_item_subnet2, null, false)
        view.layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return mDatas.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        with(holder.itemView){
           val net = mDatas.get(position).net
            val mask = mDatas.get(position).mask
            val maskShow = if (viewModel.regexMask.matches(mask)) {
                try {
                    CommonUtils.calcPrefixLengthByNetMask(mask).toString()
                } catch (e: Exception) {
                    mask
                }
            } else {
                mask
            }
            tvIP.text = "${net}/$maskShow"
            //最后一个，显示底部线条
            vSubnetBottomLine.visibility = if(position+1==getItemCount()) View.VISIBLE else View.GONE
        }
    }

    private  fun getItemSources():ArrayList<SubNet>{
        val array=viewModel.device.subNets
        return if(array==null)ArrayList() else ArrayList(array)
    }
}