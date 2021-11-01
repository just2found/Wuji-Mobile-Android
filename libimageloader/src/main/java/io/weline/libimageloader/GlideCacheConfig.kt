package io.weline.libimageloader

/**
 * Glide缓存配置文件
 */
object GlideCacheConfig {
    // 图片缓存最大容量，1000M，根据自己的需求进行修改
    const val GLIDE_CACHE_SIZE:Long = 1000 * 1000 * 1000

    // 图片缓存子目录
    const val GLIDE_CACHE_DIR = "glide"
}