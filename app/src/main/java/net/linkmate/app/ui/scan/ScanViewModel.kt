package net.linkmate.app.ui.scan

import android.text.TextUtils
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.reactivex.disposables.Disposable
import libs.source.common.livedata.Resource
import libs.source.common.utils.ToastHelper
import net.linkmate.app.R
import net.linkmate.app.util.JsonUtil
import net.linkmate.app.util.ToastUtils
import net.sdvn.common.internet.core.GsonBaseProtocol
import net.sdvn.common.internet.core.HttpLoader
import net.sdvn.common.internet.listener.ResultListener
import net.sdvn.common.internet.loader.AuthQRCodeHttpLoader
import net.sdvn.nascommon.viewmodel.RxViewModel
import java.util.regex.Pattern

/**
 *
 * @Description: java类作用描述
 * @Author: todo2088
 * @CreateDate: 2021/2/22 21:44
 */
class ScanViewModel() : RxViewModel() {
    private val _liveDataCaptureAction = MutableLiveData<Int>()
    val liveDataCaptureAction: LiveData<Int> = _liveDataCaptureAction

    private val _liveDataCaptureResult = MutableLiveData<String>()
    val liveDataCaptureResult: LiveData<String> = _liveDataCaptureResult

    private val _liveDataHttpLoad = MutableLiveData<Resource<Any>>()
    val liveDataHttpLoad: LiveData<Resource<Any>> = _liveDataHttpLoad

    fun parseScanResult(scanResult: String): MutableMap<String, Any> {
        var map = mutableMapOf<String, Any>()
        // ver=1_ot=100_act=qrcode_uuid=faacda8d-0777-45b6-a8f2-8ba9a61eb571
        val patternAuth = "ver=\\d*_ot=\\d{1,3}_act=([a-z]{6})_uuid=([0-9a-z-]{36})"
        if (Pattern.compile(patternAuth).matcher(scanResult).matches()) {
            val strings: Array<String> = scanResult.split("_".toRegex()).toTypedArray()
            for (string in strings) {
                val keyValues = string.split("=".toRegex()).toTypedArray()
                map.put(keyValues[0].trim { it <= ' ' }, keyValues[1].trim { it <= ' ' })

            }
        } else if (JsonUtil.isJsonStr(scanResult)) {
            map = JsonUtil.parseJsonToMap(scanResult)
            val action = map["action"] as String?
            if (!TextUtils.isEmpty(action)) {
                when (action) {
                    "qrcode" -> {
                        return map
                    }
                }
            }
        }
        return map
    }

    fun restartPreview() {
        _liveDataCaptureAction.postValue(CaptureActionReStart)
    }

    fun onSuccess(result: String?) {
        _liveDataCaptureResult.postValue(result)
    }

    fun onOpenCameraError() {
        ToastUtils.showToast(R.string.open_camera_error)
    }


    private val httpLoaderStateListener = object : HttpLoader.HttpLoaderStateListener {
        override fun onLoadComplete() {
            _liveDataHttpLoad.postValue(Resource.success(null))
        }

        override fun onLoadStart(disposable: Disposable) {
            addDisposable(disposable)
            _liveDataHttpLoad.postValue(Resource.loading(disposable))
        }

        override fun onLoadError() {
            _liveDataHttpLoad.postValue(Resource.error("", null))
        }
    }

    fun requestSignIn(map: Map<String, Any>) {
        val authQRCodeHttpLoader = AuthQRCodeHttpLoader(GsonBaseProtocol::class.java)
        authQRCodeHttpLoader.setParams(map.get("uuid") as String)
        authQRCodeHttpLoader.setHttpLoaderStateListener(httpLoaderStateListener)
        authQRCodeHttpLoader.executor(object : ResultListener<GsonBaseProtocol?> {
            override fun error(tag: Any?, mErrorProtocol: GsonBaseProtocol) {
                ToastUtils.showError(mErrorProtocol.result)
            }

            override fun success(tag: Any?, data: GsonBaseProtocol?) {
                ToastHelper.showLongToast(R.string.auth_success)
            }

        })
    }

}

const val CaptureActionStart = 0x1
const val CaptureActionStop = 0x2
const val CaptureActionReStart = CaptureActionStart or CaptureActionStop
