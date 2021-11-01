package net.linkmate.app.ui.nas.dnla

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.switchMap
import io.reactivex.disposables.Disposable
import io.weline.repo.api.V5HttpErrorNo
import io.weline.repo.data.model.BaseProtocol
import io.weline.repo.data.model.DLNAOptionResult
import io.weline.repo.data.model.DLNAPathResult
import io.weline.repo.net.V5Observer
import io.weline.repo.repository.V5Repository
import net.linkmate.app.ui.nas.ServiceViewModel
import net.linkmate.app.util.ToastUtils
import net.sdvn.common.internet.utils.LoginTokenUtil
import net.sdvn.nascommon.SessionManager
import net.sdvn.nascommon.fileserver.constants.SharePathType
import net.sdvn.nascommon.iface.GetSessionListener
import net.sdvn.nascommon.model.oneos.user.LoginSession
import org.json.JSONObject
import java.util.concurrent.atomic.AtomicBoolean

/**
 * @author Raleigh.Luo
 * date：21/6/4 18
 * describe：
 */
class DNLAViewModel : ServiceViewModel("dlna", 11) {
//    var deviceId: String = ""
//    private var serviceName = "dlna"//serviceId 11
//    private var serviceId = 11//serviceId 11
//    private var serviceName = "samba"//serviceId 10
    /**
     * 获取初始化配置是否成功
     */
    fun isGetInitConfigSuccess(): Boolean {
        return getConfigScanPath.value == true && getServiceStatus.value == true
    }

    //是否开启公共空间扫描
    private val _scanPublicPathOpened = MutableLiveData<Boolean>(false)
    val isScanPublicPath: LiveData<Boolean> = _scanPublicPathOpened
    fun scanPublicPathOpened(isOpened: Boolean) {
        _scanPublicPathOpened.value = isOpened
    }

    //是否开启外部存储扫描
    private val _scanExternalStoragePathOpened = MutableLiveData<Boolean>(false)
    val isScanExternalStoragePath: LiveData<Boolean> = _scanExternalStoragePathOpened
    fun scanExternalStoragePathOpened(isOpened: Boolean) {
        _scanExternalStoragePathOpened.value = isOpened
    }

    private val _isAdmin = MutableLiveData<Boolean>()
    fun startGetConfigScanPath(isAdmin: Boolean) {
        _isAdmin.value = isAdmin
    }

    /**
     * 获取配置的扫描路径
     */
    val getConfigScanPath: LiveData<Boolean> = _isAdmin.switchMap {
        object : LiveData<Boolean>() {
            private val started = AtomicBoolean(false)
            override fun onActive() {
                super.onActive()
                if (started.compareAndSet(false, true)) {
                    val v5Observer = object : V5Observer<List<DLNAPathResult>>(deviceId) {
                        override fun onSubscribe(d: Disposable) {
                            super.onSubscribe(d)
                            addDisposable(d)
                        }

                        override fun success(result: BaseProtocol<List<DLNAPathResult>>) {
                            if (result.result) {
                                //OneOSAPIs.ONE_OS_PUBLIC_ROOT_DIR
                                //公共空间
                                val publicPath = result.data?.find {
                                    it.share_path_type == SharePathType.PUBLIC.type
                                }
                                //外部存储
                                val extStoregePath = result.data?.find {
                                    it.share_path_type == SharePathType.EXTERNAL_STORAGE.type
                                }
                                _scanPublicPathOpened.value = publicPath != null
                                _scanExternalStoragePathOpened.value = extStoregePath != null
                            } else {
                                ToastUtils.showToast(V5HttpErrorNo.getResourcesId(result.error?.code
                                        ?: 0))
                            }
                            postValue(result.result)
                        }

                        override fun fail(result: BaseProtocol<List<DLNAPathResult>>) {
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

                    val v5ObserverGetOption = object : V5Observer<DLNAOptionResult>(deviceId) {
                        override fun onSubscribe(d: Disposable) {
                            super.onSubscribe(d)
                            addDisposable(d)
                        }

                        override fun success(result: BaseProtocol<DLNAOptionResult>) {
                            if (result.result) {
                                //OneOSAPIs.ONE_OS_PUBLIC_ROOT_DIR
                                //公共空间
                                val publicPath = result.data?.media_path?.find {
                                    it.share_path_type == SharePathType.PUBLIC.type
                                }
                                //外部存储
                                val extStoregePath = result.data?.media_path?.find {
                                    it.share_path_type == SharePathType.EXTERNAL_STORAGE.type
                                }
                                _scanPublicPathOpened.value = publicPath != null
                                _scanExternalStoragePathOpened.value = extStoregePath != null
                            } else {
                                ToastUtils.showToast(V5HttpErrorNo.getResourcesId(result.error?.code
                                        ?: 0))
                            }
                            postValue(result.result)
                        }

                        override fun fail(result: BaseProtocol<DLNAOptionResult>) {
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

                            if (_isAdmin.value == true) {
                                json.put("method", "scan")
                                val params = JSONObject()
                                params.put("cmd", "list")
                                json.put("params", params)
                                V5Repository.INSTANCE().dlna(deviceId,
                                        ip = loginSession?.ip!!,
                                        token = LoginTokenUtil.getToken(),
                                        params = json,
                                        onNext = v5Observer
                                )
                            } else {
                                json.put("method", "getoption")
                                V5Repository.INSTANCE().dlnaGetOption(deviceId,
                                        ip = loginSession?.ip!!,
                                        token = LoginTokenUtil.getToken(),
                                        params = json,
                                        onNext = v5ObserverGetOption
                                )
                            }
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