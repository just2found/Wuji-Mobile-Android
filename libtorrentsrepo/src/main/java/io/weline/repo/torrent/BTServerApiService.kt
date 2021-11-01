package io.weline.repo.torrent

import androidx.lifecycle.LiveData
import io.reactivex.Observable
import io.weline.repo.torrent.data.BTItem
import io.weline.repo.torrent.data.BTItems
import io.weline.repo.torrent.data.BtSession
import io.weline.repo.torrent.data.BtVersion
import libs.source.common.livedata.ApiResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface BTServerApiService {

    @POST("/auth")
    fun auth(@Body body: @JvmSuppressWildcards Map<String, Any>): LiveData<ApiResponse<BtBaseResult<BtSession>>>

    @POST("/create")
    fun create(@Body body: @JvmSuppressWildcards Map<String, Any>): LiveData<ApiResponse<BtBaseResult<BTItem>>>

    @POST("/download")
    fun download(@Body body: @JvmSuppressWildcards Map<String, Any>): LiveData<ApiResponse<BtBaseResult<BTItem>>>

    @POST("/progress")
    fun progress(@Body body: @JvmSuppressWildcards Map<String, Any>): LiveData<ApiResponse<BtBaseResult<BTItems>>>

    @POST("/stop")
    fun stop(@Body body: @JvmSuppressWildcards Map<String, Any>): LiveData<ApiResponse<BtBaseResult<BTItems>>>

    @POST("/resume")
    fun resume(@Body body: @JvmSuppressWildcards Map<String, Any>): LiveData<ApiResponse<BtBaseResult<BTItems>>>

    @POST("/list")
    fun list(@Body body: @JvmSuppressWildcards Map<String, Any>): LiveData<ApiResponse<BtBaseResult<BTItems>>>

    @POST("/cancel")
    fun cancel(@Body body: @JvmSuppressWildcards Map<String, Any>): LiveData<ApiResponse<BtBaseResult<BTItems>>>

    @POST("/{path}")
    fun requestEncryptAuth(@Path("path") path: String, @Body body: String): LiveData<ApiResponse<BtBaseResult<BtSession>>>

    @POST("/{path}")
    fun requestEncryptItem(@Path("path") path: String, @Body body: String): LiveData<ApiResponse<BtBaseResult<BTItem>>>

    @POST("/{path}")
    fun requestEncryptItems(@Path("path") path: String, @Body body: String): LiveData<ApiResponse<BtBaseResult<BTItems>>>

    @GET("/version")
    fun version(): Observable<BtBaseResult<BtVersion>>
}