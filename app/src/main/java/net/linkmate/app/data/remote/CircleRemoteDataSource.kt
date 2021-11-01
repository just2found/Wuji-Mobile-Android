package net.linkmate.app.data.remote

import android.text.TextUtils
import com.google.gson.Gson
import net.linkmate.app.base.MyApplication
import net.linkmate.app.data.model.*
import net.linkmate.app.util.JsonUtil
import net.sdvn.cmapi.CMAPI
import net.sdvn.common.internet.core.GsonBaseProtocol
import net.sdvn.common.internet.core.HttpLoader
import net.sdvn.common.internet.core.V2AgApiHttpLoader
import net.sdvn.common.internet.listener.ResultListener
import net.sdvn.common.internet.utils.Utils
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.ConcurrentHashMap

/**圈子公共数据源
 * @author Raleigh.Luo
 * date：20/8/19 16
 * describe：
 */
class CircleRemoteDataSource {
    /**
     * 创建圈子
     */
    fun createCircle(modelid: String,//圈子网络类型ID
                     networkname: String,  //用户输入的网络名称
                     create_fee: String,//网络创建费用,APP传入值需与类型中的设置值校验一致
                     loaderStateListener: HttpLoader.HttpLoaderStateListener?
                     , listener: ResultListener<GsonBaseProtocol>) {
        val loader = object : V2AgApiHttpLoader(GsonBaseProtocol::class.java) {
            init {
                action = "createnetwork"
                bodyMap = ConcurrentHashMap()
                put("ticket", CMAPI.getInstance().baseInfo.ticket)
                put("modelid", modelid)
                put("networkname", networkname)
                put("mbpoint", if (TextUtils.isEmpty(create_fee)) 0f else create_fee.toFloat())
            }
        }
        loader.setHttpLoaderStateListener(loaderStateListener)
        loader.executor(listener)
    }

    /**
     * 修改圈子名称
     */
    fun alterCircleName(networkid: String, networkname: String, loaderStateListener: HttpLoader.HttpLoaderStateListener?
                        , listener: ResultListener<GsonBaseProtocol>) {
        val loader = object : V2AgApiHttpLoader(GsonBaseProtocol::class.java) {
            init {
                action = "modifynetworkname"
                bodyMap = ConcurrentHashMap()
                put("ticket", CMAPI.getInstance().baseInfo.ticket)
                put("networkid", networkid)
                put("networkname", networkname)
            }
        }
        loader.setHttpLoaderStateListener(loaderStateListener)
        loader.executor(listener)
    }

    /**
     * 获取圈子类型
     */
    fun getCircleTypes(loaderStateListener: HttpLoader.HttpLoaderStateListener?
                       , listener: ResultListener<CircleType>) {
        val loader = object : V2AgApiHttpLoader(CircleType::class.java) {
            init {
                action = "getnetworkmodel"
                bodyMap = ConcurrentHashMap()
                put("ticket", CMAPI.getInstance().baseInfo.ticket)
                put("lang", Utils.getLanguage(MyApplication.getContext()))
            }
        }
        loader.setHttpLoaderStateListener(loaderStateListener)
        loader.executor(listener)
    }

    /**
     * 用户退出圈子（设备不退）
     */
    fun userExitCircle(networkid: String, loaderStateListener: HttpLoader.HttpLoaderStateListener?
                       , listener: ResultListener<GsonBaseProtocol>) {
        val loader = object : V2AgApiHttpLoader(GsonBaseProtocol::class.java) {
            init {
                action = "quitnet"
                bodyMap = ConcurrentHashMap()
                put("ticket", CMAPI.getInstance().baseInfo.ticket)
                put("networkid", networkid)
            }
        }
        loader.setHttpLoaderStateListener(loaderStateListener)
        loader.executor(listener)
    }

    /**
     * 获取圈子详情/分享码获取圈子详情
     */
    fun getCircleDetial(networkid: String? = null, shareCode: String?, loaderStateListener: HttpLoader.HttpLoaderStateListener?
                        , listener: ResultListener<CircleDetail>) {
        val loader = object : V2AgApiHttpLoader(CircleDetail::class.java) {
            init {
                action = "getnetworkinfo"
                bodyMap = ConcurrentHashMap()
                put("ticket", CMAPI.getInstance().baseInfo.ticket)
                networkid?.let {
                    put("networkid", networkid)
                }
                shareCode?.let {
                    put("sharecode", shareCode)
                }
                put("lang", Utils.getLanguage(MyApplication.getContext()))
            }
        }
        loader.setHttpLoaderStateListener(loaderStateListener)
        loader.executor(listener)
    }

