package net.sdvn.nascommon.model.oneos.event

import android.os.Handler
import android.os.Looper
import android.os.Message
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken
import io.weline.devhelper.DevTypeHelper
import libs.source.common.AppExecutors
import net.sdvn.nascommon.SessionManager
import net.sdvn.nascommon.constant.OneOSAPIs
import net.sdvn.nascommon.model.UiUtils
import net.sdvn.nascommon.model.oneos.transfer.thread.Priority
import net.sdvn.nascommon.model.oneos.transfer.thread.PriorityRunnable
import net.sdvn.nascommon.receiver.NetworkStateManager
import net.sdvn.nascommon.utils.GsonUtils
import net.sdvn.nascommon.utils.Utils
import net.sdvn.nascommon.utils.log.Logger
import org.greenrobot.eventbus.EventBus
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.InetSocketAddress
import java.net.URL
import java.nio.ByteBuffer
import java.nio.channels.SelectionKey
import java.nio.channels.Selector
import java.nio.channels.SocketChannel
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executor
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.regex.Pattern


/**
 * Created by yun on 2017/3/22.
 */

class EventMsgManager private constructor() {

    private var executor: Executor = AppExecutors.instance.networkIO()

    //    private OnEventMsgListener listener;
//    private val map: LruCache<String, TaskOps>
    private val mapList: ConcurrentHashMap<String, ReadyTask>
    private val userAgent: String
    private var client: Client
    private val singleThread: ExecutorService

    private val onNetworkStateChangedListener = object : NetworkStateManager.OnNetworkStateChangedListener {
        override fun onNetworkChanged(isAvailable: Boolean, isWifiAvailable: Boolean) {

        }

        override fun onStatusConnection(statusCode: Int) {
            if (statusCode == NetworkStateManager.STATUS_CODE_DISCONNECTED) {
                stopReceive()
            }
        }
    }

    init {


//        map = object : LruCache<String, TaskOps>(THREADS_MAX) {
//            override fun entryRemoved(evicted: Boolean, key: String, oldValue: TaskOps, newValue: TaskOps?) {
//                if (null != oldValue && oldValue.isRunning()) {
//                    oldValue.stop()
//                    Logger.p(Logger.Level.INFO, Logger.Logd.DEBUG, TAG, "Stop receive event msg...$key")
//                }
//                if (null != newValue && newValue.isRunning()) {
//                    newValue.stop()
//                    Logger.p(Logger.Level.INFO, Logger.Logd.DEBUG, TAG, "Stop receive event msg...$key")
//                }
//
//            }
//        }
        mapList = ConcurrentHashMap()
        userAgent = Utils.getUserAgent(Utils.getApp())
        singleThread = Executors.newSingleThreadExecutor()
        client = Client()
        NetworkStateManager.instance.addNetworkStateChangedListener(onNetworkStateChangedListener)
    }

    private var mHandler: Handler = Handler(Looper.getMainLooper()) {
        when (it.what) {
            START_CHANNEL -> {
                val devId = it.obj as String
                if (mapList.containsKey(devId)) {
                    startReceive(devId)
                }
                return@Handler true
            }
            START_CLIENT -> {
                for (key in mapList.keys()) {
                    startReceive(key)
                }
                client.restart()
                return@Handler true
            }
            STOP_CLIENT -> {
                stopReceive()
                client.stop()
                return@Handler true
            }
            else -> return@Handler false
        }
    }

    private fun postDelay(what: Int, token: Any, delay: Long) {
        val m = Message.obtain(mHandler, what, token)
        mHandler.sendMessageDelayed(m, delay)
    }
    //    public void setOnEventMsgListener(OnEventMsgListener listener) {
    //        this.listener = listener;
    //    }
    //
    //    public void removeOnEventMsgListener(OnEventMsgListener listener) {
    //        if (this.listener == listener) {
    //            this.listener = null;
    //        }
    //    }

