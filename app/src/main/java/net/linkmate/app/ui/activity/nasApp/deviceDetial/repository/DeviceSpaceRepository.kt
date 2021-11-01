package net.linkmate.app.ui.activity.nasApp.deviceDetial.repository

import androidx.arch.core.util.Function
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.google.gson.internal.LinkedTreeMap
import com.google.gson.reflect.TypeToken
import io.reactivex.disposables.Disposable
import io.weline.repo.data.model.BaseProtocol
import io.weline.repo.net.V5Observer
import io.weline.repo.repository.V5Repository
import libs.source.common.livedata.Resource
import net.linkmate.app.R
import net.linkmate.app.base.MyApplication
import net.linkmate.app.ui.simplestyle.device.disk.data.*
import net.linkmate.app.ui.simplestyle.device.self_check.data.*
import net.linkmate.app.util.ToastUtils
import net.sdvn.common.internet.utils.LoginTokenUtil
import net.sdvn.nascommon.CustomLoaderStateListener
import net.sdvn.nascommon.SessionManager
import net.sdvn.nascommon.constant.AppConstants
import net.sdvn.nascommon.iface.GetSessionListener
import net.sdvn.nascommon.model.oneos.OneOSHardDisk
import net.sdvn.nascommon.model.oneos.api.sys.OneOSHardDiskInfoAPI
import net.sdvn.nascommon.model.oneos.api.sys.OneOSSpaceAPI
import net.sdvn.nascommon.model.oneos.user.LoginSession
import net.sdvn.nascommon.utils.FileUtils
import java.util.*
import kotlin.collections.ArrayList

/**
 * @author Raleigh.Luo
 * date：20/7/29 11
 * describe：
 */
class DeviceSpaceRepository {

    fun queryHDInfo(deviceid: String, host: String, hardDisk1: OneOSHardDisk?, hardDisk2: OneOSHardDisk?, callBack: Function<Int?, Void?>) {
        val hdInfoAPI = OneOSHardDiskInfoAPI(host)
        val listener = object : OneOSHardDiskInfoAPI.OnHDInfoListener {
            override fun onStart(url: String) {}
            override fun onSuccess(url: String, model: String?, hd1: OneOSHardDisk?, hd2: OneOSHardDisk?) {
                if (hd1 != null || hd2 != null) {
                    var diskCount = 0
                    if (hd1?.getSerial() != null) diskCount++
                    if (hd2?.getSerial() != null) diskCount++
                    callBack.apply(diskCount)
                }
            }

            override fun onFailure(url: String, errorNo: Int, errorMsg: String) {
                callBack.apply(null)
            }
        }
        hdInfoAPI.setOnHDInfoListener(listener)
        val observer = object : V5Observer<Any>(deviceid) {
            override fun success(result: BaseProtocol<Any>) {
                var hd1: OneOSHardDisk? = hardDisk1 ?: OneOSHardDisk()
                var hd2: OneOSHardDisk? = hardDisk2 ?: OneOSHardDisk()
                val mode = hdInfoAPI.getHDInfor(Gson().toJson(result.data), hd1, hd2)
                listener.onSuccess("", mode, hd1, hd2)
            }

            override fun fail(result: BaseProtocol<Any>) {
                listener.onFailure("", result.error?.code ?: 0, result.error?.msg ?: "")
            }

            override fun isNotV5() {
                hdInfoAPI.query(hardDisk1, hardDisk2)
            }

            override fun retry(): Boolean {
                V5Repository.INSTANCE().getHDInforSystem(deviceid, host, LoginTokenUtil.getToken(), this)
                return true
            }
        }
        V5Repository.INSTANCE().getHDInforSystem(deviceid, host, LoginTokenUtil.getToken(), observer)
    }

