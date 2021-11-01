package net.linkmate.app.ui.viewmodel

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.PopupWindow
import androidx.lifecycle.ViewModel
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import net.linkmate.app.R
import net.linkmate.app.bean.DeviceBean
import net.linkmate.app.manager.DevManager
import net.linkmate.app.util.ToastUtils
import net.linkmate.app.util.WindowUtil
import net.linkmate.app.view.adapter.PopupCheckRVAdapter
import net.sdvn.nascommon.iface.Callback
import net.sdvn.nascommon.model.DeviceModel
import net.sdvn.nascommon.widget.SupportPopupWindow

/** 

Created by admin on 2020/9/23,11:11

 */
class DevSelectViewModel : ViewModel() {
    fun showNasSelect(context: Activity, anchor: View, devId: String, callback: Callback<String>) {
        if (!DevManager.getInstance().isInitting) {
            val deviceBeans: MutableList<DeviceBean> = ArrayList()
            for (bean in DevManager.getInstance().boundDeviceBeans) {
                if (bean.isOnline && bean.isNas) {
                    deviceBeans.add(bean)
                }
            }
            val bean = DeviceBean(context.getString(R.string.all), "", -1, 0)
            bean.id = ""
            deviceBeans.add(0, bean)
            val contentView = LayoutInflater.from(context).inflate(R.layout.popup_rv_check, null, false)
            val window = PopupWindow(contentView, LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT, true)
            val rv: RecyclerView = contentView.findViewById(R.id.popup_rv)
            val adapter = PopupCheckRVAdapter(deviceBeans, devId)
            //点击device条目
            adapter.onItemClickListener = BaseQuickAdapter.OnItemClickListener { adapter, view, position ->
                val deviceId = (adapter.getItem(position) as? DeviceBean)?.id
                callback.result(deviceId)
                window.dismiss()
            }
            rv.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
            rv.itemAnimator = null
            rv.adapter = adapter
            window.isOutsideTouchable = true
            window.isTouchable = true
            window.animationStyle = R.style.PopupWindowAnim
            window.isFocusable = true
            WindowUtil.showShadow(context)
            window.showAsDropDown(anchor, 0, 0)
            window.setOnDismissListener {
                WindowUtil.hintShadow(context)
            }
        } else {
            ToastUtils.showToast(R.string.loading_data)
        }
    }

    fun showSelectPopup(context: Activity, anchor: View, devId: String?, list: List<DeviceModel>, callback: Callback<String>) {
        if (list.isEmpty()) {
            ToastUtils.showToast(R.string.loading_data)
            return
        }
        val deviceBeans = mutableListOf<SelectData>()
        val bean = SelectData(null, context.getString(R.string.all))
        deviceBeans.add(0, bean)
        for (model in list) {
            deviceBeans.add(SelectData(model.devId, model.devName))
        }
        val contentView = LayoutInflater.from(context).inflate(R.layout.popup_rv_check, null, false)
        val window = SupportPopupWindow(contentView, LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT)
        val rv: RecyclerView = contentView.findViewById(R.id.popup_rv)
        val adapter = object : BaseQuickAdapter<SelectData, BaseViewHolder>(
                R.layout.item_popup_rv_check, deviceBeans) {
            override fun convert(helper: BaseViewHolder, data: SelectData) {
                helper.setText(R.id.iprc_tv, data.name)
                        .setVisible(R.id.iprc_iv, devId == data.id)
            }
        }
        //点击device条目
        adapter.onItemClickListener = BaseQuickAdapter.OnItemClickListener { adapter, view, position ->
            val deviceId = (adapter.getItem(position) as? SelectData)?.id
            callback.result(deviceId)
            window.dismiss()
        }
        rv.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        rv.itemAnimator = null
        rv.adapter = adapter
        window.isOutsideTouchable = true
        window.isTouchable = true
        window.animationStyle = R.style.PopupWindowAnim
        window.isFocusable = true
        WindowUtil.showShadow(context)
        window.showAsDropDown(anchor, 0, 0)
        window.setOnDismissListener {
            WindowUtil.hintShadow(context)
        }
    }
}

data class SelectData(var id: String?, var name: String)
