package net.linkmate.app.ui.simplestyle.device.self_check

import android.content.Context
import android.text.TextUtils
import androidx.lifecycle.*
import libs.source.common.livedata.Resource
import libs.source.common.livedata.Status
import net.linkmate.app.ui.activity.nasApp.deviceDetial.repository.DeviceSpaceRepository
import net.linkmate.app.ui.simplestyle.device.self_check.data.DiskCheckUiInfo
import net.linkmate.app.util.ToastUtils

/**
create by: 86136
create time: 2021/2/6 10:20
Function description:
 */

class DiskSelfCheckModel : ViewModel() {

    private val repository = DeviceSpaceRepository()
    private var mLiveData: MutableLiveData<Resource<List<DiskCheckUiInfo>>>? = null
    private val diskInfoList = mutableListOf<DiskCheckUiInfo>()
    private val withoutError = "without error"//做为磁盘状态是否良好的判断依据
    var lastTime: String? = null

    fun startDiskSelfCheck(devId: String): LiveData<Resource<Boolean>> {
        return repository.startDiskSelfCheck(devId)
    }


    fun getDiskCheckReport(owner: LifecycleOwner, devId: String): LiveData<Resource<List<DiskCheckUiInfo>>> {
        val liveData = MutableLiveData<Resource<List<DiskCheckUiInfo>>>()
        repository.getDiskCheckReport(devId).observe(owner, Observer {
            if (it.status == Status.SUCCESS) {
                val list = it.data?.checkReportList
                lastTime = it.data?.createAt
                if (list.isNullOrEmpty()) {
                    liveData.postValue(Resource.success(null))
                } else {
                    var total = 0
                    var progress = 0
                    list.forEach { diskResult ->
                        total += 100
                        progress += diskResult.progress
                    }
                    if (progress == total) {
                        mLiveData = liveData
                        diskInfoList.clear()
                        list.forEach { diskResult ->
                            val diskCheckUiInfo = DiskCheckUiInfo(diskResult.device, diskResult.msg.contains(withoutError))
                            diskInfoList.add(diskCheckUiInfo)
                        }
                        getHDSmartInforSystem(owner, devId)
                    } else {
                        liveData.postValue(Resource.loading(null, "${progress * 100 / total}%"))
                    }
                }
            } else {
                liveData.postValue(Resource.error(it.message ?: "", null, code = it.code!!))
            }
        })
        return liveData;
    }

    private var mInfoScanAll = false
    fun getHDSmartInfoScanAll(owner: LifecycleOwner, devId: String) {
        if (mInfoScanAll)
            return
        mInfoScanAll = true
        repository.getHDSmartInforScanAll(devId).observe(owner, Observer { resoucre ->
            mInfoScanAll = false
            if (resoucre.status == Status.SUCCESS) {
                resoucre.data?.hds?.forEach {
                    val diskCheckUiInfo = findDiskCheckUiInfo(it.name)
                    diskCheckUiInfo?.let { data ->
                        it?.smartinfo?.let { smartinfo ->

                            data.diskName = smartinfo.deviceModel
                            data.serialNumber = smartinfo.serialNumber
                            smartinfo.userCapacity?.let { userCapacity ->
                                if (userCapacity.contains("[") && userCapacity.contains("]")) {
                                    data.diskSize = userCapacity.substring(userCapacity.indexOf("[") + 1, userCapacity.indexOf("]"))
                                }
                            }
                        }
                    }
                }
                checkIsComplete()
            } else {
                checkIsComplete()
            }
        })
    }

    private var InforSystem = false
    fun getHDSmartInforSystem(owner: LifecycleOwner, devId: String) {
        if (InforSystem)
            return
        InforSystem = true
        repository.getHDSmartInforSystem(devId).observe(owner, Observer {
            if (it.status == Status.SUCCESS) {
                it.data?.let { result7P16 ->
                    result7P16.hds?.forEach { hDSmartInforSystem ->
                        findDiskCheckUiInfo(hDSmartInforSystem.name)?.let { diskCheckUiInfo ->
                            hDSmartInforSystem.powerOnHours?.let {
                                diskCheckUiInfo.useTime = it.toString()
                            }
                        }
                    }
                }
                getHDSmartInfoScanAll(owner, devId)
            }
            InforSystem = false
        })
    }

    fun checkIsComplete() {
        mLiveData?.postValue(Resource.success(diskInfoList))
        mLiveData = null
    }


    private fun findDiskCheckUiInfo(name: String?): DiskCheckUiInfo? {
        var d: DiskCheckUiInfo? = null
        diskInfoList.forEach {
            if (it.deviceId == name)
                d = it
        }
        return d
    }


}
