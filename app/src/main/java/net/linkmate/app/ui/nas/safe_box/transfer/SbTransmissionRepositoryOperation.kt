package net.linkmate.app.ui.nas.safe_box.transfer

import libs.source.common.AppExecutors
import net.linkmate.app.ui.nas.transfer_r.TransferEntityR
import net.sdvn.nascommon.db.SafeBoxTransferHistoryKeeper
import net.sdvn.nascommon.db.objecbox.SafeBoxTransferHistory
import net.sdvn.nascommon.db.objecbox.TransferHistory
import net.sdvn.nascommon.model.oneos.transfer_r.interfaces.CallBack
import net.sdvn.nascommon.model.oneos.transfer_r.interfaces.Repository
import net.sdvn.nascommon.model.oneos.transfer_r.interfaces.RepositoryOperation
import net.sdvn.nascommon.model.oneos.transfer_r.thread.TransferThreadExecutor

/**
create by: 86136
create time: 2021/3/12 15:17
Function description:
 */

class SbTransmissionRepositoryOperation(var deviceID: String, var isDownLoad: Boolean, var isComplete: Boolean) : RepositoryOperation<TransferEntityR> {

    override fun loadData(callBack: CallBack<List<TransferEntityR>>?) {
        executor().execute {
            val sourceList = SafeBoxTransferHistoryKeeper.all(deviceID, isDownLoad, isComplete, 0, Repository.PAGE_SIZE)
            val transferEntityList = mutableListOf<TransferEntityR>()
            for (transferHistory in sourceList) {
                transferEntityList.add(TransferEntityR(convert(transferHistory)))
            }
            AppExecutors.instance.mainThread().execute {
                callBack?.onCallBack(transferEntityList)
            }
        }
    }

    override fun loadMoreData(offset: Long, limit: Long, callBack: CallBack<List<TransferEntityR>>?) {
        executor().execute {
            val sourceList = SafeBoxTransferHistoryKeeper.all(deviceID, isDownLoad, isComplete, offset, limit)
            val transferEntityList = mutableListOf<TransferEntityR>()
            for (transferHistory in sourceList) {
                transferEntityList.add(TransferEntityR(convert(transferHistory)))
            }
            AppExecutors.instance.mainThread().execute {
                callBack?.onCallBack(transferEntityList)
            }
        }
    }


    private fun convert(safeBoxTransferHistory: SafeBoxTransferHistory): TransferHistory {
        val transferHistory = TransferHistory()
        transferHistory.id = safeBoxTransferHistory.id
        transferHistory.uid = safeBoxTransferHistory.uid
        transferHistory.state = safeBoxTransferHistory.state
        transferHistory.type = safeBoxTransferHistory.type
        transferHistory.name = safeBoxTransferHistory.name
        transferHistory.srcPath = safeBoxTransferHistory.srcPath
        transferHistory.srcDevId = safeBoxTransferHistory.srcDevId
        transferHistory.toPath = safeBoxTransferHistory.toPath
        transferHistory.size = safeBoxTransferHistory.size
        transferHistory.length = safeBoxTransferHistory.length
        transferHistory.duration = safeBoxTransferHistory.duration
        transferHistory.time = safeBoxTransferHistory.time
        transferHistory.isComplete = isComplete
        transferHistory.tmpName = safeBoxTransferHistory.tmpName
        return transferHistory
    }

    private fun executor() = TransferThreadExecutor.diskIOExecutor

    override fun getMaxCount(): Long {
        return SafeBoxTransferHistoryKeeper.allCount(deviceID, isDownLoad, isComplete)
    }

}