package net.linkmate.app.poster.repository

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import net.linkmate.app.bean.DeviceBean
import net.linkmate.app.poster.model.PosterBundleModel
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Create by Admin on 2021-07-12-14:51
 */
class RetrofitRep {

  fun initRetrofit(ip: String): Api {
    val url = "http://${ip}:9898"
    return Retrofit.Builder()
      .baseUrl(url)
      .client(getOkHttpClient())
      .addConverterFactory(GsonConverterFactory.create(dateGson))
      .build().create(Api::class.java)
  }

  fun initRetrofitCrm(ip: String): Api {
    val url = "http://${ip}:8080/api/"
    return Retrofit.Builder()
            .baseUrl(url)
            .client(getOkHttpClient())
            .addConverterFactory(GsonConverterFactory.create(dateGson))
            .build().create(Api::class.java)
  }

  private fun getOkHttpClient(): OkHttpClient {
    return OkHttpClient.Builder()
      .addInterceptor(initLogInterceptor())
//      .addInterceptor(RetryInterceptor(3))
      .connectTimeout(10, TimeUnit.SECONDS)
      .readTimeout(30, TimeUnit.SECONDS)
      .build()
  }

  private fun initLogInterceptor(): HttpLoggingInterceptor {
    val interceptor = HttpLoggingInterceptor()
    interceptor.level = HttpLoggingInterceptor.Level.HEADERS
    interceptor.level = HttpLoggingInterceptor.Level.BODY
    return interceptor
  }


  val dateGson: Gson
    get() = GsonBuilder()
      //如果不设置serializeNulls,序列化时默认忽略NULL
      .serializeNulls()
      //使打印的json字符串更美观，如果不设置，打印出来的字符串不分行
      .setPrettyPrinting()
      .create()

  /**
   * 重试拦截器
   */
  inner class RetryInterceptor(var maxRetry: Int) : Interceptor {
    private var retryNum = 0

    override fun intercept(chain: Interceptor.Chain): Response {
      val request: Request = chain.request()
      var response: Response = chain.proceed(request)
      while (!response.isSuccessful && retryNum < maxRetry) {
        retryNum++
        response = chain.proceed(request)
      }
      return response
    }
  }
}