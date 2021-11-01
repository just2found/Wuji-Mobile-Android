package net.sdvn.nascommon.viewmodel

import android.content.Context
import android.os.SystemClock
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.switchMap
import com.google.gson.Gson
import io.reactivex.Observable
import io.reactivex.ObservableOnSubscribe
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.functions.BiFunction
import io.reactivex.schedulers.Schedulers
import io.weline.devhelper.DevTypeHelper
import io.weline.repo.SessionCache
import io.weline.repo.api.*
import io.weline.repo.data.model.BaseProtocol
import io.weline.repo.data.model.Brief
import io.weline.repo.data.model.Error
import io.weline.repo.data.model.ServiceStatus
import io.weline.repo.net.NasSessionExpException
import io.weline.repo.net.V5Observer
import io.weline.repo.repository.V5Repository
import libs.source.common.livedata.Resource
import net.sdvn.cmapi.Device
import net.sdvn.common.exception.DeviceNotFountException
import net.sdvn.common.internet.SdvnHttpErrorNo
import net.sdvn.common.internet.core.GsonBaseProtocol
import net.sdvn.common.internet.core.HttpLoader.HttpLoaderStateListener
import net.sdvn.common.internet.listener.CommonResultListener
import net.sdvn.common.internet.listener.ResultListener
import net.sdvn.common.internet.loader.AuthQRCodeHttpLoader
import net.sdvn.common.internet.loader.DeviceClearBindInfoHttpLoader
import net.sdvn.common.internet.loader.UnbindDeviceHttpLoader
import net.sdvn.common.internet.protocol.UnbindDeviceResult
import net.sdvn.common.internet.utils.LoginTokenUtil
import net.sdvn.common.repo.BriefRepo
import net.sdvn.nascommon.SessionManager
import net.sdvn.nascommon.constant.AppConstants
import net.sdvn.nascommon.constant.HttpErrorNo
import net.sdvn.nascommon.db.UserInfoKeeper
import net.sdvn.nascommon.fileserver.FileShareBaseResult
import net.sdvn.nascommon.fileserver.FileShareHelper
import net.sdvn.nascommon.fileserver.data.DataShareVersion
import net.sdvn.nascommon.iface.Callback
import net.sdvn.nascommon.iface.GetSessionListener
import net.sdvn.nascommon.iface.Result
import net.sdvn.nascommon.model.DeviceModel
import net.sdvn.nascommon.model.UiUtils
import net.sdvn.nascommon.model.oneos.OneOSHardDisk
import net.sdvn.nascommon.model.oneos.OneStat
import net.sdvn.nascommon.model.oneos.api.GetSystemStatApi
import net.sdvn.nascommon.model.oneos.api.OneOSDeviceInfoAPI
import net.sdvn.nascommon.model.oneos.api.OneOSDeviceInfoAPI.OnDeviceInfoListener
import net.sdvn.nascommon.model.oneos.api.OneOSDeviceInfoAPI.SubInfo
import net.sdvn.nascommon.model.oneos.api.sys.OneOSHardDiskInfoAPI
import net.sdvn.nascommon.model.oneos.api.user.OneOSUserManageAPI
import net.sdvn.nascommon.model.oneos.api.user.OneOSUserManageAPI.OnUserManageListener
import net.sdvn.nascommon.model.oneos.user.LoginSession
import net.sdvn.nascommon.receiver.NetworkStateManager
import net.sdvn.nascommon.rx.RetryWhenHandler
import net.sdvn.nascommon.utils.*
import net.sdvn.nascommonlib.R
import java.io.IOException
import java.util.*

class DeviceViewModel : RxViewModel() {
    /***－－设备简介－－－－－－－－－－**/
    private val startGetDeviceBrief = MutableLiveData<String>()
    fun startGetDeviceBrief(deviceId: String) {
        if (startGetDeviceBrief.value == null || startGetDeviceBrief.value != deviceId) {
            startGetDeviceBrief.value = deviceId
        }
    }

