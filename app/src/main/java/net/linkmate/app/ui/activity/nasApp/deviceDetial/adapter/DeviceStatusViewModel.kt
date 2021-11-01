package net.linkmate.app.ui.activity.nasApp.deviceDetial.adapter

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.switchMap
import net.linkmate.app.base.MyOkHttpListener
import net.linkmate.app.ui.activity.nasApp.deviceDetial.DeviceDetailViewModel
import net.linkmate.app.util.ToastUtils
import net.sdvn.cmapi.CMAPI
import net.sdvn.common.data.model.CircleDevice
import net.sdvn.common.internet.core.GsonBaseProtocol
import net.sdvn.common.internet.core.V2AgApiHttpLoader
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean

/**
 * @author Raleigh.Luo
 * date：21/6/15 15
 * describe：
 */
class DeviceStatusViewModel : DeviceDetailViewModel() {
    var deviceId: String = ""
    private val _startEnableCloudDevice = MutableLiveData<Boolean>()
    val startEnableCloudDevice: LiveData<Boolean> = _startEnableCloudDevice
    fun startEnableCloudDevice(isEnable: Boolean) {
        _startEnableCloudDevice.value = isEnable
    }

    val enableCloudDeviceResult = startEnableCloudDevice.switchMap {
        object : LiveData<Boolean>() {
            val started = AtomicBoolean(false)
            override fun onActive() {
                super.onActive()
                if (started.compareAndSet(false, true)) {
                    val loader = object : V2AgApiHttpLoader(CircleDevice::class.java) {
                        init {
                            action = "ableyunen"
                            bodyMap = ConcurrentHashMap()
                            put("ticket", CMAPI.getInstance().baseInfo.ticket)
                            put("deviceid", deviceId)
                            put("able", it)
                        }
                    }
                    loader.executor(object : MyOkHttpListener<GsonBaseProtocol>() {
                        override fun error(tag: Any?, baseProtocol: GsonBaseProtocol?) {
                            super.error(tag, baseProtocol)
                            postValue(false)
                        }

                        override fun success(tag: Any?, data: GsonBaseProtocol?) {
                            postValue(data?.result == 0)
                            if (data?.result != 0) {
                                ToastUtils.showError(data?.result ?: 0, data?.errmsg)
                            }
                        }
                    })
                }
            }
        }
    }
}