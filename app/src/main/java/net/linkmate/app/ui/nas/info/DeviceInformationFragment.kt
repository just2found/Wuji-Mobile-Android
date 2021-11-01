package net.linkmate.app.ui.nas.info

import android.content.Intent
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.rxjava.rxlife.RxLife
import io.weline.devhelper.IconHelper
import io.weline.repo.api.V5HttpErrorNo
import io.weline.repo.data.model.HardInfo
import io.weline.repo.data.model.SysStatus
import kotlinx.android.synthetic.main.fragment_device_information.*
import libs.source.common.livedata.Status
import libs.source.common.utils.JsonUtilN
import net.linkmate.app.R

import net.linkmate.app.ui.nas.TipsBaseFragment
import net.linkmate.app.ui.simplestyle.device.disk.DiskSpaceActivity
import net.linkmate.app.ui.simplestyle.device.disk.data.DiskSpaceOverview
import net.linkmate.app.util.ToastUtils
import net.sdvn.nascommon.utils.FileUtils
import net.sdvn.nascommon.viewmodel.DeviceViewModel


/**
 * A simple [Fragment] subclass.
 * Use the [DeviceInformationFragment.newInstance] factory method to
 * [R.navigation.device_information_nav] 的导航栏入口
 * create an instance of this fragment.
 */
class DeviceInformationFragment : TipsBaseFragment() {

    override fun getTopView(): View? {
        return title_bar
    }

    private val viewModel by viewModels<DeviceInformationModel>({ requireParentFragment() })
    protected val mDeviceViewModel by viewModels<DeviceViewModel>({ requireParentFragment() })

    override fun getLayoutResId(): Int {
        return R.layout.fragment_device_information
    }

    override fun initView(view: View) {
        mTipsBar = tipsBar
        title_bar.setBackListener { requireActivity().onBackPressed() }
        viewModel.init(devId!!)
        initDeviceInfo()
        initStorageSpace()
        initSysStatus()

        storage_space_cl.setOnClickListener {//跳转存储空间

            requireActivity().startActivityForResult(
                Intent(requireActivity(), DiskSpaceActivity::class.java)
                    .putExtra(
                        io.weline.repo.files.constant.AppConstants.SP_FIELD_DEVICE_ID,
                        devId!!
                    ) //是否是EN服务器
                , 1223
            )
        }

        equipment_detail_cl.setOnClickListener {
            findNavController().navigate(
                R.id.action_device_to_hardware,
                HardwareInformationFragmentArgs(devId!!).toBundle()
            )
        }




        system_state_cl.setOnClickListener {
            findNavController().navigate(
                R.id.action_device_to_sys,
                SystemStateFragmentArgs(devId!!).toBundle()
            )
        }
    }

    private fun initDeviceInfo() {
        setEquipmentSn()

        viewModel.mDevice?.let { deviceBean ->
            original_equipment_img.setImageResource(IconHelper.getIcByeDevClassSimple(deviceBean.devClass))
        }

        mDeviceViewModel.refreshDevNameById(devId!!)
            .`as`(RxLife.`as`(this))
            .subscribe({ s: String? ->
                if (!s.isNullOrEmpty()) {
                    equipment_model_tv.text = s
                }
            }) { _: Throwable? ->

            }

    }

    private fun setEquipmentSn() {
        if (viewModel.mHardInfo != null) {
            equipment_sn_tv.text = "SN: ${viewModel.mHardInfo!!.sn}"
        } else {
            viewModel.getHardwareInformation().observe(this, Observer {
                if (it.status == Status.SUCCESS) {
                    val hardInfo: HardInfo? = JsonUtilN.anyToJsonObject(it.data)
                    hardInfo?.let { data ->
                        viewModel.mHardInfo = data
                        equipment_sn_tv.text = "SN: ${viewModel.mHardInfo!!.sn}"
                    }
                } else if (it.status == Status.ERROR) {
                    ToastUtils.showToast(V5HttpErrorNo.getResourcesId(it.code))
                }
            })
        }
    }

    var diskSpaceOverview: DiskSpaceOverview? = null //当用户回退回来时候不需要再次请求
    private fun initStorageSpace() {
        if (diskSpaceOverview != null) {
            setDiskSpace(diskSpaceOverview!!)
        } else {
            viewModel.getDiskStorageSpace().observe(this, Observer {
                if (it.status == Status.SUCCESS) {
                    diskSpaceOverview = it.data
                    setDiskSpace(it.data!!)
                } else if (it.status == Status.ERROR) {
                    setNoData()
                    //ToastUtils.showToast(V5HttpErrorNo.getResourcesId(it.code))
                }
            })
        }

    }

