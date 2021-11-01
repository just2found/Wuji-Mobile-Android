package io.weline.repo.api

import io.reactivex.Observable
import io.weline.repo.data.model.*
import io.weline.repo.files.data.DataSessionUser
import io.weline.repo.files.data.FileTag
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.*

/**
 * @author Raleigh.Luo
 * date：20/9/18 09
 * describe：说明
 * 1.check-session:check AddSessionInterceptor拦截器中检查session(没有就请求)，并将session添加到请求体中
 * 2.@Header("domain")  ChangeUrlInterceptor会将domain的参数替换为请求url
 */
interface ApiService {
    /*---用户接口-------------------------------------------------------------------------------*/
    /**
     * 获取设备是否是v5
     */
    @GET("/version")
    fun checkIsV5(@Header("domain") domain: String): Call<CheckV5Version>

    /**
     * 获取设备是否是v5 异步请求
     */
    @GET("/version")
    fun checkIsV5Asyn(@Header("domain") domain: String): Observable<CheckV5Version>

    /**
     * 获取登录session
     */
    @Headers("Content-Type:application/json")
    @POST("/user")
    fun getSession(
        @Header("domain") domain: String, @Body body: RequestBody
    ): Call<BaseProtocol<DataSessionUser?>>

    /**
     * 获取登录session
     */
    @Headers("Content-Type:application/json")
    @POST("/user")
    fun getSessionAsyn(
        @Header("domain") domain: String, @Body body: RequestBody
    ): Observable<BaseProtocol<DataSessionUser?>>

    /**
     * 获取登录session
     */
    @Headers("Content-Type:application/json")
    @POST("/user")
    fun getSessionAsyn2(
        @Header("domain") domain: String, @Body body: RequestBody
    ): Observable<BaseProtocol<Any>>


    /**
     * 用户
     */
    @Headers(
        "add-session:true",  // AddSessionInterceptor拦截器中检查session(没有就请求)，并将session添加到请求体中
        "Content-Type:application/json"
    )
    @POST("/user")
    fun getUserList(
        @Header("device_id") deviceId: String,//用来记录保存session
        @Header("nas_domain") ip: String,
        @Header("token") token: String,//CheckSessionInterceptor中 取值
        @Body body: RequestBody
    ): Observable<BaseProtocol<Users>> ////header更换域名

    /**
     * 操作用户，删除，增加，更新
     */
    @Headers(
        "add-session:true",  // AddSessionInterceptor拦截器中检查session(没有就请求)，并将session添加到请求体中
        "Content-Type:application/json"
    )
    @POST("/user")
    fun optUser(
        @Header("device_id") deviceId: String,//用来记录保存session
        @Header("nas_domain") ip: String,
        @Header("token") token: String,//CheckSessionInterceptor中 取值
        @Body body: RequestBody
    ): Observable<BaseProtocol<Any>> ////header更换域名

    /**
     * 获取/设置用户空间
     */
    @Headers(
        "add-session:true",  // AddSessionInterceptor拦截器中检查session(没有就请求)，并将session添加到请求体中
        "Content-Type:application/json"
    )
    @POST("/user")
    fun getUserInfo(
        @Header("device_id") deviceId: String,//用来记录保存session
        @Header("nas_domain") ip: String,
        @Header("token") token: String,//CheckSessionInterceptor中 取值
        @Body body: RequestBody
    ): Observable<BaseProtocol<User>> ////header更换域名

    /**
     * 获取/设置用户空间
     */
    @Headers(
        "add-session:true",  // AddSessionInterceptor拦截器中检查session(没有就请求)，并将session添加到请求体中
        "Content-Type:application/json"
    )
    @POST("/user")
    fun userSpace(
        @Header("device_id") deviceId: String,//用来记录保存session
        @Header("nas_domain") ip: String,
        @Header("token") token: String,//CheckSessionInterceptor中 取值
        @Body body: RequestBody
    ): Observable<BaseProtocol<UserSpace>> ////header更换域名

    /**
     * 获取/设置用户空间
     */
    @Headers(
        "add-session:true",  // AddSessionInterceptor拦截器中检查session(没有就请求)，并将session添加到请求体中
        "Content-Type:application/json"
    )
    @POST("/user")
    fun userDevMark(
        @Header("device_id") deviceId: String,//用来记录保存session
        @Header("nas_domain") ip: String,
        @Header("token") token: String,//CheckSessionInterceptor中 取值
        @Body body: @JvmSuppressWildcards Map<String, Any>
    ): Observable<BaseProtocol<DataDevMark>> //header更换域名

