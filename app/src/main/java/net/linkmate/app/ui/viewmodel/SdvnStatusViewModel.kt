package net.linkmate.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import net.sdvn.app.config.AppConfig
import net.sdvn.cmapi.CMAPI
import net.sdvn.cmapi.global.Constants
import net.sdvn.cmapi.protocal.ResultListener
import timber.log.Timber

class SdvnStatusViewModel : ViewModel() {
    fun toLogin(account: String?, password: String? = "", listener: ResultListener?) {
        if (account.isNullOrEmpty()) {
            listener?.onError(Constants.DR_INVALID_USER)
            return
        }
//        CMAPI.getInstance().setLoginAsHost("192.168.1.76")//AppConfig.host)
        CMAPI.getInstance().setLoginAsHost(AppConfig.host)
        Timber.d("setLoginAsHost : ${AppConfig.host}")
        CMAPI.getInstance().login(account, password, listener)
    }
}