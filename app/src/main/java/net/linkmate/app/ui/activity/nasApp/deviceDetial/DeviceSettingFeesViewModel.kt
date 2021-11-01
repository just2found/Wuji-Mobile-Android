package net.linkmate.app.ui.activity.nasApp.deviceDetial

import android.text.TextUtils
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.switchMap
import io.reactivex.disposables.Disposable
import net.linkmate.app.data.model.CircleManagerFees
import net.linkmate.app.repository.CircleRepository
import net.sdvn.cmapi.CMAPI
import net.sdvn.common.internet.core.HttpLoader

/**
 * @author Raleigh.Luo
 * date：20/10/18 14
 * describe：
 */
class DeviceSettingFeesViewModel : ViewModel() {
    private val repository = CircleRepository()

    /**--请求列表--------------------***/
    private val _deviceId: MutableLiveData<String> = MutableLiveData<String>()
    val devcieId: LiveData<String> = _deviceId

    //是否是EN服务器，否则为绑定设备
    var isENServer = false

    private var networkId: String? = null

    //刷新数据
    fun startRequest(networkId: String? = null, deviceId: String? = null) {
        if (!TextUtils.isEmpty(networkId)) {
            this.networkId = networkId
        }
        if (deviceId == null) {
            _deviceId.value = _deviceId.value
        } else {
            _deviceId.value = deviceId
        }

    }

    val fees = devcieId.switchMap {
        //EN服务传networkid 绑定设备只传deviceid
//        val networkid = if (isENServer) networkId else null
        repository.getManagerFees(networkid = networkId, deviceid = it, loaderStateListener = mStateListener)
    }

    //使用MutableLiveData 只观察整体对象，不观察字段变化
    val currentOperateFee: MutableLiveData<CircleManagerFees.Fee> = MutableLiveData()


    /**--加载进度条--------------------***/
    private val _isLoading: MutableLiveData<Boolean> = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading
    fun setLoadingStatus(isLoading: Boolean) {
        _isLoading.value = isLoading
    }

    val mStateListener: HttpLoader.HttpLoaderStateListener = object : HttpLoader.HttpLoaderStateListener {
        override fun onLoadComplete() {
            _isLoading.value = false
        }

        override fun onLoadStart(disposable: Disposable?) {
            _isLoading.value = true
        }

        override fun onLoadError() {
            _isLoading.value = false
        }
    }
}