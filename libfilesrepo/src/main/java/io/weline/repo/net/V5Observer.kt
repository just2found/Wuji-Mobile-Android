package io.weline.repo.net

import android.widget.Toast
import androidx.annotation.Keep
import io.reactivex.Observer
import io.reactivex.disposables.Disposable
import io.weline.repo.SessionCache
import io.weline.repo.data.model.BaseProtocol
import io.weline.repo.data.model.Error
import io.weline.repo.files.BuildConfig
import io.weline.repo.files.constant.HttpErrorNo
import libs.source.common.LibCommonApp
import net.sdvn.common.internet.utils.NetworkUtils
import java.util.concurrent.atomic.AtomicInteger

/**
 * @author Raleigh.Luo
 * date：20/9/17 14
 * describe：
 */
@Keep
open abstract class V5Observer<T>(val deviceId: String) : Observer<BaseProtocol<T>> {
    private val MAX_RETRY = 3
    private val retryCount = AtomicInteger(0)
    override fun onComplete() {
    }

    override fun onSubscribe(d: Disposable) {
    }

    override fun onNext(result: BaseProtocol<T>) {
        hasRetryForTimeOut = false
        if (result.result) {//请求成功
            success(result)
        } else {
            if (result.error?.code == HttpErrorNo.ERR_ONE_NO_LOGIN
                    || result.error?.code == -40008) {//token过期重试
                //移除
                SessionCache.instance.remove(deviceId)
                if (!retry() || retryCount.incrementAndGet() > MAX_RETRY) {
                    //不重试
                    fail(result)
                }
            } else {
                fail(result)
            }
        }
    }

    //是否已经超时重试过
    var hasRetryForTimeOut = false
    override fun onError(e: Throwable) {
        if (e.message == "Canceled") {
            if (!SessionCache.instance.isV5(deviceId)) {   //被取消请求，说明不是V5设备,在CheckV5Interceptor拦截器中被处理

                if (BuildConfig.DEBUG) {
                    Toast.makeText(LibCommonApp.getApp(), "您正在调用旧接口", Toast.LENGTH_SHORT).show()
                }
                isNotV5()
            }
        } else if (e.message == "timeout" && !hasRetryForTimeOut
                && NetworkUtils.checkNetwork(LibCommonApp.getApp())
                && retryCount.getAndIncrement() < MAX_RETRY) {//请求超时,并且有网络重试
            hasRetryForTimeOut = true
            //重试
            retry()
        } else {
            hasRetryForTimeOut = false
            val error = BaseProtocol<T>(false, Error(HttpErrorNo.ERR_DEVICE_STATUS, e.message
                    ?: "unknown error"), null)
            fail(error)
        }
    }

    abstract fun success(result: BaseProtocol<T>)
    abstract fun fail(result: BaseProtocol<T>)

    //无效的session，返回true去重试（自己实现），返回false 进fail
    open fun retry(): Boolean {
        return false
    }

    //不是V5 调用旧API
    abstract fun isNotV5();
}