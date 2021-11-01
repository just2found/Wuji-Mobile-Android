package net.sdvn.common.data.remote

import com.google.gson.reflect.TypeToken
import net.sdvn.common.internet.core.GsonBaseProtocolV2
import net.sdvn.common.internet.core.HttpLoader
import net.sdvn.common.internet.listener.ResultListener
import net.sdvn.common.internet.loader.GetAdsHttpLoader
import net.sdvn.common.internet.protocol.ad.Ads

/**
 *
 * @Description: 获取广告相关信息
 *
 * @Author: todo2088
 * @CreateDate: 2021/3/2 10:55
 */
class AdsRemoteDataSource {
    fun getAdsBanner(type: String? = null, listener: ResultListener<GsonBaseProtocolV2<Ads>>): HttpLoader {
        val getAdsHttpLoader = GetAdsHttpLoader(GsonBaseProtocolV2::class.java)
        getAdsHttpLoader.setParams(type)
        val typeOfT = object : TypeToken<GsonBaseProtocolV2<Ads>>() {
        }.getType()
        getAdsHttpLoader.executor(typeOfT, listener)
        return getAdsHttpLoader
    }
}