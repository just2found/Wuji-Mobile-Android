package io.weline.repo

import androidx.annotation.WorkerThread
import androidx.arch.core.util.Function
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import io.weline.repo.api.ApiService
import io.weline.repo.data.model.BaseProtocol
import io.weline.repo.data.model.CheckV5Version
import io.weline.repo.files.constant.AppConstants
import io.weline.repo.files.data.DataSessionUser
import io.weline.repo.net.RetrofitSingleton
import io.weline.repo.repository.V5Repository
import okhttp3.MediaType
import okhttp3.RequestBody
import org.json.JSONObject
import timber.log.Timber
import java.util.concurrent.ConcurrentHashMap

/**缓存登录的nas Session对象
 * @author Raleigh.Luo
 * date：20/9/18 10
 * describe：
 */
class SessionCache private constructor() {
    //指定deviceId是否是V5
    private val domainCheckV5Caches = ConcurrentHashMap<String, Boolean>()

    //指定deviceId是否是NasV3
    private val domainCheckNasV3Caches = ConcurrentHashMap<String, Boolean>()

    ////指定deviceId session
    private val sessionCaches = ConcurrentHashMap<String, DataSessionUser>()

    private object Holder {
        val holder = SessionCache()
    }

    companion object {
        val instance = Holder.holder
    }

    /**
     * 添加
     */
    fun put(deviceId: String, session: DataSessionUser) {
        sessionCaches.put(deviceId, session);
    }

    /**
     * 获取
     */
    fun get(deviceId: String): DataSessionUser? {
        return if (sessionCaches.containsKey(deviceId)) sessionCaches.get(deviceId) else null

    }


    /**
     * 获取 没有就去请求（同步请求）
     * 此方法必须在子线程中调用
     * 在SessionManager.getLoginSession会先请求好数据
     */
    fun getOrSynchRequest(deviceId: String, ip: String, token: String): DataSessionUser? {
        if (sessionCaches.containsKey(deviceId))
            return sessionCaches.get(deviceId)
        else {
            return getSession(deviceId, ip, token)
        }
    }

    /**
     * 获取 没有就去请求（同步请求）
     * 此方法必须在子线程中调用
     * 在SessionManager.getLoginSession会先请求好数据
     */
    fun getOrAsynRequest(deviceId: String, ip: String, token: String, callback: Function<DataSessionUser, Void?>?) {
        if (sessionCaches.containsKey(deviceId))
            callback?.apply(sessionCaches.get(deviceId))
        else {
            asynRequestSession(deviceId, ip, token)
                    .subscribe(object : Observer<BaseProtocol<Any?>> {
                        override fun onComplete() {
                        }

                        override fun onSubscribe(d: Disposable) {
                        }

                        override fun onNext(result: BaseProtocol<Any?>) {
                            if (result.result) {
                                val session = result.data as DataSessionUser
                                //保存到内存中缓存
                                session?.let {
                                    put(deviceId, it)
                                }
                                callback?.apply(session)
                            } else {
                                callback?.apply(null)
                            }

                        }

                        override fun onError(e: Throwable) {
                            callback?.apply(null)
                        }

                    })
        }
    }

    /**
     * 获取 没有就去请求（同步请求）
     * 此方法必须在子线程中调用
     * 在SessionManager.getLoginSession会先请求好数据
     */
    fun asynRequestSession(deviceId: String, ip: String, token: String): Observable<BaseProtocol<Any?>> {
        return Observable.create<BaseProtocol<Any?>> {
            val session = getSession(deviceId, ip, token)
            if (session != null) {
                it.onNext(BaseProtocol(true, null, session))
            } else {
                it.onNext(BaseProtocol<Any?>(false, null, null))
            }
        }
    }

    /**
     * 移除
     */
    fun remove(deviceId: String) {
        if (sessionCaches.containsKey(deviceId))
            sessionCaches.remove(deviceId)
        if (domainCheckNasV3Caches.contains(deviceId))
            domainCheckNasV3Caches.remove(deviceId)
        if (domainCheckV5Caches.contains(deviceId))
            domainCheckV5Caches.remove(deviceId)
    }

    /**
     * 退出登录 切换帐号或应用退出时调用
     */
    fun clear() {
        sessionCaches.clear()
        domainCheckV5Caches.clear()
        domainCheckNasV3Caches.clear()
    }

    fun put(deviceId: String, isV5: Boolean) {
        domainCheckV5Caches.put(deviceId, isV5);
    }


    fun isV5(deviceId: String): Boolean {
        return isWebApi(deviceId)
    }

    fun isV5Old(deviceId: String): Boolean {
        return if (domainCheckV5Caches.containsKey(deviceId)) {
            domainCheckV5Caches.get(deviceId) ?: false
        } else {
            false
        }
    }

    fun isWebApi(deviceId: String): Boolean {
        return isNasV3(deviceId) || isV5Old(deviceId)
    }

    fun isNasV3(deviceId: String): Boolean {
        return if (domainCheckNasV3Caches.containsKey(deviceId)) {
            domainCheckNasV3Caches.get(deviceId) ?: false
        } else {
            false
        }
    }

    @WorkerThread
    fun isWebApiOrSyncRequest(deviceId: String, ip: String): Boolean {
        return isNasV3OrSyncRequest(deviceId, ip) || isV5OrSynchRequestOld(deviceId, ip)
    }

    /**
     * 获取 没有就去请求（同步请求）
     * 此方法必须在子线程中调用
     * 在SessionManager.getLoginSession会先请求好数据
     */
    @WorkerThread
    fun isV5OrSynchRequest(deviceId: String, ip: String): Boolean {
        return isWebApiOrSyncRequest(deviceId, ip)
    }

