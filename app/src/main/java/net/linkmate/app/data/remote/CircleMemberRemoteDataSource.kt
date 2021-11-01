package net.linkmate.app.data.remote

import android.util.Log
import net.linkmate.app.data.model.CircleMember
import net.sdvn.cmapi.CMAPI
import net.sdvn.common.internet.core.GsonBaseProtocol
import net.sdvn.common.internet.core.HttpLoader
import net.sdvn.common.internet.core.V2AgApiHttpLoader
import net.sdvn.common.internet.listener.ResultListener
import org.json.JSONArray
import java.util.concurrent.ConcurrentHashMap

/**
 * @author Raleigh.Luo
 * date：20/8/18 19
 * describe：
 */
class CircleMemberRemoteDataSource {
    /**
     * 获取圈子用户
     */
    fun getMembers(networkid:String,loaderStateListener: HttpLoader.HttpLoaderStateListener?
                   , listener: ResultListener<CircleMember>){
        val loader=object : V2AgApiHttpLoader(CircleMember::class.java){
            init {
                action="getmembers"
                bodyMap = ConcurrentHashMap()
                put("ticket", CMAPI.getInstance().baseInfo.ticket)
                put("networkid", networkid)
                ///用户在网络中的状态 0-正常 1-待确认 2-审核不通过 3-系统锁定 4-owner锁定 5-已过期
                put("status", 0)
            }
        }
        loader.setHttpLoaderStateListener(loaderStateListener)
        loader.executor(listener)
    }

    /**
     * 删除用户
     */
    fun deleteMembers(networkid:String,removeuserids:List<String>,loaderStateListener: HttpLoader.HttpLoaderStateListener?
                   , listener: ResultListener<GsonBaseProtocol>){
        val loader=object : V2AgApiHttpLoader(GsonBaseProtocol::class.java){
            init {
                action="removemembers"
                bodyMap = ConcurrentHashMap()
                put("ticket", CMAPI.getInstance().baseInfo.ticket)
                put("networkid", networkid)
                bodyMap.put("members", removeuserids)
            }
        }
        loader.setHttpLoaderStateListener(loaderStateListener)
        loader.executor(listener)
    }
    /**
     * 改变网络成员级别
     */
    fun gradeMember(networkid:String, userid:String,uselevel:Int,loaderStateListener: HttpLoader.HttpLoaderStateListener?
                      , listener: ResultListener<GsonBaseProtocol>){
        val loader=object : V2AgApiHttpLoader(GsonBaseProtocol::class.java){
            init {
                //要更改的级别 1-manager 2-user
                action="grademember"
                bodyMap = ConcurrentHashMap()
                put("ticket", CMAPI.getInstance().baseInfo.ticket)
                put("networkid", networkid)
                put("memberid", userid)
                put("uselevel", uselevel)
            }
        }
        loader.setHttpLoaderStateListener(loaderStateListener)
        loader.executor(listener)
    }

}