    /**
     * 获取/设置用户自定义数据
     */
    @Headers(  // AddSessionInterceptor拦截器中检查session(没有就请求)，并将session添加到请求体中
        "Content-Type:application/json"
    )
    @POST("/user")
    fun setSysInfo(
        @Header("device_id") deviceId: String,//用来记录保存session
        @Header("nas_domain") ip: String,
        @Header("token") token: String,//CheckSessionInterceptor中 取值
        @Header("add-session") addSession: Boolean = true,// AddSessionInterceptor拦截器中检查session(没有就请求)，并将session添加到请求体中
        @Body body: @JvmSuppressWildcards Map<String, Any>
    ): Observable<BaseProtocol<Any>> ////header更换域名

    /**
     * 获取/设置用户自定义数据
     */
    @Headers(  // AddSessionInterceptor拦截器中检查session(没有就请求)，并将session添加到请求体中
        "Content-Type:application/json"
    )
    @POST("/user")
    fun sysInfo(
        @Header("device_id") deviceId: String,//用来记录保存session
        @Header("nas_domain") ip: String,
        @Header("token") token: String,//CheckSessionInterceptor中 取值
        @Header("add-session") addSession: Boolean = false,// AddSessionInterceptor拦截器中检查session(没有就请求)，并将session添加到请求体中
        @Body body: @JvmSuppressWildcards Map<String, Any>
    ): Observable<BaseProtocol<DataSysInfoItem>> ////header更换域名

    /**
     * 获取/设置用户自定义数据
     */
    @Headers("Content-Type:application/json")
    @POST("/user")
    fun sysInfoAll(
        @Header("device_id") deviceId: String,//用来记录保存session
        @Header("nas_domain") ip: String,
        @Header("token") token: String,//CheckSessionInterceptor中 取值
        @Header("add-session") addSession: Boolean = false,// AddSessionInterceptor拦截器中检查session(没有就请求)，并将session添加到请求体中
        @Body body: @JvmSuppressWildcards Map<String, Any>
    ): Observable<BaseProtocol<List<DataSysInfoItem>>> ////header更换域名


    /*---文件接口-------------------------------------------------------------------------------*/
    /**
     * 操作文件，删除，增加，更新
     */
    @Headers(
        "add-session:true",  // AddSessionInterceptor拦截器中检查session(没有就请求)，并将session添加到请求体中
        "Content-Type:application/json"
    )
    @POST("/file")
    fun getFileList(
        @Header("device_id") deviceId: String,//用来记录保存session
        @Header("nas_domain") ip: String,
        @Header("token") token: String,//CheckSessionInterceptor中 取值
        @Body body: RequestBody
    ): Observable<BaseProtocol<Any>> ////header更换域名

    /**
     * 操作文件，删除，增加，更新
     */
    @Headers(
        "add-session:true",  // AddSessionInterceptor拦截器中检查session(没有就请求)，并将session添加到请求体中
        "Content-Type:application/json"
    )
    @POST("/file")
    fun optFile(
        @Header("device_id") deviceId: String,//用来记录保存session
        @Header("nas_domain") ip: String,
        @Header("token") token: String,//CheckSessionInterceptor中 取值
        @Body body: RequestBody
    ): Observable<BaseProtocol<Any>> ////header更换域名

    @Headers(
        "add-session:true",  // AddSessionInterceptor拦截器中检查session(没有就请求)，并将session添加到请求体中
        "Content-Type:application/json"
    )
    @POST("/file")
    fun optFile(
        @Header("device_id") deviceId: String,//用来记录保存session
        @Header("nas_domain") ip: String,
        @Header("token") token: String,//CheckSessionInterceptor中 取值
        @Body body: @JvmSuppressWildcards Map<String, Any>
    ): Observable<BaseProtocol<Any>>

    /*---系统接口-------------------------------------------------------------------------------*/
    /**
     * 操作系统，删除，增加，更新
     */
    @Headers(
        "add-session:true",  // AddSessionInterceptor拦截器中检查session(没有就请求)，并将session添加到请求体中
        "Content-Type:application/json"
    )
    @POST("/sys")
    fun optSystem(
        @Header("device_id") deviceId: String,//用来记录保存session
        @Header("nas_domain") ip: String,
        @Header("token") token: String,//CheckSessionInterceptor中 取值
        @Body body: RequestBody
    ): Observable<BaseProtocol<Any>> ////header更换域名

