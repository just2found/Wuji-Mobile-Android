package io.weline.repo.net.interceptor

import android.text.TextUtils
import io.weline.repo.files.constant.AppConstants
import okhttp3.HttpUrl
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

/**
 * @author Raleigh.Luo
 * date：20/4/15 23
 * describe：
 */
class ChangeUrlInterceptor : Interceptor {
    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        //获取原始的request
        var originalRequest = chain.request()
        val oldUrl = originalRequest.url()
        val builder = originalRequest.newBuilder()
        //从request中获取headers,通过给定的domain域名,进行更改
        val nas_domain = originalRequest.header("nas_domain")
        val domain = originalRequest.header("domain")
        if (!TextUtils.isEmpty(domain)) {//普通替换域名，直接替换
            builder.removeHeader("domain")
            try {
                var newHttpUrl = builder.url(HttpUrl.parse(domain)).build().url()
                //重建request
                newHttpUrl = oldUrl.newBuilder()
                        .scheme(newHttpUrl!!.scheme())
                        .host(newHttpUrl!!.host())
                        .port(newHttpUrl!!.port())
                        .build()
                originalRequest = builder.url(newHttpUrl).build()
            } catch (e: Exception) {
                throw IOException("domain is empty")
            }
            return chain.proceed(originalRequest)
        } else if (!TextUtils.isEmpty(nas_domain)) {//nas 设备替换域名
            try {
                builder.removeHeader("nas_domain")
                //完整的url
                val url = String.format("http://%s:%s", nas_domain, AppConstants.HS_ANDROID_TV_PORT)
                var newHttpUrl = builder.url(HttpUrl.parse(url)).build().url()
                //重建request
                newHttpUrl = oldUrl.newBuilder()
                        .scheme(newHttpUrl!!.scheme())
                        .host(newHttpUrl!!.host())
                        .port(newHttpUrl!!.port())
                        .build()

                originalRequest = builder.url(newHttpUrl).build()
            } catch (e: Exception) {
                throw IOException("nas domain is empty")
            }
            return chain.proceed(originalRequest)
        } else {
            return chain.proceed(originalRequest)
        }

    }
}