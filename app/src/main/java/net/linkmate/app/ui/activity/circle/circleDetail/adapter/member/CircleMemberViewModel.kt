package net.linkmate.app.ui.activity.circle.circleDetail.adapter.member

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.switchMap
import net.linkmate.app.data.model.CircleMember
import net.linkmate.app.repository.CircleMemberRepository
import net.linkmate.app.ui.activity.circle.circleDetail.CircleFragmentViewModel

/**
 * @author Raleigh.Luo
 * date：20/10/14 13
 * describe：
 */
class CircleMemberViewModel: CircleFragmentViewModel() {
    private val repository = CircleMemberRepository()
    /*---开始请求远程数据-------------------------------------------------------------------------*/
    private val _startRequestRemoteSource: MutableLiveData<Boolean> = MutableLiveData<Boolean>()
    fun startRequestRemoteSource() {
        _startRequestRemoteSource.value = true
    }
    val members:LiveData<List<CircleMember.Member>> = _startRequestRemoteSource.switchMap {
        repository.getMembers(networkId, mStateListener)
    }
}