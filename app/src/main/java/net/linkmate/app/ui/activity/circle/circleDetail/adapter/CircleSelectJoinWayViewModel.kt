package net.linkmate.app.ui.activity.circle.circleDetail.adapter

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.switchMap
import net.linkmate.app.data.model.CircleJoinWay
import net.linkmate.app.repository.CircleRepository
import net.linkmate.app.ui.activity.circle.circleDetail.CircleFragmentViewModel

/**
 * @author Raleigh.Luo
 * date：20/10/14 13
 * describe：
 */
class CircleSelectJoinWayViewModel : CircleFragmentViewModel(){
    private val repository = CircleRepository()
    /*---开始请求远程数据-------------------------------------------------------------------------*/
    private val _startRequestRemoteSource: MutableLiveData<Boolean> = MutableLiveData<Boolean>()
    val startRequestRemoteSource: LiveData<Boolean> = _startRequestRemoteSource
    fun startRequestRemoteSource() {
        _startRequestRemoteSource.value = true
    }
    val joinWays :LiveData<List<CircleJoinWay.Fee>> =  startRequestRemoteSource.switchMap {
        repository.getFee(networkId,"net-join", mStateListener)
    }
}