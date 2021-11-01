package net.sdvn.nascommon.model.oneos.api.user

import android.annotation.SuppressLint
import android.text.TextUtils
import androidx.annotation.Keep
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import io.weline.repo.SessionCache
import io.weline.repo.data.model.BaseProtocol
import io.weline.repo.data.model.DataDevMark
import io.weline.repo.files.data.FileTag
import io.weline.repo.net.V5Observer
import io.weline.repo.repository.V5Repository
import net.sdvn.common.internet.utils.LoginTokenUtil
import net.sdvn.nascommon.SessionManager
import net.sdvn.nascommon.constant.AppConstants
import net.sdvn.nascommon.constant.HttpErrorNo
import net.sdvn.nascommon.constant.OneOSAPIs
import net.sdvn.nascommon.db.DeviceInfoKeeper
import net.sdvn.nascommon.db.DeviceSettingsKeeper
import net.sdvn.nascommon.db.SPHelper
import net.sdvn.nascommon.db.UserInfoKeeper
import net.sdvn.nascommon.db.objecbox.DeviceInfo
import net.sdvn.nascommon.db.objecbox.DeviceSettings
import net.sdvn.nascommon.db.objecbox.UserInfo
import net.sdvn.nascommon.model.DataSessionUser
import net.sdvn.nascommon.model.http.OnHttpRequestListener
import net.sdvn.nascommon.model.oneos.BaseResultModel
import net.sdvn.nascommon.model.oneos.DevAttrInfo
import net.sdvn.nascommon.model.oneos.OneOSInfo
import net.sdvn.nascommon.model.oneos.UpdateInfo
import net.sdvn.nascommon.model.oneos.api.BaseAPI
import net.sdvn.nascommon.model.oneos.api.OneOSDeviceInfoAPI
import net.sdvn.nascommon.model.oneos.api.OneOSGetMacAPI
import net.sdvn.nascommon.model.oneos.api.sys.OneOSGetUpdateInfoAPI
import net.sdvn.nascommon.model.oneos.api.sys.OneOSHDInfoAPI
import net.sdvn.nascommon.model.oneos.api.sys.OneOSVersionAPI
import net.sdvn.nascommon.model.oneos.user.LoginSession
import net.sdvn.nascommon.utils.EmptyUtils
import net.sdvn.nascommon.utils.GsonUtils
import net.sdvn.nascommon.utils.log.Logger
import net.sdvn.nascommonlib.R
import java.util.*

/**
 * OneSpace OS Login API
 *
 *
 * Created by gaoyun@eli-tech.com on 2016/1/8.
 */
class OneOSLoginAPI : BaseAPI {


    private var isV6: Boolean = false
    private var isOneOS: Boolean = false
    private var isV5: Boolean = false
    private var listener: OnLoginListener? = null
    private var user: String? = null
    private var pwd: String? = null
    private var mac: String? = null
    private var domain = AppConstants.DOMAIN_DEVICE_LAN
    //private int trys = 0;

