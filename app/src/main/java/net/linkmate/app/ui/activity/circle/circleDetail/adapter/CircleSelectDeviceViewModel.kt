package net.linkmate.app.ui.activity.circle.circleDetail.adapter

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.linkmate.app.data.model.CircleJoinWay
import net.linkmate.app.manager.DevManager
import net.linkmate.app.repository.CircleDeviceRepository
import net.linkmate.app.repository.CircleRepository
import net.linkmate.app.ui.activity.circle.circleDetail.CircleFragmentViewModel
import net.linkmate.app.ui.activity.circle.circleDetail.FunctionHelper
import net.sdvn.common.repo.BriefRepo
import net.sdvn.common.repo.DevicesRepo
import net.sdvn.common.vo.BindDeviceModel
import net.sdvn.common.vo.BriefModel
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean

/**
 * @author Raleigh.Luo
 * date：20/10/14 10
 * describe：
 */
class CircleSelectDeviceViewModel : CircleFragmentViewModel() {
    private val repository = CircleDeviceRepository()
    private val circleRepository = CircleRepository()
    var filterDeviceIds: List<String>? = null

    //是否是管理员或所有者
    var isManager = false

    /**
     * 获取我的设备
     */
//    val ownAllDevices: LiveData<List<DeviceModel>> = DeviceViewModel().liveDevices

    /*---获取我的EN设备-------------------------------------------------------------------------*/
    private val _startFilterDevices: MutableLiveData<Boolean> = MutableLiveData<Boolean>()
    fun startFilterDevices() {
        _startFilterDevices.value = true
    }

    /**
     * 获取我的EN设备
     */
    val ownFilteredDevices: LiveData<List<BindDeviceModel>> = _startFilterDevices.switchMap {
        object : LiveData<List<BindDeviceModel>>() {
            val started = AtomicBoolean(false)
            override fun onActive() {
                super.onActive()
                if (started.compareAndSet(false, true)) {
                    when (function) {
                        FunctionHelper.SELECT_EN_DEVICE -> {//选择EN  - 新增
                            postValue(DevicesRepo.getOwnENDeviceModels().filterNot {
                                val deviceId = it.devId
                                val isRealOnline = DevManager.getInstance().boundDeviceBeans?.find {
                                    it.id == deviceId
                                }?.hardData?.isRealOnline ?: false
                                //过滤不在线设备
                                filterDeviceIds?.contains(deviceId) ?: false || !isRealOnline
                            })
                        }
                        FunctionHelper.SELECT_OWN_DEVICE -> {//选择我的设备  - 新增
                            postValue(DevicesRepo.getOwnDeviceModels().filterNot {
                                val deviceId = it.devId
                                val isRealOnline = DevManager.getInstance().boundDeviceBeans?.find {
                                    it.id == deviceId
                                }?.hardData?.isRealOnline ?: false
                                //过滤不在线设备
                                filterDeviceIds?.contains(it.devId) ?: false || !isRealOnline
                            })
                        }
                    }
//                    postValue(ownAllDevices.value?.filter { dev ->
//                        dev.isOwner && dev.wareDevice?.isEN ?: false && DevTypeHelper.isNas(dev.devClass)
//                    })
                }
            }
        }
    }

    /*---申请EN服务器的费用-------------------------------------------------------------------------*/
    private val _startGetEnServerFee = MutableLiveData<BindDeviceModel>()
    val startGetEnServerFee: LiveData<BindDeviceModel> = _startGetEnServerFee
    fun startGetApplyEnServerFee(device: BindDeviceModel?) {
        _startGetEnServerFee.value = device
    }

    val applyEnServerFeeResult: LiveData<List<CircleJoinWay.Fee>> = startGetEnServerFee.switchMap {
        circleRepository.getFee(networkId, "net-provide", mStateListener)
    }

    /*--- 将我的设备加入到圈子-------------------------------------------------------------------------*/
    private val _joinCircleDeviceId: MutableLiveData<String> = MutableLiveData<String>()
    fun startDeviceJoinCircle(deviceId: String) {
        _joinCircleDeviceId.value = deviceId
    }

    val deviceJoinCircleResult: LiveData<Boolean> = _joinCircleDeviceId.switchMap {
        repository.deviceJoinCircle(networkId, it, mStateListener)
    }

    /*---  将我的设备设置为EN服务器-------------------------------------------------------------------------*/
    private val _setENServerDeviceId: MutableLiveData<String> = MutableLiveData<String>()
    fun startSetENServer(deviceId: String) {
        applyEnServerFeeResult.value?.let {
            if (it.size > 0) {
                it.get(0)?.let {
                    _setENServerDeviceId.value = deviceId
                }
            }
        }
    }

    val setENServerResult: LiveData<Boolean> = _setENServerDeviceId.switchMap {
        if (isManager) {
            repository.setMainENServer(networkId, it, mUnDismissStateListener, false)
        } else {
            var total = 0f
            applyEnServerFeeResult.value?.get(0)?.let {
                total = (it.value ?: 0f) + (it.deposit ?: 0f)
            }
            repository.setENServer(networkId, it, applyEnServerFeeResult.value!!.get(0).feeid,
                    total, mUnDismissStateListener)

        }

    }

    /***--设备简介----------------------------------------------------*****/
    private val _deviceIds = MutableLiveData<Array<String>>()
    fun startGetDeviceBriefs(devices: List<BindDeviceModel>) {
        viewModelScope.launch(Dispatchers.IO) {
            _deviceIds.postValue(devices.map {
                it.devId
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