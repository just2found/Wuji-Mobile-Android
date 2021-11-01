package net.sdvn.nascommon.model.oneos.tansfer_safebox

import android.net.Uri
import net.sdvn.nascommon.db.SafeBoxTransferHistoryKeeper
import net.sdvn.nascommon.db.objecbox.SafeBoxTransferHistory
import net.sdvn.nascommon.model.oneos.api.file.OneOSUploadFileAPI
import net.sdvn.nascommon.model.oneos.transfer.OnTransferFileListener
import net.sdvn.nascommon.model.oneos.transfer.TransferException
import net.sdvn.nascommon.model.oneos.transfer.TransferState
import net.sdvn.nascommon.model.oneos.transfer.UploadElement
import net.sdvn.nascommon.model.oneos.transfer_r.thread.TaskRunnable
import net.sdvn.nascommon.utils.FileUtils
import org.view.libwidget.log.L
import java.io.File
import java.util.concurrent.locks.ReentrantLock

/**
create by: 86136
create time: 2021/3/12 10:35
Function description:
 */

object SafeBoxUploadManager : SafeBoxTransferManager<UploadElement>(false) {

    override fun convertData(dbList: List<SafeBoxTransferHistory>): List<UploadElement> {
        val list = mutableListOf<UploadElement>()
        for (history in dbList) {
            if (history.length as Long == history.size) {
                history.isComplete = true
                history.state = TransferState.COMPLETE
                SafeBoxTransferHistoryKeeper.update(history)

                val file = File(history.srcPath)
                val element = UploadElement(file, history.toPath)
                if (FileUtils.isPictureFile(file.name)
                        || FileUtils.isVideoFile(file.name)
                        || FileUtils.isGifFile(file.name)) element.thumbUri = Uri.fromFile(file)
                element.length = history.length
                element.toDevId = history.srcDevId
                element.id = history.id
                addSafeBoxTransferHistory(history)
                onComplete(element)

                continue
            }
            val file = File(history.srcPath)
            val element = UploadElement(file, history.toPath)
            if (FileUtils.isPictureFile(file.name)
                    || FileUtils.isVideoFile(file.name)
                    || FileUtils.isGifFile(file.name)) element.thumbUri = Uri.fromFile(file)
            element.length = history.length
            element.toDevId = history.srcDevId
            element.id = history.id
            val state = history.state
            if (file.exists()) {
                element.state = state
            } else {
                element.state = TransferState.FAILED
                element.exception = TransferException.FILE_NOT_FOUND
            }
            list.add(element)
        }
        return list
    }

    override fun enqueue(element: UploadElement): Int {
        var hashCode = FILE_IS_EXIST
        var query = SafeBoxTransferHistoryKeeper.query(SafeBoxTransferHistoryKeeper.getTransferType(isDownload), element.getDevId(), element.getSrcPath(), element.srcName, element.getToPath())
        if (query != null) {
            if (!query.isComplete) { //未完成则更新进度
                element.length = query.length
                element.id = query.id
            } else {   //如果任务已经完成则则见返回-1
                element.isCheck = true

                return hashCode
            }
        } else {
            query = SafeBoxTransferHistory(null, null, SafeBoxTransferHistoryKeeper.getTransferType(isDownload),
                    element.srcName, element.getSrcPath(),
                    element.getDevId(), element.getToPath(), element.getSize(),
                    element.getLength(), 0L, element.getTime(), false, null)
            element.id = SafeBoxTransferHistoryKeeper.insert(query)
        }
        hashCode = element.hashCode()
        wakeUpTask()
        notifyTransferReCount()
        return hashCode
    }


//    override fun onComplete(element: UploadElement) {
//        mTransferThreadExecutor.runInIO {
//            val state = element.state
//            val query = SafeBoxTransferHistoryKeeper.queryById(element.id)
//            var dataRefreshEvent: TransferRefreshEvent<UploadElement>? = null
//            if (state == TransferState.CANCELED) {
//                SafeBoxTransferHistoryKeeper.delete(query)
//                notifyTransferReCount()
//                dataRefreshEvent = TransferRefreshEvent(TRANSFER_CANCELED, element)
//            } else if (state == TransferState.PAUSE) {
//                if (query != null) {
//                    query.length = element.length
//                    query.state = state
//                    SafeBoxTransferHistoryKeeper.update(query)
//                }
//                dataRefreshEvent = TransferRefreshEvent(TRANSFER_PAUSE, element)
//            } else if (state == TransferState.FAILED) {
//                if (query != null) {
//                    query.length = element.length
//                    query.state = state
//                    SafeBoxTransferHistoryKeeper.update(query)
//                }
//                dataRefreshEvent = TransferRefreshEvent(TRANSFER_FAIL, element)
//            } else if (state == TransferState.COMPLETE) {
//                if (query != null) {
//                    query.time = System.currentTimeMillis()
//                    query.isComplete = true
//                    query.length = element.length
//                    query.state = state
//                    SafeBoxTransferHistoryKeeper.update(query)
//                }
//                dataRefreshEvent = TransferRefreshEvent(TRANSFER_COMPLETE, element)
//                notifyTransferReCount()
//                notifyComplete(element)
//                transferRefreshLiveData.postValue(dataRefreshEvent)
//            }
//
//            dataRefreshEvent?.let {
//                mTransferThreadExecutor.onlyRemove(element.tag)
//                wakeUpTask(false)
//                callBack?.get()?.onCallBack(it)
//            }
//            L.i(element.tag + ":" + state.name, "onComplete", "UploadManagerR", "nwq", "2021/3/17")
//        }
//    }
//
//    private var lastTime = 0L
//    override fun onTransmission(element: UploadElement) {
//        mTransferThreadExecutor.runInIO {
//            val state = element.state
//            val query = SafeBoxTransferHistoryKeeper.queryById(element.id)
//            if (state == TransferState.START) { //开始状态 完成状态为什么会进入到这个状态
//                if (query != null && System.currentTimeMillis() - lastTime > 1000) {
//                    lastTime = System.currentTimeMillis()
//                    query.length = element.length
//                    SafeBoxTransferHistoryKeeper.update(query)
//                }
//                var dataRefreshEvent = TransferRefreshEvent(TRANSFER_TRANSMISSION, element)
//                callBack?.get()?.onCallBack(dataRefreshEvent)
//            }
//        }
//    }

