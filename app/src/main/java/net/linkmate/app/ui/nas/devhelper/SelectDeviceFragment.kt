package net.linkmate.app.ui.nas.devhelper

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.rxjava.rxlife.RxLife
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.functions.Consumer
import io.weline.devhelper.IconHelper
import io.weline.repo.torrent.BTHelper
import kotlinx.android.synthetic.main.fragment_select_device.*
import kotlinx.android.synthetic.main.include_swipe_refresh_and_recycle_view.*
import libs.source.common.livedata.Status
import net.linkmate.app.R
import net.linkmate.app.ui.nas.TipsBackPressedFragment
import net.sdvn.app.config.AppConfig
import net.sdvn.cmapi.CMAPI
import net.sdvn.common.internet.core.HttpLoader
import net.sdvn.common.repo.BriefRepo
import net.sdvn.common.vo.BriefModel
import net.sdvn.nascommon.LibApp
import net.sdvn.nascommon.SessionManager
import net.sdvn.nascommon.constant.AppConstants
import net.sdvn.nascommon.fileserver.FileShareHelper
import net.sdvn.nascommon.iface.Callback
import net.sdvn.nascommon.model.DeviceModel
import net.sdvn.nascommon.utils.SPUtils
import net.sdvn.nascommon.utils.ToastHelper
import net.sdvn.nascommon.viewmodel.DeviceViewModel
import net.sdvn.nascommon.widget.SELF
import org.view.libwidget.handler.DelayedUnit
import org.view.libwidget.handler.DelayedUtils
import org.view.libwidget.setOnRefreshWithTimeoutListener

/**
 * Description:
 * @author  admin
 * CreateDate: 2021/6/11
 */

class SelectDeviceFragment : TipsBackPressedFragment(), HttpLoader.HttpLoaderStateListener {
    private val mDeviceViewModel by viewModels<DeviceViewModel>({ requireActivity() })
    private val navArgs by navArgs<SelectDeviceFragmentArgs>()
    private lateinit var filterExtIdsL: List<String>

    val mDeviceRVAdapter = object : BaseQuickAdapter<DeviceModel, BaseViewHolder>
    (R.layout.item_listview_choose_device) {
        private var briefs: List<BriefModel>? = null
        override fun convertPayloads(
                helper: BaseViewHolder,
                item: DeviceModel?,
                payloads: MutableList<Any>
        ) {
            if (item != null) {
                convert(helper, item)
            }
        }

        override fun setNewData(data: List<DeviceModel>?) {
            if (data != null && data.size > 0) {
                val ids = data.map {
                    it.devId
                }.toTypedArray()
                //加载简介数据
                briefs = BriefRepo.getBriefs(ids, BriefRepo.FOR_DEVICE)
            } else {
                briefs = null
            }
            super.setNewData(data)
        }

        override fun convert(holder: BaseViewHolder, item: DeviceModel) {
            if (item.devId == SELF) {
                holder.setText(R.id.tv_device_name, CMAPI.getInstance().baseInfo.deviceName)
                holder.setText(R.id.tv_device_ip, R.string.current_device)
                holder.setImageResource(
                        R.id.iv_device,
                        IconHelper.getIconByeDevClassSimple(AppConfig.CONFIG_DEV_CLASS)
                )
            } else {
                if (SPUtils.getBoolean(AppConstants.SP_SHOW_REMARK_NAME, true)) {
                    item.devNameFromDB
                            .observeOn(AndroidSchedulers.mainThread())
                            .`as`(RxLife.`as`(view))
                            .subscribe { s -> holder.setText(R.id.tv_device_name, s) }
                } else {
                    val deviceModel = SessionManager.getInstance().getDeviceModel(item.devId)
                    if (deviceModel != null) {
                        deviceModel.device?.name
                        if (!deviceModel.device?.name.isNullOrEmpty()) {
                            holder.setText(R.id.tv_device_name, deviceModel.device?.name)
                        } else {
                            holder.setText(R.id.tv_device_name, item.devName)
                        }
                    }
                }

                holder.setGone(R.id.select_box, item.isOnline)
                holder.setText(R.id.tv_device_ip, item.device?.vip ?: "")
                val iconByeDevClass = IconHelper.getIconByeDevClassSimple(
                        item.devClass
                )
                val iconView = holder.getView<ImageView>(R.id.iv_device)
                if (holder.itemView.getTag() != item.devId) {
                    iconView.setTag(null)
                    holder.itemView.setTag(item.devId)
                }
                if (iconView.getTag() == null) iconView.setImageResource(iconByeDevClass)
                val brief = briefs?.find {
                    it.deviceId == item.devId
                }
                LibApp.instance.getBriefDelegete().loadDeviceBrief(item.devId, brief, iconView, null, defalutImage = iconByeDevClass)
            }
            holder.setChecked(R.id.select_box, toId == item.devId)
        }
    }


