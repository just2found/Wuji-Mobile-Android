package net.linkmate.app.ui.nas.transfer_r

import android.net.Uri
import com.chad.library.adapter.base.entity.MultiItemEntity
import net.linkmate.app.ui.nas.transfer.TransferEntity
import net.sdvn.nascommon.db.objecbox.TransferHistory
import net.sdvn.nascommon.model.oneos.OneOSFile
import net.sdvn.nascommon.model.oneos.transfer.DownloadElement
import net.sdvn.nascommon.model.oneos.transfer.TransferException
import net.sdvn.nascommon.model.oneos.transfer.TransferState
import net.sdvn.nascommon.model.oneos.transfer.UploadElement
import net.sdvn.nascommon.model.oneos.transfer.thread.Priority
import net.sdvn.nascommon.model.oneos.transfer_r.interfaces.RepositoryData
import net.sdvn.nascommon.utils.FileUtils
import java.io.File
import java.util.*

class TransferEntityR(history: TransferHistory) : MultiItemEntity, RepositoryData {

    var tmpName: String?
    var isComplete: Boolean
    var exception: TransferException
    var id: Long
    var fileName: String
    var fileSize: Long
    var toPath: String?
    var devId: String?
    var srcPath: String?
    var isDownload: Boolean
    var tag: String
    var state: TransferState
    var offset: Long
    var speed: Long
    var lastTime: Long = 0L //用于记录上次刷新时间

    init {
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
            element.setGroup(history.groupId)
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
            element.setGroup(history.groupId)
            element

        }
        element.toPath = history.toPath
        element.tmpName = history.tmpName

        //TODO 严格来说 history应该直接转换过来，不需要Element进行转换 最大化的二个数据类型解耦
        tmpName = element.tmpName
        isComplete = element.state == TransferState.COMPLETE
        exception = element.exception
        id = element.id
        fileName = element.srcName
        fileSize = element.size
        toPath = element.toPath
        devId = element.devId
        srcPath = element.srcPath
        isDownload = element.isDownload
        tag = element.tag
        state = element.state
        offset = element.length
        speed = element.speed
    }


    override fun getItemType(): Int {
        return if (isComplete) {
            QuickTransmissionAdapterR.COMPLETED_ITEM
        } else {
            QuickTransmissionAdapterR.PROGRESS_ITEM
        }
    }

    override fun isSameData(other: RepositoryData?): Boolean {
        if (this === other) return true
        if (other !is TransferEntity) return false
        return (Objects.equals(tag, other.tag))
    }

    override fun equalByTag(tag: Any): Boolean {
        if (tag is String) {
            return this.tag == tag
        }
        return false
    }


}