    fun querySpace(loaderStateListener: CustomLoaderStateListener,
                   deviceid: String, host: String, isM8: Boolean,
                   spaceValues: HashMap<Int, String>, callback: Function<Boolean, Void?>,
                   isCanClearDisk: Function<Int?, Void?>
    ) {
        SessionManager.getInstance().getLoginSession(deviceid, object : GetSessionListener() {
            override fun onSuccess(url: String?, loginSession: LoginSession) {
                val spaceAPI2 = OneOSSpaceAPI(loginSession)
                val listener = object : OneOSSpaceAPI.OnSpaceListener {
                    override fun onStart(url: String) {
                    }

                    override fun onSuccess(url: String, isOneOSSpace: Boolean, hd1: OneOSHardDisk, hd2: OneOSHardDisk?) {
                        if (loaderStateListener.isCanceled()) return

                        val total = hd1.total
                        val free = hd1.free
                        val used = hd1.used
                        var totalInfo = FileUtils.fmtFileSize(total)
                        var freeInfo = FileUtils.fmtFileSize(free)
                        var usedInfo = FileUtils.fmtFileSize(total - free)
                        if (isM8) {
                            usedInfo = String.format("%s/%s", freeInfo, totalInfo)
                            totalInfo = FileUtils.fmtFileSize(total + AppConstants.M8_SYSTEM_SPACE)
                            freeInfo = FileUtils.fmtFileSize(AppConstants.M8_SYSTEM_SPACE)
                        }
                        spaceValues.put(0, totalInfo)
                        spaceValues.put(1, freeInfo)
                        spaceValues.put(2, usedInfo)
                        callback.apply(true)
                        queryHDInfo(deviceid, host, hd1, hd2, isCanClearDisk)
                    }

                    override fun onFailure(url: String, errorNo: Int, errorMsg: String) {
                        faulure()
                    }
                }

                val observer = object : V5Observer<Any>(deviceid) {
                    override fun onSubscribe(d: Disposable) {
                        super.onSubscribe(d)
                        loaderStateListener.onLoadStart(d)
                    }

                    override fun success(result: BaseProtocol<Any>) {
                        val hd1 = OneOSHardDisk()
                        val hd2 = OneOSHardDisk()
                        spaceAPI2.getHDInfo(Gson().toJson(result.data), hd1, hd2)
                        listener.onSuccess("", true, hd1, hd2)
                    }

                    override fun fail(result: BaseProtocol<Any>) {
                        listener.onFailure("", result.error?.code ?: 0, result.error?.msg ?: "")
                    }

                    override fun isNotV5() {//调用旧接口
                        spaceAPI2.setOnSpaceListener(listener)
                        spaceAPI2.query(true)
                        loaderStateListener.onLoadStart(spaceAPI2)
                    }

                    override fun retry(): Boolean {
                        V5Repository.INSTANCE().getHDSmartInforSystem(deviceid, loginSession.ip, LoginTokenUtil.getToken(), this)
                        return true
                    }
                }
                V5Repository.INSTANCE().getHDSmartInforSystem(deviceid, loginSession.ip, LoginTokenUtil.getToken(), observer)

            }

            override fun onFailure(url: String?, errorNo: Int, errorMsg: String?) {
                if (loaderStateListener.isCanceled()) return
                super.onFailure(url, errorNo, errorMsg)
                faulure()
            }

            private fun faulure() {
                queryHDInfo(deviceid, host, null, null, isCanClearDisk)
                spaceValues.put(0, getString(R.string.query_space_failure))
                spaceValues.put(1, getString(R.string.query_space_failure))
                spaceValues.put(2, getString(R.string.query_space_failure))
                callback.apply(false)
            }
        })
    }

    fun getString(id: Int): String {
        return MyApplication.getInstance().resources.getString(id)
    }

    fun getPercentage(z: Long, m: Long): Int {
        var i = (z.toDouble() / m.toDouble() * 100).toInt()
        if (i == 0) {
            i++
        }
        return i
    }

