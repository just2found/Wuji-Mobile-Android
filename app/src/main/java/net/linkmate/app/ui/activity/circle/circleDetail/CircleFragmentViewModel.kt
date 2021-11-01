package net.linkmate.app.ui.activity.circle.circleDetail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import net.sdvn.common.internet.core.HttpLoader

/**
 * @author Raleigh.Luo
 * date：20/8/14 11
 * describe：
 */
open class CircleFragmentViewModel : ViewModel() {
    var function: Int = 0
    protected lateinit var networkId: String
    protected lateinit var mStateListener: HttpLoader.HttpLoaderStateListener
    protected lateinit var mUnDismissStateListener: HttpLoader.HttpLoaderStateListener

    fun init(networkId: String, mStateListener: HttpLoader.HttpLoaderStateListener, mUnDismissStateListener: HttpLoader.HttpLoaderStateListener) {
        this.networkId = networkId
        this.mStateListener = mStateListener
        this.mUnDismissStateListener = mUnDismissStateListener
    }

    /*---头部和底部样式---------------------------------------------------------*/
    private val _viewStatusParams: MutableLiveData<FunctionHelper.ViewStatusParams> = MutableLiveData<FunctionHelper.ViewStatusParams>()
    val viewStatusParams: LiveData<FunctionHelper.ViewStatusParams> = _viewStatusParams
    fun setViewStatusParams(params: FunctionHelper.ViewStatusParams) {
        _viewStatusParams.value = params
    }

    fun updateViewStatusParams(headerIcon: Int? = null,
                               headerTitle: String? = null,
                               headerDescribe: String? = null,
                               headBackButtonVisibility: Int? = null,
                               bottomAddTitle: String? = null,
                               bottomAddIsEnable: Boolean? = null,
                               bottomTitle: String? = null,
                               bottomIsFullButton: Boolean? = null,
                               bottomIsEnable: Boolean? = null) {
        if (_viewStatusParams.value == null) _viewStatusParams.value = FunctionHelper.ViewStatusParams()
        val params = _viewStatusParams.value
        headerIcon?.let {
            params?.headerIcon = headerIcon
        }
        headerTitle?.let {
            params?.headerTitle = headerTitle
        }
        headerDescribe?.let {
            params?.headerDescribe = headerDescribe
        }
        headBackButtonVisibility?.let {
            params?.headBackButtonVisibility = headBackButtonVisibility
        }
        bottomTitle?.let {
            params?.bottomTitle = bottomTitle
        }
        bottomIsFullButton?.let {
            params?.bottomIsFullButton = bottomIsFullButton
        }
        bottomIsEnable?.let {
            params?.bottomIsEnable = bottomIsEnable
        }
        bottomAddTitle?.let {
            params?.bottomAddTitle = bottomAddTitle
        }
        bottomAddIsEnable?.let {
            params?.bottomAddIsEnable = bottomAddIsEnable
        }
        _viewStatusParams.postValue(params)
    }

}