package net.sdvn.nascommon.model.oneos.tansfer_safebox

import net.sdvn.nascommon.db.SafeBoxTransferHistoryKeeper
import net.sdvn.nascommon.db.objecbox.SafeBoxTransferHistory
import net.sdvn.nascommon.model.oneos.OneOSFile
import net.sdvn.nascommon.model.oneos.api.file.OneOSDownloadFileAPI
import net.sdvn.nascommon.model.oneos.transfer.DownloadElement
import net.sdvn.nascommon.model.oneos.transfer.OnTransferFileListener
import net.sdvn.nascommon.model.oneos.transfer.TransferException
import net.sdvn.nascommon.model.oneos.transfer.TransferState
import net.sdvn.nascommon.model.oneos.transfer.thread.Priority
import net.sdvn.nascommon.model.oneos.transfer_r.thread.TaskRunnable
import net.sdvn.nascommon.utils.IOUtils
import java.io.File
import java.util.concurrent.locks.ReentrantLock


/**
create by: 86136
create time: 2021/2/18 13:41
Function description:
 */
object SafeBoxDownloadManager : SafeBoxTransferManager<DownloadElement>(true) {

    override fun convertData(dbList: List<SafeBoxTransferHistory>): List<DownloadElement> {
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
        var query = SafeBoxTransferHistoryKeeper.query(SafeBoxTransferHistoryKeeper
                .getTransferType(isDownload), element.getDevId(), element.getSrcPath(), element.srcName, element.getToPath())
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
                SafeBoxTransferHistoryKeeper.delete(query)//如果数据不对则将数据清空
                query = null//这样是为了后面能插入新的
            }
        }

        if (query == null) {//未查到数据则插入数据
            query = SafeBoxTransferHistory(null, null, SafeBoxTransferHistoryKeeper.getTransferType(isDownload), element.srcName,
                    element.getSrcPath(), element.getDevId(), element.getToPath(), element.getSize(), element.getLength(), 0L,
                    System.currentTimeMillis(), false, element.getTmpName())
            element.id = SafeBoxTransferHistoryKeeper.insert(query)
        } else {
            query.isComplete = false
            query.length = element.getLength()
            query.size = element.getSize()
            element.id = query.id
            SafeBoxTransferHistoryKeeper.update(query)
        }
        hasCode = element.hashCode()
        wakeUpTask()
        notifyTransferReCount()
        return hasCode
    }


    override fun getSafeBoxTransferHistory(element: DownloadElement): SafeBoxTransferHistory {
        return SafeBoxTransferHistory(null, null, SafeBoxTransferHistoryKeeper.getTransferType(isDownload), element.srcName,
                element.srcPath, element.srcDevId, element.toPath, element.size, element.length, 0L,
                System.currentTimeMillis(), false, element.tmpName)
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

    override fun convertData(history: SafeBoxTransferHistory): DownloadElement? {
        val file = OneOSFile()
        file.setPath(history.srcPath)
        file.setName(history.name)
        file.setSize(history.size)
        val element = DownloadElement(file, history.toPath, history.length, history.tmpName)
        element.srcDevId = history.srcDevId
        element.state = history.state
        element.id = history.id
        element.priority = (Priority.DEFAULT - element.id).toInt()
        return element
    }
}




