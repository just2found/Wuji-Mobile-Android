package io.weline.repo.net.interceptor

import android.text.TextUtils
import io.weline.repo.SessionCache
import okhttp3.Interceptor
import okhttp3.MediaType
import okhttp3.RequestBody
import okhttp3.Response
import okio.Buffer
import org.json.JSONObject

/** 检查是否需要获取session
 *  会在参数增加一个 session
 *
 *  没有session 同步请求获取session后继续本次请求
 * @author Raleigh.Luo
 * date：20/9/18 09
 * describe：
 */
class AddNasSessionInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        //获取原始的request
        var originalRequest = chain.request()
        val oldUrl = originalRequest.url()
        val builder = originalRequest.newBuilder()
        /*---------Header自定义参数---------------------------------------------*/
        //从request中获取headers,通过给定的domain域名,进行更改
        val addSession = originalRequest.header("add-session")
        val token = originalRequest.header("token")
        val ip = originalRequest.header("nas_domain")
        val deviceId = originalRequest.header("device_id")

        if (!TextUtils.isEmpty(addSession) && addSession == "true"
                && !TextUtils.isEmpty(deviceId)
                && !TextUtils.isEmpty(token) && !TextUtils.isEmpty(ip)) {
            //移除自定义的Header ,domain 不移除，进入下一个拦截器
            builder.removeHeader("add-session")
            builder.removeHeader("token")
            builder.removeHeader("device_id")
            /*---------获取缓存中的session或请求---------------------------------------------*/
            val user = SessionCache.instance.getOrSynchRequest(deviceId!!, ip!!, token!!)
            user?.let {
                val session = it.session ?: ""
                originalRequest.body()?.let {
                    /*---------body中添加session字段---------------------------------------------*/
                    getNewRequestBody(it, ip, session).let {
                        originalRequest = originalRequest.newBuilder().post(it).build()
                    }
                }
            } ?: let {
                //取消本次请求,不是V5,调用旧接口
                chain.call().cancel()
            }
            return chain.proceed(originalRequest)

        } else {
            return chain.proceed(originalRequest)
        }
    }

    /**
     * 添加 session对象，并且返回新的请求体
     */
    private fun getNewRequestBody(body: RequestBody, domain: String, session: String): RequestBody {
        val buffer = Buffer() // 创建缓存
        body.writeTo(buffer) //将请求体内容,写入缓存
        val parameterStr: String = buffer.readUtf8() // 读取参数字符串
        //如果是json串就解析 从新加餐 如果是字符串就进行修改 具体业务逻辑自己加
        val json = JSONObject(parameterStr)
        json.put("session", session)
        //对应请求头按照自己的传输方式 定义
        val requestBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), json.toString())
        return requestBody
    }


}