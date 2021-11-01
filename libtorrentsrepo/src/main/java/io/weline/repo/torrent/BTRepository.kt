package io.weline.repo.torrent

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import chainspace.WMsgBase
import io.weline.repo.torrent.constants.BTResultCode
import io.weline.repo.torrent.constants.BT_Config
import io.weline.repo.torrent.data.BTItem
import io.weline.repo.torrent.data.BTItems
import io.weline.repo.torrent.data.BtSession
import libs.source.common.AppExecutors
import libs.source.common.livedata.ApiSuccessResponse
import libs.source.common.livedata.LiveDataCallAdapterFactory
import libs.source.common.livedata.ReqStringResGsonConverterFactory
import libs.source.common.livedata.Resource
import libs.source.common.utils.RateLimiter
import net.sdvn.common.internet.OkHttpClientIns
import okhttp3.*
import okio.ByteString
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import timber.log.Timber
import java.io.File
import java.util.concurrent.TimeUnit

class BTRepository(private val appExecutors: AppExecutors) {

    private val repoListRateLimit = RateLimiter<String>(1, TimeUnit.SECONDS)

    companion object {
        fun provideNasService(host: String, isDebug: Boolean = BT_Config.isDebug,
                              isUseLiveData: Boolean = true,
                              isLocal: Boolean = false,
                              timeout: Long = 0): BTServerApiService {
            val port = if (isDebug) {
                if (isLocal) {
                    BT_Config.PORT_LOCAL_DEBUG
                } else {
                    BT_Config.PORT_DEBUG
                }
            } else {
                if (isLocal) {
                    BT_Config.PORT_LOCAL
                } else {
                    BT_Config.PORT
                }
            }
            val builder = HttpUrl.Builder()
                    .scheme(BT_Config.SCHEME)
                    .host(host)
                    .port(port)
            val retrofitBuilder = Retrofit.Builder()
                    .baseUrl(builder.build())
                    .addConverterFactory(if (isDebug) {
                        GsonConverterFactory.create()
                    } else {
                        ReqStringResGsonConverterFactory.create()
                    })
            if (isUseLiveData)
                retrofitBuilder.addCallAdapterFactory(LiveDataCallAdapterFactory.create())
            return retrofitBuilder
                    .client(OkHttpClientIns.getApiClient())
                    .build()
                    .create(BTServerApiService::class.java)

        }
    }

    fun auth(devId: String, token: String): LiveData<Resource<BtSession>> {
        val result = MediatorLiveData<Resource<BtSession>>()
        val (host, isLocal) = getHost(devId)
        if (host.isNullOrEmpty()) {
            result.postValue(Resource.error("host isNullOrEmpty", null, BTResultCode.ERR_HOST_NOT_FOUND))
            return result
        }
        val provideNasService = provideNasService(host = host, isLocal = isLocal)
        val body = mapOf("token" to token)
        val apiResponse = if (BT_Config.isDebug) {
            provideNasService.auth(body)
        } else {
            provideNasService.requestEncryptAuth(BT_Config.API_PATH_AUTH, BTHelper.map2Encrypt(body))
        }
        result.addSource(apiResponse) { response ->
            result.removeSource(apiResponse)
            val resource = when (response) {
                is ApiSuccessResponse -> {
                    val data = response.body
                    if (data.isSuccessful) {
                        Resource.success(data.result)
                    } else {
                        Resource.error(data.msg, data, data.status)
                    }
                }
                else -> {
                    Resource.error(response.toString(), null, BTResultCode.UNKNOWN_EXCEPTION)
                }
            }
            result.postValue(resource as Resource<BtSession>)
        }
        return result
    }

