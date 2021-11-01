package net.sdvn.nascommon.fileserver

import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import libs.source.common.utils.RateLimiter
import net.sdvn.nascommon.fileserver.constants.HttpFileService
import net.sdvn.nascommon.fileserver.data.DataShareVersion
import net.sdvn.nascommon.iface.Callback
import net.sdvn.nascommon.iface.Result
import java.util.concurrent.TimeUnit

/**
 *  
 *
 *
 * Created by admin on 2020/7/3,09:48
 */
object FileShareHelper {
    private val repoListRateLimit = RateLimiter<String>(1, TimeUnit.SECONDS)

    @JvmStatic
    fun checkAvailable(ip: String?, callback: Callback<Result<Boolean>>): Disposable {
        return Observable.create<String> {
            if (!ip.isNullOrEmpty()) {
                it.onNext(ip)
            } else {
                it.onNext("")
            }
            it.onComplete()
        }.flatMap {
            if (it.isNotEmpty() && repoListRateLimit.shouldFetch(it, 1000)) {
                return@flatMap version(ip!!)

            } else {
                return@flatMap Observable.just(FileShareBaseResult<DataShareVersion>().apply { status = -1 })
            }
        }.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    if (it.isSuccessful) {
                        callback.result(Result(true).apply {
                            msg = it.result.version
                        })
                    } else {
                        callback.result(Result(it.status, it.msg))
                    }
                }, {
//                    it.printStackTrace()
                    callback.result(Result(-402, it.message))
                })

    }

    @JvmStatic
    fun version(ip: String): Observable<FileShareBaseResult<DataShareVersion>> {
        return RetrofitFactory.createRetrofit(
                HttpFileService.getHost(ip))
                .create(FileServerApiService::class.java)
                .version()
    }

}