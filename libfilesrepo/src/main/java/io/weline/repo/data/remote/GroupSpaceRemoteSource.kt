package io.weline.repo.data.remote

import io.reactivex.Observable
import io.weline.repo.api.ApiService
import io.weline.repo.data.model.BaseProtocol
import okhttp3.MediaType
import okhttp3.RequestBody
import org.json.JSONObject

/**
create by: 86136
create time: 2021/6/1 10:24
Function description:
 */

class GroupSpaceRemoteSource(val apiService: ApiService) {


    //获取设备信息
    fun getGroupListJoined(
        deviceId: String,
        ip: String,
        token: String
    ): Observable<BaseProtocol<Any>> {
        val json = JSONObject()
        json.put("method", "list_joined")
        val body =
            RequestBody.create(MediaType.parse("application/json; charset=utf-8"), json.toString())
        return apiService.optGroupSpace(deviceId, ip, token, body)
    }

    //获取设备信息
    fun createGroupSpace(
        deviceId: String,
        ip: String,
        token: String,
        groupName: String
    ): Observable<BaseProtocol<Any>> {
        val json = JSONObject()
        val params = JSONObject()
        json.put("method", "create")
        params.put("name", groupName)
        json.put("params", params)
        val body =
            RequestBody.create(MediaType.parse("application/json; charset=utf-8"), json.toString())
        return apiService.optGroupSpace(deviceId, ip, token, body)
    }

    fun getGroupAnnouncementHistory(
        deviceId: String,
        ip: String,
        token: String,
        groupId: Long
    ): Observable<BaseProtocol<Any>> {
        val json = JSONObject()
        val params = JSONObject()
        json.put("method", "get_group_notice_history")
        params.put("id", groupId)
        json.put("params", params)
        val body =
            RequestBody.create(MediaType.parse("application/json; charset=utf-8"), json.toString())
        return apiService.optGroupSpace(deviceId, ip, token, body)
    }

    fun publishAnnouncement(
        deviceId: String,
        ip: String,
        token: String,
        groupId: Long,
        content: String
    ): Observable<BaseProtocol<Any>> {
        val json = JSONObject()
        val params = JSONObject()
        json.put("method", "add_group_notice")
        params.put("id", groupId)
        params.put("notice", content)
        json.put("params", params)
        val body =
            RequestBody.create(MediaType.parse("application/json; charset=utf-8"), json.toString())
        return apiService.optGroupSpace(deviceId, ip, token, body)
    }

}