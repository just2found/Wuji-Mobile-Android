package net.sdvn.nascommon.model.oneos.api.sys

import androidx.annotation.Keep
import com.google.gson.reflect.TypeToken
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import io.weline.repo.SessionCache
import io.weline.repo.data.model.BaseProtocol
import io.weline.repo.net.V5Observer
import io.weline.repo.repository.V5Repository
import libs.source.common.livedata.Resource
import libs.source.common.livedata.Status
import net.sdvn.common.internet.utils.LoginTokenUtil
import net.sdvn.nascommon.constant.HttpErrorNo
import net.sdvn.nascommon.constant.OneOSAPIs
import net.sdvn.nascommon.iface.Callback
import net.sdvn.nascommon.iface.Result
import net.sdvn.nascommon.model.http.OnHttpRequestListener
import net.sdvn.nascommon.model.oneos.BaseResultModel
import net.sdvn.nascommon.model.oneos.OneStat
import net.sdvn.nascommon.model.oneos.api.BaseAPI
import net.sdvn.nascommon.model.oneos.api.GetSystemStatApi
import net.sdvn.nascommon.model.oneos.user.LoginSession
import net.sdvn.nascommon.utils.GsonUtils
import net.sdvn.nascommon.utils.log.Logger
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

/**
 * Created by yun on 2018/3/28.
 * 请求参数：
 * 名称	类型	必须	描述
 * method	string	是	hdformat
 * session	string	是	登录session信息
 * cmd	string	是	硬盘模式：BASIC, LVM, RAID1, RAID0, LVMADD, RAIDADD
 *
 *
 * { "method":"flagfile", "params":{"path":"/tmp/format","read":xxx}}
 *
 *
 * 注 : 硬盘格式化结果通过flagfile接口读取hdformat文件获取；格式化完成后自动重启
 * data：	faile  		格式化失败
 * formatting 	正在格式化
 * success  	格式化成功
 */
