package net.linkmate.app.ui.simplestyle.dynamic.detial

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.switchMap
import net.linkmate.app.ui.simplestyle.dynamic.DynamicBaseViewModel
import net.linkmate.app.ui.simplestyle.dynamic.delegate.db.DBDelegete
import net.sdvn.common.vo.Dynamic

/**
 * @author Raleigh.Luo
 * date：20/11/25 14
 * describe：
 */
class DynamicDetailViewModel : DynamicBaseViewModel() {
    private val _toastText: MutableLiveData<String> = MutableLiveData()
    val toastText: LiveData<String> = _toastText
    fun showToast(text: String) {
        _toastText.value = text
    }

    /**--恢复评论框----------------------------------------------------------**/
    private var _recoveryCommentDialog: MutableLiveData<Boolean> = MutableLiveData(true)
    val recoveryCommentDialog: LiveData<Boolean> = _recoveryCommentDialog
    fun recoveryCommentDialog() {
        _recoveryCommentDialog.value = true
    }

    private var deviceId: String = ""
    private var deviceIP: String = ""
    private var networkId: String = ""

    fun init(networkId: String, deviceId: String, deviceIP: String) {
        this.networkId = networkId
        this.deviceId = deviceId
        this.deviceIP = deviceIP
    }

    /**--获取指定动态----------------------------------------------------------**/
    private val _getDynamicId = MutableLiveData<Long>()

    val dynamic = _getDynamicId.switchMap {
        DBDelegete.dynamicDelegete.getDynamic(networkId, deviceId, it)
                ?: MutableLiveData()
    }

    val dynamicResult = _getDynamicId.switchMap {
        if (it != -1L) repository.getDynamic(networkId, deviceId, deviceIP, it)
        else MutableLiveData()
    }

    fun getDynamic(): Dynamic? {
        return if (dynamic.value != null && (dynamic.value?.size
                        ?: 0) > 0) dynamic.value?.get(0) else null
    }


    fun getDynamic(id: Long? = null) {
        id?.let {
            _getDynamicId.value = id
        } ?: let {//刷新
            _getDynamicId.value = _getDynamicId.value
        }
    }

    /**--指定回复评论的位置----------------------------------------------------------**/
    var defualtReplayCommentScreenY: MutableLiveData<Int> = MutableLiveData()

    /**
     * 本地操作，删除了动态
     */
    val hasDeletedDynamic = MutableLiveData<Boolean>()

    override fun deletedDynamic() {
        hasDeletedDynamic.postValue(true)
    }
}