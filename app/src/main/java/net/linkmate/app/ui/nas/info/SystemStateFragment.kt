package net.linkmate.app.ui.nas.info

import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import io.weline.repo.api.V5HttpErrorNo
import io.weline.repo.data.model.SysStatus
import kotlinx.android.synthetic.main.fragment_system_state.*
import libs.source.common.livedata.Status
import libs.source.common.utils.JsonUtilN
import net.linkmate.app.R
import net.linkmate.app.ui.nas.TipsBaseFragment
import net.linkmate.app.ui.nas.info.adpter.DiskTemperatureAdapter
import net.linkmate.app.ui.nas.info.adpter.SpeedOfTheFanAdapter
import net.linkmate.app.util.FormatUtils
import net.linkmate.app.util.ToastUtils
import net.sdvn.nascommon.utils.FileUtils


class SystemStateFragment : TipsBaseFragment() {


    override fun getTopView(): View? {
        return title_bar
    }

    private val viewModel by viewModels<DeviceInformationModel>({ requireParentFragment() })

    private val MSG_KEY = 1021
    private val INTERVAL = 1000L
    private val mHandler = Handler(Looper.getMainLooper()) {
        if (it.what == MSG_KEY) {
            updateData()
            true
        }
        false
    }

    private fun updateData() {
        viewModel.getSystemStatus().observe(this, Observer {
            if (it.status == Status.SUCCESS) {
                val sysStatus: SysStatus? = JsonUtilN.anyToJsonObject(it.data)
                sysStatus?.let { data ->
                    viewModel.sysStatus = data
                    initSysStatusUi(data)
                }
                mHandler.sendEmptyMessageDelayed(MSG_KEY, INTERVAL)
            } else if (it.status == Status.ERROR) {
                ToastUtils.showToast(V5HttpErrorNo.getResourcesId(it.code))
                mHandler.sendEmptyMessageDelayed(MSG_KEY, INTERVAL)
            }
        })
    }

    override fun initView(view: View) {
        mTipsBar = tipsBar
        title_bar.setBackListener { findNavController().navigateUp() }
        initSysStatus()
    }

    override fun getLayoutResId(): Int {
        return R.layout.fragment_system_state
    }

    private fun initSysStatus() {
        if (viewModel.sysStatus != null) {
            initSysStatusUi(viewModel.sysStatus!!)
            mHandler.sendEmptyMessageDelayed(MSG_KEY, INTERVAL)
        } else {
            updateData()
        }
    }


    private fun initSysStatusUi(data: SysStatus) {
        data.cpu_use?.let { cpu_wpv.progress = viewModel.getInt(it).toInt() };
        data.mem_use?.let { memory_wpv.progress = viewModel.getInt(it).toInt() };
        data.tx_speed?.let { download_speed_tv.text = " ${FileUtils.fmtFileSize(it)}/s" }
        data.tx_speed?.let { upload_speed_tv.text = " ${FileUtils.fmtFileSize(it)}/s" }
        data.sys_runtime?.let { running_time_ail.setTips(FormatUtils.getUptimeDay(it,requireActivity())) }
        data.fan_rpm?.size?.let { size ->
            if (size == 1) {
                fan_speed_ail.setTips("${data.fan_rpm!![0]}${getString(R.string.unit_of_speed)}")
            } else if (size > 1) {
                fan_speed_rv.visibility = View.VISIBLE
                val mSpeedOfTheFanAdapter = SpeedOfTheFanAdapter()
                fan_speed_rv.adapter = mSpeedOfTheFanAdapter
                mSpeedOfTheFanAdapter.setNewData(data.fan_rpm!!.toMutableList())
            }
        }

        if(data.hd_temp.isNullOrEmpty())
        {
            hard_disk_temperature_ail.visibility=View.GONE
        }

        data.hd_temp?.size?.let { size ->
            if (size == 1) {
                hard_disk_temperature_ail.setTips("${data.hd_temp!![0]}°C")
            } else if (size > 1) {
                hard_disk_temperature_rv.visibility = View.VISIBLE
                val mDiskTemperatureAdapter = DiskTemperatureAdapter()
                hard_disk_temperature_rv.adapter = mDiskTemperatureAdapter
                mDiskTemperatureAdapter.setNewData(data.hd_temp!!.toMutableList())
            }
        }
    }

}