    fun startReceive(devId: String) {

        if (NetworkStateManager.instance.isEstablished()
                && SessionManager.getInstance().isLogin(devId)) {
            val deviceModel = SessionManager.getInstance().getDeviceModel(devId)
            if (DevTypeHelper.isOneOSNas(deviceModel?.devClass ?: 0)) {

                if (!client.isRunning()) {
                    singleThread.execute(client)
                }
                var eventMsgTask = mapList.get(devId)
                if (eventMsgTask == null) {
                    eventMsgTask = ConnectTask(devId)
                    mapList.put(devId, eventMsgTask)
                }
                if (!eventMsgTask.isRunning()) {
                    val priority = if (deviceModel?.isOwner == true) {
                        Priority.DEFAULT
                    } else {
                        Priority.BG_LOW
                    }
                    executor.execute(PriorityRunnable(priority, eventMsgTask))
                }
            }
            Logger.p(Logger.Level.INFO, Logger.Logd.DEBUG, TAG, "Start receive event msg...$devId")
//            if (Objects.equals("563027262834363", devId)) {
//                threadPool.executor.execute(EventMsgTask(devId))
//            }
        }
    }

    fun isReceive(devId: String): Boolean {
        val eventMsgTask = mapList.get(devId)
        return eventMsgTask?.isReady() ?: false
    }

    fun pause() {
        postDelay(STOP_CLIENT, client, 10 * 1000)
        log("pause tasks")
    }

    fun resume() {
        if (client.isRunning() && !client.isInterrupt) {
            mHandler.removeMessages(STOP_CLIENT)
        }
        postDelay(START_CLIENT, client, 10 * 1000)
        log("resume tasks")
    }

    fun onDestroy() {
        stopReceive()
        client.stop()
        NetworkStateManager.instance.removeNetworkStateChangedListener(onNetworkStateChangedListener)
        //        listener = null;
    }

    fun stopReceive(devId: String) {
        val eventMsgTask = mapList.get(devId)
        if (null != eventMsgTask) {
            eventMsgTask.stop()
            Logger.p(Logger.Level.INFO, Logger.Logd.DEBUG, TAG, "Stop receive event msg...$devId")
        }
        mapList.remove(devId)
        if (mapList.isEmpty()) {
            client.stop()
        }
    }

    fun stopReceive() {
        val iterator = mapList.entries.iterator()
        while (iterator.hasNext()) {
            val entry = iterator.next()
            entry.value.stop()
            iterator.remove()
        }
        if (mapList.isEmpty()) {
            client.stop()
        }
    }

    private val charset = Charset.forName("UTF8")

    fun getRequestByIP(nasIp: String): String {
        val temp = StringBuffer()
        temp.append("GET http://" + nasIp + ":" + OneOSAPIs.ONE_API_DEFAULT_PORT + "/oneapi/event/sub HTTP/1.1\r\n")
        temp.append("Host: " + nasIp + ":" + OneOSAPIs.ONE_API_DEFAULT_PORT + "\r\n")
        temp.append("Connection: Keep-Alive\r\n")
        temp.append("Cache-Control: max-age=0\r\n")
        temp.append("\r\n")
        return temp.toString()
    }

    private fun isRunning(devId: String): Boolean {
        return mapList.get(devId)?.isRunning() ?: false
    }