    /**
     * 获取 没有就去请求（同步请求）
     * 此方法必须在子线程中调用
     * 在SessionManager.getLoginSession会先请求好数据
     */
    @WorkerThread
    fun isV5SyncRequest(deviceId: String, ip: String): Boolean {
        return isWebApiSyncRequest(deviceId, ip)
    }

    private fun isWebApiSyncRequest(deviceId: String, ip: String): Boolean {
        return getNasV3(deviceId, ip) || getV5(deviceId, ip);
    }

    @WorkerThread
    fun isV5OrSynchRequestOld(deviceId: String, ip: String): Boolean {
        if (domainCheckV5Caches.containsKey(deviceId)) {
            return domainCheckV5Caches.get(deviceId) ?: false
        } else {
            return getV5(deviceId, ip)
        }
    }

    @WorkerThread
    fun isNasV3OrSyncRequest(deviceId: String, ip: String): Boolean {
        if (domainCheckNasV3Caches.containsKey(deviceId)) {
            return domainCheckNasV3Caches.get(deviceId) ?: false
        } else {
            return getNasV3(deviceId, ip)
        }
    }

    @WorkerThread
    fun getNasV3(deviceId: String, ip: String): Boolean {
        if (apiService == null) {
            apiService = RetrofitSingleton.instance.getRetrofit()
                    .create(ApiService::class.java)
        }
        var isNasV3 = false
        try {
            //同步请求
            val response = apiService!!.checkIsNasV3(deviceId, ip).execute()
            if (response.isSuccessful) {
                isNasV3 = response.body()?.data?.isNasV3() ?: false
            }
        } catch (e: Exception) {
            Timber.e(e)
        }
        //保存到内存中缓存
        putIsNasV3(deviceId, isNasV3)
        return isNasV3
    }

    fun isNasV3OrAsyncReq(deviceId: String, ip: String, callback: Function<Boolean, Void?>?): Disposable? {
        var isNasV3 = false
        if (domainCheckNasV3Caches.containsKey(deviceId)) {
            callback?.apply(domainCheckNasV3Caches.get(deviceId) ?: false)
        } else {
            return V5Repository.INSTANCE().getNasVersion(deviceId, ip)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({ result ->
                        isNasV3 = result.data?.isNasV3() ?: false
                        //保存到内存中缓存
                        putIsNasV3(deviceId, isNasV3)
                        callback?.apply(isNasV3)
                    }, {
                        callback?.apply(false)
                    })
        }
        return null
    }

    private fun putIsNasV3(deviceId: String, isNasV3: Boolean) {
        domainCheckNasV3Caches.put(deviceId, isNasV3)
        domainCheckV5Caches.put(deviceId, isNasV3)
    }

    /** 设备登录前会先请求下来（sessionManager.getLoginSession），保证后续操作不需要请求
     * 获取 没有就去请求（异步请求）
     * 此方法必须在子线程中调用
     * 在SessionManager.getLoginSession会先请求好数据
     */
    fun isV5OrAsynRequest(deviceId: String, ip: String, callback: Function<Boolean, Void?>?) {
        if (domainCheckV5Caches.containsKey(deviceId)) {
            callback?.apply(domainCheckV5Caches.get(deviceId) ?: false)
        } else {
            if (apiService == null) {
                apiService = RetrofitSingleton.instance.getRetrofit()
                        .create(ApiService::class.java)
            }
            val url = String.format("http://%s:9994", ip)
            var isV5 = false
            //异步请求
            apiService?.checkIsV5Asyn(url)?.subscribeOn(Schedulers.io())?.observeOn(AndroidSchedulers.mainThread())?.subscribe(object : Observer<CheckV5Version> {
                override fun onComplete() {
                }

                override fun onSubscribe(d: Disposable) {
                }

                override fun onNext(result: CheckV5Version) {
                    isV5 = result.isV5()
                    //保存到内存中缓存
                    put(deviceId, isV5)
                    callback?.apply(isV5)
                }

                override fun onError(e: Throwable) {
                    callback?.apply(false)
                }

            })
        }
    }

    private var apiService: ApiService? = null
    private fun getV5(deviceId: String, ip: String): Boolean {
        if (apiService == null) {
            apiService = RetrofitSingleton.instance.getRetrofit()
                    .create(ApiService::class.java)
        }

        val url = String.format("http://%s:9994", ip)
        var isV5 = false
        try {
            //同步请求
            val result = apiService?.checkIsV5(url)?.execute()
            isV5 = result?.body()?.isV5() ?: false

        } catch (e: Exception) {

        }
        //保存到内存中缓存
        put(deviceId, isV5)
        return isV5
    }

    private fun getSession(deviceId: String, ip: String, token: String): DataSessionUser? {
        if (apiService == null) {
            apiService = RetrofitSingleton.instance.getRetrofit()
                    .create(ApiService::class.java)
        }
        val json = JSONObject()
        json.put("method", "access")
        json.put("params", JSONObject().put("token", token))
        val body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), json.toString())
        var session: DataSessionUser? = null
        try {
            //同步请求
            val result = apiService?.getSession(getFullUrl(ip), body)?.execute()
            if (result?.body()?.result == true) {
                session = result?.body()?.data
                //保存到内存中缓存
                session?.let {
                    put(deviceId, it)
                }
            }
        } catch (e: Exception) {

        }
        return session
    }

    private fun getFullUrl(ip: String): String {
        return String.format("http://%s:%s", ip, AppConstants.HS_ANDROID_TV_PORT)
    }
}