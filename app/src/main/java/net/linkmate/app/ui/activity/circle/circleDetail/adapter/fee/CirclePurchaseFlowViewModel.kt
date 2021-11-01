package net.linkmate.app.ui.activity.circle.circleDetail.adapter.fee

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.switchMap
import net.linkmate.app.R
import net.linkmate.app.base.MyApplication
import net.linkmate.app.data.model.CircleJoinWay
import net.linkmate.app.repository.CircleRepository
import net.linkmate.app.ui.activity.circle.circleDetail.CircleFragmentViewModel

/**选购流量
 * @author Raleigh.Luo
 * date：20/10/17 11
 * describe：
 */
class CirclePurchaseFlowViewModel : CircleFragmentViewModel() {
    private val repository = CircleRepository()
    //是否当前费用到后生效,如果为是则为到期续费,否则费用立即生效
    var expire_renew:Boolean? = null

    /*---获取-------------------------------------------------------------------------*/
    private val _startGetFees: MutableLiveData<Boolean> = MutableLiveData<Boolean>(true)
    fun startGetFees() {
        _startGetFees.value = true
    }

    val feeType: LiveData<CircleJoinWay.FeeType> = _startGetFees.switchMap {
        repository.getFeeType(networkId, "flow-net", mStateListener)
    }
    val fees: LiveData<List<CircleJoinWay.Fee>> = feeType.switchMap {
        object : LiveData<List<CircleJoinWay.Fee>>() {
            override fun onActive() {
                super.onActive()
                var isEmpty = true
                it.fees?.let {
                    if (it.size > 0) {
                        postValue(it)
                        isEmpty = false
                    }
                }
                if (isEmpty) {
                    //获取的数据为空，且可创建免费方式
                    if (it.isfree ?: false) {//是空的，就创建一项 免费的
                        val list = ArrayList<CircleJoinWay.Fee>()
                        list.add(CircleJoinWay.Fee(title = MyApplication.getContext().getString(R.string.free_join_circle)))
                        postValue(list)
                    } else {
                        postValue(null)
                    }
                }

            }
        }
    }

    /*---购买-------------------------------------------------------------------------*/
    private val _startPurchase: MutableLiveData<CircleJoinWay.Fee> = MutableLiveData<CircleJoinWay.Fee>()
    fun startPurchase(fee: CircleJoinWay.Fee) {
        _startPurchase.value = fee
    }

    val purchaseResult: LiveData<Boolean> = _startPurchase.switchMap {
        repository.setFee(networkId, "flow-net", it.feeid, it.value ?: 0f, mStateListener,
                expire_renew = expire_renew)
    }
}