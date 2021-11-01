package io.weline.libimageloader

import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.util.Log
import com.bumptech.glide.load.engine.GlideException
import okhttp3.MediaType
import okhttp3.ResponseBody
import okio.*
import java.io.IOException

class ProgressResponseBody(private val tag: Any, private val responseBody: ResponseBody,
                           private val progressListener: OnProgressListener?) : ResponseBody() {
    private val mMainThreadHandler = Handler(Looper.getMainLooper())
    private var bufferedSource: BufferedSource? = null
    override fun contentType(): MediaType? {
        return responseBody.contentType()
    }

    override fun contentLength(): Long {
        return responseBody.contentLength()
    }

    override fun source(): BufferedSource {
        if (bufferedSource == null) {
            bufferedSource = Okio.buffer(source(responseBody.source(), contentLength()))
        }
        return bufferedSource!!
    }

    private fun source(source: Source, contentLength: Long): Source {
        return object : ForwardingSource(source) {
            var totalBytesRead: Long = 0
            var refreshTime: Long = 300
            var lastRefreshTime: Long = 0
            var lastReadBytes: Long = 0

            @Throws(IOException::class)
            override fun read(sink: Buffer, byteCount: Long): Long {
                val bytesRead = super.read(sink, byteCount)
                totalBytesRead += if (bytesRead == -1L) 0 else bytesRead
                Log.d("progressResponse", "imageUrl=${tag} , totalBytesRead=$totalBytesRead");
                val currentTimeMillis = SystemClock.uptimeMillis()
                if ((currentTimeMillis - lastRefreshTime > refreshTime || totalBytesRead - lastReadBytes > 4 * 1024 || bytesRead == -1L)) {
                    lastRefreshTime = currentTimeMillis
                    lastReadBytes = totalBytesRead
                    mainThreadCallback(totalBytesRead, contentLength,
                            bytesRead == -1L, null)
                }
                return bytesRead
            }
        }
    }

    private fun mainThreadCallback(bytesRead: Long, totalBytes: Long, isDone: Boolean, exception: GlideException?) {
        mMainThreadHandler.post(Runnable { //final int percent = (int) ((bytesRead * 1.0f / totalBytes) * 100.0f);
            progressListener?.onProgress(tag, bytesRead, totalBytes, isDone, exception)
        })
    }

}