    fun querySpace(deviceid: String, isM8: Boolean): LiveData<Resource<DiskSpaceOverview>> {
        val liveData = MutableLiveData<Resource<DiskSpaceOverview>>()
        SessionManager.getInstance().getLoginSession(deviceid, object : GetSessionListener() {
            override fun onSuccess(url: String?, loginSession: LoginSession) {
                val spaceAPI2 = OneOSSpaceAPI(loginSession)
                val listener = object : OneOSSpaceAPI.OnSpaceListener {
                    override fun onStart(url: String) {
                    }

                    override fun onSuccess(url: String, isOneOSSpace: Boolean, hd1: OneOSHardDisk, hd2: OneOSHardDisk?) {
                        var total = hd1.total
                        val free = hd1.free
                        val used = hd1.used

                        var totalInfo = FileUtils.fmtFileSize(total)
                        var freeInfo = FileUtils.fmtFileSize(free)
                        var usedInfo = FileUtils.fmtFileSize(total - free)
                        var systemInfo: String? = null
                        var list: List<Int>
                        if (isM8) {
                            //usedInfo = String.format("%s/%s", freeInfo, totalInfo)
                            total += AppConstants.M8_SYSTEM_SPACE
                            totalInfo = FileUtils.fmtFileSize(total)
                            //freeInfo = FileUtils.fmtFileSize(AppConstants.M8_SYSTEM_SPACE)
                            systemInfo = FileUtils.fmtFileSize(AppConstants.M8_SYSTEM_SPACE)
                            //系统空间，使用空间 剩余空间
                            list = listOf(getPercentage(AppConstants.M8_SYSTEM_SPACE, total), getPercentage(used, total), getPercentage(free, total))
                        } else {
                            list = listOf(getPercentage(used, total), getPercentage(free, total))
                        }
                        val diskSpaceOverview = DiskSpaceOverview(totalInfo, systemInfo, usedInfo, freeInfo, list)
                        liveData.postValue(Resource.success(diskSpaceOverview))
                    }

                    override fun onFailure(url: String, errorNo: Int, errorMsg: String) {
                        faulure()
                    }
                }

                val observer = object : V5Observer<Any>(deviceid) {
                    override fun success(result: BaseProtocol<Any>) {
                        val hd1 = OneOSHardDisk()
                        val hd2 = OneOSHardDisk()
                        spaceAPI2.getHDInfo(Gson().toJson(result.data), hd1, hd2)
                        listener.onSuccess("", true, hd1, hd2)
                    }

                    override fun fail(result: BaseProtocol<Any>) {
                        listener.onFailure("", result.error?.code ?: 0, result.error?.msg ?: "")
                    }

                    override fun isNotV5() {//调用旧接口
                        spaceAPI2.setOnSpaceListener(listener)
                        spaceAPI2.query(true)
                    }

                    override fun retry(): Boolean {
                        V5Repository.INSTANCE().getHDSmartInforSystem(deviceid, loginSession.ip, LoginTokenUtil.getToken(), this)
                        return true
                    }
                }
                V5Repository.INSTANCE().getHDSmartInforSystem(deviceid, loginSession.ip, LoginTokenUtil.getToken(), observer)

            }

            override fun onFailure(url: String?, errorNo: Int, errorMsg: String?) {
                super.onFailure(url, errorNo, errorMsg)
                faulure()
            }

            private fun faulure() {
                liveData.postValue(Resource.error("", null))
            }
        })
        return liveData
    }

    fun getDiskActionItem(deviceid: String): LiveData<Resource<List<ActionItem>>> {
        val liveData = MutableLiveData<Resource<List<ActionItem>>>()
        SessionManager.getInstance().getLoginSession(deviceid, object : GetSessionListener() {
            override fun onSuccess(url: String?, loginSession: LoginSession) {
                V5Repository.INSTANCE().getDiskOperation(deviceid, loginSession.ip, LoginTokenUtil.getToken(), object : V5Observer<Any>(deviceid) {
                    override fun isNotV5() {
                    }

                    override fun retry(): Boolean {
                        V5Repository.INSTANCE().getDiskInfo(deviceid, loginSession.ip, LoginTokenUtil.getToken(), this)
                        return true
                    }

                    override fun success(result: BaseProtocol<Any>) {
                        val gson = Gson()
                        result.data?.let {
                            try {
                                val dataStr = gson.toJson(result.data)
                                val objectType = object : TypeToken<List<ActionItem>>() {}.type
                                val data = gson.fromJson<List<ActionItem>>(dataStr, objectType)
                                liveData.postValue(Resource.success(data))
                            } catch (e: Exception) {
                                liveData.postValue(Resource.error("", null))
                            }
                        }
                    }

                    override fun fail(result: BaseProtocol<Any>) {
                        liveData.postValue(Resource.error(result?.error?.msg ?: "", null));
                    }
                })
            }

            override fun onFailure(url: String?, errorNo: Int, errorMsg: String?) {
                super.onFailure(url, errorNo, errorMsg)
                liveData.postValue(Resource.error("", null))
            }

        })
        return liveData
    }

