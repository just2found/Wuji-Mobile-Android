package net.linkmate.app.ui.nas.torrent

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.entity.SectionEntity
import com.rxjava.rxlife.RxLife
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers
import io.weline.repo.torrent.BTHelper
import io.weline.repo.torrent.constants.BTStatus
import io.weline.repo.torrent.data.BTItem
import kotlinx.android.synthetic.main.fragment_torrent.*
import kotlinx.android.synthetic.main.include_swipe_refresh_and_rv.*
import libs.source.common.livedata.Status
import net.linkmate.app.R
import net.linkmate.app.manager.SDVNManager
import net.linkmate.app.manager.SDVNManager.Companion.instance
import net.linkmate.app.ui.nas.TipsBaseFragment
import net.linkmate.app.ui.scan.ScanActivity
import net.linkmate.app.ui.viewmodel.DevSelectViewModel
import net.linkmate.app.ui.viewmodel.TorrentsViewModel
import net.linkmate.app.util.DialogUtil
import net.linkmate.app.util.ToastUtils
import net.linkmate.app.view.SimDividerItemDecoration
import net.sdvn.cmapi.CMAPI
import net.sdvn.cmapi.global.Constants
import net.sdvn.common.ErrorCode
import net.sdvn.nascommon.SessionManager
import net.sdvn.nascommon.constant.AppConstants
import net.sdvn.nascommon.iface.Callback
import net.sdvn.nascommon.model.DeviceModel
import net.sdvn.nascommon.rx.RxWorkLife
import net.sdvn.nascommon.utils.DialogUtils
import net.sdvn.nascommon.utils.ToastHelper
import net.sdvn.nascommon.utils.Utils
import net.sdvn.nascommon.viewmodel.DeviceViewModel
import org.view.libwidget.setOnRefreshWithTimeoutListener
import org.view.libwidget.showRefreshAndNotify
import org.view.libwidget.singleClick
import timber.log.Timber
import java.util.concurrent.TimeUnit

