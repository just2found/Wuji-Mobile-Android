package net.linkmate.app.ui.activity.circle.circleDetail.adapter.fee

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.switchMap
import net.linkmate.app.repository.CircleRepository
import net.linkmate.app.ui.activity.circle.circleDetail.CircleFragmentViewModel

/**
 * @author Raleigh.Luo
 * date：20/10/20 20
 * describe：
 */
class CircleENServerFeesViewModel : CircleFragmentViewModel() {
    private val repository = CircleRepository()
    private val _requestId: MutableLiveData<String> = MutableLiveData()
    fun startRemoteRequest(id: String) {
        _requestId.value = id
    }

    val fees = _requestId.switchMap {
        repository.getShare(networkid = it, loaderStateListener = mStateListener)
    }

    private val _shareFees: MutableLiveData<Float> = MutableLiveData()
    fun startSetShare(share: Float) {
        _shareFees.value = share
    }
    val setShareResult = _shareFees.switchMap {
        repository.setShare(networkid = _requestId.value,owner_share = it, loaderStateListener = mStateListener)
    }

}