    fun createDiskActionItem(deviceid: String, mode: String, devices: List<String>, cmd: String): LiveData<Resource<Boolean>> {
        val liveData = MutableLiveData<Resource<Boolean>>()
        SessionManager.getInstance().getLoginSession(deviceid, object : GetSessionListener() {
            override fun onSuccess(url: String?, loginSession: LoginSession) {
                V5Repository.INSTANCE().manageDisk(deviceid, loginSession.ip, LoginTokenUtil.getToken(), cmd,
                        mode, 1, devices, object : V5Observer<Any>(deviceid) {
                    override fun isNotV5() {
                    }

                    override fun retry(): Boolean {
                        V5Repository.INSTANCE().manageDisk(deviceid, loginSession.ip, LoginTokenUtil.getToken(), "create",
                                mode, 1, devices, this)
                        return true
                    }

                    override fun success(result: BaseProtocol<Any>) {
                        liveData.postValue(Resource.success(result.result))
                    }

                    override fun fail(result: BaseProtocol<Any>) {
                        liveData.postValue(Resource.error(result?.error?.msg ?: "", null));
                    }
                })
            }

            override fun onFailure(url: String?, errorNo: Int, errorMsg: String?) {
                super.onFailure(url, errorNo, errorMsg)
                liveData.postValue(Resource.error("", null))
            }

        })
        return liveData;
    }


    fun getDiskManageStatus(deviceid: String): LiveData<Resource<Boolean>> {
        val liveData = MutableLiveData<Resource<Boolean>>()
        SessionManager.getInstance().getLoginSession(deviceid, object : GetSessionListener() {
            override fun onSuccess(url: String?, loginSession: LoginSession) {
                V5Repository.INSTANCE().getDiskManageStatus(deviceid, loginSession.ip, LoginTokenUtil.getToken(), object : V5Observer<Any>(deviceid) {
                    override fun isNotV5() {
                    }

                    override fun retry(): Boolean {
                        V5Repository.INSTANCE().getDiskManageStatus(deviceid, loginSession.ip, LoginTokenUtil.getToken(), this)
                        return true
                    }

                    override fun success(result: BaseProtocol<Any>) {
                        if (result.result) {
                            val gson = Gson()
                            result.data?.let {
                                try {
                                    val dataStr = gson.toJson(result.data)
                                    val objectType = object : TypeToken<OptResult>() {}.type
                                    val data = gson.fromJson<OptResult>(dataStr, objectType)
                                    when (data.status) {
                                        "ok" -> {
                                            liveData.postValue(Resource.success(true))
                                        }
                                        "failed" -> {
                                            liveData.postValue(Resource.success(false))
                                        }
                                        "running" -> {
                                            liveData.postValue(Resource.loading(true))
                                        }
                                        else -> {
                                            liveData.postValue(Resource.error("", null))
                                        }
                                    }
                                } catch (e: Exception) {
                                    liveData.postValue(Resource.error("", null))
                                }
                            }
                        }
                    }

                    override fun fail(result: BaseProtocol<Any>) {
                        liveData.postValue(Resource.error(result.error?.msg ?: "", null))
                    }
                })
            }

            override fun onFailure(url: String?, errorNo: Int, errorMsg: String?) {
                super.onFailure(url, errorNo, errorMsg)
                liveData.postValue(Resource.error(errorMsg ?: "", null))
            }

        })
        return liveData;
    }


