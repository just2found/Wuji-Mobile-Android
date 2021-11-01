package net.linkmate.app.ui.viewmodel

import io.reactivex.Observable
import io.reactivex.functions.BiFunction
import io.weline.repo.repository.V5Repository
import net.sdvn.common.internet.core.GsonBaseProtocol
import net.sdvn.common.internet.utils.LoginTokenUtil
import net.sdvn.nascommon.constant.HttpErrorNo
import net.sdvn.nascommon.iface.Result
import net.sdvn.nascommon.model.DeviceModel
import net.sdvn.nascommon.viewmodel.RxViewModel
import timber.log.Timber
import java.io.IOException
import java.util.concurrent.TimeUnit

/** 

Created by admin on 2021/1/8,20:11

 */
class HDManageViewModel : RxViewModel() {
    fun formatWithToken(deviceModel: DeviceModel, force: Boolean = false): Observable<Any>? {
        return Observable.create<Result<String>> { emitter ->
            LoginTokenUtil.getLoginToken(object : LoginTokenUtil.TokenCallback {
                override fun error(protocol: GsonBaseProtocol?) {
                    emitter.onNext(Result<String>(protocol?.result
                            ?: HttpErrorNo.UNKNOWN_EXCEPTION, protocol?.errmsg))
                }

                override fun success(token: String) {
                    emitter.onNext(Result(token))
                }
            })
        }.filter { t: Result<*> ->
            t.isSuccess
        }.flatMap { resultToken ->
            V5Repository.INSTANCE().initDisk(deviceModel.devId, deviceModel.device?.vip
                    ?: "", resultToken.data, if (force) {
                1
            } else {
                0
            }).flatMap { initDiskResult ->
                if (initDiskResult.result) {
                    V5Repository.INSTANCE().queryDiskStatus(deviceModel.devId, deviceModel.device?.vip
                            ?: "", resultToken.data)
//                            .repeatWhen { t ->
//                                t.delay(1, TimeUnit.SECONDS)
//                            }.takeUntil { t -> t.result && (t.data?.isOK() == true || t.data?.isFailed() == true) }
//                            .filter { t -> t.result && t.data?.isRunning() == true }
                            /* .flatMap { t ->
                                 if (t.result) {
                                     Timber.d("queryDiskStatus $t")
                                     when {
                                         t.data?.isOK() == true -> {
                                             Observable.just(true)
                                         }
                                         t.data?.isFailed() == true -> {
                                             Observable.just(false)
                                         }
                                         else -> {
                                             // throw  IOException("cause retry")
                                             Observable.just("$t")
                                         }
                                     }
                                 } else {
                                     //throw  IOException("cause retry")
                                     Observable.just("$t")
                                 }
                             } */.flatMap { t ->
                                if (t.result) {
                                    Timber.d("queryDiskStatus $t")
                                    when {
                                        t.data?.isOK() == true -> {
                                            Observable.just(true)
                                        }
                                        t.data?.isFailed() == true -> {
                                            Observable.just(false)
                                        }
                                        else -> {
                                            throw  IOException("cause retry")
                                        }
                                    }
                                } else {
                                    throw  IOException("cause retry")
                                }
                            }
                            .retryWhen { t -> t.zipWith(Observable.range(1, 1000), BiFunction<Throwable?, Int?, Int?> { t1, t2 -> t2 }).flatMap { Observable.timer(1.toLong(), TimeUnit.SECONDS) } }


                } else {
                    Observable.just(Result<Any>(initDiskResult.error?.code
                            ?: HttpErrorNo.UNKNOWN_EXCEPTION, initDiskResult.error?.msg))
                }
            }
        }

    }
}