    private val mReentrantLock by lazy {
        ReentrantLock()
    }

    override fun getReentrantLock(): ReentrantLock {
        return mReentrantLock
    }

    override fun getSafeBoxTransferHistory(element: UploadElement): SafeBoxTransferHistory {
        return SafeBoxTransferHistory(null, null, SafeBoxTransferHistoryKeeper.getTransferType(isDownload),
                element.srcName, element.srcPath,
                element.devId, element.toPath, element.size,
                element.length, 0L, element.time, false, null)
    }

    override fun buildTaskRunnable(t: UploadElement, onTransferFileListener: OnTransferFileListener<UploadElement>): TaskRunnable<UploadElement> {
        L.i(t.tag, "buildTaskRunnable", "SafeBoxUploadManager", "nwq", "2021/5/15");
        return DownloadTaskRunnable(t, onTransferFileListener)
    }

    internal class DownloadTaskRunnable(uploadElement: UploadElement, private val onTransferFileListener: OnTransferFileListener<UploadElement>) : TaskRunnable<UploadElement>(uploadElement) {
        private var uploadFileAPI: OneOSUploadFileAPI? = null

        override fun run() {
            if (!isInterrupt()) {
                val loginSession = getLoginSession(transferElement.devId)
                if (loginSession == null) {
                    doUploadException()
                } else {
                    uploadFileAPI = OneOSUploadFileAPI(loginSession, transferElement)
                    uploadFileAPI?.setOnUploadFileListener(onTransferFileListener)
                    uploadFileAPI?.upload()
                }
            }
        }

        private fun doUploadException() {
            transferElement.state = TransferState.FAILED
            transferElement.exception = TransferException.FAILED_REQUEST_SERVER
            onComplete(transferElement)
        }

        override fun interrupt() {
            uploadFileAPI?.stopUpload()
            uploadFileAPI = null
        }
    }


    override fun convertData(history: SafeBoxTransferHistory): UploadElement? {
        if (history.length as Long == history.size) {
            history.isComplete = true
            SafeBoxTransferHistoryKeeper.update(history)
            return null
        } else {
            val file = File(history.srcPath)
            val element = UploadElement(file, history.toPath)
            if (FileUtils.isPictureFile(file.name)
                    || FileUtils.isVideoFile(file.name)
                    || FileUtils.isGifFile(file.name)) element.thumbUri = Uri.fromFile(file)
            element.length = history.length
            element.toDevId = history.srcDevId
            element.id = history.id
            val state = history.state
            if (file.exists()) {
                element.state = state
            } else {
                element.state = TransferState.FAILED
                element.exception = TransferException.FILE_NOT_FOUND
            }
            return element
        }
    }


}