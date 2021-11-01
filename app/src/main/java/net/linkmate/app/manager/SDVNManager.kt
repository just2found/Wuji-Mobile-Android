package net.linkmate.app.manager

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.text.TextUtils
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import libs.source.common.AppExecutors
import net.linkmate.app.BuildConfig
import net.linkmate.app.R
import net.linkmate.app.base.MyApplication
import net.linkmate.app.base.MyConstants
import net.linkmate.app.service.DynamicQueue
import net.linkmate.app.ui.activity.LoginActivity
import net.linkmate.app.util.NetworkUtils
import net.sdvn.app.config.AppConfig
import net.sdvn.cmapi.CMAPI
import net.sdvn.cmapi.Config
import net.sdvn.cmapi.Device
import net.sdvn.cmapi.Network
import net.sdvn.cmapi.global.Constants
import net.sdvn.cmapi.protocal.ConnectStatusListenerPlus
import net.sdvn.cmapi.util.CLogUtils
import net.sdvn.common.internet.OkHttpClientIns
import net.sdvn.scorepaylib.pay.paypal.PayPalUtils
import timber.log.Timber
import java.net.InetAddress
import java.util.*

/**
 *  
 *
 *
 * Created by admin on 2020/7/29,13:39
 */
class SDVNManager private constructor() {
    fun getNetworkId(): String = CMAPI.getInstance().baseInfo.netid?:""
    private var nm: NotificationManager? = null
    fun getDevVip(devId: String): String? {
        return CMAPI.getInstance().devices.find { it.id == devId }?.vip ?: kotlin.run {
            val device = Device()
            CMAPI.getInstance().getDeviceById(devId, device)
            device.vip
        }
    }

    fun isCurrentNet(netId: String?): Boolean {
        return CMAPI.getInstance().networkList.find {
            it.id == netId
        }?.isCurrent
                ?: false
    }

    fun hasNet(netId: String?): Boolean {
        return getNetById(netId) != null
    }

    fun getNetById(netId: String?): Network? {
        return CMAPI.getInstance().networkList.find {
            it.id == netId
        }
    }

    private object SingletonHolder {
        val instance = SDVNManager()
    }

    private var sApp: Application? = null
    private var isServiceConnected = false
    private var isCancelNotify = false
    private val _liveDataConnectionStatus = MutableLiveData(Constants.CS_PREPARE)

