package io.weline.repo.net

import androidx.annotation.Keep
import androidx.lifecycle.MutableLiveData
import io.weline.repo.api.NOT_V5
import io.weline.repo.api.UNKNOWN
import io.weline.repo.data.model.BaseProtocol
import libs.source.common.livedata.Resource

/**
 * 这个是简单的实现，如果不需要进行转换操作就可以使用
 */
@Keep
class V5ObserverImpl<T>(deviceId: String, val liveData: MutableLiveData<Resource<T>>) : V5Observer<T>(deviceId) {


    override fun success(result: BaseProtocol<T>) {
        liveData.postValue(Resource.success(result.data))
    }

    override fun fail(result: BaseProtocol<T>) {
        liveData.postValue(Resource.error("", result.data, result.error?.code ?: UNKNOWN))
    }

    override fun isNotV5() {
        liveData.postValue(Resource.error("", null, NOT_V5))
    }

}