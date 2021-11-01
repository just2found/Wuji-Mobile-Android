package io.weline.repo.data.remote

import io.reactivex.Observable
import io.weline.repo.api.ApiService
import io.weline.repo.data.model.*
import io.weline.repo.files.data.SharePathType
import net.sdvn.common.repo.BriefRepo
import okhttp3.MediaType
import okhttp3.RequestBody
import org.json.JSONArray
import org.json.JSONObject

/**
 * @author Raleigh.Luo
 * date：20/9/17 13
 * describe：
 */
class UserRemoteSource(val apiService: ApiService) {
    /**
     * 获取用户列表
     */
    fun getUserList(deviceId: String, ip: String, token: String): Observable<BaseProtocol<Users>> {
        val json = JSONObject()
        json.put("method", "list")
        //session 在拦截器中添加
        json.put("params", JSONObject().put("type", "all"))
        val body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), json.toString())
        return apiService.getUserList(deviceId, ip, token, body)
    }

    /**
     * 添加用户
     */
    fun addUser(deviceId: String, ip: String, token: String, username: String,
                password: String, admin: Int): Observable<BaseProtocol<Any>> {
        val json = JSONObject()
        json.put("method", "add")
        val params = JSONObject()
        params.put("username", username)
        params.put("password", password)
        params.put("admin", admin)
        json.put("params", params)
        val body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), json.toString())
        return apiService.optUser(deviceId, ip, token, body)
    }

    /**
     * 删除用户
     */
    fun deleteUser(deviceId: String, ip: String, token: String, username: String): Observable<BaseProtocol<Any>> {
        val json = JSONObject()
        json.put("method", "delete")
        val params = JSONObject()
        params.put("username", username)
        json.put("params", params)
        val body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), json.toString())
        return apiService.optUser(deviceId, ip, token, body)
    }

    /**
     * 更新用户密码
     */
    fun updateUserPassword(deviceId: String, ip: String, token: String, username: String, password: String): Observable<BaseProtocol<Any>> {
        val json = JSONObject()
        json.put("method", "update")
        val params = JSONObject()
        params.put("username", username)
        params.put("password", password)
        json.put("params", params)
        val body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), json.toString())
        return apiService.optUser(deviceId, ip, token, body)
    }

    /**
     * 清除用户
     */
    fun clearUser(deviceId: String, ip: String, token: String): Observable<BaseProtocol<Any>> {
        val json = JSONObject()
        json.put("method", "clear")
        val body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), json.toString())
        return apiService.optUser(deviceId, ip, token, body)
    }


    /**
     *  设置用户备注
     */
    fun setUserMark(deviceId: String, ip: String, token: String, username: String, mark: String): Observable<BaseProtocol<Any>> {
        val json = JSONObject()
        json.put("method", "mark")
        val params = JSONObject()
        params.put("username", username)
        params.put("mark", mark)
        json.put("params", params)
        val body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), json.toString())
        return apiService.optUser(deviceId, ip, token, body)
    }

    /**
     *  设置设备备注
     */
    fun setDeviceMark(deviceId: String, ip: String, token: String, markName: String?, markDesc: String?): Observable<BaseProtocol<Any>> {
        val json = JSONObject()
        json.put("method", "device")
        val params = JSONObject()
        if (!markName.isNullOrEmpty())
            params.put("name", markName)
        if (!markDesc.isNullOrEmpty())
            params.put("desc", markDesc)
        json.put("params", params)
        val body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), json.toString())
        return apiService.optUser(deviceId, ip, token, body)
    }

    fun getDevMark(deviceId: String, ip: String, token: String): Observable<BaseProtocol<DataDevMark>> {
        val map = mapOf<String, String>("method" to "device")
        return apiService.userDevMark(deviceId, ip, token, map);
    }

    /**
     * 获取用户使用空间
     */
    fun getUserSpace(deviceId: String, ip: String, token: String, username: String): Observable<BaseProtocol<UserSpace>> {
        /**
         * username 如果为空，就是当前用户
         * space 如果为空 ，功能就是获取当前用户的space
         */
        val json = JSONObject()
        json.put("method", "space")
        val params = JSONObject()
        params.put("username", username)
        json.put("params", params)
        val body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), json.toString())
        return apiService.userSpace(deviceId, ip, token, body)
    }

    /**
     * 获取用户使用空间
     */
    fun setUserSpace(deviceId: String, ip: String, token: String, username: String, space: Long): Observable<BaseProtocol<UserSpace>> {
        /**
         * username 如果为空，就是当前用户
         * space 如果为空 ，功能就是获取当前用户的space
         */
        val json = JSONObject()
        json.put("method", "space")
        val params = JSONObject()
        params.put("username", username)
        params.put("space", space)
        json.put("params", params)
        val body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), json.toString())
        return apiService.userSpace(deviceId, ip, token, body)
    }

    fun setSysInfo(deviceId: String, ip: String, token: String, key: String, value: String, level: Int): Observable<BaseProtocol<Any>> {
        val map = hashMapOf<String, Any>()
        map.put("method", "sysinfo")
        val params = hashMapOf<String, Any>()
        params.put("cmd", "set")
        params.put("name", key)
        params.put("value", value)
        params.put("level", level)
        map.put("params", params)
        return apiService.setSysInfo(deviceId, ip, token, true, map)
    }

    fun getSysInfo(deviceId: String, ip: String, token: String, addSession: Boolean = false, key: String, level: Int): Observable<BaseProtocol<DataSysInfoItem>> {
        val map = hashMapOf<String, Any>()
        map.put("method", "sysinfo")
        val params = hashMapOf<String, Any>()
        params.put("cmd", "get")
        params.put("name", key)
        params.put("level", level)
        map.put("params", params)
        return apiService.sysInfo(deviceId, ip, token, addSession, map)
    }

    fun getAllSysInfo(deviceId: String, ip: String, token: String, addSession: Boolean = false, level: Int): Observable<BaseProtocol<List<DataSysInfoItem>>> {
        val map = hashMapOf<String, Any>()
        map.put("method", "sysinfo")
        val params = hashMapOf<String, Any>()
        params.put("cmd", "getall")
        params.put("level", level)
        map.put("params", params)
        return apiService.sysInfoAll(deviceId, ip, token, addSession, map)
    }

    fun setPermission(deviceId: String, ip: String, token: String, username: String, sharePathType: SharePathType, perm: Int): Observable<BaseProtocol<Any>> {
//            "method": "permission",
//            "session": "{{session}}",
//            "params": {
//            "username": "13066867956",
//            "share_path_type": 0,
//            "perm": 3
        val map = hashMapOf<String, Any>()
        map.put("method", "permission")
        val params = hashMapOf<String, Any>()
        params.put("username", username)
        params.put("share_path_type", sharePathType.type)
        params.put("perm", perm)
        map.put("params", params)
        return apiService.permission(deviceId, ip, token, map)
    }

    fun getUserInfo(deviceId: String, ip: String, token: String, username: String): Observable<BaseProtocol<User>> {
        /**
         * username 如果为空，就是当前用户
         */
        val json = JSONObject()
        json.put("method", "info")
        val params = JSONObject()
        params.put("username", username)
        json.put("params", params)
        val body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), json.toString())
        return apiService.getUserInfo(deviceId, ip, token, body)
    }

    /****----简介-------------------------------*****/
    /**设置简介
     * @param type //1 设置背景 2设置头像 3设置文本
     * @param for //device/circle 设备或圈子
     * @param data //文本或图片的二进制数据的base64编码，图片只支持png 或者jpg
     */
    fun setBrief(deviceId: String, ip: String, token: String, type: Int, For: String, data: String): Observable<BaseProtocol<BriefTimeStamp>> {
        /**
         * username 如果为空，就是当前用户
         */
        val json = JSONObject()
        json.put("method", "set")
        val params = JSONObject()
        params.put("type", type)
        params.put("for", For)//device/circle 设备或圈子
        params.put("data", data)//文本或图片的二进制数据的base64编码，图片只支持png 或者jpg
        json.put("params", params)
        val body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), json.toString())
        return apiService.setBrief(deviceId, ip, token, body)
    }

    /**
     * 获取简介
     * @param type //1 背景 2 头像 3 文字 4 所有
     * @param For //device/circle 设备或圈子
     * @param timestamp //如果type==4，则需要提交各部分的最后时间戳
     */
    fun getBrief(deviceId: String, ip: String, token: String, type: Int, For: String, backgroudTimestamp: Long? = null,
                 avatarTimestamp: Long? = null, text: Long? = null): Observable<BaseProtocol<Brief>> {
        /**getB
         * username 如果为空，就是当前用户
         */
        val json = JSONObject()
        json.put("method", "get")
        val params = JSONObject()
        params.put("type", type)//1 背景 2 头像 3 文字 4 所有
        params.put("for", For)//device/circle 设备或圈子
        val timestamp = JSONObject()
        timestamp.put("bg", backgroudTimestamp)
        timestamp.put("avatar", avatarTimestamp)
        timestamp.put("text", text)
        params.put("update_at", timestamp)//如果type==ALL_TYPE，则需要提交各部分的最后时间戳
        json.put("params", params)
        val body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), json.toString())
        return apiService.getBrief(deviceId, ip, token, body)
    }

}