    @JvmField
    var liveDataConnectionStatus: LiveData<Int> = _liveDataConnectionStatus
    fun init(app: Application) {
        sApp = app
        //创建通知渠道
        nm = app.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val mChannel = NotificationChannel(Constants.SDVN_CHANNEL_ID, Constants.SDVN_CHANNEL_NAME, importance)
            nm!!.createNotificationChannel(mChannel)
        }
        val printSdvnLog = false
        if (BuildConfig.DEBUG) {
            CMAPI.getInstance().config
                    .setLogLevel(if (printSdvnLog) 5 else 0)
                    .setLogCallback { s ->
                        if (printSdvnLog) {
                            Timber.d("sdvn----> %s", s)
                        }
                    }
        }
        CMAPI.getInstance().init(app.applicationContext, MyConstants.CONFIG_APPID,
                MyConstants.CONFIG_PARTID, MyConstants.CONFIG_DEV_CLASS)
        CLogUtils.getInstance().clearLogFile()
        CMAPI.getInstance().addConnectionStatusListener(mConnectStatusListenerPlus)

//        SdvnMessageManager.getInstance();
        AppExecutors.instance.networkIO().execute(Runnable {
            if (NetworkUtils.checkNetwork(app)) {
                Timber.d("network is unavailable")
                return@Runnable
            }
            var fastDomain: String? = AppConfig.host
            val domains: MutableList<String> = ArrayList()
            domains.add(AppConfig.host_cn)
            domains.add(AppConfig.host_us)
            var minAvg: Long = 100000
            for (domain in domains) {
                if (!TextUtils.isEmpty(domain)) {
                    try {
                        val start = System.currentTimeMillis()
                        val isReachable = InetAddress.getByName(domain).isReachable(10000)
                        val end = System.currentTimeMillis()
                        Timber.d("%s \nreachable:%s  %s ", domain, isReachable, end - start)
                        if (isReachable) {
                            val time = end - start
                            if (time < minAvg) {
                                fastDomain = domain
                                minAvg = time
                            }
                        }
                    } catch (e: Exception) {
                        Timber.e(e)
                    }
                }
            }
            Timber.d("fastDomain host : %s", fastDomain)
            if (CMAPI.getInstance().isDisconnected) {
                OkHttpClientIns.setHost(fastDomain)
            }
        })
    }

    private val mConnectStatusListenerPlus: ConnectStatusListenerPlus = object : ConnectStatusListenerPlus {
        override fun onConnecting() {
            if (isServiceConnected && !isApplicationInForeground) //服务连接成功并且当前APP不在前台
                broken_notify(getString(R.string.string_notification_ticker), getString(R.string.string_notification_text))
            _liveDataConnectionStatus.postValue(Constants.CS_CONNECTING)
        }

        override fun onDisconnecting() {
            //停止后台服务
            DynamicQueue.setRun(false)
            _liveDataConnectionStatus.postValue(Constants.CS_DISCONNECTING)
        }

        override fun onEstablished() {
            //启动动态后台服务
            DynamicQueue.setRun(true)
            nm!!.cancel(Constants.SERVICE_NOTIFICATION_ID)
            isCancelNotify = true
            isServiceConnected = true
            _liveDataConnectionStatus.postValue(Constants.CS_ESTABLISHED)
        }

        override fun onDisconnected(reason: Int) {
            LoginManager.getInstance().notifyLogin(false)
            _liveDataConnectionStatus.postValue(Constants.CS_DISCONNECTED)
            refreshAsHost()
            if (!isApplicationInForeground && isServiceConnected) {
                broken_notify(getString(R.string.string_notification_ticker), getString(R.string.string_notification_text))
            }
            val intent = Intent()
            intent.action = MyConstants.STATUS_DISCONNECTION
            LocalBroadcastManager.getInstance(sApp!!.applicationContext).sendBroadcast(intent)
            isServiceConnected = false
        }

        override fun onAuthenticated() {
            _liveDataConnectionStatus.postValue(Constants.CS_AUTHENTICATED)
        }

        override fun onConnected() {
            val builder1 = NotificationCompat.Builder(context, Constants.SDVN_CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_weline_logo)
                    .setContentTitle(getString(R.string.tunnel_established))
            CMAPI.getInstance().config
                    .put(Config.NOTIFICATION_BUILDER, builder1)
            refreshAsHost()
            isCancelNotify = true
            LoginManager.getInstance().notifyLogin(true)
            DevManager.getInstance().initHardWareList(null)//sdvn onConnected()
            _liveDataConnectionStatus.postValue(Constants.CS_CONNECTED)
            PayPalUtils.getInstance().hasNoCommitedOrder(sApp!!.applicationContext, CMAPI.getInstance().baseInfo.userId)
        }
    }

    fun broken_notify(ticker: String?, contentText: String?) {
        if (isApplicationInForeground) {
            return
        }
        val intent = Intent(context, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NO_HISTORY
        val pi = PendingIntent.getActivity(context, 0, intent, 0)
        val notify = NotificationCompat.Builder(context, Constants.SDVN_CHANNEL_ID)
                .setAutoCancel(true)
                .setTicker(ticker)
                .setSmallIcon(R.drawable.ic_weline_logo)
                .setContentTitle(getString(R.string.app_name))
                .setContentText(contentText)
                .setWhen(System.currentTimeMillis())
                .setContentIntent(pi)
                .setDefaults(Notification.DEFAULT_VIBRATE)
                .build()
        if (isCancelNotify) {
            nm!!.notify(Constants.SERVICE_NOTIFICATION_ID, notify)
            isCancelNotify = false
        }
    }

    private val isApplicationInForeground: Boolean
        private get() = MyApplication.getInstance().isApplicationInForeground

    private val context: Context
        private get() = sApp!!.applicationContext

    private fun getString(resStrId: Int): String {
        return sApp!!.getString(resStrId)
    }

    private var saveLogTimes = 0

    @Synchronized
    fun saveLog(save: Boolean, logLevel: Int) {
        if (save) {
            if (saveLogTimes == 0) {
                CMAPI.getInstance().config
                        .setLogLevel(logLevel)
                        .setLogCallback { log -> CLogUtils.getInstance().saveLogToFile(log) }
            }
        } else if (saveLogTimes == 1) {
            CMAPI.getInstance().config
                    .setLogLevel(0)
                    .setLogCallback(null)
        }
        if (save) {
            saveLogTimes++
        } else {
            saveLogTimes--
            if (saveLogTimes < 0) saveLogTimes = 0
        }
    }

    fun exit() {
        isCancelNotify = true
        nm!!.cancelAll()
        CMAPI.getInstance().removeConnectionStatusListener(mConnectStatusListenerPlus)
        CMAPI.getInstance().destroy()
    }

    fun isConnected(): Boolean {
        return CMAPI.getInstance().isConnected
    }

    companion object {
        @JvmStatic
        val instance: SDVNManager
            get() = SingletonHolder.instance

        private fun refreshAsHost() {
            AppExecutors.instance.networkIO().execute {
                val asHost = CMAPI.getInstance().baseInfo.asHost
                Timber.d("ashost : %s", asHost)
                if (!BuildConfig.DEBUG && !TextUtils.isEmpty(asHost)) {
                    OkHttpClientIns.setHost(asHost)
                }
            }
        }
    }
}