    fun createByWS(devId: String, path: String, pathType: Int, session: String): LiveData<Resource<BtBaseResult<BTItem>>> {
        val result = MediatorLiveData<Resource<BtBaseResult<BTItem>>>()
        val client = OkHttpClient.Builder()
                .connectTimeout(3, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .writeTimeout(15, TimeUnit.SECONDS)
                .build()
        val host = BTHelper.getDeviceVipById(devId)
        if (host.isNullOrEmpty()) {
            result.postValue(Resource.error("", null, BTResultCode.ERR_HOST_NOT_FOUND))
            return result
        }
        val request = Request.Builder().url(BTHelper.getWSUrl(host)).build()
        val fileName = path.substring(path.lastIndexOf(File.separator) + 1)
        val newWebSocket = client.newWebSocket(request, object : WebSocketListener() {
            private var wMsgRQ: WMsgBase.WMsgRQ? = null

            override fun onOpen(webSocket: WebSocket, response: Response) {
                super.onOpen(webSocket, response)
                Timber.d("create onOpen : ${response}")
                val createBtRq = WMsgBase.Create_BT_RQ.newBuilder()
                        .setPath(path)
                        .setSession(session)
                        .setPathType(pathType)
                        .build()
                wMsgRQ = WMsgBase.WMsgRQ.newBuilder()
                        .setMainId(BT_Config.M_CMD_BT)
                        .setSubId(BT_Config.S_CMD_BT_CREATE)
                        .setSn(100)
                        .setData(createBtRq.toByteString())
                        .build()
                val srcData = wMsgRQ!!.toByteString().toByteArray()

                val aesEncrypt = BTHelper.aesEncrypt(srcData)
                val send = webSocket.send(aesEncrypt)

                Timber.d("create : pathType = ${createBtRq.pathType}")
                Timber.d("create : ${wMsgRQ}\n---$srcData---$aesEncrypt---$send")

            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                super.onFailure(webSocket, t, response)
                Timber.e("onFailure :${response.toString()}")
                Timber.e("onFailure :${t}")
                result.postValue(Resource.error(t.message, BtBaseResult(BTResultCode.UNKNOWN_EXCEPTION, t.message), BTResultCode.UNKNOWN_EXCEPTION))
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                super.onClosing(webSocket, code, reason)
                Timber.d("onClosing{ code : ${code} , reason : ${reason}}")
            }

            override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
                super.onMessage(webSocket, bytes)
                Timber.d(bytes.toString())
                val wMsgRP = WMsgBase.WMsgRP.parseFrom(bytes.toByteArray())
                Timber.d(wMsgRP.toString())
                if (wMsgRQ != null && isSameRQRP(wMsgRP, wMsgRQ!!)) {
                    val log = if (wMsgRP.result) {
                        val btRp = WMsgBase.Create_BT_RP.parseFrom(wMsgRP.data)
                        val btItem = mapToBTItem(btRp)
                        btItem.name = fileName
//                        btItem.devId = devId
                        result.postValue(Resource.success(BtBaseResult(btItem)))
                        "$btRp"
                    } else {
                        val msgError = wMsgRP.err
                        result.postValue(Resource.error(msgError.errMsg, BtBaseResult(msgError.errNo, msgError.errMsg), msgError.errNo))
                        "$msgError"
                    }
                    webSocket.close(BTResultCode.MSG_WEB_SOCKET_OK, BTResultCode.MSG_WEB_SOCKET_SUCCESS)
                    Timber.d(log)
                }
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                super.onMessage(webSocket, text)
                Timber.d(text)
                val wMsgRP = WMsgBase.WMsgRP.parseFrom(text.toByteArray())
                Timber.d(wMsgRP.toString())
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                super.onClosed(webSocket, code, reason)
                Timber.d("onClosed{ code : ${code} , reason : ${reason}}")
            }
        })
        newWebSocket.request()
        return result

    }

    private fun isSameRQRP(wMsgRP: WMsgBase.WMsgRP, wMsgRQ: WMsgBase.WMsgRQ) = wMsgRP.mainId == wMsgRQ.mainId &&
            wMsgRP.subId == (wMsgRQ.subId or BT_Config.S_CMD_RESPONSE)

