package net.sdvn.common.repo

import androidx.lifecycle.LiveData
import io.objectbox.Box
import io.objectbox.android.ObjectBoxLiveData
import net.sdvn.cmapi.CMAPI
import net.sdvn.common.IntrDBHelper
import net.sdvn.common.data.model.CircleDevice
import net.sdvn.common.data.remote.NetRemoteDataSource
import net.sdvn.common.internet.core.GsonBaseProtocol
import net.sdvn.common.internet.core.HttpLoader
import net.sdvn.common.internet.listener.ResultListener
import net.sdvn.common.vo.InNetDeviceModel
import net.sdvn.common.vo.InNetDeviceModel_
import java.util.*

/** 

Created by admin on 2020/10/22,20:19

 */
object InNetDevRepo {
    fun saveData(netId: String, devices: List<CircleDevice.Device>?) {
        val userId = IntrDBHelper.getUserId()
        val box = getInNetDevBox()
        val localDevList = box.query()
                .equal(InNetDeviceModel_.userId, userId)
                .equal(InNetDeviceModel_.netId, netId)
                .build()
                .find()
        devices?.map { device ->
            val iterator = localDevList.iterator()
            while (iterator.hasNext()) {
                val next = iterator.next()
                if (Objects.equals(next.deviceId, device.deviceid)) {
                    iterator.remove()
                    return@map transform(netId, userId, device, next)
                }
            }
            return@map transform(netId, userId, device)
        }?.toList()?.also {
            box.put(it)
        }
        box.remove(localDevList)
    }

    fun transform(netId: String, userId: String, device: CircleDevice.Device, model: InNetDeviceModel? = null): InNetDeviceModel {
        return (model ?: InNetDeviceModel(netId = netId,
                userId = userId,
                deviceId = device.deviceid)).apply {
            deviceName = device.devicename
            deviceSn = device.devicesn
            status = device.status
            addTime = device.addtime
            ownerId = device.ownerid
            firstName = device.firstname
            lastName = device.lastname
            loginName = device.loginname
            deviceType = device.devicetype
            deviceClass = device.deviceclass
            joinStatus = device.joinstatus
            nickname = device.nickname
            flowStatus = device.flowstatus
            isEn = device.isen
            srvMain = device.srvmain
            enable = device.enable
            srvProvide = device.srvprovide
            mbpointratio = device.mbpointratio
        }
    }


    @JvmStatic
    fun getNetDeviceModel(devId: String): InNetDeviceModel? {
        if (CMAPI.getInstance().baseInfo != null && CMAPI.getInstance().baseInfo.userId != null) {
            val box = getInNetDevBox()
            return box.query()
                    .equal(InNetDeviceModel_.userId, IntrDBHelper.getUserId())
                    .equal(InNetDeviceModel_.deviceId, devId)
                    .equal(InNetDeviceModel_.netId, CMAPI.getInstance().baseInfo.netid)
                    .build()
                    .findFirst()
        } else {
            return null
        }

    }

    @JvmStatic
    fun getNetDeviceModels(netId: String): List<InNetDeviceModel> {
        return getInNetDevBox().query()
                .equal(InNetDeviceModel_.userId, IntrDBHelper.getUserId())
                .equal(InNetDeviceModel_.netId, netId)
                .build()
                .find()

    }

    private fun getInNetDevBox(): Box<InNetDeviceModel> {
        return IntrDBHelper.getBoxStore().boxFor(InNetDeviceModel::class.java)
    }

    private val mNetRemoteDataSource: NetRemoteDataSource by lazy {
        NetRemoteDataSource()
    }
    private val devListener: ResultListener<CircleDevice> = object : ResultListener<CircleDevice> {
        override fun error(tag: Any?, baseProtocol: GsonBaseProtocol?) {

        }

        override fun success(tag: Any?, data: CircleDevice) {
            saveData(tag as String, data.data?.list)
        }
    }

    fun refreshInNetENDevices(networkId: String): HttpLoader {
        return mNetRemoteDataSource.getENDevices(networkId, this.devListener)
    }

    fun getInNetDevicesLD(netId: String): LiveData<List<InNetDeviceModel>> {
        val query = getInNetDevBox().query()
                .equal(InNetDeviceModel_.userId, AccountRepo.getUserId())
                .equal(InNetDeviceModel_.netId, netId)
                .build()
        return ObjectBoxLiveData(query)
    }
}