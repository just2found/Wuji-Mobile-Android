package net.linkmate.app.ui.activity.nasApp.deviceDetial.repository

import androidx.arch.core.util.Function
import io.reactivex.disposables.Disposable
import io.weline.repo.SessionCache
import io.weline.repo.data.model.BaseProtocol
import io.weline.repo.net.V5Observer
import io.weline.repo.repository.V5Repository
import net.linkmate.app.R
import net.linkmate.app.base.MyOkHttpListener
import net.linkmate.app.manager.DevManager
import net.linkmate.app.util.ToastUtils
import net.linkmate.app.util.business.DeviceUserUtil
import net.sdvn.cmapi.CMAPI
import net.sdvn.cmapi.global.Constants
import net.sdvn.common.internet.core.GsonBaseProtocol
import net.sdvn.common.internet.core.HttpLoader
import net.sdvn.common.internet.listener.ResultListener
import net.sdvn.common.internet.protocol.UnbindDeviceResult
import net.sdvn.common.internet.utils.LoginTokenUtil
import net.sdvn.nascommon.CustomLoaderStateListener
import net.sdvn.nascommon.SessionManager
import net.sdvn.nascommon.constant.AppConstants
import net.sdvn.nascommon.constant.HttpErrorNo
import net.sdvn.nascommon.iface.GetSessionListener
import net.sdvn.nascommon.model.oneos.api.sys.OneOSPowerAPI
import net.sdvn.nascommon.model.oneos.api.sys.OneOSPowerAPI.OnPowerListener
import net.sdvn.nascommon.model.oneos.api.user.OneOSClearUsersAPI
import net.sdvn.nascommon.model.oneos.api.user.OneOSClearUsersAPI.ClearUserListener
import net.sdvn.nascommon.model.oneos.api.user.OneOSUserManageAPI
import net.sdvn.nascommon.model.oneos.api.user.OneOSUserManageAPI.OnUserManageListener
import net.sdvn.nascommon.model.oneos.user.LoginSession
import timber.log.Timber

/**
 * @author Raleigh.Luo
 * date：20/7/29 09
 * describe：
 */
class DeviceControlRepository {
    /**
     * 开机和重启
     *
     * callback 关闭页面
     */
    fun doPowerOffOrRebootDevice(isPowerOff: Boolean, deviceId: String, vip: String, callback: Function<Void?, Void?>) {
        SessionManager.getInstance().getLoginSession(deviceId, object : GetSessionListener() {
            override fun onStart(url: String) {
                if (isPowerOff) ToastUtils.showToast(R.string.power_off_device) else ToastUtils.showToast(R.string.rebooting_device)
            }

            override fun onSuccess(url: String, data: LoginSession) {
                val listener = object : OnPowerListener {
                    override fun onStart(url: String) {}
                    override fun onSuccess(url: String, isPowerOff: Boolean) {
                        if (isPowerOff) ToastUtils.showToast(R.string.success_power_off_device) else ToastUtils.showToast(R.string.success_reboot_device)
                        callback.apply(null)
                    }

                    override fun onFailure(url: String, errorNo: Int, errorMsg: String) {
                        ToastUtils.showToast(HttpErrorNo.getResultMsg(false, errorNo, errorMsg))
                        if (!isPowerOff) {
                            if (rebootDevice(vip)) {
                                callback.apply(null)
                                ToastUtils.showToast(R.string.success_reboot_device)
                            }
                        }
                    }
                }
                val observer = object : V5Observer<Any>(data.ip) {
                    override fun success(result: BaseProtocol<Any>) {
                        listener.onSuccess("", isPowerOff)
                    }

                    override fun fail(result: BaseProtocol<Any>) {
                        listener.onFailure("", result.error?.code ?: 0, result.error?.msg ?: "")
                    }

                    override fun isNotV5() {
                        val oneOSPowerAPI = OneOSPowerAPI(data)
                        oneOSPowerAPI.setOnPowerListener(listener)
                        oneOSPowerAPI.power(isPowerOff)
                    }

                    override fun retry(): Boolean {
                        V5Repository.INSTANCE().rebootOrHaltSystem(data.ip, data.ip, LoginTokenUtil.getToken(), isPowerOff, this)
                        return true
                    }
                }
                V5Repository.INSTANCE().rebootOrHaltSystem(data.ip, data.ip, LoginTokenUtil.getToken(), isPowerOff, observer)
            }

            override fun onFailure(url: String, errorNo: Int, errorMsg: String) {
                if (!isPowerOff) {
                    if (rebootDevice(vip)) {
                        callback.apply(null)
                        ToastUtils.showToast(R.string.success_reboot_device)
                        return
                    }
                }
                super.onFailure(url, errorNo, errorMsg)
            }
        })
    }