    val deviceBrief = startGetDeviceBrief.switchMap {
        BriefRepo.getBriefLiveData(it, BriefRepo.FOR_DEVICE)
    }

    private val mObserver: Observer<List<DeviceModel>>
    val liveDevices = MutableLiveData<List<DeviceModel>>()
    private val mapOfDevLoginRetry = hashMapOf<String, Boolean>()
    fun updateDevices(listener: HttpLoaderStateListener?) {
        SessionManager.getInstance().updateDeviceModels(listener)
    }

    val mapsDevName = hashMapOf<String, String>()

    init {
        mObserver = Observer { deviceModels ->
            liveDevices.postValue(deviceModels)
            deviceModels?.forEach { devModel ->
                mapsDevName.put(devModel.devId, devModel.devName)
                if (!devModel.isOnline) {
                    mapOfDevLoginRetry.remove(devModel.devId)
                }
            }
        }
        SessionManager.getInstance().registerDeviceDataObserver(mObserver)
    }

    override fun onCleared() {
        super.onCleared()
        SessionManager.getInstance().unregisterDeviceDataObserver(mObserver)
    }

    /**
     * 设置设备名称，类似于用户给设备定义的备注
     */
    fun setDeviceName(deviceName: String?, deviceId: String) {
        val user = SessionManager.getInstance().username
        if (user != null) {
            UserInfoKeeper.saveDevMarkInfo(user, deviceId, deviceName, null, true)
        }
        val deviceModels = liveDevices.value
        if (deviceModels != null) for (mDeviceModel in deviceModels) {
            if (mDeviceModel != null && mDeviceModel.devId == deviceId) {
                mDeviceModel.devName = deviceName ?: ""
                SessionManager.getInstance().getLoginSession(deviceId, object : GetSessionListener() {
                    override fun onSuccess(url: String, loginSession: LoginSession) {
                        val oneOSDeviceInfoAPI = OneOSDeviceInfoAPI(loginSession)
                        oneOSDeviceInfoAPI.setListener(object : OnDeviceInfoListener {
                            override fun onStart(url: String) {}
                            override fun onSuccess(url: String, info: SubInfo) {
                                if (user != null) {
                                    UserInfoKeeper.saveDevMarkInfo(user, deviceId, deviceName, null, false)
                                }
                                liveDevices.postValue(deviceModels)
                            }

                            override fun onFailure(url: String, errorNo: Int, errorMsg: String) {
                            }
                        })
                        oneOSDeviceInfoAPI.update(deviceName, null)
                    }
                })
                break
            }
        }
    }

    fun showDeviceName(context: Context, deviceId: String, content: String?, listener: Callback<String>?) {
        DialogUtils.showEditDialog(context, R.string.tip_set_device_name,
                R.string.hint_set_device_name, content,
                R.string.max_name_length,
                R.string.confirm, R.string.cancel) { dialog, isPositiveBtn, mContentEditText ->
            if (isPositiveBtn) {
                val newName = mContentEditText.text.toString().trim { it <= ' ' }
                if (EmptyUtils.isEmpty(newName) || newName.length > 16) {
                    AnimUtils.sharkEditText(context, mContentEditText)
                } else {
                    setDeviceName(newName, deviceId)
                    InputMethodUtils.hideKeyboard(context, mContentEditText)
                    listener?.result(newName)
                    dialog.dismiss()
                }
            } else {
                InputMethodUtils.hideKeyboard(context, mContentEditText)
                dialog.dismiss()
            }
        }
    }