    @Headers(
        "add-session:false",  // AddSessionInterceptor拦截器中检查session(没有就请求)，并将session添加到请求体中
        "Content-Type:application/json"
    )
    @POST("/status")
    fun getSysStatus(
        @Header("nas_domain") ip: String,
        @Body body: RequestBody
    ): Observable<BaseProtocol<List<Int>>> ////header更换域名

    @Headers(
        "add-session:false",  // AddSessionInterceptor拦截器中检查session(没有就请求)，并将session添加到请求体中
        "Content-Type:application/json"
    )
    @POST("/initdisk")
    fun initDisk(
        @Header("device_id") deviceId: String,
        @Header("nas_domain") ip: String,
        @Body body: RequestBody
    ): Observable<BaseProtocol<Any>>

    @Headers(
        "add-session:false",  // AddSessionInterceptor拦截器中检查session(没有就请求)，并将session添加到请求体中
        "Content-Type:application/json"
    )
    @POST("/diskstatus")
    fun queryDiskStatus(
        @Header("device_id") deviceId: String,
        @Header("nas_domain") ip: String,
        @Body body: RequestBody
    ): Observable<BaseProtocol<DataDiskStatus>>


    @Headers(
        "add-session:true",  // AddSessionInterceptor拦截器中检查session(没有就请求)，并将session添加到请求体中
        "Content-Type:application/json"
    )
    @POST("/service")
    fun getServiceStatus(
        @Header("device_id") deviceId: String,
        @Header("nas_domain") ip: String,
        @Header("token") token: String,//CheckSessionInterceptor中 取值
        @Body body: RequestBody
    ): Observable<BaseProtocol<List<ServiceStatus>>>

    @Headers(
        "add-session:true",  // AddSessionInterceptor拦截器中检查session(没有就请求)，并将session添加到请求体中
        "Content-Type:application/json"
    )
    @POST("/service")
    fun getServiceList(
        @Header("device_id") deviceId: String,
        @Header("nas_domain") ip: String,
        @Header("token") token: String,//CheckSessionInterceptor中 取值
        @Body body: RequestBody
    ): Observable<BaseProtocol<List<ServiceItem>>>

    /**
     * 获取设备nasVersion
     */
    @Headers(
        "add-session:false"  // AddSessionInterceptor拦截器中检查session(没有就请求)，并将session添加到请求体中
    )
    @GET("/version")
    fun getNasVersion(
        @Header("device_id") deviceId: String,
        @Header("nas_domain") ip: String
    ): Observable<BaseProtocol<NasVersion>>

    /**
     * 获取设备nasVersion
     */
    @Headers(
        "add-session:false"  // AddSessionInterceptor拦截器中检查session(没有就请求)，并将session添加到请求体中
    )
    @GET("/version")
    fun checkIsNasV3(
        @Header("device_id") deviceId: String,
        @Header("nas_domain") ip: String
    ): Call<BaseProtocol<NasVersion>>


    /*---磁盘操作接口   所有的操作的都是走这个-------------------------------------------------------------------------------*/
    /**
     *  打开磁盘电源
     */
    @Headers(
        "add-session:true",  // AddSessionInterceptor拦截器中检查session(没有就请求)，并将session添加到请求体中
        "Content-Type:application/json"
    )
    @POST("/disk")
    fun optDisk(
        @Header("device_id") deviceId: String,//用来记录保存session
        @Header("nas_domain") ip: String,
        @Header("token") token: String,//CheckSessionInterceptor中 取值
        @Body body: RequestBody//请求的参数
    ): Observable<BaseProtocol<Any>>

    /*---离线下载操作接口   所有的操作的都是走这个-------------------------------------------------------------------------------*/
    /**
     *  离线下载操作接口
     */
    @Headers(
        "add-session:true",  // AddSessionInterceptor拦截器中检查session(没有就请求)，并将session添加到请求体中
        "Content-Type:application/json"
    )
    @POST("/proxy")
    fun optDownloadOffline(
        @Header("device_id") deviceId: String,//用来记录保存session
        @Header("nas_domain") ip: String,
        @Header("token") token: String,//CheckSessionInterceptor中 取值
        @Body body: RequestBody//请求的参数
    ): Observable<BaseProtocol<Any>>

