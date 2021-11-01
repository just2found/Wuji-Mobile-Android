package net.sdvn.nascommon.viewmodel

import android.annotation.SuppressLint
import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import io.weline.repo.data.model.BaseProtocol
import io.weline.repo.net.V5Observer
import io.weline.repo.repository.V5Repository
import libs.source.common.livedata.Resource
import net.sdvn.common.internet.utils.LoginTokenUtil
import net.sdvn.nascommon.CustomLoaderStateListener
import net.sdvn.nascommon.SessionManager
import net.sdvn.nascommon.constant.AppConstants
import net.sdvn.nascommon.constant.HttpErrorNo
import net.sdvn.nascommon.constant.OneOSAPIs
import net.sdvn.nascommon.iface.GetSessionListener
import net.sdvn.nascommon.model.DeviceModel
import net.sdvn.nascommon.model.http.OnHttpRequestListener
import net.sdvn.nascommon.model.oneos.BaseResultModel
import net.sdvn.nascommon.model.oneos.UpdateInfo
import net.sdvn.nascommon.model.oneos.api.sys.OneOSGetUpdateInfoAPI
import net.sdvn.nascommon.model.oneos.api.sys.OneOSInstallUpdatePkgApi
import net.sdvn.nascommon.model.oneos.api.sys.OneOSPowerAPI
import net.sdvn.nascommon.model.oneos.api.user.OneOSLoginAPI
import net.sdvn.nascommon.model.oneos.event.EventMsgManager
import net.sdvn.nascommon.model.oneos.event.UpgradeProgress
import net.sdvn.nascommon.model.oneos.user.LoginSession
import net.sdvn.nascommon.utils.DialogUtils
import net.sdvn.nascommon.utils.GsonUtils
import net.sdvn.nascommon.utils.ToastHelper
import net.sdvn.nascommon.utils.log.Logger
import net.sdvn.nascommonlib.R
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import timber.log.Timber
import java.util.concurrent.TimeUnit

class M3UpdateViewModel : ViewModel() {
    private val isDebug: Boolean = true
    private var mLoginSession: LoginSession? = null
    //update info
    private val result = MediatorLiveData<Resource<UpdateInfo>>()
    private val upgradeProgressResult = MediatorLiveData<Resource<UpgradeProgress>>()
    private val upgradeResult = MediatorLiveData<Resource<Boolean>>()
    var compositeDisposable = CompositeDisposable()

