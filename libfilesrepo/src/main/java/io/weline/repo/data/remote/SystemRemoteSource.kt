package io.weline.repo.data.remote

import io.reactivex.Observable
import io.weline.repo.api.ApiService
import io.weline.repo.data.model.*
import okhttp3.MediaType
import okhttp3.RequestBody
import org.json.JSONArray
import org.json.JSONObject

/**
 * @author Raleigh.Luo
 * date：20/9/18 18
 * describe：
 */
class SystemRemoteSource(val apiService: ApiService) {
    /**
     * 系统重启/关机
     */
    fun rebootOrHaltSystem(deviceId: String, ip: String, token: String, isPowerOff: Boolean): Observable<BaseProtocol<Any>> {
        val json = JSONObject()
        json.put("method", if (isPowerOff) "halt" else "reboot")
        val body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), json.toString())
        return apiService.optSystem(deviceId, ip, token, body)
    }

    /**
     * 获取磁盘smart信息情况
     */
    fun getHDSmartInforSystem(deviceId: String, ip: String, token: String): Observable<BaseProtocol<Any>> {
        val json = JSONObject()
        json.put("method", "hdsmart")
        val body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), json.toString())
        return apiService.optSystem(deviceId, ip, token, body)
    }


    /**
     * 获取磁盘smart信息情况
     */
    fun getDiskSmart7P16(deviceId: String, ip: String, token: String): Observable<BaseProtocol<Any>> {
        val json = JSONObject()
        json.put("method", "hdsmart")
        val params = JSONObject()
        json.put("params", params)
        val body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), json.toString())
        return apiService.optSystem(deviceId, ip, token, body)
    }

    /**
     * 获取磁盘smart信息情况
     */
    fun getHDSmartInforScan(deviceId: String, ip: String, token: String, dev: String): Observable<BaseProtocol<Any>> {
        val json = JSONObject()
        json.put("method", "hdsmart")
        val params = JSONObject()
        params.put("name", "scan")
        params.put("dev", dev)
        json.put("params", params)
        val body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), json.toString())
        return apiService.optSystem(deviceId, ip, token, body)
    }

    /**
     * 获取磁盘相关信息
     */
    fun getHDInforSystem(deviceId: String, ip: String, token: String): Observable<BaseProtocol<Any>> {
        val json = JSONObject()
        json.put("method", "hdsmart")
        val params = JSONObject()
        params.put("name", "hdinfo")
        json.put("params", params)
        val body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), json.toString())
        return apiService.optSystem(deviceId, ip, token, body)
    }



    /**
     * 格式化
     */
    fun formatSystem(deviceId: String, ip: String, token: String, cmd: String): Observable<BaseProtocol<Any>> {
        val json = JSONObject()
        json.put("method", "hdformat")
        val params = JSONObject()
        params.put("cmd", cmd)
        json.put("params", params)
        val body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), json.toString())
        return apiService.optSystem(deviceId, ip, token, body)
    }

    /**
     * 格式化状态
     * {"method":"hdformatstatus","session","xxx"}
     */
    fun formatHdStatus(deviceId: String, ip: String, token: String): Observable<BaseProtocol<Any>> {
        val json = JSONObject()
        json.put("method", "hdformatstatus")
        val body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), json.toString())
        return apiService.optSystem(deviceId, ip, token, body)
    }

    /**
     * 7.20 查看系统状态
     * Code	Msg
     *-40030	磁盘未挂载
     *-40012	磁盘正在格式化中…
     * */
    fun getSysStatus(ip: String): Observable<BaseProtocol<List<Int>>> {
        val json = JSONObject()
        json.put("method", "status")
        val body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), json.toString())
        return apiService.getSysStatus(ip, body)
    }

    fun initDisk(deviceId: String, ip: String, token: String, force: Int): Observable<BaseProtocol<Any>> {
        val json = JSONObject()
        val params = JSONObject()
        params.put("token", token)
        params.put("force", force)
        json.put("params", params)
        val body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), json.toString())
        return apiService.initDisk(deviceId, ip, body)
    }

    fun queryDiskStatus(deviceId: String, ip: String, token: String): Observable<BaseProtocol<DataDiskStatus>> {
        val json = JSONObject()
        val params = JSONObject()
        params.put("token", token)
        json.put("params", params)
        val body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), json.toString())
        return apiService.queryDiskStatus(deviceId, ip, body)
    }

    fun getNasVersion(deviceId: String, ip: String): Observable<BaseProtocol<NasVersion>> {
        return apiService.getNasVersion(deviceId, ip)
    }

    //slot	int	必须	槽位
    // Code	Msg
    //-40021	没有磁盘
    fun openDiskPower(deviceId: String, ip: String, token: String, slot: Int): Observable<BaseProtocol<Any>> {
        val json = JSONObject()
        json.put("method", "poweron")
        val params = JSONObject()
        params.put("slot", slot)
        json.put("params", params)
        val body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), json.toString())
        return apiService.optDisk(deviceId, ip, token, body)
    }

    //slot	int	必须	槽位
    //force	int	必须	是否强制关闭
    //Code	Msg
    //-40002	参数错误
    //-40021	沒有磁盘
    //-40034	磁盘raid不能关闭电源
    //-40035	lvm不能关闭电源
    fun closeDiskPower(deviceId: String, ip: String, token: String, slot: Int, force: Int): Observable<BaseProtocol<Any>> {
        val json = JSONObject()
        json.put("method", "poweroff")
        val params = JSONObject()
        params.put("slot", slot)
        params.put("force", force)
        json.put("params", params)
        val body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), json.toString())
        return apiService.optDisk(deviceId, ip, token, body)
    }

    //获取设备信息
    fun getDiskInfo(deviceId: String, ip: String, token: String): Observable<BaseProtocol<Any>> {
        val json = JSONObject()
        json.put("method", "info")
        val body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), json.toString())
        return apiService.optDisk(deviceId, ip, token, body)
    }

    //获取磁盘操作选项
    fun getDiskOperation(deviceId: String, ip: String, token: String): Observable<BaseProtocol<Any>> {
        val json = JSONObject()
        json.put("method", "option")
        val body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), json.toString())
        return apiService.optDisk(deviceId, ip, token, body)
    }


    //进行磁盘操作选项
    fun manageDisk(deviceId: String, ip: String, token: String, cmd: String, mode: String, force: Int, devices: List<String>
    ): Observable<BaseProtocol<Any>> {
        val json = JSONObject()
        json.put("method", "admin")
        val params = JSONObject()
        params.put("cmd", cmd)
        params.put("mode", mode)
        params.put("force", force)
        val jsonArray = JSONArray()
        devices.forEach {
            jsonArray.put(it)
        }
        params.put("devices", jsonArray)

        json.put("params", params)
        val body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), json.toString())
        return apiService.optDisk(deviceId, ip, token, body)
    }

    //磁盘操作后查询状态
    fun getDiskManageStatus(deviceId: String, ip: String, token: String
    ): Observable<BaseProtocol<Any>> {
        val json = JSONObject()
        json.put("method", "admin_status")
        val body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), json.toString())
        return apiService.optDisk(deviceId, ip, token, body)
    }


    //查询电源状态
    fun getDiskPowerStatus(deviceId: String, ip: String, token: String
    ): Observable<BaseProtocol<Any>> {
        val json = JSONObject()
        json.put("method", "pminfo")
        val body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), json.toString())
        return apiService.optDisk(deviceId, ip, token, body)
    }

    //查询电源状态
    fun startDiskSelfCheck(deviceId: String, ip: String, token: String
    ): Observable<BaseProtocol<Any>> {
        val json = JSONObject()
        json.put("method", "self_check")
        val body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), json.toString())
        return apiService.optDisk(deviceId, ip, token, body)
    }

    //查询电源状态
    fun getDiskCheckReport(deviceId: String, ip: String, token: String
    ): Observable<BaseProtocol<Any>> {
        val json = JSONObject()
        json.put("method", "self_check_report")
        val body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), json.toString())
        return apiService.optDisk(deviceId, ip, token, body)
    }


    //查询电源状态
    fun getGroupList(deviceId: String, ip: String, token: String
    ): Observable<BaseProtocol<Any>> {
        val json = JSONObject()
        json.put("method", "self_check_report")
        val body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), json.toString())
        return apiService.optDisk(deviceId, ip, token, body)
    }


    //查询硬件信息
    fun getHardwareInformation(deviceId: String, ip: String, token: String): Observable<BaseProtocol<Any>> {
        val json = JSONObject()
        json.put("method", "gethardinfo")
        val body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), json.toString())
        return apiService.optSystem(deviceId, ip, token, body)
    }
    //查询系统状态
    fun getSystemStatus(deviceId: String, ip: String, token: String): Observable<BaseProtocol<Any>> {
        val json = JSONObject()
        json.put("method", "getsysstatus")
        val body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), json.toString())
        return apiService.optSystem(deviceId, ip, token, body)
    }
    //dlna
    fun dlna(deviceId: String, ip: String, token: String,params: JSONObject
    ): Observable<BaseProtocol<List<DLNAPathResult>>> {
        val body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), params.toString())
        return apiService.dlna(deviceId, ip, token, body)
    }
    //dlna
    fun dlnaGetOption(deviceId: String, ip: String, token: String,params: JSONObject
    ): Observable<BaseProtocol<DLNAOptionResult>> {
        val body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), params.toString())
        return apiService.dlnaGetOption(deviceId, ip, token, body)
    }
    //samba
    fun samba(deviceId: String, ip: String, token: String,params: JSONObject
    ): Observable<BaseProtocol<LanScanVisibleResult>> {
        val body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), params.toString())
        return apiService.samba(deviceId, ip, token, body)
    }
}