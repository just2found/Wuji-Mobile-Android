package net.linkmate.app.ui.nas.transfer_r

import io.objectbox.annotation.Transient
import net.sdvn.nascommon.model.oneos.transfer.TransferElement
import net.sdvn.nascommon.model.oneos.transfer.TransferState
import net.sdvn.nascommon.model.oneos.transfer_r.BasicRepository
import net.sdvn.nascommon.model.oneos.transfer_r.interfaces.Repository
import org.view.libwidget.log.L
import java.util.concurrent.locks.ReentrantLock

/**
create by: 86136
create time: 2021/3/12 13:37
Function description:
 */

class TransferEntityRepository(key: String? = null) : BasicRepository<TransferEntityR>(key) {

    private val mReentrantLock by lazy {ReentrantLock()}

    @Transient
    val REFRESH_TIME: Long = 400

    override fun getLock(): ReentrantLock {
        return mReentrantLock
    }

    fun isAllStart(): Boolean {
        var flag = true
        for (transferEntityR in mDataSource) {
            if (transferEntityR.state != TransferState.WAIT && transferEntityR.state != TransferState.START) {
                flag = false
            }
        }
        return flag
    }

    fun hasStart(): Boolean {
        var flag = false
        for (transferEntityR in mDataSource) {
            if (transferEntityR.state == TransferState.WAIT || transferEntityR.state == TransferState.START) {
                return true
            }
        }
        return flag
    }


    fun getMaxCount(): Int {
        return mRepositoryOperation?.getMaxCount()?.toInt() ?: mDataSource.size
    }

    fun updateTransmission(transferElement: TransferElement) {
        val t = findDataObjectS(transferElement.tag)
        t?.let {
            if (it.state != TransferState.PAUSE) {
                it.state = transferElement.state
                it.offset = transferElement.length
                it.fileSize = transferElement.size
                it.speed = transferElement.speed
                if (System.currentTimeMillis() - it.lastTime >= REFRESH_TIME) {
                    it.lastTime = System.currentTimeMillis()
                    notifyDataObserver(Repository.UPDATE_DATA, findDataPositionS(t), 1)
                }
            }
        }
    }


    fun onlyDelete(entity: TransferEntityR) {
        val t = findDataPositionS(entity)
        if (t != -1) {
            mDataSource.removeAt(t)
        }
    }


    //这个是传输完成后，删除对应的数据
    fun deleteComplete(transferElement: TransferElement) {
        val t = findDataObjectS(transferElement.tag)
        t?.let {
            it.state = transferElement.state
            it.offset = transferElement.offset
            it.fileSize = transferElement.size
            it.isComplete = true
            deleteData(t)
        }
    }


    fun filterByTransferState(state: TransferState): List<TransferEntityR> {
        val list = mDataSource.filter {
            it.state == state
        }
        return list
    }

    fun deleteList(list: List<TransferEntityR>?) {
        deleteDataList(list)
    }


    //这个是传输完成后，插入数据
    fun insertComplete(entity: TransferEntityR) {
        mDataSource.add(0, entity)
    }


    fun updateFail(transferElement: TransferElement) {
        val t = findDataObjectS(transferElement.tag)
        t?.let {
            it.state = transferElement.state
            it.offset = transferElement.offset
            it.fileSize = transferElement.size
            it.speed = transferElement.speed
            it.exception = transferElement.exception
            notifyDataObserver(Repository.UPDATE_DATA, findDataPositionS(t), 1)
        }
    }

    /**
     * Pause all tasks
     *
     * @return true if succeed, false otherwise.
     * @see .pause
     */
    fun pause() {
        mDataSource.forEach {
            if (it.state != TransferState.PAUSE) {
                it.state = TransferState.PAUSE
            }
        }
    }

    /**
     * Pause all tasks
     * @return true if succeed, false otherwise.
     * @see .pause
     */
    fun resume() {
        mDataSource.forEach {
            if (it.state != TransferState.WAIT || it.state != TransferState.START) {
                it.state = TransferState.WAIT
            }
        }
    }


}