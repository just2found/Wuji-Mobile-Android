package net.linkmate.app.ui.nas.samba

import android.annotation.SuppressLint
import android.text.TextUtils
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.switchMap
import io.reactivex.disposables.Disposable
import io.weline.repo.api.V5HttpErrorNo
import io.weline.repo.data.model.BaseProtocol
import io.weline.repo.data.model.DLNAPathResult
import io.weline.repo.data.model.LanScanVisibleResult
import io.weline.repo.net.V5Observer
import io.weline.repo.repository.V5Repository
import net.linkmate.app.ui.nas.ServiceViewModel
import net.linkmate.app.util.ToastUtils
import net.sdvn.cmapi.CMAPI
import net.sdvn.common.internet.SdvnHttpErrorNo
import net.sdvn.common.internet.core.GsonBaseProtocol
import net.sdvn.common.internet.listener.CommonResultListener
import net.sdvn.common.internet.loader.DeviceSharedUsersHttpLoader
import net.sdvn.common.internet.protocol.SharedUserList
import net.sdvn.common.internet.utils.LoginTokenUtil
import net.sdvn.nascommon.SessionManager
import net.sdvn.nascommon.iface.GetSessionListener
import net.sdvn.nascommon.model.oneos.user.LoginSession
import net.sdvn.nascommon.utils.ToastHelper
import org.json.JSONObject
import java.util.concurrent.atomic.AtomicBoolean

/**SAMBA功能
 * @author Raleigh.Luo
 * date：21/6/5 14
 * describe：
 */
class SAMBAViewModel : ServiceViewModel("samba", 10) {
    var isAdmin = false

    /**
     * 获取初始化配置是否成功
     */
    fun isGetInitConfigSuccess(): Boolean {
        return getLanScanVisible.value == true && getServiceStatus.value == true
    }
    /**--局域网可见--------------------------------------------**/
    private val _lanScanVisible = MutableLiveData<Boolean>(false)
    val lanScanVisible: LiveData<Boolean> = _lanScanVisible

    private val _configLanScanVisible = MutableLiveData<Boolean>()

    /**
     * 请求配置局域网可见
     */
    fun configLanScanVisible(isVisible: Boolean) {
        _configLanScanVisible.value = isVisible
    }

    //获取局域网可见结果
    val getLanScanVisible = _startGetConfig.switchMap {
        object : LiveData<Boolean>() {
            val started = AtomicBoolean(false)
            override fun onActive() {
                super.onActive()
                if (started.compareAndSet(false, true)) {
                    val v5Observer = object : V5Observer<LanScanVisibleResult>(deviceId) {
                        override fun onSubscribe(d: Disposable) {
                            super.onSubscribe(d)
                            addDisposable(d)
                        }

                        override fun success(result: BaseProtocol<LanScanVisibleResult>) {
                            if (result.result) {
                                _lanScanVisible.value = result.data?.network_discovery == 1
                            } else {
                                ToastUtils.showToast(V5HttpErrorNo.getResourcesId(result.error?.code
                                        ?: 0))
                            }
                            postValue(result.result)
                        }

                        override fun fail(result: BaseProtocol<LanScanVisibleResult>) {
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

                    }
                    SessionManager.getInstance().getLoginSession(deviceId, object : GetSessionListener() {
                        override fun onSuccess(url: String?, loginSession: LoginSession?) {
                            val json = JSONObject()
                            json.put("method", "getoption")
                            V5Repository.INSTANCE().samba(deviceId,
                                    ip = loginSession?.ip!!,
                                    token = LoginTokenUtil.getToken(),
                                    params = json,
                                    onNext = v5Observer
                            )
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
    //配置局域网可见结果
    val configLanScanVisibleResult = _configLanScanVisible.switchMap {
        object : LiveData<Boolean>() {
            val started = AtomicBoolean(false)
            override fun onActive() {
                super.onActive()
                if (started.compareAndSet(false, true)) {
                    val v5Observer = object : V5Observer<LanScanVisibleResult>(deviceId) {
                        override fun onSubscribe(d: Disposable) {
                            super.onSubscribe(d)
                            addDisposable(d)
                        }

                        override fun success(result: BaseProtocol<LanScanVisibleResult>) {
                            postValue(result.result)
                            if (!result.result) {
                                _lanScanVisible.value = _lanScanVisible.value
                                ToastUtils.showToast(V5HttpErrorNo.getResourcesId(result.error?.code
                                        ?: 0))
                            } else {
                                _lanScanVisible.value = _configLanScanVisible.value
                            }
                        }

                        override fun fail(result: BaseProtocol<LanScanVisibleResult>) {
                            //还原
                            _lanScanVisible.value = _lanScanVisible.value
                            postValue(false)
                            ToastUtils.showToast(V5HttpErrorNo.getResourcesId(result.error?.code
                                    ?: 0))
                        }

                        override fun isNotV5() {
                            _lanScanVisible.value = _lanScanVisible.value
                            postValue(false)
                            ToastUtils.showToast(V5HttpErrorNo.getResourcesId(0))
                        }

                        override fun retry(): Boolean {
                            return true
                        }

                    }
                    SessionManager.getInstance().getLoginSession(deviceId, object : GetSessionListener() {
                        override fun onSuccess(url: String?, loginSession: LoginSession?) {
                            val json = JSONObject()
                            json.put("method", "setoption")
                            val params = JSONObject()
                            params.put("network_discovery", if (_configLanScanVisible.value == true) 1 else 0)
                            json.put("params", params)
                            V5Repository.INSTANCE().samba(deviceId,
                                    ip = loginSession?.ip!!,
                                    token = LoginTokenUtil.getToken(),
                                    params = json,
                                    onNext = v5Observer
                            )
                        }

                        override fun onFailure(url: String?, errorNo: Int, errorMsg: String?) {
                            super.onFailure(url, errorNo, errorMsg)
                            _lanScanVisible.value = _lanScanVisible.value
                            postValue(false)
                            ToastUtils.showToast(V5HttpErrorNo.getResourcesId(errorNo))
                        }
                    })
                }
            }
        }

    }

    /**--获取用户名--------------------------------------------**/
    val getUserName = _startGetConfig.switchMap {
        object : LiveData<String>() {
            private val started = AtomicBoolean(false)
            override fun onActive() {
                super.onActive()
                if (started.compareAndSet(false, true)) {
                    SessionManager.getInstance().getLoginSession(deviceId, object : GetSessionListener() {
                        @SuppressLint("SetTextI18n")
                        override fun onSuccess(url: String?, loginSession: LoginSession) {
                            postValue(loginSession.userInfo?.username ?: "")
                        }

                        override fun onFailure(url: String?, errorNo: Int, errorMsg: String?) {
                            super.onFailure(url, errorNo, errorMsg)
                            postValue("")
                        }
                    })
                }
            }
        }
    }
}