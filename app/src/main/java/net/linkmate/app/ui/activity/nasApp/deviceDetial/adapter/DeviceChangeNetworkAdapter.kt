package net.linkmate.app.ui.activity.nasApp.deviceDetial.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.dialog_device_item_network.view.*
import net.linkmate.app.R
import net.linkmate.app.ui.activity.nasApp.deviceDetial.DeviceDetailViewModel
import net.linkmate.app.ui.activity.nasApp.deviceDetial.DeviceViewModel
import net.sdvn.cmapi.DevicePrivateModel
import net.sdvn.cmapi.Network
import net.sdvn.cmapi.global.Constants
import net.sdvn.common.ErrorCode
import net.sdvn.nascommon.iface.Result
import net.sdvn.nascommon.utils.ToastHelper

/** 切换网络
 * @author Raleigh.Luo
 * date：20/7/27 14
 * describe：
 */
class DeviceChangeNetworkAdapter(context: Fragment, fragmentViewModel: DeviceDetailViewModel,
                                 viewModel: DeviceViewModel)
    : DeviceBaseAdapter<DeviceDetailViewModel>(context, fragmentViewModel, viewModel) {
    private val sources: ArrayList<Network> = ArrayList()
    private val model = DevicePrivateModel(viewModel.device.getVip())

    //记录上个被选择的位置o
    private var mLastCheckedPosition: Int = -1

    init {
        fragmentViewModel.updateViewStatusParams(bottomIsEnable = false)
        getItemSources()
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
            tvNetworkName.text = sources[position].name
            tvNetworkDescribe.text = sources[position].owner

            val isChecked = sources[position].isCurrent

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
        if(mLastCheckedPosition == -1) return
            val id: String = sources.get(mLastCheckedPosition).id
            addDisposable(Observable.create<Result<*>> { emitter ->
                val code = model.switchNetwork(id)
                if (code == Constants.CE_SUCC || code == Constants.CE_PENDING) {
                    emitter.onNext(net.sdvn.nascommon.iface.Result<String>(id))
                } else {
                    emitter.onNext(Result<Any?>(code, ""))
                }
            }.subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe { result: Result<*> ->
                        if (result.isSuccess) {
                            mLastCheckedPosition = -1
                            sources.clear()
                            model.networks?.let {
                                sources.addAll(it)
                            }
                            notifyDataSetChanged()
                            ToastHelper.showLongToast(R.string.switch_success)
                        } else {
                            ToastHelper.showLongToast(ErrorCode.error2String(result.code))
                        }
                    })

    }

    private fun getItemSources() {
        addDisposable(Observable.create<Result<List<Network>>> { emitter ->
            val code: Int = model.refreshNetworks()
            if (code == Constants.CE_SUCC) {
                emitter.onNext(net.sdvn.nascommon.iface.Result<List<Network>>(model.getNetworks()))
            } else {
                emitter.onNext(Result<List<Network>>(code, ""))
            }
        }.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { result: Result<List<Network>> ->
                    if (result.isSuccess) {
                        sources.addAll(result.data)
                        if (sources.size > 0) {
                            if (sources.size > 1 || !sources[0].isCurrent) {
                                //并非只有一个网络，或未被选中
                                fragmentViewModel.updateViewStatusParams(bottomIsEnable = true)
                            }
                        } else {
                            ToastHelper.showLongToast(R.string.tips_this_net_no_dev)
                        }
                        notifyDataSetChanged()
                    } else {
                        ToastHelper.showLongToast(ErrorCode.error2String(result.code))
                    }
                })
    }

    private var compositeDisposable: CompositeDisposable? = null
    fun addDisposable(disposable: Disposable) {
        if (compositeDisposable == null) {
            compositeDisposable = CompositeDisposable()
        }
        compositeDisposable?.add(disposable)
    }

    override fun onDestory() {
        super.onDestory()
        compositeDisposable?.dispose()
    }
}