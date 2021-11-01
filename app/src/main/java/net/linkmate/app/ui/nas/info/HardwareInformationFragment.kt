package net.linkmate.app.ui.nas.info

import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import io.weline.repo.api.V5HttpErrorNo
import io.weline.repo.data.model.HardInfo
import kotlinx.android.synthetic.main.fragment_hardware_information.*
import libs.source.common.livedata.Status
import libs.source.common.utils.JsonUtilN

import net.linkmate.app.R
import net.linkmate.app.ui.nas.TipsBaseFragment
import net.linkmate.app.ui.nas.info.adpter.NasDiskSlotAdapter
import net.linkmate.app.ui.simplestyle.device.self_check.data.HdsInfo
import net.linkmate.app.util.ToastUtils
import net.sdvn.nascommon.utils.FileUtils


/**
 * A simple [Fragment] subclass.
 * Use the [HardwareInformationFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class HardwareInformationFragment : TipsBaseFragment() {

    private val viewModel by viewModels<DeviceInformationModel>({ requireParentFragment() })
    private var hasSplicingData = false
    override fun getTopView(): View? {
        return title_bar
    }


    override fun getLayoutResId(): Int {
        return R.layout.fragment_hardware_information
    }

    override fun initView(view: View) {
        mTipsBar = tipsBar
        hasSplicingData = false
        title_bar.setBackListener { findNavController().navigateUp() }
        requestHardInfo()
        getHDSmartInfoScanAll()
        supplementSlot()
    }

    private fun requestHardInfo() {
        if (viewModel.mHardInfo != null) {
            initHardInfo(viewModel.mHardInfo!!)
        } else {
            viewModel.getSystemStatus().observe(this, Observer {
                if (it.status == Status.SUCCESS) {
                    val hardInfo: HardInfo? = JsonUtilN.anyToJsonObject(it.data)
                    hardInfo?.let { data ->
                        viewModel.mHardInfo = data
                        initHardInfo(data)
                    }
                } else if (it.status == Status.ERROR) {
                    ToastUtils.showToast(V5HttpErrorNo.getResourcesId(it.code))
                }
            })
        }
    }

    private fun initHardInfo(data: HardInfo) {
        product_model_ail.setTips(data.model)
        sn_ail.setTips(data.sn)
        mac_ail.setTips(data.mac)
        system_version_number_ail.setTips(data.sys_version)
        cpu_ail.setTips(data.cpu)
        data.ddr_size?.let { memory_ail.setTips(FileUtils.fmtFileSize(it)) }
    }

    private fun supplementSlot() {
        //先获取电源信息 盘道信息
        viewModel.getDiskPowerStatus().observe(this, Observer {
            if (it.status == Status.SUCCESS) {
                viewModel.arrayPower = it.data

                //在获取磁盘信息进行拼接
                viewModel.getDiskNodeInfo().observe(this, Observer {
                    if (it.status == Status.SUCCESS) {
                        viewModel.arrayNode = it.data
                        viewModel.isGetNode = true
                        viewModel.checkDiskPower()
                        if (viewModel.checkDiskPower()) {
                            splicingData()
                        }
                    }
                })


            }
        })
    }


    private fun splicingData(): Boolean {
        if (viewModel.mHardInfo == null || hasSplicingData) {
            return false
        }
        hasSplicingData = true
        val list = mutableListOf<HdsInfo>()
        viewModel.arrayNode?.forEach {
            val s = findOrCreateByName(it.device)
            s.slot = it.slot
            list.add(s)
        }
        if (!list.isNullOrEmpty()) {
            list.sortBy {
                it.slot
            }
            viewModel.mHdsInfoList = list
            initHDSmartInfoScanAll(list)
        } else {
            return false
        }
        return true
    }

    private fun findOrCreateByName(name: String): HdsInfo {
        viewModel.mHdsInfoList?.forEach {
            if (it.name == name) {
                return it
            }
        }
        return HdsInfo(null, null, -1)
    }


    private fun getHDSmartInfoScanAll() {
        viewModel.getHDSmartInfoScanAll().observe(this, Observer {
            if (it.status == Status.SUCCESS) {
                it.data?.hds?.let { listData ->
                    viewModel.mHdsInfoList = listData
                    if (!splicingData()) {
                        initHDSmartInfoScanAll(listData)
                    }
                }
            } else if (it.status == Status.ERROR) {
                ToastUtils.showToast(V5HttpErrorNo.getResourcesId(it.code))
            }
        })
    }

    private fun initHDSmartInfoScanAll(list: List<HdsInfo>) {
        val nasDiskSlotAdapter = NasDiskSlotAdapter()
        nasDiskSlotAdapter.setNewData(list)
        disk_list_rv.adapter = nasDiskSlotAdapter
    }


}