    private fun rebootDevice(vip: String): Boolean {
        val result = CMAPI.getInstance().rebootDevice(vip)
        if (result == Constants.CE_SUCC) {
            return true
        } else {
            Timber.d("reboot result : %s", result)
        }
        return false
    }


    fun clearNasUser(deviceId: String, successCallback: Function<Void?, Void?>,
                     failedCallback: Function<String, Void?>, loaderStateListener: CustomLoaderStateListener? = null) {
        SessionManager.getInstance().getLoginSession(deviceId, object : GetSessionListener() {
            override fun onSuccess(url: String, data: LoginSession) {
                val listener = object : ClearUserListener<String?> {
                    override fun onStart(url: String?) {}
                    override fun onSuccess(url: String?) {
                        if (loaderStateListener?.isCanceled()?:false) return
                        val listener = object : OnUserManageListener {
                            override fun onStart(url: String) {}
                            override fun onSuccess(url: String, cmd: String) {
                                successCallback.apply(null)
                            }

                            override fun onFailure(url: String, errorNo: Int, errorMsg: String) {
                                val msg = HttpErrorNo.getResultMsg(true, errorNo, errorMsg)
                                failedCallback.apply(msg)
                            }
                        }
                        if (SessionCache.instance.isV5(data.id ?: "") == true) {
                            successCallback.apply(null)
                        } else {
                            val manageAPI = OneOSUserManageAPI(data)
                            manageAPI.setOnUserManageListener(listener)
                            manageAPI.chpwd(AppConstants.DEFAULT_USERNAME_ADMIN, AppConstants.DEFAULT_USERNAME_PWD)
                        }

//                        val observer = object : V5Observer<Any>(deviceId) {
//                            override fun success(result: BaseProtocol<Any>) {
//                                listener.onSuccess("", "")
//                            }
//
//                            override fun fail(result: BaseProtocol<Any>) {
//                                listener.onFailure("", result.error?.code ?: 0, result.error?.msg ?: "")
//                            }
//
//                            override fun isNotV5() {
//                                val manageAPI = OneOSUserManageAPI(data)
//                                manageAPI.setOnUserManageListener(listener)
//                                manageAPI.chpwd(AppConstants.DEFAULT_USERNAME_ADMIN, AppConstants.DEFAULT_USERNAME_PWD)
//                            }
//
//                            override fun retry(): Boolean {
//                                V5Repository.INSTANCE().updateUserPassword(deviceId,data.ip, LoginTokenUtil.getToken(), AppConstants.DEFAULT_USERNAME_ADMIN, AppConstants.DEFAULT_USERNAME_PWD, this)
//                                return true
//                            }
//                        }
//                        V5Repository.INSTANCE().updateUserPassword(deviceId,data.ip, LoginTokenUtil.getToken(), AppConstants.DEFAULT_USERNAME_ADMIN, AppConstants.DEFAULT_USERNAME_PWD, observer)


                    }

                    override fun onFailure(url: String?, errorNo: Int, errorMsg: String) {
                        if (loaderStateListener?.isCanceled()?:false) return
                        val msg = HttpErrorNo.getResultMsg(true, errorNo, errorMsg)
                        failedCallback.apply(msg)
                    }
                }

                val observer = object : V5Observer<Any>(deviceId) {
                    override fun onSubscribe(d: Disposable) {
                        super.onSubscribe(d)
                        loaderStateListener?.onLoadStart(d)
                    }

                    override fun success(result: BaseProtocol<Any>) {
                        listener.onSuccess("")
                    }

                    override fun fail(result: BaseProtocol<Any>) {
                        listener.onFailure("", result.error?.code ?: 0, result.error?.msg ?: "")
                    }

                    override fun isNotV5() {
                        val osClearUsersAPI = OneOSClearUsersAPI(data)
                        osClearUsersAPI.setClearUserListener(listener)
                        osClearUsersAPI.clear()
                        loaderStateListener?.onLoadStart(osClearUsersAPI)
                    }

                    override fun retry(): Boolean {
                        V5Repository.INSTANCE().clearUser(deviceId, data.ip, LoginTokenUtil.getToken(), this)
                        return true
                    }

                }
                V5Repository.INSTANCE().clearUser(deviceId, data.ip, LoginTokenUtil.getToken(), observer)

            }

            override fun onFailure(url: String, errorNo: Int, errorMsg: String) {
                val msg = HttpErrorNo.getResultMsg(true, errorNo, errorMsg)
                failedCallback.apply(msg)
            }
        })
    }


