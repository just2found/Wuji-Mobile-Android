package net.linkmate.app.ui.simplestyle.device.disk

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import libs.source.common.livedata.Resource
import net.linkmate.app.bean.DeviceBean
import net.linkmate.app.manager.DevManager
import net.linkmate.app.ui.activity.nasApp.deviceDetial.repository.DeviceSpaceRepository
import net.linkmate.app.ui.simplestyle.device.disk.data.ActionItem
import net.linkmate.app.ui.simplestyle.device.disk.data.DiskNode
import net.linkmate.app.ui.simplestyle.device.disk.data.DiskPower
import net.linkmate.app.ui.simplestyle.device.disk.data.DiskSpaceOverview
import net.sdvn.nascommon.model.UiUtils

/** 

Created by admin on 2020/8/20,16:05

 */
class DiskSpaceModel : ViewModel() {

    companion object {
        const val LVM_MODE = "lvm"
        const val RADIO0_MODE = "raid0"
        const val RADIO1_MODE = "raid1"
        const val RADIO5_MODE = "raid5"
        const val BASIC_MODE = "basic"
        const val RADIO10_MODE = "raid10"
        const val OPT_EXTEND = "extend"
        const val OPT_CREATE = "create"
        const val MAIN_UNKNOWN = 1000


        const val DISK_SPACE_MODEL_KEY = "DiskSpaceModelKey"

        //这三个是Activity用的
        const val SPACE_MANAGEMENT = 0;//登录的流程控制
        const val FORMAT = 1;//初始话
    }

    private val repository = DeviceSpaceRepository()

    var mDevice: DeviceBean? = null
    var mDeviceId: String? = null


    var arrayNode: List<DiskNode>? = null
    var isGetNode: Boolean = false//这个是为了解决五个空盘的问题
    var arrayPower: List<DiskPower>? = null //这个电源节点

    var arrayOpt: List<ActionItem>? = null
    var diskSpaceOverview: DiskSpaceOverview? = null

    /**
     * 在调用方法前必须先调用这个方法
     */
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
                    it.main = MAIN_UNKNOWN
                    refresh = true
                }
            } ?: let {
                list.add(DiskNode(diskPower.slot, "", "", "", "", MAIN_UNKNOWN))
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


    fun getDiskNodeInfo(): LiveData<Resource<List<DiskNode>>> {
        return mDeviceId?.let {
            repository.queryDiskNodeInfo(it)
        } ?: MutableLiveData()
    }

    fun getDiskActionItem(): LiveData<Resource<List<ActionItem>>> {
        return mDeviceId?.let {
            repository.getDiskActionItem(it)
        } ?: MutableLiveData()

    }

    fun createDiskActionItem(mode: String): LiveData<Resource<Boolean>> {
        return mDeviceId?.let { deviceId ->
            val devices = mutableListOf<String>()
            arrayNode!!.forEach {
                if (it.main == 0 || it.main == 1)
                    devices.add(it.device)
            }
            repository.createDiskActionItem(deviceId, mode, devices, OPT_CREATE)
        } ?: MutableLiveData()
    }

    //扩容
    fun extendDiskActionItem(): LiveData<Resource<Boolean>> {
        mDeviceId?.let { deviceId ->
            //获取可以扩展的操作
            arrayOpt?.forEach {
                if (it.op == OPT_EXTEND && !it.deivces.isNullOrEmpty()) {
                    return repository.createDiskActionItem(deviceId, it.mode, it.deivces, OPT_EXTEND)
                }
            }
        }
        return MutableLiveData<Resource<Boolean>>()
    }


    fun getDiskManageStatus(): LiveData<Resource<Boolean>> {
        return mDeviceId?.let {
            repository.getDiskManageStatus(it)
        } ?: MutableLiveData()
    }

    fun getDiskPowerStatus(): LiveData<Resource<List<DiskPower>>> {
        return mDeviceId?.let {
            repository.getDiskPowerStatus(it)
        } ?: MutableLiveData()
    }

}
