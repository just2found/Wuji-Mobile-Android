package net.linkmate.app.ui.nas

import androidx.annotation.Keep
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.switchMap
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import io.weline.repo.api.V5HttpErrorNo
import io.weline.repo.data.model.BaseProtocol
import io.weline.repo.data.model.ServiceStatus
import io.weline.repo.net.V5Observer
import io.weline.repo.repository.V5Repository
import net.linkmate.app.R
import net.linkmate.app.base.BaseViewModel
import net.linkmate.app.util.ToastUtils
import net.sdvn.cmapi.CMAPI
import net.sdvn.common.internet.utils.LoginTokenUtil
import net.sdvn.nascommon.SessionManager
import net.sdvn.nascommon.iface.GetSessionListener
import net.sdvn.nascommon.model.oneos.user.LoginSession
import java.util.concurrent.atomic.AtomicBoolean

/** 服务基本viewModel,如dlna samba
 * @author Raleigh.Luo
 * date：21/6/5 18
 * describe：
 */
open class ServiceViewModel @Keep constructor(val serviceName: String, val serviceId: Int) : BaseViewModel() {
    var deviceId: String = ""

    //功能开启
    private val _functionOpened = MutableLiveData<Boolean>()
    val functionOpened: LiveData<Boolean> = _functionOpened

    protected val _startGetConfig = MutableLiveData<Boolean>()

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading

    fun loading(isLoading: Boolean) {
        _loading.value = isLoading
    }

    fun startGetConfig() {
        if (CMAPI.getInstance().isConnected) {//连接成功才请求
            _loading.value = true
            _startGetConfig.value = true
        } else {
            ToastUtils.showToast(R.string.network_not_available)
        }
    }

    //获取配置的路径
    val getServiceStatus: LiveData<Boolean> = _startGetConfig.switchMap {
        object : LiveData<Boolean>() {
            private val started = AtomicBoolean(false)
            override fun onActive() {
                super.onActive()
                if (started.compareAndSet(false, true)) {
                    val v5Observer = object : V5Observer<List<ServiceStatus>>(deviceId) {
                        override fun onSubscribe(d: Disposable) {
                            super.onSubscribe(d)
                            addDisposable(d)
                        }

                        override fun fail(result: BaseProtocol<List<ServiceStatus>>) {
                            postValue(false)
                            ToastUtils.showToast(V5HttpErrorNo.getResourcesId(result.error?.code
                                    ?: 0))
                        }

                        override fun isNotV5() {
                            postValue(false)
                            ToastUtils.showToast(V5HttpErrorNo.getResourcesId(0))
                        }

                        override fun retry(): Boolean {
                            return true
                        }

                        override fun success(result: BaseProtocol<List<ServiceStatus>>) {
                            if (result.result == true) {
                                val result = result.data?.find {
                                    it.name == serviceName
                                }
                                _functionOpened.value = result != null && result.status == 1
                                postValue(true)
                            } else {
                                ToastUtils.showToast(V5HttpErrorNo.getResourcesId(result.error?.code
                                        ?: 0))
                                postValue(false)
                            }

                        }

                    }
                    SessionManager.getInstance().getLoginSession(deviceId, object : GetSessionListener() {
                        override fun onSuccess(url: String?, loginSession: LoginSession?) {
                            V5Repository.INSTANCE().getServiceStatus(deviceId,
                                    ip = loginSession?.ip!!,
                                    token = LoginTokenUtil.getToken()
                            ).subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(v5Observer)
                        }

                        override fun onFailure(url: String?, errorNo: Int, errorMsg: String?) {
                            super.onFailure(url, errorNo, errorMsg)
                            postValue(false)
                            ToastUtils.showToast(V5HttpErrorNo.getResourcesId(errorNo))
                        }
                    })
                }
            }
        }
    }

    //功能开启
    private val _startConfig = MutableLiveData<Boolean>()
    fun startConfig(isOpened: Boolean) {
        if (CMAPI.getInstance().isEstablished) {//连接成功才请求
            _loading.value = true
            _startConfig.value = isOpened
        } else {
            //恢复
            _functionOpened.value = _functionOpened.value ?: false
            ToastUtils.showToast(R.string.network_not_available)
        }
    }

    //配置服务
    val configServiceResult: LiveData<Boolean> = _startConfig.switchMap {
        object : LiveData<Boolean>() {
            private val started = AtomicBoolean(false)
            override fun onActive() {
                super.onActive()
                if (started.compareAndSet(false, true)) {
                    val v5Observer = object : V5Observer<Any>(deviceId) {
                        override fun onSubscribe(d: Disposable) {
                            super.onSubscribe(d)
                            addDisposable(d)
                        }

                        override fun fail(result: BaseProtocol<Any>) {
                            ToastUtils.showToast(V5HttpErrorNo.getResourcesId(result.error?.code
                                    ?: 0))
                            _functionOpened.value = _functionOpened.value
                            postValue(false)
                        }

                        override fun isNotV5() {
                            ToastUtils.showToast(V5HttpErrorNo.getResourcesId(0))
                            _functionOpened.value = _functionOpened.value
                            postValue(false)
                        }

                        override fun retry(): Boolean {
                            return true
                        }

                        override fun success(result: BaseProtocol<Any>) {
                            if (result.result == true) {
                                _functionOpened.value = _startConfig.value
                                postValue(true)
                            } else {
                                ToastUtils.showToast(V5HttpErrorNo.getResourcesId(result.error?.code
                                        ?: 0))
                                _functionOpened.value = _functionOpened.value
                                postValue(false)
                            }
                        }

                    }
                    SessionManager.getInstance().getLoginSession(deviceId, object : GetSessionListener() {
                        override fun onSuccess(url: String?, loginSession: LoginSession?) {
                            V5Repository.INSTANCE().optService(deviceId,
                                    ip = loginSession?.ip!!,
                                    token = LoginTokenUtil.getToken(),
                                    method = if (_startConfig.value == true) "start" else "stop",
                                    serviceId = serviceId,
                                    onNext = v5Observer
                            )
                        }

                        override fun onFailure(url: String?, errorNo: Int, errorMsg: String?) {
                            super.onFailure(url, errorNo, errorMsg)
                            _functionOpened.value = _functionOpened.value
                            postValue(false)
                            ToastUtils.showToast(V5HttpErrorNo.getResourcesId(errorNo))
                        }
                    })
                }
            }
        }
    }
}