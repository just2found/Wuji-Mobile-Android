package net.linkmate.app.ui.nas.samba

import android.text.TextUtils
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
import net.linkmate.app.util.ToastUtils
import net.sdvn.common.internet.utils.LoginTokenUtil
import net.sdvn.nascommon.SessionManager
import net.sdvn.nascommon.iface.GetSessionListener
import net.sdvn.nascommon.model.oneos.user.LoginSession
import net.sdvn.nascommon.viewmodel.RxViewModel
import org.json.JSONObject
import java.util.concurrent.atomic.AtomicBoolean

/**SAMBA设置密码
 * @author Raleigh.Luo
 * date：21/6/7 10
 * describe：
 */
class SAMBASettingPasswordViewModel : RxViewModel() {
    //设备id
    var deviceId: String = ""

    /**--设置密码--------------------------------------------**/
    private val _settingPassword = MutableLiveData<String>()

    //触发请求设置密码
    fun setPassword(password: String) {
        _settingPassword.value = password
    }

    //设置密码结果
    val setPasswordResult: LiveData<Boolean> = _settingPassword.switchMap {
        object : LiveData<Boolean>() {
            private val started = AtomicBoolean(false)
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
                            json.put("method", "open")
                            val params = JSONObject()
                            params.put("password", _settingPassword.value)
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
                            postValue(false)
                            ToastUtils.showToast(V5HttpErrorNo.getResourcesId(errorNo))
                        }
                    })
                }
            }
        }
    }
}