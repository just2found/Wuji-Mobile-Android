package net.linkmate.app.api

import androidx.lifecycle.LiveData
import io.reactivex.Observable
import libs.source.common.livedata.ApiResponse
import net.linkmate.app.data.model.Base
import net.linkmate.app.data.model.dynamic.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*

/** 动态API
 * @author Raleigh.Luo
 * date：20/12/24 15
 * describe：
 */
interface DynamicApiService {
    /*---登录接口-------------------------------------------------------------------------------*/
    /**
     * 登录
     */
    @Headers("Content-Type:application/json")
    @POST("/login")
    fun login(@Header("domain") domain: String,
              @Body body: RequestBody): Call<Login>

    /**
     * 获取动态列表
     */
    @Headers("Content-Type:application/json")
    @POST("/login")
    fun loginLiveData(@Header("domain") domain: String,
              @Body body: RequestBody): LiveData<ApiResponse<Login>>
    /*---动态接口-------------------------------------------------------------------------------*/
    /**
     * 获取动态列表
     */
    @Headers("add-dynamic-authorization:true")
    @GET("/api/v1/moments")
    fun getDynamicList(@Header("domain") domain: String,
                       @Header("device_id") deviceId: String,//用来记录保存authorization
                       @Header("token") token: String,
                       @Query("lasttime") lastTime: String,//value如 >=1610445850 ，lastTime最大长度12，即最多两位操作数加十位unix时间戳(秒级),支持操作符：<>、!=、=、==、>、<、>=、<=
                       @Query("pageindex") pageIndex: Int = 0,//pageIndex start with 0
                       @Query("pagesize") pageSize: Int = 5): LiveData<ApiResponse<DynamicList>>

    /**
     * 获取动态列表 从后台缓存中加载，提升速度
     */
    @Headers("add-dynamic-authorization:true")
    @GET("/api/v1/moments")
    fun getNewestDynamicList(@Header("domain") domain: String,
                             @Header("device_id") deviceId: String,//用来记录保存authorization
                             @Header("token") token: String,
                             @Query("latest") latest: Int//从缓存中取多少条(默认为 0),最大为100
    ): LiveData<ApiResponse<DynamicList>>


    /**
     * 获取指定动态
     */
    @Headers("add-dynamic-authorization:true",
            "Content-Type:application/json")
    @POST("/api/v1/getmoment")
    fun getDynamic(@Header("domain") domain: String,
                   @Header("device_id") deviceId: String,//用来记录保存authorization
                   @Header("token") token: String,
                   @Body body: RequestBody
    ): LiveData<ApiResponse<DynamicList>>

    /**
     * 获取指定动态数组
     */
    @Headers("add-dynamic-authorization:true",
            "Content-Type:application/json")
    @POST("/api/v1/getmoment")
    fun getDynamics(@Header("domain") domain: String,
                    @Header("device_id") deviceId: String,//用来记录保存authorization
                    @Header("token") token: String,
                    @Body body: RequestBody
    ): Observable<DynamicList>

    /**
     * 获取指定动态
     */
    @Headers("add-dynamic-authorization:true",
            "Content-Type:application/json")
    @POST("/api/v1/getmoment")
    fun getDynamicsRx(@Header("domain") domain: String,
                      @Header("device_id") deviceId: String,//用来记录保存authorization
                      @Header("token") token: String,
                      @Body body: RequestBody
    ): Observable<DynamicList>

    /**
     * 删除动态
     */
    @Headers("add-dynamic-authorization:true",
            "Content-Type:application/json")
    @DELETE("/api/v1/moment/{id}")
    fun deleteDynamic(@Header("domain") domain: String,
                      @Header("device_id") deviceId: String,//用来记录保存authorization
                      @Header("token") token: String,
                      @Path("id") id: Long
    ): LiveData<ApiResponse<Base>>


    /**
     * 删除动态
     */
    @Headers("add-dynamic-authorization:true",
            "Content-Type:application/json")
    @DELETE("/api/v1/moment/{id}")
    fun deleteDynamicRx(@Header("domain") domain: String,
                        @Header("device_id") deviceId: String,//用来记录保存authorization
                        @Header("token") token: String,
                        @Path("id") id: Long
    ): Observable<Base>


    /**
     * 发布评论
     */
    @Headers("add-dynamic-authorization:true",
            "Content-Type:application/json")
    @POST("/api/v1/comment")
    fun publishComment(@Header("domain") domain: String,
                       @Header("device_id") deviceId: String,//用来记录保存authorization
                       @Header("token") token: String,
                       @Body body: RequestBody): LiveData<ApiResponse<Base>>

