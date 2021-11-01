package io.weline.repo.data.remote

import android.content.pm.ServiceInfo
import io.reactivex.Observable
import io.weline.repo.api.ApiService
import io.weline.repo.data.model.*
import okhttp3.MediaType
import okhttp3.RequestBody
import org.json.JSONObject

/**
 * @author Raleigh.Luo
 * date：20/9/18 18
 * describe：
 */
class ServiceRemoteSource(val apiService: ApiService) {

    fun getServiceStatus(deviceId: String, ip: String, token: String): Observable<BaseProtocol<List<ServiceStatus>>> {
        val json = JSONObject()
        val params = JSONObject()
        json.put("method", "status")
        json.put("params", params)
        val body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), json.toString())
        return apiService.getServiceStatus(deviceId, ip, token, body)
    }

    fun getServiceList(deviceId: String, ip: String, token: String): Observable<BaseProtocol<List<ServiceItem>>> {
        val json = JSONObject()
        val params = JSONObject()
        json.put("method", "list")
        json.put("params", params)
        val body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), json.toString())
        return apiService.getServiceList(deviceId, ip, token, body)
    }


    fun querySafeBoxStatus(deviceId: String, ip: String, token: String): Observable<BaseProtocol<SafeBoxStatus>> {
        val json = JSONObject()
        val params = JSONObject()
        json.put("method", "safebox")
        params.put("cmd", "status")
        json.put("params", params)
        val body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), json.toString())
        return apiService.querySafeBoxStatus(deviceId, ip, token, body)
    }


    fun initSafeBoxStatus(deviceId: String, ip: String, token: String, newQuestion: String, newAnswer: String, newKey: String): Observable<BaseProtocol<Any>> {
        val json = JSONObject()
        val params = JSONObject()
        json.put("method", "safebox")
        params.put("cmd", "init")
        params.put("newQuestion", newQuestion)
        params.put("newAnswer", newAnswer)
        params.put("newKey", newKey)
        json.put("params", params)
        val body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), json.toString())
        return apiService.optSafeBox(deviceId, ip, token, body)
    }


    fun unlockSafeBoxStatus(deviceId: String, ip: String, token: String, oldKey: String): Observable<BaseProtocol<Any>> {
        val json = JSONObject()
        val params = JSONObject()
        json.put("method", "safebox")
        params.put("cmd", "unlock")
        params.put("oldKey", oldKey)
        json.put("params", params)
        val body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), json.toString())
        return apiService.optSafeBox(deviceId, ip, token, body)
    }

    fun lockSafeBoxStatus(deviceId: String, ip: String, token: String): Observable<BaseProtocol<Any>> {
        val json = JSONObject()
        val params = JSONObject()
        json.put("method", "safebox")
        params.put("cmd", "lock")
        json.put("params", params)
        val body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), json.toString())
        return apiService.optSafeBox(deviceId, ip, token, body)
    }

    fun resetSafeBoxByOldKey(deviceId: String, ip: String, token: String, ranStr: String, newKey: String): Observable<BaseProtocol<Any>> {
        val json = JSONObject()
        val params = JSONObject()
        json.put("method", "safebox")
        params.put("cmd", "resetpw")
        params.put("ranStr", ranStr)
        params.put("newKey", newKey)
        json.put("params", params)
        val body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), json.toString())
        return apiService.optSafeBox(deviceId, ip, token, body)
    }


    fun resetSafeBoxQuestion(deviceId: String, ip: String, token: String, trans: String, newQuestion: String, newAnswer: String): Observable<BaseProtocol<Any>> {
        val json = JSONObject()
        val params = JSONObject()
        json.put("method", "safebox")
        params.put("cmd", "resetqa")
        params.put("ranStr", trans)
        params.put("newQuestion", newQuestion)
        params.put("newAnswer", newAnswer)
        json.put("params", params)
        val body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), json.toString())
        return apiService.optSafeBox(deviceId, ip, token, body)
    }

    fun resetSafeBox(deviceId: String, ip: String, token: String): Observable<BaseProtocol<Any>> {
        val json = JSONObject()
        val params = JSONObject()
        json.put("method", "safebox")
        params.put("cmd", "resetsafebox")
        json.put("params", params)
        val body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), json.toString())
        return apiService.optSafeBox(deviceId, ip, token, body)
    }

    fun checkSafeBoxOldPsw(deviceId: String, ip: String, token: String, oldPsw: String): Observable<BaseProtocol<SafeBoxCheckData>> {
        val json = JSONObject()
        val params = JSONObject()
        json.put("method", "safebox")
        params.put("cmd", "valpw")
        params.put("oldKey", oldPsw)
        json.put("params", params)
        val body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), json.toString())
        return apiService.querySafeBoxCheckData(deviceId, ip, token, body)

    }

    fun checkSafeBoxOldAnswer(deviceId: String, ip: String, token: String, oldAnswer: String): Observable<BaseProtocol<SafeBoxCheckData>> {
        val json = JSONObject()
        val params = JSONObject()
        json.put("method", "safebox")
        params.put("cmd", "valans")
        params.put("oldAnswer", oldAnswer)
        json.put("params", params)
        val body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), json.toString())
        return apiService.querySafeBoxCheckData(deviceId, ip, token, body)
    }


    fun optService(deviceId: String, ip: String, token: String, method: String, serviceId: Int): Observable<BaseProtocol<Any>> {
        val json = JSONObject()
        val params = JSONObject()
        json.put("method", method)
        params.put("id", serviceId)
        json.put("params", params)
        val body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), json.toString())
        return apiService.optService(deviceId, ip, token, body)
    }


}