    fun queryDiskNodeInfo(deviceid: String): LiveData<Resource<List<DiskNode>>> {
        val liveData = MutableLiveData<Resource<List<DiskNode>>>()
        SessionManager.getInstance().getLoginSession(deviceid, object : GetSessionListener() {
            override fun onSuccess(url: String?, loginSession: LoginSession) {
                V5Repository.INSTANCE().getDiskInfo(deviceid, loginSession.ip, LoginTokenUtil.getToken(), object : V5Observer<Any>(deviceid) {
                    override fun isNotV5() {

                    }

                    override fun retry(): Boolean {
                        V5Repository.INSTANCE().getDiskInfo(deviceid, loginSession.ip, LoginTokenUtil.getToken(), this)
                        return true
                    }

                    override fun success(result: BaseProtocol<Any>) {
                        val gson = Gson()
                        result.data?.let {
                            try {
                                val dataStr = gson.toJson(result.data)
                                val objectType = object : TypeToken<List<DiskNode>>() {}.type
                                val data = gson.fromJson<List<DiskNode>>(dataStr, objectType)
                                liveData.postValue(Resource.success(data))
                            } catch (e: Exception) {
                                liveData.postValue(Resource.error("", null))
                            }
                        }
                        if (result.data == null) {
                            liveData.postValue(Resource.success(null))
                        }
                    }

                    override fun fail(result: BaseProtocol<Any>) {
                        liveData.postValue(Resource.error(result?.error?.msg
                                ?: "", null));
                    }
                })
            }

            override fun onFailure(url: String?, errorNo: Int, errorMsg: String?) {
                super.onFailure(url, errorNo, errorMsg)
                liveData.postValue(Resource.error("", null))
            }

        })
        return liveData;
    }


    fun getDiskPowerStatus(deviceid: String): LiveData<Resource<List<DiskPower>>> {
        val liveData = MutableLiveData<Resource<List<DiskPower>>>()
        SessionManager.getInstance().getLoginSession(deviceid, object : GetSessionListener() {
            override fun onSuccess(url: String?, loginSession: LoginSession) {
                V5Repository.INSTANCE().getDiskPowerStatus(deviceid, loginSession.ip, LoginTokenUtil.getToken(), object : V5Observer<Any>(deviceid) {
                    override fun isNotV5() {
                    }

                    override fun retry(): Boolean {
                        V5Repository.INSTANCE().getDiskPowerStatus(deviceid, loginSession.ip, LoginTokenUtil.getToken(), this)
                        return true
                    }

                    override fun success(result: BaseProtocol<Any>) {
                        val gson = Gson()
                        result.data?.let {
                            try {
                                val dataStr = gson.toJson(result.data)
                                val objectType = object : TypeToken<List<DiskPower>>() {}.type
                                val data = gson.fromJson<List<DiskPower>>(dataStr, objectType)
                                liveData.postValue(Resource.success(data))
                            } catch (e: Exception) {
                                liveData.postValue(Resource.error("", null))
                            }

                        }
                    }

                    override fun fail(result: BaseProtocol<Any>) {
                        liveData.postValue(Resource.error(result?.error?.msg
                                ?: "", null));
                    }
                })
            }

            override fun onFailure(url: String?, errorNo: Int, errorMsg: String?) {
                super.onFailure(url, errorNo, errorMsg)
                liveData.postValue(Resource.error("", null))
            }

        })
        return liveData;
    }


