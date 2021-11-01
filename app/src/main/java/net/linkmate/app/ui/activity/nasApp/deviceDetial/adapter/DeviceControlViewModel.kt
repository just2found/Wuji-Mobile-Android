package net.linkmate.app.ui.activity.nasApp.deviceDetial.adapter

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.switchMap
import net.linkmate.app.data.model.CircleJoinWay
import net.linkmate.app.repository.CircleRepository
import net.linkmate.app.ui.activity.nasApp.deviceDetial.DeviceDetailViewModel
import net.sdvn.cmapi.CMAPI

/**
 * @author Raleigh.Luo
 * date：20/11/9 11
 * describe：
 */
class DeviceControlViewModel : DeviceDetailViewModel() {
    private val repository = CircleRepository()
    var deviceId = ""

    /*---获取-------------------------------------------------------------------------*/
    private val _startGetFees: MutableLiveData<Boolean> = MutableLiveData<Boolean>()
    fun startGetFees() {
        _startGetFees.value = true
    }

    val feeType: LiveData<CircleJoinWay.FeeType> = _startGetFees.switchMap {
        repository.getFeeType(feetype = "flow-netdev"
                , networkid = CMAPI.getInstance().baseInfo.netid, deviceid = deviceId, loaderStateListener = mStateListener)
    }

    /*---修改流量费付费方-------------------------------------------------------------------------*/
    private val _startAlterFlowFeePayer: MutableLiveData<Int> = MutableLiveData<Int>()
    val startAlterFlowFeePayer: LiveData<Int> = _startAlterFlowFeePayer
    fun startAlterFlowFeePayer(deviceId: String, chargetype: Int) {
        this.deviceId = deviceId
        _startAlterFlowFeePayer.value = chargetype
    }

    val alterFlowFeePayerResult = startAlterFlowFeePayer.switchMap {
        repository.alterFlowFeePayer(deviceId, it, mStateListener)
    }
}