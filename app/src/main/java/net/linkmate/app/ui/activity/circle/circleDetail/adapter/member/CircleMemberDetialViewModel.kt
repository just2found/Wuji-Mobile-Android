package net.linkmate.app.ui.activity.circle.circleDetail.adapter.member

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.switchMap
import net.linkmate.app.data.model.CircleMember
import net.linkmate.app.repository.CircleMemberRepository
import net.linkmate.app.ui.activity.circle.circleDetail.CircleFragmentViewModel

/**
 * @author Raleigh.Luo
 * date：20/10/14 14
 * describe：
 */
class CircleMemberDetialViewModel: CircleFragmentViewModel() {
    private val repository = CircleMemberRepository()
    var member: CircleMember.Member? = null
    /*---删除-------------------------------------------------------------------------*/
    private val _toDeleteMember: MutableLiveData<Boolean> = MutableLiveData<Boolean>()
    fun startDeleteMember() {
        _toDeleteMember.value = true
    }

    val isDeleteSuccess  =  _toDeleteMember.switchMap {
        repository.deleteMember(networkId, member?.userid?:"", mStateListener)
    }
    /*---升级 降级-------------------------------------------------------------------------*/
    private val _toGradeMemberLevel: MutableLiveData<Int> = MutableLiveData<Int>()
    fun startGradeMember(level: Int) {
        _toGradeMemberLevel.value = level
    }

    val gradeMemberResult  =  _toGradeMemberLevel.switchMap {
        repository.gradeMember(networkId, member?.userid?:"",it, mStateListener)
    }
}