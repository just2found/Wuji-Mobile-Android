package net.linkmate.app.ui.nas.info


import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.weline.repo.data.model.BaseProtocol
import io.weline.repo.data.model.HardInfo
import io.weline.repo.data.model.SysStatus
import io.weline.repo.net.V5Observer
import io.weline.repo.net.V5ObserverImpl
import io.weline.repo.repository.V5Repository
import libs.source.common.livedata.Resource
import libs.source.common.livedata.Status
import net.linkmate.app.bean.DeviceBean
import net.linkmate.app.manager.DevManager
import net.linkmate.app.ui.activity.nasApp.deviceDetial.repository.DeviceSpaceRepository
import net.linkmate.app.ui.simplestyle.device.disk.DiskSpaceModel
import net.linkmate.app.ui.simplestyle.device.disk.data.DiskNode
import net.linkmate.app.ui.simplestyle.device.disk.data.DiskPower
import net.linkmate.app.ui.simplestyle.device.disk.data.DiskSpaceOverview
import net.linkmate.app.ui.simplestyle.device.self_check.data.HDSmartInfoScanAll
import net.linkmate.app.ui.simplestyle.device.self_check.data.HdsInfo
import net.sdvn.common.internet.utils.LoginTokenUtil
import net.sdvn.nascommon.SessionManager
import net.sdvn.nascommon.iface.GetSessionListener
import net.sdvn.nascommon.model.UiUtils
import net.sdvn.nascommon.model.oneos.user.LoginSession
import java.math.BigDecimal


/**
 * 保险箱文件展示相关，
 */
class DeviceInformationModel : ViewModel() {


    var mDevice: DeviceBean? = null
    private lateinit var mDeviceId: String
    private val repository = DeviceSpaceRepository()
    var sysStatus: SysStatus? = null
    var mHardInfo: HardInfo? = null
    var arrayNode: List<DiskNode>? = null
    var isGetNode: Boolean = false//这个是为了解决五个空盘的问题
    var arrayPower: List<DiskPower>? = null //这个电源节点
     var mHdsInfoList: List<HdsInfo>? = null

    fun init(deviceId: String) {
        mDeviceId = deviceId
        val device = DevManager.getInstance().deviceBeans.find {
            it.id == deviceId
        } ?: (let {
            DevManager.getInstance().boundDeviceBeans.find {
                it.id == deviceId
            }
        } ?: let {
            DevManager.getInstance().localDeviceBeans.find {
                it.id == deviceId
            }
        })
        mDevice = device!!
    }




    fun getDiskStorageSpace(): LiveData<Resource<DiskSpaceOverview>> {
        return mDevice?.let {
            val isM8 = UiUtils.isM8(it.devClass)
            repository.querySpace(it.id, isM8)
        } ?: MutableLiveData<Resource<DiskSpaceOverview>>()
    }


    private fun <T> getLoginSession(
        liveData: MutableLiveData<Resource<T>>,
        devID: String,
        next: (loginSession: LoginSession) -> Unit
    ) {
        SessionManager.getInstance().getLoginSession(devID,
            object : GetSessionListener(false) {
                override fun onSuccess(url: String, data: LoginSession) {
                    next(data)
                }

                override fun onFailure(url: String, errorNo: Int, errorMsg: String) {
                    liveData.postValue(Resource.error("", null, errorNo))
                }
            })
    }


    fun getHardwareInformation(): LiveData<Resource<Any>> {
        val liveData = MutableLiveData<Resource<Any>>()
        getLoginSession(liveData, mDeviceId) { loginSession ->
            val v5ObserverImpl = V5ObserverImpl(mDeviceId, liveData)
            V5Repository.INSTANCE().getHardwareInformation(
                loginSession.id
                    ?: "", loginSession.ip, LoginTokenUtil.getToken(), v5ObserverImpl
            )
        }
        return liveData
    }

    fun getSystemStatus(): LiveData<Resource<Any>> {
        val liveData = MutableLiveData<Resource<Any>>()
        getLoginSession(liveData, mDeviceId) { loginSession ->
            val v5ObserverImpl = V5ObserverImpl(mDeviceId, liveData)
            V5Repository.INSTANCE().getSystemStatus(
                loginSession.id
                    ?: "", loginSession.ip, LoginTokenUtil.getToken(), v5ObserverImpl
            )
        }
        return liveData
    }


    fun getHDSmartInfoScanAll(): LiveData<Resource<HDSmartInfoScanAll>> {
        return repository.getHDSmartInforScanAll(mDeviceId)
    }


    fun getAverageValue(list: List<Int>): String {
        var sum = 0;
        for (i in list) {
            sum += i
        }
        return (sum / list.size).toString()
    }

    //四舍五入取整
    fun getInt(number: Double): String {
        val bd: BigDecimal = BigDecimal(number).setScale(0, BigDecimal.ROUND_HALF_UP)
        return bd.toString()
    }

    fun getDiskPowerStatus(): LiveData<Resource<List<DiskPower>>> {
        return mDeviceId?.let {
            repository.getDiskPowerStatus(it)
        } ?: MutableLiveData()
    }

    fun getDiskNodeInfo(): LiveData<Resource<List<DiskNode>>> {
        return mDeviceId?.let {
            repository.queryDiskNodeInfo(it)
        } ?: MutableLiveData()
    }


    fun checkDiskPower(): Boolean {
        var refresh = false
        if (!isGetNode|| arrayPower.isNullOrEmpty()) {
            return refresh;
        }
        val list = mutableListOf<DiskNode>()
        arrayNode?.let { list.addAll(it) }
        arrayPower?.forEach { diskPower ->
            val diskNode = findBySlot(diskPower.slot)
            diskNode?.let {
                if (!diskPower.diskIsOn()) {
                    it.main = DiskSpaceModel.MAIN_UNKNOWN
                    refresh = true
                }
            } ?: let {
                list.add(DiskNode(diskPower.slot, "", "", "", "", DiskSpaceModel.MAIN_UNKNOWN))
                refresh = true
            }
        }
        arrayNode = list
        return refresh;
    }

    fun findBySlot(slot: Int): DiskNode? {
        var diskNode: DiskNode? = null
        arrayNode?.forEach {
            if (it.slot == slot)
                diskNode = it
        }
        return diskNode
    }

}