    /**
     * 发布评论
     */
    @Headers("add-dynamic-authorization:true",
            "Content-Type:application/json")
    @POST("/api/v1/comment")
    fun publishCommentRx(@Header("domain") domain: String,
                         @Header("device_id") deviceId: String,//用来记录保存authorization
                         @Header("token") token: String,
                         @Body body: RequestBody): Observable<DynamicCommentDetail>

    /**
     * 删除评论
     */
    @Headers("add-dynamic-authorization:true",
            "Content-Type:application/json")
    @DELETE("/api/v1/comment/{id}")
    fun deleteComment(@Header("domain") domain: String,
                      @Header("device_id") deviceId: String,//用来记录保存authorization
                      @Header("token") token: String,
                      @Path("id") id: Long): LiveData<ApiResponse<Base>>

    /**
     * 删除评论
     */
    @Headers("add-dynamic-authorization:true",
            "Content-Type:application/json")
    @DELETE("/api/v1/comment/{id}")
    fun deleteCommentRx(@Header("domain") domain: String,
                        @Header("device_id") deviceId: String,//用来记录保存authorization
                        @Header("token") token: String,
                        @Path("id") id: Long): Observable<Base>


    /**
     * 点赞
     */
    @Headers("add-dynamic-authorization:true",
            "Content-Type:application/json")
    @POST("/api/v1/like")
    fun like(@Header("domain") domain: String,
             @Header("device_id") deviceId: String,//用来记录保存authorization
             @Header("token") token: String,
             @Body body: RequestBody): LiveData<ApiResponse<Base>>

    /**
     * 点赞
     */
    @Headers("add-dynamic-authorization:true",
            "Content-Type:application/json")
    @POST("/api/v1/like")
    fun likeRx(@Header("domain") domain: String,
               @Header("device_id") deviceId: String,//用来记录保存authorization
               @Header("token") token: String,
               @Body body: RequestBody): Observable<DynamicLikeDetail>

    /**
     * 取消点赞
     */
    @Headers("add-dynamic-authorization:true",
            "Content-Type:application/json")
    @POST("/api/v1/unlike")
    fun unLike(@Header("domain") domain: String,
               @Header("device_id") deviceId: String,//用来记录保存authorization
               @Header("token") token: String,
               @Body body: RequestBody): LiveData<ApiResponse<Base>>

    /**
     * 取消点赞
     */
    @Headers("add-dynamic-authorization:true",
            "Content-Type:application/json")
    @POST("/api/v1/unlike")
    fun unLikeRx(@Header("domain") domain: String,
                 @Header("device_id") deviceId: String,//用来记录保存authorization
                 @Header("token") token: String,
                 @Body body: RequestBody): Observable<Base>

    /**
     * 与我相关
     */
    @Headers("add-dynamic-authorization:true")
    @GET("/api/v1/about")
    fun getRelatedMessage(@Header("domain") domain: String,
                          @Header("device_id") deviceId: String,//用来记录保存authorization
                          @Header("token") token: String,
                          @Query("del") isDelete: Int): LiveData<ApiResponse<AboutMessage>> //del可为空,默认为0,如为1则调用一次后会清空与我相关

    /**
     * 与我相关
     */
    @Headers("add-dynamic-authorization:true")
    @GET("/api/v1/aboutinfo")
    fun getRelatedList(@Header("domain") domain: String,
                       @Header("device_id") deviceId: String,//用来记录保存authorization
                       @Header("token") token: String): LiveData<ApiResponse<RelatedList>> //del可为空,默认为0,如为1则调用一次后会清空与我相关


    /**
     * 下载文件
     */
    @Headers("add-dynamic-authorization:true")
    @GET("/api/v1/file")
    fun getFile(@Header("domain") domain: String,
                @Header("device_id") deviceId: String,//用来记录保存authorization
                @Header("token") token: String,
                @Query("fname") fileName: String): Call<ResponseBody> //del可为空,默认为0,如为1则调用一次后会清空与我相关


}

/**
 * 动态上传
 */
interface DynamicUploadApiService {
    /**
     * 发布动态  返回发布的信息
     */
    @Multipart
    @Headers("add-dynamic-authorization:true",
            "add-dynamic-upload-listener:true",
            "Connection:keep-alive")// 避免java.net.SocketException: Socket closed后重复调用报错
    @POST("/api/v1/moment")
    fun publishDynamicRx(@Header("domain") domain: String,
                         @Header("device_id") deviceId: String,//用来记录保存authorization
                         @Header("token") token: String,
                         @Header("identification") identification: String,
                         @PartMap body: @JvmSuppressWildcards Map<String, RequestBody>,
                         @Part parts: List<MultipartBody.Part>): Observable<DynamicDetail>
}