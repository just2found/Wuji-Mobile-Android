package net.sdvn.nascommon.model.oneos.transfer_r

import net.sdvn.nascommon.db.TransferHistoryKeeper
import net.sdvn.nascommon.db.objecbox.TransferHistory
import net.sdvn.nascommon.model.oneos.OneOSFile
import net.sdvn.nascommon.model.oneos.api.file.OneOSDownloadFileAPI
import net.sdvn.nascommon.model.oneos.transfer.DownloadElement
import net.sdvn.nascommon.model.oneos.transfer.OnTransferFileListener
import net.sdvn.nascommon.model.oneos.transfer.TransferException
import net.sdvn.nascommon.model.oneos.transfer.TransferState
import net.sdvn.nascommon.model.oneos.transfer.thread.Priority
import net.sdvn.nascommon.model.oneos.transfer_r.thread.TaskRunnable
import net.sdvn.nascommon.utils.IOUtils
import net.sdvn.nascommon.utils.MediaScanner
import java.io.File
import java.util.concurrent.locks.ReentrantLock


/**
create by: 86136
create time: 2021/2/18 13:41
Function description:
 */
object DownloadManagerR : TransferManagerR<DownloadElement>(true) {

    override fun convertData(dbList: List<TransferHistory>): List<DownloadElement> {
        val list = mutableListOf<DownloadElement>()
        for (history in dbList) {
            val file = OneOSFile()
            file.setPath(history.srcPath)
            file.setName(history.name)
            file.setSize(history.size)
            val element = DownloadElement(file, history.toPath, history.length, history.tmpName)
            element.srcDevId = history.srcDevId
            element.state = history.state
            element.id = history.id
            element.priority = (Priority.DEFAULT - element.id).toInt()
            //  transferList.add(element);
            element.setGroup(history.groupId)
            list.add(element)
        }
        return list
    }

    override fun buildTaskRunnable(t: DownloadElement, onTransferFileListener: OnTransferFileListener<DownloadElement>): TaskRunnable<DownloadElement> {
        return DownloadTaskRunnable(t, onTransferFileListener)
    }