    private fun mapToBTItem(btRp: WMsgBase.Create_BT_RP): BTItem {
        return BTItem(btTicket = btRp.btTicket, remoteServer = btRp.remoteServer,
                timestamp = btRp.createdate, totalLen = btRp.length, name = btRp.name
                , remoteServerId = btRp.deviceId, devId = btRp.deviceId,
                netId = btRp.networkId)
    }

    fun create(devId: String, path: String, session: String): LiveData<Resource<BtBaseResult<BTItem>>> {
        val result = MediatorLiveData<Resource<BtBaseResult<BTItem>>>()
        val (host, isLocal) = getHost(devId)
        if (host.isNullOrEmpty()) {
            result.postValue(Resource.error("", null, BTResultCode.ERR_HOST_NOT_FOUND))
            return result
        }
        val provideNasService = provideNasService(host = host, isLocal = isLocal, timeout = 120)
        val body = mapOf("path" to path, "session" to session)
        val apiResponse = if (BT_Config.isDebug) {
            provideNasService.create(body)
        } else {
            provideNasService.requestEncryptItem(BT_Config.API_PATH_CREATE, BTHelper.map2Encrypt(body))
        }
        val fileName = path.substring(path.lastIndexOf(File.separator) + 1)
        result.addSource(apiResponse) { response ->
            result.removeSource(apiResponse)
            val resource = when (response) {
                is ApiSuccessResponse -> {
                    val data = response.body
                    if (data.isSuccessful) {
                        val btItem = data.result as BTItem
                        if (btItem != null) {
                            btItem.devId = devId
                            btItem.name = fileName
                        }
                        Resource.success(data)
                    } else {
                        Resource.error(data.msg, data, data.status)
                    }
                }
                else -> {
                    Resource.error("", null, BTResultCode.UNKNOWN_EXCEPTION)
                }
            }
            result.postValue(resource)
        }
        return result
    }


    fun download(devId: String, session: String, btTicket: String, remoteServer: String
                 , saveDir: String, pathType: Int):
            LiveData<Resource<BtBaseResult<BTItem>>> {
        val result = MediatorLiveData<Resource<BtBaseResult<BTItem>>>()
        val (host, isLocal) = getHost(devId)
        if (host.isNullOrEmpty()) {
            result.postValue(Resource.error("", null, BTResultCode.ERR_HOST_NOT_FOUND))
            return result
        }
        val provideNasService = provideNasService(host = host, isLocal = isLocal)
        val body = mapOf("bt_ticket" to btTicket,
                "session" to session,
                "remote_server" to remoteServer,
                "save_dir" to saveDir,
                "path_type" to pathType)
        val apiResponse = if (BT_Config.isDebug) {
            provideNasService.download(body)
        } else {
            provideNasService.requestEncryptItem(BT_Config.API_PATH_DOWNLOAD, BTHelper.map2Encrypt(body))
        }
        result.addSource(apiResponse) { response ->
            result.removeSource(apiResponse)
            val resource = when (response) {
                is ApiSuccessResponse -> {
                    val data = response.body
                    if (data.isSuccessful) {
                        val btItem = data.result
                        if (btItem != null) {
                            btItem.devId = devId
                        }
                        Resource.success(data)
                    } else {
                        Resource.error(data.msg, data, data.status)
                    }
                }
                else -> {
                    Resource.error("", null, BTResultCode.UNKNOWN_EXCEPTION)
                }
            }
            result.postValue(resource)
        }
        return result
    }

