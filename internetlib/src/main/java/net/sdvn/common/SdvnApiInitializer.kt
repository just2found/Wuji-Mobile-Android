package net.sdvn.common

import android.app.Application
import net.sdvn.cmapi.CMAPI
import net.sdvn.cmapi.Device
import net.sdvn.cmapi.RealtimeInfo
import net.sdvn.cmapi.protocal.ConnectStatusListener
import net.sdvn.cmapi.protocal.ConnectStatusListenerPlus
import net.sdvn.cmapi.protocal.EventObserver
import net.sdvn.common.repo.DevicesRepo
import net.sdvn.common.repo.NetsRepo
import net.sdvn.common.repo.SdvnMsgRepo

/** 

Created by admin on 2020/10/20,14:21

 */
object SdvnApiInitializer {

    private lateinit var app: Application
    private val mConnectStatusListenerPlus: ConnectStatusListener = object : ConnectStatusListenerPlus {
        override fun onConnecting() {
        }

        override fun onAuthenticated() {
        }

        override fun onEstablished() {
        }

        override fun onConnected() {
            NetsRepo.init()
            DevicesRepo.init()
            SdvnMsgRepo.init()

        }

        override fun onDisconnected(p0: Int) {
        }

        override fun onDisconnecting() {
        }
    }

    private val eventObserver: EventObserver = object : EventObserver() {
        override fun onDeviceChanged() {
        }

        override fun onTunnelRevoke(p0: Boolean) {
        }

        override fun onDeviceStatusChange(p0: Device?) {
        }

        override fun onRealTimeInfoChanged(p0: RealtimeInfo?) {
        }

        override fun onNetworkChanged() {
            NetsRepo.updateEnable(CMAPI.getInstance().networkList)
        }
    }


    @JvmStatic
    fun init(app: Application) {
        this.app = app
        IntrDBHelper.init(app)
        Local.init(app)
        CMAPI.getInstance().addConnectionStatusListener(mConnectStatusListenerPlus)
        CMAPI.getInstance().subscribe(eventObserver)
    }

    fun onTerminate() {
        CMAPI.getInstance().removeConnectionStatusListener(mConnectStatusListenerPlus)
        CMAPI.getInstance().unsubscribe(eventObserver)
    }
}

