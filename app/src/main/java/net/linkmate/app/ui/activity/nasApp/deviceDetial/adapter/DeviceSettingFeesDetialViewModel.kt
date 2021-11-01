package net.linkmate.app.ui.activity.nasApp.deviceDetial.adapter

import android.text.TextUtils
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.switchMap
import net.linkmate.app.data.model.CircleManagerFees
import net.linkmate.app.repository.CircleRepository
import net.linkmate.app.ui.activity.nasApp.deviceDetial.DeviceDetailViewModel
import net.sdvn.cmapi.CMAPI

/**设置收费项 详情
 * @author Raleigh.Luo
 * date：20/10/18 16
 * describe：
 */
class DeviceSettingFeesDetialViewModel : DeviceDetailViewModel() {
    private var networkId: String? = null

    //设置提供服务的圈子ID（处理默认圈子数据为空问题）
    fun setNetworkId(networkId: String?) {
        if (!TextUtils.isEmpty(networkId)) {
            this.networkId = networkId
        }
    }

    private val repository = CircleRepository()
    var deviceId: String = ""
    var fee: CircleManagerFees.Fee? = null

    //是否是EN服务器，否则为绑定设备
    var isENServer = false
    private val _isEnable: MutableLiveData<Boolean> = MutableLiveData<Boolean>()
    val isEnable: LiveData<Boolean> = _isEnable
    fun startEnable(enable: Boolean) {
        _isEnable.value = enable
    }

    val enableResult = isEnable.switchMap {
        //EN服务传networkid 绑定设备只传deviceid
        val networkId = if (isENServer) networkId else null
        repository.setCharge(networkid = networkId, deviceid = deviceId, feetype = fee?.feetype
                ?: "", basic_feeid = fee?.basic?.feeid, enable = it, loaderStateListener = mStateListener);
    }
    private val _alterVaddValue: MutableLiveData<Float> = MutableLiveData<Float>()
    val alterVaddValue: MutableLiveData<Float> = _alterVaddValue
    fun startAlterVaddValue(value: Float) {
        _alterVaddValue.value = value
    }

    val alterVaddResult = alterVaddValue.switchMap {
        //EN服务传networkid 绑定设备只传deviceid
//        val networkId = if (isENServer) networkId else null
        repository.setCharge(networkid = networkId, deviceid = deviceId, feetype = fee?.feetype
                ?: "", basic_feeid = fee?.basic?.feeid, value = it, vadd_feeid = fee?.vadd?.feeid, loaderStateListener = mStateListener);
    }
}