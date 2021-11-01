package net.linkmate.app.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.rxjava.rxlife.RxLife
import kotlinx.android.synthetic.main.item_device_flow_detail_filter.view.*
import net.linkmate.app.R
import net.linkmate.app.base.MyConstants
import net.linkmate.app.bean.DeviceBean
import net.linkmate.app.manager.DevManager
import net.linkmate.app.view.ViewHolder
import net.sdvn.nascommon.SessionManager
import net.sdvn.nascommon.utils.SPUtils

/**
 * @author Raleigh.Luo
 * date：21/4/23 19
 * describe：
 */
class FlexDeviceAdapter(val mContext: Context) : RecyclerView.Adapter<ViewHolder>() {
    private val deviceBeans: ArrayList<DeviceBean> = arrayListOf()

    fun refreshDevices() {
        deviceBeans.clear()
        val devices = DevManager.getInstance().getBoundDeviceBeans()
        if (devices == null) return
        for (bean: DeviceBean in devices) {
            if (bean.getHardData() != null && (bean.getHardData()?.isEN()
                            ?: false) && bean.getMnglevel() == 0) {
                deviceBeans.add(bean)
            }
        }
    }

    fun reset() {
        selectedDeviceId = null
        notifyDataSetChanged()
    }

    var selectedDeviceId: String? = null
    private var mSelectedDeviceName: String? = null

    fun getDeviceName(): String? {
        return mSelectedDeviceName
    }

    /**
     * 重置，并触发点击事件
     */
    fun recovery(selectedDeviceId: String?) {
        refreshDevices()
        this.selectedDeviceId = selectedDeviceId
        if (selectedDeviceId == null) {
            selectedListener?.selected(null, true)
        } else {
            selectedListener?.selected(deviceBeans.find {
                it.id == selectedDeviceId
            }, true)
        }
        notifyDataSetChanged()
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
                LayoutInflater.from(mContext).inflate(R.layout.item_device_flow_detail_filter, null, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return deviceBeans.size + 1
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        with(holder.itemView) {
            cbCheck.setOnCheckedChangeListener(null)
            cbCheck.setOnClickListener(null)
            if (position == 0) {
                ivChecked.setTag(null)
                cbCheck.setText(mContext.getString(R.string.all_dev))

                if (selectedDeviceId == null) {
                    cbCheck.isChecked = true
                    ivChecked.visibility = View.VISIBLE
                    mSelectedDeviceName = mContext.getString(R.string.all_dev)
                } else {
                    cbCheck.isChecked = false
                    ivChecked.visibility = View.GONE
                }
                cbCheck.setOnClickListener {
                    if (cbCheck.isChecked) {
                        mSelectedDeviceName = cbCheck.text.toString()
                        selectedDeviceId = null
                        selectedListener?.selected(null)
                        ivChecked.visibility = View.VISIBLE
                        notifyDataSetChanged()
                    } else {//不能取消
                        cbCheck?.isChecked = true
                    }
                }
            } else {
                val index = position - 1
                val bean = deviceBeans.get(index)
                if (root.getTag() != bean.id) {
                    ivChecked.setTag(null)
                    root.setTag(bean.id)
                }

                if (selectedDeviceId == bean.id) {
                    cbCheck.isChecked = true
                    ivChecked.visibility = View.VISIBLE
                } else {
                    cbCheck.isChecked = false
                    ivChecked.visibility = View.GONE
                }


                if (bean.isNas && SPUtils.getBoolean(MyConstants.SP_SHOW_REMARK_NAME, true)) {
                    val deviceModel = SessionManager.getInstance()
                            .getDeviceModel(bean.id)
                    if (deviceModel != null) {
                        if (root.getTag() == bean.id && ivChecked.getTag() == null) {
                            if (selectedDeviceId == bean.id) mSelectedDeviceName = deviceModel.devName
                            cbCheck.setText(deviceModel.devName)
                        }
                        deviceModel.devNameFromDB
                                .`as`(RxLife.`as`(holder.itemView))
                                .subscribe({ s: String? ->
                                    if (selectedDeviceId == bean.id) mSelectedDeviceName = s
                                    if (cbCheck.text.toString() != s) {
                                        if (root.getTag() == bean.id) {
                                            cbCheck.setText(s)
                                            ivChecked.setTag(s)
                                        }
                                    }
                                }) { throwable: Throwable? -> }
                    } else {
                        val text = bean.name.trim { it <= ' ' }
                        if (cbCheck.text.toString() != text) {
                            if (selectedDeviceId == bean.id) mSelectedDeviceName = text
                            if (root.getTag() == bean.id) cbCheck.setText(text)
                        }
                    }
                } else {
                    val text = bean.name.trim { it <= ' ' }
                    if (cbCheck.text.toString() != text) {
                        if (selectedDeviceId == bean.id) mSelectedDeviceName = text
                        if (root.getTag() == bean.id) cbCheck.setText(text)
                    }
                }

                cbCheck.setOnClickListener {
                    if (cbCheck.isChecked) {
                        mSelectedDeviceName = cbCheck.text.toString()
                        selectedDeviceId = bean.id
                        selectedListener?.selected(bean)
                        ivChecked.visibility = View.VISIBLE
//                        notifyItemChanged(lastPosition)
                        notifyDataSetChanged()
                    } else {//不能取消
                        cbCheck?.isChecked = true
                    }
                }
            }


        }
    }

    var selectedListener: SelectedListener? = null

    interface SelectedListener {
        fun selected(bean: DeviceBean?, isRecovery: Boolean = false)
    }
}