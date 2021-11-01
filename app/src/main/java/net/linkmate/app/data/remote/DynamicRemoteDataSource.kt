package net.linkmate.app.data.remote

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.*
import android.provider.Settings
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import io.reactivex.Observable
import io.weline.repo.files.constant.AppConstants
import libs.source.common.livedata.ApiResponse
import net.linkmate.app.api.DynamicApiService
import net.linkmate.app.api.DynamicUploadApiService
import net.linkmate.app.base.MyApplication
import net.linkmate.app.data.model.Base
import net.linkmate.app.data.model.dynamic.*
import net.linkmate.app.net.RetrofitSingleton
import net.linkmate.app.service.DynamicQueue
import net.sdvn.common.internet.utils.LoginTokenUtil
import net.sdvn.common.vo.Dynamic
import net.sdvn.common.vo.DynamicComment
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.Call
import java.io.File
import java.io.IOException
import java.text.DecimalFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap


/**
 * @author Raleigh.Luo
 * date：20/12/24 16
 * describe：
 */
class DynamicRemoteDataSource() {
    private val apiUploadService = RetrofitSingleton.instance.getUploadRetrofit().create(DynamicUploadApiService::class.java)
    private val apiService = RetrofitSingleton.instance.getRetrofit().create(DynamicApiService::class.java)
    private val PORT = AppConstants.HS_DYNAMIC_PORT

    fun getDomain(ip: String): String {
        return String.format("http://%s:%s", ip, PORT)

    }

