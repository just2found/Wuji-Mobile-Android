package net.linkmate.app.ui.activity.nasApp.deviceDetial.repository

import androidx.arch.core.util.Function
import net.linkmate.app.base.MyOkHttpListener
import net.linkmate.app.bean.DeviceBean
import net.linkmate.app.util.business.SmartNodeUtil
import net.sdvn.common.internet.core.GsonBaseProtocol
import net.sdvn.common.internet.core.HttpLoader
import net.sdvn.common.internet.protocol.SubnetList
import net.sdvn.common.internet.protocol.entity.SubnetEntity

/**
 * @author Raleigh.Luo
 * date：20/7/29 19
 * describe：
 */
class DeviceNodeConfigRepository {
    fun getSubnet(bean: DeviceBean, listener: HttpLoader.HttpLoaderStateListener, callback:Function<List<SubnetEntity> ,Void?>){
        SmartNodeUtil.getSubnet(bean, listener,
                object : MyOkHttpListener<SubnetList?>() {
                    override fun error(tag: Any?, baseProtocol: GsonBaseProtocol) {
                        super.error(tag, baseProtocol)
                    }

                    override fun success(tag: Any?, subnetList: SubnetList?) {
                        if (subnetList != null) {
                            callback.apply(subnetList.subnet)
                        }
                    }
                })
    }


    fun isAccessInternet(device: DeviceBean):Boolean{
        return SmartNodeUtil.isAccessInternet(device)
    }
    fun isAccessSubnet(device: DeviceBean):Boolean{
        return SmartNodeUtil.isAccessSubnet( device)
    }
    fun isDNSEnable(device: DeviceBean):Boolean{
        var isEnable=false
        val mDns: String? =  device.getDns()
        if (!mDns.isNullOrEmpty()) {
            val split = mDns.split(",").toTypedArray()
            if (split.size > 0) {
                if (isAccessInternet(device)) {
                    isEnable = true
                }
            }
        }
        return isEnable
    }
    /**
     * 提交
     */
    fun submit(listener: HttpLoader.HttpLoaderStateListener, device: DeviceBean,dns: String,isAccessInternet:Boolean,isAccessSubnet:Boolean,
               isSubnetChange:Boolean, subnet: List<SubnetEntity>,finishCallback:Function<Boolean,Void?>) {
        val nextToUpdateSubnet:Function<Boolean, Void?> = Function {
            if(it&&isAccessSubnet&&isSubnetChange){//子网开启时才更新，关闭不更新
                updateSubnet(device,subnet,listener,finishCallback)
            }else{
                finishCallback.apply(it)
            }
            null
        }
        if (isAccessInternet(device) != isAccessInternet || isAccessSubnet(device) != isAccessSubnet) {
            SmartNodeUtil.submitAccessFlag(device, isAccessInternet, isAccessSubnet, listener, object : MyOkHttpListener<GsonBaseProtocol>() {
                override fun success(tag: Any?, data: GsonBaseProtocol) {
                    updateDNS(listener, device ,dns,isAccessInternet,isAccessSubnet,nextToUpdateSubnet)
                }

                override fun error(tag: Any?, baseProtocol: GsonBaseProtocol?) {
                    finishCallback.apply(false)
                }
            })
        } else {
           updateDNS(listener, device,dns,isAccessInternet,isAccessSubnet,nextToUpdateSubnet)
        }
    }
    /**
     * 上传dns
     */
    private fun updateDNS(listener: HttpLoader.HttpLoaderStateListener, device: DeviceBean,dns:String,isAccessInternet:Boolean,isAccessSubnet:Boolean,callback: Function<Boolean, Void?>){
        if (isAccessInternet&& device.getDns() != dns) {
            SmartNodeUtil.submitDns(device, dns, listener, object : MyOkHttpListener<GsonBaseProtocol>() {
                override fun success(tag: Any?, data: GsonBaseProtocol) {
                    callback.apply(true)
                }

                override fun error(tag: Any?, baseProtocol: GsonBaseProtocol?) {
                    super.error(tag, baseProtocol)
                    callback.apply(false)
                }
            })
        }else{
            callback.apply(true)
        }
    }
    /**
     * 上传子网
     */
    private fun updateSubnet(bean: DeviceBean, subnet: List<SubnetEntity>,listener: HttpLoader.HttpLoaderStateListener?
                     ,callback:Function<Boolean,Void?>){
        SmartNodeUtil.submitSubnet(bean, subnet, listener, object : MyOkHttpListener<GsonBaseProtocol>() {
            override fun success(tag: Any?, data: GsonBaseProtocol) {
                callback.apply(true)
            }
        })
    }
}