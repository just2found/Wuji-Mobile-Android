package net.sdvn.nascommon.model.phone

import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import net.sdvn.nascommon.model.oneos.DataFile
import net.sdvn.nascommon.utils.Utils
import java.io.File
import java.io.Serializable
import java.util.*

/**
 * Created by gaoyun@eli-tech.com on 2016/3/1.
 */
class LocalFile : Serializable, DataFile {
    var file: File? = null
        private set

    // for sticky header
    var section = 0

    // photo date time
    var date: Long = 0

    // video dur
    var duration: Long = 0
    var isPicOrVideo = false
    var pathUri: String? = null

    // weather is download directory
    var isDownloadDir = false

    // weather is backup directory
    var isBackupDir = false
    var lastModifyTime: Long? = null
    var isDir: Boolean? = null
    var isfile: Boolean? = null
    private val isProvider = false
    var uri: Uri? = null
        private set
    private var documentFile: DocumentFile? = null

    constructor(file: File) : this(file, 0)

    constructor(file: File, section: Int) {
        this.file = file
        this.section = section
        documentFile = DocumentFile.fromFile(file)
    }

    constructor(uri: Uri) {
        this.uri = uri
        val app = Utils.getApp()
        if (DocumentFile.isDocumentUri(app, uri)) {
            documentFile = DocumentFile.fromSingleUri(app, uri)
        }
    }

    constructor(pathUri: String) {
        this.pathUri = pathUri
        val app = Utils.getApp()
        val uri = Uri.parse(pathUri)
        if (DocumentFile.isDocumentUri(app, uri)) {
            documentFile = DocumentFile.fromSingleUri(app, uri)
        }
    }

    override fun getTime(): Long {
        return lastModified() / 1000
    }

    override fun getName(): String {
        return documentFile?.name ?: ""
    }

    override fun isDirectory(): Boolean {
        if (isfile != null) {
            return !isfile!!
        }
        if (isDir != null) {
            return isDir!!
        }
        return (documentFile?.isDirectory ?: false).also { isDir = it }
    }

    fun isFile(): Boolean {
        if (isfile != null) {
            return isfile!!
        }
        return (documentFile?.isFile ?: false).also { isfile = it }
    }

    fun isHidden(): Boolean {
        return documentFile?.name?.startsWith(".") ?: false
    }

    override fun getPath(): String {
        return documentFile?.uri?.toString() ?: ""
    }

    fun lastModified(): Long {
        if (lastModifyTime != null && lastModifyTime!! > 0) {
            return lastModifyTime!!
        }
        lastModifyTime = documentFile?.lastModified() ?: 0
        return lastModifyTime!!
    }

    fun length(): Long {
        return documentFile?.length() ?: 0
    }

    override fun equals(o: Any?): Boolean {
        if (this === o) return true
        if (o !is LocalFile) return false
        val localFile = o
        return section == localFile.section && date == localFile.date && isDownloadDir == localFile.isDownloadDir && isBackupDir == localFile.isBackupDir && isProvider == localFile.isProvider &&
                file == localFile.file
    }

    override fun hashCode(): Int {
        return Objects.hash(file, section, date, isDownloadDir, isBackupDir, isProvider)
    }

    fun exists(): Boolean {
        return documentFile?.exists() ?: false
    }

    override fun toString(): String {
        return if (file != null) file!!.absolutePath else uri.toString()
    }

    override fun getSize(): Long {
        return length()
    }

    override fun isPublicFile(): Boolean {
        return false
    }

    companion object {
        private const val serialVersionUID = 111181567L
    }
}