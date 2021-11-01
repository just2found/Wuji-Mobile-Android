package net.linkmate.app.ui.viewmodel

import android.content.Context
import android.text.TextUtils
import android.text.format.DateUtils
import android.util.Pair
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import io.reactivex.Observable
import io.reactivex.functions.Predicate
import io.reactivex.schedulers.Schedulers
import io.weline.devhelper.IconHelper
import libs.source.common.utils.RateLimiter
import net.linkmate.app.R
import net.sdvn.cmapi.CMAPI
import net.sdvn.cmapi.UpdateInfo
import net.sdvn.cmapi.global.Constants
import net.sdvn.common.repo.BriefRepo
import net.sdvn.nascommon.LibApp.Companion.instance
import net.sdvn.nascommon.iface.Result
import net.sdvn.nascommon.model.DeviceModel
import net.sdvn.nascommon.model.UiUtils
import net.sdvn.nascommon.utils.DialogUtils
import net.sdvn.nascommon.utils.SPUtils
import net.sdvn.nascommon.utils.ToastHelper
import net.sdvn.nascommon.utils.Utils
import net.sdvn.nascommon.viewmodel.RxViewModel
import net.sdvn.nascommon.widget.PopDialogFragment
import org.jetbrains.annotations.NotNull
import timber.log.Timber
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

class M8CheckUpdateViewModel : RxViewModel() {
    private val KEY_LAST_CHECK_UPDATE_PREFIX: String = "key_last_check_update_"
    private var dialog: AlertDialog? = null
    private var mPopDialogFragment: PopDialogFragment? = null
    private val TAG_M8_UPGRADE = "tag_m8_upgrade"
    private val _liveDataUpdataInfos: MutableLiveData<List<Pair<DeviceModel, UpdateInfo>>> = MutableLiveData()
    val updateInfosLiveData: LiveData<List<Pair<DeviceModel, UpdateInfo>>> = _liveDataUpdataInfos
    private val _liveDataUpdateResults: MutableLiveData<List<Pair<DeviceModel, Result<Int>>>> = MutableLiveData()
    val updateResultsLiveData: LiveData<List<Pair<DeviceModel, Result<Int>>>> = _liveDataUpdateResults

    private val updateCheckIntervalRateLimit = RateLimiter<String>(120, TimeUnit.SECONDS)
    private val updateShowRateLimit = RateLimiter<String>(30, TimeUnit.MINUTES)

    private val upgradingMap: LiveData<MutableMap<DeviceModel, UpdateInfo>> = MutableLiveData(hashMapOf())

