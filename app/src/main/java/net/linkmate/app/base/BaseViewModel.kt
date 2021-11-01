package net.linkmate.app.base

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import net.linkmate.app.base.BaseActivity.LoadingStatus
import net.linkmate.app.util.MySPUtils
import net.sdvn.nascommon.viewmodel.RxViewModel

/**
 * @author Raleigh.Luo
 * date：20/11/26 10
 *
 * describe：
 */
open class BaseViewModel : RxViewModel() {
    /**--loading dialog 进行了取消操作 -------------------------------------------***/
    private val _cancelLoading: MutableLiveData<LoadingStatus> = MutableLiveData()
    val cancelLoading: LiveData<LoadingStatus> = _cancelLoading
    fun cancelLoading(cancelStatus: LoadingStatus?) {
        _cancelLoading.value = cancelStatus
    }

    //本次运行，曾经登录过，每次登录成功都会调用
    //仅Activity中有效，
    val hasLoggedin = MutableLiveData<Boolean>()

    //是否登录过
    fun checkLoggedin(): Boolean {
        return MySPUtils.getBoolean(MyConstants.IS_LOGINED) && (hasLoggedin.value ?: false)
    }
}