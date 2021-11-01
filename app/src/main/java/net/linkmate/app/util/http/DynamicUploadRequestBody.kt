package net.linkmate.app.util.http

import android.util.Log
import net.linkmate.app.service.DynamicQueue
import net.linkmate.app.service.UploadStatus
import okhttp3.MediaType
import okhttp3.RequestBody
import okio.*
import java.io.IOException

/**动态上传进度监听
 * @author Raleigh.Luo
 * date：21/3/11 16
 * describe：
 * @param dynamicQueueType 动态消息类型，如DynamicQueue.UNLIKE_TYPE
 * @param autoIncreaseId 对应动态消息类型的本地数据库自增id
 * @param identification 同一请求体唯一标识
 * 发布动态使用DynamicQueue message作为标识，即PUBLISH_DYNAMIC_TYPE/autoIncreaseId，如publishDynamic/201
 */
class DynamicUploadRequestBody(private val requestBody: RequestBody,
                               private var progressListener: ProgressRequestListener? = null,
                               private val identification: String? = null) : RequestBody() {
    //总字节长度，避免多次调用contentLength()方法
    var contentLength = 0L

    interface ProgressRequestListener {
        fun isCanceled(): Boolean

        fun cancel()

        fun start(identification: String?, contentLength: Long)

        //progress= (100 * bytesWrite) / contentLength
        fun onRequestProgress(bytesWritten: Long, contentLength: Long, done: Boolean)

        fun getUploadIdentification(): DynamicQueue.UploadIdentification?

        fun clear()
    }


    init {
        contentLength = contentLength()
        progressListener?.start(identification, contentLength)
    }


    //包装完成的BufferedSink
    private var bufferedSink: BufferedSink? = null
    override fun contentType(): MediaType? {
        return requestBody.contentType()
    }

    override fun contentLength(): Long {
        return requestBody.contentLength()
    }

    /**
     *  重写进行写入
     */
    @Throws(IOException::class)
    override fun writeTo(sink: BufferedSink) {
        if (bufferedSink == null) {
            //包装
            val sk = sink(sink)
            bufferedSink = Okio.buffer(sk);
        }
        //写入
        requestBody.writeTo(bufferedSink)
        //必须调用flush，否则最后一部分数据可能不会被写入
        bufferedSink?.flush();
    }

    /**
     * 写入，回调进度接口
     * @param sink Sink
     * @return Sink
     */
    private fun sink(sink: Sink): Sink {
        return object : ForwardingSink(sink) {
            //当前写入字节数
            var bytesWritten = 0L

            @Throws(IOException::class)
            override fun write(source: Buffer, byteCount: Long) {
                if (contentLength == 0L) {
                    //获得contentLength的值，后续不再调用
                    contentLength = contentLength();
                }
                //增加当前写入的字节数
                bytesWritten += byteCount;

                if (progressListener?.isCanceled() ?: false) {
                    //已取消,移除监听器
                    progressListener = null
                } else {
                    progressListener?.onRequestProgress(bytesWritten, contentLength, bytesWritten == contentLength)
                }
                //回调

                super.write(source, byteCount)
            }

        }
    }
}