    private fun checkLastCheck(id: String): Boolean {
        val last = SPUtils.getValue(Utils.getApp(), "$KEY_LAST_CHECK_UPDATE_PREFIX$id", "0")
        if (TextUtils.isEmpty(last)) return false
        try {
            val lastTime = last!!.toLong()
            if (!DateUtils.isToday(lastTime)) {
                return true
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }

    fun refreshDeviceUpdateInfo(devices: List<DeviceModel>) {
        val toMutableList = devices.toMutableList()
        toMutableList.forEach {
            val mutableMap = upgradingMap.value!!
            mutableMap.get(it)?.let { updateInfo ->
                if (it.device?.appVersion.isNullOrEmpty()) {
                    if (Objects.equals(it.device?.appVersion, updateInfo.newVersion)) {
                        updateInfo.version = it.device?.appVersion
                        upgradingMap.value
                    }
                }

            }
        }
        val toObservable = Observable.fromIterable(toMutableList)
                .filter(Predicate {
                    it.isOwner && it.isOnline && it.device != null
                            && updateCheckIntervalRateLimit.shouldFetch(it.devId) /*&& checkLastCheck(it.devId)*/
                })
                .flatMap { t ->
                    val deviceUpdateInfo = CMAPI.getInstance().getDeviceUpdateInfo(t.device!!.vip)
                    deviceUpdateInfo?.version = t.device?.appVersion
                    //---------test----------
                    if (false && deviceUpdateInfo?.getResult() != Constants.CE_SUCC) {
                        deviceUpdateInfo?.result = Constants.CE_SUCC
                        deviceUpdateInfo?.newVersion = "3.0.0.2888"
                        deviceUpdateInfo?.version = "3.0.0.2877"
                        val logModel: UpdateInfo.LogModel = UpdateInfo.LogModel()
                        logModel.changelogchs = "简体"
                        logModel.changelogcht = "繁体"
                        logModel.changelogen = "English"
                        deviceUpdateInfo?.log = logModel
                    }
                    //---------test----------
                    Observable.just((Pair(t, deviceUpdateInfo)))
                }
                .filter {
                    it.second != null && it.second!!.result == Constants.CE_SUCC
                            && UiUtils.isNewVersion(it.second!!.version, it.second!!.newVersion)
                }
                .subscribeOn(Schedulers.single())
                .toList()
                .subscribe({ t ->
                    _liveDataUpdataInfos.postValue(t as List<Pair<DeviceModel, UpdateInfo>>)
                }, { Timber.e(it) })
        addDisposable(toObservable)
    }

    @Suppress("MemberVisibilityCanBePrivate")
    fun doUpgrade(pairs: @NotNull List<Pair<DeviceModel, UpdateInfo>>) {
        val counter: AtomicInteger = AtomicInteger(pairs.size)
        upgradingMap.observeForever {
            if (it.isNotEmpty()) {
                it.toMutableMap().forEach { map ->
                    val devModel = map.key
                    val updateInfo = map.value
                    if (updateInfo.version == updateInfo.newVersion) {
                        upgradingMap.value?.remove(devModel)
                        val count = counter.decrementAndGet()
                        if (count == 0) {
                            if (pairs.size == 1) {
                                if (dialog?.isShowing == true) {
                                    dialog?.dismiss()
                                }
                            } else {
                                if (mPopDialogFragment?.isShowing == true) {
                                    mPopDialogFragment?.dismiss()
                                }
                            }
                        }
                    }
                }
            }
        }
        val subscribe = Observable.create<List<Pair<DeviceModel, Result<Int>>>> {
            val list: MutableList<Pair<DeviceModel, Result<Int>>> = mutableListOf()
            val mutableListOf = mutableListOf<Pair<DeviceModel, UpdateInfo>>()
            val toMutableList = _liveDataUpdataInfos.value?.toMutableList()

            pairs.forEach { pair ->
                pair.first.let { deviceModel ->
                    val device = deviceModel.device
                    if (deviceModel.isOnline && device != null) {
                        val result = CMAPI.getInstance().deviceUpgrade(device.vip)
                        if (result == Constants.CE_SUCC) {
                            mutableListOf.add(pair)
                            list.add(Pair(deviceModel, Result(result)))
                            upgradingMap.value!!.put(deviceModel, pair.second)
                            SPUtils.setValue(Utils.getApp(), "$KEY_LAST_CHECK_UPDATE_PREFIX${device.id}", System.currentTimeMillis().toString())
                        } else {
                            counter.decrementAndGet()
                            list.add(Pair(deviceModel, Result<Int>(result, "error")))
                        }
                    }
                }
            }
            toMutableList?.removeAll(mutableListOf)
            _liveDataUpdataInfos.postValue(toMutableList)
            it.onNext(list)
            it.onComplete()
        }.subscribeOn(Schedulers.single())
                .subscribe({
                    _liveDataUpdateResults.postValue(it)
                }, {
                    Timber.e(it)
                })
        addDisposable(subscribe)
    }

    fun showM8Upgrade(context: @NotNull Context, fragmentManager: FragmentManager,
                      pairs: @NotNull List<Pair<DeviceModel, UpdateInfo>>) {
        if (pairs.size == 1) {
            mPopDialogFragment?.dismiss()
            val item = pairs.get(0)
            if (updateShowRateLimit.shouldFetch(item.first.devId)) {
                showDeviceItemUpgradeDetail(context, item)
            }
            return
        }
        dialog?.apply {
            if (isShowing) {
                dismiss()
            }
        }
        var count = 0
        pairs.forEach {
            val deviceModel = it.first
            val updateInfo = it.second
            if (updateInfo.mustupgrade == 0) {
                SPUtils.setValue(Utils.getApp(), "$KEY_LAST_CHECK_UPDATE_PREFIX${deviceModel.devId}", System.currentTimeMillis().toString())
            } else {
                count++
            }
        }
        if (mPopDialogFragment == null) {
            mPopDialogFragment = fragmentManager.findFragmentByTag(TAG_M8_UPGRADE) as PopDialogFragment?
        }
        if (mPopDialogFragment == null) {
            val contentView = LayoutInflater.from(context).inflate(R.layout.layout_dialog_dev_select, null)
            mPopDialogFragment = PopDialogFragment.newInstance(false, contentView)
            mPopDialogFragment!!.addDismissListener { }
            mPopDialogFragment!!.show(fragmentManager, TAG_M8_UPGRADE)
            mPopDialogFragment!!.isCancelable = false
            val baseViewHolder = BaseViewHolder(contentView)
            val baseQuickAdapter: BaseQuickAdapter<Pair<DeviceModel, UpdateInfo>, BaseViewHolder> =
                    object : BaseQuickAdapter<Pair<DeviceModel, UpdateInfo>, BaseViewHolder>(R.layout.item_dev_upgrade) {
                        override fun convert(baseViewHolder: BaseViewHolder, data: Pair<DeviceModel, UpdateInfo>) {
                            baseViewHolder.setText(R.id.txt_dev_name, data.first.devName)
                                    .setText(R.id.txt_old_version, data.second.version)
                                    .setText(R.id.txt_new_version, data.second.newVersion)
                            val iconByeDevClass = IconHelper.getIconByeDevClass(data.first.devClass)
                            val iconView = baseViewHolder.getView<ImageView>(R.id.iv_device)
                            data.first.devId
                            if (baseViewHolder.itemView.getTag() != data.first.devId) {
                                iconView.setTag(null)
                                baseViewHolder.itemView.setTag(data.first.devId)
                            }
                            if (iconView.getTag() == null) iconView.setImageResource(iconByeDevClass)
                            instance.getBriefDelegete().loadDeviceBrief(data.first.devId, BriefRepo.getBrief(data.first.devId, BriefRepo.FOR_DEVICE), iconView, null, iconByeDevClass, null, 0)
                        }
                    }
            baseQuickAdapter.setOnItemClickListener { baseQuickAdapter, view, i ->
                val item1 = baseQuickAdapter.getItem(i)
                if (item1 is Pair<*, *>) {
                    val item = item1 as Pair<DeviceModel, UpdateInfo>
                    showDeviceItemUpgradeDetail(context, item)
                }
            }
            val recyclerView = baseViewHolder.getView<RecyclerView>(R.id.recycle_view)
            recyclerView.layoutManager = LinearLayoutManager(context)
            recyclerView.addItemDecoration(DividerItemDecoration(context, LinearLayoutManager.VERTICAL));
            recyclerView.adapter = baseQuickAdapter
            baseViewHolder.setText(R.id.text_title, R.string.discover_new_firmware_version)
            baseViewHolder.setText(R.id.negative, R.string.cancel)
            baseViewHolder.setOnClickListener(R.id.negative) { v: View? -> mPopDialogFragment?.dismiss() }
            baseViewHolder.setText(R.id.positive, R.string.upgrade_all)
            baseViewHolder.setOnClickListener(R.id.positive) { v: View? ->
                mPopDialogFragment?.dismiss()
                showUpdateWarnDialog(context, pairs)
            }
            baseViewHolder.setVisible(R.id.group_negative, count == 0)
            baseViewHolder.getView<View>(R.id.layout_bottom).requestLayout()
            baseQuickAdapter.setNewData(pairs)
        } else {
            mPopDialogFragment?.getCustomView()?.let { holder ->
                holder.findViewById<View>(R.id.group_negative)?.isVisible = count == 0
                holder.findViewById<RecyclerView>(R.id.recycle_view)?.let { recyclerView ->
                    val adapter = recyclerView.adapter
                    if (adapter != null) {
                        val baseQuickAdapter = adapter as BaseQuickAdapter<Pair<DeviceModel, UpdateInfo>, BaseViewHolder>
                        baseQuickAdapter.setNewData(pairs)
                    }
                }
            }
        }

    }

    private fun showUpdateWarnDialog(context: Context, pairs: @NotNull List<Pair<DeviceModel, UpdateInfo>>) {
        DialogUtils.showNotifyDialog(context, 0, R.string.tips_upgrade_oneos, R.string.confirm) { _, _ ->
            doUpgrade(pairs)
            ToastHelper.showLongToast(R.string.tips_upgrading_firmware)
        }
    }

    fun showDeviceItemUpgradeDetail(context: Context, item: @NotNull Pair<DeviceModel, UpdateInfo>) {
        if (dialog == null || !dialog!!.isShowing) {
            val builder = AlertDialog.Builder(context, R.style.DialogThemeAlert)
            builder.setCancelable(false)
            dialog = builder.show()
        }
        refreshDialog(context, dialog!!, item)
    }

    private fun refreshDialog(context: Context, dialog: AlertDialog, item: Pair<DeviceModel, UpdateInfo>) {
        val updateInfo = item.second
        val device = item.first
        val view = LayoutInflater.from(context).inflate(R.layout.layout_m8_update_info, null)
        val txtUpdate = view.findViewById<TextView>(R.id.txt_update)
        val tvContent = view.findViewById<TextView>(R.id.tv_content)
        val tvNegative = view.findViewById<TextView>(R.id.negative)
        val tvPositive = view.findViewById<TextView>(R.id.positive)
        val tvOldVersion = view.findViewById<TextView>(R.id.txt_old_version)
        val tvNewVersion = view.findViewById<TextView>(R.id.txt_new_version)
        val tvDevName = view.findViewById<TextView>(R.id.txt_dev_name)
        val ivDevice = view.findViewById<ImageView>(R.id.iv_device)
        val groupNegative = view.findViewById<View>(R.id.group_negative)
        tvNegative.setOnClickListener { dialog.dismiss() }
        tvPositive.setOnClickListener {
            dialog.dismiss()
            showUpdateWarnDialog(context, listOf(item))
        }

        dialog.setContentView(view)
        groupNegative.isVisible = updateInfo.mustupgrade == 0
        val iconByeDevClass = IconHelper.getIconByeDevClass(device.devClass)
        ivDevice.setImageResource(iconByeDevClass)

        instance.getBriefDelegete().loadDeviceBrief(device.devId, BriefRepo.getBrief(device.devId, BriefRepo.FOR_DEVICE), ivDevice, null, iconByeDevClass, null, 0)

        tvOldVersion.text = device.device?.appVersion
        tvNewVersion.text = updateInfo.newVersion
        tvDevName.text = device.devName

        txtUpdate.setText(R.string.discover_new_firmware_version)
        tvPositive.setText(R.string.upgrade_app_now)
        tvNegative.setText(R.string.cancel)
        //        tvNegative.setText(R.string.upgrade_next_time);
        val message = StringBuilder()
//        message.append("V ")
//                .append(updateInfo.newVersion)
//                .append("\n")
        message.append(context.getString(R.string.strUpgradeDialogFeatureLabel)).append(":\n")
        updateInfo.log?.let { log ->
            when {
                UiUtils.isHans() -> {
                    message.append(log.changelogchs)
                }
                UiUtils.isHant() -> {
                    message.append(log.changelogcht)
                }
                else -> {
                    message.append(log.changelogen)
                }
            }
//            when {
//                "zh_cn".equals(language + "_" + country, ignoreCase = true) -> {
//                    message.append(log.changelogchs)
//                }
//                "zh_tw".equals(language + "_" + country, ignoreCase = true) -> {
//                    message.append(log.changelogcht)
//                }
//                else -> {
//                    message.append(log.changelogen)
//                }
//            }
        }
        tvContent.text = message
        if (updateInfo.mustupgrade == 0) {
            SPUtils.setValue(Utils.getApp(), "$KEY_LAST_CHECK_UPDATE_PREFIX${device.devId}", System.currentTimeMillis().toString())
        }
    }

    fun showDeviceItemUpgradeResult(requireContext: Context, deviceModelResultPair: Pair<DeviceModel, Result<Int>>) {

    }

    fun showM8UpgradeResults(requireContext: Context, requireFragmentManager: FragmentManager, pairs: List<Pair<DeviceModel, Result<Int>>>) {

    }

}