class TorrentsFragment : TipsBaseFragment() {
    private var refreshProgressDisposable: Disposable? = null
    private val deviceViewModel by viewModels<DeviceViewModel>()
    private val devSelectViewModel by viewModels<DevSelectViewModel>()
    private val torrentsViewModel by viewModels<TorrentsViewModel>()
    private val availableDevices = mutableSetOf<DeviceModel>()
    private val mapOfIsOwner = mutableMapOf<String, Boolean>()
    private lateinit var rxWorkLife: RxWorkLife
    private var hasAdminRights: Boolean = false
    private var isOnlyDownload: Boolean = false
    private var btName: String? = null
    override fun onBackPressed(): Boolean {
        return false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        rxWorkLife = RxWorkLife(this)
        refreshArgs()
        if (devId != null) {
            if (BTHelper.isLocal(devId!!)) {
                hasAdminRights = true
            } else {
                val deviceModel = SessionManager.getInstance().getDeviceModel(devId)
                if (deviceModel != null) {
                    hasAdminRights = deviceModel.hasAdminRights()
                }
            }
        }
        deviceViewModel.liveDevices.observe(this, Observer {
            availableDevices.clear()
            it.filter { devModel ->
                devModel.isOnline.also {
                    if (!devModel.isBtServerAvailable) {
                        val checkAvailable = BTHelper.checkAvailable(devModel.device?.vip, Consumer {
                            if (it.status == Status.SUCCESS) {
                                devModel.isBtServerAvailable = true
                                availableDevices.add(devModel)
                                refreshData()
                            }
                        })
                        rxWorkLife.addDisposable(checkAvailable)
                    }
                } && devModel.isBtServerAvailable
            }.let {
                Timber.d(it.toString())
                availableDevices.addAll(it)
                refreshData()
            }
        })
        if (devId == null) {
            torrentsViewModel.torrentsMapLiveData.observe(this, Observer {
                swipe_refresh_layout?.isRefreshing = false
                if (devId == null) {
                    val list = mutableListOf<SectionEntity<BTItem>>()
                    val mapProgress = mutableMapOf<String, List<BTItem>>()
                    it?.forEach { entry ->
                        entry.value?.takeIf { it -> it.isNotEmpty() }?.let { list1 ->
                            val elementHeader = SectionEntity<BTItem>(true, deviceViewModel.mapsDevName[entry.key])
                            list.add(elementHeader)
                            var count = 0
                            list1.sortedByDescending { btItem ->
                                btItem.timestamp
                            }.forEach { btItem ->
                                if (filterListEnable(btItem, entry.key, isOnlyDownload)) {
                                    list.add(SectionEntity(btItem))
                                    count++
                                }
                            }
                            if (count == 0) {
                                list.remove(elementHeader)
                            }
                            if (mapOfIsOwner[entry.key] == true) {
                                list1.filter { btItem ->
                                    filterProgressEnable(btItem)
                                }.let { list2 ->
                                    if (list2.isNotEmpty()) {
                                        mapProgress.put(entry.key, list2)
                                    }
                                }
                            }
                        }
                    }
                    doRefreshProgress(mapProgress)
                    torrentsAdapter.setNewData(list)
                }
            })
        }
        torrentsViewModel.torrentsLiveData.observe(this, Observer {
            swipe_refresh_layout?.isRefreshing = false
            if (devId != null) {
                torrentsAdapter.setNewData(it?.filter { btItem ->
                    filterListEnable(btItem, devId!!, isOnlyDownload)
                }?.sortedByDescending { btItem ->
                    btItem.timestamp
                }?.map { SectionEntity(it) })
                if (hasAdminRights && it != null) {
                    it.filter { btItem ->
                        filterProgressEnable(btItem)
                    }.let {
                        if (it.isNotEmpty()) {
                            doRefreshProgress(mapOf(devId!! to it))
                        }
                    }

                }
                if(btName != null && !it.isNullOrEmpty() && !isClick){
                    isClick = true
                    val item = torrentsAdapter.getItem(0)?.t
                    onItemClick(item)
                }
            }
        })
    }
    private var isClick = false

    private fun refreshArgs() {
        isOnlyDownload = arguments?.getBoolean(ARG_IS_ONLY_DOWNLOAD, false) ?: false
        btName = arguments?.getString("btName",null)
    }

    private fun filterProgressEnable(btItem: BTItem) =
            (btItem.status != BTStatus.COMPLETE
                    && !btItem.isMainSeed)

    private fun filterListEnable(btItem: BTItem, key: String, isOnlyDownload: Boolean = false): Boolean {
        return if (isOnlyDownload) {
            ((mapOfIsOwner[key] == true && !btItem.isMainSeed)
                    || btItem.status == BTStatus.COMPLETE
                    || btItem.isMainSeed && btItem.status == BTStatus.COMPLETE)
        } else {
            (mapOfIsOwner[key] == true
                    || btItem.status == BTStatus.COMPLETE
                    || btItem.isMainSeed && btItem.status == BTStatus.COMPLETE)
        }
    }

