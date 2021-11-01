package net.sdvn.nascommon.model.oneos.user

import android.text.TextUtils
import io.reactivex.disposables.Disposable
import io.reactivex.functions.Consumer
import io.weline.repo.torrent.BTHelper
import libs.source.common.livedata.Status
import net.sdvn.nascommon.constant.AppConstants
import net.sdvn.nascommon.constant.OneOSAPIs
import net.sdvn.nascommon.db.objecbox.DeviceInfo
import net.sdvn.nascommon.db.objecbox.DeviceSettings
import net.sdvn.nascommon.db.objecbox.UserInfo
import net.sdvn.nascommon.fileserver.FileShareHelper
import net.sdvn.nascommon.iface.Callback
import net.sdvn.nascommon.model.oneos.DevAttrInfo
import net.sdvn.nascommon.model.oneos.OneOSInfo
import net.sdvn.nascommonlib.BuildConfig
import java.io.Serializable

/**
 * User Login information
 *
 *
 * Created by gaoyun@eli-tech.com on 2016/1/7.
 */
class LoginSession : Serializable {
    var isV5: Boolean = false
    var isOneOS: Boolean = false
    var isShareV2Available: Boolean = false
    var isBtServerAvailable: Boolean = BuildConfig.DEBUG || false

    /**
     * User information
     */
    var userInfo: UserInfo? = null

    /**
     * User settings
     */
    var deviceSettings: DeviceSettings? = null

    /**
     * Login device information
     */
    var deviceInfo: DeviceInfo? = null

    /**
     * Login mSession
     */
    var session: String? = null

    /**
     * OneOS information
     */
    var oneOSInfo: OneOSInfo? = null

    /**
     * Whether is new device to database
     */
    var isNew: Boolean = false

    /**
     * Login timestamp
     */
    var loginTime: Long = 0

    var hdError = -1
    var hdCount = -1
    var devAttrInfo: DevAttrInfo? = null
    var isHDStatusEnable: Boolean = true
    var id: String? = null
        private set

    val ip: String
        get() {
            if (null == deviceInfo) {
                return ""
            }

            if (deviceInfo!!.domain == AppConstants.DOMAIN_DEVICE_WAN) {
                return deviceInfo!!.wanIp
            }
            return if (deviceInfo!!.domain == AppConstants.DOMAIN_DEVICE_VIP) deviceInfo!!.vIp else deviceInfo!!.lanIp


        }

    val port: String?
        get() {
            if (null == deviceInfo) {
                return null
            }

            if (deviceInfo!!.domain == AppConstants.DOMAIN_DEVICE_WAN) {
                return deviceInfo!!.wanPort
            }
            return if (deviceInfo!!.domain == AppConstants.DOMAIN_DEVICE_VIP) deviceInfo!!.vipPort else deviceInfo!!.lanPort

        }

    /**
     * Formatted url, such as http://192.168.1.17:80
     *
     * @return Formatted url
     */
    val url: String?
        get() {
            if (null != deviceInfo) {
                val ip: String
                val port: String
                if (deviceInfo!!.domain == AppConstants.DOMAIN_DEVICE_VIP) {
                    ip = deviceInfo!!.vIp
                    port = deviceInfo!!.vipPort
                } else if (deviceInfo!!.domain == AppConstants.DOMAIN_DEVICE_WAN) {
                    ip = deviceInfo!!.wanIp
                    port = deviceInfo!!.wanPort
                } else {
                    ip = deviceInfo!!.lanIp
                    port = deviceInfo!!.lanPort
                }

                return OneOSAPIs.PREFIX_HTTP + ip + ":" + port
            }

            return null
        }

    /**
     * Whether the user is an administrator
     *
     * @return `true` if administrator, `false` otherwise.
     */
    val isAdmin: Boolean
        get() = userInfo!!.admin == 1

    /**
     * Whether LAN
     *
     * @return `true` if LAN, `false` otherwise.
     */
    val isLANDevice: Boolean
        get() = null == userInfo || userInfo!!.domain == AppConstants.DOMAIN_DEVICE_LAN

    /**
     * Whether WAN
     *
     * @return `true` if WAN, `false` otherwise.
     */
    val isWANDevice: Boolean
        get() = null != userInfo && userInfo!!.domain == AppConstants.DOMAIN_DEVICE_WAN

    /**
     * Whether SSUDP
     *
     * @return `true` if SSUDP, `false` otherwise.
     */
    val isSSUDPDevice: Boolean
        get() = null != userInfo && userInfo!!.domain == AppConstants.DOMAIN_DEVICE_SSUDP

    //session 未超过过期时间
    val isLogin: Boolean
        get() = (userInfo != null
                && deviceInfo != null
                && !TextUtils.isEmpty(ip)
                && !TextUtils.isEmpty(session)
                && System.currentTimeMillis() - loginTime < AppConstants.SESSION_LIVE_TIME)

    constructor(id: String) {
        this.id = id
    }

    constructor(id: String, userInfo: UserInfo, deviceInfo: DeviceInfo, deviceSettings1: DeviceSettings, session: String, isNew: Boolean, time: Long) {
        this.id = id
        this.userInfo = userInfo
        this.deviceInfo = deviceInfo
        this.deviceSettings = deviceSettings1
        this.session = session
        this.isNew = isNew
        this.loginTime = time
    }

    constructor(loginSession: LoginSession) {
        this.userInfo = loginSession.userInfo
        this.deviceInfo = loginSession.deviceInfo
        this.deviceSettings = loginSession.deviceSettings
        this.session = loginSession.session
        this.isNew = loginSession.isNew
        this.loginTime = loginSession.loginTime
        this.devAttrInfo = loginSession.devAttrInfo
        this.oneOSInfo = loginSession.oneOSInfo
        this.hdError = loginSession.hdError
        this.hdCount = loginSession.hdCount
        this.isHDStatusEnable = loginSession.isHDStatusEnable
        this.isShareV2Available = loginSession.isShareV2Available
        this.isBtServerAvailable = loginSession.isBtServerAvailable
        this.id = loginSession.id
        this.isV5 = loginSession.isV5
        this.isOneOS = loginSession.isOneOS
    }

    fun refreshData(loginSession: LoginSession): LoginSession {
        this.userInfo = loginSession.userInfo
        this.deviceInfo = loginSession.deviceInfo
        this.deviceSettings = loginSession.deviceSettings
        this.session = loginSession.session
        this.isNew = loginSession.isNew
        this.loginTime = loginSession.loginTime
        this.devAttrInfo = loginSession.devAttrInfo
        this.oneOSInfo = loginSession.oneOSInfo
//        this.hdError = loginSession.hdError
//        this.hdCount = loginSession.hdCount
//        this.isHDStatusEnable = loginSession.isHDStatusEnable
//        this.isShareV2Available = loginSession.isShareV2Available
        return this
    }

    fun checkIfShareAvailable(): Disposable? {
        return FileShareHelper.checkAvailable(ip, Callback {
            isShareV2Available = it.isSuccess
        })

    }

    fun checkIfBtServerAvailable(): Disposable? {
        return BTHelper.checkAvailable(ip, Consumer {
            isBtServerAvailable = it.status == Status.SUCCESS
        })
    }

    companion object {
        val HD_ERROR_STATUS_NO_SATA = -1
        private const val serialVersionUID = 3391671502123128628L
    }
}