    fun authSignInRequest(map: HashMap<String?, Any?>) {
        val authQRCodeHttpLoader = AuthQRCodeHttpLoader(GsonBaseProtocol::class.java)
        authQRCodeHttpLoader.setParams(map["uuid"] as String?)
        authQRCodeHttpLoader.executor(object : CommonResultListener<GsonBaseProtocol>() {
            override fun success(tag: Any?, mBaseProtocol: GsonBaseProtocol) {
                ToastHelper.showToast(R.string.success)
            }

            override fun error(tag: Any?, mErrorProtocol: GsonBaseProtocol) {
                ToastHelper.showToast(SdvnHttpErrorNo.ec2String(mErrorProtocol.result))
            }
        })
    }

    fun toLogin(dev: Device, isAuto: Boolean): Observable<Resource<Result<*>>> {
        if (!NetworkStateManager.instance.isNetAvailable()) {
            return Observable.just(Resource.error("not net",
                    Result<Any>(HttpErrorNo.EC_NOT_NET, "not net")))
        }
        if (!NetworkStateManager.instance.isEstablished()) {
            return Observable.just(Resource.error("service was disconnected",
                    Result<Any>(HttpErrorNo.ERR_SERVER_DISCONNECT, "service was disconnected")))
        }
        val countNum = arrayOf("0")
        val deviceId = dev.id
        val isFirst = mapOfDevLoginRetry[deviceId]

        val retryCount = if (isFirst != true) {
            mapOfDevLoginRetry.put(deviceId, true)
            20
        } else {
            1
        }
        val max = 60 * 1000
        val start = SystemClock.uptimeMillis()
        val hdInfo = GetSystemStatApi(dev.vip)
                .observer()
                .retryWhen(RetryWhenHandler(retryCount))
                .flatMap { data: OneStat? ->
                    if (data == null) {
                        return@flatMap Observable.just(Result<Any>(502, "Bad Gatway"))
                    }
                    var countHd = 0
                    try {
                        data.hd?.msg?.forEach {
                            if (!it.name.isNullOrEmpty()) {
                                countHd++
                            }
                        }
                        countNum[0] = countHd.toString()

                    } catch (ignore: Exception) {
                    }
                    return@flatMap Observable.just(Result(data))
                }

        val versionObservable = Observable.create<FileShareBaseResult<DataShareVersion>?> { em ->
            FileShareHelper.version(dev.vip)
                    .subscribe({
                        em.onNext(it)
                    }, {
//                        ConnectException
                        val apply: FileShareBaseResult<DataShareVersion> = FileShareBaseResult()
                        apply.status = -404
                        apply.msg = it.message
                        em.onNext(apply)
                    })
        }.retryWhen(RetryWhenHandler(retryCount))
                .flatMap {
                    Observable.just(if (it.isSuccessful) {
                        Result(true)
                    } else {
                        Result(it.status, it.msg)
                    })
                }
        val hdInfoV5 = Observable.create<Result<*>> {
            V5Repository.INSTANCE().getHDInforSystem(deviceId,
                    dev.vip, LoginTokenUtil.getToken(), object : io.reactivex.Observer<BaseProtocol<Any>> {
                override fun onComplete() {
                    it.onComplete()
                }

                override fun onSubscribe(d: Disposable) {

                }

                override fun onNext(result: BaseProtocol<Any>) {
                    if (result.result) {
                        val hd1: OneOSHardDisk? = OneOSHardDisk()
                        val hd2: OneOSHardDisk? = OneOSHardDisk()
                        val mode = OneOSHardDiskInfoAPI(dev.vip).getHDInfor(Gson().toJson(result.data), hd1, hd2)
                        var diskCount = 0
                        if (hd1?.getSerial() != null) diskCount++
                        if (hd2?.getSerial() != null) diskCount++
//                        Result(HttpErrorNo.ERR_ONESERVER_HDERROR, countNum[0])
                        it.onNext(Result(diskCount))
                    } else {
                        it.onNext(Result<Any>(result.error?.code
                                ?: HttpErrorNo.UNKNOWN_EXCEPTION, result.error?.msg
                                ?: "get hd info failed"))
                    }

                }

                override fun onError(e: Throwable) {
                    it.onError(e)
                }
            })
        }

        val login = Observable.create(ObservableOnSubscribe<Result<*>> { emitter ->
            SessionManager.getInstance().getLoginSession(deviceId, object : GetSessionListener(false) {
                override fun onSuccess(url: String, loginSession: LoginSession) {
                    emitter.onNext(Result(loginSession))
                }

                override fun onFailure(url: String, errorNo: Int, errorMsg: String) {
                    super.onFailure(url, errorNo, errorMsg)
                    emitter.onNext(Result<Any?>(errorNo, errorMsg))
                }
            })
        }).flatMap { result ->
            if (result.isSuccess) {
                return@flatMap Observable.just(result)
            } else {
                if (result.code == V5_ERR_DISK_NOT_MOUNTED) {
                    return@flatMap Observable.just(result)
                } else {
                    if (retryCount != 1) {
                        if (SystemClock.uptimeMillis() - start < 0.8 * max) {
                            throw IOException()
                        } else {
                            return@flatMap Observable.just(result)
                        }
                    } else {
                        return@flatMap Observable.just(result)
                    }
                }
            }
        }.retryWhen(RetryWhenHandler(retryCount))


        return Observable.create<Boolean> {
            val isWebApi: Boolean = SessionCache.instance.isV5SyncRequest(dev.id, dev.vip)
            it.onNext(isWebApi)
        }.flatMap { isV5 ->
            //nas 3.0 版本单独处理
            if (SessionCache.instance.isNasV3(deviceId)) {
                return@flatMap login.concatMap { result ->
                    if (result.isSuccess) {
                        V5Repository.INSTANCE()
                                .getServiceStatus(deviceId, dev.vip, LoginTokenUtil.getToken())
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .flatMap {
                                    Observable.just(if (it.result && it.data != null) {
                                        Result(it.data)
                                    } else {
                                        if (it.error?.code == V5_ERR_SESSION_EXP) {
                                            SessionManager.getInstance().removeSession(deviceId)
                                            throw NasSessionExpException()
                                        }
                                        Result<List<ServiceStatus>?>(it.error?.code
                                                ?: HttpErrorNo.UNKNOWN_EXCEPTION, it.error?.msg)
                                    })
                                }
                                .retryWhen(RetryWhenHandler(retryCount))
                                .flatMap { resultStatus ->
                                    return@flatMap getResultByServiceStatus(dev, resultStatus)
                                }
                    } else {
                        Observable.just(Resource.error("Other errors", result))
                    }
                }
            } else if (isV5 || DevTypeHelper.isWebApi(devClass = dev.devClass)
                    || UiUtils.isAndroidTV(dev.devClass)) {
                val devStatus = Observable.create<BaseProtocol<List<Int>>?> { em ->
                    V5Repository.INSTANCE()
                            .getSysStatus(dev.vip)
                            .subscribe({
                                em.onNext(it)
                            }, {
                                em.onNext(BaseProtocol(false, error = Error(HttpErrorNo.UNKNOWN_EXCEPTION, it.message), data = null))
                            })
                }.flatMap {
                    Observable.just(if (it.result) {
                        Result(it.data)
                    } else {
                        Result(it.error?.code
                                ?: HttpErrorNo.UNKNOWN_EXCEPTION, it.error?.msg
                                ?: "protocol error")
                    })
                }
                val observable = if (isV5 || DevTypeHelper.isWebApi(devClass = dev.devClass)) {
                    devStatus
                } else {
                    versionObservable
                }
                return@flatMap Observable.zip(observable, login, BiFunction<Result<*>, Result<*>, Resource<Result<*>>> { resultSysStatus, result ->
                    val deviceModel = SessionManager.getInstance().getDeviceModel(deviceId)
                            ?: return@BiFunction Resource.error("NOT_FOUND_DEVICE", Result<Any>(HttpErrorNo.ERR_ONESERVER_DEVICE_OFFLINE, dev.name))
                    if (result.isSuccess) {
                        getSuccessResult(deviceModel)
                    } else if (resultSysStatus.isSuccess) {
                        val sysStatusData = resultSysStatus.data
                        if (sysStatusData != null && sysStatusData is List<*>) {
                            val find = sysStatusData.find {
                                it == V5_ERR_DISK_NOT_MOUNTED
                            }
                            if (find != null) {
                                return@BiFunction if (deviceModel.isOwner) {
                                    Resource.error("SATA_NEED_FORMAT", Result<Any>(HttpErrorNo.ERR_ONESERVER_HDERROR, 1.toString()))
                                } else {
                                    Resource.error("Other errors", Result<Any>(HttpErrorNo.ERR_DEVICE_STATUS, countNum[0]))
                                }
                            }
                        }
                        Resource.error("Other errors", result)
                    } else {
                        Resource.error("Other errors", result)
                    }
                })
            } else {
                return@flatMap Observable.zip(hdInfo, login, object : BiFunction<Result<*>, Result<*>, Resource<Result<*>>> {
                    override fun apply(resultStatus: Result<*>, result: Result<*>): Resource<Result<*>> {
                        return if (result.isSuccess && resultStatus.isSuccess) {
                            val loginSession = result.data as LoginSession
                            val deviceModel = SessionManager.getInstance().getDeviceModel(deviceId)
                                    ?: return Resource.error("NOT_FOUND_DEVICE", Result<Any>(HttpErrorNo.ERR_ONESERVER_DEVICE_OFFLINE, dev.name))
                            val hdCount = countNum[0].toInt()
                            val oneStat = resultStatus.data as OneStat?
                            val isHdOk = oneStat?.hd?.isOk == true
                            val isMysqlOk = oneStat?.mysql?.isOk == true
                            loginSession.isHDStatusEnable = isHdOk and isMysqlOk
                            if ((isHdOk && isMysqlOk)) {
                                getSuccessResult(deviceModel)
                            } else {
                                if (hdCount > 0) {
                                    if (deviceModel.isOwner) {
                                        Resource.error("SATA_NEED_FORMAT", Result(HttpErrorNo.ERR_ONESERVER_HDERROR, countNum[0]))
                                    } else if (!isMysqlOk) {
                                        Resource.error("ONE_MySQL_ERROR", Result<Any>(HttpErrorNo.ERR_DEVICE_MYSQL_STATUS, "SATA_NEED_REBOOT | RESET"))
                                    } else {
                                        Resource.error("Other errors", Result(HttpErrorNo.ERR_DEVICE_STATUS, countNum[0]))
                                    }
                                } else {
                                    Resource.error("ONE_NO_SATA", Result<Any>(HttpErrorNo.ERR_ONE_NO_SATA, "SATA_NEED_FORMAT"))
                                }

                            }
                        } else {
                            Resource.error("Other errors", result)
                        }
                    }
                })
            }

        }


    }

