package io.weline.libimageloader

import com.bumptech.glide.load.engine.GlideException

interface OnProgressListener {
    fun onProgress(tag: Any, bytesRead: Long, totalBytes: Long, isDone: Boolean, exception: GlideException?)
}