    fun stop(devId: String, session: String, dlTickets: List<String>): LiveData<Resource<BtBaseResult<BTItems>>> {
        val result = MediatorLiveData<Resource<BtBaseResult<BTItems>>>()
        val (host, isLocal) = getHost(devId)
        if (host.isNullOrEmpty()) {
            result.postValue(Resource.error("", null, BTResultCode.ERR_HOST_NOT_FOUND))
            return result
        }
        val provideNasService = provideNasService(host = host, isLocal = isLocal)
        val body = mapOf("dl_tickets" to dlTickets.toTypedArray(), "session" to session)
        val apiResponse = if (BT_Config.isDebug) {
            provideNasService.stop(body)
        } else {
            provideNasService.requestEncryptItems(BT_Config.API_PATH_STOP, BTHelper.map2Encrypt(body))
        }
        result.addSource(apiResponse) { response ->
            result.removeSource(apiResponse)
            val resource = when (response) {
                is ApiSuccessResponse -> {
                    val data = response.body
                    if (data.isSuccessful) {
                        val items = data.result?.items
                        if (!items.isNullOrEmpty()) {
                            items.forEach { btItem ->
                                btItem.devId = devId
                            }
                        }
                        Resource.success(data)
                    } else {
                        Resource.error(data.msg, data, data.status)
                    }
                }
                else -> {
                    Resource.error("", null, BTResultCode.UNKNOWN_EXCEPTION)
                }
            }
            result.postValue(resource)
        }
        return result
    }

    fun resume(devId: String, session: String, dlTicket: String): LiveData<Resource<BtBaseResult<BTItems>>> {
        val result = MediatorLiveData<Resource<BtBaseResult<BTItems>>>()
        val (host, isLocal) = getHost(devId)
        if (host.isNullOrEmpty()) {
            result.postValue(Resource.error("", null, BTResultCode.ERR_HOST_NOT_FOUND))
            return result
        }
        val provideNasService = provideNasService(host = host, isLocal = isLocal)
        val body = mapOf("dl_ticket" to dlTicket, "session" to session)
        val apiResponse = if (BT_Config.isDebug) {
            provideNasService.resume(body)
        } else {
            provideNasService.requestEncryptItems(BT_Config.API_PATH_RESUME, BTHelper.map2Encrypt(body))
        }
        result.addSource(apiResponse) { response ->
            result.removeSource(apiResponse)
            val resource = when (response) {
                is ApiSuccessResponse -> {
                    val data = response.body
                    if (data.isSuccessful) {
                        val items = data.result?.items
                        if (!items.isNullOrEmpty()) {
                            items.forEach { btItem ->
                                btItem.devId = devId
                            }
                        }
                        Resource.success(data)
                    } else {
                        Resource.error(data.msg, data, data.status)
                    }
                }
                else -> {
                    Resource.error("", null, BTResultCode.UNKNOWN_EXCEPTION)
                }
            }
            result.postValue(resource)
        }
        return result
    }

    fun cancel(devId: String, session: String, dlTickets: List<String>): LiveData<Resource<BtBaseResult<BTItems>>> {
        val result = MediatorLiveData<Resource<BtBaseResult<BTItems>>>()
        val (host, isLocal) = getHost(devId)
        if (host.isNullOrEmpty()) {
            result.postValue(Resource.error("", null, BTResultCode.ERR_HOST_NOT_FOUND))
            return result
        }
        val provideNasService = provideNasService(host = host, isLocal = isLocal)
        val body = mapOf("dl_tickets" to dlTickets.toTypedArray(), "session" to session)
        val apiResponse = if (BT_Config.isDebug) {
            provideNasService.cancel(body)
        } else {
            provideNasService.requestEncryptItems(BT_Config.API_PATH_CANCEL, BTHelper.map2Encrypt(body))
        }
        result.addSource(apiResponse) { response ->
            result.removeSource(apiResponse)
            val resource = when (response) {
                is ApiSuccessResponse -> {
                    val data = response.body
                    if (data.isSuccessful) {
                        val items = data.result?.items
                        if (!items.isNullOrEmpty()) {
                            items.forEach { btItem ->
                                btItem.devId = devId
                            }
                        }
                        Resource.success(data)
                    } else {
                        Resource.error(data.msg, data, data.status)
                    }
                }
                else -> {
                    Resource.error("", null, BTResultCode.UNKNOWN_EXCEPTION)
                }
            }
            result.postValue(resource)
        }
        return result
    }

