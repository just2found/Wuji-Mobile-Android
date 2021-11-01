package io.weline.internetdb

import io.objectbox.Box
import io.objectbox.android.ObjectBoxLiveData
import io.weline.internetdb.vo.NetworkModel
import io.weline.internetdb.vo.NetworkModel_
import net.sdvn.common.internet.protocol.entity.BindNetModel
import java.util.*

/** 

Created by admin on 2020/10/18,00:17

 */
object NetsRepo {
    @JvmStatic
    fun saveData(list: List<BindNetModel>?) {
        val userId = IntrDBHelper.getUserId()
        val box = getBox()
        val localDevList = box.query()
                .equal(NetworkModel_.userId, userId)
                .build()
                .find()
        list?.map { newData ->
            val iterator = localDevList.iterator()
            while (iterator.hasNext()) {
                val next = iterator.next()
                if (Objects.equals(next.netId, newData.networkId)) {
                    iterator.remove()
                    return@map transform(userId, newData, next)
                }
            }
            return@map transform(userId, newData)
        }?.toList().also {
            box.put(it)
        }
        box.remove(localDevList)
    }

    fun transform(userId: String, newData: BindNetModel, old: NetworkModel? = null): NetworkModel {
        val model = old ?: NetworkModel()
        model.userId = userId
        model.apply {
            netId = newData.networkId
            netName = newData.networkName
            addTime = newData.addTime
            ownerId = newData.ownerId
            firstName = newData.firstname
            lastName = newData.lastname
            loginName = newData.loginname
            netStatus = newData.networkStatus
            srvProvide = newData.isSrvProvide
            userLevel = newData.userLevel
            userStatus = newData.userStatus
            isDevSepCharge = newData.isDevSepCharge
            flowStatus = newData.flowStatus
        }
        return model
    }

    fun getData(): ObjectBoxLiveData<NetworkModel> {
        val box = getBox()
        val build = box.query()
                .equal(NetworkModel_.userId, IntrDBHelper.getUserId())
                .build()
        return ObjectBoxLiveData(build)
    }

    private fun getBox(): Box<NetworkModel> {
        return IntrDBHelper.getBoxStore().boxFor(NetworkModel::class.java)
    }
}