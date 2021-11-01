package net.linkmate.app.ui.simplestyle.circledetail

import android.text.TextUtils
import androidx.lifecycle.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.linkmate.app.base.DevBoundType
import net.linkmate.app.bean.DeviceBean
import net.linkmate.app.manager.BriefManager
import net.linkmate.app.manager.DevManager
import net.linkmate.app.ui.simplestyle.BriefCacheViewModel
import net.sdvn.common.repo.BriefRepo
import net.sdvn.common.repo.NetsRepo
import net.sdvn.common.vo.BriefModel
import net.sdvn.common.vo.NetworkModel
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.atomic.AtomicBoolean

/** 我的绑定设备
 * @author Raleigh.Luo
 * date：20/11/19 10
 * describe：
 */
class CircleDeviceViewModel : BriefCacheViewModel() {
    private val _refreshDevice = MutableLiveData<Boolean>()
    private val mObserver: DevManager.DevUpdateObserver

    init {
        mObserver = object : DevManager.DevUpdateObserver {
            override fun onDevUpdate(devBoundType: Int) {
                if (devBoundType == DevBoundType.IN_THIS_NET) {
                    refresh()
                }
            }
        }
        DevManager.getInstance().addDevUpdateObserver(mObserver)
        refresh()
    }

    override fun onCleared() {
        super.onCleared()
        DevManager.getInstance().deleteDevUpdateObserver(mObserver)
    }

    /**
     * 刷新并刷新服务器数据
     */
    fun refresh() {
        _refreshDevice.value = true
    }


    //我的设备
    val devices: LiveData<CopyOnWriteArrayList<DeviceBean>> = _refreshDevice.switchMap {
        object : LiveData<CopyOnWriteArrayList<DeviceBean>>() {
            private val started = AtomicBoolean(false)
            override fun onActive() {
                super.onActive()
                if (started.compareAndSet(false, true)) {
                    postValue(CopyOnWriteArrayList(DevManager.getInstance().adapterDevices.filter {
                        it.type != -1//过滤掉增加的头部标题
                    }))
                }
            }
        }
    }

    //当前操作设备对象
    val currentOptDevice: MutableLiveData<DeviceBean> = MutableLiveData()

    //当前长按操作设备对象
    val currentLongOptDevice: MutableLiveData<DeviceBean> = MutableLiveData()

    private val refreshCurrentNetwork = MutableLiveData<String>()
    fun refreshCurrentNetwork(defaultNetworkId: String) {
        refreshCurrentNetwork.value = defaultNetworkId
    }

    val currentNetwork = refreshCurrentNetwork.switchMap {
        object : LiveData<NetworkModel>() {
            override fun onActive() {
                super.onActive()
                //getCurrentNet数据更新更新不及时，需去数据库查询，如网络名称
                val networkId = NetsRepo.getCurrentNet()?.netId ?: it
                postValue(NetsRepo.getOwnNetwork(networkId))
            }
        }
    }

    /***－－圈子简介－－－－－－－－－－**/
    val startGetCircleBrief = MutableLiveData<String>()
    fun startGetCircleBrief(deviceId: String?) {
        if (startGetCircleBrief.value == null || startGetCircleBrief.value != deviceId) {
            startGetCircleBrief.value = deviceId
            deviceId?.let {
                BriefManager.requestRemoteBrief(deviceId, BriefRepo.FOR_CIRCLE, BriefRepo.ALL_TYPE)
            }
        }
    }

    val circleBrief = startGetCircleBrief.switchMap {
        if (TextUtils.isEmpty(it)) {
            MutableLiveData<List<BriefModel>>(null)
        } else {
            BriefRepo.getBriefLiveData(it, BriefRepo.FOR_CIRCLE)
        }
    }

    /***--所有设备简介----------------------------------------------------*****/
    private val _deviceIds = MutableLiveData<Array<String>>()
    fun startGetDeviceBriefs(devices: List<DeviceBean>) {
        viewModelScope.launch(Dispatchers.IO) {
            //是否有新设备id
            var hasNewDeviceId = false
            _deviceIds.value?.let {
                devices.forEach {
                    if (!deviceBriefsMap.containsKey(it.id)) {
                        hasNewDeviceId = true
                        return@forEach
                    }
                }
                true
            } ?: let {
                hasNewDeviceId = true
            }

            if (hasNewDeviceId) {//有新数据，则需更新livedata数据源，否则不触发
                _deviceIds.postValue(devices.map {
                    it.id
                }.toTypedArray())
            }
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