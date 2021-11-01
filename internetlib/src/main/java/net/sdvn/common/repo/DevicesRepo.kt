package net.sdvn.common.repo

import androidx.lifecycle.LiveData
import io.objectbox.android.ObjectBoxLiveData
import io.objectbox.query.Query
import net.sdvn.common.IntrDBHelper
import net.sdvn.common.internet.protocol.entity.HardWareDevice
import net.sdvn.common.internet.protocol.entity.MGR_LEVEL
import net.sdvn.common.vo.BindDeviceModel
import net.sdvn.common.vo.BindDeviceModel_
import java.util.*

object DevicesRepo {
    @JvmStatic
    fun saveData(devList: List<HardWareDevice>) {
        val userId = IntrDBHelper.getUserId()
        val box = getDevbox()
        val localDevList = box.query()
                .equal(BindDeviceModel_.userId, userId)
                .build()
                .find()
        devList.map { device ->
            val iterator = localDevList.iterator()
            while (iterator.hasNext()) {
                val next = iterator.next()
                if (Objects.equals(next.devId, device.deviceid)) {
                    iterator.remove()
                    return@map transform(userId, device, next)
                }
            }
            return@map transform(userId, device)
        }.toList().also {
            box.put(it)
        }
        box.remove(localDevList)

    }

    private fun transform(userId: String, device: HardWareDevice, next: BindDeviceModel? = null): BindDeviceModel {
        val model = next ?: BindDeviceModel()
        model.firstName = device.firstname
        model.lastName = device.lastname
        model.ownerUserId = device.userid
        model.devId = device.deviceid
        model.devSN = device.devicesn
        model.devClass = device.ostype
        model.mgrLevel = device.mgrlevel
        model.devName = device.devicename
        model.scanConfirm = device.isScanconfirm
        model.enableShare = device.enableshare
        model.location = device.location
        model.comment = device.comment
        model.devType = device.devicetype
        model.networks = device.networkIds
        model.networkId = device.networkId
        model.gainMBPUrl = device.gainmbp_url
        model.dateTime = device.datetime

        model.isSrcProvide = device.isSrcProvide
        model.isEN = device.isEN
        model.mbpRatio = device.mbpointratio
        model.maxMbpRatio = device.maxMbpointratio
        model.minMbpRatio = device.minMbpointratio
        model.chRatioAble = device.isChangeRatioAble
        model.mbpChValue = device.mbprationSchemevalue
        model.mbpChTime = device.mbpratioSchemetime
        model.gb2cRatio = device.gb2cRatio
        model.maxGb2cRatio = device.maxGb2cRatio
        model.minGb2cRatio = device.minGb2cRatio
        model.gb2cChValue = device.gb2cRatioSchemeValue
        model.status = device.status
        model.nickname = device.nickname
        model.chargetype = device.chargetype
        model.userId = userId
        return model
    }

    @JvmStatic
    fun transform(model: BindDeviceModel): HardWareDevice {
        val device = HardWareDevice()
        device.firstname = model.firstName
        device.lastname = model.lastName
        device.userid = model.ownerUserId
        device.deviceid = model.devId
        device.devicesn = model.devSN
        device.ostype = model.devClass
        device.mgrlevel = model.mgrLevel
        device.devicename = model.devName
        device.isScanconfirm = model.scanConfirm
        device.enableshare = model.enableShare
        device.location = model.location
        device.comment = model.comment
        device.devicetype = model.devType
        device.networkIds = model.networks
        device.networkId = model.networkId
        device.gainmbp_url = model.gainMBPUrl
        device.datetime = model.dateTime

        device.setEn(model.isEN)
        device.isSrcProvide = model.isSrcProvide
        device.mbpointratio = model.mbpRatio
        device.maxMbpointratio = model.maxMbpRatio
        device.minMbpointratio = model.minMbpRatio
        device.isChangeRatioAble = model.chRatioAble
        device.mbprationSchemevalue = model.mbpChValue
        device.mbpratioSchemetime = model.mbpChTime
        device.gb2cRatio = model.gb2cRatio
        device.maxGb2cRatio = model.maxGb2cRatio
        device.minGb2cRatio = model.minGb2cRatio
        device.gb2cRatioSchemeValue = model.gb2cChValue
        device.status = model.status
        device.nickname = model.nickname
        device.chargetype = model.chargetype
        return device
    }


    @JvmStatic
    fun saveData(dev: BindDeviceModel) {
        val box = getDevbox()
        box.put(dev)
    }

    @JvmStatic
    fun getDeviceModel(devId: String): BindDeviceModel? {

        val box = getDevbox()
        return box.query()
                .equal(BindDeviceModel_.userId, IntrDBHelper.getUserId())
                .equal(BindDeviceModel_.devId, devId)
                .build()
                .findFirst()
    }

    @JvmStatic
    fun getDeviceModels(): LiveData<List<BindDeviceModel>> {
        val query: Query<BindDeviceModel> = getDevbox().query().equal(BindDeviceModel_.userId, IntrDBHelper.getUserId())
                .build()
        return ObjectBoxLiveData(query)
    }

    @JvmStatic
    fun getDeviceModelList(): List<BindDeviceModel> {
        val query: Query<BindDeviceModel> = getDevbox().query().equal(BindDeviceModel_.userId, IntrDBHelper.getUserId())
                .build()
        return query.find()
    }

    @JvmStatic
    fun getOwnDeviceModels(): List<BindDeviceModel> {
        val box = getDevbox()
        return box.query()
                .equal(BindDeviceModel_.userId, IntrDBHelper.getUserId())
                .equal(BindDeviceModel_.mgrLevel, MGR_LEVEL.OWNER)
                .build()
                .find()
    }

    @JvmStatic
    fun getOwnENDeviceModels(): List<BindDeviceModel> {
        val box = getDevbox()
        return box.query()
                .equal(BindDeviceModel_.userId, IntrDBHelper.getUserId())
                .equal(BindDeviceModel_.mgrLevel, MGR_LEVEL.OWNER)
                .equal(BindDeviceModel_.isEN, true)
                .build()
                .find()
    }

    private fun getDevbox() = IntrDBHelper.getBoxStore().boxFor(BindDeviceModel::class.java)

    fun init() {


    }

    @JvmStatic
    fun remove(devId: String): Boolean {
        val devbox = getDevbox()
        val find = devbox.query()
                .equal(BindDeviceModel_.userId, IntrDBHelper.getUserId())
                .equal(BindDeviceModel_.devId, devId)
                .build()
                .findFirst()
        return find?.let {
            devbox.remove(it)
        } ?: false
    }
}