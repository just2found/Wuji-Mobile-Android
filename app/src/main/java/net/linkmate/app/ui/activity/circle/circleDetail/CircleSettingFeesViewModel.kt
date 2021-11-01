package net.linkmate.app.ui.activity.circle.circleDetail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.switchMap
import io.reactivex.disposables.Disposable
import net.linkmate.app.data.model.CircleManagerFees
import net.linkmate.app.repository.CircleRepository
import net.sdvn.common.internet.core.HttpLoader

/**
 * @author Raleigh.Luo
 * date：20/10/16 10
 * describe：
 */
class CircleSettingFeesViewModel: ViewModel() {
    private val repository = CircleRepository()
    /**--请求列表--------------------***/
    private val _networkId: MutableLiveData<String> = MutableLiveData<String>()
    val networkId: LiveData<String> = _networkId
    //刷新数据
    fun startRequest(networkid: String? = null){
        if(networkid ==null){
            _networkId.value = _networkId.value
        }else{
            _networkId.value = networkid
        }

    }
    val fees = networkId.switchMap {
        repository.getManagerFees(networkid = it, loaderStateListener = mStateListener)
    }

    //使用MutableLiveData 只观察整体对象，不观察字段变化
    val currentOperateFee:MutableLiveData<CircleManagerFees.Fee> = MutableLiveData()


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