    fun startDiskSelfCheck(deviceid: String): LiveData<Resource<Boolean>> {
        val liveData = MutableLiveData<Resource<Boolean>>()
        SessionManager.getInstance().getLoginSession(deviceid, object : GetSessionListener() {
            override fun onSuccess(url: String?, loginSession: LoginSession) {
                V5Repository.INSTANCE().startDiskSelfCheck(deviceid, loginSession.ip, LoginTokenUtil.getToken(), object : V5Observer<Any>(deviceid) {
                    override fun isNotV5() {
                    }

                    override fun retry(): Boolean {
                        V5Repository.INSTANCE().startDiskSelfCheck(deviceid, loginSession.ip, LoginTokenUtil.getToken(), this)
                        return true
                    }

                    override fun success(result: BaseProtocol<Any>) {
                        liveData.postValue(Resource.success(result.result))
                    }

                    override fun fail(result: BaseProtocol<Any>) {
                        liveData.postValue(Resource.error(result?.error?.msg
                                ?: "", null, code = result?.error?.code ?: 0));
                    }
                })
            }

            override fun onFailure(url: String?, errorNo: Int, errorMsg: String?) {
                super.onFailure(url, errorNo, errorMsg)
                liveData.postValue(Resource.error("", null, errorNo))
            }

        })
        return liveData;
    }


    fun getDiskCheckReport(deviceid: String): LiveData<Resource<DiskCheckInfoResult>> {
        val liveData = MutableLiveData<Resource<DiskCheckInfoResult>>()
        SessionManager.getInstance().getLoginSession(deviceid, object : GetSessionListener() {
            override fun onSuccess(url: String?, loginSession: LoginSession) {
                V5Repository.INSTANCE().getDiskCheckReport(deviceid, loginSession.ip, LoginTokenUtil.getToken(), object : V5Observer<Any>(deviceid) {
                    override fun isNotV5() {
                    }

                    override fun retry(): Boolean {
                        V5Repository.INSTANCE().getDiskCheckReport(deviceid, loginSession.ip, LoginTokenUtil.getToken(), this)
                        return true
                    }

                    override fun success(result: BaseProtocol<Any>) {
                        val gson = Gson()
                        result.data?.let {
                            try {
                                val dataStr = gson.toJson(result.data)
                                val objectType = object : TypeToken<DiskCheckInfoResult>() {}.type
                                val data = gson.fromJson<DiskCheckInfoResult>(dataStr, objectType)
                                liveData.postValue(Resource.success(data))
                            } catch (e: Exception) {
                                liveData.postValue(Resource.error("", null, 12))
                            }

                        }
                    }

                    override fun fail(result: BaseProtocol<Any>) {
                        liveData.postValue(Resource.error(result?.error?.msg
                                ?: "", null, code = result?.error?.code ?: 0));
                    }
                })
            }

            override fun onFailure(url: String?, errorNo: Int, errorMsg: String?) {
                super.onFailure(url, errorNo, errorMsg)
                liveData.postValue(Resource.error("", null, errorNo))
            }

        })
        return liveData;
    }

    //7.16接口返回数据信息
    fun getHDSmartInforSystem(deviceid: String): LiveData<Resource<HDSmartInforSystem>> {
        val liveData = MutableLiveData<Resource<HDSmartInforSystem>>()
        SessionManager.getInstance().getLoginSession(deviceid, object : GetSessionListener() {
            override fun onSuccess(url: String?, loginSession: LoginSession) {
                V5Repository.INSTANCE().getHDSmartInforSystem(deviceid, loginSession.ip, LoginTokenUtil.getToken(), object : V5Observer<Any>(deviceid) {
                    override fun isNotV5() {
                    }

                    override fun retry(): Boolean {
                        V5Repository.INSTANCE().getHDSmartInforSystem(deviceid, loginSession.ip, LoginTokenUtil.getToken(), this)
                        return true
                    }

                    override fun success(result: BaseProtocol<Any>) {
                        val gson = Gson()
                        result.data?.let {
                            try {
                                val dataStr = gson.toJson(result.data)
                                val objectType = object : TypeToken<HDSmartInforSystem>() {}.type
                                val data = gson.fromJson<HDSmartInforSystem>(dataStr, objectType)
                                liveData.postValue(Resource.success(data))
                            } catch (e: Exception) {
                                liveData.postValue(Resource.error("", null))
                            }
                        }
                    }

                    override fun fail(result: BaseProtocol<Any>) {
                        val gson = Gson()
                        result.data?.let {
                            try {
                                val dataStr = gson.toJson(result.data)
                                val objectType = object : TypeToken<HDSmartInforSystem>() {}.type
                                val data = gson.fromJson<HDSmartInforSystem>(dataStr, objectType)
                                liveData.postValue(Resource.success(data))
                            } catch (e: Exception) {
                                liveData.postValue(Resource.error(result?.error?.msg
                                        ?: "", null))
                            }
                        }


                    }
                })
            }

            override fun onFailure(url: String?, errorNo: Int, errorMsg: String?) {
                super.onFailure(url, errorNo, errorMsg)
                liveData.postValue(Resource.error("", null))
            }

        })
        return liveData;
    }