    fun clearDeviceUser(deviceId: String, loaderStateListener: HttpLoader.HttpLoaderStateListener?
                        , callback: Function<Boolean, Void?>) {
        DeviceUserUtil.deviceClearBinds(deviceId, loaderStateListener,
                object : MyOkHttpListener<GsonBaseProtocol>() {
                    override fun success(tag: Any?, data: GsonBaseProtocol) {
                        DevManager.getInstance().initHardWareList(null)//设备清空绑定
                        callback.apply(true)

                    }

                    override fun error(tag: Any?, baseProtocol: GsonBaseProtocol) {
                        super.error(tag, baseProtocol)
                        callback.apply(false)
                    }
                })
    }

    fun deleteThisDevice(deviceId: String, loaderStateListener: HttpLoader.HttpLoaderStateListener?,
                         successCallback: Function<Void?, Void?>,
                         failedCallback: Function<Int?, Void?>) {
        DeviceUserUtil.deleteThisDeviceSingle(deviceId, loaderStateListener,
                object : ResultListener<UnbindDeviceResult> {
                    override fun error(tag: Any?, baseProtocol: GsonBaseProtocol) {
                        failedCallback.apply(baseProtocol.result)

                    }

                    override fun success(tag: Any?, data: UnbindDeviceResult?) {
                        DevManager.getInstance().initHardWareList(null)//用户解绑
                        successCallback.apply(null)
                    }
                })
    }


