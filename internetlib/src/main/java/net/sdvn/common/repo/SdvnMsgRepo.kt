package net.sdvn.common.repo

import net.sdvn.common.IntrDBHelper
import net.sdvn.common.internet.protocol.entity.MsgCommon
import net.sdvn.common.internet.protocol.entity.SdvnMessage
import net.sdvn.common.vo.MsgCommonModel
import net.sdvn.common.vo.MsgCommonModel_
import net.sdvn.common.vo.SdvnMessageModel
import net.sdvn.common.vo.SdvnMessageModel_
import java.util.*

object SdvnMsgRepo {
    @JvmStatic
    fun saveData(list: List<SdvnMessage>, createTime: Long): Long {
        val userId = IntrDBHelper.getUserId()
        val boxFor = boxSysMsg()
        val history = boxFor.query()
                .equal(SdvnMessageModel_.userId, userId)
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
            val findFirst = boxSysMsg()
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
        return boxSysMsg()
                .put(data)
    }

    @JvmStatic
    fun saveData(datas: List<SdvnMessageModel>) {
        boxSysMsg()
                .put(datas)
    }

    fun boxSysMsg() = IntrDBHelper.getBoxStore()
            .boxFor(SdvnMessageModel::class.java)

    fun init() {

    }

    @JvmStatic
    fun saveCommonData(list: MutableList<MsgCommon>, userId: String, createTime: Long): Long {
        var lastTime = createTime
        val boxFor = boxMsgCommon()
        val history = boxFor.query()
                .equal(MsgCommonModel_.userId, userId)
                .orderDesc(MsgCommonModel_.timestamp)
                .build()
                .find()
                .toMutableList()
        val result = list.map {
            lastTime = Math.max(lastTime, Math.max(it.readTime, Math.max(it.createTime, it.confirmTime)))
            var model: MsgCommonModel? = null
            if (history.isNotEmpty()) {
                val iterator = history.iterator()
                while (iterator.hasNext()) {
                    val next = iterator.next()
                    if (Objects.equals(next.msgId, it.msgId)) {
                        //过期消息不再处理
                        if (next.expired) {
                            model = next
                            iterator.remove()
                            break
                        }
                        if (next.confirmAck != it.confirmAck) {
                            next.confirmAck = it.confirmAck
                            next.isWasRead = false
                        }
                        model = transformMsgCommon(userId, it, next)
                        iterator.remove()
                        break
                    }
                }
            }
            model ?: transformMsgCommon(userId, it)
        }.sortedBy {
            it.timestamp
        }.toSet().also {
            boxFor.put(it)
        }
        if (result.isNotEmpty())
            return maxOf(lastTime, result.last().timestamp)
        return lastTime
    }

    private fun transformMsgCommon(userId: String, it: MsgCommon, model: MsgCommonModel? = null): MsgCommonModel {
        return (model ?: MsgCommonModel().also {
            it.isDisplay = true
        }).apply {
            confirmAck = it.confirmAck
            confirm = it.confirm
            confirmTime = it.confirmTime
            params = it.params
            content = it.content
            msgId = it.msgId
            timestamp = it.createTime
            type = it.msgClass
            msgType = it.msgType
            expired = it.expired == 1
//            isWasRead = it.alreadyRead == 1
            this.title = it.title
            this.userId = userId
        }
    }

    @JvmStatic
    fun getLastTime(userId: String?): Long {
        userId?.let {
            val findFirst = boxMsgCommon()
                    .query()
                    .equal(MsgCommonModel_.userId, userId)
                    .orderDesc(MsgCommonModel_.timestamp)
                    .build()
                    .findFirst()

            val findFirst2 = boxMsgCommon()
                    .query()
                    .equal(MsgCommonModel_.userId, userId)
                    .orderDesc(MsgCommonModel_.confirmTime)
                    .build()
                    .findFirst()

            return maxOf(findFirst?.timestamp ?: 0, findFirst2?.confirmTime ?: 0)
        } ?: return 0
    }

    fun boxMsgCommon() = IntrDBHelper.getBoxStore()
            .boxFor(MsgCommonModel::class.java)

    fun updateCommonData(msgs: MutableList<MsgCommonModel>) {
        boxMsgCommon().put(msgs)
    }

    fun updateCommonData(vararg msg: MsgCommonModel) {
        boxMsgCommon().put(msg.toList())
    }
}