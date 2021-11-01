package net.linkmate.app.data.remote

import net.linkmate.app.base.MyOkHttpListener
import net.sdvn.cmapi.CMAPI
import net.sdvn.common.internet.core.GsonBaseProtocol
import net.sdvn.common.internet.core.HttpLoader
import net.sdvn.common.internet.core.V2AgApiHttpLoader
import net.sdvn.common.internet.listener.ResultListener
import net.sdvn.common.internet.protocol.ShareCode
import java.util.concurrent.ConcurrentHashMap

/**
 * @author Raleigh.Luo
 * date：20/8/19 13
 * describe：
 */
class CircleShareRemoteDataSource {
    /**
     * 获取网络分享码
     */
    fun getNetworkShareCode(networkid: String, loaderStateListener: HttpLoader.HttpLoaderStateListener?
                            , listener: MyOkHttpListener<ShareCode>) {
        val loader = object : V2AgApiHttpLoader(ShareCode::class.java) {
            init {
                action = "applysharenetwork"
                bodyMap = ConcurrentHashMap()
                put("ticket", CMAPI.getInstance().baseInfo.ticket)
                put("networkid", networkid)
            }
        }
        loader.setHttpLoaderStateListener(loaderStateListener)
        loader.executor(listener)
    }

    /**
     * 设置网络加入确认
     */
    fun setNetworkConfirm(networkid: String, provide_confirm: Boolean?, join_confirm: Boolean?, loaderStateListener: HttpLoader.HttpLoaderStateListener?
                          , listener: MyOkHttpListener<GsonBaseProtocol>) {
        val loader = object : V2AgApiHttpLoader(GsonBaseProtocol::class.java) {
            init {
                action = "setnetworkconfirm"
                bodyMap = ConcurrentHashMap()
                put("ticket", CMAPI.getInstance().baseInfo.ticket)
                put("networkid", networkid)
                provide_confirm?.let {
                    put("provide_confirm", provide_confirm)
                }
                join_confirm?.let {
                    put("join_confirm", join_confirm)
                }
            }
        }
        loader.setHttpLoaderStateListener(loaderStateListener)
        loader.executor(listener)
    }
}