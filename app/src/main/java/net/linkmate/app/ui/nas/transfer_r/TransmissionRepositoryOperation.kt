package net.linkmate.app.ui.nas.transfer_r

import libs.source.common.AppExecutors
import net.sdvn.nascommon.db.TransferHistoryKeeper
import net.sdvn.nascommon.model.oneos.transfer_r.interfaces.CallBack
import net.sdvn.nascommon.model.oneos.transfer_r.interfaces.Repository
import net.sdvn.nascommon.model.oneos.transfer_r.interfaces.RepositoryOperation
import net.sdvn.nascommon.model.oneos.transfer_r.thread.TransferThreadExecutor

/**
create by: 86136
create time: 2021/3/12 15:17
Function description:
 */

class TransmissionRepositoryOperation(var deviceID: String, var isDownLoad: Boolean, var isComplete: Boolean) : RepositoryOperation<TransferEntityR> {

    override fun loadData(callBack: CallBack<List<TransferEntityR>>?) {
        executor().execute {
            val sourceList = TransferHistoryKeeper.all(deviceID, isDownLoad, isComplete, 0, Repository.PAGE_SIZE)
            val transferEntityList = mutableListOf<TransferEntityR>()
            for (transferHistory in sourceList) {
                transferEntityList.add(TransferEntityR(transferHistory))
            }
            AppExecutors.instance.mainThread().execute {
                callBack?.onCallBack(transferEntityList)
            }
        }
    }

    override fun loadMoreData(offset: Long, limit: Long, callBack: CallBack<List<TransferEntityR>>?) {
        executor().execute {
            val sourceList = TransferHistoryKeeper.all(deviceID, isDownLoad, isComplete, offset, limit)
            val transferEntityList = mutableListOf<TransferEntityR>()
            for (transferHistory in sourceList) {
                transferEntityList.add(TransferEntityR(transferHistory))
            }
            AppExecutors.instance.mainThread().execute {
                callBack?.onCallBack(transferEntityList)
            }
        }
    }

    private fun executor() = TransferThreadExecutor.diskIOExecutor

    override fun getMaxCount(): Long {
        return TransferHistoryKeeper.allCount(deviceID, isDownLoad, isComplete)
    }

}