package net.linkmate.app.data.remote

import net.linkmate.app.data.model.CircleDevice
import net.sdvn.cmapi.CMAPI
import net.sdvn.common.internet.core.GsonBaseProtocol
import net.sdvn.common.internet.core.HttpLoader
import net.sdvn.common.internet.core.V2AgApiHttpLoader
import net.sdvn.common.internet.listener.ResultListener
import java.util.concurrent.ConcurrentHashMap

/**
 * @author Raleigh.Luo
 * date：20/10/14 15
 * describe：
 */
class CircleDeviceRemoteDataSource {
    /**
     * 申请成为合作伙伴
     */
    fun applyToPartner(networkid: String, deviceid: String, join_fee: String, loaderStateListener: HttpLoader.HttpLoaderStateListener?
                       , listener: ResultListener<GsonBaseProtocol>) {
        val loader = object : V2AgApiHttpLoader(GsonBaseProtocol::class.java) {
            init {
                action = "applyprovide"
                bodyMap = ConcurrentHashMap()
                put("ticket", CMAPI.getInstance().baseInfo.ticket)
                put("networkid", networkid)
                put("deviceid", deviceid)
                put("join_fee", join_fee)
            }
        }
        loader.setHttpLoaderStateListener(loaderStateListener)
        loader.executor(listener)
    }


    /**
     * 获取EN设备
     */
    fun getENDevices(networkid: String, loaderStateListener: HttpLoader.HttpLoaderStateListener? = null
                     , listener: ResultListener<CircleDevice>) {
        val loader = object : V2AgApiHttpLoader(CircleDevice::class.java) {
            init {
                action = "getnetworkprovide"
                bodyMap = ConcurrentHashMap()
                put("ticket", CMAPI.getInstance().baseInfo.ticket)
                put("networkid", networkid)
            }
        }
        loader.setHttpLoaderStateListener(loaderStateListener)
        loader.executor(listener)
    }

    /**
     * 启用或禁用EN设备
     */
    fun enableENDevice(networkid: String, deviceid: String, enable:Boolean, loaderStateListener: HttpLoader.HttpLoaderStateListener?
                       , listener: ResultListener<GsonBaseProtocol>) {
        val loader = object : V2AgApiHttpLoader(GsonBaseProtocol::class.java) {
            init {
                action = "ablenetworkdevice"
                bodyMap = ConcurrentHashMap()
                put("ticket", CMAPI.getInstance().baseInfo.ticket)
                put("networkid", networkid)
                put("deviceid", deviceid)
                put("able", enable) //禁用为false,启用为true
            }
        }
        loader.setHttpLoaderStateListener(loaderStateListener)
        loader.executor(listener)
    }

    /**
     * 取消合作
     */
    fun cancelCooperation(networkid: String, deviceid: String, loaderStateListener: HttpLoader.HttpLoaderStateListener?
                          , listener: ResultListener<GsonBaseProtocol>) {
        val loader = object : V2AgApiHttpLoader(GsonBaseProtocol::class.java) {
            init {
                action = "quitprovide"
                bodyMap = ConcurrentHashMap()
                put("ticket", CMAPI.getInstance().baseInfo.ticket)
                put("networkid", networkid)
                put("deviceid", deviceid)
            }
        }
        loader.setHttpLoaderStateListener(loaderStateListener)
        loader.executor(listener)
    }

    /**
     * 获取我的设备
     */
    fun getDevices(networkid: String, loaderStateListener: HttpLoader.HttpLoaderStateListener? = null
                   , listener: ResultListener<CircleDevice>) {
        val loader = object : V2AgApiHttpLoader(CircleDevice::class.java) {
            init {
                action = "mynetworkdevices"
                bodyMap = ConcurrentHashMap()
                put("ticket", CMAPI.getInstance().baseInfo.ticket)
                put("networkid", networkid)
            }
        }
        loader.setHttpLoaderStateListener(loaderStateListener)
        loader.executor(listener)
    }

    /**
     * 将我的设备加入到圈子
     */
    fun deviceJoinCircle(networkid: String, deviceid: String, loaderStateListener: HttpLoader.HttpLoaderStateListener? = null
                         , listener: ResultListener<GsonBaseProtocol>) {
        val loader = object : V2AgApiHttpLoader(GsonBaseProtocol::class.java) {
            init {
                action = "switchnetwork"
                bodyMap = ConcurrentHashMap()
                put("ticket", CMAPI.getInstance().baseInfo.ticket)
                put("networkid", networkid)
                put("deviceid", deviceid)
            }
        }
        loader.setHttpLoaderStateListener(loaderStateListener)
        loader.executor(listener)
    }

    /**
     * 将我的设备设置为EN服务器
     */
    fun setENServer(networkid: String, deviceid: String, feeid: String?, mbpoint: Float?, invitemsgid: String?, loaderStateListener: HttpLoader.HttpLoaderStateListener? = null
                    , listener: ResultListener<GsonBaseProtocol>) {
        val loader = object : V2AgApiHttpLoader(GsonBaseProtocol::class.java) {
            init {
                action = "applyprovide"
                bodyMap = ConcurrentHashMap()
                put("ticket", CMAPI.getInstance().baseInfo.ticket)
                put("networkid", networkid)
                put("deviceid", deviceid)
                feeid?.let {
                    put("feeid", feeid)
                }
                mbpoint?.let {
                    put("mbpoint", mbpoint)
                }
                invitemsgid?.let {
                    put("invitemsgid", invitemsgid)
                }
            }
        }
        loader.setHttpLoaderStateListener(loaderStateListener)
        loader.executor(listener)
    }

    /**
     * 将我的设备设置为主EN服务器
     * @param srvmain 是否是主EN服务器
     */
    fun setMainENServer(networkid: String, deviceid: String, srvmain:Boolean, loaderStateListener: HttpLoader.HttpLoaderStateListener? = null
                        , listener: ResultListener<GsonBaseProtocol>) {
        val loader = object : V2AgApiHttpLoader(GsonBaseProtocol::class.java) {
            init {
                action = "setprovide"
                bodyMap = ConcurrentHashMap()
                put("ticket", CMAPI.getInstance().baseInfo.ticket)
                put("networkid", networkid)
                put("deviceid", deviceid)
                put("srvmain", srvmain)
            }
        }
        loader.setHttpLoaderStateListener(loaderStateListener)
        loader.executor(listener)
    }

}