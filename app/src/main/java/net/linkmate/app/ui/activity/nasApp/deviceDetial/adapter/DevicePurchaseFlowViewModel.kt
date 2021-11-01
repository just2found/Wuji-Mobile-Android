package net.linkmate.app.ui.activity.nasApp.deviceDetial.adapter

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.switchMap
import net.linkmate.app.R
import net.linkmate.app.base.MyApplication
import net.linkmate.app.data.model.CircleJoinWay
import net.linkmate.app.repository.CircleRepository
import net.linkmate.app.ui.activity.nasApp.deviceDetial.DeviceDetailViewModel
import net.linkmate.app.ui.activity.nasApp.deviceDetial.FunctionHelper
import net.sdvn.cmapi.CMAPI

/**
 * @author Raleigh.Luo
 * date：20/10/18 17
 * describe：
 */
class DevicePurchaseFlowViewModel  : DeviceDetailViewModel(){
    private val repository = CircleRepository()
    var deviceId = ""
    //是否当前费用到后生效,如果为是则为到期续费,否则费用立即生效
    var expire_renew:Boolean? = null

    /*---获取-------------------------------------------------------------------------*/
    private val _startGetFees: MutableLiveData<Boolean> = MutableLiveData<Boolean>(true)
    fun startGetFees() {
        _startGetFees.value = true
    }

    val feeType: LiveData<CircleJoinWay.FeeType> = _startGetFees.switchMap {
        repository.getFeeType(feetype = if(function == FunctionHelper.DEVICE_PURCHASE_FLOW)"flow-netdev" else "netdev-join"
                ,networkid = CMAPI.getInstance().baseInfo.netid, deviceid = deviceId, loaderStateListener = mStateListener)
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
        repository.setFee(feetype = if(function == FunctionHelper.DEVICE_PURCHASE_FLOW)"flow-netdev" else "netdev-join"
                ,networkid = CMAPI.getInstance().baseInfo.netid,deviceid = deviceId, feeid = it.feeid?:"", mbpoint = it.value?:0f,loaderStateListener = mStateListener,
                expire_renew = expire_renew)
    }
}