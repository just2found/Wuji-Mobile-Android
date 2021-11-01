package net.linkmate.app.ui.viewmodel

import androidx.lifecycle.*
import net.sdvn.nascommon.SessionManager
import net.sdvn.nascommon.db.TransferHistoryKeeper
import net.sdvn.nascommon.model.oneos.transfer.DownloadElement
import net.sdvn.nascommon.model.oneos.transfer.TransferManager
import net.sdvn.nascommon.service.NasService

class TransferCountViewModel : ViewModel() {

    private var service: NasService? = null
    val downloadCompleteLiveData = MutableLiveData<DownloadElement>()
    val transferCountLiveData = MutableLiveData<Int>()
    private val downloadCompleteListener = object : TransferManager.OnTransferCompleteListener<DownloadElement> {
        override fun onComplete(isDownload: Boolean, element: DownloadElement) {
            if (isDownload) {
                downloadCompleteLiveData.postValue(element)
            }
        }
    }

    //检测上传下载的任务数并显示
    private var uploadCount = 0
    private var downloadCount = 0
    private val transferCountListener = object : TransferManager.OnTransferCountListener {
        override fun onChanged(isDownload: Boolean, count: Int) {
            if (isDownload) {
                downloadCount = count
            } else {
                uploadCount = count
            }
            transferCountLiveData.postValue(uploadCount + downloadCount)
        }
    }

    private val observer = Observer<NasService> {
        service?.let { service ->
            service.removeOnTransferCountListener(transferCountListener)
            service.removeDownloadCompleteListener(downloadCompleteListener)
        }
        service = it
        service?.let { service ->
            service.addOnTransferCountListener(transferCountListener)
            service.addDownloadCompleteListener(downloadCompleteListener)
        }
    }

//    private val observerDB = object : Observer<BoxStore?> {
//        var liveDataDelegate: LiveDataDelegate<TransferHistory>? = null
//        override fun onChanged(it: BoxStore?) {
//            if (it != null) {
//                if (liveDataDelegate != null) {
//                    liveDataDelegate = null
//                }
//                val query: Query<TransferHistory> = it
//                        .boxFor(TransferHistory::class.java)
//                        .query()
//                        .isNull(TransferHistory_.userId)
//                        .or()
//                        .equal(TransferHistory_.userId, DBHelper.getAccount())
//                        .build()
//                liveDataDelegate = LiveDataDelegate(query).apply {
//                    observeForever {
//
//                    }
//                }
//
//            }
//        }
//    }

    init {
        SessionManager.getInstance().serviceLiveData.observeForever(observer)
//        DBHelper.sStoreLiveData.observeForever(observerDB)
    }

    override fun onCleared() {
        service?.let { service ->
            service.removeOnTransferCountListener(transferCountListener)
            service.removeDownloadCompleteListener(downloadCompleteListener)
        }
        SessionManager.getInstance().serviceLiveData.removeObserver(observer)
//        observerDB.onChanged(null)
//        DBHelper.sStoreLiveData.removeObserver(observerDB)
        super.onCleared()
    }

    fun getLiveDataIncompleteCount(devId: String?): LiveData<Int> {
        return transferCountLiveData.map {
            TransferHistoryKeeper.getIncompleteCount(devId).toInt()
        }
    }
}