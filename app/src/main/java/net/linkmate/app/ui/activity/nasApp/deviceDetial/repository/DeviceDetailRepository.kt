package net.linkmate.app.ui.activity.nasApp.deviceDetial.repository

import androidx.arch.core.util.Function
import net.linkmate.app.base.MyOkHttpListener
import net.linkmate.app.util.business.ShareUtil
import net.sdvn.common.internet.core.GsonBaseProtocol
import net.sdvn.common.internet.core.HttpLoader
import net.sdvn.common.internet.listener.ResultListener
import net.sdvn.common.internet.protocol.ShareCode

/**
 * @author Raleigh.Luo
 * date：20/7/25 17
 * describe：
 */
class DeviceDetailRepository {
    /**
     * 获取设备分享码
     */
    fun getDeviceShareCode(deviceid:String,backCall:Function<ShareCode,Void>){
        //获取分享吗
        ShareUtil.getDeviceShareCode(deviceid, null,
                object : MyOkHttpListener<ShareCode?>() {
                    override fun success(tag: Any?, data: ShareCode?) {
                        backCall.apply(data)
                    }
                })
    }

    /**
     * 启动／关闭分享设备功能
     */
    fun savedEnableShareState(deviceid:String,isChecked: Boolean, stateListener: HttpLoader.HttpLoaderStateListener,
                              backCall:Function<Boolean,Void?>){
        ShareUtil.savedEnableShareState(deviceid, isChecked,
                stateListener, object :ResultListener<GsonBaseProtocol?> {
            override fun success(tag: Any?, data: GsonBaseProtocol?) {
                backCall.apply(true)
            }

            override fun error(tag: Any?, baseProtocol: GsonBaseProtocol?) {
                backCall.apply(false)
            }

        })
    }

    /**
     * 开启／关闭 是否分享需要验证
     */
    fun savedScanConfirmState(deviceid:String,isChecked: Boolean,stateListener: HttpLoader.HttpLoaderStateListener,
                              backCall:Function<Boolean,Void?>){
        ShareUtil.savedScanConfirmState(deviceid, isChecked,
                stateListener, object : ResultListener<GsonBaseProtocol?> {
            override fun success(tag: Any?, data: GsonBaseProtocol?) {
                backCall.apply(true)

            }

            override fun error(tag: Any?, baseProtocol: GsonBaseProtocol) {
                backCall.apply(false)
            }
        })
    }


}