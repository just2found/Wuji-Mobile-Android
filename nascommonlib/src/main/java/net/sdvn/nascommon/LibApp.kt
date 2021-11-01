package net.sdvn.nascommon

import android.app.Application
import libs.source.common.AppExecutors
import net.sdvn.nascommon.db.DBHelper
import net.sdvn.nascommon.receiver.LocalDeviceStateManager
import net.sdvn.nascommon.receiver.NetworkStateManager
import net.sdvn.nascommonlib.BuildConfig
import timber.log.Timber

class LibApp private constructor() {

    companion object {
        val instance = SingletonHolder.holder
    }

    private object SingletonHolder {
        val holder = LibApp()
    }

    private lateinit var register: LocalDeviceStateManager

    private lateinit var sApp: Application

    fun getApp(): Application {
        return sApp
    }

    fun onCreate(app: Application) {
        sApp = app
        NetworkStateManager.instance.init(app)
        DBHelper.init()
        if (BuildConfig.DEBUG && Timber.treeCount() == 0) {
            Timber.plant(Timber.DebugTree())
        }
        register = LocalDeviceStateManager.register(app)
    }

    private lateinit var briefDelegete: BriefDelegete
    fun getBriefDelegete():BriefDelegete{
        return briefDelegete
    }
    fun initBriefDelegete(briefDelegete: BriefDelegete) {
        this.briefDelegete = briefDelegete
    }

    fun onTerminate() {
        sApp.unregisterReceiver(register)
        SessionManager.getInstance().unbindService()
        NetworkStateManager.instance.onDestroy()
    }

    fun getAppExecutors(): AppExecutors {
        return AppExecutors.instance
    }
}
