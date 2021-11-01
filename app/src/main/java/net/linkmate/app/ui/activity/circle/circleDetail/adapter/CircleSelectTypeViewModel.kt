package net.linkmate.app.ui.activity.circle.circleDetail.adapter

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.switchMap
import net.linkmate.app.data.model.CircleType
import net.linkmate.app.repository.CircleRepository
import net.linkmate.app.ui.activity.circle.circleDetail.CircleFragmentViewModel

/**
 * @author Raleigh.Luo
 * date：20/10/14 11
 * describe：
 */
class CircleSelectTypeViewModel: CircleFragmentViewModel() {
    private val repository = CircleRepository()

    /*---开始请求远程数据-------------------------------------------------------------------------*/
    private val _startRequestRemoteSource: MutableLiveData<Boolean> = MutableLiveData<Boolean>()
    fun startRequestRemoteSource() {
        _startRequestRemoteSource.value = true
    }
    val circleTypes: LiveData<List<CircleType.Type>> = _startRequestRemoteSource.switchMap {
        repository.getCircleTypes(mStateListener)
    }
}