    /**
     * 获取/设置用户权限
     */
    @Headers(
        "add-session:true",  // AddSessionInterceptor拦截器中检查session(没有就请求)，并将session添加到请求体中
        "Content-Type:application/json"
    )
    @POST("/user")
    fun permission(
        @Header("device_id") deviceId: String,//用来记录保存session
        @Header("nas_domain") ip: String,
        @Header("token") token: String,//CheckSessionInterceptor中 取值
        @Body body: @JvmSuppressWildcards Map<String, Any>
    ): Observable<BaseProtocol<Any>>


    /*---离线下载操作接口   所有的操作的都是走这个-------------------------------------------------------------------------------*/
    /**
     *  离线下载操作接口
     */
    @Headers(
        "add-session:false" // AddSessionInterceptor拦截器中检查session(没有就请求)，并将session添加到请求体中
    )
    @POST("/proxy")
    @Multipart
    @JvmSuppressWildcards
    fun addDownloadOfflineTask(
        @Header("device_id") deviceId: String,//用来记录保存session
        @Header("nas_domain") ip: String,
        @Header("token") token: String,//CheckSessionInterceptor中 取值
        @PartMap descriptions: Map<String, RequestBody>
    ): Observable<BaseProtocol<Any>>

    /**
     * 获取tags
     */
    @Headers(
        "add-session:true",  // AddSessionInterceptor拦截器中检查session(没有就请求)，并将session添加到请求体中
        "Content-Type:application/json"
    )
    @POST("/file")
    fun tags(
        @Header("device_id") deviceId: String,//用来记录保存session
        @Header("nas_domain") ip: String,
        @Header("token") token: String,//CheckSessionInterceptor中 取值
        @Body body: @JvmSuppressWildcards Map<String, Any>
    ): Observable<BaseProtocol<List<FileTag>>>

    /**
     * 获取tags
     */
    @Headers(
        "add-session:true",  // AddSessionInterceptor拦截器中检查session(没有就请求)，并将session添加到请求体中
        "Content-Type:application/json"
    )
    @POST("/file")
    fun tagFiles(
        @Header("device_id") deviceId: String,//用来记录保存session
        @Header("nas_domain") ip: String,
        @Header("token") token: String,//CheckSessionInterceptor中 取值
        @Body body: @JvmSuppressWildcards Map<String, Any>
    ): Observable<BaseProtocol<Any>>


    /**
     * 获取tags
     */
    @Headers(
        "add-session:true",  // AddSessionInterceptor拦截器中检查session(没有就请求)，并将session添加到请求体中
        "Content-Type:application/json"
    )
    @POST("/file")
    fun fileOptTag(
        @Header("device_id") deviceId: String,//用来记录保存session
        @Header("nas_domain") ip: String,
        @Header("token") token: String,//CheckSessionInterceptor中 取值
        @Body body: @JvmSuppressWildcards Map<String, Any>
    ): Observable<BaseProtocol<OptTagFileError>>

    /*---保险箱操作接口   所有的操作的都是走这个-------------------------------------------------------------------------------*/
    /**
     * 获取保险箱状态
     */
    @Headers(
        "add-session:true",  // AddSessionInterceptor拦截器中检查session(没有就请求)，并将session添加到请求体中
        "Content-Type:application/json"
    )
    @POST("/safebox")
    fun optSafeBox(
        @Header("device_id") deviceId: String,//用来记录保存session
        @Header("nas_domain") ip: String,
        @Header("token") token: String,//CheckSessionInterceptor中 取值
        @Body body: RequestBody
    ): Observable<BaseProtocol<Any>>


    @Headers(
        "add-session:true",  // AddSessionInterceptor拦截器中检查session(没有就请求)，并将session添加到请求体中
        "Content-Type:application/json"
    )
    @POST("/safebox")
    @JvmSuppressWildcards
    fun querySafeBoxStatus(
        @Header("device_id") deviceId: String,//用来记录保存session
        @Header("nas_domain") ip: String,
        @Header("token") token: String,//CheckSessionInterceptor中 取值
        @Body body: RequestBody
    ): Observable<BaseProtocol<SafeBoxStatus>>

    @Headers(
        "add-session:true",  // AddSessionInterceptor拦截器中检查session(没有就请求)，并将session添加到请求体中
        "Content-Type:application/json"
    )
    @POST("/safebox")
    @JvmSuppressWildcards
    fun querySafeBoxCheckData(
        @Header("device_id") deviceId: String,//用来记录保存session
        @Header("nas_domain") ip: String,
        @Header("token") token: String,//CheckSessionInterceptor中 取值
        @Body body: RequestBody
    ): Observable<BaseProtocol<SafeBoxCheckData>>


