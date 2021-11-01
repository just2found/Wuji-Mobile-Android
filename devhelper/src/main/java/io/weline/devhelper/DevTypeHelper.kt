package io.weline.devhelper

import net.sdvn.cmapi.global.Constants
import net.sdvn.cmapi.util.CommonUtils

object DevTypeHelper {
    @kotlin.jvm.JvmStatic
    fun isIzzbieOne(devClass: Int): Boolean {
        return CommonUtils.getManufacturer(devClass) == Manufacturer.Izzbie
                && CommonUtils.getDeviceType(devClass) == Constants.DT_SN;
    }

    @JvmStatic
    fun isNas(devClass: Int): Boolean {
        return devClass > 0 &&
                (CommonUtils.getManufacturer(devClass) == 0 && devClass == Constants.OT_ONESPACE)
                || CommonUtils.getDeviceType(devClass) == Constants.DT_NAS
                || isM8(devClass)
                || isAndroidTV(devClass)
                || isWebApi(devClass)
    }

    @JvmStatic
    fun isNasByFeature(devFeature: Int): Boolean {
        return devFeature > 0 && (devFeature and Constants.DF_FUNCTION_NAS != 0)
    }

    @JvmStatic
    fun isSupportRemoteCtrlFeature(devFeature: Int): Boolean {
        return devFeature > 0 && (devFeature and Constants.DF_FUNCTION_REMOTE_COMMUNI != 0)
    }

    @JvmStatic
    fun isSupportSmartNodeCtrlFeature(devFeature: Int): Boolean {
        return devFeature > 0 &&
                (devFeature and Constants.DF_ACCESS_INTERNET != 0)
                || (devFeature and Constants.DF_ACCESS_SUBNET != 0)
    }

    @JvmStatic
    fun isSupportRemoteDoUpdateFeature(devFeature: Int): Boolean {
        return devFeature > 0 &&
                (devFeature and Constants.DF_FUNCTION_REMOTE_DO_UPDATE != 0)
    }

    @JvmStatic
    fun isSupportRemoteRebootFeature(devFeature: Int): Boolean {
        return devFeature > 0 &&
                (devFeature and Constants.DF_FUNCTION_REMOTE_REBOOT != 0)
    }

    @JvmStatic
    fun isWebApi(devClass: Int): Boolean {
        return devClass == 137800
                || devClass == 137801
    }

    @JvmStatic
    fun isAndroidTV(devClass: Int): Boolean {
        return devClass == 7274753//开发测试Test(111) : Android(4) : 1
                || devClass == 7536897// 零刻BeeLink(115) : Android(4) : 1
                || (CommonUtils.getManufacturer(devClass) == Manufacturer.E
                && CommonUtils.getDeviceType(devClass) == Constants.DT_ANDROID
                && CommonUtils.getDeviceSubType(devClass) >= DevSubType.E_A9)
                || ((BuildConfig.DEBUG || BuildConfig.isTestOnly) && devClass == 6553729)
//                || (BuildConfig.DEBUG && devClass == 137796)
    }

    @JvmStatic
    fun isM8(devClass: Int): Boolean {
        return devClass == 137793
                || devClass == 137796
                || devClass == 137798
                || devClass == 137794
                || devClass == 7477825//E1
    }

    @JvmStatic
    fun isOneOSNas(devClass: Int): Boolean {
        return devClass > 0 &&
                CommonUtils.getManufacturer(devClass) == 0
                && devClass == Constants.OT_ONESPACE ||
                CommonUtils.getDeviceType(devClass) == Constants.DT_NAS

    }

    @JvmStatic
    fun isOneOSWithAccessByMnet(devClass: Int): Boolean {
        return devClass > 0 && (CommonUtils.getDeviceType(devClass) == Constants.DT_NAS
                && (devClass == 203137 || devClass == 203138 || devClass == 6953345
                || devClass == 72065 || devClass == 72066))
    }


    @JvmStatic
    fun isHasUserData(devClass: Int): Boolean {
        return isOneOSNas(devClass) || isM8(devClass) || isWebApi(devClass)
    }

    fun isM3(devClass: Int?): Boolean {
        return devClass == 203137 || devClass == 203138
    }
}