    /**
     * 用户获取收费方式
     */
    fun getFee(feetype: String?, networkid: String, deviceid: String?, devicesn: String?, loaderStateListener: HttpLoader.HttpLoaderStateListener?
               , listener: ResultListener<CircleJoinWay>) {
        val loader = object : V2AgApiHttpLoader(CircleJoinWay::class.java) {
            init {
                action = "getfee"
                bodyMap = ConcurrentHashMap()
                put("ticket", CMAPI.getInstance().baseInfo.ticket)
                networkid?.let {
                    put("networkid", networkid)
                }
                put("feetype", feetype)
                deviceid?.let {
                    put("deviceid", deviceid)
                }
                devicesn?.let {
                    put("devicesn", devicesn)
                }


            }
        }
        loader.setHttpLoaderStateListener(loaderStateListener)
        loader.executor(listener)
    }

    /**
     * 用户订购收费方式
     */
    fun setFee(feetype: String, networkid: String, deviceid: String?, devicesn: String?, feeid: String?, mbpoint: Float, expire_renew: Boolean?, loaderStateListener: HttpLoader.HttpLoaderStateListener?
               , listener: ResultListener<GsonBaseProtocol>) {
        val loader = object : V2AgApiHttpLoader(GsonBaseProtocol::class.java) {
            init {
                action = "setfee"
                bodyMap = ConcurrentHashMap()
                put("ticket", CMAPI.getInstance().baseInfo.ticket)
                val params = HashMap<String, String>()
                networkid?.let {
                    params.put("networkid", networkid)
                }
                deviceid?.let {
                    params.put("deviceid", deviceid)
                }

                devicesn?.let {
                    params.put("devicesn", devicesn)
                }
                put("param", params)
                put("feetype", feetype)
                put("mbpoint", mbpoint)
                put("feetype", feetype)
                feeid?.let {
                    put("feeid", feeid)
                }
                put("mbpoint", mbpoint)
                //是否当前费用到后生效,如果为是则为到期续费,否则费用立即生效
                expire_renew?.let {
                    put("expire_renew", expire_renew)
                }

            }
        }
        loader.setHttpLoaderStateListener(loaderStateListener)
        loader.executor(listener)
    }

