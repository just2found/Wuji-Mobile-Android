package io.weline.repo.net.interceptor

import android.text.TextUtils
import android.util.Log
import net.linkmate.app.api.DynamicApiService
import net.linkmate.app.net.RetrofitSingleton
import net.linkmate.app.service.DynamicQueue
import okhttp3.Interceptor
import okhttp3.MediaType
import okhttp3.RequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException

/** 检查是否需要获取 动态Authorization
 *  会在参数增加一个 Authorization
 *
 *  没有Authorization 同步请求获取Authorization后继续本次请求
 * @author Raleigh.Luo
 * date：20/9/18 09
 * describe：
 */
class AddDynamicAuthorizationInterceptor : Interceptor {

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        //获取原始的request
        var originalRequest = chain.request()
        val oldUrl = originalRequest.url()
        val builder = originalRequest.newBuilder()
        /*---------Header自定义参数---------------------------------------------*/
        //从request中获取headers,通过给定的domain域名,进行更改
        val addAuthorization = originalRequest.header("add-dynamic-authorization")
        val token = originalRequest.header("token")
        val domain = originalRequest.header("domain")
        val deviceId = originalRequest.header("device_id")

        if (!TextUtils.isEmpty(addAuthorization) && addAuthorization == "true"
                && !TextUtils.isEmpty(deviceId)
                && !TextUtils.isEmpty(token) && !TextUtils.isEmpty(domain)) {
            //移除自定义的Header ,domain 不移除，进入下一个拦截器
            builder.removeHeader("add-dynamic-authorization")
            builder.removeHeader("token")
            builder.removeHeader("device_id")
            /*---------获取缓存中的session或请求---------------------------------------------*/
            val authorization = getDynamicAuthorization(domain!!, deviceId!!, token!!)
            authorization?.let {
                builder.addHeader("Authorization", String.format("Bearer %s", it))
                originalRequest = builder.url(oldUrl).build()
                true
            } ?: let {
                //手动抛出异常，登录获取不到 Authorization，说明设备不支持动态接口
                throw IOException(DynamicQueue.THE_CIRCLE_NOT_SUPPORT_DYNAMIC);
            }
            val response = chain.proceed(originalRequest)
            if (isTokenExpired(response, deviceId)) {//Token过期，重新获取Token,只会重试一次
                val authorization = getDynamicAuthorization(domain!!, deviceId!!, token!!)
                authorization?.let {
                    val newRequest = chain.request().newBuilder()
                            .header("Authorization", String.format("Bearer %s", it))
                            .build()
                    return chain.proceed(newRequest)
                } ?: let {
                    //手动抛出异常，登录获取不到 Authorization，说明设备不支持动态接口
                    throw IOException(DynamicQueue.THE_CIRCLE_NOT_SUPPORT_DYNAMIC);
                    return chain.proceed(originalRequest)
                }
            } else {
                return response
            }
        } else {
            return chain.proceed(originalRequest)
        }
    }

    private fun isTokenExpired(response: Response, deviceId: String): Boolean {
        if (response.code() == DynamicQueue.TOKEN_EXPIRE) {
            RetrofitSingleton.instance.removeDynamicAuthorization(deviceId)
            return true
        }
        return false
    }

    /**
     * 获取动态的authorization
     * 优先内存缓存
     */
    private fun getDynamicAuthorization(domain: String, deviceId: String, token: String): String? {
        var authorization = RetrofitSingleton.instance.getDynamicAuthorization(deviceId)
        if (TextUtils.isEmpty(authorization)) {
            val apiService = RetrofitSingleton.instance.getRetrofit().create(DynamicApiService::class.java)
            try {
                val json = JSONObject()
                json.put("token", token)
                val body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), json.toString())
                //同步请求
                val result = apiService.login(domain, body).execute()
                if (result.body()?.code == DynamicQueue.SUCCESS_CODE) {
                    result.body()?.token?.let {
                        RetrofitSingleton.instance.putDynamicAuthorization(deviceId, it)
                        authorization = it
                    }
                }
            } catch (e: Exception) {
            }
        }
        return authorization
    }

}