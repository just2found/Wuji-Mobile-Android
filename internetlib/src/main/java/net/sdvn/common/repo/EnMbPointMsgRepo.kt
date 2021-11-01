package net.sdvn.common.repo

import net.sdvn.common.IntrDBHelper
import net.sdvn.common.internet.protocol.DataEnMbPointMsg
import net.sdvn.common.internet.protocol.entity.FlowMbpointRatioModel
import net.sdvn.common.vo.EnMbPointMsgModel
import net.sdvn.common.vo.EnMbPointMsgModel_
import java.util.*

object EnMbPointMsgRepo {


    @JvmStatic
    fun saveData(list: List<DataEnMbPointMsg>, userId: String?, createTime: Long): Long {
        val boxFor = IntrDBHelper.getBoxStore()
                .boxFor(EnMbPointMsgModel::class.java)
        val history = boxFor.query()
                .equal(EnMbPointMsgModel_.timestamp, createTime)
                .or()
                .greater(EnMbPointMsgModel_.timestamp, createTime)
                .orderDesc(EnMbPointMsgModel_.timestamp)
                .build()
                .find()
                .toMutableList()

        val map = mutableMapOf<String, EnMbPointMsgModel>()
        val result = list.sortedBy { it.createtime }.map { data ->
            var foundModel: EnMbPointMsgModel? = null
            if (history.isNotEmpty()) {
                val iterator = history.iterator()
                for (model in iterator) {
                    if (Objects.equals(data.msgid, model.msgId)) {
                        foundModel = model
                        iterator.remove()
                        break
                    }
                }
            }
            data.content?.deviceid.takeIf { !it.isNullOrEmpty() }
                    ?.let {
                        DevicesRepo.getDeviceModel(it)?.let { bindDeviceModel ->
                            if (data.content.type == FlowMbpointRatioModel.Type.setmbpratio.name
                                    || data.content.type == FlowMbpointRatioModel.Type.cancelmbpratio.name) {
                                bindDeviceModel.mbpChValue = data.content.mbpointratio
                                bindDeviceModel.gb2cChValue = data.content.gb2cRatio
                                bindDeviceModel.mbpChTime = data.content.schemedate
                                DevicesRepo.saveData(bindDeviceModel)
                            }
                        }
                    }

            transform(data, userId, foundModel)
        }.sortedBy {
            it.timestamp
        }.map {
            val deviceId = it.content.deviceid
            if (deviceId?.isNotEmpty() == true) {
                val get = map.get(deviceId)
                if (get != null) {
                    if (get.timestamp < it.timestamp) {
//                        get.isWasRead = true
//                        get.isDisplay = false
//                        it.isDisplay = true
                        map.put(deviceId, it)
                    } else {
//                        it.isWasRead = true
//                        it.isDisplay = false
//                        get.isDisplay = true
                    }
                } else {
//                    it.isDisplay = true
                    map.put(deviceId, it)
                }
            }
            it
        }.toSet().also {
            boxFor.put(it)
        }
        if (result.isNotEmpty())
            return result.last().timestamp
        return createTime

    }

    private fun transform(data: DataEnMbPointMsg, userId: String?, enMbPointMsgModel: EnMbPointMsgModel? = null): EnMbPointMsgModel {
        val model = enMbPointMsgModel ?: EnMbPointMsgModel()
        model.content =data.content
        model.timestamp = data.createtime
        model.msgId = data.msgid
        model.type = data.content?.type ?: data.msgtype.toString()
        model.userId = userId
        return model
    }

    @JvmStatic
    fun getLastItemTime(userId: String?): Long {
        userId?.let {
            val findFirst = IntrDBHelper.getBoxStore()
                    .boxFor(EnMbPointMsgModel::class.java)
                    .query()
                    .equal(EnMbPointMsgModel_.userId, userId)
                    .orderDesc(EnMbPointMsgModel_.timestamp)
                    .build()
                    .findFirst()
            return findFirst?.timestamp ?: 0
        } ?: return 0
    }

    @JvmStatic
    fun saveData(data: EnMbPointMsgModel): Long {
        return IntrDBHelper.getBoxStore()
                .boxFor(EnMbPointMsgModel::class.java)
                .put(data)
    }

    @JvmStatic
    fun saveData(datas: List<EnMbPointMsgModel>) {
        IntrDBHelper.getBoxStore()
                .boxFor(EnMbPointMsgModel::class.java)
                .put(datas)
    }
}