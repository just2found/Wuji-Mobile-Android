package net.sdvn.nascommon.model.oneos.transfer_r

import android.net.Uri
import net.sdvn.nascommon.db.TransferHistoryKeeper
import net.sdvn.nascommon.db.objecbox.TransferHistory
import net.sdvn.nascommon.model.oneos.api.file.OneOSUploadFileAPI
import net.sdvn.nascommon.model.oneos.transfer.OnTransferFileListener
import net.sdvn.nascommon.model.oneos.transfer.TransferException
import net.sdvn.nascommon.model.oneos.transfer.TransferState
import net.sdvn.nascommon.model.oneos.transfer.UploadElement
import net.sdvn.nascommon.model.oneos.transfer_r.thread.TaskRunnable
import net.sdvn.nascommon.utils.FileUtils
import java.io.File
import java.util.concurrent.locks.ReentrantLock

/**
create by: 86136
create time: 2021/3/12 10:35
Function description:
 */

object UploadManagerR : TransferManagerR<UploadElement>(false) {

    override fun convertData(dbList: List<TransferHistory>): List<UploadElement> {
        val list = mutableListOf<UploadElement>()
        for (history in dbList) {
            val element = convertData(history)
            element?.let { list.add(it) }
        }
        return list
    }

    override fun enqueue(element: UploadElement): Int {
        var hashCode = FILE_IS_EXIST
        var query = TransferHistoryKeeper.query(TransferHistoryKeeper.getTransferType(isDownload), element.getDevId(), element.getSrcPath(), element.srcName, element.getToPath())
        if (query != null) {
            if (!query.isComplete) { //未完成则更新进度
                element.length = query.length
                element.id = query.id
            } else {   //如果任务已经完成则则见返回-1
                element.isCheck = true

                return hashCode
            }
        } else {
            query = TransferHistory(null, null, TransferHistoryKeeper.getTransferType(isDownload),
                    element.srcName, element.getSrcPath(),
                    element.getDevId(), element.getToPath(), element.getSize(),
                    element.getLength(), 0L, element.getTime(), false, null,element.isCheck,element.groupId)
            element.id = TransferHistoryKeeper.insert(query)
        }
        hashCode = element.hashCode()
        wakeUpTask()
        notifyTransferReCount()
        return hashCode
    }


//    override fun onComplete(element: UploadElement) {
//        mTransferThreadExecutor.runInIO {
//            val state = element.state
//            val query = TransferHistoryKeeper.queryById(element.id)
//            var dataRefreshEvent: TransferRefreshEvent<UploadElement>? = null
//            if (state == TransferState.CANCELED) {
//                TransferHistoryKeeper.delete(query)
//                notifyTransferReCount()
//                dataRefreshEvent = TransferRefreshEvent(TRANSFER_CANCELED, element)
//            } else if (state == TransferState.PAUSE) {
//                if (query != null) {
//                    query.length = element.length
//                    query.state = state
//                    TransferHistoryKeeper.update(query)
//                }
//                dataRefreshEvent = TransferRefreshEvent(TRANSFER_PAUSE, element)
//            } else if (state == TransferState.FAILED) {
//                if (query != null) {
//                    query.length = element.length
//                    query.state = state
//                    TransferHistoryKeeper.update(query)
//                }
//                dataRefreshEvent = TransferRefreshEvent(TRANSFER_FAIL, element)
//            } else if (state == TransferState.COMPLETE) {
//                if (query != null) {
//                    query.time = System.currentTimeMillis()
//                    query.isComplete = true
//                    query.length = element.length
//                    query.state = state
//                    TransferHistoryKeeper.update(query)
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
//            val query = TransferHistoryKeeper.queryById(element.id)
//            if (state == TransferState.START) { //开始状态 完成状态为什么会进入到这个状态
//                if (query != null && System.currentTimeMillis() - lastTime > 1000) {
//                    lastTime = System.currentTimeMillis()
//                    query.length = element.length
//                    TransferHistoryKeeper.update(query)
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

    override fun getTransferHistory(element: UploadElement): TransferHistory {
        return TransferHistory(null, null, TransferHistoryKeeper.getTransferType(isDownload),
                element.srcName, element.srcPath,
                element.devId, element.toPath, element.size,
                element.length, 0L, element.time, false, null
        ,element.isCheck,element.groupId)
    }

    override fun buildTaskRunnable(t: UploadElement, onTransferFileListener: OnTransferFileListener<UploadElement>): TaskRunnable<UploadElement> {
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


    override fun convertData(history: TransferHistory): UploadElement? {
        if (history.length as Long == history.size) {
            history.isComplete = true
            history.state = TransferState.COMPLETE
            TransferHistoryKeeper.update(history)

            val file = File(history.srcPath)
            val element = UploadElement(file, history.toPath)
            if (FileUtils.isPictureFile(file.name)
                    || FileUtils.isVideoFile(file.name)
                    || FileUtils.isGifFile(file.name)) element.thumbUri = Uri.fromFile(file)
            element.length = history.length
            element.toDevId = history.srcDevId
            element.id = history.id
            element.isCheck = history.check
            element.setGroup(history.groupId)
            addTransferHistory(history)
            onComplete(element)

            return null
        }
        val file = File(history.srcPath)
        val element = UploadElement(file, history.toPath)
        if (FileUtils.isPictureFile(file.name)
                || FileUtils.isVideoFile(file.name)
                || FileUtils.isGifFile(file.name)) element.thumbUri = Uri.fromFile(file)
        element.length = history.length
        element.toDevId = history.srcDevId
        element.id = history.id
        element.isCheck = history.check
        element.setGroup(history.groupId)
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