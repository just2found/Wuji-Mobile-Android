package net.linkmate.app.ui.simplestyle.dynamic.related

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.switchMap
import io.objectbox.android.ObjectBoxLiveData
import io.objectbox.kotlin.query
import net.linkmate.app.base.MyApplication
import net.linkmate.app.repository.DynamicRepository
import net.linkmate.app.service.DynamicQueue
import net.sdvn.common.DynamicDBHelper
import net.sdvn.common.vo.DynamicRelated
import net.sdvn.common.vo.DynamicRelated_

/**与我相关
 * @author Raleigh.Luo
 * date：21/2/3 17
 * describe：
 */
class RelatedViewModel : ViewModel() {
    val networkId = DynamicQueue.mLastNetworkId ?: ""
    val deviceId = DynamicQueue.deviceId
    val deviceIP = DynamicQueue.deviceIP
    private val repository = DynamicRepository()
    val getRelatedListResult = repository.getRelatedList(networkId, deviceId, deviceIP)
    private val start = MutableLiveData<Boolean>(true)
    val relateds:LiveData<out List<DynamicRelated>> = start.switchMap {
        DynamicDBHelper.INSTANCE(MyApplication.getInstance())?.getBoxStore()?.let {
           ObjectBoxLiveData(it.boxFor(DynamicRelated::class.java).query {
                equal(DynamicRelated_.networkId, networkId)
                equal(DynamicRelated_.deviceId, deviceId)
                orderDesc(DynamicRelated_.createAt)
            })
        }?:let {
            MutableLiveData<List<DynamicRelated>>(ArrayList())
        }
    }

    fun clearDB() {
        DynamicDBHelper.INSTANCE(MyApplication.getInstance())?.getBoxStore()?.let {
            it.runInTxAsync(Runnable {
                it.boxFor(DynamicRelated::class.java).query {
                    equal(DynamicRelated_.networkId, networkId)
                    equal(DynamicRelated_.deviceId, deviceId)
                }.remove()
            }, null)
        }
    }
}