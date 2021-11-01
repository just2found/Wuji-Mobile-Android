package net.linkmate.app.ui.simplestyle

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import net.linkmate.app.base.BaseViewModel

/**
 * @author Raleigh.Luo
 * date：21/2/22 16
 * describe：
 */
class MainViewModel : BaseViewModel() {
    /**
     * 动态消息数量
     */
    private val _dynamicMessageCount = MutableLiveData<Int>()
    val dynamicMessageCount:LiveData<Int> = _dynamicMessageCount
    fun updateDynamicMessageCount(count: Int){
        _dynamicMessageCount.value = count
    }
}