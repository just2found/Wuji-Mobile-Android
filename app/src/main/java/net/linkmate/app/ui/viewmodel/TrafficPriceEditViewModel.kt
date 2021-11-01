package net.linkmate.app.ui.viewmodel

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModel
import com.chad.library.adapter.base.BaseViewHolder
import net.linkmate.app.R
import net.linkmate.app.base.MyConstants
import net.linkmate.app.base.MyConstants.DEFAULT_UNIT
import net.linkmate.app.data.model.InputFloatTextWatcher
import net.linkmate.app.manager.MessageManager
import net.linkmate.app.ui.activity.mine.WebActivity
import net.linkmate.app.util.ToastUtils
import net.sdvn.cmapi.CMAPI
import net.sdvn.common.internet.SdvnHttpErrorNo
import net.sdvn.common.internet.core.GsonBaseProtocol
import net.sdvn.common.internet.listener.ResultListener
import net.sdvn.common.internet.loader.SetENMbpointRatioHttpLoader
import net.sdvn.common.repo.DevicesRepo
import net.sdvn.common.repo.DevicesRepo.getDeviceModel
import net.sdvn.common.vo.EnMbPointMsgModel
import net.sdvn.nascommon.iface.Callback
import net.sdvn.nascommon.iface.Result
import net.sdvn.nascommon.utils.AnimUtils
import net.sdvn.nascommon.utils.DialogUtils
import net.sdvn.nascommon.utils.Utils
import org.jetbrains.annotations.NotNull
import timber.log.Timber
import java.util.*
import kotlin.collections.HashSet


class TrafficPriceEditViewModel : ViewModel() {
    enum class STATUS {
        EDITABLE, EDITING
    }

