package net.sdvn.nascommon.model.glide

import io.weline.libimageloader.BuildConfig
import io.weline.libimageloader.CacheKeyGlideUrl
import timber.log.Timber

class WithDefaultKeyGlideUrl(val url: String, var key: String) : CacheKeyGlideUrl(url) {
    init {
        if (BuildConfig.DEBUG) {
            Timber.d("url: $url \n $key")
        }
    }

    override fun genCacheKey(): String? {
        return key
    }
}