    init {
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
        }
    }

    fun getLoginSession(devId: String, eventListener: GetSessionListener) {
        val currentTimeMillis = System.currentTimeMillis()
        if (mLoginSession?.userInfo == null ||
            mLoginSession?.deviceInfo == null ||
            mLoginSession?.session.isNullOrEmpty() ||
            currentTimeMillis - mLoginSession?.loginTime!! >= AppConstants.SESSION_LIVE_TIME) {
            SessionManager.getInstance().getLoginSession(devId, object : GetSessionListener() {
                @SuppressLint("SetTextI18n")
                override fun onSuccess(url: String?, loginSession: LoginSession) {
                    val token = LoginTokenUtil.getToken()
                    val oneOSLoginAPI = OneOSLoginAPI(loginSession.ip, OneOSAPIs.ONE_API_DEFAULT_PORT, token, loginSession.id!!)
                    oneOSLoginAPI.setOnLoginListener(object : OneOSLoginAPI.OnLoginListener {
                        override fun onStart(url: String?) {

                        }

                        override fun onSuccess(url: String, loginSession: LoginSession) {
                            mLoginSession?.refreshData(loginSession) ?: kotlin.run {
                                mLoginSession = loginSession
                            }
                            eventListener.onSuccess(url, loginSession)
                        }

                        override fun onFailure(url: String, errorNo: Int, errorMsg: String) {
                            ToastHelper.showToast(HttpErrorNo.getResultMsg(true, errorNo, errorMsg))
                        }
                    })
                    oneOSLoginAPI.accessOneOS(AppConstants.DOMAIN_DEVICE_VIP)
                }
            })
        } else {
            eventListener.onSuccess("", mLoginSession)
        }
    }

    fun getUpdateInfo(infos: DeviceModel): LiveData<Resource<UpdateInfo>> {
        getLoginSession(infos.devId, object : GetSessionListener(false) {
            override fun onSuccess(url: String?, data: LoginSession) {
                val oneOSGetUpdateInfoAPI = OneOSGetUpdateInfoAPI(data)
                oneOSGetUpdateInfoAPI.setOnUpdateInfoListener(object : OneOSGetUpdateInfoAPI.OnUpdateInfoListener {
                    override fun onSuccess(url: String?, updateInfo: UpdateInfo?) {
                        if (updateInfo?.isNeedup == true) {
                            infos.loginSession?.oneOSInfo?.isNeedsUp = true
                            Observable.interval(100, 500, TimeUnit.MILLISECONDS)
                                    .subscribeOn(Schedulers.single())
                                    .subscribe(object : Observer<Long> {
                                        var count = 0
                                        var disposable: Disposable? = null

                                        override fun onSubscribe(d: Disposable) {
                                            compositeDisposable.add(d)
                                            disposable = d
                                        }

                                        override fun onNext(aLong: Long) {
                                            if (EventMsgManager.instance.isReceive(infos.devId)) {
                                                result.postValue(Resource.success(updateInfo))
                                                disposable?.dispose()
                                            } else {
                                                if (count % 4 == 0)
                                                    EventMsgManager.instance.startReceive(infos.devId)
                                                if (count >= 30) {
                                                    disposable?.dispose()
                                                    result.postValue(Resource.error("subscribe failed", updateInfo))
                                                }
                                                count++
                                            }
                                        }

                                        override fun onError(e: Throwable) {
                                            Timber.e(e)
                                        }

                                        override fun onComplete() {

                                        }
                                    })
                        }
                    }

                    override fun onFailure(url: String?, errorNo: Int, errorMsg: String?) {
                        result.postValue(Resource.error(errorMsg!!, null))
                    }

                    override fun onStart(url: String?) {
                    }
                })
                oneOSGetUpdateInfoAPI.get()
            }

            override fun onFailure(url: String?, errorNo: Int, errorMsg: String?) {
                super.onFailure(url, errorNo, errorMsg)
                result.postValue(Resource.error(errorMsg!!, null))
            }

            override fun onStart(url: String?) {
            }
        })
        return result
    }

    fun subUpgradeProgress(): LiveData<Resource<UpgradeProgress>> {
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
        }
        return upgradeProgressResult
    }

    @Subscribe(sticky = false, threadMode = ThreadMode.MAIN)
    fun onProgress(upgradeProgress: UpgradeProgress) {
        Logger.p(Logger.Level.DEBUG, isDebug, "Event bus", Thread.currentThread().name)
        upgradeProgressResult.postValue(Resource.success(upgradeProgress))
//        if ("download".equals(upgradeProgress.name, ignoreCase = true)) {
//            if (upgradeProgress.percent >= 0)
//                upgradeProgressResult.postValue(Resource.success(upgradeProgress))
//            else {
//                ToastHelper.showToast(R.string.device_upgrade_failed_by_download)
//                upgradeProgressResult.postValue(Resource.error("download exception", upgradeProgress))
//            }
//        }
//        if ("install".equals(upgradeProgress.name, ignoreCase = true)) {
//            if (upgradeProgress.percent >= 0) {
//                if (upgradeProgress.percent == 100) {
//                    upgradeProgressResult.postValue(Resource.success(upgradeProgress))
//                }
//            } else {
//                ToastHelper.showToast(R.string.device_upgrade_failed_by_install)
//                upgradeProgressResult.postValue(Resource.error("download exception", upgradeProgress))
//            }
//        }

    }

    public override fun onCleared() {
        super.onCleared()
        compositeDisposable.clear()
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this)
        }

    }

    fun update(context: Context, devId: String, updateInfo: UpdateInfo, loaderStateListener: CustomLoaderStateListener? = null): LiveData<Resource<Boolean>> {
        DialogUtils.showWarningDialog(context,
                R.string.upgrade,
                R.string.waring_upgrade_device,
                R.string.install,
                R.string.cancel) { diglog, isPositiveBtn ->
            diglog.dismiss()
            if (isPositiveBtn) {
                getLoginSession(devId, object : GetSessionListener() {
                    override fun onSuccess(url: String?, data: LoginSession?) {
                        if (loaderStateListener?.isCanceled() ?: false) return
                        if (data != null) {
                            val oneOSInstallUpdatePkgApi = OneOSInstallUpdatePkgApi(data)
                            oneOSInstallUpdatePkgApi.install(updateInfo.url, updateInfo.isOnline,
                                    object : OnHttpRequestListener {
                                        override fun onSuccess(url: String?, result: String?) {
                                            val resultModel = GsonUtils.decodeJSON(result, BaseResultModel::class.java)
                                            upgradeResult.postValue(Resource.success(resultModel?.isSuccess))
                                        }

                                        override fun onFailure(url: String?, httpCode: Int, errorNo: Int, strMsg: String?) {
                                            ToastHelper.showToast(HttpErrorNo.getResultMsg(false, errorNo, strMsg))
                                            upgradeResult.postValue(Resource.error(strMsg!!, null))
                                        }

                                        override fun onStart(url: String?) {
                                        }
                                    })
                            loaderStateListener?.onLoadStart(oneOSInstallUpdatePkgApi)
                        }
                    }

                    override fun onFailure(url: String?, errorNo: Int, errorMsg: String?) {
                        if (loaderStateListener?.isCanceled() ?: false) return
                        super.onFailure(url, errorNo, errorMsg)
                        upgradeResult.postValue(Resource.error(errorMsg!!, null))
                    }

                    override fun onStart(url: String?) {
                    }
                })
                upgradeResult.postValue(Resource.loading(null))
            }
        }
        return upgradeResult
    }

    var isShowDialog: Boolean = false
    fun showPowerDialog(context: Context, devId: String, isPowerOff: Boolean) {
        val contentRes = if (isPowerOff) R.string.confirm_power_off_device else R.string.confirm_reboot_device
        if (!isShowDialog) {
            isShowDialog = true
            DialogUtils.showConfirmDialog(context,
                    R.string.tips, contentRes,
                    R.string.confirm,
                    R.string.cancel) { dialog, isPos ->
                if (isPos) {
                    val listener = object : OneOSPowerAPI.OnPowerListener {
                        override fun onSuccess(url: String?, isPowerOff: Boolean) {
                            if (isPowerOff)
                                ToastHelper.showToast(R.string.success_power_off_device)
                            else
                                ToastHelper.showToast(R.string.success_reboot_device)
                        }

                        override fun onFailure(url: String?, errorNo: Int, errorMsg: String?) {
                            ToastHelper.showToast(HttpErrorNo.getResultMsg(false, errorNo, errorMsg))
                        }

                        override fun onStart(url: String?) {
                        }
                    }
                    doPowerOffOrRebootDevice(isPowerOff, devId, listener)
                }
                dialog.dismiss()
                isShowDialog = false
            }
        }

    }

    fun doPowerOffOrRebootDevice(isPowerOff: Boolean, devId: String, listener: OneOSPowerAPI.OnPowerListener) {
        getLoginSession(devId, object : GetSessionListener() {
            override fun onSuccess(url: String?, data: LoginSession) {
                val observer = object : V5Observer<Any>(data.id ?: "") {
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
                        V5Repository.INSTANCE().rebootOrHaltSystem(data.id
                                ?: "", data.ip, LoginTokenUtil.getToken(), isPowerOff, this)
                        return true
                    }
                }

                V5Repository.INSTANCE().rebootOrHaltSystem(data.id
                        ?: "", data.ip, LoginTokenUtil.getToken(), isPowerOff, observer)


            }

            override fun onStart(url: String?) {
                if (isPowerOff)
                    ToastHelper.showToast(R.string.power_off_device)
                else
                    ToastHelper.showToast(R.string.rebooting_device)
            }
        })
    }

}