    /**
     * 加入圈子
     */
    fun joinCircle(networkid: String, shareCode: String, feeid: String?, mbpoint: Float?, invitemsgid: String?, loaderStateListener: HttpLoader.HttpLoaderStateListener?
                   , listener: ResultListener<GsonBaseProtocol>) {
        val loader = object : V2AgApiHttpLoader(GsonBaseProtocol::class.java) {
            init {
                action = "joinnet"
                bodyMap = ConcurrentHashMap()
                put("ticket", CMAPI.getInstance().baseInfo.ticket)
                put("networkid", networkid)
                put("sharecode", shareCode)
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
     * 订阅圈子主EN
     */
    fun subscribeMainEN(networkid: String,  deviceid: String, shareCode: String, loaderStateListener: HttpLoader.HttpLoaderStateListener?
                   , listener: ResultListener<GsonBaseProtocol>) {
        val loader = object : V2AgApiHttpLoader(GsonBaseProtocol::class.java) {
            init {
                action = "subnetendevice"
                bodyMap = ConcurrentHashMap()
                put("ticket", CMAPI.getInstance().baseInfo.ticket)
                put("networkid", networkid)
                put("sharecode", shareCode)
                put("deviceid", deviceid)
            }
        }
        loader.setHttpLoaderStateListener(loaderStateListener)
        loader.executor(listener)
    }



    /**
     * 管理员设置收费
     */
    fun setCharge(networkid: String?, deviceid: String?, feetype: String, basic_feeid: String?, enable: Boolean?, value: Float?, vadd_feeid: String?, loaderStateListener: HttpLoader.HttpLoaderStateListener?
                  , listener: ResultListener<GsonBaseProtocol>) {
        val loader = object : V2AgApiHttpLoader(GsonBaseProtocol::class.java) {
            init {
                action = "setcharge"
                bodyMap = ConcurrentHashMap()
                put("ticket", CMAPI.getInstance().baseInfo.ticket)
                networkid?.let {
                    put("networkid", networkid)
                }
                deviceid?.let {
                    put("deviceid", deviceid)
                }
                put("feetype", feetype)
                val params = ArrayList<Map<String, Any>>()
                val json = HashMap<String, Any>()
                enable?.let {
                    json.put("enable", enable)
                }
                value?.let {
                    json.put("value", value)
                }
                vadd_feeid?.let {
                    json.put("vadd_feeid", vadd_feeid)
                }
                basic_feeid?.let {
                    json.put("basic_feeid", basic_feeid)
                }
                params.add(json)
                bodyMap.put("fees", params)
            }
        }
        loader.setHttpLoaderStateListener(loaderStateListener)
        loader.executor(listener)
    }

    /**
     * 用户获取已付费资源
     */
    fun getFeeRecords(networkid: String, feetype: String?, deviceid: String?, status: String?, loaderStateListener: HttpLoader.HttpLoaderStateListener?
                      , listener: ResultListener<CircleFeeRecords>) {
        val loader = object : V2AgApiHttpLoader(CircleFeeRecords::class.java) {
            init {
                action = "getuserres"
                bodyMap = ConcurrentHashMap()
                put("ticket", CMAPI.getInstance().baseInfo.ticket)
                networkid?.let {
                    put("networkid", networkid)
                }
                feetype?.let {
                    put("feetype", feetype)
                }
                deviceid?.let {
                    put("deviceid", deviceid)
                }
                status?.let {////状态,选填 1:生效中 0:未生效 -1:已失效
                    put("status", status)
                }

            }
        }
        loader.setHttpLoaderStateListener(loaderStateListener)
        loader.executor(listener)
    }


    /**
     * 管理员获取收费
     */
    fun getManagerFees(networkid: String?, feetype: String?, deviceid: String?, loaderStateListener: HttpLoader.HttpLoaderStateListener?
                       , listener: ResultListener<CircleManagerFees>) {
        val loader = object : V2AgApiHttpLoader(CircleManagerFees::class.java) {
            init {
                action = "getcharge"
                bodyMap = ConcurrentHashMap()
                put("ticket", CMAPI.getInstance().baseInfo.ticket)
                networkid?.let {
                    put("networkid", networkid)
                }
                feetype?.let {
                    put("feetype", feetype)
                }
                deviceid?.let {
                    put("deviceid", deviceid)
                }
            }
        }
        loader.setHttpLoaderStateListener(loaderStateListener)
        loader.executor(listener)
    }


    /**
     * 管理员获取设备分成(new)
     */
    fun getShare(networkid: String?, deviceid: String?, loaderStateListener: HttpLoader.HttpLoaderStateListener?
                 , listener: ResultListener<CircleShareFees>) {
        val loader = object : V2AgApiHttpLoader(CircleShareFees::class.java) {
            init {
                action = "getshare"
                bodyMap = ConcurrentHashMap()
                put("ticket", CMAPI.getInstance().baseInfo.ticket)
                networkid?.let {
                    put("networkid", networkid)
                }
                deviceid?.let {
                    put("deviceid", deviceid)
                }
            }
        }
        loader.setHttpLoaderStateListener(loaderStateListener)
        loader.executor(listener)
    }

    /**
     * 管理员获取设备分成(new)
     * @param owner_share    //拥有者分成,范围0~owner_max
     */
    fun setShare(networkid: String?, deviceid: String?, owner_share: Float, loaderStateListener: HttpLoader.HttpLoaderStateListener?
                 , listener: ResultListener<GsonBaseProtocol>) {
        val loader = object : V2AgApiHttpLoader(GsonBaseProtocol::class.java) {
            init {
                action = "setshare"
                bodyMap = ConcurrentHashMap()
                put("ticket", CMAPI.getInstance().baseInfo.ticket)
                networkid?.let {
                    put("networkid", networkid)
                }
                deviceid?.let {
                    put("deviceid", deviceid)
                }
                put("owner_share", owner_share)
            }
        }
        loader.setHttpLoaderStateListener(loaderStateListener)
        loader.executor(listener)
    }

}