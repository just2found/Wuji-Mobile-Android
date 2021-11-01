package net.linkmate.app.ui.activity.nasApp.deviceDetial.adapter

import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.dialog_device_item_network.view.*
import net.linkmate.app.R
import net.linkmate.app.ui.activity.nasApp.deviceDetial.DeviceDetailViewModel
import net.linkmate.app.ui.activity.nasApp.deviceDetial.DeviceViewModel
import net.linkmate.app.util.ToastUtils
import net.sdvn.cmapi.CMAPI
import net.sdvn.cmapi.global.Constants
import net.sdvn.cmapi.protocal.ResultListener
import net.sdvn.common.ErrorCode
import net.sdvn.common.repo.NetsRepo
import net.sdvn.common.vo.NetworkModel
import java.util.*

/** 所处网络
 * @author Raleigh.Luo
 * date：20/7/24 14
 * describe：
 */
class DeviceNetWorkAdapter(context: Fragment, fragmentViewModel: DeviceDetailViewModel,
                           viewModel: DeviceViewModel)
    : DeviceBaseAdapter<DeviceDetailViewModel>(context, fragmentViewModel, viewModel) {
    private val sources: ArrayList<NetworkModel>

    //记录上个被选择的位置o
    private var mLastCheckedPosition: Int = -1

    init {
        sources = getItemSources()
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
                LayoutInflater.from(context.requireContext()).inflate(R.layout.dialog_device_item_network, null, false)
        view.layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return sources.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        with(holder.itemView) {
            cbNetwork.setOnCheckedChangeListener(null)
            tvNetworkName.text = sources.get(position).netName
            var name = sources.get(position).nickname
            if (TextUtils.isEmpty(name)) name = sources.get(position).owner
            tvNetworkDescribe.text = name
            setPaddingRelative(paddingLeft, if (position == 0) paddingLeft else 0, paddingLeft, paddingLeft)
            if (mLastCheckedPosition == -1) {//第一次加载
                //是当前网络,且第一次
                val isCurrent = NetsRepo.getCurrentNet()?.netId == sources.get(position).netId
                cbNetwork.isChecked = isCurrent
                if (isCurrent) {
                    mLastCheckedPosition = position
                }
            } else {
                cbNetwork.isChecked = mLastCheckedPosition == position
            }
            cbNetwork.setOnCheckedChangeListener { compoundButton, b ->
                if (b) {//选择了其他网络
                    //fragmentViewModel.updateViewStatusParams(bottomIsEnable = false)
                    val tempLastPositon = mLastCheckedPosition
                    mLastCheckedPosition = position
                    //取消上一个勾选
                    notifyItemChanged(tempLastPositon)
                } else {
                    //不能取消自己
                    cbNetwork.isChecked = true
                }
            }
        }
    }

    private fun getItemSources(): ArrayList<NetworkModel> {
        val networks: ArrayList<NetworkModel> = ArrayList()
        val netId = viewModel.device.hardData?.networkId
        val networkIds = viewModel.device.hardData?.networkIds
        networkIds?.forEach { networkId ->
            NetsRepo.getOwnNetwork(networkId)?.let {
                networks.add(it)
            }
        }
        //当networkId不在绑定的圈子中,说
        if (!netId.isNullOrEmpty()) {
            NetsRepo.getOwnNetwork(netId)?.let {
                networks.clear()
                networks.add(it)
            }
        }
        return networks
    }

    override fun internalItemClick(view: View, position: Int) {
        if (view.id == R.id.btnBottomConfirm) {//进入所选网络
            toNetwork()
        }
    }

    override fun internalItemLongClick(view: View, position: Int) {
    }

    /**
     * 进入所选网络
     */
    fun toNetwork() {
        if (mLastCheckedPosition == -1) return
        val network = sources.get(mLastCheckedPosition)
        val isCurrent = NetsRepo.getCurrentNet()?.netId == sources.get(mLastCheckedPosition).netId
        if (!isCurrent) {
            viewModel.setLoadingStatus(true,false)
            CMAPI.getInstance().switchNetwork(network.netId, object : ResultListener {
                override fun onError(error: Int) {
                    viewModel.setLoadingStatus(false)
                    if (error == Constants.CE_SUCC) {
                        NetsRepo.refreshNetList()
                        //关闭弹框
                        viewModel.toFinishActivity()
                    } else {
                        ToastUtils.showToast(context.getString(ErrorCode.error2String(error)));
                    }
                }
            })
        }else{
            ToastUtils.showToast(context.getString(R.string.currently_in_this_circle))
        }
    }
}