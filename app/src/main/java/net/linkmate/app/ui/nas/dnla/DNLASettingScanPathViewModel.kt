package net.linkmate.app.ui.nas.dnla

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.switchMap
import io.weline.repo.api.V5HttpErrorNo
import io.weline.repo.data.model.BaseProtocol
import io.weline.repo.data.model.DLNAPathResult
import io.weline.repo.net.V5Observer
import io.weline.repo.repository.V5Repository
import net.linkmate.app.util.ToastUtils
import net.sdvn.common.internet.utils.LoginTokenUtil
import net.sdvn.nascommon.SessionManager
import net.sdvn.nascommon.fileserver.constants.SharePathType
import net.sdvn.nascommon.iface.GetSessionListener
import net.sdvn.nascommon.model.oneos.user.LoginSession
import net.sdvn.nascommon.viewmodel.RxViewModel
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.atomic.AtomicBoolean

/**DLNA设置扫描路径
 * @author Raleigh.Luo
 * date：21/6/5 17
 * describe：
 */
class DNLASettingScanPathViewModel : RxViewModel() {
    //设备id
    var deviceId = ""
    //是否开启公共空间扫描
    private val _isScanPublicPath = MutableLiveData<Boolean>()
    val isScanPublicPath: LiveData<Boolean> = _isScanPublicPath

    //是否开启外部存储扫描
    private val _isScanExternalStoragePath = MutableLiveData<Boolean>()
    val isScanExternalStoragePath: LiveData<Boolean> = _isScanExternalStoragePath

    /**
     * 请求是否扫描公共空间
     */
    fun configPublicPath(isScaned: Boolean) {
        _isScanPublicPath.value = isScaned
    }

    /**
     * 请求是否扫描外部存储空间
     */
    fun configExternalStoregePath(isScaned: Boolean) {
        _isScanExternalStoragePath.value = isScaned
    }

    //配置公共空间结果
    val configPublicPathResult = _isScanPublicPath.switchMap {
        val method = if (it) "add" else "del"
        object : LiveData<Boolean>() {
            private val started = AtomicBoolean(false)
            override fun onActive() {
                super.onActive()
                if (started.compareAndSet(false, true)) {
                    val v5Observer = object : V5Observer<List<DLNAPathResult>>(deviceId) {
                        override fun success(result: BaseProtocol<List<DLNAPathResult>>) {
                            postValue(result.result)
                            if (!result.result) {
                                ToastUtils.showToast(V5HttpErrorNo.getResourcesId(result.error?.code
                                        ?: 0))
                            }
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
                    SessionManager.getInstance().getLoginSession(deviceId, object : GetSessionListener() {
                        override fun onSuccess(url: String?, loginSession: LoginSession?) {
                            V5Repository.INSTANCE().dlna(deviceId,
                                    ip = loginSession?.ip!!,
                                    token = LoginTokenUtil.getToken(),
                                    params = configPathParams(method, SharePathType.PUBLIC.type),
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

    //配置外部存储结果
    val configExtStoregePathResult = _isScanExternalStoragePath.switchMap {
        val method = if (it) "add" else "del"
        object : LiveData<Boolean>() {
            private val started = AtomicBoolean(false)
            override fun onActive() {
                super.onActive()
                if (started.compareAndSet(false, true)) {
                    val v5Observer = object : V5Observer<List<DLNAPathResult>>(deviceId) {
                        override fun success(result: BaseProtocol<List<DLNAPathResult>>) {
                            postValue(result.result)
                            if (!result.result) {
                                ToastUtils.showToast(V5HttpErrorNo.getResourcesId(result.error?.code
                                        ?: 0))
                            }
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
                    SessionManager.getInstance().getLoginSession(deviceId, object : GetSessionListener() {
                        override fun onSuccess(url: String?, loginSession: LoginSession?) {
                            V5Repository.INSTANCE().dlna(deviceId,
                                    ip = loginSession?.ip!!,
                                    token = LoginTokenUtil.getToken(),
                                    params = configPathParams(method, SharePathType.EXTERNAL_STORAGE.type),
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

    /**
     * 配置路径参数
     */
    private fun configPathParams(method: String, shareType: Int): JSONObject {
        val json = JSONObject()
        json.put("method", "scan")
        val params = JSONObject()
        params.put("cmd", method)
        val paths = JSONArray()
        paths.put(JSONObject().put("path", "/").put("share_path_type", shareType))
        params.put("media_path", paths)
        json.put("params", params)
        return json
    }

}