class OneOSHDFormatAPI(loginSession: LoginSession) :
    BaseAPI(loginSession, OneOSAPIs.SYSTEM_SYS, "hdformat") {
    private var listener: OnFormatListener? = null

    private var compositeDisposable: CompositeDisposable? = null

    @Synchronized
    private fun addDisposable(disposable: Disposable) {
        if (compositeDisposable == null) {
            compositeDisposable = CompositeDisposable()
        }
        compositeDisposable!!.add(disposable)
    }

    private fun dispose() {
        if (compositeDisposable != null) compositeDisposable!!.dispose()
    }


    fun setOnFormatListener(onListener: OnFormatListener?) {
        listener = onListener
    }

    fun format(session: String?, hdFType: HD_FType?) {
        val finalHdFType = hdFType ?: HD_FType.BASIC
        val params: MutableMap<String, Any> = ConcurrentHashMap()
        val listener = object : OnHttpRequestListener {
            override fun onStart(url: String?) {
                if (listener != null) {
                    listener!!.onStart(url)
                }
            }

            override fun onSuccess(url: String?, result: String?) {
                count = 0
                val isNotM8 = finalHdFType.ordinal < HD_FType.fixinternal.ordinal
                queryFormatStatus(isNotM8)
            }

            override fun onFailure(url: String?, httpCode: Int, errorNo: Int, strMsg: String?) {
                if (listener != null) {
                    listener!!.onFailure(url, errorNo, strMsg)
                }
            }
        }
        val observer = object : V5Observer<Any>(loginSession.id ?: "") {
            override fun onSubscribe(d: Disposable) {
                super.onSubscribe(d)
                listener.onStart("")
            }

            override fun success(result: BaseProtocol<Any>) {
                listener.onSuccess("", "")
            }

            override fun fail(result: BaseProtocol<Any>) {
                listener.onFailure("", 0, result.error?.code ?: 0, result.error?.msg ?: "")
            }

            override fun isNotV5() {
                params["cmd"] = finalHdFType.name
                setParams(params)
                httpRequest?.post(oneOsRequest!!, listener)
            }

            override fun retry(): Boolean {
//                V5Repository.INSTANCE().formatSystem(loginSession.id
//                        ?: "", loginSession.ip, LoginTokenUtil.getToken(), finalHdFType.name, this)
                return false
            }
        }
        V5Repository.INSTANCE().formatSystem(
            loginSession.id
                ?: "", loginSession.ip, LoginTokenUtil.getToken(), finalHdFType.name, observer
        )
    }

    private var maxCount = Int.MAX_VALUE
    fun queryFormatStatus(isNotM8: Boolean, isFromInit: Boolean = false) {
        if (isFromInit) {
            maxCount = 3
        }
        if (SessionCache.instance.isV5(loginSession.id ?: "") == true) {
            //等待设备重启，后onDeviceStatusChange 关闭页面，acitivity中处理
            delayQueryStatus()
        } else {
            if (isNotM8) {
                delayQuery()
            } else {
                delayQueryM8()
            }
        }
    }

    private fun delayQueryStatus() {
        addDisposable(Observable.timer(1, TimeUnit.SECONDS)
            .subscribeOn(Schedulers.io())
            .subscribe { getFormatStatus() })
    }

    var countNone: AtomicInteger = AtomicInteger(0)

    private fun getFormatStatus() {
        val id = loginSession.id
        val observer = object : V5Observer<Any>(
            id
                ?: ""
        ) {
            override fun onSubscribe(d: Disposable) {
                addDisposable(d)
            }

            override fun success(result: BaseProtocol<Any>) {
//                {"status":1,"msg":"Formating"}
                val data = GsonUtils.encodeJSON(result.data)
                if (data?.contains("ok") == true) {
                    if (listener != null) {
                        listener!!.onSuccess(url())
                    }
                } else if (data?.contains("failed") == true) {
                    if (listener != null) {
                        listener!!.onFailure(
                            url(), result.error?.code ?: 0, result.error?.msg
                                ?: ""
                        )
                    }
                } else if (data?.contains("none") == true) {
                    if (countNone.incrementAndGet() == 3) {
                        if (listener != null) {
                            listener!!.onSuccess(url())
                        }
                    } else {
                        delayQueryStatus()
                    }
                } else {
                    delayQueryStatus()
                }
            }

            override fun fail(result: BaseProtocol<Any>) {
//                        -40031	格式化未知错误
//                        -40032	磁盘挂载失败
//                        -40033	逻辑卷扩容失败
//                        -40034	没有发现磁盘
//                        -40035	逻辑卷不存在，请先对内部存储格式化
//                        -40036	磁盘正在使用中，无法进行扩展
                when (result.error?.code) {
                    -40031, -40032, -40033, -40034, -40035, -40036 -> {
                        if (listener != null) {
                            listener!!.onFailure(
                                url(), result.error?.code
                                    ?: HttpErrorNo.UNKNOWN_EXCEPTION, result.error?.msg
                            )
                        }
                    }
                    else -> {
                        if (count++ < maxCount) {
                            delayQueryStatus()
                        } else {
                            if (listener != null) {
                                listener!!.onFailure(
                                    url(), result.error?.code ?: 0, result.error?.msg
                                        ?: ""
                                )
                            }
                        }
                    }
                }
            }

            override fun isNotV5() {
            }

            override fun retry(): Boolean {
                return false
            }
        }
        V5Repository.INSTANCE().formatHDStatus(
            id
                ?: "", loginSession.ip, LoginTokenUtil.getToken(), observer
        )

    }

    private fun delayQueryM8() {
        addDisposable(Observable.timer(10, TimeUnit.SECONDS)
            .subscribeOn(Schedulers.io())
            .subscribe { formatResultM8() })

    }

    private fun formatResultM8() {
        val getSystemStatApi = GetSystemStatApi(oneOsRequest.address)
        getSystemStatApi.exec(Callback<Resource<Result<*>>> { t ->
            if (t.status != Status.SUCCESS) {
                if (count++ < maxCount) {
                    delayQueryM8()
                } else {
                    if (listener != null) {
                        listener!!.onFailure(url(), HttpErrorNo.ERR_FORMAT_FAILURE, "")
                    }
                }
            } else {
                val data = t.data?.data
                if (data is OneStat && data.hd.isOk && data.mysql.isOk) {
                    if (listener != null) {
                        listener!!.onSuccess(url())
                    }
                } else {
                    if (listener != null) {
                        listener!!.onFailure(url(), HttpErrorNo.ERR_FORMAT_FAILURE, "")
                    }
                }
            }
        })
    }

    private fun delayQuery() {
        addDisposable(Observable.timer(3, TimeUnit.SECONDS)
            .subscribeOn(Schedulers.io())
            .subscribe { formatResult })
    }

    override fun cancel() {
        dispose()
    }

    private val onHttpRequestListener: OnHttpRequestListener = object : OnHttpRequestListener {
        override fun onStart(url: String?) {}
        override fun onSuccess(url: String?, response: String?) {
            var responseVar = response
            responseVar = responseVar?.replace("\\n", "")
            Logger.p(Logger.Level.DEBUG, Logger.Logd.OSAPI, "format status ------> ", responseVar)
            val type = object : TypeToken<BaseResultModel<String?>?>() {}.type
            var result: BaseResultModel<String?>? = null
            try {
                result = GsonUtils.decodeJSON<BaseResultModel<String?>>(responseVar, type)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            if (result != null && result.isSuccess) {
                if (HD_FResult.faile.name.equals(result.data, ignoreCase = true)) {
                    if (listener != null) {
                        listener!!.onFailure(url, HttpErrorNo.ERR_FORMAT_FAILURE, "")
                    }
                } else if (HD_FResult.formating.name.equals(result.data, ignoreCase = true)) {
                    delayQuery()
                } else if (HD_FResult.success.name.equals(result.data, ignoreCase = true)) {
                    if (listener != null) {
                        listener!!.onSuccess(url)
                    }
                }
            } else {
                delayQuery()
            }
        }

        override fun onFailure(url: String?, httpCode: Int, errorNo: Int, strMsg: String?) {
            if (errorNo == HttpErrorNo.ERR_ONE_NO_FOUND || errorNo == HttpErrorNo.ERR_ONE_NO_SATA) {
                count++
                if (count < maxCount) {
                    delayQuery()
                } else {
                    if (listener != null) listener!!.onFailure(url, errorNo, strMsg)
                }
            } else {
                if (listener != null) listener!!.onFailure(url, errorNo, strMsg)
            }
        }
    }
    private var count = 0
    private val formatResult: Unit
        get() {
            val params2: MutableMap<String, Any> = ConcurrentHashMap()
            params2["path"] = "/tmp/format"
            params2["read"] = 1
            setMethod("flagfile")
            setParams(params2)
            httpRequest.setParseResult(false)
            httpRequest.post(oneOsRequest, onHttpRequestListener)
        }

    @Keep
    enum class HD_FType {
        BASIC, LVM, RAID1, RAID0, LVMADD, RAIDADD, fixinternal, extend, entire;
    }

    @Keep
    interface OnFormatListener {
        fun onStart(url: String?)
        fun onSuccess(url: String?)
        fun onFailure(url: String?, errorNo: Int, errorMsg: String?)
    }

    @Keep
    enum class HD_FResult {
        faile, formating, success
    }
}