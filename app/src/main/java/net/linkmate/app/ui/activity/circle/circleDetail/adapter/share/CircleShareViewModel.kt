package net.linkmate.app.ui.activity.circle.circleDetail.adapter.share

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.switchMap
import net.linkmate.app.repository.CircleShareRepository
import net.linkmate.app.ui.activity.circle.circleDetail.CircleFragmentViewModel
import net.sdvn.common.internet.protocol.ShareCode

/**
 * @author Raleigh.Luo
 * date：20/10/14 13
 * describe：
 */
class CircleShareViewModel : CircleFragmentViewModel(){
    private val repository = CircleShareRepository()
    /*---开始请求远程数据-------------------------------------------------------------------------*/
    private val _startRequestRemoteSource: MutableLiveData<Boolean> = MutableLiveData<Boolean>()
    fun startRequestRemoteSource() {
        _startRequestRemoteSource.value = true
    }
    val shareCodeResult:LiveData<ShareCode> = _startRequestRemoteSource.switchMap {
        repository.getNetworkShareCode(networkId, mStateListener)
    }

    private val _startSetNetworkConfirm: MutableLiveData<Boolean> = MutableLiveData<Boolean>()
    fun startSetNetworkConfirm(join_confirm: Boolean) {
        _startSetNetworkConfirm.value = join_confirm
    }
    val setNetworkConfirmResult:LiveData<Boolean> = _startSetNetworkConfirm.switchMap {
        repository.setNetworkConfirm(networkId, join_confirm = it, loaderStateListener = mStateListener)
    }
}