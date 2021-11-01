package net.linkmate.app.ui.activity.mine

import android.text.TextUtils
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.switchMap
import libs.source.common.livedata.Resource
import net.linkmate.app.data.model.IdentifyCode
import net.linkmate.app.ui.simplestyle.BriefCacheViewModel
import net.sdvn.cmapi.CMAPI
import net.sdvn.common.internet.core.GsonBaseProtocol
import net.sdvn.common.internet.core.V1AgApiHttpLoader
import net.sdvn.common.internet.core.V2AgApiHttpLoader
import net.sdvn.common.internet.listener.ResultListener
import net.sdvn.common.internet.protocol.GetUserInfoResultBean
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean

/**
 * @author Raleigh.Luo
 * date：21/3/22 10
 * describe：
 */
class IdentifyCodeViewModel : BriefCacheViewModel() {
    private val start = MutableLiveData<Boolean>()
    private var deviceId: String? = null
    fun start(deviceId: String? = null) {
        this.deviceId = deviceId
        start.value = true
    }

    val getIdentifyCodeResult = start.switchMap {
        object : LiveData<IdentifyCode>() {
            val started = AtomicBoolean(false)
            override fun onActive() {
                super.onActive()
                if (started.compareAndSet(false, true)) {
                    object : V2AgApiHttpLoader(IdentifyCode::class.java) {
                        init {
                            bodyMap = ConcurrentHashMap()
                            put("ticket", CMAPI.getInstance().getBaseInfo().getTicket())
                            deviceId?.let {
                                action = "getdeviceindentifycode"
                                put("deviceid", deviceId)
                                true
                            } ?: let {
                                action = "getuserindentifycode"
                            }

                        }
                    }.executor(object : ResultListener<IdentifyCode> {
                        override fun error(tag: Any?, baseProtocol: GsonBaseProtocol?) {
                            val identifyCode = IdentifyCode()
                            identifyCode.errmsg = baseProtocol?.errmsg
                            identifyCode.result = baseProtocol?.result ?: -1
                            postValue(identifyCode)
                        }

                        override fun success(tag: Any?, data: IdentifyCode?) {
                            postValue(data)
                        }
                    })
                }
            }
        }
    }
}