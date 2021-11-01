package net.sdvn.nascommon.widget

import android.content.Context
import android.text.SpannableString
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatDialog
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.rxjava.rxlife.RxLife
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.Consumer
import io.weline.repo.torrent.BTHelper
import kotlinx.android.synthetic.main.dialog_device_select.*
import libs.source.common.livedata.Status
import net.sdvn.common.repo.BriefRepo
import net.sdvn.common.vo.BriefModel
import net.sdvn.nascommon.LibApp
import net.sdvn.nascommon.SessionManager
import net.sdvn.nascommon.constant.AppConstants
import net.sdvn.nascommon.constant.OneOSAPIs
import net.sdvn.nascommon.fileserver.FileShareHelper
import net.sdvn.nascommon.iface.Callback
import net.sdvn.nascommon.model.DeviceModel
import net.sdvn.nascommon.model.oneos.OneOSFileType
import net.sdvn.nascommon.utils.SPUtils
import net.sdvn.nascommon.utils.Utils
import net.sdvn.nascommon.viewmodel.DeviceViewModel
import net.sdvn.nascommonlib.R
import org.view.libwidget.setOnRefreshWithTimeoutListener
import org.view.libwidget.showRefreshAndNotify

/**
 *  
 *
 *
 * Created by admin on 2020/7/2,20:32
 */
class DeviceSelectDialog(context: Context,
                         val filterType: FilterType = FilterType.IS_ONLINE,
                         filterExtIds: List<String>? = null,
                         isLocalEnable: Boolean = false,
                         titleResId: Int? = null,
                         showAll: Boolean = false,
                         callback: Callback<String>)
    : AppCompatDialog(context, R.style.DialogTheme) {
    enum class FilterType {
        ALL,
        IS_ONLINE,
        FILE_SHARE,
        BT_DOWNLOAD
    }

    private var filterExtIdsL: List<String> = filterExtIds ?: emptyList()
    private var mBaseViewHolder: BaseViewHolder

    init {
        val view: View = LayoutInflater.from(context).inflate(R.layout.dialog_device_select, null)
        setContentView(view)
        mBaseViewHolder = BaseViewHolder(view)
        var toId: String? = null
        val btnToPhone = mBaseViewHolder.getView<TextView>(R.id.btn_to_phone)
        btnToPhone.isVisible = isLocalEnable
        if (titleResId != null) {
            popup_title.setText(titleResId)
        }
        val mDeviceRVAdapter = object : BaseQuickAdapter<DeviceModel, BaseViewHolder>
        (R.layout.item_listview_choose_device) {
            private var briefs: List<BriefModel>? = null
            override fun setNewData(data: List<DeviceModel>?) {
                if (data == null) {
                    this.mData = arrayListOf()
                    briefs = null
                } else {
                    this.mData = data
                    val ids = data.map {
                        it.devId
                    }.toTypedArray()
                    //加载简介数据
                    briefs = BriefRepo.getBriefs(ids, BriefRepo.FOR_DEVICE)
                }
                this.notifyItemRangeChanged(0, itemCount, arrayListOf(1))

            }

            override fun convertPayloads(helper: BaseViewHolder, item: DeviceModel?, payloads: MutableList<Any>) {
                if (item != null) {
                    convert(helper, item)
                }
            }

            override fun convert(holder: BaseViewHolder, item: DeviceModel) {
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
                holder.setChecked(R.id.select_box, toId == item.devId)
                holder.setGone(R.id.select_box, item.isOnline)
                holder.setText(R.id.tv_device_ip, item.device?.vip ?: "")
                val iconByeDevClass = io.weline.devhelper.IconHelper.getIconByeDevClass(item.devClass, item.isOnline, true)
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
        }

        mDeviceRVAdapter.onItemClickListener = BaseQuickAdapter.OnItemClickListener { adapter, view, position ->
            val o = adapter.data[position] as DeviceModel
            toId = o.devId
            mBaseViewHolder.setText(R.id.tv_path, getTvPathText(view.context,
                    OneOSAPIs.ONE_OS_PRIVATE_ROOT_DIR, toId))
            btnToPhone.isSelected = false
            adapter.notifyItemRangeChanged(0, adapter.itemCount, listOf(o))
        }
        view.findViewById<RecyclerView>(R.id.recycle_view).let {
            it.layoutManager = LinearLayoutManager(context)
            it.adapter = mDeviceRVAdapter
        }
        val deviceViewModel = DeviceViewModel()
        val swipeRefreshLayout = view.findViewById<SwipeRefreshLayout>(R.id.swipe_refresh_layout)
        swipeRefreshLayout
                .setOnRefreshWithTimeoutListener(SwipeRefreshLayout.OnRefreshListener {
                    deviceViewModel.updateDevices(null)
                })
        deviceViewModel.liveDevices.observeForever {
            val filter = filter(it)
            if (filter.isNullOrEmpty()) {
                val inflate = layoutInflater.inflate(R.layout.layout_empty_text, null)
                inflate.findViewById<TextView>(R.id.tv_tips).setText(R.string.tips_this_net_no_dev)
                mDeviceRVAdapter.emptyView = inflate
            }
            mDeviceRVAdapter.setNewData(filter)
        }
        swipeRefreshLayout.showRefreshAndNotify()
        btnToPhone.apply {
            setOnClickListener {
                toId = if (toId == SELF) {
                    isSelected = false
                    null
                } else {
                    isSelected = true
                    SELF
                }
                mDeviceRVAdapter.notifyDataSetChanged()
            }
        }
        mBaseViewHolder.getView<TextView>(R.id.btn_cancel).apply {
            setOnClickListener {
                dismiss()
            }
        }
        mBaseViewHolder.getView<TextView>(R.id.btn_confirm).apply {
            setOnClickListener {
                if (toId.isNullOrEmpty()) {
                    return@setOnClickListener
                }
                dismiss()
                callback.result(toId)
            }
        }
    }

    private val devRequests = mutableListOf<String>()
    fun filter(list: List<DeviceModel>): List<DeviceModel>? {
        val filter = if (filterExtIdsL.isNotEmpty()) {
            list.filter {
                !filterExtIdsL.contains(it.devId)
            }
        } else {
            list
        }
        return when (filterType) {
            FilterType.BT_DOWNLOAD -> {
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
            FilterType.FILE_SHARE -> {
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
            FilterType.IS_ONLINE -> {
                filter.filter {
                    it.isOnline
                }
            }
            else -> {
                filter
            }
        }
    }

    private fun getTvPathText(context: Context, pathName: String,
                              showDevId: String?): SpannableString {
        val sb = StringBuilder()
        val pathWithTypeName = OneOSFileType.getPathWithTypeName(pathName)
        val deviceModel = SessionManager.getInstance()
                .getDeviceModel(showDevId)
        var devMarkName: String? = null
        if (deviceModel != null) {
            devMarkName = deviceModel.devName
        }
        if (!TextUtils.isEmpty(devMarkName)) {
            sb.append(devMarkName).append(":").append(pathWithTypeName)
        } else {
            sb.append(pathWithTypeName)
        }
        return Utils.setKeyWordColor(context, R.color.primary, sb.toString(), devMarkName)
    }

}

const val SELF = "SelectMyPhone"