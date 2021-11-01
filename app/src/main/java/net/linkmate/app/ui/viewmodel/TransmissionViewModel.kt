package net.linkmate.app.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import io.objectbox.android.ObjectBoxLiveData
import io.objectbox.query.Query
import libs.source.common.AppExecutors
import net.linkmate.app.ui.nas.transfer.TransferEntity
import net.sdvn.nascommon.db.TransferHistoryKeeper
import net.sdvn.nascommon.db.objecbox.TransferHistory
import net.sdvn.nascommon.model.oneos.transfer.DownloadManager
import net.sdvn.nascommon.model.oneos.transfer.TransmissionManager
import net.sdvn.nascommon.model.oneos.transfer.UploadManager
import net.sdvn.nascommon.utils.log.Logger
import java.util.*
import kotlin.collections.set

class TransmissionViewModel : ViewModel() {
    //    private val mOnTransferCompleteListenerDownload: TransferManager.OnTransferCompleteListener<DownloadElement>? = null
//    private val mOnTransferCompleteListenerUpload: TransferManager.OnTransferCompleteListener<UploadElement>? = null
    private val map = HashMap<String, LiveData<List<TransferEntity>>>()
    private val map2 = HashMap<String, Query<TransferHistory>>()
//    private var onTransferCountListener: TransferManager.OnTransferCountListener? = null

    init {
//        val service = SessionManager.getInstance().service
//        if (service != null) {
//            onTransferCountListener = TransferManager.OnTransferCountListener { isDownload, count ->
////                notifyChanged(isDownload, true)
//            }
//            //            mOnTransferCompleteListenerDownload = (isDownload, count) -> notifyChanged(isDownload, false);
//            //            mOnTransferCompleteListenerUpload = (isDownload, count) -> notifyChanged(isDownload, false);
//            //            service.addDownloadCompleteListener(mOnTransferCompleteListenerDownload);
//            //            service.addUploadCompleteListener(mOnTransferCompleteListenerUpload);
//            service.addOnTransferCountListener(onTransferCountListener)
//        }
    }

    fun getManager(isDownload: Boolean): TransmissionManager<*> {
        return if (isDownload)
            DownloadManager.getInstance()
        else
            UploadManager.getInstance()
    }

    fun notifyChanged(isDownload: Boolean, refresh: Boolean) {
        val start = System.currentTimeMillis()
        val liveData = map[getKey(isDownload, null)]
        if (liveData != null) {
            val manager = getManager(isDownload)
            val transferList = manager.transferList.toMutableList()
            liveData.value?.let {
                for (transferEntity in it) {
                    if (transferList.isEmpty()) {
                        break
                    }
                    val iterator = transferList.iterator()
                    for (transferElement in iterator) {
                        if (transferEntity.id == transferElement.id) {
                            transferEntity.element = transferElement
                            iterator.remove()
                            break
                        }
                    }
                }
            }

        }
        Logger.LOGD(TAG, "find time consumed :", System.currentTimeMillis() - start)
    }

    fun getTransferHistoryLiveDataPaged(isDownload: Boolean, isComplete: Boolean?): LiveData<List<TransferEntity>> {
        val key = getKey(isDownload, isComplete)
        var liveData = map[key]
        if (liveData == null || map2[key] == null) {
            val query = TransferHistoryKeeper.QueryTransferHistory(isDownload, isComplete).invoke()
            liveData = MediatorLiveData<List<TransferEntity>>()
            liveData.addSource(ObjectBoxLiveData(query), Observer {
                val manager = getManager(isDownload)
                val transferList = manager.transferList.toMutableList()
                AppExecutors.instance.networkIO().execute {
                    liveData.postValue(it.map<TransferHistory, TransferEntity> { history ->
                        if (history.isComplete) {
                            TransferEntity.convert(history)
                        } else {
                            val iterator = transferList.iterator()
                            var entity: TransferEntity? = null
                            for (transferElement in iterator) {
                                if (history.id == transferElement.id) {
                                    entity = TransferEntity(transferElement)
                                    iterator.remove()
                                    break
                                }
                            }
                            entity ?: TransferEntity.convert(history)
                        }
                    })
                }
            })
            map[key] = liveData
            map2[key] = query
        }

        return liveData
    }

    private fun getKey(isDownload: Boolean, isComplete: Boolean?): String {
        return String.format("%s/%s", isDownload, isComplete)
    }


    override fun onCleared() {
        super.onCleared()
        destroy()
        map2.clear()
        map.clear()
    }

    fun destroy() {
//        val service = SessionManager.getInstance().service
//        if (service != null) {
//            if (onTransferCountListener != null) {
//                service.removeOnTransferCountListener(onTransferCountListener)
//            }
//            if (mOnTransferCompleteListenerDownload != null) {
//                service.removeDownloadCompleteListener(mOnTransferCompleteListenerDownload)
//            }
//            if (mOnTransferCompleteListenerUpload != null) {
//                service.removeUploadCompleteListener(mOnTransferCompleteListenerUpload)
//            }
//        }
    }

    fun deleteComplete(devId: String?, isDownload: Boolean) {
        TransferHistoryKeeper.deleteComplete(devId, isDownload)
//        notifyChanged(isDownload, true)
    }

    companion object {
        internal val TAG = TransmissionViewModel::class.java.simpleName
    }


}


