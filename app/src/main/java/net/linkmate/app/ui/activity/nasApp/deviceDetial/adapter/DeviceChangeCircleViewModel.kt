package net.linkmate.app.ui.activity.nasApp.deviceDetial.adapter

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.switchMap
import io.reactivex.disposables.Disposable
import net.linkmate.app.repository.CircleDeviceRepository
import net.linkmate.app.ui.activity.nasApp.deviceDetial.DeviceDetailViewModel
import net.sdvn.common.internet.core.HttpLoader
import net.sdvn.common.repo.NetsRepo

/**
 * @author Raleigh.Luo
 * date：21/6/15 13
 * describe：
 */
class DeviceChangeCircleViewModel : DeviceDetailViewModel() {
    private val repository = CircleDeviceRepository()
    var deviceId: String = ""
    val sources = NetsRepo.getData2()

    /*--- 将我的设备加入到圈子-------------------------------------------------------------------------*/
    private val _joinCircleDeviceId: MutableLiveData<String> = MutableLiveData<String>()
    val joinCircleDeviceId: LiveData<String> = _joinCircleDeviceId
    fun startDeviceJoinCircle(networkId: String) {
        _joinCircleDeviceId.value = networkId
    }

    val deviceJoinCircleResult: LiveData<Boolean> = joinCircleDeviceId.switchMap {
        repository.deviceJoinCircle(it, deviceId, object : HttpLoader.HttpLoaderStateListener{
            override fun onLoadComplete() {
            }

            override fun onLoadStart(disposable: Disposable?) {
                mStateListener.onLoadStart(disposable)
            }

            override fun onLoadError() {
            }
        })
    }
}