    inner class ConnectTask(val devId: String) : ReadyTask {
        override fun isReady(): Boolean {
            return selectionKey?.isValid ?: false
        }

        private var isInterrupt: Boolean = false
        private var isPreparing: Boolean = false

        @Synchronized
        override fun isRunning(): Boolean {
            if (isPreparing) return true
            return selectionKey?.isValid ?: false
        }

        override fun stop() {
            isInterrupt = true
            if (isRunning()) {
                val channel = selectionKey?.channel()
                selectionKey?.cancel()
                channel?.close()
            }
        }

        var selectionKey: SelectionKey? = null
        override fun run() {
            try {
                if (isInterrupt) return
                if (isRunning(devId)) {
                    log("task already running $devId")
                    return
                }
                isPreparing = true
                val start = System.currentTimeMillis()
                val model = SessionManager.getInstance().getDeviceModel(devId)
                if (model?.device == null ||
                        UiUtils.isM8(model.devClass) ||
                        UiUtils.isAndroidTV(model.devClass)) return
                val nasIp = model.device!!.vip
                val address = InetSocketAddress(nasIp, OneOSAPIs.ONE_APIS_DEFAULT_PORT)
                log("address : $address")
                val channel = SocketChannel.open(address)
                channel.configureBlocking(false)// 配置通道使用非阻塞模式
                while (!channel.finishConnect()) {
                    Thread.sleep(10)
                    if (isInterrupt) return
                }
                val requestByIP = getRequestByIP(nasIp)
                log("requestByIP : $requestByIP")
                val write = channel.write(charset.encode(requestByIP))
                if (isInterrupt) return
                log("write request length: $write")
                val buffer = ByteBuffer.allocate(256)// 创建字节的缓冲
                if (isInterrupt) return
                var isReady = false
                while (!isReady && !isInterrupt) {
                    channel.read(buffer)
                    buffer.flip()
                    while (buffer.hasRemaining()) {
                        if (isInterrupt) return
                        val charBuffer = charset.decode(buffer)
                        log("connect $nasIp : $charBuffer")
                        val compile = Pattern.compile("HTTP/1.1 200 OK")
                        if (compile.matcher(charBuffer.toString()).find()) {
                            isReady = true
                            buffer.clear()
                            break
                        }
                    }
                    buffer.clear()
                }
                log("consumed buffer $nasIp : ${System.currentTimeMillis() - start}")
                if (isReady && client != null) {
                    selectionKey = client.register(devId, channel)
                } else {
                    channel?.close()
                }
                isPreparing = false
                log("consumed $nasIp : ${System.currentTimeMillis() - start}")
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                isPreparing = false
            }
        }

    }