    private fun getResultByServiceStatus(dev: Device, resultStatus: Result<List<ServiceStatus>?>): Observable<Resource<Result<*>>> {
        val resource = if (resultStatus.isSuccess) {
            val deviceModel = SessionManager.getInstance().getDeviceModel(dev.id)
            if (deviceModel == null) {
                Resource.error("NOT_FOUND_DEVICE", Result<Any>(HttpErrorNo.ERR_ONESERVER_DEVICE_OFFLINE, dev.name))
            } else {
                val list = resultStatus.data!!
                var fileServer: ServiceStatus? = null
                list.forEach {
                    if (it.isFileService()) {
                        fileServer = it
                    }
                    if (it.isFileShareService()) {
                        deviceModel.loginSession?.isShareV2Available = it.isAvailable()
                    }
                }
                if (fileServer?.isAvailable() == true) {
                    getSuccessResult(deviceModel)
                } else {
                    val code = fileServer?.error?.code
                    val msg = fileServer?.error?.msg
                    if (code == V5_ERR_DISK_MAIN_NOT_EXIST
                            || code == V5_ERR_DISK_NOT_SELECT_MAIN
                            || code == V5_ERR_DISK_MAIN_CANNOT_MOUNTED
                            || code == V5_ERR_DISK_NOT_SELECTED_MAIN
                            || code == V5_ERR_DISK_MAIN_ERROR) {
                        if (deviceModel.isOwner) {
                            Resource.error("SATA_NEED_FORMAT", Result<Any>(HttpErrorNo.ERR_ONESERVER_HDERROR, 1.toString()))
                        } else {
                            Resource.error("Other errors", Result<Any>(HttpErrorNo.ERR_DEVICE_STATUS, msg))
                        }
                    } else {
                        Resource.error("Other errors", Result<Any>(code
                                ?: HttpErrorNo.UNKNOWN_EXCEPTION, msg))
                    }
                }
            }
        } else {
            Resource.error("Other errors", Result<Any>(resultStatus.code, resultStatus.msg))
        }
        return Observable.just(resource)
    }

