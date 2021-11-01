package io.weline.internetdb

import io.weline.internetdb.vo.SdvnMessageModel
import io.weline.internetdb.vo.SdvnMessageModel_
import net.sdvn.common.internet.protocol.entity.SdvnMessage
import java.util.*

object SdvnMsgRepo {
    @JvmStatic
    fun saveData(list: List<SdvnMessage>, createTime: Long): Long {
        val userId = IntrDBHelper.getUserId()
        val boxFor = IntrDBHelper.getBoxStore()
                .boxFor(SdvnMessageModel::class.java)
        val history = boxFor.query()
                .equal(SdvnMessageModel_.display, true)
                .orderDesc(SdvnMessageModel_.timestamp)
                .build()
                .find()
                .toMutableList()
        val result = list.map {
            var sdvnMessageModel: SdvnMessageModel? = null
            if (history.isNotEmpty()) {
                val iterator = history.iterator()
                while (iterator.hasNext()) {
                    val next = iterator.next()
                    if (Objects.equals(next.msgId, it.getNewsid())) {
                        next.status = it.status
                        if (next.status != it.status) {
                            next.isWasRead = false
                        }
                        sdvnMessageModel = next
                        iterator.remove()
                        break
                    }
                }
            }
            sdvnMessageModel ?: transform(userId, it)
        }.sortedBy {
            it.timestamp
        }.toSet().also {
            boxFor.put(it)
        }
        if (result.isNotEmpty())
            return result.last().timestamp
        return createTime

    }

    private fun transform(userId: String?, it: SdvnMessage, model: SdvnMessageModel? = null): SdvnMessageModel {
        val sdvnMessageModel = model ?: SdvnMessageModel().also {
            it.isDisplay = true
        }
        sdvnMessageModel.status = it.status
        sdvnMessageModel.isSelect = it.isSelect
        sdvnMessageModel.message = it.message
        sdvnMessageModel.username = it.username
        sdvnMessageModel.msgId = it.newsid
        sdvnMessageModel.timestamp = it.timestamp
        sdvnMessageModel.type = it.type
        sdvnMessageModel.userId = userId
        return sdvnMessageModel
    }

    @JvmStatic
    fun getLastItemTime(userId: String?): Long {
        userId?.let {
            val findFirst = IntrDBHelper.getBoxStore()
                    .boxFor(SdvnMessageModel::class.java)
                    .query()
                    .equal(SdvnMessageModel_.userId, userId)
                    .orderDesc(SdvnMessageModel_.timestamp)
                    .build()
                    .findFirst()
            return findFirst?.timestamp ?: 0
        } ?: return 0
    }

    @JvmStatic
    fun saveData(data: SdvnMessageModel): Long {
        return IntrDBHelper.getBoxStore()
                .boxFor(SdvnMessageModel::class.java)
                .put(data)
    }

    @JvmStatic
    fun saveData(datas: List<SdvnMessageModel>) {
        IntrDBHelper.getBoxStore()
                .boxFor(SdvnMessageModel::class.java)
                .put(datas)
    }
}