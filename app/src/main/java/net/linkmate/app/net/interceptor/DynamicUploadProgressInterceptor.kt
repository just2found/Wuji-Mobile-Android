package net.linkmate.app.net.interceptor

import android.text.TextUtils
import android.util.Log
import net.linkmate.app.service.DynamicQueue
import net.linkmate.app.util.http.DynamicUploadRequestBody
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

/**
 * @author Raleigh.Luo
 * date：21/3/12 13
 * describe：
 */
class DynamicUploadProgressInterceptor : Interceptor {
    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response? {
        //获取原始的request
        var originalRequest = chain.request()
        val oldUrl = originalRequest.url()
        /*---------Header自定义参数---------------------------------------------*/
        //从request中获取headers,通过给定的domain域名,进行更改
        val isAddProgressListenerKey = "add-dynamic-upload-listener"
        val identificationKey = "identification"
        val isAddProgressListener = originalRequest.header(isAddProgressListenerKey)
        val identification = originalRequest.header(identificationKey)
        if (!TextUtils.isEmpty(isAddProgressListener) && isAddProgressListener == "true") {
            try {
                /**
                 * 注意：使用了上传进度监听，故不能使用HttpLoggingInterceptor 打印日志，HttpLoggingInterceptor中会writeTo一次requestbody，监听进度拦截器又会writeTo一次，重复导致写入翻倍异常
                 */
                originalRequest.body()?.let {
                    val builder = originalRequest.newBuilder()
                            .url(oldUrl)
                            .method(originalRequest.method(), DynamicUploadRequestBody(it, DynamicQueue.getUploadIdentification(chain.call()), identification))
                            //移除自定义的Header
                            .removeHeader(isAddProgressListenerKey)
                            .removeHeader(identificationKey)
                    originalRequest = builder.build()
                }
            } catch (e: Exception) {
            }
            return chain.proceed(originalRequest)
        } else {
            return chain.proceed(originalRequest)
        }
    }

}