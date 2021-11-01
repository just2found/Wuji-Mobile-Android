package net.linkmate.app.poster.repository

import net.linkmate.app.poster.model.*
import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.http.*

interface Api {

    //    {"method":"list","session":"xxx","params":{"path":"xx","ftype":"xx","order":"xx"}}
    @POST("/file")
    suspend fun fileList(@Body params: Any): BaseResult<FileListResult>

    //    {"method":"manage","session":"xxx","params":{"cmd":"readtxt","path":["xxx"]}}
    @POST("/file")
    suspend fun readTxt(@Body params: Any): BaseResult<TxtResult>

    @POST("/file")
    suspend fun updateFile(@Body params: Any): BaseResult<Any>

    @POST("/file")
    suspend fun fileInfo(@Body params: Any): BaseResult<FileInfoResult>

    @Headers("XX-Device-Type:android")
    @FormUrlEncoded
    @POST("user/doRegister")
    suspend fun register(@Field("username") username:String,
                         @Field("password") password:String): BaseResultCrm<Any>

    @Headers("XX-Device-Type:android")
    @FormUrlEncoded
    @POST("user/login")
    suspend fun login(@Field("username") username:String,
                      @Field("password") password:String): BaseResultCrm<RegisterCrmModel>

    @Headers("XX-Device-Type:android")
    @FormUrlEncoded
    @POST("user/passwordReset")
    suspend fun passwordReset(@Header("XX-Token") token:String,
                              @Field("password") password:String): BaseResultCrm<Any>

    @Headers("XX-Device-Type:android")
    @POST("product/lists")
    suspend fun productLists(@Header("XX-Token") token:String): BaseResultCrm<ArrayList<ProductResult>>

    @Headers("XX-Device-Type:android")
    @POST("product/qrcode")
    suspend fun qrcode(@Header("XX-Token") token:String): BaseResultCrm<ProductPaymentCode>

    @Headers("XX-Device-Type:android")
    @Multipart
    @POST("product/uploadscreenshot")
    suspend fun uploadScreenshot(@Header("XX-Token") token:String,
                                 @Part file: MultipartBody.Part): BaseResultCrm<UploadScreenshot>

    @Headers("XX-Device-Type:android")
    @FormUrlEncoded
    @POST("product/place")
    suspend fun place(@Header("XX-Token") token: String,
                      @Field("product_id") productId: String,
                      @Field("pay_type") payType: String,
                      @Field("screenshot") screenshot: String): BaseResultCrm<Any>

    @Headers("XX-Device-Type:android")
    @FormUrlEncoded
    @POST("product/orders")
    suspend fun orders(@Header("XX-Token") token: String,
                       @Field("status") status: String): BaseResultCrm<ArrayList<OrderModel>>
}