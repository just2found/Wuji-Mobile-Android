package net.sdvn.nascommon.viewmodel

import android.annotation.SuppressLint
import androidx.lifecycle.LiveData
import libs.source.common.AppExecutors
import net.sdvn.common.internet.utils.LoginTokenUtil
import net.sdvn.nascommon.SessionManager
import net.sdvn.nascommon.constant.AppConstants
import net.sdvn.nascommon.constant.HttpErrorNo
import net.sdvn.nascommon.constant.OneOSAPIs
import net.sdvn.nascommon.db.SPHelper
import net.sdvn.nascommon.iface.Callback
import net.sdvn.nascommon.iface.GetSessionListener
import net.sdvn.nascommon.model.oneos.api.user.OneOSLoginAPI
import net.sdvn.nascommon.model.oneos.api.user.OneOSUserManageAPI
import net.sdvn.nascommon.model.oneos.user.LoginSession
import net.sdvn.nascommon.utils.ToastHelper
import net.sdvn.nascommon.utils.Utils
import net.sdvn.nascommonlib.R
import java.util.concurrent.atomic.AtomicBoolean

class NasLanAccessViewModel : RxViewModel() {
    private var mLoginSession: LoginSession? = null
    fun liveDataLoginSession(devId: String): LiveData<LoginSession> {
        return object : LiveData<LoginSession>() {
            val started = AtomicBoolean(false)
            override fun onActive() {
                if (started.compareAndSet(false, true)) {
                   getLoginSession(devId, object : GetSessionListener() {
                        @SuppressLint("SetTextI18n")
                        override fun onSuccess(url: String?, loginSession: LoginSession) {
                            postValue(loginSession)
                        }
                    })
                }
            }
        }
    }

    fun getLoginSession(devId: String, eventListener: GetSessionListener) {
        val currentTimeMillis = System.currentTimeMillis()
        if (mLoginSession?.userInfo == null ||
                mLoginSession?.deviceInfo == null ||
                mLoginSession?.session.isNullOrEmpty() ||
                currentTimeMillis - mLoginSession?.loginTime!! >= AppConstants.SESSION_LIVE_TIME) {
            SessionManager.getInstance().getLoginSession(devId, object : GetSessionListener() {
                @SuppressLint("SetTextI18n")
                override fun onSuccess(url: String?, loginSession: LoginSession) {
                    val token = LoginTokenUtil.getToken()
                    val oneOSLoginAPI = OneOSLoginAPI(loginSession.ip, OneOSAPIs.ONE_API_DEFAULT_PORT, token, loginSession.id!!)
                    oneOSLoginAPI.setOnLoginListener(object : OneOSLoginAPI.OnLoginListener {
                        override fun onStart(url: String?) {

                        }

                        override fun onSuccess(url: String, loginSession: LoginSession) {
                            mLoginSession?.refreshData(loginSession) ?: kotlin.run {
                                mLoginSession = loginSession
                            }
                            eventListener.onSuccess(url, loginSession)
                        }

                        override fun onFailure(url: String, errorNo: Int, errorMsg: String) {
                            ToastHelper.showToast(HttpErrorNo.getResultMsg(true, errorNo, errorMsg))
                        }
                    })
                    oneOSLoginAPI.accessOneOS(AppConstants.DOMAIN_DEVICE_VIP)
                }
            })
        } else {
            eventListener.onSuccess("", mLoginSession)
        }
    }

    fun setLanAccess(devId: String, trim: String, callback: Callback<Boolean>?) {
        getLoginSession(devId, object : GetSessionListener() {
            override fun onSuccess(url: String?, loginSession: LoginSession) {

                val username = loginSession.userInfo?.username ?: ""
                val mListener = object : OneOSUserManageAPI.OnUserManageListener {
                    override fun onStart(url: String) {}

                    override fun onSuccess(url: String, cmd: String) {
                        ToastHelper.showToast(R.string.setting_success)
                        callback?.result(true)
                    }

                    override fun onFailure(url: String, errorNo: Int, errorMsg: String) {
                        ToastHelper.showToast(HttpErrorNo.getResultMsg(true, errorNo, errorMsg))
                        callback?.result(false)
                    }
                }
                val manageAPI = OneOSUserManageAPI(loginSession)
                manageAPI.setOnUserManageListener(mListener)
                manageAPI.chpwd(username, trim)
            }

            override fun onFailure(url: String?, errorNo: Int, errorMsg: String?) {
                callback?.result(false)
            }

        })

    }

    companion object {
        //更新用户密码
        @JvmStatic
        fun randomAdmin(loginSession: LoginSession) {
            randomAdmin(loginSession, null)
        }

        //更新用户密码
        @JvmStatic
        fun randomAdmin(loginSession: LoginSession, ppp: String? = null) {
            AppExecutors.instance.networkIO().execute {
                val username = AppConstants.DEFAULT_USERNAME_ADMIN
                val pwd = if (ppp.isNullOrEmpty()) {
                    //获取保存的随机密码
                    var pwd1 = SPHelper.get(AppConstants.DEFAULT_USERNAME_ADMIN, "")
                    //如果为null则生成保存的随机密码
                    if (pwd1.isNullOrEmpty()) {
                        pwd1 = Utils.genRandomNum(6)
                        SPHelper.put(AppConstants.DEFAULT_USERNAME_ADMIN, pwd1)
                    }
                    pwd1
                } else {
                    ppp
                }
                val devId = loginSession.id
                if (!pwd.isNullOrEmpty() && !devId.isNullOrEmpty()) {
                    val key = AppConstants.DEFAULT_USERNAME_ADMIN + devId
                    //获取是否已修改
                    val isPush = SPHelper.get(key, false)
                    //已修改则跳过
                    if (isPush) {
                        return@execute
                    }

                    val mListener: OneOSUserManageAPI.OnUserManageListener = object : OneOSUserManageAPI.OnUserManageListener {
                        override fun onStart(url: String) {}
                        override fun onSuccess(url: String, cmd: String) {
                            //修改成功保存记录
                            SPHelper.put(key, true)
                        }

                        override fun onFailure(url: String, errorNo: Int, errorMsg: String) {}
                    }
                    val finalPwd = pwd
                    //使用admin 123456 oneos 接口登录
                    val oneOSLoginAPI = OneOSLoginAPI(loginSession.ip, OneOSAPIs.ONE_API_DEFAULT_PORT,
                            username, AppConstants.DEFAULT_USERNAME_PWD, devId)
                    oneOSLoginAPI.setOnLoginListener(object : OneOSLoginAPI.OnLoginListener {
                        override fun onStart(url: String?) {
                        }

                        override fun onSuccess(url: String, loginSession: LoginSession) {
                            //登录成功修改密码
                            val manageAPI = OneOSUserManageAPI(loginSession)
                            manageAPI.setOnUserManageListener(mListener)
                            manageAPI.chpwd(username, finalPwd)
                        }

                        override fun onFailure(url: String, errorNo: Int, errorMsg: String) {
                        }
                    })
                    oneOSLoginAPI.login(AppConstants.DOMAIN_DEVICE_VIP)
                }
            }
        }
    }
}