    private fun doRefreshProgress(map: Map<String, List<BTItem>>) {
        onStopRefreshProgress()
        refreshProgressDisposable = Observable.interval(1200, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .`as`(RxLife.`as`(this))
                .subscribe {
                    if (isResumed) {
                        map.forEach { entry ->
                            val dlTickets = entry.value.filter { it.status == BTStatus.DOWNLOADING }
                                    .map { btItem -> btItem.dlTicket }
                            if (dlTickets.isNotEmpty()) {
                                torrentsViewModel.getProgress(entry.key, dlTickets)
                            }
                        }
                    }
                }
    }

    private fun onStopRefreshProgress() {
        if (refreshProgressDisposable != null) {
            refreshProgressDisposable!!.dispose()
            refreshProgressDisposable = null
        }
    }

    override fun onStop() {
        super.onStop()
        onStopRefreshProgress()
    }

    override fun getLayoutResId(): Int {
        return R.layout.fragment_torrent
    }

    override fun getTopView(): View? {
        return title_layout
    }

    private val torrentsAdapter = TorrentMapAdapter()

    override fun initView(view: View) {
        iv_return.setOnClickListener {
            requireActivity().finish()
        }
        mTipsBar = tipsBar
        recycle_view?.let { recyclerView ->
            recyclerView.layoutManager = LinearLayoutManager(view.context)
            recyclerView.addItemDecoration(SimDividerItemDecoration(view.context))
            recyclerView.adapter = torrentsAdapter
            torrentsAdapter.setEmptyView(R.layout.layout_empty_view, recyclerView)
            torrentsAdapter.setOnItemClickListener { baseQuickAdapter, view, i ->
                if (Utils.isFastClick(view)) {
                    return@setOnItemClickListener
                }
                val item = (baseQuickAdapter.getItem(i) as? SectionEntity<BTItem>)?.t
                onItemClick(item)

            }
            torrentsAdapter.setOnItemLongClickListener { baseQuickAdapter, view, i ->
                val item = (baseQuickAdapter.getItem(i) as? SectionEntity<BTItem>)?.t
                if (item != null && mapOfIsOwner[item.devId] == true) {
                    showDesItem(requireContext(), item, i, baseQuickAdapter)
                    return@setOnItemLongClickListener true
                }
                return@setOnItemLongClickListener false
            }
        }
        swipe_refresh_layout.setOnRefreshWithTimeoutListener(SwipeRefreshLayout.OnRefreshListener {
            refreshData()
        }, 1000)
        view_scan.isVisible = true
        view_scan.singleClick {
            val intent = Intent(requireContext(), ScanActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivityForResult(intent, 110)
        }
        view_filter.isVisible = isOnlyDownload
        view_filter.singleClick {
            devSelectViewModel.showSelectPopup(requireActivity(),
                    it, devId, availableDevices.toList(), Callback {
                devId = it
                refreshData()
            })
        }
    }

    private fun onItemClick(item: BTItem?){
        if (item != null) {
            if (item.status == BTStatus.COMPLETE || item.isMainSeed) {
                if (item.seeding) {
                    val deviceModel = SessionManager.getInstance().getDeviceModel(item.devId)
                    if (deviceModel != null) {
                        val netId = item.netId
                        if (SDVNManager.instance.isCurrentNet(netId)) {
                            if (deviceModel.isInNetProvide) {
                                item.remoteServer = deviceModel.device?.domain ?: ""
                                torrentsViewModel.showBtItemQRCodeView(requireActivity(), item, deviceModel.hasAdminRights())
                            } else {
                                ToastHelper.showLongToast(R.string.completed)
                            }
                        } else {
                            val bean = instance.getNetById(netId)
                            if (bean != null) {
//                                        if (!devId.isNullOrEmpty() && !deviceModel.isInNet(netId)) {
//                                            ToastHelper.showLongToast(R.string.resource_expired)
//                                            return@setOnItemClickListener
//                                        }
                                //show join circle
                                DialogUtil.showSelectDialog(requireContext(),  //                                            String.format(, bean.getName()),
                                        getString(R.string.tips_switch_to_circle),
                                        getString(R.string.confirm), DialogUtil.OnDialogButtonClickListener { v: View?, strEdit: String?, dialog: Dialog, isCheck: Boolean ->
                                    dialog.dismiss()
                                    CMAPI.getInstance().switchNetwork(bean.id) { error: Int ->
                                        if (error != Constants.CE_SUCC) {
                                            ToastUtils.showToast(getString(ErrorCode.error2String(error)))
                                        } else {
                                            ToastHelper.showLongToast(R.string.switch_success)
                                        }
                                    }
                                }, getString(R.string.cancel), null)
                            } else {
                                if (deviceModel.isOwner) {
                                    DialogUtils.showConfirmDialog(requireContext(),
                                            R.string.resource_expired,
                                            R.string.waring_remove_title,
                                            R.string.confirm,
                                            R.string.cancel) { dialog, isPositiveBtn ->
                                        if (isPositiveBtn) {
                                            dialog.dismiss()
                                            torrentsViewModel.cancel(devId!!, item)
                                        }
                                    }
                                } else {
                                    ToastHelper.showLongToast(R.string.resource_expired)
                                }
                            }
                            return
                        }

                    } else {
                        ToastHelper.showLongToast(R.string.tip_wait_for_service_connect)
                    }
                } else {
                    ToastHelper.showLongToast(R.string.expired)
                }
            } else {
                doOnItem(item)
            }
        }
    }

    private fun doOnItem(item: BTItem) {
        when (item.status) {
            BTStatus.DOWNLOADING -> {
                torrentsViewModel.stop(item.devId, item)
            }
            BTStatus.STOPPED -> {
                torrentsViewModel.resume(item.devId, item.dlTicket)
            }
        }
    }

    private fun showDesItem(context: Context, item: BTItem, position: Int, adapter: BaseQuickAdapter<*, *>) {
        val viewHolder = BtItemViewHolder(context, item)
        val dialog = DialogUtils.showCustomDialog(context, viewHolder.view)
        dialog.setCancelable(true)
        dialog.setCanceledOnTouchOutside(true)
        dialog.setOnDismissListener {
            adapter.notifyItemChanged(position)
        }
        viewHolder.negative.setOnClickListener {
//            if (item.status != BTStatus.COMPLETE) {
//                showTipToRemove(context, item, position, adapter, Callback<Boolean> {
//                    if (it) {
//                        dialog.dismiss()
//                    }
//                })
//            } else {
            dialog.dismiss()
//            }
        }
        viewHolder.positive.setOnClickListener {
//            if (item.status != BTStatus.COMPLETE) {
//                doOnItem(item)
//            } else {
            showTipToRemove(context, item, position, adapter, Callback<Boolean> { t ->
                if (t) {
                    dialog.dismiss()
                }
            })
//            }
        }
    }

    private fun showTipToRemove(context: Context, item: BTItem, position: Int, adapter: BaseQuickAdapter<*, *>, callback: Callback<Boolean>) {
        DialogUtils.showConfirmDialog(context, R.string.waring_remove_title, -1, R.string.confirm,
                R.string.cancel) { dialog, isPositiveBtn ->
            dialog.dismiss()
            if (isPositiveBtn) {
                torrentsViewModel.cancel(item.devId, item)
            }
            callback.result(true)
        }
    }

    override fun onResume() {
        super.onResume()
        refreshArgs()
        refreshData()
        swipe_refresh_layout.showRefreshAndNotify()
    }

    private fun refreshData() {
        if (devId != null) {
            val availableDevice = SessionManager.getInstance().getDeviceModel(devId)
            if (availableDevice != null) {
                getList(availableDevice,btName)
            }
        } else {
            if (availableDevices.isNotEmpty()) {
                for (availableDevice in availableDevices) {
                    getList(availableDevice,btName)
                }
            }
        }
    }

    private fun getList(availableDevice: DeviceModel,btName: String?) {
        if (availableDevice.isOnline) {
            torrentsViewModel.getList(availableDevice.devId,btName, availableDevice.hasAdminRights())
            mapOfIsOwner.put(availableDevice.devId, availableDevice.hasAdminRights())
        }
    }

    companion object {
        const val ARG_IS_ONLY_DOWNLOAD = "arg_is_only_download"
        fun newInstance(devId: String?,name: String?, isOnlyDownload: Boolean = false): TorrentsFragment {
            val fragment = TorrentsFragment()
            val args = Bundle()
            if (!devId.isNullOrEmpty()) {
                args.putString(AppConstants.SP_FIELD_DEVICE_ID, devId)
            }
            if (!name.isNullOrEmpty()) {
                args.putString("btName", name)
            }
            args.putBoolean(ARG_IS_ONLY_DOWNLOAD, isOnlyDownload)
            fragment.arguments = args
            return fragment
        }
    }
}
