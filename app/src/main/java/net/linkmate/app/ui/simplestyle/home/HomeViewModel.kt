package net.linkmate.app.ui.simplestyle.home

import android.os.Handler
import android.os.Message
import androidx.arch.core.util.Function
import androidx.lifecycle.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.linkmate.app.base.DevBoundType
import net.linkmate.app.base.MyConstants
import net.linkmate.app.bean.DeviceBean
import net.linkmate.app.manager.DevManager
import net.linkmate.app.ui.simplestyle.BriefCacheViewModel
import net.linkmate.app.util.MySPUtils
import net.sdvn.cmapi.CMAPI
import net.sdvn.common.repo.BriefRepo
import net.sdvn.common.vo.BriefModel
import java.util.concurrent.ConcurrentHashMap


/**
 * @author Raleigh.Luo
 * date：20/11/18 13
 * describe：
 */
class HomeViewModel : BriefCacheViewModel() {
    @Volatile
    private var mLastUserId: String? = null

    private val mObserver: DevManager.DevUpdateObserver

    init {
        mObserver = object : DevManager.DevUpdateObserver {
            override fun onDevUpdate(devBoundType: Int) {
                if (devBoundType == DevBoundType.ALL_BOUND_DEVICES) {
                    refresh()
                }
            }
        }
        DevManager.getInstance().addDevUpdateObserver(mObserver)
    }

    //最大显示Loading时间为5秒
    private val MAX_INIT_DEVICE_DELAY = 5 * 1000L
    private val handler = object : Handler() {
        override fun handleMessage(msg: Message?) {
            super.handleMessage(msg)
            if (msg?.what == 0) {
                if (_refreshDevices.value == null) {
                    _refreshDevices.value = true
                }
            }
        }
    }

    private val _refreshDevices = MutableLiveData<Boolean>()
    val refreshDevices: LiveData<Boolean> = _refreshDevices


    @Volatile
    private var isRefreshing: Boolean = false

    fun refresh() {
        if (MySPUtils.getBoolean(MyConstants.IS_LOGINED)) {
            if (!isSameUser()) {//不同用户更新
                loadingDevices(Function {
                    isRefreshing = false
                    null
                })
            } else {//相同用户更新
                if (!isRefreshing) {
                    isRefreshing = true
                    loadingDevices(Function {
                        if (refreshDevices.value == null && devices.size > 0) {
                            //null第一次触发，
                            _refreshDevices.value = true
                        } else {
                            _refreshDevices.value = refreshDevices.value
                        }
                        isRefreshing = false
                        null
                    })
                }
            }
        }
    }

    /**
     * 检查是否时相同用户
     */
    private fun isSameUser(): Boolean {
        val currentUserId = CMAPI.getInstance().baseInfo.userId
        if (mLastUserId != currentUserId) {//不同用户时，先清空设备数据
            devices.clear()
            _refreshDevices.value = null
            mLastUserId = currentUserId
            //延迟加载机制
            handler.sendEmptyMessageDelayed(0, MAX_INIT_DEVICE_DELAY)
            return false
        } else {
            return true
        }

    }

    private var loadingDevicesJob: Job? = null

    /**
     *  协程加载数据
     */
    fun loadingDevices(callback: Function<Void, Void>? = null) {
        if ((DevManager.getInstance().boundDeviceBeans?.size ?: 0) > 0) {
//            val temp = DevManager.getInstance().getAllBoundAdapterDevices2()
//            if (temp.size > 0) {
//                devices = temp
//                _refreshDevices.value = true
//            }
//            callback?.apply(null)
            if (loadingDevicesJob != null && !(loadingDevicesJob?.isCompleted ?: false)) {
                //已有任务，
                loadingDevicesJob?.cancel()
            }
            loadingDevicesJob = null
            loadingDevicesJob = viewModelScope.launch {
                loading()
            }
            loadingDevicesJob?.invokeOnCompletion {//任务完成
                callback?.apply(null)
            }
        } else {//清空数据
            if (devices.size > 0) devices.clear()
            callback?.apply(null)
        }

    }


    suspend fun loading() = withContext(Dispatchers.IO) {
        // 繁重任务
        val temp = DevManager.getInstance().allBoundAdapterDevices2
        if (temp.size > 0) {
            //加载简介
            startGetDeviceBriefs(temp)
            devices = temp
            _refreshDevices.postValue(true)
        }
    }

    fun startGetDeviceBriefs(devices: List<DeviceBean>) {
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

    /***--所有设备简介----------------------------------------------------*****/
    private val _deviceIds = MutableLiveData<Array<String>>()

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


    /**
     * onStart检查绑定设备是否已有数据
     */
    /**
     * onStart检查绑定设备是否已有数据
     */
    fun checkRefresh() {
        if (checkLoggedin()) {
            isSameUser()
            if (refreshDevices.value == null) {
                loadingDevices()
            }
        }
    }

    //我的设备
    var devices: ArrayList<DeviceBean> = arrayListOf()

    fun getDevicesSize(): Int {
        return devices.size
    }

    //当前操作设备对象
    val currentOptDevice: MutableLiveData<DeviceBean> = MutableLiveData()

    //当前长按操作设备对象
    val currentLongOptDevice: MutableLiveData<DeviceBean> = MutableLiveData()

    val initHardWareListResult: MutableLiveData<Boolean> = MutableLiveData()

    override fun onCleared() {
        super.onCleared()
        DevManager.getInstance().deleteDevUpdateObserver(mObserver)
    }
}