    private fun getSuccessResult(deviceModel: DeviceModel): Resource<Result<DeviceModel>> {
        SessionManager.getInstance().selectDeviceModel = deviceModel
        return Resource.success(Result(deviceModel))
    }

    fun removeSelf(itemId: String, username: String, callback: Callback<Resource<String>>, clearBinds: Boolean = true) {
        SessionManager.getInstance().getLoginSession(itemId, object : GetSessionListener() {
            override fun onSuccess(url: String, data: LoginSession) {
                val deletListener = object : OnUserManageListener {
                    override fun onStart(url: String?) {}
                    override fun onSuccess(url: String, cmd: String) {
                        val listener = object : OnUserManageListener {
                            override fun onStart(url: String) {}
                            override fun onSuccess(url: String, cmd: String) {
                                if (clearBinds) {
                                    clearDeviceUser(itemId, callback)
                                }
                            }

                            override fun onFailure(url: String, errorNo: Int, errorMsg: String) {
                                val msg = HttpErrorNo.getResultMsg(true, errorNo, errorMsg)
                                callback.result(Resource.error(msg, ""))
                            }
                        }
                        if (SessionCache.instance.isV5(data.id ?: "")) {
                            listener.onSuccess("", "")
                        } else {
                            val manageAPI = OneOSUserManageAPI(data)
                            manageAPI.setOnUserManageListener(listener)
                            manageAPI.chpwd(AppConstants.DEFAULT_USERNAME_ADMIN, AppConstants.DEFAULT_USERNAME_PWD)
                        }
//                        val observer = object : V5Observer<Any>(data.id ?: "") {
//                            override fun success(result: BaseProtocol<Any>) {
//                                listener.onSuccess("", "")
//                            }
//
//                            override fun fail(result: BaseProtocol<Any>) {
////                                listener.onFailure("", result.error?.code ?: 0, result.error?.msg
////                                        ?: "")
//                                listener.onSuccess("", "")
//                            }
//
//                            override fun isNotV5() {
//                                val manageAPI = OneOSUserManageAPI(data)
//                                manageAPI.setOnUserManageListener(listener)
//                                manageAPI.chpwd(AppConstants.DEFAULT_USERNAME_ADMIN, AppConstants.DEFAULT_USERNAME_PWD)
//                            }
//
//                            override fun retry(): Boolean {
//                                V5Repository.INSTANCE().updateUserPassword(data.id
//                                        ?: "", data.ip, LoginTokenUtil.getToken(), AppConstants.DEFAULT_USERNAME_ADMIN, AppConstants.DEFAULT_USERNAME_PWD, this)
//                                return true
//                            }
//                        }
//                        V5Repository.INSTANCE().updateUserPassword(data.id
//                                ?: "", data.ip, LoginTokenUtil.getToken(), AppConstants.DEFAULT_USERNAME_ADMIN, AppConstants.DEFAULT_USERNAME_PWD, observer)
                    }

                    override fun onFailure(url: String?, errorNo: Int, errorMsg: String) {
                        val msg = HttpErrorNo.getResultMsg(true, errorNo, errorMsg)
                        callback.result(Resource.error(msg, ""))
                    }
                }

                val observer = object : V5Observer<Any>(data.id ?: "") {
                    override fun success(result: BaseProtocol<Any>) {
                        deletListener.onSuccess("", "")
                    }

                    override fun fail(result: BaseProtocol<Any>) {
                        deletListener.onFailure("", result.error?.code ?: 0, result.error?.msg
                                ?: "")
                    }

                    override fun isNotV5() {
                        val oneOSUserManageAPI = OneOSUserManageAPI(data)
                        oneOSUserManageAPI.setOnUserManageListener(deletListener)
                        oneOSUserManageAPI.delete(username)
                    }

                    override fun retry(): Boolean {
                        V5Repository.INSTANCE().deleteUser(data.id
                                ?: "", data.ip, LoginTokenUtil.getToken(), username, this)
                        return true
                    }
                }
                V5Repository.INSTANCE().deleteUser(data.id
                        ?: "", data.ip, LoginTokenUtil.getToken(), username, observer)


            }

            override fun onFailure(url: String, errorNo: Int, errorMsg: String) {
                val msg = HttpErrorNo.getResultMsg(true, errorNo, errorMsg)
                callback.result(Resource.error(msg, ""))
            }
        })
    }

