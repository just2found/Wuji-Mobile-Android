package io.weline.libimageloader

import com.bumptech.glide.load.model.GlideUrl

/** 

Created by admin on 2020/9/16,11:18

 */
abstract class CacheKeyGlideUrl(private val url: String) : GlideUrl(url) {
    override fun getCacheKey(): String {
        return genCacheKey() ?: super.getCacheKey()
    }

    abstract fun genCacheKey(): String?
}