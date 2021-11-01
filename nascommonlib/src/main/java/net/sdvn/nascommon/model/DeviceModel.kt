package net.sdvn.nascommon.model


import android.annotation.SuppressLint
import android.text.TextUtils
import io.reactivex.Observable
import io.reactivex.ObservableOnSubscribe
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import io.weline.devhelper.IconHelper.getIconByeDevClass
import net.sdvn.cmapi.CMAPI
import net.sdvn.cmapi.Device
import net.sdvn.cmapi.global.Constants
import net.sdvn.common.internet.protocol.entity.HardWareDevice
import net.sdvn.common.repo.InNetDevRepo
import net.sdvn.nascommon.SessionManager
import net.sdvn.nascommon.constant.AppConstants
import net.sdvn.nascommon.db.UserInfoKeeper
import net.sdvn.nascommon.iface.EventListener
import net.sdvn.nascommon.iface.Result
import net.sdvn.nascommon.model.oneos.user.LoginSession
import net.sdvn.nascommon.utils.SPUtils
import net.sdvn.nascommonlib.R
import java.util.concurrent.CopyOnWriteArrayList

/**
 * Created by yun on 18/05/19.
 */

class DeviceModel(val devId: String) {
    init {
        devNameFromDB.subscribe()
    }

    var devVip: String = ""
        get() {
            return device?.vip ?: ""
        }
    var isInNetProvide: Boolean = false
        get() {
            val netDeviceModel = InNetDevRepo.getNetDeviceModel(devId)
            return netDeviceModel?.enable ?: false
        }

    var isSrcProvide: Boolean = false
        get() {
            return wareDevice?.isSrcProvide ?: false
        }
    var isBtServerAvailable: Boolean
        get() = if (!_isBtServerAvailable && loginSession != null) {
            loginSession!!.isBtServerAvailable
        } else {
            _isBtServerAvailable
        }
        set(value) {
            _isBtServerAvailable = value
            if (loginSession != null) {
                loginSession!!.isBtServerAvailable = value
            }
        }
    private var _isBtServerAvailable: Boolean = false
    private var _isShareV2Available: Boolean = false
    var isShareV2Available
        get() = if (!_isShareV2Available && loginSession != null) {
            loginSession!!.isShareV2Available
        } else {
            _isShareV2Available
        }
        set(value) {
            _isShareV2Available = value
            if (loginSession != null) {
                loginSession!!.isShareV2Available = value
            }
        }

    @get:Synchronized
    @set:Synchronized
    var isRequestSession: Boolean = false

    private var listeners: MutableList<EventListener<LoginSession>>? = null

    var device: Device? = null
    var wareDevice: HardWareDevice? = null
        set(value) {
            if (value != null) {
                setDeviceName(value.devicename)
            }
            field = value
        }
    var loginSession: LoginSession? = null

    fun getIcon(): Int {
        return if (isVNode) R.drawable.icon_node else getIconByeDevClass(devClass, true, true)
    }

    var isVNode: Boolean = false
        get() = if (device == null) {
            false
        } else {
            device!!.deviceType == Constants.DT_V_NODE
        }

    var isEn: Boolean = false
        get() = wareDevice?.isEN ?: false
    var devClass: Int = 0
        get() = if (device != null) device!!.devClass else if (wareDevice != null) wareDevice!!.ostype else 0
    var isOnline: Boolean = false
        get() = device?.isOnline ?: false
    var isEnable: Boolean = false
        get() = device?.isDevDisable == false
    var isOwner: Boolean = false
        get() = wareDevice?.isOwner ?: false
    var isAdmin: Boolean = false
        get() = wareDevice?.isAdmin ?: false
    var isEnableUseSpace: Boolean = false
        get() = wareDevice?.isEnableUseSpace ?: false

    // markname
    private var mDevMarkName: String? = null
    private var mDeviceName: String? = null
    var devName: String
        @SuppressLint("CheckResult")
        get() = if (SPUtils.getBoolean(AppConstants.SP_SHOW_REMARK_NAME, true)) {
            if (mDevMarkName == null) {
                devNameFromDB.subscribe {
                    devName = it
                }
                if (!devSn.isNullOrEmpty()) {
                    UiUtils.genNameBySN(devSn!!)
                } else {
                    device?.name ?: ""
                }
            } else {
                mDevMarkName!!
            }
        } else {
            mDeviceName ?: wareDevice?.devicename ?: device?.name ?: ""
        }
        set(value) {
            mDevMarkName = value
        }

    /**
     * 必须调用devNameFromDB后调用才有效
     */
    fun getRemakName(): String? {
        return if (TextUtils.isEmpty(mDevMarkName)) devName else mDevMarkName
    }

