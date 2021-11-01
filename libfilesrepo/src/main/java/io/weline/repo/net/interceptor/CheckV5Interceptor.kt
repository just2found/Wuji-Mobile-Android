package io.weline.repo.net.interceptor

import android.text.TextUtils
import io.weline.repo.SessionCache
import okhttp3.Interceptor
import okhttp3.Response

/** 检查是不是V5
 * @author Raleigh.Luo
 * date：20/9/18 14
 * describe：
 */
class CheckV5Interceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        //获取原始的request
        val originalRequest = chain.request()
        val oldUrl = originalRequest.url()
        val builder = originalRequest.newBuilder()
        //从request中获取headers,通过给定的domain域名,进行更改
        val token = originalRequest.header("token")
        val ip = originalRequest.header("nas_domain")
        val deviceId = originalRequest.header("device_id")
        if (!TextUtils.isEmpty(deviceId) && !TextUtils.isEmpty(ip) && !TextUtils.isEmpty(token)) {
            //非v5，走旧接口，取消本次请求
            if(!SessionCache.instance.isV5OrSynchRequest(deviceId!!,ip!!)){
                //取消本次请求，不是V5,调用旧接口
                chain.call().cancel()
            }
            return chain.proceed(originalRequest)
        } else {
            return chain.proceed(originalRequest)
        }
    }
}