    @Headers(
        "add-session:true",  // AddSessionInterceptor拦截器中检查session(没有就请求)，并将session添加到请求体中
        "Content-Type:application/json"
    )
    @POST("/group")
    @JvmSuppressWildcards
    fun optGroupSpace(
        @Header("device_id") deviceId: String,//用来记录保存session
        @Header("nas_domain") ip: String,
        @Header("token") token: String,//CheckSessionInterceptor中 取值
        @Body body: RequestBody
    ): Observable<BaseProtocol<Any>>

    @Headers(
        "add-session:true",  // AddSessionInterceptor拦截器中检查session(没有就请求)，并将session添加到请求体中
        "Content-Type:application/json"
    )
    @POST("/group")
    fun group(
        @Header("device_id") deviceId: String,//用来记录保存session
        @Header("nas_domain") ip: String,
        @Header("token") token: String,//CheckSessionInterceptor中 取值
        @Body body: @JvmSuppressWildcards Map<String, Any>
    ): Observable<BaseProtocol<Any>>

    /*---dlna-------------------------------------------------------------------------------*/
    @Headers(
        "add-session:true",  // AddSessionInterceptor拦截器中检查session(没有就请求)，并将session添加到请求体中
        "Content-Type:application/json"
    )
    @POST("/dlna")
    @JvmSuppressWildcards
    fun dlna(
        @Header("device_id") deviceId: String,//用来记录保存session
        @Header("nas_domain") ip: String,
        @Header("token") token: String,//CheckSessionInterceptor中 取值
        @Body body: RequestBody
    ): Observable<BaseProtocol<List<DLNAPathResult>>>

    @Headers(
        "add-session:true",  // AddSessionInterceptor拦截器中检查session(没有就请求)，并将session添加到请求体中
        "Content-Type:application/json"
    )
    @POST("/dlna")
    @JvmSuppressWildcards
    fun dlnaGetOption(
        @Header("device_id") deviceId: String,//用来记录保存session
        @Header("nas_domain") ip: String,
        @Header("token") token: String,//CheckSessionInterceptor中 取值
        @Body body: RequestBody
    ): Observable<BaseProtocol<DLNAOptionResult>>

    /*---samb-------------------------------------------------------------------------------*/
    @Headers(
        "add-session:true",  // AddSessionInterceptor拦截器中检查session(没有就请求)，并将session添加到请求体中
        "Content-Type:application/json"
    )
    @POST("/samba")
    @JvmSuppressWildcards
    fun samba(
        @Header("device_id") deviceId: String,//用来记录保存session
        @Header("nas_domain") ip: String,
        @Header("token") token: String,//CheckSessionInterceptor中 取值
        @Body body: RequestBody
    ): Observable<BaseProtocol<LanScanVisibleResult>>

    @Headers(
        "add-session:true",  // AddSessionInterceptor拦截器中检查session(没有就请求)，并将session添加到请求体中
        "Content-Type:application/json"
    )
    @POST("/service")
    fun optService(
        @Header("device_id") deviceId: String,
        @Header("nas_domain") ip: String,
        @Header("token") token: String,//CheckSessionInterceptor中 取值
        @Body body: RequestBody
    ): Observable<BaseProtocol<Any>>



    /**
     * 获取／设置简介
     */
    @Headers("add-session:true",  // AddSessionInterceptor拦截器中检查session(没有就请求)，并将session添加到请求体中
            "Content-Type:application/json")
    @POST("/briefintro")
    fun getBrief(
            @Header("device_id") deviceId: String,//用来记录保存session
            @Header("nas_domain") ip: String,
            @Header("token") token: String,//CheckSessionInterceptor中 取值
            @Body body: RequestBody
    ): Observable<BaseProtocol<Brief>>
    /**
     * 获取／设置简介
     */
    @Headers("add-session:true",  // AddSessionInterceptor拦截器中检查session(没有就请求)，并将session添加到请求体中
            "Content-Type:application/json")
    @POST("/briefintro")
    fun setBrief(
            @Header("device_id") deviceId: String,//用来记录保存session
            @Header("nas_domain") ip: String,
            @Header("token") token: String,//CheckSessionInterceptor中 取值
            @Body body: RequestBody
    ): Observable<BaseProtocol<BriefTimeStamp>>
}