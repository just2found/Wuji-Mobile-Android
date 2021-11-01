package net.linkmate.app.data.model.dynamic

import android.text.TextUtils
import android.util.Log
import androidx.annotation.Keep
import com.bumptech.glide.load.model.GlideUrl
import io.weline.repo.files.constant.AppConstants
import net.linkmate.app.data.model.Base
import net.linkmate.app.net.RetrofitSingleton
import net.linkmate.app.util.TimeUtil
import net.sdvn.common.vo.*
import java.io.File

/** 动态列表
 * @author Raleigh.Luo
 * date：20/12/25 11
 * describe：
 */
@Keep
data class DynamicList(var data: List<Dynamic>?) : Base()

@Keep
data class DynamicDetail(var data: Dynamic?) : Base()

@Keep
data class DynamicLikeDetail(var data: DynamicLike?) : Base()

@Keep
data class DynamicCommentDetail(var data: DynamicComment?) : Base()

fun Dynamic.getRecentlyTime(): String {
    return TimeUtil.getDetailTime((CreateAt ?: 0L) * 1000)
}

fun Dynamic.getDetailTime(): String {
    return TimeUtil.getDetailTime((CreateAt ?: 0L) * 1000)
}

fun DynamicComment.getDetailTime(): String {
    return TimeUtil.getDetailTime((createAt ?: 0L) * 1000)
}

fun DynamicLike.getDetailTime(): String {
    return TimeUtil.getDetailTime((updateAt ?: 0L) * 1000)
}

fun DynamicRelated.getDetailTime(): String {
    return TimeUtil.getDetailTime((createAt ?: 0L) * 1000)
}

fun DynamicAttachment.getDownloadUrl(deviceId: String, ip: String): Any? {
    if (id == -1L) {
        return url
    } else {
        return RetrofitSingleton.instance.getDynamicGlideUrl(deviceId, getDownloadUrlKey(deviceId, ip),getDownloadUrlPath(deviceId, ip))
    }
}

/**
 * 完整url
 */
fun DynamicAttachment.getDownloadUrlPath(deviceId: String, ip: String): String {
    if (id == -1L || (!TextUtils.isEmpty(localPath) && File(localPath).exists())) {//本地数据
        return localPath ?: ""
    } else {
        return String.format("http://%s:%s/api/v1/file?fname=%s", ip, AppConstants.HS_DYNAMIC_PORT, url)
    }
}
/**
 * 用于做Tag
 */
fun DynamicAttachment.getDownloadUrlKey(deviceId: String, ip: String): String {
    if (id == -1L || (!TextUtils.isEmpty(localPath) && File(localPath).exists())) {//本地数据
        return localPath ?: ""
    } else {//deviceid＋端口＋文件名唯一标识，无ip
        return String.format("http://%s:%s/api/v1/file?fname=%s",deviceId,AppConstants.HS_DYNAMIC_PORT,url)
    }
}

/**
 * 原图
 */
fun DynamicMedia.getDownloadUrl(deviceId: String, ip: String): Any? {
    if (isLocalPath()) {
        return localPath
    } else {
        return RetrofitSingleton.instance.getDynamicGlideUrl(deviceId, getDownloadUrlKey(deviceId, ip),getDownloadUrlPath(deviceId, ip))
    }
}

/**
 * 缩略图
 */
fun DynamicMedia.getDownloadThumbnailUrl(deviceId: String, ip: String): Any? {
    if (isLocalPath()) {
        return localPath
    } else {
        return RetrofitSingleton.instance.getDynamicGlideUrl(deviceId, getDownloadThumbnailUrlKey(deviceId, ip),getDownloadThumbnailUrlPath(deviceId, ip))
    }
}

/**
 * 用于做Tag
 */
fun DynamicMedia.getDownloadUrlKey(deviceId: String, ip: String): String {
    if (isLocalPath()) {//本地数据
        return localPath ?: ""
    } else {//deviceid＋端口＋文件名唯一标识，无ip
        return String.format("http://%s:%s/api/v1/file?fname=%s",deviceId,AppConstants.HS_DYNAMIC_PORT,url)
    }
}

/**
 * 完整url
 */
fun DynamicMedia.getDownloadUrlPath(deviceId: String, ip: String): String {
    if (isLocalPath()) {//本地数据
        return localPath ?: ""
    } else {
        return String.format("http://%s:%s/api/v1/file?fname=%s", ip, AppConstants.HS_DYNAMIC_PORT, url)
    }
}

/**
 * 完整地址
 */
private fun DynamicMedia.getDownloadThumbnailUrlPath(deviceId: String, ip: String): String {
    if (isLocalPath()) {//本地数据
        return localPath ?: ""
    } else {
        //ftype ==0 默认，表示原图 =1表示缩略图
        return String.format("http://%s:%s/api/v1/file?fname=%s&ftype=1", ip, AppConstants.HS_DYNAMIC_PORT, url)
    }
}
/**
 * 用于做Tag 缩略图
 */
fun DynamicMedia.getDownloadThumbnailUrlKey(deviceId: String, ip: String): String {
    if (isLocalPath()) {//本地数据
        return localPath ?: ""
    } else {//deviceid＋端口＋文件名唯一标识，无ip
        return String.format("http://%s:%s/api/v1/file?fname=%s",deviceId,AppConstants.HS_DYNAMIC_PORT,url)
    }
}
/**
 * 用于做Tag 缩略图
 */
fun DynamicMedia.isLocalPath(): Boolean {
    if (id == -1L || (!TextUtils.isEmpty(localPath) && File(localPath).exists())) {//本地数据
        return true
    } else {
        return false
    }
}