    inner class Client : TaskOps {
        @Synchronized
        override fun isRunning(): Boolean {
            return running
        }

        override fun stop() {
            isInterrupt = true
        }

        var isInterrupt = false
        var running = false
        var selector: Selector? = null

        override fun run() {
            try {
                running = true
                isInterrupt = false
                selector = Selector.open()
                selector?.let {
                    val buffer = ByteBuffer.allocate(1024 * 4)// 创建1024字节的缓冲
                    while (true) {
                        if (isInterrupt) break
                        val size = it.keys().size
                        log("selector keys size :" + size)
                        logD(it.keys())
                        if (size <= 0 || it.select(10000) == 0) {
                            try {
                                Thread.sleep(1000)
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                            continue
                        }
                        if (isInterrupt) break
                        log("selector selectedKeys size :" + it.selectedKeys().size)
                        val iterator = it.selectedKeys().iterator()
                        var count = 0
                        while (iterator.hasNext()) {
                            val key = iterator.next()
                            if (isInterrupt) break
                            if (!key.isValid) {
                                postDelay(START_CHANNEL, key.attachment(), DELAY)
                            }
                            if (isInterrupt) break
                            if (key.isReadable) {
                                val channel = key.channel() as SocketChannel
                                val read = channel.read(buffer)
                                if (read != -1) {
                                    buffer.flip()
                                    while (buffer.hasRemaining()) {
                                        val charBuffer = charset.decode(buffer)
                                        log("selector ${key.attachment()} read : $charBuffer")
                                        if (charBuffer.limit() > 0) {
                                            action(key.attachment() as String, charBuffer.toString())
                                        }
                                    }
                                    buffer.clear()
                                    //有数据时
                                    count++
                                }
                            }
                            iterator.remove()
                        }
                        if (count == 0) {
                            try {
                                Thread.sleep(200)
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                running = false
                selector?.keys()?.iterator()?.forEach {
                    if (it?.isValid == true) {
                        it.runCatching {
                            cancel()
                        }
                    }
                }
                selector?.close()

            }
        }

        private fun logD(selectedKeys: Set<SelectionKey>?) {
            if (selectedKeys != null) {
                val builder = StringBuilder()
                builder.append("[")
                selectedKeys.forEach {
                    it.let {
                        builder.append(it.attachment())
                        builder.append(",")
                    }
                }
                builder.append("]")
                log(builder.toString())
            }
        }

        fun register(devId: String, channel: SocketChannel): SelectionKey? {
            selector?.let {
                val key = channel.register(it, SelectionKey.OP_READ, devId)
                it.runCatching {
                    wakeup()
                }
                return key
            }
            return null
        }

        @Synchronized
        fun restart() {
            if (!isRunning()) {
                isInterrupt = false
                singleThread.execute(this)
            }
        }
    }

    fun log(msg: Any) {
        if (DEBUG)
            Logger.LOGD(TAG, "Client ", msg)
    }


    private inner class EventMsgTask(val devId: String) : TaskOps {
        override fun isRunning(): Boolean {
            return running
        }

        private var tryCount: Int = 0

        @get:Synchronized
        var running: Boolean = false
            private set
        private var isInterrupt: Boolean = false

        init {
            isInterrupt = false
        }

        override fun run() {
            running = true
            var reader: BufferedReader? = null
            try {
                val start = System.currentTimeMillis()
                val deviceModels = SessionManager.getInstance().getDeviceModel(devId)
                if (deviceModels == null || deviceModels.device == null) return
                val ip = deviceModels.device!!.vip
                val url = OneOSAPIs.getSubUrl(ip)
                Logger.LOGI(TAG, "Receive url: $url")
                val realUrl = URL(url)
                val connection = realUrl.openConnection() as HttpURLConnection
                connection.setRequestProperty("accept", "*/*")
                connection.setRequestProperty("connection", "Keep-Alive")
                connection.setRequestProperty("user-agent", userAgent)
//                connection.connect()

                reader = BufferedReader(InputStreamReader(connection.inputStream, StandardCharsets.UTF_8))
                log("consumed http $ip : ${System.currentTimeMillis() - start}")
                var line: String
                while (true) {
                    line = reader.readLine()
                    if (line != null && !isInterrupt) break

                    Logger.p(Logger.Level.INFO, TAG, "$devId Receive msg: $line")
                    action(devId, line)
                }
                reader.close()
            } catch (e: Exception) {
                e.printStackTrace()
                if (tryCount < 3 && !isInterrupt) {
                    postDelay(START_CHANNEL, devId, DELAY)
                    tryCount++
                }
                running = false
            } finally {
                try {
                    reader?.close()
                } catch (e2: Exception) {
                    e2.printStackTrace()
                }

                Logger.p(Logger.Level.INFO, TAG, "-------------Stop Event sub-------------")
            }
        }

        override fun stop() {
            running = false
            isInterrupt = true
            mHandler.removeCallbacksAndMessages(devId)
        }
    }

    private fun action(devId: String, line: String) {
        try {
            val list = line.split(regex)
            list.forEach {
                val result = it//.replace(regexReplace, "")
                log("forEach line :($result)")
                if (result.isNotEmpty()
                        && result.length > 5
                        && !result.startsWith("data:--", true)
                        && result.startsWith("data:")
                ) {
                    try {
                        val jsonStr = it.substring(5).trim()
                        val event = GsonUtils.decodeJSONWithoutCatchException(jsonStr, Event::class.java)
                        if (event != null) {
                            when (event.channel) {
                                "file" -> {

                                    val type = object : TypeToken<Progress<Content>>() {}.type
                                    val o = GsonUtils.decodeJSON<Progress<Content>>(jsonStr, type)
                                    if (o != null) {
                                        o.devId = devId
                                        EventBus.getDefault().post(o)
                                    }
                                }
                                "app", "sys" -> when (event.action) {
                                    "upgrade" -> {
                                        val upgradeProgress = GsonUtils.decodeJSON(jsonStr, UpgradeProgress::class.java)
                                        if (upgradeProgress != null) {
                                            upgradeProgress.devId = devId
                                            EventBus.getDefault().post(upgradeProgress)
                                        }
                                    }
                                }
                            }
                        }
                    } catch (e: JsonSyntaxException) {
                        e.printStackTrace()
                    }
                }
            }
        } catch (ignore: Exception) {
            ignore.printStackTrace()
        }
    }

    companion object {
        private val TAG = EventMsgManager::class.java.simpleName
        private val regex = "\n".toRegex()
        private val regexReplace = "\\s*|\t|\r|\n".toRegex()
        val instance = EventMsgManager()
        const val THREADS_MAX = 3
        const val START_CHANNEL = 0x00000001
        const val START_CLIENT = 0x00000002
        const val STOP_CLIENT = 0x00000004
        private const val DELAY = (3 * 60 * 1000).toLong()
        private val DEBUG: Boolean
            get() = false//BuildConfig.DEBUG
    }


}