    //这是已经实在IO线程里面调用了
    override fun enqueue(element: DownloadElement): Int {
        var hasCode = FILE_IS_EXIST
        //保证所有的数据库操作都是再IO线程
        var query = TransferHistoryKeeper.query(TransferHistoryKeeper
                .getTransferType(isDownload), element.devId, element.srcPath, element.srcName, element.toPath)
        if (query != null) {
            if (query.size == element.size) {//对别文件大小一直
                if (!query.isComplete) {//未完成则以数据库的信息获得中间文件
                    val tmpFile = File(query.toPath, query.tmpName)
                    if (tmpFile.exists()) {//根据中间文件设置参数
                        element.offset = tmpFile.length()
                        element.length = tmpFile.length()
                        element.tmpName = query.tmpName
                    }
                } else {//是完成状态的话
                    val file = File(query.toPath, query.name)
                    if (file.exists()) {//文件存在则不再继续下载
                        return hasCode
                    } else {//找不到文件则从新开始下载
                        query.isComplete = false
                        IOUtils.delFileOrFolder(File(query.toPath, query.tmpName))
                        query.length = 0L

                    }
                }
            } else {
                TransferHistoryKeeper.delete(query)//如果数据不对则将数据清空
                query = null//这样是为了后面能插入新的
            }
        }

        if (query == null) {//未查到数据则插入数据
            query = TransferHistory(null, null, TransferHistoryKeeper.getTransferType(isDownload), element.srcName,
                    element.getSrcPath(), element.getDevId(), element.getToPath(), element.getSize(), element.getLength(), 0L,
                    System.currentTimeMillis(), false, element.getTmpName(),element.isCheck,element.groupId)
            element.id = TransferHistoryKeeper.insert(query)
        } else {
            query.isComplete = false
            query.length = element.getLength()
            query.size = element.getSize()
            element.id = query.id
            TransferHistoryKeeper.update(query)
        }
        hasCode = element.hashCode()
        wakeUpTask()
        notifyTransferReCount()
        return hasCode
    }


//    override fun onComplete(element: DownloadElement) {
//        mTransferThreadExecutor.runInIO {
//            val state = element.state
//            val query = TransferHistoryKeeper.query(TransferHistoryKeeper.getTransferType(isDownload),
//                    element.srcDevId, element.srcPath, element.srcName, element.toPath)
//            var dataRefreshEvent: TransferRefreshEvent<DownloadElement>? = null
//            if (state == TransferState.CANCELED) {//其实不用存
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
//                    if (element.toName != null) {
//                        query.name = element.toName!!
//                    }
//                    query.state = state
//                    query.isComplete = true
//                    query.length = element.length
//                    TransferHistoryKeeper.update(query)
//                }
//                MediaScanner.getInstance().scanningFile(element.toPath + File.separator + element.toName)//我现在不知道这一行是用来做什么的
//                notifyTransferReCount()
//                notifyComplete(element)
//                dataRefreshEvent = TransferRefreshEvent(TRANSFER_COMPLETE, element)
//                transferRefreshLiveData.postValue(dataRefreshEvent)
//            }
//            dataRefreshEvent?.let {
//                mTransferThreadExecutor.onlyRemove(element.tag)
//                wakeUpTask(false)
//                callBack?.get()?.onCallBack(it)
//            }
//        }
//    }
//
//    //这个用于通知传输数据改变
//    override fun onTransmission(element: DownloadElement) {
//        mTransferThreadExecutor.runInIO {
//            val state = element.state
//            val query = TransferHistoryKeeper.query(TransferHistoryKeeper.getTransferType(isDownload),
//                    element.devId, element.srcPath, element.srcName, element.toPath)
//            if (state == TransferState.START) { //开始状态 完成状态为什么会进入到这个状态
//                if (query == null) {
//                    val history = TransferHistory(null, null,
//                            TransferHistoryKeeper.getTransferType(isDownload), element.srcName,
//                            element.srcPath, element.srcDevId, element.toPath,
//                            element.size, element.length, 0L,
//                            System.currentTimeMillis(), false, element.tmpName)
//                    TransferHistoryKeeper.insert(history)
//                } else {
//                    query.length = element.length
//                    TransferHistoryKeeper.update(query)
//                }
//                val data = TransferRefreshEvent(TRANSFER_TRANSMISSION, element)
//                callBack?.get()?.onCallBack(data)
//            }
//        }
//    }

    override fun getTransferHistory(element: DownloadElement): TransferHistory {
        return TransferHistory(null, null, TransferHistoryKeeper.getTransferType(isDownload), element.srcName,
                element.srcPath, element.srcDevId, element.toPath, element.size, element.length, 0L,
                System.currentTimeMillis(), false, element.tmpName,element.isCheck,element.groupId)
    }


    internal class DownloadTaskRunnable(mDownloadElement: DownloadElement, private val onTransferFileListener: OnTransferFileListener<DownloadElement>) : TaskRunnable<DownloadElement>(mDownloadElement) {
        private var downloadFileAPI: OneOSDownloadFileAPI? = null

        override fun run() {
            if (!isInterrupt()) {
                val loginSession = getLoginSession(transferElement.devId)
                if (loginSession == null) {
                    doDownloadException()
                } else {
                    downloadFileAPI = OneOSDownloadFileAPI(loginSession, transferElement)
                    downloadFileAPI?.setOnDownloadFileListener(onTransferFileListener)
                    downloadFileAPI?.download()
                }
            }
        }

        private fun doDownloadException() {
            transferElement.state = TransferState.FAILED
            transferElement.exception = TransferException.FAILED_REQUEST_SERVER
            onComplete(transferElement)
        }

        override fun interrupt() {
            downloadFileAPI?.stopDownload()
            downloadFileAPI = null
        }
    }

    private val mReentrantLock by lazy {
        ReentrantLock()
    }

    override fun getReentrantLock(): ReentrantLock {
        return mReentrantLock
    }

    override fun convertData(history: TransferHistory): DownloadElement? {
        val file = OneOSFile()
        file.setPath(history.srcPath)
        file.setName(history.name)
        file.setSize(history.size)
        val element = DownloadElement(file, history.toPath, history.length, history.tmpName)
        element.srcDevId = history.srcDevId
        element.state = history.state
        element.id = history.id
        element.priority = (Priority.DEFAULT - element.id).toInt()
        element.setGroup(history.groupId)
        return element
    }
}




