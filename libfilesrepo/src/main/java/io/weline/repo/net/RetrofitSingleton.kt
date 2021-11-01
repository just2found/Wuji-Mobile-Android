package io.weline.repo.net

import io.weline.repo.files.constant.AppConstants
import io.weline.repo.net.interceptor.AddNasSessionInterceptor
import io.weline.repo.net.interceptor.ChangeUrlInterceptor
import io.weline.repo.net.interceptor.CheckV5Interceptor
import libs.source.common.LibCommonApp
import libs.source.common.livedata.LiveDataCallAdapterFactory
import libs.source.common.livedata.ReqStringResGsonConverterFactory
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
import java.util.concurrent.TimeUnit


class RetrofitSingleton {
    private var retrofitWeakReference: WeakReference<Retrofit>? = null

    /**
     *  单例子：双重校验锁式（Double Check)
     */
    companion object {
        val instance: RetrofitSingleton by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
            RetrofitSingleton()
        }
    }

    @Synchronized
    fun getRetrofit(): Retrofit {
        if (retrofitWeakReference == null || retrofitWeakReference?.get() == null) {
            retrofitWeakReference = null
            val sslParams = HttpsUtils.getSslSocketFactory();
            val httpClientBuilder = OkHttpClient.Builder()
                    .connectTimeout(3, TimeUnit.SECONDS)
                    .writeTimeout(15, TimeUnit.SECONDS)
                    .readTimeout(15, TimeUnit.SECONDS)
                    //设置缓存,最大缓存空间为10Mb，
                    .cache(Cache(File(LibCommonApp.getApp().getExternalCacheDir(), AppConstants.DEFAULT_APP_ROOT_DIR_NAME + "/http_cache"), 10 * 1024 * 1024))
                    .hostnameVerifier(HttpsUtils.UnSafeHostnameVerifier)
                    .sslSocketFactory(sslParams.sSLSocketFactory, sslParams.trustManager)
                    //必须按顺序调用
                    .addInterceptor(CheckV5Interceptor())
                    .addInterceptor(AddNasSessionInterceptor())
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
                    .addConverterFactory(ReqStringResGsonConverterFactory.create())
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .addCallAdapterFactory(LiveDataCallAdapterFactory())
                    .build()
            retrofitWeakReference = WeakReference(retrofit)
        }
        return retrofitWeakReference?.get()!!
    }
}