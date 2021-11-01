package net.sdvn.nascommon.model.oneos.api

import io.reactivex.Observable
import libs.source.common.livedata.Resource
import net.sdvn.nascommon.constant.OneOSAPIs
import net.sdvn.nascommon.iface.Callback
import net.sdvn.nascommon.iface.Result
import net.sdvn.nascommon.model.http.OnHttpRequestListener
import net.sdvn.nascommon.model.oneos.OneStat
import net.sdvn.nascommon.repository.base.NasService
import net.sdvn.nascommon.utils.GsonUtils

class GetSystemStatApi(val address: String) : BaseAPI(address, OneOSAPIs.SYSTEM_STAT) {
    fun exec(callback: Callback<Resource<Result<*>>>) {
        httpRequest.setOnHttpRequestListener(object : OnHttpRequestListener {
            override fun onStart(url: String) {}
            override fun onSuccess(url: String, result: String) {
                val oneStat = GsonUtils.decodeJSON(result, OneStat::class.java)
                callback.result(Resource.success(Result(oneStat)))
            }

            override fun onFailure(url: String, httpCode: Int, errorNo: Int, strMsg: String) {
                callback.result(Resource.error(strMsg, Result<Any?>(errorNo, strMsg)))
            }
        })
        httpRequest.setParseResult(false)
        httpRequest.get(oneOsRequest.url())
    }

    fun observer(): Observable<OneStat?> {
        return createProductRetrofit(address)
                .create(NasService::class.java)
                .systemStat
    }
}