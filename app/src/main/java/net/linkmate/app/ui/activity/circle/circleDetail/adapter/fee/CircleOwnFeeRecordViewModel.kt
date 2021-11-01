package net.linkmate.app.ui.activity.circle.circleDetail.adapter.fee

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.switchMap
import net.linkmate.app.data.model.CircleFeeRecords
import net.linkmate.app.repository.CircleRepository
import net.linkmate.app.ui.activity.circle.circleDetail.CircleFragmentViewModel

/**
 * @author Raleigh.Luo
 * date：20/10/15 19
 * describe：
 */
class CircleOwnFeeRecordViewModel : CircleFragmentViewModel() {
    private val repository = CircleRepository()

    /**---请求费用列表-------------------------------------------------*/
    private val _startGetRecords: MutableLiveData<Boolean> = MutableLiveData<Boolean>()
    fun startGetRecords() {
        _startGetRecords.value = true
    }

    val records: LiveData<List<CircleFeeRecords.Record>> = _startGetRecords?.switchMap {
        repository.getFeeRecords(networkId,status = "available", loaderStateListener = mStateListener)
    }
}