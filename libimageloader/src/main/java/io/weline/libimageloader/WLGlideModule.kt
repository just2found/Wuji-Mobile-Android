package io.weline.libimageloader

import android.content.Context
import com.bumptech.glide.Glide
import com.bumptech.glide.GlideBuilder
import com.bumptech.glide.Registry
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.integration.okhttp3.OkHttpUrlLoader
import com.bumptech.glide.load.engine.bitmap_recycle.LruBitmapPool
import com.bumptech.glide.load.engine.cache.InternalCacheDiskCacheFactory
import com.bumptech.glide.load.engine.cache.LruResourceCache
import com.bumptech.glide.load.engine.cache.MemorySizeCalculator
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.module.AppGlideModule
import okhttp3.ConnectionPool
import okhttp3.OkHttpClient
import java.io.InputStream
import java.util.concurrent.TimeUnit

@GlideModule
class WLGlideModule : AppGlideModule() {
    private val CPU_COUNT = Runtime.getRuntime().availableProcessors()

    // Disable manifest parsing to avoid adding similar modules twice.
    override fun isManifestParsingEnabled(): Boolean {
        return false
    }

    override fun applyOptions(context: Context, builder: GlideBuilder) {
        //自定义缓存目录
//        File cacheDir = context.getExternalCacheDir();
        //builder.setDiskCache(new DiskLruCacheFactory(cacheDir.getPath()+"/"+GlideCacheConfig.GLIDE_CACHE_DIR, GlideCacheConfig.GLIDE_CACHE_SIZE));
        val calculator = MemorySizeCalculator.Builder(context)
                .build()
        val defaultMemoryCacheSize = calculator.memoryCacheSize
        val defaultBitmapPoolSize = calculator.bitmapPoolSize
        val customMemoryCacheSize = (1.2 * defaultMemoryCacheSize).toInt()
        val customBitmapPoolSize = (1.2 * defaultBitmapPoolSize).toInt()
        builder.setMemoryCache(LruResourceCache(customMemoryCacheSize.toLong()))
        builder.setBitmapPool(LruBitmapPool(customBitmapPoolSize.toLong()))
        builder.setDiskCache(InternalCacheDiskCacheFactory(context, context.packageName,
                GlideCacheConfig.GLIDE_CACHE_SIZE))
    }

    override fun registerComponents(context: Context, glide: Glide,
                                    registry: Registry) {
        val okHttpClient = OkHttpClient.Builder()
                .connectionPool(ConnectionPool(CPU_COUNT, 3, TimeUnit.MINUTES))
                .connectTimeout(3,TimeUnit.SECONDS)
                .addInterceptor(ProgressInterceptor())
                .build()
        registry.replace(GlideUrl::class.java, InputStream::class.java,
                OkHttpUrlLoader.Factory(okHttpClient))
    }
}