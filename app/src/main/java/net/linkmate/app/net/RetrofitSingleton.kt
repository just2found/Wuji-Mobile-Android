package net.linkmate.app.net

import android.text.TextUtils
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.load.model.Headers
import com.bumptech.glide.load.model.LazyHeaders
import io.weline.repo.files.constant.AppConstants
import io.weline.repo.net.interceptor.AddDynamicAuthorizationInterceptor
import io.weline.repo.net.interceptor.ChangeUrlInterceptor
import libs.source.common.LibCommonApp
import libs.source.common.livedata.LiveDataCallAdapterFactory
import net.linkmate.app.net.interceptor.DynamicUploadProgressInterceptor
import net.sdvn.common.internet.BuildConfig
import net.sdvn.common.internet.utils.HttpsUtils
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.lang.ref.WeakReference
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit


class RetrofitSingleton {
    private var retrofitWeakReference: WeakReference<Retrofit>? = null
    private var retrofitUploadWeakReference: WeakReference<Retrofit>? = null

    /**
     *  单例子：双重校验锁式（Double Check)
     */
    companion object {
        val instance: RetrofitSingleton by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
            RetrofitSingleton()
        }
    }

    /**
     * 动态Authorization缓存
     */
    private val dynamicAuthorizationCache = ConcurrentHashMap<String, String>()
    private val dynamicAuthorizationHashCode = ConcurrentHashMap<String, Int>()
    fun putDynamicAuthorization(deviceId: String, authorization: String) {
        if (!TextUtils.isEmpty(authorization)) {
            dynamicAuthorizationCache.put(deviceId, authorization)
            dynamicAuthorizationHashCode.put(deviceId, authorization.hashCode())
        }
    }

    /**
     * 用于做Tag区分同一url不同authorization
     */
    fun getDynamicAuthorizationHashCode(deviceId: String): Int? {
        return dynamicAuthorizationHashCode.get(deviceId)
    }

    fun removeDynamicAuthorization(deviceId: String) {
        dynamicAuthorizationCache.remove(deviceId)
        dynamicAuthorizationHashCode.remove(deviceId)
    }

    fun getDynamicAuthorization(deviceId: String): String? {
        return dynamicAuthorizationCache.get(deviceId)
    }

    fun clear() {
        dynamicAuthorizationCache.clear()
    }

    /**
     * Glide增加头部
     */
    fun getDynamicGlideUrl(deviceId: String, key: String, url: String?): GlideUrl {
        return DynamicGlideUrl(url, key, LazyHeaders.Builder()
                .addHeader("Authorization", String.format("Bearer %s", getDynamicAuthorization(deviceId)
                        ?: ""))
                .build())
    }

    class DynamicGlideUrl(url: String?, private val key: String, headers: Headers) : GlideUrl(url, headers) {
        override fun getCacheKey(): String {
            return key
        }
    }

    @Synchronized
    fun getRetrofit(): Retrofit {
        if (retrofitWeakReference == null || retrofitWeakReference?.get() == null) {
            retrofitWeakReference = null
            val sslParams = HttpsUtils.getSslSocketFactory();
            val httpClientBuilder = OkHttpClient.Builder()
                    .connectTimeout(15, TimeUnit.SECONDS)
                    .writeTimeout(Int.MAX_VALUE.toLong(), TimeUnit.MILLISECONDS)
                    .readTimeout(15, TimeUnit.SECONDS)
                    //设置缓存,最大缓存空间为10Mb，
                    .cache(Cache(File(LibCommonApp.getApp().getExternalCacheDir(), AppConstants.DEFAULT_APP_ROOT_DIR_NAME + "/http_cache"), 10 * 1024 * 1024))
                    .hostnameVerifier(HttpsUtils.UnSafeHostnameVerifier)
                    .sslSocketFactory(sslParams.sSLSocketFactory, sslParams.trustManager)
                    //必须按顺序调用
                    .addInterceptor(AddDynamicAuthorizationInterceptor())
                    .addInterceptor(ChangeUrlInterceptor())

            if (BuildConfig.DEBUG) {
                val loggingInterceptor = HttpLoggingInterceptor()
                loggingInterceptor.level = HttpLoggingInterceptor.Level.BODY
                httpClientBuilder.addInterceptor(loggingInterceptor)
            }

            val httpClient = httpClientBuilder.build()
            val retrofit = Retrofit.Builder()
                    .baseUrl("http://app.memenet.net")//URL
                    .client(httpClient)
                    .addConverterFactory(GsonConverterFactory.create())
                    //支持json,且处理转换报错
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .addCallAdapterFactory(LiveDataCallAdapterFactory())
                    .build()
            retrofitWeakReference = WeakReference(retrofit)
        }
        return retrofitWeakReference?.get()!!
    }

    /**
     * 上传进度监听
     * 不能使用HttpLoggingInterceptor 打印日志，HttpLoggingInterceptor中会writeTo一次requestbody，监听进度拦截器又会writeTo一次，重复导致写入翻倍异常
     */
    @Synchronized
    fun getUploadRetrofit(): Retrofit {
        if (retrofitUploadWeakReference == null || retrofitUploadWeakReference?.get() == null) {
            retrofitUploadWeakReference = null
            val sslParams = HttpsUtils.getSslSocketFactory();
            val httpClientBuilder = OkHttpClient.Builder()
                    .connectTimeout(15, TimeUnit.SECONDS)
                    .writeTimeout(15, TimeUnit.SECONDS)//上传最大写入超时时间
                    .readTimeout(15, TimeUnit.SECONDS)
                    //设置缓存,最大缓存空间为10Mb，
                    .hostnameVerifier(HttpsUtils.UnSafeHostnameVerifier)
                    .sslSocketFactory(sslParams.sSLSocketFactory, sslParams.trustManager)
                    //必须按顺序调用
                    .addInterceptor(AddDynamicAuthorizationInterceptor())
                    .addInterceptor(ChangeUrlInterceptor())
                    .addInterceptor(DynamicUploadProgressInterceptor())
            val httpClient = httpClientBuilder.build()
            val retrofit = Retrofit.Builder()
                    .baseUrl("http://app.memenet.net")//URL
                    .client(httpClient)
                    .addConverterFactory(GsonConverterFactory.create())
                    //支持json,且处理转换报错
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .addCallAdapterFactory(LiveDataCallAdapterFactory())
                    .build()
            retrofitUploadWeakReference = WeakReference(retrofit)
        }
        return retrofitUploadWeakReference?.get()!!
    }
}