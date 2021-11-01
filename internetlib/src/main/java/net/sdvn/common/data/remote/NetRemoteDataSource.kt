package net.sdvn.common.data.remote

import com.google.gson.reflect.TypeToken
import net.sdvn.cmapi.CMAPI
import net.sdvn.common.data.model.CircleDevice
import net.sdvn.common.internet.core.HttpLoader
import net.sdvn.common.internet.core.InitParamsV2AgApiHttpLoader
import net.sdvn.common.internet.core.V2AgApiHttpLoader
import net.sdvn.common.internet.listener.ResultListener
import net.sdvn.common.internet.protocol.BindNetsInfo
import java.util.concurrent.ConcurrentHashMap

/**
 *  
 *
 *
 * Created by admin on 2020/10/18,00:44
 */
class NetRemoteDataSource {
    fun getBindNetsInfo(listener: ResultListener<BindNetsInfo>): HttpLoader {
        val v2AgApiHttpLoader: InitParamsV2AgApiHttpLoader = object : InitParamsV2AgApiHttpLoader(BindNetsInfo::class.java) {
            override fun initParams(vararg objs: Any) {
                action = "mynetworks"
                bodyMap = ConcurrentHashMap()
                put("ticket", CMAPI.getInstance().baseInfo.ticket)
            }
        }
        val type = object : TypeToken<BindNetsInfo>() {}.type
        v2AgApiHttpLoader.executor(type,listener)
        return v2AgApiHttpLoader
    }

    /**
     * 获取EN设备
     */
    fun getENDevices(networkid: String, listener: ResultListener<CircleDevice>): HttpLoader {
        val loader = object : V2AgApiHttpLoader(CircleDevice::class.java) {
            init {
                action = "getnetworkprovide"
                bodyMap = ConcurrentHashMap()
                put("ticket", CMAPI.getInstance().baseInfo.ticket)
                put("networkid", networkid)
            }
        }
        val type = object : TypeToken<CircleDevice>() {}.type
        loader.executor(networkid,type, listener)
        return loader
    }
}