    private var _devSn: String? = null
    var devSn: String?
        get() = if (wareDevice != null)
            wareDevice!!.devicesn
        else
            _devSn
        set(value) {
            _devSn = value
        }

    //        if (mLoginSession != null && mLoginSession.getUserInfo() != null && !TextUtils.isEmpty(mLoginSession.getUserInfo().getDevMarkName())) {
    //            final String devMarkName = mLoginSession.getUserInfo().getDevMarkName();
    //            setDevName(devMarkName);
    //            return Observable.just(devMarkName);
    //        }
    val devNameFromDB: Observable<String>
        @SuppressLint("CheckResult")
        get() = Observable.just(Result<String>(mDevMarkName))
                .flatMap { stringResult ->
                    if (TextUtils.isEmpty(stringResult.data)) {
                        Observable.create(ObservableOnSubscribe<String> { emitter ->
                            val user = SessionManager.getInstance().username
                            val devicesn = devSn ?: ""
                            val defaultName = if (TextUtils.isEmpty(devicesn)) {//sn为空，直接显示设备名
                                device?.name ?: ""
                            } else {
                                UiUtils.genNameBySN(devicesn)
                            }
                            val deviceName = //if (devicesn.isEmpty()) {
                                    device?.name ?: wareDevice?.devicename
                                    ?: defaultName
                            //} else {
                            //   UiUtils.genNameBySN(devicesn)
                            // }
                            if (TextUtils.isEmpty(user)) {
                                emitter.onNext(deviceName)
                                return@ObservableOnSubscribe
                            }
                            val userInfo = UserInfoKeeper.getUserInfo(user, devId)
                            if (userInfo != null) {
                                val markName = userInfo.devMarkName
                                if (!TextUtils.isEmpty(markName)) {
                                    this@DeviceModel.devName = markName
                                    this@DeviceModel.mDevMarkName = markName
                                    emitter.onNext(markName)
                                } else {
                                    this@DeviceModel.devName = deviceName
                                    emitter.onNext(deviceName)
                                }
                            } else {
                                this@DeviceModel.devName = deviceName
                                emitter.onNext(deviceName)
                            }
                            emitter.onComplete()
                        }).subscribeOn(Schedulers.single())
                                .observeOn(AndroidSchedulers.mainThread())
                    } else {
                        Observable.just(devName)
                    }
                }

    fun getListeners(): List<EventListener<LoginSession>>? {
        return listeners
    }

    fun addEventListener(listener: EventListener<LoginSession>) {
        if (listeners == null) {
            listeners = CopyOnWriteArrayList()
        }
        listeners!!.add(listener)
    }

    fun removeEventListener(listener: EventListener<LoginSession>?) {
        if (listeners != null && listener != null) {
            listeners!!.remove(listener)
        }
    }

    override fun toString(): String {
        return "DeviceModel(devId='$devId', _isBtServerAvailable=$_isBtServerAvailable, _isShareV2Available=$_isShareV2Available, mDevName=$mDevMarkName, listeners=$listeners, device=$device, wareDevice=$wareDevice, loginSession=$loginSession, _devSn=$_devSn)"
    }

    fun isEnableDownloadShare(): Boolean {
        return isOnline && ((!isEn && isEnableUseSpace) || (isEn && isOwner))
    }

    fun hasAdminRights(): Boolean {
        return isOwner || isAdmin
    }

    fun isInCurrentNet(): Boolean {
        return isInNet(CMAPI.getInstance().realtimeInfo?.currentNetwork?.id)
    }

    fun isInNet(netId: String?): Boolean {
        return netId == wareDevice?.networkId
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is DeviceModel) return false

        if (devId != other.devId) return false
        if (_isBtServerAvailable != other._isBtServerAvailable) return false
        if (_isShareV2Available != other._isShareV2Available) return false
        if (listeners != other.listeners) return false
        if (device != other.device) return false
        if (wareDevice != other.wareDevice) return false
        if (loginSession != other.loginSession) return false
        if (mDevMarkName != other.mDevMarkName) return false
        if (_devSn != other._devSn) return false

        return true
    }

    override fun hashCode(): Int {
        var result = devId.hashCode()
        result = 31 * result + _isBtServerAvailable.hashCode()
        result = 31 * result + _isShareV2Available.hashCode()
        result = 31 * result + (listeners?.hashCode() ?: 0)
        result = 31 * result + (device?.hashCode() ?: 0)
        result = 31 * result + (wareDevice?.hashCode() ?: 0)
        result = 31 * result + (loginSession?.hashCode() ?: 0)
        result = 31 * result + (mDevMarkName?.hashCode() ?: 0)
        result = 31 * result + (_devSn?.hashCode() ?: 0)
        return result
    }

    fun setMarkName(newName: String) {
        mDevMarkName = newName
    }

    fun setDeviceName(newName: String) {
        mDeviceName = newName
    }


}
