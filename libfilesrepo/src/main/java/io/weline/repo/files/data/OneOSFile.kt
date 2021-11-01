package io.weline.repo.files.data

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import io.weline.repo.files.constant.OneOSAPIs
import libs.source.common.utils.MIMETypeUtils
import java.io.File
import java.io.Serializable
import java.util.*

/**
 * Created by gaoyun@eli-tech.com on 2016/1/14.
 */
@Keep
class OneOSFile : Serializable, DataFile {
    // for sticky header
    var section = 0

    // {"perm":"rwxr-xr-x","type":"audio","toPath":"haizeiw .mp3","gid":0,"path":"\/haizeiw .mp3","uid":1001,"time":1187168313,"size":6137050}
    private var path: String? = null
    var perm: String? = null

    /**
     * File Type
     *
     *
     * Server file type table:
     *
     *
     * { directory="dir",jpg="pic",gif="pic",png="pic",jpeg="pic",bmp="pic",
     *
     *
     * mp3="audio",ogg="audio",wav="audio",flac="audio", wma="audio",m4a="audio",
     *
     *
     * avi="video",mp4="video",flv="video",rmvb="video",mkv="video",mov="video",
     *
     *
     * wmv="video", mpg="video",doc="doc",xls="doc",ppt="doc",docx="doc",xlsx="doc",
     *
     *
     * pptx="doc",pdf="doc",txt="doc",csv="doc",tea="enc" }
     */

    @SerializedName(value = "type", alternate = ["ftype"])
    var type: String? = null

    private var name: String = ""
    var gid = 0
    var uid = 0
    private var time: Long = 0
    private var size: Long = 0
    var month: Long = 0
    var progress = 0
    private var encrypt = 0
    var phototime: Long = 0
    var udtime: Long = 0
    var cttime: Long = 0
    var fullpath: String? = null
    var localFile: File? = null
    var fmtUDTime: String? = null
    var fmtCTTime: String? = null
    var files: Long = 0
    var dirs: Long = 0

    @field:SerializedName("share_path_type")
    var share_path_type = -1
    var md5: String? = null

    @field:SerializedName("user_tags")
    var userTags: List<FileTag>? = null

    /**
     * file shown icon
     */
    var icon: Int = 0

    /**
     * formatted file time
     */
    var fmtTime: String? = null

    /**
     * formatted file size
     */
    var fmtSize: String? = null
    //    protected OneOSFile(Parcel in) {
    //        section = in.readInt();
    //        path = in.readString();
    //        perm = in.readString();
    //        type = in.readString();
    //        name = in.readString();
    //        gid = in.readInt();
    //        uid = in.readInt();
    //        time = in.readLong();
    //        size = in.readLong();
    //        month = in.readLong();
    //        actProgress = in.readInt();
    //        encrypt = in.readInt();
    //        phototime = in.readLong();
    //        udtime = in.readLong();
    //        cttime = in.readLong();
    //        fullpath = in.readString();
    //        fmtUDTime = in.readString();
    //        fmtCTTime = in.readString();
    //        icon = in.readInt();
    //        fmtTime = in.readString();
    //        fmtSize = in.readString();
    //    }
    /**
     * OneOS File absolute path
     *
     * @param user user name
     * @return private file: [/home/user/path], public file: [path]
     */
    fun getAbsolutePath(user: String): String? {
        return if (isPublicFile()) {
            path
        } else {
            "home/$user$path"
        }
    }

    fun isOwner(uid: Int): Boolean {
        return this.uid == uid
    }

    val isGroupRead: Boolean
        get() = if (perm == null || perm!!.length != 9) {
            false
        } else perm!![3] == 'r'

    val isGroupWrite: Boolean
        get() = if (perm == null || perm!!.length != 9) {
            false
        } else perm!![4] == 'w'

    val isOtherRead: Boolean
        get() = if (perm == null || perm!!.length != 9) {
            false
        } else perm!![6] == 'r'

    val isOtherWrite: Boolean
        get() = if (perm == null || perm!!.length != 9) {
            false
        } else perm!![7] == 'w'

    override fun getPath(): String {
        return path!!
    }

    fun setPath(path: String?) {
        this.path = path
    }

    override fun getName(): String {
        return name
    }

    fun setName(name: String) {
        this.name = name
    }

    override fun getTime(): Long {
        return time
    }

    fun setTime(time: Long) {
        this.time = time
    }

    override fun getSize(): Long {
        return size
    }

    fun setSize(size: Long) {
        this.size = size
    }

    fun setEncrypt(encr: Int) {
        encrypt = encr
    }

    val isEncr: Boolean
        get() = encrypt == 1

    val isPicture: Boolean
        get() {
            if (null != type && type.equals("pic", ignoreCase = true)) {
                return true
            }
            return if (MIMETypeUtils.isImageFile(name)) {
                type = "pic"
                true
            } else {
                false
            }
        }

    val isVideo: Boolean
        get() {
            if (null != type && type.equals("video", ignoreCase = true)) {
                return true
            }
            return if (MIMETypeUtils.isVideoFile(name)) {
                type = "video"
                true
            } else {
                false
            }
        }

    val isAudio: Boolean
        get() {
            if (null != type && type.equals("audio", ignoreCase = true)) {
                return true
            }
            return if (MIMETypeUtils.isAudioFile(name)) {
                type = "audio"
                true
            } else {
                false
            }
        }

    val isGif: Boolean
        get() = name.endsWith(".gif", true)

    val isDocument: Boolean
        get() = null != type && type.equals("doc", ignoreCase = true)

    val isExtract: Boolean
        get() {
            val toLowerCase = name.toLowerCase(Locale.ENGLISH)
            return (toLowerCase.endsWith(".zip")
                    || toLowerCase.endsWith(".rar")
                    || toLowerCase.endsWith(".tgz")
                    || toLowerCase.endsWith(".nz2")
                    || toLowerCase.endsWith(".bz")
                    || toLowerCase.endsWith(".gz")
                    || toLowerCase.endsWith(".7z")
                    || toLowerCase.endsWith(".jar"))
        }

    fun isEncrypt(): Boolean {
        return null != type && type.equals("enc", ignoreCase = true)
    }

    override fun isDirectory(): Boolean {
        return null != type && type.equals("dir", ignoreCase = true)
    }

    override fun isPublicFile(): Boolean {
        return path?.startsWith(OneOSAPIs.ONE_OS_PUBLIC_ROOT_DIR) == true
    }

    override fun equals(other: Any?): Boolean {
        if (other == null) return false
        if (this === other) {
            return true
        }
        if (other is OneOSFile) {
            val file = other
            return if (path == null || file.path == null) false else path == file.path
        }
        return false
    }

    override fun toString(): String {
        return ("OneOSFile:{name:\"" + name + "\", path:\"" + path + "\", uid:\"" + uid + "\", type:\"" + type
                + "\", size:\"" + fmtSize + "\", time:\"" + fmtTime + "\", perm:\"" + perm + "\", gid:\"" + gid +
                "\", month:\"" + month + "\", cttime:\"" + cttime + "\", udtime:\"" + udtime + "\"}")
    }

    fun hasLocalFile(): Boolean {
        return localFile != null && localFile!!.exists()
    }

    override fun hashCode(): Int {
        return path?.hashCode() ?: 0
    }


    companion object {
        private const val serialVersionUID = 11181567L
    }
}

//class FileTag(
//        @field:SerializedName("id")
//        val id: Long,
//        @field:SerializedName("uid")
//        val uid: Int,
//        @field:SerializedName("name")
//        var name: String,
//        @field:SerializedName("color")
//        var color: Int) {
//}