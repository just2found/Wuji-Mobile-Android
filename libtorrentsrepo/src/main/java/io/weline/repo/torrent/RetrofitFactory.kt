package io.weline.repo.torrent

import io.weline.repo.torrent.constants.BT_Config
import libs.source.common.livedata.LiveDataCallAdapterFactory
import net.sdvn.common.internet.OkHttpClientIns
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitFactory {
    private const val debug = false
    fun createRetrofit(host: String, isDebug: Boolean = BT_Config.isDebug,
                       isUseLiveData: Boolean = true,
                       isLocal: Boolean = false,
                       timeout: Long = 0): Retrofit {
        val port = if (isDebug) {
            if (isLocal) {
                BT_Config.PORT_LOCAL_DEBUG
            } else {
                BT_Config.PORT_DEBUG
            }
        } else {
            if (isLocal) {
                BT_Config.PORT_LOCAL
            } else {
                BT_Config.PORT
            }
        }
        return if (debug) {
            createMockRetrofit(host)
        } else {
            createProductRetrofit(host, port, isUseLiveData, timeout)
        }
    }

    fun createProductRetrofit(host: String, port: Int, isUseLiveData: Boolean = true,
                              timeout: Long = 0): Retrofit {
        val builder = HttpUrl.Builder()
                .scheme(BT_Config.SCHEME)
                .host(host)
                .port(port)
                .build()
        val retrofitBuilder = Retrofit.Builder()
                .baseUrl(builder)
                .addConverterFactory(GsonConverterFactory.create())
        if (isUseLiveData)
            retrofitBuilder.addCallAdapterFactory(LiveDataCallAdapterFactory.create())
        else
            retrofitBuilder.addCallAdapterFactory(RxJava2CallAdapterFactory.create())
        var apiClient = OkHttpClientIns.getApiClient()
        if (timeout > 0) {
            apiClient = apiClient.newBuilder()
                    .connectTimeout(if (timeout > 10) 10 else timeout, TimeUnit.SECONDS)
                    .writeTimeout(timeout, TimeUnit.SECONDS)
                    .readTimeout(timeout, TimeUnit.SECONDS)
                    .build()
        }
        return retrofitBuilder
                .client(apiClient)
                .build()
    }

    fun createMockRetrofit(host: String): Retrofit {
        val loggingInterceptor = HttpLoggingInterceptor()
        loggingInterceptor.level = HttpLoggingInterceptor.Level.BODY
        return Retrofit.Builder()
                .baseUrl(host)
                .client(OkHttpClient.Builder()
                        .connectTimeout(3, TimeUnit.SECONDS)
                        .readTimeout(15, TimeUnit.SECONDS)
                        .writeTimeout(15, TimeUnit.SECONDS)
                        .addInterceptor(loggingInterceptor)
                        .build())
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build()
    }
}