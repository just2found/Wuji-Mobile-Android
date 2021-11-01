package net.linkmate.app.ui.activity.circle

import android.text.TextUtils
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.switchMap
import io.reactivex.disposables.Disposable
import net.linkmate.app.data.model.CircleType
import net.linkmate.app.repository.CircleRepository
import net.sdvn.common.internet.core.HttpLoader

/**
 * @author Raleigh.Luo
 * date：20/8/21 16
 * describe：
 */
class CreateCircleViewModel : ViewModel() {
    private val repository = CircleRepository()
    var circleType: CircleType.Type? = null
        set(value) {
            field = value
            checkParams()
        }
    var circleName: String? = null
        set(value) {
            field = value
            checkParams()
        }
    private val startRequestCreateCircle: MutableLiveData<Boolean> = MutableLiveData()

    /**
     * 开始创建圈子请求
     */
    fun startRequestCreateCircle() {
        startRequestCreateCircle.value = true
    }

    val createResult: LiveData<Boolean> = startRequestCreateCircle.switchMap {

        repository.createCircle(circleType?.modelid ?: "", circleName ?: ""
        ?: "", getCreateFee(), mStateListener)
    }

    /**
     * 获取网络创建保证金
     */
    fun getCreateFee(): String {
        //网络创建保证金
        var fee = ""
        circleType?.modelprops?.network_fee?.filter {
            it.key == "create_fee" ||  it.key == "create_deposit"
        }?.let {
            if(it.size > 1){//有两个 积分＋质押金
                val total = (it.get(0).value?.toFloat()?:0f) + (it.get(1).value?.toFloat()?:0f)
                fee = String.format("%.2f",total)
            }else if(it.size > 0){
                fee = it.get(0).value ?: ""
            }
        }


        return fee
    }

    private val _nextButtonEnable: MutableLiveData<Boolean> = MutableLiveData()
    val nextButtonEnable: LiveData<Boolean> = _nextButtonEnable

    /**
     * 检查参数，提交按钮是否可用
     */
    private fun checkParams() {
        val isEnable = !TextUtils.isEmpty(circleName) && circleType!=null
        _nextButtonEnable.value = isEnable
    }

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