    inner class EditViewHolder(view: View, val devId: String, var callback: Callback<Result<*>>) : BaseViewHolder(view) {
        private var status: STATUS = STATUS.EDITABLE
        private val viewGroupEdit = getView<View>(R.id.group_edit)
        private val viewGroupEditing = getView<View>(R.id.group_editing)
        private val viewGroupNegative = getView<View>(R.id.group_negative)
        private val viewGroupPositive = getView<View>(R.id.group_positive)
        private val viewGroupLine3 = getView<View>(R.id.group_line3)
        private val checkBox = getView<CheckBox>(R.id.checkBox)
        private val editTextPrice = getView<EditText>(R.id.editText_price)
        private val editTextHours = getView<EditText>(R.id.editText_hours)
        private val editTextMinutes = getView<EditText>(R.id.editText_minutes)
        private val positive = getView<TextView>(R.id.positive)
        private val negative = getView<TextView>(R.id.negative)
//     private   val viewLine3Title = getView<TextView>(R.id.textView_line3_title)

        //        val textViewHours = getView<TextView>(R.id.textView_hours)
        private val textViewNewPrice = getView<TextView>(R.id.textView_new_price)


        init {
            val bindDeviceModel = DevicesRepo.getDeviceModel(devId = devId)
            toEditView()
            val min = bindDeviceModel?.minGb2cRatio ?: 0f
            val max = bindDeviceModel?.maxGb2cRatio ?: 0f
            editTextPrice.addTextChangedListener(InputFloatTextWatcher(editTextPrice))
            editTextHours.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {
                    if (s.isNullOrEmpty()) {
                        editTextHours.hint = 1.toString()
                    } else {
                        try {
                            val toInt = s.toString().toInt()
                            if (toInt < 1) {
                                editTextHours.setText(1.toString())
                                AnimUtils.sharkEditText(editTextHours)
                            } else if (toInt > 24) {
                                editTextHours.setText(24.toString())
                                val mins = editTextMinutes.text
                                if (!mins.isNullOrEmpty() && mins.toString().toInt() > 0) {
                                    editTextMinutes.setText(0.toString())
                                    AnimUtils.sharkEditText(editTextMinutes)
                                }
                                AnimUtils.sharkEditText(editTextHours)
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                            editTextHours.setText(1.toString())
                            AnimUtils.sharkEditText(editTextHours)
                        }
                    }
                    editTextHours.setSelection(editTextHours.text.trim().toString().length)

                    refreshTipsTime()
                }

                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                }
            })
            editTextMinutes.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {
                    if (s.isNullOrEmpty()) {
                        editTextMinutes.hint = 0.toString()
                    } else {
                        try {
                            val toInt = s.toString().toInt()
                            if (toInt < 0) {
                                editTextMinutes.setText(0.toString())
                                AnimUtils.sharkEditText(editTextMinutes)
                            } else if (toInt > 59) {
                                editTextMinutes.setText((59).toString())
                                AnimUtils.sharkEditText(editTextMinutes)
                            } else {
                                val hours = editTextHours.text
                                if (!hours.isNullOrEmpty() && hours.toString().toInt() == 24
                                        && toInt != 0) {
                                    editTextMinutes.setText(0.toString())
                                    AnimUtils.sharkEditText(editTextMinutes)
                                }
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                            editTextMinutes.setText("")
                            AnimUtils.sharkEditText(editTextMinutes)
                        }
                    }
                    editTextMinutes.setSelection(editTextMinutes.text.trim().toString().length)
                    refreshTipsTime()
                }

                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                }
            })
            getView<View>(R.id.button_edit).setOnClickListener {
                toEditingView()
            }
            getView<View>(R.id.button_undo).setOnClickListener {
                setGone(R.id.layout_loading, true)
                commitNewPrice(devId, null, null, true, callback = Callback {
                    if (it.isSuccess) {
                        ToastUtils.showToast(R.string.canceled)

                        bindDeviceModel?.let {
                            it.mbpChValue = ""
                            it.gb2cChValue = 0f
                            it.mbpChTime = 0
                            DevicesRepo.saveData(it)
                        }
                        callback.result(Result(it))
                    }
                    setGone(R.id.layout_loading, false)
                })
            }
            getView<View>(R.id.positive)
                    .setOnClickListener {
                        if (status == STATUS.EDITING) {
                            val delayTime = getDelayTime()
                            val toString = editTextPrice.text?.toString()
                            if (toString.isNullOrEmpty()) {
                                AnimUtils.sharkEditText(editTextPrice)
                                editTextPrice.requestFocus()
                                return@setOnClickListener
                            }
                            try {
                                val newP = toString.toFloat()
                                if (newP < min || newP > max) {
                                    AnimUtils.sharkEditText(editTextPrice)
                                    editTextPrice.requestFocus()
                                    return@setOnClickListener
                                }
                            } catch (e: Exception) {
                                Timber.w(e)
                            }
                            val newPrice = toString.toFloat()
                            setGone(R.id.layout_loading, true)
                            commitNewPrice(devId, newPrice = newPrice, delayTime = delayTime, callback = Callback {
                                if (it.isSuccess) {
                                    val time = (System.currentTimeMillis() / 1000) + (delayTime * 60)
                                    ToastUtils.showToast(R.string.setting_success)
                                    bindDeviceModel?.let {
                                        it.gb2cChValue = newPrice
                                        it.mbpChTime = time
                                        DevicesRepo.saveData(it)
                                    }
                                    callback.result(Result(it))
                                }
                                setGone(R.id.layout_loading, false)
                            })
                        } else {
                            if (checkBox.isChecked) {
                                bindDeviceModel?.mbpChTime?.let { schemetime ->
                                    getDeviceModel(devId = devId)?.let {
                                        it.enTimestamp = schemetime
                                        DevicesRepo.saveData(it)
                                    }
                                }
                            }
                            callback.result(Result(it))
                        }
                    }
            getView<View>(R.id.negative)
                    .setOnClickListener {
                        toEditView()
                    }

        }


        private fun getDelayTime(): Int {
            val minutes = try {
                editTextMinutes.text?.toString()?.takeIf { !it.isNullOrEmpty() }?.toInt()
                        ?: 0
            } catch (e: Exception) {
                0
            }
            val hours = try {
                (editTextHours.text?.toString()?.takeIf { !it.isNullOrEmpty() }?.toInt()
                        ?: 1)
            } catch (e: Exception) {
                1
            }
            var delayTime = hours * 60 + minutes
            if (delayTime > 24 * 60) {
                delayTime = 24 * 60
            }
            if (delayTime < 60) {
                delayTime = 60
            }
            return delayTime
        }

        private fun toEditingView() {
            val bindDeviceModel = DevicesRepo.getDeviceModel(devId = devId)
            status = STATUS.EDITING
            val priceUnit: String = itemView.context.getString(R.string.fmt_traffic_unit_price2)
                    .replace("\$TRAFFIC$", DEFAULT_UNIT)
            setText(R.id.device_sn, bindDeviceModel?.devSN)
                    .setText(R.id.tv_current_price_unit, priceUnit)
            viewGroupEdit.isVisible = false
            viewGroupLine3.isVisible = false
            viewGroupEditing.isVisible = true
            viewGroupNegative.isVisible = true
            viewGroupPositive.isVisible = true
//            viewLine3Title.setText(R.string.delay_time_title)
//            textViewHours.setText(R.string.hours)
            bindDeviceModel?.let {
                setText(R.id.tips_price_limit, "${it.minGb2cRatio} ≤ X ≤ ${it.maxGb2cRatio}")
                setGone(R.id.tips_price_limit, true)
            } ?: kotlin.run {
                setGone(R.id.tips_price_limit, false)
            }
            setGone(R.id.tv_tips, false)
            setGone(R.id.button_edit, false)
            setGone(R.id.button_undo, false)
        }

        private fun toEditView() {
            val bindDeviceModel = DevicesRepo.getDeviceModel(devId = devId)
            val isOwner = bindDeviceModel?.isOwner ?: false
            status = STATUS.EDITABLE
            viewGroupEditing.isVisible = false
            viewGroupNegative.isVisible = false
            viewGroupEdit.isVisible = true
            viewGroupPositive.isVisible = true

            val currentPrice: String = bindDeviceModel?.gb2cRatio?.takeIf { it > 0 }?.let {
                it.toString() + itemView.context.getString(R.string.fmt_traffic_unit_price2)
                        .replace("\$TRAFFIC$", DEFAULT_UNIT)

            } ?: kotlin.run {
                itemView.context.getString(R.string.fmt_traffic_unit_price)
                        .replace("\$TRAFFIC$", bindDeviceModel?.mbpRatio ?: "")
            }

            setText(R.id.device_sn, bindDeviceModel?.devSN)
                    .setText(R.id.tv_current_price, currentPrice)
            positive.setText(R.string.confirm)
            val mbpChValue = bindDeviceModel?.mbpChValue
            val mbpChTime = bindDeviceModel?.mbpChTime ?: 0
            setGone(R.id.tips_price_limit, false)
            var priceUnit: String? = null

            val gB2CSchemeValue = bindDeviceModel?.gb2cChValue
            if (gB2CSchemeValue != null && gB2CSchemeValue > 0) {
                priceUnit = gB2CSchemeValue.toString() + itemView.context.getString(R.string.fmt_traffic_unit_price2)
                        .replace("\$TRAFFIC$", DEFAULT_UNIT)
            } else if (!mbpChValue.isNullOrEmpty()) {
//                viewLine3Title.setText(R.string.last_flow_unit_price)
                priceUnit = itemView.context.getString(R.string.fmt_traffic_unit_price)
                        .replace("\$TRAFFIC$", mbpChValue)
            }
            if (priceUnit != null) {
                textViewNewPrice.text = priceUnit
                if (mbpChTime > 0) {
                    val format = MyConstants.sdf.format(Date(mbpChTime * 1000))
                    val effectiveTime = itemView.context.getString(R.string.effective_time)
                    setText(R.id.tv_tips, "$effectiveTime : $format")
                    setGone(R.id.tv_tips, true)
                } else {
                    setGone(R.id.tv_tips, false)
                }
                viewGroupLine3.isVisible = true
                checkBox.isVisible = !isOwner && isShouldShowTrafficTips(devId)
                setGone(R.id.button_undo, isOwner)
                setGone(R.id.button_edit, isOwner)
                getView<View>(R.id.button_edit).isEnabled = isOwner
            } else {
                viewGroupLine3.isVisible = false
                checkBox.isVisible = false
                setGone(R.id.button_edit, isOwner)
            }
        }

        private fun refreshTipsTime() {
            var delayTime = getDelayTime()
            val format = MyConstants.sdf.format(Date(System.currentTimeMillis() + delayTime * 60 * 1000))
            val effectiveTime = itemView.context.getString(R.string.effective_time)
            setText(R.id.tv_tips, "$effectiveTime : $format")
            setGone(R.id.tv_tips, true)
        }

    }

    fun showEditView(context: @NotNull Context, devId: String): @NotNull Dialog {
        return showEditView(context, devId, null)
    }

    fun showEditView(context: @NotNull Context, devId: String, callback: Callback<Result<*>>? = null): @NotNull Dialog {
        val view: View = LayoutInflater.from(context).inflate(R.layout.layout_setup_mbpoint, null)
        val dialog = DialogUtils.showCustomDialog(context, view)
        val baseViewHolder = EditViewHolder(view, devId, Callback<Result<*>> {
            dialog.dismiss()
            callback?.result(it)
        })
        return dialog
    }


    private fun commitNewPrice(devId: String, newPrice: Float?, delayTime: Int? = 0,
                               cancel: Boolean = false, callback: Callback<Result<GsonBaseProtocol?>>) {
        val loader = SetENMbpointRatioHttpLoader(GsonBaseProtocol::class.java)
        loader.setParams(devId, newPrice, delayTime, cancel)
        loader.executor(object : ResultListener<GsonBaseProtocol> {
            override fun error(tag: Any?, baseProtocol: GsonBaseProtocol?) {
                callback.result(Result(baseProtocol?.result ?: SdvnHttpErrorNo.EC_REQUEST,
                        baseProtocol?.errmsg))
                ToastUtils.showError(baseProtocol?.result ?: SdvnHttpErrorNo.EC_REQUEST)
                if (baseProtocol?.result == SdvnHttpErrorNo.EC_NO_SUCH_SETTING) {
                    // 成功的时候刷新消息
                    MessageManager.getInstance().refreshEnMsg(true)
                }
            }

            override fun success(tag: Any?, data: GsonBaseProtocol?) {
                callback.result(Result(data))
                // 成功的时候刷新消息
                MessageManager.getInstance().refreshEnMsg(true)
            }
        })
    }

    fun whetherShowTrafficTips(context: @NotNull Context, deviceId: String, callback: Callback<Result<*>>?): Boolean {
        if (isShouldShowTrafficTips(deviceId) || checkIsFirstTipsDevice(deviceId)) {
            showEditView(context, deviceId, callback)
            return true
        }
        return false
    }


    @SuppressLint("SetTextI18n")
    fun showMsgView(context: Context, data: EnMbPointMsgModel) {
        val view = LayoutInflater.from(context).inflate(R.layout.layout_setup_mbpoint, null)
        view.findViewById<TextView>(R.id.device_sn).text = data.content?.devicename?.toString()
        view.findViewById<TextView>(R.id.textView14).setText(R.string.last_flow_unit_price)
        val priceUnit: String = data.content?.gb2cRatio?.takeIf { it > 0 }?.let {
            it.toString() + context.getString(R.string.fmt_traffic_unit_price2)
                    .replace("\$TRAFFIC$", DEFAULT_UNIT)

        } ?: kotlin.run {
            context.getString(R.string.fmt_traffic_unit_price)
                    .replace("\$TRAFFIC$", data.content?.mbpointratio ?: "")
        }

        val tvPrice = view.findViewById<TextView>(R.id.tv_current_price)
        view.findViewById<View>(R.id.group_edit).isVisible = true
        tvPrice.isVisible = true
        tvPrice.text = priceUnit
        view.findViewById<View>(R.id.group_negative).isVisible = false
        val positive = view.findViewById<TextView>(R.id.positive)
        val format = MyConstants.sdf.format(Date(data.content.schemedate * 1000))
        val effectiveTime = context.getString(R.string.effective_time)
        val tvTips = view.findViewById<TextView>(R.id.tips_price_limit)
        tvTips.isVisible = true
        tvTips.text = "$effectiveTime : $format"
        positive.setText(R.string.confirm)
        val showCustomDialog = DialogUtils.showCustomDialog(context, view)
        positive.setOnClickListener {
            showCustomDialog.dismiss()
        }
        showCustomDialog.setCancelable(true)
    }
    companion object{
        /**
         * 跳转到领取积分
         */
        fun startScoreActivity(context: Context, url: String) {
            val curLocale = context.resources.configuration.locale
            val language = curLocale.language
            val script = curLocale.script
            val country = curLocale.country //"CN""TW"

            val lang: String
            lang = if ("zh" == language &&
                    ("cn" != country.toLowerCase() || "hant" == script.toLowerCase())) {
                "tw"
            } else {
                language
            }
            val ticket = CMAPI.getInstance().baseInfo.ticket
            val i = Intent(context, WebActivity::class.java)
            i.putExtra("url", url
                    ?.replace("{0}", ticket)
                    ?.replace("{1}", lang)
                    ?.replace("\\u0026", "&"))
            i.putExtra("title", context.getString(R.string.receive_score))
            i.putExtra("ConnectionState", true)
            i.putExtra("enableScript", true)
            i.putExtra("hasFullTitle", false)
            i.putExtra("sllType", "app")
            context.startActivity(i)
        }

    }
}

