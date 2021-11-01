package net.linkmate.app.ui.activity.circle.circleDetail.adapter.device

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.switchMap
import net.linkmate.app.data.model.CircleDevice
import net.linkmate.app.repository.CircleDeviceRepository
import net.linkmate.app.ui.activity.circle.circleDetail.CircleFragmentViewModel

/**
 * @author Raleigh.Luo
 * date：20/10/14 14
 * describe：
 */
class CircleENDeviceDetailViewModel: CircleFragmentViewModel() {
    private val repository = CircleDeviceRepository()


    var enDevice: CircleDevice.Device? = null
    /*---取消合作-------------------------------------------------------------------------*/
    private val _enableENDevice: MutableLiveData<Boolean> = MutableLiveData<Boolean>()
    fun startEnableENDevice(enable: Boolean) {
        _enableENDevice.value = enable
    }
    val enableENDeviceResult = _enableENDevice.switchMap {
        repository.enableENDevice( networkId, enDevice?.deviceid!!,it, mUnDismissStateListener)
    }
}