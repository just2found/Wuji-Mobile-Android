package net.linkmate.app.ui.activity.nasApp.deviceDetial.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.dialog_device_item_network.view.*
import net.linkmate.app.R
import net.linkmate.app.ui.activity.nasApp.deviceDetial.DeviceDetailViewModel
import net.linkmate.app.ui.activity.nasApp.deviceDetial.DeviceViewModel
import net.sdvn.cmapi.DevicePrivateModel
import net.sdvn.cmapi.global.Constants
import net.sdvn.common.ErrorCode
import net.sdvn.nascommon.iface.Result
import net.sdvn.nascommon.utils.ToastHelper

/**切换节点
 * @author Raleigh.Luo
 * date：20/7/27 14
 * describe：
 */
class DeviceChangeNodeAdapter(context: Fragment, fragmentViewModel: DeviceDetailViewModel,
                              viewModel: DeviceViewModel)
    : DeviceBaseAdapter<DeviceDetailViewModel>(context, fragmentViewModel, viewModel) {
    private val sources: ArrayList<DataDev> = ArrayList()
    private val model = DevicePrivateModel(viewModel.device.getVip())

    init {
        fragmentViewModel.updateViewStatusParams(bottomIsEnable = false)
        getItemSources()
    }

    //记录上个被选择的位置o
    private var mLastCheckedPosition: Int = -1
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

    data class DataDev(var id: String, var name: String, var vip: String, var isChecked: Boolean)

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        with(holder.itemView) {
            cbNetwork.setOnCheckedChangeListener(null)
            tvNetworkName.text = sources[position].name
            tvNetworkDescribe.text = sources[position].vip
            val isChecked = sources[position].isChecked
            if (mLastCheckedPosition == -1) {
                cbNetwork.isChecked = isChecked
                if (isChecked) {
                    //初始化
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
                    if (cbNetwork.isChecked) {
                        //不能取消自己
                        cbNetwork.isChecked = false
                        mLastCheckedPosition = -1
                    }
                }

            }
        }
    }

    //
    override fun internalItemClick(view: View, position: Int) {
        val id = if (mLastCheckedPosition == -1) {
            ""
        } else {
            val index: Int = mLastCheckedPosition
            val item = sources.get(index)
            item.id
        }
        viewModel.addDisposable(Observable.create<Result<*>> { emitter ->
            val code = model.selectSmartNode(id)
            if (code == Constants.CE_SUCC) {
                emitter.onNext(net.sdvn.nascommon.iface.Result<String>(id))
                for (source in sources) {
                    source.isChecked = model.baseInfo?.hadSelectedSn(source.id) ?: false
                }
            } else {
                emitter.onNext(Result<Any?>(code, ""))
            }
        }.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { result: Result<*> ->
                    if (result.isSuccess) {
                        mLastCheckedPosition = -1
                        notifyDataSetChanged()
                        ToastHelper.showLongToast(R.string.success)
                    } else {
                        ToastHelper.showLongToast(ErrorCode.error2String(result.code))
                    }
                })
    }

    override fun internalItemLongClick(view: View, position: Int) {
    }

    fun getItemSources() {
        viewModel.setLoadingStatus(true)
        viewModel.addDisposable(Observable.create<Result<List<DataDev>>> { emitter ->
            val baseInfoCode: Int = model.refreshRemoteBaseInfo()
            val code: Int = model.refreshRemoteDevices()
            if (baseInfoCode == Constants.CE_SUCC && code == Constants.CE_SUCC) {
                val devices = model.getDevices()
                val results: MutableList<DataDev> = mutableListOf()
                val baseInfo = model.baseInfo
                if (devices != null && baseInfo != null) {
                    val iterator = devices.iterator()
                    while (iterator.hasNext()) {
                        val next = iterator.next()
                        if (next.isOnline && next.selectable) {
                            results.add(DataDev(next.id, next.name, next.vip,
                                    baseInfo.hadSelectedSn(next.id)))
                        }
                    }
                }
                emitter.onNext(Result(results))
            } else {
                emitter.onNext(Result(code, ""))
            }
        }.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { result: Result<List<DataDev>> ->
                    viewModel.setLoadingStatus(false)
                    if (result.isSuccess) {
                        sources.addAll(result.data)
                        if (sources.size > 0) {
                            if (sources.size > 1 || !sources[0].isChecked) {
                                //并非只有一个节点，或未被选中
                                fragmentViewModel.updateViewStatusParams(bottomIsEnable = true)
                            }

                        } else {
                            ToastHelper.showToast(R.string.tips_this_net_no_dev)
                        }
                        notifyDataSetChanged()
                    } else {
                        ToastHelper.showLongToast(ErrorCode.error2String(result.code))
                    }
                })
    }
}