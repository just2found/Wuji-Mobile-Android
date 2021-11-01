package net.linkmate.app.ui.activity.circle.circleDetail.adapter

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.switchMap
import net.linkmate.app.data.model.CircleManagerFees
import net.linkmate.app.repository.CircleRepository
import net.linkmate.app.ui.activity.circle.circleDetail.CircleFragmentViewModel

/**
 * @author Raleigh.Luo
 * date：20/10/14 11
 * describe：
 */
class CircleSettingFeesDetialViewModel : CircleFragmentViewModel() {
    private val repository = CircleRepository()
    var fee:CircleManagerFees.Fee? = null
    private val _isEnable: MutableLiveData<Boolean> = MutableLiveData<Boolean>()
    val isEnable: LiveData<Boolean> = _isEnable
    fun startEnable(enable: Boolean) {
        _isEnable.value = enable
    }

    val enableResult = isEnable.switchMap {
        repository.setCharge(networkid = networkId, feetype = fee?.feetype?:"",basic_feeid = fee?.basic?.feeid, enable = it, loaderStateListener = mStateListener);
    }
    private val _alterVaddValue: MutableLiveData<Float> = MutableLiveData<Float>()
    val alterVaddValue: MutableLiveData<Float> = _alterVaddValue
    fun startAlterVaddValue(value: Float) {
        _alterVaddValue.value = value
    }

    val alterVaddResult = alterVaddValue.switchMap {
        repository.setCharge(networkid = networkId, feetype = fee?.feetype?:"", basic_feeid = fee?.basic?.feeid,value = it,vadd_feeid = fee?.vadd?.feeid, loaderStateListener = mStateListener);
    }
}