    fun clearDeviceUser(deviceId: String, callback: Callback<Resource<String>>) {
        val httpLoader = DeviceClearBindInfoHttpLoader(GsonBaseProtocol::class.java)
        httpLoader.setParams(deviceId)
        httpLoader.executor(object : net.sdvn.common.internet.listener.ResultListener<GsonBaseProtocol?> {
            override fun error(tag: Any?, baseProtocol: GsonBaseProtocol?) {
                callback.result(Resource.error(SdvnHttpErrorNo.ec2String(baseProtocol?.result
                        ?: -1), "${baseProtocol?.result}"))
            }

            override fun success(tag: Any?, data: GsonBaseProtocol?) {
                callback.result(Resource.success(data.toString()))
            }
        })
    }

    fun unbindDevice(deviceId: String, userId: String, callback: Callback<Resource<String>>) {
        val unbindDeviceHttpLoader = UnbindDeviceHttpLoader()
        unbindDeviceHttpLoader.unbindSingle(deviceId, userId, object : ResultListener<UnbindDeviceResult?> {
            override fun error(tag: Any?, baseProtocol: GsonBaseProtocol?) {
                callback.result(Resource.error(SdvnHttpErrorNo.ec2String(baseProtocol?.result
                        ?: -1), "${baseProtocol?.result}"))
            }

            override fun success(tag: Any?, data: UnbindDeviceResult?) {
                callback.result(Resource.success(data.toString()))
            }
        })
    }

    fun refreshDevNameById(devId: String): Observable<String> {
        return Observable.create<String> {
            if (devId.isNotEmpty()) {
                val deviceModel = SessionManager.getInstance().getDeviceModel(devId)
                if (deviceModel != null) {
                    if (SPUtils.getBoolean(AppConstants.SP_SHOW_REMARK_NAME, true)) {
                        it.onNext(deviceModel.devName)
                        deviceModel.devNameFromDB
                                .subscribe { name ->
                                    it.onNext(name)
                                }
                    } else {
                        it.onNext(deviceModel.devName)
                    }
                } else {
                    it.onError(DeviceNotFountException(devId))
                }
            } else {
                it.onError(DeviceNotFountException(devId))
            }
        }.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())

    }

}