    fun getHDSmartInforScan(deviceid: String, dev: String): LiveData<Resource<HDSmartInfoScan>> {
        val liveData = MutableLiveData<Resource<HDSmartInfoScan>>()
        SessionManager.getInstance().getLoginSession(deviceid, object : GetSessionListener() {
            override fun onSuccess(url: String?, loginSession: LoginSession) {
                V5Repository.INSTANCE().getHDSmartInforScan(deviceid, loginSession.ip, LoginTokenUtil.getToken(), dev, object : V5Observer<Any>(deviceid) {
                    override fun isNotV5() {
                    }

                    override fun retry(): Boolean {
                        V5Repository.INSTANCE().getHDSmartInforScan(deviceid, loginSession.ip, LoginTokenUtil.getToken(), dev, this)
                        return true
                    }

                    override fun success(result: BaseProtocol<Any>) {
                        val gson = Gson()
                        result.data?.let {
                            try {
                                val dataStr = gson.toJson(result.data)
                                val objectType = object : TypeToken<HDSmartInfoScan>() {}.type
                                val data = gson.fromJson<HDSmartInfoScan>(dataStr, objectType)
                                liveData.postValue(Resource.success(data))
                            } catch (e: Exception) {
                                liveData.postValue(Resource.error("", null))
                            }
                        }
                    }

                    override fun fail(result: BaseProtocol<Any>) {
                        liveData.postValue(Resource.error(result?.error?.msg
                                ?: "", null));
                    }
                })
            }

            override fun onFailure(url: String?, errorNo: Int, errorMsg: String?) {
                super.onFailure(url, errorNo, errorMsg)
                liveData.postValue(Resource.error("", null))
            }

        })
        return liveData;
    }

    fun getHDSmartInforScanAll(deviceid: String): LiveData<Resource<HDSmartInfoScanAll>> {
        val liveData = MutableLiveData<Resource<HDSmartInfoScanAll>>()
        SessionManager.getInstance().getLoginSession(deviceid, object : GetSessionListener() {
            override fun onSuccess(url: String?, loginSession: LoginSession) {
                V5Repository.INSTANCE().getHDInforSystem(deviceid, loginSession.ip, LoginTokenUtil.getToken(), object : V5Observer<Any>(deviceid) {
                    override fun isNotV5() {
                    }

                    override fun retry(): Boolean {
                        V5Repository.INSTANCE().getHDInforSystem(deviceid, loginSession.ip, LoginTokenUtil.getToken(), this)
                        return true
                    }

                    override fun success(result: BaseProtocol<Any>) {
                        val gson = Gson()
                        result.data?.let {
                            try {
                                val dataStr = gson.toJson(result.data)
                                val objectType = object : TypeToken<HDSmartInfoScanAll>() {}.type
                                val data = gson.fromJson<HDSmartInfoScanAll>(dataStr, objectType)
                                liveData.postValue(Resource.success(data))
                            } catch (e: Exception) {
                                liveData.postValue(Resource.error("", null))
                            }
                        }
                    }

                    override fun fail(result: BaseProtocol<Any>) {
                        liveData.postValue(Resource.error(result?.error?.msg
                                ?: "", null));
                    }
                })
            }

            override fun onFailure(url: String?, errorNo: Int, errorMsg: String?) {
                super.onFailure(url, errorNo, errorMsg)
                liveData.postValue(Resource.error("", null))
            }

        })
        return liveData;
    }

}