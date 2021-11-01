package net.linkmate.app.repository

import androidx.lifecycle.LiveData
import libs.source.common.livedata.Resource
import net.sdvn.common.data.remote.AdsRemoteDataSource
import net.sdvn.common.internet.core.GsonBaseProtocol
import net.sdvn.common.internet.core.GsonBaseProtocolV2
import net.sdvn.common.internet.listener.ResultListener
import net.sdvn.common.internet.protocol.ad.Ads
import java.util.concurrent.atomic.AtomicBoolean

/**
 *
 * @Description: java类作用描述
 * @Author: todo2088
 * @CreateDate: 2021/3/2 11:12
 */
class AdsRepo {
    private val remoteDataSource = AdsRemoteDataSource()
    fun getAdsBanner(type: String? = null): LiveData<Resource<Ads>> {
        return object : LiveData<Resource<Ads>>() {
            val started = AtomicBoolean(false)
            override fun onActive() {
                super.onActive()
                if (started.compareAndSet(false, true)) {
                    remoteDataSource.getAdsBanner(type = type, listener = object : ResultListener<GsonBaseProtocolV2<Ads>> {
                        override fun error(tag: Any?, baseProtocol: GsonBaseProtocol) {
                            postValue(Resource.error(baseProtocol.errmsg, null, baseProtocol.result))
                        }

                        override fun success(tag: Any?, data: GsonBaseProtocolV2<Ads>?) {
                            postValue(Resource.success(data?.data))
                        }
                    })
                }
            }
        }
    }
}