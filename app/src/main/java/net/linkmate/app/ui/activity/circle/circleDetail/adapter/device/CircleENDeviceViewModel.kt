package net.linkmate.app.ui.activity.circle.circleDetail.adapter.device

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.linkmate.app.data.model.CircleDevice
import net.linkmate.app.repository.CircleDeviceRepository
import net.linkmate.app.ui.activity.circle.circleDetail.CircleFragmentViewModel
import net.sdvn.cmapi.CMAPI
import net.sdvn.common.repo.BriefRepo
import net.sdvn.common.vo.BriefModel
import java.util.concurrent.ConcurrentHashMap

/**
 * @author Raleigh.Luo
 * date：20/10/14 13
 * describe：
 */
class CircleENDeviceViewModel : CircleFragmentViewModel(){
    private val repository = CircleDeviceRepository()
    /*---开始请求远程数据-------------------------------------------------------------------------*/
    private val _startRequestRemoteSource: MutableLiveData<Boolean> = MutableLiveData<Boolean>()
    fun startRequestRemoteSource() {
        _startRequestRemoteSource.value = true
    }
    val enDevices: LiveData<List<CircleDevice.Device>> =  _startRequestRemoteSource.switchMap {
        repository.getENDevices( networkId,loaderStateListener = mStateListener)
    }
    fun getDeviceIds():ArrayList<String>{
        val deviceIds = arrayListOf<String>()
        enDevices.value?.filter {
            it.ownerid == CMAPI.getInstance().baseInfo.userId
        }?.forEach {
            deviceIds.add(it.deviceid?:"")
        }
        return deviceIds
    }


    /***--设备简介----------------------------------------------------*****/
    private val _deviceIds = MutableLiveData<Array<String>>()
    fun startGetDeviceBriefs(devices: List<CircleDevice.Device>) {
        viewModelScope.launch(Dispatchers.IO) {
            _deviceIds.postValue(devices.map {
                it.deviceid?:""
            }.toTypedArray())
        }
    }

    val deviceBriefs = _deviceIds.switchMap {
        BriefRepo.getBriefsLiveData(it, BriefRepo.FOR_DEVICE)
    }

    private val deviceBriefsMap: ConcurrentHashMap<String, Int> = ConcurrentHashMap<String, Int>()
    fun initDeviceBriefsMap() {
        viewModelScope.launch(Dispatchers.IO) {
            deviceBriefsMap.clear()
            deviceBriefs.value?.forEachIndexed { index, briefModel ->
                deviceBriefsMap.put(briefModel.deviceId, index)
            }
        }.invokeOnCompletion {
            _refreshAdapter.postValue(-1)
        }
    }

    //value position －1 表示刷新所有
    private val _refreshAdapter = MutableLiveData<Int>()
    val refreshAdapter = _refreshAdapter

    fun getBrief(deviceId: String): BriefModel? {
        val index = deviceBriefsMap.get(deviceId)
        if (index != null && index >= 0 && index < (deviceBriefs.value?.size ?: 0)) {
            return deviceBriefs.value?.get(index)
        } else {
            return null
        }
    }
}