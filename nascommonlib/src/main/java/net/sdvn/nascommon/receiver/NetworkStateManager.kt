package net.sdvn.nascommon.receiver

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.os.Handler
import android.os.Looper
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import net.sdvn.cmapi.CMAPI
import net.sdvn.cmapi.protocal.ConnectStatusListenerPlus
import net.sdvn.common.internet.core.GsonBaseProtocol
import net.sdvn.common.internet.utils.LoginTokenUtil
import net.sdvn.nascommon.SessionManager
import net.sdvn.nascommon.model.oneos.event.EventMsgManager
import net.sdvn.nascommon.utils.ToastHelper
import net.sdvn.nascommon.utils.Utils
import net.sdvn.nascommon.utils.log.Logger
import net.sdvn.nascommonlib.R
import java.util.*

/**
 * Created by gaoyun@eli-tech.com on 2016/1/14.
 */

class NetworkStateManager private constructor() {
    private var mContext: Context? = null
    private val listenerList = ArrayList<OnNetworkStateChangedListener>()
    private var available: Boolean = false
    private var isWifi: Boolean = false
    private var mHandler: Handler? = null
    private var mNetworkReceiver: BroadcastReceiver? = null

    private val _isNetAvailableLiveData = MutableLiveData<Boolean>()
    val isNetAvailableLiveData: LiveData<Boolean> = _isNetAvailableLiveData
    private val _isWifiAvailableLiveData = MutableLiveData<Boolean>()
    val isWifiAvailableLiveData: LiveData<Boolean> = _isWifiAvailableLiveData
    private val _CSLiveData = MutableLiveData<Int>()
    val CSLiveData: LiveData<Int> = _CSLiveData

    private val appContext: Context
        get() {
            if (mContext == null) {
                throw NullPointerException("pls init $TAG")
            }
            return mContext as Context
        }

    private var mCurrentStatusCode = STATUS_CODE_DISCONNECTED
    private val statusListener = object : ConnectStatusListenerPlus {

        override fun onAuthenticated() {

        }

        override fun onConnected() {
            SessionManager.getInstance().init(appContext)
            SessionManager.getInstance().setIsLogin(true)
            getLoginToken()
        }

        override fun onConnecting() {
            Logger.LOGI(TAG, "onConecting: ============")
            refreshConnectState(STATUS_CODE_CONNECTING)
            _CSLiveData.postValue(STATUS_CODE_CONNECTING)
            //            SdvnMessageManager.getInstance().startOrStopGetMsg(false);
        }

        override fun onDisconnecting() {
            refreshConnectState(STATUS_CODE_DISCONNECTING)
            _CSLiveData.postValue(STATUS_CODE_DISCONNECTING)
            //            SdvnMessageManager.getInstance().startOrStopGetMsg(false);
        }

        override fun onEstablished() {
            Logger.LOGI(TAG, "onEstablished: ================" + CMAPI.getInstance().baseInfo.vip)
            refreshConnectState(STATUS_CODE_ESTABLISHED)
            _CSLiveData.postValue(STATUS_CODE_ESTABLISHED)
            //            XGPushManager.bindAccount(MyApplication.getAppContext(), account);
            //            SdvnMessageManager.getInstance().startOrStopGetMsg(true);
        }


        override fun onDisconnected(i: Int) {
            Logger.LOGE(TAG, "onDisconnected: ===================$i")
            LoginTokenUtil.clearToken()
            refreshConnectState(STATUS_CODE_DISCONNECTED)
            _CSLiveData.postValue(STATUS_CODE_DISCONNECTED)
            //            SdvnMessageManager.getInstance().startOrStopGetMsg(false);
        }
    }

    private var count: Int = 0


    fun isEstablished(): Boolean = mCurrentStatusCode == STATUS_CODE_ESTABLISHED

    private object NetworkStateManagerImpl {
        @SuppressLint("StaticFieldLeak")
        val INSTANCE = NetworkStateManager()
    }

    fun isNetAvailable(): Boolean {
        return checkNetwork()
    }

    fun init(context: Context) {
        mContext = context
        Utils.init(context.applicationContext)
        checkNetwork()
        mNetworkReceiver = NetworkStateReceiver()
        CMAPI.getInstance().addConnectionStatusListener(statusListener)
        registerNetworkReceiver()
        val filter = IntentFilter()
        filter.addAction(Intent.ACTION_SCREEN_OFF)
        filter.addAction(Intent.ACTION_SCREEN_ON)
        filter.addAction(Intent.ACTION_USER_PRESENT)
        LocalBroadcastManager.getInstance(appContext)
                .registerReceiver(mNetworkReceiver!!, filter)
    }

