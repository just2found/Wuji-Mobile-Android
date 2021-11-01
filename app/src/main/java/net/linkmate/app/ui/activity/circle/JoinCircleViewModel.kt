package net.linkmate.app.ui.activity.circle

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.switchMap
import io.reactivex.disposables.Disposable
import net.linkmate.app.base.BaseViewModel
import net.linkmate.app.data.model.CircleDetail
import net.linkmate.app.data.model.CircleJoinWay
import net.linkmate.app.repository.CircleRepository
import net.sdvn.common.internet.core.HttpLoader

/**
 * @author Raleigh.Luo
 * date：20/10/12 14
 * describe：
 */
class JoinCircleViewModel : BaseViewModel() {
    private val repository = CircleRepository()
    var shareCode = ""
    var seletedJoinMode: CircleJoinWay.Fee? = null

    /*****请求获取圈子详情***************************************************/
    private val _startRequestCircleDetail: MutableLiveData<Boolean> = MutableLiveData<Boolean>()
    fun startRequestCircleDetail() {
        _startRequestCircleDetail.value = true
    }

    val circleDetail: LiveData<CircleDetail.Circle> = _startRequestCircleDetail.switchMap {
        repository.getCircleDetial(shareCode = shareCode, loaderStateListener = mStateListener)
    }

    private val _startRequestJoinCircle: MutableLiveData<Boolean> = MutableLiveData<Boolean>()
    fun startRequestJoinCircle() {
        _startRequestJoinCircle.value = true
    }

    val joinResult: LiveData<Boolean> = _startRequestJoinCircle.switchMap {
        repository.joinCircle(circleDetail.value?.networkid
                ?: "", shareCode, seletedJoinMode?.feeid, seletedJoinMode?.value, loaderStateListener = mStateListener)
    }

    /*****订阅主EN***************************************************/
    fun startSubscribeMainEN(): LiveData<Boolean>{
        val networkid = circleDetail.value?.networkid ?: ""
        val deviceId = circleDetail.value?.getMainENDeviceId() ?: ""
        return repository.subscribeMainEN(networkid, deviceId, shareCode)
    }
    var subscribeMainENResult: LiveData<Boolean>? = null

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