    /**
     * 获取动态列表, 获取lastTime之前的数据，且指定页数
     */
    fun login(ip: String): LiveData<ApiResponse<Login>> {
        val token = LoginTokenUtil.getToken()
        val json = JSONObject()
        json.put("token", token)
        val body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), json.toString())
        return apiService.loginLiveData(getDomain(ip), body)
    }

    /**
     * 获取动态列表, 获取lastTime之前的数据，且指定页数
     */
    fun getDynamicList(deviceId: String, ip: String, limitMaxTime: Long, pageSize: Int): LiveData<ApiResponse<DynamicList>> {
        val token = LoginTokenUtil.getToken()
        val lastTime = "<=" + limitMaxTime
        return apiService.getDynamicList(getDomain(ip), deviceId, token, lastTime, pageSize = pageSize)
    }

    /**
     * 获取动态列表, 获取最新数据
     */
    fun getNewestDynamicList(deviceId: String, ip: String, latest: Int = 10): LiveData<ApiResponse<DynamicList>> {
        val token = LoginTokenUtil.getToken()
        return apiService.getNewestDynamicList(getDomain(ip), deviceId, token, latest)
    }

    /**
     * 上传动态
     * 注意：上传动态因使用了上传进度监听，故不能使用HttpLoggingInterceptor 打印日志，HttpLoggingInterceptor中会writeTo一次requestbody，监听进度拦截器又会writeTo一次，重复导致写入翻倍异常
     */
    fun publishDynamicRx(deviceId: String, ip: String,
                         dynamic: Dynamic): Observable<DynamicDetail> {
        val body: HashMap<String, RequestBody> = hashMapOf()
        dynamic.Content?.let {
            body.put("content", RequestBody.create(MediaType.parse("text/plain"), it))
        }
        getLocation()?.let {
            body.put("location", RequestBody.create(MediaType.parse("text/plain"), it))
        }
        val files: ArrayList<MultipartBody.Part> = ArrayList()

        var attachIndex = 0
        val attachmentinfo = JSONArray()
        dynamic.AttachmentsPO.forEach {
            val file = File(it.localPath)
            val key = "attachment" + attachIndex
            val part = MultipartBody.Part.createFormData(key, file.getName(), RequestBody.create(MediaType.parse("file/*"), file))
            files.add(part)
            val json = JSONObject()
            json.put("index", attachIndex)
            json.put("size", it.size ?: 0L)
            json.put("name", it.name ?: "")
            json.put("cost", it.cost ?: "")
            attachmentinfo.put(json)
            attachIndex++
        }
        if (attachmentinfo.length() > 0) {
            body.put("attachmentinfo", RequestBody.create(MediaType.parse("text/plain"), attachmentinfo.toString()))
        }

        var mediaIndex = 0
        val mediainfo = JSONArray()
        dynamic.MediasPO.forEach {
            val file = File(it.localPath)
            //如media1-video
            val key = "media" + mediaIndex
            // Content-Disposition: form-data; name="media0"; filename="IMG_20201222_10:05:00.jpg"
            // Content-Type: multipart/form-data
            val part = MultipartBody.Part.createFormData(key, file.getName(), RequestBody.create(MediaType.parse("file/*"), file))
            files.add(part)
            val json = JSONObject()
            json.put("index", mediaIndex)
            json.put("type", it.type ?: "")
            json.put("width", it.width ?: 0)
            json.put("height", it.height ?: 0)
            mediainfo.put(json)
            mediaIndex++
        }

        if (mediainfo.length() > 0) {
            body.put("mediainfo", RequestBody.create(MediaType.parse("text/plain"), mediainfo.toString()))
        }
        val identification = DynamicQueue.PUBLISH_DYNAMIC_TYPE + "/" + dynamic.autoIncreaseId
        return apiUploadService.publishDynamicRx(getDomain(ip), deviceId, LoginTokenUtil.getToken(), identification, body, files)
    }

    /**
     * 获取指定动态数组
     */
    fun getDynamicsRx(deviceId: String, ip: String, momentids: JSONArray): Observable<DynamicList> {
        val token = LoginTokenUtil.getToken()
        val json = JSONObject()
        json.put("momentids", momentids)
        val requestbody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), json.toString())
        return apiService.getDynamics(getDomain(ip), deviceId, token, requestbody)
    }

    /**
     * 获取动态
     */
    fun getDynamic(deviceId: String, ip: String, id: Long): LiveData<ApiResponse<DynamicList>> {
        val token = LoginTokenUtil.getToken()
        val json = JSONObject()
        json.put("momentids", JSONArray().put(id))
        val requestbody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), json.toString())
        return apiService.getDynamic(getDomain(ip), deviceId, token, requestbody)
    }

    /**
     * 获取动态
     */
    fun getDynamicRx(deviceId: String, ip: String, id: Long): Observable<DynamicList> {
        val token = LoginTokenUtil.getToken()
        val json = JSONObject()
        json.put("momentids", JSONArray().put(id))
        val requestbody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), json.toString())
        return apiService.getDynamicsRx(getDomain(ip), deviceId, token, requestbody)
    }

    /**
     * 删除动态
     */
    fun deleteDynamicRx(deviceId: String, ip: String, dynamicId: Long): Observable<Base> {
        val token = LoginTokenUtil.getToken()
        return apiService.deleteDynamicRx(getDomain(ip), deviceId, token, dynamicId)
    }


    /**
     * 发布评论
     */
    fun publishCommentRx(deviceId: String,
                         ip: String,
                         publishComment: DynamicComment
    ): Observable<DynamicCommentDetail> {
        val token = LoginTokenUtil.getToken()
        val params = JSONObject()
        params.put("content", publishComment.content)
//        publishComment.id?.let {
//            params.put("id", publishComment.id)
//        }
        params.put("momentid", publishComment.momentID)
        publishComment.targetUID?.let {
            params.put("targetuid", publishComment.targetUID)
        }
        publishComment.targetUserName?.let {
            params.put("targetusername", publishComment.targetUserName)
        }
        val body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), params.toString())
        return apiService.publishCommentRx(getDomain(ip), deviceId, token, body)
    }

    /**
     * 删除评论
     */
    fun deleteComment(deviceId: String, ip: String, id: Long): LiveData<ApiResponse<Base>> {
        val token = LoginTokenUtil.getToken()
        return apiService.deleteComment(getDomain(ip), deviceId, token, id)
    }

    /**
     * 删除评论
     */
    fun deleteCommentRx(deviceId: String, ip: String, id: Long): Observable<Base> {
        val token = LoginTokenUtil.getToken()
        return apiService.deleteCommentRx(getDomain(ip), deviceId, token, id)
    }

    /**
     * 点赞
     */
    fun like(deviceId: String, ip: String, id: Long): LiveData<ApiResponse<Base>> {
        val token = LoginTokenUtil.getToken()
        val params = JSONObject()
        params.put("momentid", id)
        val body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), params.toString())
        return apiService.like(getDomain(ip), deviceId, token, body)
    }

    /**
     * 点赞
     */
    fun likeRx(deviceId: String, ip: String, id: Long): Observable<DynamicLikeDetail> {
        val token = LoginTokenUtil.getToken()
        val params = JSONObject()
        params.put("momentid", id)
        val body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), params.toString())
        return apiService.likeRx(getDomain(ip), deviceId, token, body)
    }

    /**
     * 点赞
     */
    fun unLike(deviceId: String, ip: String, id: Long): LiveData<ApiResponse<Base>> {
        val token = LoginTokenUtil.getToken()
        val params = JSONObject()
        params.put("momentid", id)
        val body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), params.toString())
        return apiService.unLike(getDomain(ip), deviceId, token, body)
    }

    /**
     * 点赞
     */
    fun unLikeRx(deviceId: String, ip: String, id: Long): Observable<Base> {
        val token = LoginTokenUtil.getToken()
        val params = JSONObject()
        params.put("momentid", id)
        val body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), params.toString())
        return apiService.unLikeRx(getDomain(ip), deviceId, token, body)
    }

    /**
     * 与我相关
     * isDelete可为空,默认为0,如为1则调用一次后会清空与我相关
     */
    fun getRelatedMessage(deviceId: String, ip: String, isDelete: Int): LiveData<ApiResponse<AboutMessage>> {
        val token = LoginTokenUtil.getToken()
        return apiService.getRelatedMessage(getDomain(ip), deviceId, token, isDelete)
    }

    /**
     * 与我相关
     * isDelete可为空,默认为0,如为1则调用一次后会清空与我相关
     */
    fun getRelatedList(deviceId: String, ip: String): LiveData<ApiResponse<RelatedList>> {
        val token = LoginTokenUtil.getToken()
        return apiService.getRelatedList(getDomain(ip), deviceId, token)
    }

    /**
     * 与我相关
     * isDelete可为空,默认为0,如为1则调用一次后会清空与我相关
     */
    fun getFile(deviceId: String, ip: String, fileName: String): Call<ResponseBody> {
        val token = LoginTokenUtil.getToken()
        return apiService.getFile(getDomain(ip), deviceId, token, fileName)
    }

    private fun getLocation(): String? {
        val mContext = MyApplication.getContext()
        //1.获取位置管理器
        val locationManager = mContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        var address:String? = null
        try {
            //添加用户权限申请判断
            if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) !== PackageManager.PERMISSION_GRANTED
                    && ContextCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) !== PackageManager.PERMISSION_GRANTED) {
                return address
            }
            //2.获取位置提供器，GPS或是NetWork
            // 获取所有可用的位置提供器
            val providerList: List<String> = locationManager.getProviders(true)
            var locationProvider: String? = null
            if (providerList.contains(LocationManager.GPS_PROVIDER)) {
                //GPS 定位的精准度比较高，但是非常耗电。
                locationProvider = LocationManager.GPS_PROVIDER
            } else if (providerList.contains(LocationManager.NETWORK_PROVIDER)) { //Google服务被墙不可用
                //网络定位的精准度稍差，但耗电量比较少。
                locationProvider = LocationManager.NETWORK_PROVIDER
//        } else {
//            println("=====NO_PROVIDER=====")
//            // 当没有可用的位置提供器时，弹出Toast提示用户
//            val intent = Intent()
//            intent.action = Settings.ACTION_LOCATION_SOURCE_SETTINGS
//            mContext.startActivity(intent)
            }
            //3.获取上次的位置，一般第一次运行，此值为null

            locationProvider?.let {
                var location = locationManager.getLastKnownLocation(it)

                if (location != null) {
                    address = getAddress(location.latitude, location.longitude)
                    // 显示当前设备的位置信息
                } else { //当GPS信号弱没获取到位置的时候可从网络获取
                    val locationManager = mContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager
                    val location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                    address = getAddress(location.latitude, location.longitude)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return address

    }

    private fun getAddress(latitude: Double, longitude: Double): String {
        val mContext = MyApplication.getContext()
        //Geocoder通过经纬度获取具体信息
        val gc = Geocoder(mContext, Locale.getDefault())
        var addressText = ""
        try {
            val locationList: List<Address> = gc.getFromLocation(latitude, longitude, 1)
            if (locationList != null && locationList.size > 0) {
                val address: Address = locationList[0]
//                val countryName: String? = address.getCountryName() //国家
//                val countryCode: String? = address.getCountryCode()
//                val adminArea: String? = address.getAdminArea() //省
//                val locality: String? = address.getLocality() //市
//                val subLocality: String? = address.getSubLocality() //区
//                val featureName: String? = address.getFeatureName() //街道

                addressText = address.getAddressLine(0)
//                var i = 0
//                while (address.getAddressLine(i) != null) {
//                    val addressLine: String = address.getAddressLine(i)
//                    //街道名称:广东省深圳市罗湖区蔡屋围一街深圳瑞吉酒店
//                    println("addressLine=====$addressLine")
//                    i++
//                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return addressText
    }

}