    inner class NetworkStateReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent?) {
            if (intent != null && intent.action != null) {
                when (intent.action) {
                    Intent.ACTION_CONFIGURATION_CHANGED -> {
                        val mManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
                        var activeNetInfo: NetworkInfo? = null
                        if (mManager != null) {
                            activeNetInfo = mManager.activeNetworkInfo
                        }
                        val isWifiAvailable: Boolean
                        val isAvailable: Boolean

                        if (activeNetInfo != null) {
                            if (activeNetInfo.isConnected) {
                                isAvailable = true
                                isWifiAvailable = activeNetInfo.type == ConnectivityManager.TYPE_WIFI
                            } else {
                                isAvailable = false
                                isWifiAvailable = false
                            }
                        } else {
                            isAvailable = false
                            isWifiAvailable = false
                        }
                        onChanged(isAvailable, isWifiAvailable)
                    }
                    Intent.ACTION_SCREEN_OFF ->
                        //                        SdvnMessageManager.getInstance().startOrStopGetMsg(false);
                        EventMsgManager.instance.pause()
                    Intent.ACTION_SCREEN_ON -> if (isEstablished()) {
                        EventMsgManager.instance.resume()
                        //                            SdvnMessageManager.getInstance().startOrStopGetMsg(true);
                    }
                    Intent.ACTION_USER_PRESENT -> {
                    }
                }
            }
        }
    }

    fun onChanged(isAvailable: Boolean, isWifiAvailable: Boolean) {
        available = isAvailable
        isWifi = isWifiAvailable
        for (listener in listenerList.toMutableList()) {
            listener.onNetworkChanged(isAvailable, isWifiAvailable)
        }
    }

    fun checkNetwork(showTips: Boolean): Boolean {
        val isNetAvailable = checkNetwork()
        if (!isNetAvailable && showTips) {
            ToastHelper.showLongToast(R.string.tips_no_network)
        }
        return isNetAvailable
    }

    fun checkNetwork(): Boolean {
        val mManager = appContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        var activeNetInfo: NetworkInfo? = null
        if (mManager != null) {
            activeNetInfo = mManager.activeNetworkInfo
        }
        val isNetAvailable: Boolean
        if (activeNetInfo != null) {
            isNetAvailable = activeNetInfo.isConnected
            isWifi = activeNetInfo.type == ConnectivityManager.TYPE_WIFI
        } else {
            isNetAvailable = false
        }
        if (this.available != isNetAvailable)
            onChanged(isNetAvailable, isWifi)
        return isNetAvailable

    }

    private fun registerNetworkReceiver() {
        val filter = IntentFilter()
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION)
        appContext.registerReceiver(mNetworkReceiver, filter)
    }

    private fun unregisterNetworkReceiver() {
        if (null != mNetworkReceiver && mContext != null) {
            appContext.unregisterReceiver(mNetworkReceiver)
            LocalBroadcastManager.getInstance(appContext)
                    .unregisterReceiver(mNetworkReceiver!!)
        }
    }

    fun addNetworkStateChangedListener(listener: OnNetworkStateChangedListener) {
        if (!listenerList.contains(listener)) {
            listenerList.add(listener)
            listener.onNetworkChanged(available, isWifi)
            listener.onStatusConnection(mCurrentStatusCode)
        }
    }

    fun addNetworkStateChangedListenerWithoutInitStatus(listener: OnNetworkStateChangedListener) {
        if (!listenerList.contains(listener)) {
            listenerList.add(listener)
        }
    }

    fun removeNetworkStateChangedListener(listener: OnNetworkStateChangedListener) {
        this.listenerList.remove(listener)
    }

    private fun refreshConnectState(statusCodeDisconnected: Int) {
        mCurrentStatusCode = statusCodeDisconnected
        for (listener in listenerList) {
            listener.onStatusConnection(statusCodeDisconnected)
        }
    }

    fun refreshAsHost() {
        val asHost = CMAPI.getInstance().baseInfo.asHost
        Logger.LOGI(TAG, "host : $asHost")
//        if (/*!BuildConfig.DEBUG &&*/!TextUtils.isEmpty(asHost))
//            OkHttpClientIns.setHost(asHost)
    }

    private fun getLoginToken() {
        LoginTokenUtil.getLoginToken(object : LoginTokenUtil.TokenCallback {
            override fun success(token: String) {
                //                ShareElementManager.getInstance().init().startRefresh();
                count = 0
            }

            override fun error(protocol: GsonBaseProtocol) {
                if (count < 3) {
                    if (mHandler == null)
                        mHandler = Handler(Looper.getMainLooper())
                    mHandler!!.postDelayed({
                        if (isEstablished())
                            getLoginToken()
                        else
                            count += 3
                    }, 1000)
                } else {
                    ToastHelper.showToast(R.string.tip_pls_retry_later)
                    //                    CMAPI.getInstance().disconnect();
                }
                count++
            }
        }, true)
    }


    interface OnNetworkStateChangedListener {
        fun onNetworkChanged(isAvailable: Boolean, isWifiAvailable: Boolean)

        fun onStatusConnection(statusCode: Int)
    }

    fun onDestroy() {
        unregisterNetworkReceiver()
    }

    fun checkNetworkWithCheckServer(showTips: Boolean): Boolean {
        val checkNetwork = checkNetwork(showTips)
        return if (checkNetwork) {
            val established = isEstablished()
            if (!established) {
                ToastHelper.showLongToast(R.string.tip_wait_for_service_connect)
            }
            established
        } else {
            false
        }
    }

    companion object {

        private val TAG = NetworkStateManager::class.java.simpleName


        const val STATUS_CODE_CONNECTING = 0
        const val STATUS_CODE_ESTABLISHED = 1
        const val STATUS_CODE_DISCONNECTED = 2
        const val STATUS_CODE_DISCONNECTING = 3

        val instance: NetworkStateManager
            get() = NetworkStateManagerImpl.INSTANCE


    }
}
