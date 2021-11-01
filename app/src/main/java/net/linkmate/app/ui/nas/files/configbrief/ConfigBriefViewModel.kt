package net.linkmate.app.ui.nas.files.configbrief

import android.util.Base64
import android.util.Log
import androidx.annotation.Keep
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.switchMap
import io.objectbox.TxCallback
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import io.weline.repo.data.model.BaseProtocol
import io.weline.repo.data.model.BriefTimeStamp
import io.weline.repo.net.V5Observer
import io.weline.repo.repository.V5Repository
import net.linkmate.app.util.ToastUtils
import net.sdvn.common.internet.utils.LoginTokenUtil
import net.sdvn.common.repo.BriefRepo
import net.sdvn.nascommon.SessionManager
import net.sdvn.nascommon.iface.GetSessionListener
import net.sdvn.nascommon.model.oneos.user.LoginSession
import net.sdvn.nascommon.viewmodel.RxViewModel
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.IOException
import java.lang.Exception
import java.util.concurrent.atomic.AtomicBoolean

/**
 * @author Raleigh.Luo
 * date：21/5/6 15
 * describe：
 */
class ConfigBriefViewModel @Keep constructor(val deviceId: String, val For: String, val type: Int) : RxViewModel() {

    private var deviceIP: String = ""

    /***----------------loading--------------------------------***/
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading
    fun isLoading(loading: Boolean) {
        _isLoading.value = loading
    }

    /**
     * 开始配置简介
     */
    fun startConfigBrief(path: String) {
        SessionManager.getInstance().getLoginSession(deviceId, object : GetSessionListener() {
            override fun onSuccess(url: String?, loginSession: LoginSession) {
                deviceIP = loginSession.ip
                startConfig.value = path
//                viewModelScope.launch(Dispatchers.IO) {//开启协程，下载文件
//
//                }
            }

            override fun onFailure(url: String?, errorNo: Int, errorMsg: String?) {
                super.onFailure(url, errorNo, errorMsg)
                ToastUtils.showError(errorNo, errorMsg)
                isLoading(false)
            }
        })
    }

    //value 存储路径
    val startConfig = MutableLiveData<String>()

    val configResult: LiveData<BaseProtocol<BriefTimeStamp>> = startConfig.switchMap {
        object : LiveData<BaseProtocol<BriefTimeStamp>>() {
            private val started = AtomicBoolean(false)
            override fun onActive() {
                super.onActive()
                if (started.compareAndSet(false, true)) {
                    val observer = object : V5Observer<BriefTimeStamp>(deviceId) {
                        override fun onSubscribe(d: Disposable) {
                            super.onSubscribe(d)
                            addDisposable(d)
                        }

                        override fun success(result: BaseProtocol<BriefTimeStamp>) {
                            postValue(result)
                        }

                        override fun fail(result: BaseProtocol<BriefTimeStamp>) {
                            postValue(result)
                        }

                        override fun isNotV5() {
                            postValue(null)
                        }

                        override fun retry(): Boolean {
                            return true
                        }
                    }
                    encodeFileToBase64(it)?.let {
                        V5Repository.INSTANCE().setBrief(deviceId, deviceIP, LoginTokenUtil.getToken(),
                                type, For, it)
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(observer)
                        true
                    } ?: let {//解析失败
                        postValue(null)
                    }
                }
            }
        }
    }


    /**
     * encodeSHA_256 file to Base64 String
     *
     * @param path file path
     * @return
     * @throws FileNotFoundException
     * @throws IOException
     */
    fun encodeFileToBase64(path: String): String? {
        try {
            val file = File(path)
            if (!file.exists()) {
                return null
            }
            val inputFile = FileInputStream(file)
            val buffer = ByteArray(file.length().toInt())
            inputFile.read(buffer)
            inputFile.close()
            return Base64.encodeToString(buffer, Base64.NO_WRAP)
        } catch (e: Exception) {
            return null
        }
    }

}