    var toId: String? = null
    override fun initView(view: View) {
        filterExtIdsL = navArgs.filterExtIds ?: emptyList()
        titleBackLayout
                .setBackVisible(true)
                .setBackTitle(R.string.tv_select_device)
                .setRightText(R.string.confirm)
                .setOnRightTextClickListener {
                    if (toId.isNullOrEmpty()) {
                        ToastHelper.showLongToast(R.string.tip_select_device)
                        return@setOnRightTextClickListener
                    }
                    setFragmentResult(navArgs.requestKey, Bundle().apply {
                        putString(AppConstants.SP_FIELD_DEVICE_ID, toId)
                    })
                    findNavController().popBackStack()
                }
                .setOnBackClickListener {
                    setFragmentResult(navArgs.requestKey, Bundle().apply {
                        putString(AppConstants.SP_FIELD_DEVICE_ID, "")
                    })
                    findNavController().popBackStack()
                }


        val mRecycleView: RecyclerView = view.findViewById(R.id.recycle_view)
        val layoutManager = LinearLayoutManager(mRecycleView.context)
        mRecycleView.layoutManager = layoutManager

        mDeviceRVAdapter.onItemClickListener =
                BaseQuickAdapter.OnItemClickListener { adapter, view, position ->
                    val o = adapter.data[position] as DeviceModel
                    toId = o.devId
                    adapter.notifyItemRangeChanged(0, adapter.itemCount, listOf(o))
                }
        mRecycleView.let {
            it.layoutManager = LinearLayoutManager(context)
            it.adapter = mDeviceRVAdapter
        }
        val swipeRefreshLayout = view.findViewById<SwipeRefreshLayout>(R.id.swipe_refresh_layout)
        swipeRefreshLayout
                .setOnRefreshWithTimeoutListener(SwipeRefreshLayout.OnRefreshListener {
                    mDeviceViewModel.updateDevices(null)
                })
        mDeviceViewModel.liveDevices.observe(this, Observer {
            refreshListData(it)
        })
    }


    private fun refreshListData(list: List<DeviceModel> ) {
        mDeviceList=list
        DelayedUtils.addDelayedUnit(DelayedUtils.DELAY_DEVICE_MANAGE_LIST, DelayedUnit(mDelayRunnable, delayTime = 500, maxIntervalTime = 2000))
    }

    private val mDelayRunnable = Runnable {
        dealData()
    }

    private var mDeviceList: List<DeviceModel>? = null
    private fun dealData() {
        if (mDeviceList.isNullOrEmpty()) {
            mDeviceRVAdapter.setNewData(mutableListOf<DeviceModel>())
            return
        }
        val filter = filter(mDeviceList!!)
        val result = if (navArgs.isLocalEnable) {
            filter?.toMutableList()?.apply {
                add(0, DeviceModel(SELF).apply {
                    val newName = CMAPI.getInstance().baseInfo.deviceName
                    this.setDeviceName(newName)
                    this.setMarkName(newName)
                })
            }
        } else {
            filter
        }
        if (result.isNullOrEmpty()) {
            val inflate = layoutInflater.inflate(R.layout.layout_empty_text, null)
            inflate.findViewById<TextView>(R.id.tv_tips).setText(R.string.tips_this_net_no_dev)
            mDeviceRVAdapter.emptyView = inflate
        }
        mDeviceRVAdapter.setNewData(result)
    }

    override fun onResume() {
        super.onResume()
        mDeviceViewModel.updateDevices(this)
    }

    fun filter(list: List<DeviceModel>): List<DeviceModel>? {
        val filter = if (filterExtIdsL.isNotEmpty()) {
            list.filter {
                !filterExtIdsL.contains(it.devId)
            }
        } else {
            list
        }
        return when (navArgs.filterType) {
            SelectDeviceFragmentArgs.Companion.FilterType.BT_DOWNLOAD -> {
                filter.filter { devModel ->
                    (devModel.isEnableDownloadShare()).also {
                        if (it && !devModel.isBtServerAvailable) {
                            BTHelper.checkAvailable(devModel.device?.vip, Consumer {
                                if (it.status == Status.SUCCESS) {
                                    devModel.isBtServerAvailable = true
                                }
                            })
                        }
                    } && devModel.isBtServerAvailable && devModel.isInCurrentNet() && devModel.isEnableUseSpace
                }
            }
            SelectDeviceFragmentArgs.Companion.FilterType.FILE_SHARE -> {
                filter.filter { devModel ->
                    (devModel.isEnableDownloadShare()).also {
                        if (it && !devModel.isShareV2Available) {
                            FileShareHelper.checkAvailable(devModel.device?.vip, Callback {
                                if (it.isSuccess) {
                                    devModel.isShareV2Available = true
                                }
                            })
                        }
                    } && devModel.isShareV2Available && devModel.isEnableUseSpace
                }
            }
            SelectDeviceFragmentArgs.Companion.FilterType.IS_ONLINE -> {
                filter.filter {
                    it.isOnline
                }
            }
            else -> {
                filter
            }
        }
    }


    override fun getTopView(): View? {
        return titleBackLayout
    }

    override fun getLayoutResId(): Int {
        return R.layout.fragment_select_device
    }

    override fun isEnableOnBackPressed(): Boolean {
        return true
    }

    override fun onBackPressed(): Boolean {
        setFragmentResult(navArgs.requestKey, Bundle().apply {
            putString(AppConstants.SP_FIELD_DEVICE_ID, "")
        })
        return findNavController().popBackStack()
    }

    override fun onLoadComplete() {
        swipe_refresh_layout.isRefreshing = false
    }

    override fun onLoadStart(disposable: Disposable?) {
        swipe_refresh_layout.isRefreshing = true
    }

    override fun onLoadError() {
        swipe_refresh_layout.isRefreshing = false
    }

}