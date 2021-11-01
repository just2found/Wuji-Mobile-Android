package net.linkmate.app.ui.nas.transfer

import android.net.Uri
import com.chad.library.adapter.base.entity.MultiItemEntity
import net.linkmate.app.R
import net.sdvn.nascommon.db.objecbox.TransferHistory
import net.sdvn.nascommon.model.oneos.OneOSFile
import net.sdvn.nascommon.model.oneos.transfer.*
import net.sdvn.nascommon.model.oneos.transfer.thread.Priority
import net.sdvn.nascommon.model.oneos.transfer_r.interfaces.RepositoryData
import net.sdvn.nascommon.utils.FileUtils
import java.io.File
import java.util.*

class TransferEntity(var element: TransferElement) : MultiItemEntity, RepositoryData {

    companion object {
        fun convert(history: TransferHistory): TransferEntity {
            val element = if (history.isDownload) {
                val file = OneOSFile()
                file.setPath(history.srcPath)
                file.setName(history.name)
                file.setSize(history.size)
                val element = DownloadElement(file, history.toPath, history.length, history.tmpName)
                element.srcDevId = history.srcDevId
                element.state = if (history.isComplete) TransferState.COMPLETE else history.state
                element.id = history.id
                element.priority = (Priority.DEFAULT - element.id).toInt()
                element
            } else {
                val file = File(history.srcPath)
                val element = UploadElement(file, history.toPath)
                if (FileUtils.isPictureFile(file.name)
                        || FileUtils.isVideoFile(file.name)
                        || FileUtils.isGifFile(file.name)) element.thumbUri = Uri.fromFile(file)
                element.length = history.length
                element.toDevId = history.srcDevId
                element.id = history.id
                element.priority = (Priority.DEFAULT - element.id).toInt()
                if (history.isComplete) {
                    element.state = TransferState.COMPLETE
                } else {
                    element.state = history.state
                }
                element

            }
            element.toPath = history.toPath
            element.tmpName = history.tmpName

            return TransferEntity(element)
        }
    }

    var tmpName: String? = element.tmpName
    fun isComplete(): Boolean = element.state == TransferState.COMPLETE
    var exception: TransferException = element.exception
    var id: Long = element.id
    var fileName: String = element.srcName
    var fileSize: Long = element.size
    var toPath: String? = element.toPath
    var devId: String? = element.devId
    var srcPath: String? = element.srcPath
    var isDownload: Boolean = element.isDownload
    var tag: String = element.tag
    fun state(): TransferState = element.state
//    var isRunning: Boolean = state.ordinal < TransferState.COMPLETE.ordinal

    fun offset(): Long = element.length

    fun subscribe(observer: TransferElement.TransferStateObserver) {
        element.apply {
            if (!isComplete())
                addTransferStateObserver(observer)
        }
    }

    fun dispose(observer: TransferElement.TransferStateObserver) {
        if (observer != null) {
            element.removeTransferStateObserver(observer)
        }
    }

    fun getManager(): TransmissionManager<*> {
        return if (isDownload)
            DownloadManager.getInstance()
        else
            UploadManager.getInstance()
    }

    fun getSpeed(): Long {
        if (element.state == TransferState.START) {
            return (element).speed
        }
        return 0
    }

    override fun getItemType(): Int {
        return  R.layout.item_transfer
    }

    override fun isSameData(repositoryData: RepositoryData?): Boolean {
        if (repositoryData == null)
            return false
        if (this === repositoryData) {
            return true
        }
        if (repositoryData is TransferEntity) {
            return this.tag == repositoryData.tag
        }

        return false
    }

    override fun equalByTag(tag: Any): Boolean {
        if (tag is String) {
            return this.tag == tag
        }
        return false
    }


    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is TransferEntity) return false
        return (Objects.equals(tag, other.tag))
    }

    override fun hashCode(): Int {
        return tag.hashCode()
    }

    fun isStart(): Boolean {
        if (isComplete()) return false
        return (state() == TransferState.WAIT || state() == TransferState.START)
    }
}
