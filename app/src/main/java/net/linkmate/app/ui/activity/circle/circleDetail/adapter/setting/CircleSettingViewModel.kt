package net.linkmate.app.ui.activity.circle.circleDetail.adapter.setting

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.switchMap
import net.linkmate.app.repository.CircleRepository
import net.linkmate.app.ui.activity.circle.circleDetail.CircleFragmentViewModel

/**
 * @author Raleigh.Luo
 * date：20/10/14 14
 * describe：
 */
class CircleSettingViewModel : CircleFragmentViewModel() {
    private val repository = CircleRepository()
    /*---退出圈子-------------------------------------------------------------------------*/
    private val _startExitCircle: MutableLiveData<Boolean> = MutableLiveData<Boolean>()
    fun startExitCircle() {
        _startExitCircle.value = true
    }

    val exitCircleResult: LiveData<Boolean> = _startExitCircle.switchMap {
        repository.userExitCircle(networkId, mStateListener)
    }
    /*---修改圈子名称-------------------------------------------------------------------------*/
    private val _alterCircleName: MutableLiveData<String> = MutableLiveData<String>()
    val alterCircleName: LiveData<String> = _alterCircleName
    fun startAlterCircleName(name:String) {
        _alterCircleName.value = name
    }

    val alterCircleNameResult: LiveData<Boolean> = alterCircleName.switchMap {
        repository.alterCircleName(networkId, it,mUnDismissStateListener)
    }
}