    fun deleteNasUser(loaderStateListener: CustomLoaderStateListener, deviceId: String, isOwner: Boolean, successCallback: Function<Boolean, Void?>,
                      failedCallback: Function<String?, Void?>) {
        var deleteThisDevice = false
        SessionManager.getInstance().getLoginSession(deviceId, object : GetSessionListener() {
            override fun onSuccess(url: String, data: LoginSession) {
                val manageAPI = OneOSUserManageAPI(data)
                val listener = object : OnUserManageListener {
                    override fun onStart(url: String) {}
                    override fun onSuccess(url: String, cmd: String) {
                        if (loaderStateListener.isCanceled()) return
                        if (isOwner) {
                            val isV5 = SessionCache.instance.isV5(data.id ?: "")
                            if (isV5 != true) {//旧接口
                                //如果是所有者 则需要把管理员的密码恢复至默认密码
                                manageAPI.chpwd(AppConstants.DEFAULT_USERNAME_ADMIN, AppConstants.DEFAULT_USERNAME_PWD)
                            }
                            if (isV5 == true || cmd == "update") {
                                deleteThisDevice = true
                            }

//                            val observer = object : V5Observer<Any>(data.ip) {
//                                override fun success(result: BaseProtocol<Any>) {
//                                    successCallback.apply(deleteThisDevice)
//                                }
//
//                                override fun fail(result: BaseProtocol<Any>) {
//                                    val errorNo = result.error?.code ?: 0
//                                    val errorMsg = result.error?.msg ?: ""
//                                    if (errorNo == -40000 && "Delete system user failed" == errorMsg) {
//                                        failedCallback.apply(null)
//                                    } else {
//                                        val msg = HttpErrorNo.getResultMsg(true, errorNo, errorMsg)
//                                        failedCallback.apply(msg)
//                                    }
//                                }
//
//                                override fun isNotV5() {
//                                    //如果是所有者 则需要把管理员的密码恢复至默认密码
//                                    manageAPI.chpwd(AppConstants.DEFAULT_USERNAME_ADMIN, AppConstants.DEFAULT_USERNAME_PWD)
//                                }
//
//                                override fun retry(): Boolean {
//                                    V5Repository.INSTANCE().updateUserPassword(data.ip, data.ip, LoginTokenUtil.getToken(), AppConstants.DEFAULT_USERNAME_ADMIN, AppConstants.DEFAULT_USERNAME_PWD, this)
//                                    return true
//                                }
//                            }
//                            V5Repository.INSTANCE().updateUserPassword(data.ip, data.ip, LoginTokenUtil.getToken(), AppConstants.DEFAULT_USERNAME_ADMIN, AppConstants.DEFAULT_USERNAME_PWD, observer)


                        } else deleteThisDevice = true
                        if (deleteThisDevice) successCallback.apply(deleteThisDevice)
                    }

                    override fun onFailure(url: String, errorNo: Int, errorMsg: String) {

                        if (errorNo == -40000 && "Delete system user failed" == errorMsg) {
                            failedCallback.apply(null)
                        } else {
                            val msg = HttpErrorNo.getResultMsg(true, errorNo, errorMsg)
                            failedCallback.apply(msg)
                        }
                    }
                }

                val observer = object : V5Observer<Any>(data.ip) {
                    override fun onSubscribe(d: Disposable) {
                        super.onSubscribe(d)
                        loaderStateListener.onLoadStart(d)
                    }

                    override fun success(result: BaseProtocol<Any>) {
                        listener.onSuccess("", "")
                    }

                    override fun fail(result: BaseProtocol<Any>) {
                        listener.onFailure("", result.error?.code ?: 0, result.error?.msg ?: "")
                    }

                    override fun isNotV5() {
                        manageAPI.setOnUserManageListener(listener)
                        manageAPI.delete(SessionManager.getInstance().username)
                        loaderStateListener.onLoadStart(manageAPI)
                    }

                    override fun retry(): Boolean {
                        V5Repository.INSTANCE().deleteUser(deviceId, data.ip, LoginTokenUtil.getToken(), SessionManager.getInstance().username, this)
                        return true
                    }
                }

                V5Repository.INSTANCE().deleteUser(deviceId, data.ip, LoginTokenUtil.getToken(), SessionManager.getInstance().username, observer)
            }

            override fun onFailure(url: String, errorNo: Int, errorMsg: String) {
                if (loaderStateListener.isCanceled()) return
                val msg = HttpErrorNo.getResultMsg(true, errorNo, errorMsg)
                failedCallback.apply(msg)
            }
        })
    }

    fun update(deviceId: String) {
//        SessionManager.getInstance().getLoginSession(deviceId, object : GetSessionListener() {
//            override fun onSuccess(url: String?, data: LoginSession?) {
//                if (data != null) {
//                    val oneOSInstallUpdatePkgApi = OneOSInstallUpdatePkgApi(data)
//                    oneOSInstallUpdatePkgApi.install(updateInfo.url, updateInfo.isOnline,
//                            object : OnHttpRequestListener {
//                                override fun onSuccess(url: String?, result: String?) {
//                                    val resultModel = GsonUtils.decodeJSON(result, BaseResultModel::class.java)
//                                    upgradeResult.postValue(Resource.success(resultModel?.isSuccess))
//                                }
//
//                                override fun onFailure(url: String?, httpCode: Int, errorNo: Int, strMsg: String?) {
//                                    ToastHelper.showToast(HttpErrorNo.getResultMsg(false, errorNo, strMsg))
//                                    upgradeResult.postValue(Resource.error(strMsg!!, null))
//                                }
//
//                                override fun onStart(url: String?) {
//                                }
//                            })
//
//                }
//            }
//
//            override fun onFailure(url: String?, errorNo: Int, errorMsg: String?) {
//                super.onFailure(url, errorNo, errorMsg)
//                upgradeResult.postValue(Resource.error(errorMsg!!, null))
//            }
//
//            override fun onStart(url: String?) {
//            }
//        })
    }


}