    private val httpListener = object : OnHttpRequestListener {
        override fun onStart(url: String) {

        }

        override fun onSuccess(url: String, result: String) {
            Logger.p(Logger.Level.DEBUG, Logger.Logd.OSAPI, TAG, "Response Data:$result")
            if (listener != null) {
                try {
                    val type = object : TypeToken<BaseResultModel<DataSessionUser>>() {}.type
                    val decodeJSON = GsonUtils.decodeJSON<BaseResultModel<DataSessionUser>>(result, type)
//                    val json = JSONObject(result)
//                    val ret = json.getBoolean("result")
                    // Response Data:{"result":true, "model":"a20", "id":1,"username":"admin","email":"admin@eli-tech.com","admin":1,"phone":"-", "remark":"-", "session":"bu4p1h9armlc4kqpe6i2l75p0fm8lljvta54fe74n8899piu62f0===="}
                    //
                    if (decodeJSON.isSuccess) {
                        //{
                        //      "result":true,"data":{"session":"57a33197283ada5e8ca6e868051585b3",
                        //      "user":{"username":"admin","nickname":"OneSpace",
                        // "email":"admin@onespace.com","phone":"18805518888",
                        // "role":0,"avatar":"","remark":"Admin user created default",
                        // "uid":1006,"gid":0,"admin":1,"space":1024,"used":0,"isdelete":0,"create":0}}
                        // }

//                        var devAttrInfo: DevAttrInfo? = null
//                        val datajson = json.getJSONObject("data")
//                        val userjson: JSONObject = datajson.getJSONObject("user")
//                        if (datajson.has("devinfo") && !datajson.isNull("devinfo")) {
//                            val devinfo = datajson.getString("devinfo")
//                            devAttrInfo = GsonUtils.decodeJSON(devinfo, DevAttrInfo::class.java)
//                        }
//                        val uid = userjson.getInt("uid")
//                        val gid = userjson.getInt("gid")
//                        val admin = userjson.getInt("admin")
//                        val role = userjson.getInt("role")
//                        val isdelete = userjson.getInt("isdelete")
//                        val username = userjson.getString("username")
//                        val nickname = userjson.getString("nickname")
//                        val email = userjson.getString("email")
//                        val phone = userjson.getString("phone")
//                        val avatar = userjson.getString("avatar")
//                        val remark = userjson.getString("remark")
//                        val space = userjson.getLong("space")
//                        val used = userjson.getLong("used")
//                        val encrypt = userjson.getString("encrypt")
//                        val create_at = userjson.getLong("create_at")
//                        val session = datajson.getString("session")
                        val time = System.currentTimeMillis()
//                        val userInfo = UserInfo(null, user ?: "", mac
//                                ?: "", pwd, admin, uid, gid, domain, time,
//                                false, true, false, username,
//                                nickname, email, phone, role, avatar, remark, 1, space, used,
//                                encrypt, isdelete, time, create_at, "", "")
//
                        val devAttrInfo = decodeJSON.data.devAttrInfo
                        val userInfo = decodeJSON.data.user
                        val session = decodeJSON.data.session!!
                        if (!EmptyUtils.isEmpty(mac)) {
                            genLoginSession(mac!!, userInfo, session, time, domain, devAttrInfo)
                        } else {
                            // get device mac address
                            val getMacAPI = OneOSGetMacAPI(ip, port, domain != AppConstants.DOMAIN_DEVICE_SSUDP)
                            val finalDevAttrInfo = devAttrInfo
                            getMacAPI.setOnGetMacListener(object : OneOSGetMacAPI.OnGetMacListener {
                                override fun onStart(url: String) {}

                                override fun onSuccess(url: String, mac: String) {
                                    genLoginSession(mac, userInfo, session, time, domain, finalDevAttrInfo)
                                }

                                override fun onFailure(url: String, errorNo: Int, errorMsg: String) {
                                    val msg = context.resources.getString(R.string.error_get_device_mac)
                                    listener!!.onFailure(url, errorNo, msg)
                                }
                            })

                            getMacAPI.getMac()

                        }
                    } else {
//                        val errJson = json.getJSONObject("error")
//                        val errorNo = errJson.getInt("code")
//                        val msg = errJson.getString("msg")
                        httpRequest.callbackOnUIThread {
                            listener!!.onFailure(url, decodeJSON.error?.code
                                    ?: HttpErrorNo.ERR_ONE_NO_FOUND, decodeJSON.error?.msg ?: "")
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    httpRequest.callbackOnUIThread { listener!!.onFailure(url, HttpErrorNo.ERR_JSON_EXCEPTION, context.resources.getString(R.string.error_json_exception)) }
                }

            }
        }

        override fun onFailure(url: String, httpCode: Int, errorNo: Int, strMsgf: String) {
            httpRequest.callbackOnUIThread {
                if (listener != null) {
                    var strMsg = strMsgf
                    if (errorNo == HttpErrorNo.ERR_ONEOS_VERSION) {
                        strMsg = context.resources.getString(R.string.oneos_version_mismatch)
                    }
                    listener!!.onFailure(url, errorNo, strMsg)
                }
            }
        }
    }


    private var mAccess_token: String? = null
    private var mUpdatePwd: Boolean = false
    private var ip: String? = null
    private var port: String? = null

    private var username: String? = null

    constructor(ip: String, port: String, user: String, pwd: String, mac: String) : super(ip, OneOSAPIs.USER) {
        this.ip = ip
        this.port = port
        this.user = user
        this.pwd = pwd
        this.mac = mac
    }

    constructor(ip: String, port: String, access_token: String, tag: String) : super(ip, OneOSAPIs.USER) {
        this.ip = ip
        this.port = port
        mAccess_token = access_token
        this.mac = tag
    }

    fun access(domain: Int, isV5: Boolean, isOneOs: Boolean) {
        this.domain = domain
        this.isOneOS = isOneOs
        this.isV5 = isV5
//        isV6 = SessionCache.instance.isV5(mac!!)
        val subscribe = Observable.create<Boolean> {
            val isWebApi: Boolean = SessionCache.instance.isV5SyncRequest(mac!!, ip!!)
            it.onNext(isWebApi)
        }.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                isV6 = it
                if (isV5 || isV6) {
                    val observer: V5Observer<Any?> = object : V5Observer<Any?>(mac ?: "") {
                        override fun onSubscribe(d: Disposable) {
                            super.onSubscribe(d)
                            httpListener.onStart("")
                        }

                        override fun success(result: BaseProtocol<Any?>) {
                            httpListener.onSuccess("", Gson().toJson(result))
                        }

                        override fun fail(result: BaseProtocol<Any?>) {
                            httpListener.onFailure(
                                "", result.error?.code
                                    ?: HttpErrorNo.UNKNOWN_EXCEPTION, result.error?.code
                                    ?: HttpErrorNo.UNKNOWN_EXCEPTION, result.error?.msg ?: ""
                            )
                        }

                        override fun isNotV5() {
                            isV6 = false
                        }
                    }
                    SessionCache.instance.asynRequestSession(mac ?: "", ip!!, mAccess_token ?: "")
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(observer)

                } else {
                    //        url = genOneOSAPIUrl(OneOSAPIs.USER);
                    //        Logger.p(Level.DEBUG, Logd.DEBUG, TAG, "Login: " + url);
                    val params = HashMap<String, Any>()
                    params["token"] = mAccess_token!!
                    setParams(params)
                    setMethod("menet")
                    httpRequest.setParseResult(false)
                    httpRequest.setBackOnUI(false)
                    httpRequest.post(oneOsRequest, httpListener)
                    //        httpUtils.postJson(url, new RequestBody("menet", "", params), httpListener);
                    if (listener != null) {
                        if (!mUpdatePwd)
                            listener!!.onStart(url())
                    }
                }
            }
    }


    fun accessOneOS(domain: Int) {
        this.isOneOS = true
        this.domain = domain
        //        url = genOneOSAPIUrl(OneOSAPIs.USER);
        //        Logger.p(Level.DEBUG, Logd.DEBUG, TAG, "Login: " + url);
        val params = HashMap<String, Any>()
        params["token"] = mAccess_token!!
        setParams(params)
        setMethod("menet")
        httpRequest.setParseResult(false)
        httpRequest.setBackOnUI(false)
        httpRequest.post(oneOsRequest, httpListener)
        //        httpUtils.postJson(url, new RequestBody("menet", "", params), httpListener);
        if (listener != null) {
            if (!mUpdatePwd)
                listener!!.onStart(url())
        }
    }

    fun setOnLoginListener(listener: OnLoginListener) {
        this.listener = listener
    }

    fun login(domain: Int) {
        this.domain = domain
        //        url = genOneOSAPIUrl(OneOSAPIs.USER);
        //        Logger.p(Level.DEBUG, Logd.DEBUG, TAG, "Login: " + url);
        val params = HashMap<String, Any>()
        params["username"] = user!!
        params["password"] = pwd!!
        params["keep"] = 1
        setParams(params)
        setMethod("login")
        httpRequest.setParseResult(false)
        httpRequest.post(oneOsRequest, httpListener)
        //        httpUtils.postJson(url, new RequestBody("login", "", params), httpListener);

        if (listener != null) {
            listener!!.onStart(url())
        }
    }

    private fun checkOneOSVersion(loginSession: LoginSession) {
        val versionAPI = OneOSVersionAPI(ip, port, domain != AppConstants.DOMAIN_DEVICE_SSUDP)
        versionAPI.setOnSystemVersionListener(object : OneOSVersionAPI.OnSystemVersionListener {
            override fun onStart(url: String) {}

            override fun onSuccess(url: String, info: OneOSInfo) {
                loginSession.oneOSInfo = info
//                if (OneOSVersionManager.check(info.version)) {
//                    listener!!.onSuccess(url, loginSession)
//                } else {
//                    val msg = String.format(context.resources.getString(R.string.fmt_oneos_version_upgrade), OneOSVersionManager.MIN_ONEOS_VERSION)
//                    listener!!.onFailure(url, HttpErrorNo.ERR_ONEOS_VERSION, msg)
//                }
                checkIfNeedUpdate(loginSession)
            }

            override fun onFailure(url: String, errorNo: Int, errorMsg: String) {
                Logger.LOGE(TAG, "Get oneos version error: $errorMsg")
                val msg = context.resources.getString(R.string.oneos_version_check_failed)
                listener!!.onFailure(url, HttpErrorNo.ERR_ONEOS_VERSION, msg)
            }
        })
        versionAPI.query()
    }


    private fun checkIfNeedUpdate(loginSession: LoginSession) {
        if (loginSession.isAdmin) {
            val oneOSGetUpdateInfoAPI = OneOSGetUpdateInfoAPI(loginSession)
            oneOSGetUpdateInfoAPI.setOnUpdateInfoListener(object : OneOSGetUpdateInfoAPI.OnUpdateInfoListener {
                override fun onStart(url: String) {

                }

                @SuppressLint("CheckResult")
                override fun onSuccess(url: String, updateInfo: UpdateInfo) {
                    if (updateInfo.isNeedup) {
                        loginSession.oneOSInfo?.isNeedsUp = true
                    }
                }

                override fun onFailure(url: String, errorNo: Int, errorMsg: String) {

                }
            })
            oneOSGetUpdateInfoAPI.get()
        }
    }

    //设真实的服务器登录名，替换admin的默认下载路径
    fun setTrueUser(username: String) {
        this.username = username
    }

    fun genLoginSession(mac: String, userInfo: UserInfo, session: String, time: Long, domain: Int, devAttrInfo: DevAttrInfo?) {

        val id: Long // user id
        var deviceSettings: DeviceSettings?
        if (user.isNullOrEmpty()) {
            user = userInfo.username
        }
        if (username.isNullOrEmpty()) {
            username = user
        }
        var userInfoQuery = UserInfoKeeper.getUserInfo(username, mac)
        if (null == userInfoQuery) {
            Logger.LOGI(TAG, "userInfoQuery is null")
            //            loginSession = new UserInfo(null, user, mac, pwd, admin, uid, gid, domain, time, false, true);
            userInfo.name = username!!
            userInfo.mac = mac
//            mUpdatePwd = updateUserPwd(mac, userInfo, session)
            userInfo.domain = domain
            userInfo.time = time
            userInfo.isLogout = false
            userInfo.isActive = true
            userInfoQuery = userInfo
            id = UserInfoKeeper.insert(userInfoQuery)
            //            if (id == -1) {
            //                Logger.p(Level.ERROR, true, TAG, "Insert UserInfo Error: " + id);
            //                new Throwable(new Exception("Insert UserInfo Error"));
            //                return;
            //            } else {
            deviceSettings = DeviceSettingsKeeper.insertDefault(mac, username)
            //            }
        } else {
            userInfoQuery.admin = userInfo.admin
            userInfoQuery.uid = userInfo.uid
            userInfoQuery.gid = userInfo.gid
            userInfoQuery.time = time
            userInfoQuery.domain = domain
            userInfoQuery.isLogout = false
            userInfoQuery.isActive = true
            userInfoQuery.username = userInfo.username
            userInfoQuery.nickname = userInfo.nickname
            userInfoQuery.avatar = userInfo.avatar
            userInfoQuery.email = userInfo.email
            userInfoQuery.phone = userInfo.phone

            userInfoQuery.role = userInfo.role
            userInfoQuery.space = userInfo.space
            userInfoQuery.used = userInfo.used
            userInfoQuery.encrypt = userInfo.encrypt
            userInfoQuery.isdelete = userInfo.isdelete
            userInfoQuery.gender = userInfo.gender
            userInfoQuery.login_time = userInfo.login_time
            userInfoQuery.create_at = userInfo.create_at
            userInfoQuery.remark = userInfo.remark
            userInfoQuery.permissions = userInfo.permissions
//            mUpdatePwd = updateUserPwd(mac, userInfoQuery, session)
            UserInfoKeeper.update(userInfoQuery)

            id = userInfoQuery.id!!

            deviceSettings = DeviceSettingsKeeper.getSettings(mac)
            if (deviceSettings == null)
                deviceSettings = DeviceSettingsKeeper.insertDefault(mac, username)
        }
        val finalUserInfoQuery = userInfoQuery
        setUserInfoMarkInfo(mac, session, userInfoQuery)

        //        if (mUpdatePwd) return;
        Logger.p(Logger.Level.INFO, true, TAG, "Login User ID: $id")

        var isNewDevice = false
        var deviceInfo = DeviceInfoKeeper.query(mac)
        if (null == deviceInfo) {
            deviceInfo = DeviceInfo(mac)
            isNewDevice = true
        }
        deviceInfo.mac = mac
        deviceInfo.time = time
        deviceInfo.domain = domain
        if (domain == AppConstants.DOMAIN_DEVICE_LAN) {
            deviceInfo.lanIp = ip
            deviceInfo.lanPort = port
        } else if (domain == AppConstants.DOMAIN_DEVICE_WAN) {
            deviceInfo.wanIp = ip
            deviceInfo.wanPort = port
        } else if (domain == AppConstants.DOMAIN_DEVICE_VIP) {
            deviceInfo.vIp = ip
            deviceInfo.vipPort = port
        }

        if (isNewDevice) {
            DeviceInfoKeeper.insert(deviceInfo)
        } else {
            DeviceInfoKeeper.update(deviceInfo)
        }
        Logger.p(Logger.Level.DEBUG, Logger.Logd.DEBUG, TAG, String.format("userinfo=%s, deviceInfo=%s, userStting=%s, session=%s, isNew=%s", userInfo, deviceInfo, deviceSettings, session, isNewDevice))
        val loginSession = LoginSession(mac, finalUserInfoQuery, deviceInfo, deviceSettings!!, session, isNewDevice, time)
        loginSession.devAttrInfo = devAttrInfo

        loginSession.isOneOS = isOneOS
        if (isOneOS) {
            checkOneOSVersion(loginSession)
            checkHDInfo(loginSession)
        }
        if (isV6) {
            updateFavorites(mac, loginSession, userInfoQuery)
        }
        listener!!.onSuccess(ip!!, loginSession)
        loginSession.checkIfShareAvailable()
        loginSession.checkIfBtServerAvailable()
    }

    private fun updateFavorites(devId: String, loginSession: LoginSession, userInfo: UserInfo) {
        V5Repository.INSTANCE().tags(devId, loginSession.ip, LoginTokenUtil.getToken(), object : V5Observer<List<FileTag>>(devId) {
            override fun success(result: BaseProtocol<List<FileTag>>) {
                if (result.result) {
                    result.data?.find { it.name == FileTag.TAG_FAVORITE }?.let {
                        userInfo.favoriteId = it.id
                        UserInfoKeeper.update(userInfo)
                    }
                }
            }

            override fun fail(result: BaseProtocol<List<FileTag>>) {
            }

            override fun isNotV5() {

            }
        })
    }

    private fun checkHDInfo(loginSession: LoginSession) {
        val osFormatAPI = OneOSHDInfoAPI(this.ip, this.port)
        osFormatAPI.setTag(this.mac)
        osFormatAPI.setListener(object : OneOSHDInfoAPI.OnHDInfoListener<String, Any> {
            override fun onStart(url: String) {}

            override fun onSuccess(url: String, tag: Any, error: String, count: String) {
                try {
                    loginSession.hdCount = Integer.parseInt(count)
                    loginSession.hdError = Integer.parseInt(error)
                } catch (e: Exception) {
                    e.printStackTrace()
                }

            }

            override fun onFailure(url: String, tag: Any, errorNo: Int, errorMsg: String) {}
        })
        osFormatAPI.getHdInfo("")
    }

    //更新用户密码
    private fun updateUserPwd(deviceId: String, userInfo: UserInfo, session: String): Boolean {
        pwd = SPHelper.get(AppConstants.SP_FIELD_PWD, "")
        if (!TextUtils.isEmpty(pwd) && pwd != userInfo.pwd && mAccess_token != null && !userInfo.username.isNullOrEmpty()) {
            val mListener = object : OneOSUserManageAPI.OnUserManageListener {
                override fun onStart(url: String) {}

                override fun onSuccess(url: String, cmd: String) {
                    userInfo.pwd = pwd!!
                    UserInfoKeeper.update(userInfo)
                    if (mAccess_token == null) {
                        login(domain)
                    }
                }

                override fun onFailure(url: String, errorNo: Int, errorMsg: String) {
                    //                    if (listener != null) {
                    //                        listener.onFailure(url, SdvnHttpErrorNo.ERR_ONE_REQUEST, errorNo + "fail updatepasswd " + errorMsg);
                    //                    }
                }
            }

            val observer = object : V5Observer<Any>(deviceId) {
                override fun success(result: BaseProtocol<Any>) {
                    mListener?.onSuccess("", "")
                }

                override fun fail(result: BaseProtocol<Any>) {
                    mListener?.onFailure("", result.error?.code ?: 0, result.error?.msg ?: "")
                }

                override fun isNotV5() {
                    val manageAPI = OneOSUserManageAPI(ip, port, session)
                    manageAPI.setOnUserManageListener(mListener)
                    manageAPI.chpwd(userInfo.username, pwd)
                }

                override fun retry(): Boolean {
                    V5Repository.INSTANCE().updateUserPassword(deviceId, ip
                            ?: "", LoginTokenUtil.getToken(), userInfo.username!!, pwd ?: "", this)
                    return true
                }
            }
            AppConstants.DEFAULT_USERNAME_ADMIN
            V5Repository.INSTANCE().updateUserPassword(deviceId, ip
                    ?: "", LoginTokenUtil.getToken(), userInfo.username!!, pwd ?: "", observer)
            return true
        }
        return false

    }

    /**
     * 设置用户设备备注信息
     *
     * @param session
     * @param finalUserInfoQuery
     */
    private fun setUserInfoMarkInfo(deviceId: String, session: String, finalUserInfoQuery: UserInfo) {
        val deviceInfoAPI = OneOSDeviceInfoAPI(ip, port, session)
        val onDeviceInfoListener = object : OneOSDeviceInfoAPI.OnDeviceInfoListener {
            override fun onStart(url: String) {

            }

            override fun onSuccess(url: String, info: OneOSDeviceInfoAPI.SubInfo?) {
                Logger.LOGE(TAG, "UserInfoMarkInfo :$info")
                if (info != null) {
                    UserInfoKeeper.saveDevMarkInfo(finalUserInfoQuery.name, finalUserInfoQuery.mac, info.name, info.desc, false)
                    SessionManager.getInstance().getDeviceModel(deviceId)?.devName = info.name
                }
            }

            override fun onFailure(url: String, errorNo: Int, errorMsg: String) {
                Logger.LOGE(TAG, "UserInfoMarkInfo :$errorNo$errorMsg")
            }
        }
        deviceInfoAPI.setListener(onDeviceInfoListener)

        if (finalUserInfoQuery.devInfoChanged &&
                (!TextUtils.isEmpty(finalUserInfoQuery.devMarkName)
                        || !TextUtils.isEmpty(finalUserInfoQuery.devMarkDesc))) {

            val observer = object : V5Observer<Any>(deviceId) {
                override fun success(result: BaseProtocol<Any>) {
                    try {
                        onDeviceInfoListener.onSuccess("", Gson().fromJson(Gson().toJson(result.data), OneOSDeviceInfoAPI.SubInfo::class.java))
                    } catch (e: Exception) {
                    }
                }

                override fun fail(result: BaseProtocol<Any>) {
                    onDeviceInfoListener.onFailure("", result.error?.code ?: 0, result.error?.msg
                            ?: "")
                }

                override fun isNotV5() {
                    deviceInfoAPI.update(finalUserInfoQuery.devMarkName, finalUserInfoQuery.devMarkDesc)
                }

                override fun retry(): Boolean {
                    return false
                }
            }
            V5Repository.INSTANCE().setDeviceMark(deviceId, ip!!, LoginTokenUtil.getToken(), finalUserInfoQuery.devMarkName, finalUserInfoQuery.devMarkDesc, observer)


        } else {
            val observer = object : V5Observer<DataDevMark>(deviceId) {
                override fun success(result: BaseProtocol<DataDevMark>) {
                    try {
                        onDeviceInfoListener.onSuccess("", OneOSDeviceInfoAPI.SubInfo().apply {
                            name = result.data?.name
                            desc = result.data?.desc
                        })
                    } catch (e: Exception) {
                    }
                }

                override fun fail(result: BaseProtocol<DataDevMark>) {
                    onDeviceInfoListener.onFailure("", result.error?.code ?: 0, result.error?.msg
                            ?: "")
                }

                override fun isNotV5() {
                    deviceInfoAPI.query()
                }

                override fun retry(): Boolean {
                    return false
                }
            }
            V5Repository.INSTANCE().getDeviceMark(deviceId, ip!!, LoginTokenUtil.getToken(), observer)
        }
    }

    @Keep
    interface OnLoginListener {
        fun onStart(url: String?)

        fun onSuccess(url: String, loginSession: LoginSession)

        fun onFailure(url: String, errorNo: Int, errorMsg: String)
    }

    companion object {
        private val TAG = OneOSLoginAPI::class.java.simpleName
    }


}
