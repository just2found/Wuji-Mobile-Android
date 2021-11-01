package net.linkmate.app.ui.activity.circle.circleDetail.adapter.device

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.linkmate.app.data.model.CircleDevice
import net.linkmate.app.manager.DevManager
import net.linkmate.app.repository.CircleDeviceRepository
import net.linkmate.app.ui.activity.circle.circleDetail.CircleFragmentViewModel
import net.sdvn.common.repo.BriefRepo
import net.sdvn.common.repo.DevicesRepo
import net.sdvn.common.vo.BindDeviceModel
import net.sdvn.common.vo.BriefModel
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean

/**
 * @author Raleigh.Luo
 * date：20/10/14 11
 * describe：
 */
class CircleSetMainENDeviceViewModel : CircleFragmentViewModel() {
    private val repository = CircleDeviceRepository()

    /*---开始请求远程数据-------------------------------------------------------------------------*/
    private val _startRequestRemoteSource: MutableLiveData<Boolean> = MutableLiveData<Boolean>()
    fun startRequestRemoteSource() {
        _startRequestRemoteSource.value = true
    }
    val remoteEnDevices: LiveData<List<CircleDevice.Device>> =  _startRequestRemoteSource.switchMap {
        repository.getMainENServer( networkId,loaderStateListener = mStateListener)
    }
    /**
     * 获取我的设备
     */
//    val ownAllDevices: LiveData<List<DeviceModel>> = DeviceViewModel().liveDevices

    /*---获取我的EN设备-------------------------------------------------------------------------*/
    private val _startFilterDevices: MutableLiveData<Boolean> = MutableLiveData<Boolean>()
    fun startLocalEnDevices() {
        _startFilterDevices.value = true
    }

    /**
     * 获取我的EN设备
     */
    val ownLocalEnDevices: LiveData<List<BindDeviceModel>> = _startFilterDevices.switchMap {
        object : LiveData<List<BindDeviceModel>>() {
            val started = AtomicBoolean(false)
            override fun onActive() {
                super.onActive()
                if (started.compareAndSet(false, true)) {
                    postValue(DevicesRepo.getOwnENDeviceModels().filter {
                        //过滤不在线设备
                        val deviceId = it.devId
                        val isRealOnline = DevManager.getInstance().boundDeviceBeans?.find {
                            it.id == deviceId
                        }?.hardData?.isRealOnline ?: false
                        isRealOnline
                    })
                }
            }
        }
    }

    fun getDeviceId(position: Int): String {
//        if(remoteEnDevices.value == null || remoteEnDevices.value?.size ==0){
        return ownLocalEnDevices.value?.get(position)?.devId ?: ""
//        }else{
//            return remoteEnDevices.value?.get(position)?.deviceid?:""
//        }
    }

    /*---申请EN服务器的费用-------------------------------------------------------------------------*/
    private val _setMainENServerDeviceId: MutableLiveData<String> = MutableLiveData<String>()
    fun startSetMainENServer(deviceId: String) {
        _setMainENServerDeviceId.value = deviceId
    }

    val setMainENServerResult: LiveData<Boolean> = _setMainENServerDeviceId.switchMap {
        repository.setMainENServer(networkId, it, mUnDismissStateListener)
    }

    /***--设备简介----------------------------------------------------*****/
    private val _deviceIds = MutableLiveData<Array<String>>()
    fun startGetDeviceBriefs(devices: List<BindDeviceModel>?) {
        devices?.let {
            viewModelScope.launch(Dispatchers.IO) {
                _deviceIds.postValue(it.map {
                    it.devId
                }.toTypedArray())
            }
            true
        } ?: let {
            _deviceIds.value = null
        }

    }

    val deviceBriefs = _deviceIds.switchMap {
        if (it == null) {
            MutableLiveData<List<BriefModel>>(null)
        } else {
            BriefRepo.getBriefsLiveData(it, BriefRepo.FOR_DEVICE)
        }

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