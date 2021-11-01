package net.linkmate.app.ui.simplestyle.circle

import android.text.TextUtils
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.linkmate.app.base.MyConstants
import net.linkmate.app.ui.simplestyle.BriefCacheViewModel
import net.linkmate.app.util.MySPUtils
import net.sdvn.cmapi.CMAPI
import net.sdvn.common.repo.BriefRepo
import net.sdvn.common.repo.NetsRepo
import net.sdvn.common.vo.BriefModel
import net.sdvn.common.vo.NetworkModel
import java.util.concurrent.ConcurrentHashMap

/**
 * @author Raleigh.Luo
 * date：20/11/25 16
 * describe：
 */
class CircleViewModel : BriefCacheViewModel() {
    private val refreshCircles = MutableLiveData<Boolean>()

    fun refresh() {
        if (MySPUtils.getBoolean(MyConstants.IS_LOGINED) && !TextUtils.isEmpty(CMAPI.getInstance().baseInfo.userId)) {
            NetsRepo.refreshNetList()//CircleViewModel
            refreshCircles.value = true
        }
    }

    //我的圈子
    val circles: LiveData<List<NetworkModel>> = refreshCircles.switchMap {
        NetsRepo.getData()
    }


    fun getCirclesSize(): Int {
        return circles.value?.size ?: 0
    }

    //当前操作网络对象
    val currentOptNetwork: MutableLiveData<NetworkModel> = MutableLiveData()

    val switchNetworkResult = MutableLiveData<Int>()

    /***--圈子简介----------------------------------------------------*****/
    private val _deviceIds = MutableLiveData<Array<String>>()
    fun startGetBriefs(networks: List<NetworkModel>) {
        viewModelScope.launch(Dispatchers.IO) {
            val filterNetworks = networks.filter {
                !TextUtils.isEmpty(it.mainENDeviceId)
            }
            if (filterNetworks != null && filterNetworks.size > 0) {
                //是否有新设备id
                var hasNewDeviceId = false
                if (_deviceIds.value == null) {
                    hasNewDeviceId = true
                } else {
                    filterNetworks.forEach {
                        if (!circleBriefsMap.containsKey(it.mainENDeviceId)) {
                            hasNewDeviceId = true
                            return@forEach
                        }
                    }
                }
                if (hasNewDeviceId) {//有新数据，则需更新livedata数据源，否则不触发
                    _deviceIds.postValue(filterNetworks.map {
                        it.mainENDeviceId ?: ""
                    }.toTypedArray())
                }
            } else {
                _deviceIds.postValue(null)
            }
        }
    }

    val circleBriefs = _deviceIds.switchMap {
        if (it == null || it.size == 0) {
            MutableLiveData<List<BriefModel>>(null)
        } else {
            BriefRepo.getBriefsLiveData(it, BriefRepo.FOR_CIRCLE)
        }
    }

    private val circleBriefsMap: ConcurrentHashMap<String, Int> = ConcurrentHashMap<String, Int>()
    fun initDeviceBriefsMap() {
        circleBriefs.value?.let {
            viewModelScope.launch(Dispatchers.IO) {
                circleBriefsMap.clear()
                it.forEachIndexed { index, briefModel ->
                    circleBriefsMap.put(briefModel.deviceId, index)
                }
            }.invokeOnCompletion {
                _refreshAdapter.postValue(-1)
            }
            true
        } ?: let {
            circleBriefsMap.clear()
            _refreshAdapter.value = -1
        }
    }

    //value position －1 表示刷新所有
    private val _refreshAdapter = MutableLiveData<Int>()
    val refreshAdapter = _refreshAdapter

    fun getBrief(deviceId: String): BriefModel? {
        val index = circleBriefsMap.get(deviceId)
        if (index != null && index >= 0 && index < (circleBriefs.value?.size ?: 0)) {
            return circleBriefs.value?.get(index)
        } else {
            return null
        }
    }
}