    fun progress(devId: String, session: String, dlTickets: List<String>): LiveData<Resource<BtBaseResult<BTItems>>> {
        val result = MediatorLiveData<Resource<BtBaseResult<BTItems>>>()
        val (host, isLocal) = getHost(devId)
        if (host.isNullOrEmpty()) {
            result.postValue(Resource.error("", null, BTResultCode.ERR_HOST_NOT_FOUND))
            return result
        }
        val provideNasService = provideNasService(host = host, isLocal = isLocal)
        val body = mapOf("dl_tickets" to dlTickets.toTypedArray(), "session" to session)
        val apiResponse = if (BT_Config.isDebug) {
            provideNasService.progress(body)
        } else {
            provideNasService.requestEncryptItems(BT_Config.API_PATH_PROGRESS, BTHelper.map2Encrypt(body))
        }
        result.addSource(apiResponse) { response ->
            result.removeSource(apiResponse)
            val resource = when (response) {
                is ApiSuccessResponse -> {
                    val data = response.body
                    if (data.isSuccessful) {
                        val items = data.result?.items
                        if (!items.isNullOrEmpty()) {
                            items.forEach { btItem ->
                                btItem.devId = devId
                            }
                        }
                        Resource.success(data)
                    } else {
                        Resource.error(data.msg, data, data.status)
                    }
                }
                else -> {
                    Resource.error("", null, BTResultCode.UNKNOWN_EXCEPTION)
                }
            }
            result.postValue(resource)
        }
        return result
    }


    fun list(devId: String?,btName: String?, session: String? = null, host: String? = null): LiveData<Resource<BtBaseResult<BTItems>>> {
        val result = MediatorLiveData<Resource<BtBaseResult<BTItems>>>()
        val body = HashMap<String, Any>()
        if (!session.isNullOrEmpty()) {
            val pair = "session" to session
            body.plus(pair)
        }
        if (!btName.isNullOrEmpty()) {
            body["name"] = btName
        }
        val (host, isLocal) = if (!devId.isNullOrEmpty()) {
            getHost(devId)
        } else {
            Result(host, false)
        }
        if (host.isNullOrEmpty()) {
            result.postValue(Resource.error("", null, BTResultCode.ERR_HOST_NOT_FOUND))
            return result
        }
        val provideNasService = provideNasService(host = host, isLocal = isLocal)
        val apiResponse = if (BT_Config.isDebug) {
            provideNasService.list(body)
        } else {
            provideNasService.requestEncryptItems(BT_Config.API_PATH_LIST, BTHelper.map2Encrypt(body))
        }
        result.addSource(apiResponse) { response ->
            result.removeSource(apiResponse)
            val resource = when (response) {
                is ApiSuccessResponse -> {
                    val data = response.body
                    if (data.isSuccessful) {
                        val items = data.result?.items
                        if (!items.isNullOrEmpty() && !devId.isNullOrEmpty()) {
                            items.forEach { btItem ->
                                if (btItem.isMainSeed) {
                                    btItem.remoteServerId = devId
                                }
                                btItem.devId = devId
                            }
                        }
                        Resource.success(data)
                    } else {
                        Resource.error(data.msg, data, data.status)
                    }
                }
                else -> {
                    Resource.error("", null, BTResultCode.UNKNOWN_EXCEPTION)
                }
            }
            result.postValue(resource)
        }
        return result
    }


    fun getHost(devId: String): Result {
        var isLocal = false
        val ip = if (BTHelper.isLocal(devId)) {
            isLocal = true
            BT_Config.BT_LOCAL_DEVICE_HOST
        } else {
            BTHelper.getDeviceVipById(devId)
        }
        return Result(ip, isLocal)
    }
}

data class Result(val host: String?, val isLocal: Boolean)