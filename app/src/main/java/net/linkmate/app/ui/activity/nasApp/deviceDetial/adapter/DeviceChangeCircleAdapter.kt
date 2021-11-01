package net.linkmate.app.ui.activity.nasApp.deviceDetial.adapter

import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Observer
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.dialog_device_item_network.view.*
import libs.source.common.utils.ToastHelper
import net.linkmate.app.R
import net.linkmate.app.ui.activity.nasApp.deviceDetial.DeviceViewModel
import net.linkmate.app.util.ToastUtils
import net.sdvn.cmapi.DevicePrivateModel
import net.sdvn.cmapi.global.Constants
import net.sdvn.common.ErrorCode
import net.sdvn.common.repo.NetsRepo
import net.sdvn.nascommon.iface.Result

/**设备切圈
 * @author Raleigh.Luo
 * date：21/6/15 13
 * describe：
 */
class DeviceChangeCircleAdapter(context: Fragment, fragmentViewModel: DeviceChangeCircleViewModel,
                                viewModel: DeviceViewModel)
    : DeviceBaseAdapter<DeviceChangeCircleViewModel>(context, fragmentViewModel, viewModel) {
    private val model = DevicePrivateModel(viewModel.device.getVip())

    //记录上个被选择的位置o
    private var mLastCheckedPosition: Int = -1

    init {
        fragmentViewModel.deviceId = viewModel.device.id
        fragmentViewModel.sources.observe(context, Observer {
            if (it.size > 0) {
                if (it.size > 1 || !isDeviceCurrentNetwork(it[0].netId)) {
                    //并非只有一个网络，或未被选中，底部按可用
                    fragmentViewModel.updateViewStatusParams(bottomIsEnable = true)
                } else {
                    fragmentViewModel.updateViewStatusParams(bottomIsEnable = false)
                }
            }
            notifyDataSetChanged()
        })
        fragmentViewModel.deviceJoinCircleResult.observe(context, Observer {
            if (it) {
                mLastCheckedPosition = -1
                ToastUtils.showToast(context.getString(R.string.transfer_device_successful))
                NetsRepo.refreshNetList()
                //关闭弹框
                viewModel.toFinishActivity()
            } else {//请求失败，刷新列表
                viewModel.setLoadingStatus(false)
                mLastCheckedPosition = -1
                notifyDataSetChanged()
            }
        })


    }

    /**
     *是否是设备当前所属网络
     */
    private fun isDeviceCurrentNetwork(networkId: String): Boolean {
        val netId = viewModel.device.hardData?.networkId
        //当networkId不在绑定的圈子中,说
        var result = false
        if (!TextUtils.isEmpty(netId)) {
            result = networkId == netId
        } else {
            val networkIds = viewModel.device.hardData?.networkIds
            val isExsit = networkIds?.find {
                it == networkId
            }
            if (!TextUtils.isEmpty(isExsit)) {
                result = true
            }
        }
        return result
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
        return fragmentViewModel.sources.value?.size ?: 0
    }


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        with(holder.itemView) {
            cbNetwork.setOnCheckedChangeListener(null)
            val source = fragmentViewModel.sources.value?.get(position)
            tvNetworkName.text = source?.netName
            tvNetworkDescribe.text = source?.owner

            val isChecked = isDeviceCurrentNetwork(source?.netId ?: "")

            if (mLastCheckedPosition == -1) {
                cbNetwork.isChecked = isChecked
                if (isChecked) {
                    mLastCheckedPosition = position
                }
            } else {
                cbNetwork.isChecked = mLastCheckedPosition == position
            }
            cbNetwork.setOnCheckedChangeListener { compoundButton, b ->
                if (b) {//选择了其他网络
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

    override fun internalItemLongClick(view: View, position: Int) {
    }

    override fun internalItemClick(view: View, position: Int) {
        if (mLastCheckedPosition == -1) return
        val id = fragmentViewModel.sources.value?.get(mLastCheckedPosition)?.netId
        id?.let {
            fragmentViewModel.startDeviceJoinCircle(it)
        }
    }
}