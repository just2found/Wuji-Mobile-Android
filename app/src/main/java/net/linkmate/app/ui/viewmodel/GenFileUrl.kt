package net.linkmate.app.ui.viewmodel

import io.weline.libimageloader.CacheKeyGlideUrl
import io.weline.repo.data.model.IconSize
import net.sdvn.nascommon.SessionManager
import net.sdvn.nascommon.constant.OneOSAPIs
import net.sdvn.nascommon.model.glide.EliCacheGlideUrl
import net.sdvn.nascommon.model.glide.WithDefaultKeyGlideUrl
import net.sdvn.nascommon.model.oneos.DataFile
import net.sdvn.nascommon.model.oneos.OneOSFile
import net.sdvn.nascommon.model.oneos.user.LoginSession
import okhttp3.HttpUrl

/** 

Created by admin on 2020/11/6,18:20

 */
object GenFileUrl {

    fun genCacheGlideUrl(prefix: String?, devId: String, type: Int,
                         path: String, session: String? = null,
                         ip: String? = null): CacheKeyGlideUrl? {
        val host = if (ip.isNullOrEmpty()) "Placeholder" else ip
        val suffix = String.format("/file/download?share_path_type=%s&path=%s", type, path)

        val url = HttpUrl.Builder().apply {
            scheme(OneOSAPIs.SCHME_HTTP)
            host(host)
            port(9898)
            addPathSegments("file/download")
            if (!session.isNullOrEmpty()) {
                addQueryParameter("session", session)
            }
            addQueryParameter("share_path_type", type.toString())
            addQueryParameter("path", path)
        }.build().toString()

        return WithDefaultKeyGlideUrl(url, "${prefix}_${devId}_${suffix}")
    }

    fun genCacheGlideUrlTb(prefix: String?, devId: String, type: Int,
                           path: String, session: String? = null,
                           ip: String? = null, iconSize: IconSize? = null): CacheKeyGlideUrl? {
        val host = if (ip.isNullOrEmpty()) "Placeholder" else ip
        val suffix = String.format("/file/thumbnail?share_path_type=%s&path=%s", type,
                path)
        val url = HttpUrl.Builder().apply {
            scheme(OneOSAPIs.SCHME_HTTP)
            host(host)
            port(9898)
            addPathSegments("file/thumbnail")
            if (!session.isNullOrEmpty()) {
                addQueryParameter("session", session)
            }
            if (iconSize != null) {
                addQueryParameter("size", iconSize.getSize())
            }
            addQueryParameter("share_path_type", type.toString())
            addQueryParameter("path", path)
        }.build().toString()


        return WithDefaultKeyGlideUrl(url, "${prefix}_${devId}_${suffix}")
    }

    fun genEliCacheTB(loginSession: LoginSession, file: OneOSFile): EliCacheGlideUrl? {
        val url = OneOSAPIs.genThumbnailUrl(loginSession, file)
        return EliCacheGlideUrl(url)
    }

    fun genEliCache(loginSession: LoginSession, file: OneOSFile): EliCacheGlideUrl? {
        val url = OneOSAPIs.genOpenUrl(loginSession, file)
        return EliCacheGlideUrl(url)
    }

    /**
     *获取图片缩略图url
     * */
    fun getGlideModeTb(devId: String, pathType: Int, path: String, size: String? = null,groupId:Long?=null): CacheKeyGlideUrl? {
        val loginSession = SessionManager.getInstance().getLoginSession(devId) ?: return null
        val genThumbnailUrl = OneOSAPIs.genThumbnailUrl(loginSession, pathType, path,size,groupId)
        return EliCacheGlideUrl(genThumbnailUrl)
    }

    fun getGlideMode(devId: String, pathType: Int, path: String,groupId:Long?=null): EliCacheGlideUrl? {
        val loginSession = SessionManager.getInstance().getLoginSession(devId) ?: return null
        val url = OneOSAPIs.genDownloadUrl(loginSession, pathType, path,groupId)
        return EliCacheGlideUrl(url)
    }

    fun genFileCacheKey(devId: String, dataFile: DataFile): String {
        val md5 = dataFile.getMD5()
        if (!md5.isNullOrEmpty()) {
            return md5
        }
        val prefix = "${dataFile.getSize()}${dataFile.getTag()}"
        val suffix = String.format("share_path_type=%s&path=%s", dataFile.getPathType(), dataFile.getPath())
        return "${devId}_${prefix}_${suffix}"
    }
}