/**
 * 检测是否需要第一次启动
 * 1.积分大于0
 * 2.非自己设备
 */
private fun checkIsFirstTipsDevice(devId: String?): Boolean {
    var result = false
    if (devId != null) {
        val deviceModel = DevicesRepo.getDeviceModel(devId)
        deviceModel?.let {
            if (!it.isOwner() && it.gb2cRatio > 0f) {
                //第一次登录，显示流量提示，退出登录也不删除
                val TABLE_NAME = "trafic_tips_for_first_table"
                //已经显示过的，＋用户id
                val KEY_NAME = "tips_" + CMAPI.getInstance().getBaseInfo().userId
                val sp: SharedPreferences = Utils.getApp().getSharedPreferences(TABLE_NAME, Context.MODE_PRIVATE)
                val deviceIds: HashSet<String> = sp.getStringSet(KEY_NAME, HashSet<String>()).toHashSet()
                if (!deviceIds.contains(devId)) {
                    result = true
                    //保存，下次进来不提示
                    deviceIds.add(devId)
                    sp.edit().putStringSet(KEY_NAME, deviceIds).apply()
                }
            }
        }
    }
    return result

}


fun isShouldShowTrafficTips(devId: String): Boolean {
    val bindDeviceModel = getDeviceModel(devId)
    return (bindDeviceModel != null && !bindDeviceModel.isOwner &&
            bindDeviceModel.mbpChTime > bindDeviceModel.enTimestamp)
}