    private fun setNoData(){
        no_disk_flayout.visibility =View.VISIBLE
        available_color_v.visibility = View.VISIBLE
        available_title_tv.visibility = View.VISIBLE
        available_content_tv.visibility = View.VISIBLE

        already_color_v.visibility = View.VISIBLE
        already_title_tv.visibility = View.VISIBLE
        already_content_tv.visibility = View.VISIBLE

        disk_space_total.text =getString(R.string.tip_no_sata)
        already_content_tv.text = 0.toString()
        available_content_tv.text  = 0.toString()
    }


    private fun setDiskSpace(diskSpaceOverview: DiskSpaceOverview) {
        // pie_chart_cl.visibility = View.VISIBLE
        // disk_space_ll.visibility = View.VISIBLE
        if (diskSpaceOverview.proportion.size == 2) {
            pie_chart.setData(
                diskSpaceOverview.proportion.toIntArray(),
                diskSpaceOverview.proportion[0].toString() + "%",
                intArrayOf(
                    ContextCompat.getColor(requireContext(), R.color.color_0d80f8),
                    ContextCompat.getColor(requireContext(), R.color.color_e2edfa)
                )
            )
            //如果不是M8的就不显示磁盘空间大小  有数据再进行展示
            available_color_v.visibility = View.VISIBLE
            available_title_tv.visibility = View.VISIBLE
            available_content_tv.visibility = View.VISIBLE
            no_disk_flayout.visibility =View.GONE

            already_color_v.visibility = View.VISIBLE
            already_title_tv.visibility = View.VISIBLE
            already_content_tv.visibility = View.VISIBLE

            disk_space_total.text = diskSpaceOverview.totalSpace
            already_content_tv.text = diskSpaceOverview.usedSpace
            available_content_tv.text = diskSpaceOverview.freeSpace
        } else if (diskSpaceOverview.proportion.size == 3) {
            pie_chart.setData(
                diskSpaceOverview.proportion.toIntArray(),
                diskSpaceOverview.proportion[1].toString() + "%",
                intArrayOf(
                    ContextCompat.getColor(requireContext(), R.color.color_7cbaf9),
                    ContextCompat.getColor(requireContext(), R.color.color_0d80f8),
                    ContextCompat.getColor(requireContext(), R.color.color_e2edfa)
                )
            )
            //如果不是M8的就不显示磁盘空间大小  有数据再进行展示
            sys_title_tv.visibility = View.VISIBLE
            sys_color_v.visibility = View.VISIBLE
            sys_content_tv.visibility = View.VISIBLE

            available_color_v.visibility = View.VISIBLE
            available_title_tv.visibility = View.VISIBLE
            available_content_tv.visibility = View.VISIBLE

            already_color_v.visibility = View.VISIBLE
            already_title_tv.visibility = View.VISIBLE
            already_content_tv.visibility = View.VISIBLE
            no_disk_flayout.visibility =View.GONE

            sys_content_tv.text = diskSpaceOverview.systemSpace
            disk_space_total.text = diskSpaceOverview.totalSpace
            already_content_tv.text = diskSpaceOverview.usedSpace
            available_content_tv.text = diskSpaceOverview.freeSpace
        }
    }


    private fun initSysStatus() {
        if (viewModel.sysStatus != null) {
            initSysStatusUi(viewModel.sysStatus!!)
        } else {
            viewModel.getSystemStatus().observe(this, Observer {
                if (it.status == Status.SUCCESS) {
                    val sysStatus: SysStatus? = JsonUtilN.anyToJsonObject(it.data)
                    sysStatus?.let { data ->
                        viewModel.sysStatus = data
                        initSysStatusUi(data)
                    }
                } else if (it.status == Status.ERROR) {
                    ToastUtils.showToast(V5HttpErrorNo.getResourcesId(it.code))
                }
            })
        }

    }


    private fun initSysStatusUi(data: SysStatus) {
        cpu_usage_tv.text = data.cpu_use?.let { viewModel.getInt(it) }
        memory_tv.text = data.mem_use?.let { viewModel.getInt(it) }
        cpu_temperature_tv.text = data.cpu_temp?.toString()
        data.hd_temp?.let {  hard_disk_temperature_tv.text =  viewModel.getAverageValue(it) }
        receive_tv.text = "${data.tx_speed?.let { FileUtils.fmtFileSize(it) }}/s"
        send_tv.text = "${data.tx_speed?.let { FileUtils.fmtFileSize(it) }}/s"
    }


}