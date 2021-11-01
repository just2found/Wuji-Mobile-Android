package net.linkmate.app.repository

import androidx.lifecycle.LiveData
import net.linkmate.app.R
import net.linkmate.app.base.MyApplication
import net.linkmate.app.base.MyOkHttpListener
import net.linkmate.app.data.model.*
import net.linkmate.app.data.remote.CircleRemoteDataSource
import net.linkmate.app.util.ToastUtils
import net.sdvn.cmapi.CMAPI
import net.sdvn.common.internet.core.GsonBaseProtocol
import net.sdvn.common.internet.core.HttpLoader
import net.sdvn.common.internet.core.V2AgApiHttpLoader
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean

/** 圈子公共仓库
 * @author Raleigh.Luo
 * date：20/8/19 16
 * describe：
 */
class CircleRepository {
    private val remoteDataSource = CircleRemoteDataSource()

    /**
     * 获取圈子类型
     */
    fun getCircleTypes(loaderStateListener: HttpLoader.HttpLoaderStateListener? = null): LiveData<List<CircleType.Type>> {
        return object : LiveData<List<CircleType.Type>>() {
            val started = AtomicBoolean(false)
            override fun onActive() {
                super.onActive()
                if (started.compareAndSet(false, true)) {
                    remoteDataSource.getCircleTypes(loaderStateListener, object : MyOkHttpListener<CircleType>() {
                        override fun success(tag: Any?, data: CircleType?) {
                            postValue(data?.data?.list)
                        }
                    })
                }
            }
        }

    }

    /**
     * 创建圈子
     */
    fun createCircle(modelid: String,//圈子网络类型ID
                     networkname: String,  //用户输入的网络名称
                     create_fee: String,//网络创建费用,APP传入值需与类型中的设置值校验一致
                     loaderStateListener: HttpLoader.HttpLoaderStateListener? = null): LiveData<Boolean> {
        return object : LiveData<Boolean>() {
            val started = AtomicBoolean(false)
            override fun onActive() {
                super.onActive()
                if (started.compareAndSet(false, true)) {
                    remoteDataSource.createCircle(modelid, networkname, create_fee,
                            loaderStateListener, object : MyOkHttpListener<GsonBaseProtocol>() {
                        override fun success(tag: Any?, data: GsonBaseProtocol?) {
                            postValue(true)
                        }

                        override fun error(tag: Any?, baseProtocol: GsonBaseProtocol?) {
                            super.error(tag, baseProtocol)
                            postValue(false)
                        }
                    })
                }
            }
        }

    }

    /**
     * 修改圈子名称
     */
    fun alterCircleName(networkid: String, networkname: String, loaderStateListener: HttpLoader.HttpLoaderStateListener? = null): LiveData<Boolean> {
        return object : LiveData<Boolean>() {
            val started = AtomicBoolean(false)
            override fun onActive() {
                super.onActive()
                if (started.compareAndSet(false, true)) {
                    remoteDataSource.alterCircleName(networkid, networkname,
                            loaderStateListener, object : MyOkHttpListener<GsonBaseProtocol>() {
                        override fun success(tag: Any?, data: GsonBaseProtocol?) {
                            postValue(true)
                        }

                        override fun error(tag: Any?, baseProtocol: GsonBaseProtocol?) {
                            super.error(tag, baseProtocol)
                            postValue(false)
                        }
                    })
                }
            }
        }
    }

    /**
     * 用户退出圈子（设备不退）
     */
    fun userExitCircle(networkid: String, loaderStateListener: HttpLoader.HttpLoaderStateListener? = null): LiveData<Boolean> {
        return object : LiveData<Boolean>() {
            val started = AtomicBoolean(false)
            override fun onActive() {
                super.onActive()
                if (started.compareAndSet(false, true)) {
                    remoteDataSource.userExitCircle(networkid, loaderStateListener, object : MyOkHttpListener<GsonBaseProtocol>() {
                        override fun success(tag: Any?, data: GsonBaseProtocol?) {
                            postValue(true)
                        }
                    })
                }
            }
        }

    }

    /**
     * 获取圈子详情
     * @param isByShareCode 是否是通过输入分享吗 非扫一扫
     */
    fun getCircleDetial(networkid: String? = null, shareCode: String? = null, loaderStateListener: HttpLoader.HttpLoaderStateListener? = null): LiveData<CircleDetail.Circle> {
        return object : LiveData<CircleDetail.Circle>() {
            val started = AtomicBoolean(false)
            override fun onActive() {
                super.onActive()
                if (started.compareAndSet(false, true)) {
                    remoteDataSource.getCircleDetial(networkid, shareCode, loaderStateListener, object : MyOkHttpListener<CircleDetail>() {
                        override fun success(tag: Any?, data: CircleDetail?) {
                            postValue(data?.data)
                        }
                    })
                }
            }
        }

    }

    /**
     * 获取圈子用户获取收费方式
     */
    fun getFee(networkid: String, feetype: String? = null, loaderStateListener: HttpLoader.HttpLoaderStateListener? = null,
               deviceid: String? = null, devicesn: String? = null): LiveData<List<CircleJoinWay.Fee>> {
        return object : LiveData<List<CircleJoinWay.Fee>>() {
            val started = AtomicBoolean(false)
            override fun onActive() {
                super.onActive()
                if (started.compareAndSet(false, true)) {
                    remoteDataSource.getFee(feetype, networkid, deviceid, devicesn, loaderStateListener, object : MyOkHttpListener<CircleJoinWay>() {
                        override fun success(tag: Any?, data: CircleJoinWay?) {
                            data?.data?.list?.let {
                                val share = it.get(0).owner_share
                                if (it.size > 0) {
                                    var isEmpty = true
                                    it.get(0).fees?.let {
                                        if (it.size > 0) {
                                            it.forEach {
                                                it.owner_share = share
                                            }
                                            postValue(it)
                                            isEmpty = false
                                        }
                                    }
                                    if (isEmpty) {
                                        //获取的数据为空，且可创建免费方式
                                        if (it.get(0).isfree ?: false) {//是空的，就创建一项 免费的
                                            val list = ArrayList<CircleJoinWay.Fee>()
                                            val fee = CircleJoinWay.Fee(title = MyApplication.getContext().getString(R.string.free_join_circle))
                                            fee.owner_share = it.get(0).owner_share
                                            list.add(fee)
                                            postValue(list)
                                        } else {
                                            postValue(null)
                                        }
                                    }

                                }
                            }
                        }
                    })
                }
            }
        }
    }

    /**
     * 获取圈子用户获取收费方式-返回对象不同
     */
    fun getFeeType(networkid: String, feetype: String? = null, loaderStateListener: HttpLoader.HttpLoaderStateListener? = null,
                   deviceid: String? = null, devicesn: String? = null): LiveData<CircleJoinWay.FeeType> {
        return object : LiveData<CircleJoinWay.FeeType>() {
            val started = AtomicBoolean(false)
            override fun onActive() {
                super.onActive()
                if (started.compareAndSet(false, true)) {
                    remoteDataSource.getFee(feetype, networkid, deviceid, devicesn, loaderStateListener, object : MyOkHttpListener<CircleJoinWay>() {
                        override fun success(tag: Any?, data: CircleJoinWay?) {
                            data?.data?.list?.let {
                                postValue(if (it.size > 0) it.get(0) else null)
                            } ?: let {
                                postValue(null)
                            }
                        }
                    })
                }
            }
        }
    }

    /**
     * 用户订购收费方式
     */
    fun setFee(networkid: String, feetype: String, feeid: String?, mbpoint: Float, loaderStateListener: HttpLoader.HttpLoaderStateListener? = null,
               deviceid: String? = null, devicesn: String? = null, expire_renew: Boolean? = null)
            : LiveData<Boolean> {
        return object : LiveData<Boolean>() {
            val started = AtomicBoolean(false)
            override fun onActive() {
                super.onActive()
                if (started.compareAndSet(false, true)) {
                    remoteDataSource.setFee(feetype, networkid, deviceid, devicesn, feeid, mbpoint, expire_renew, loaderStateListener, object : MyOkHttpListener<GsonBaseProtocol>() {
                        override fun success(tag: Any?, data: GsonBaseProtocol?) {
                            postValue(true)
                        }

                        override fun error(tag: Any?, baseProtocol: GsonBaseProtocol?) {
                            super.error(tag, baseProtocol)
                            postValue(false)
                        }
                    })
                }
            }
        }

    }

    /**
     * 加入圈子
     *
     */
    fun joinCircle(networkid: String, shareCode: String, feeid: String? = null, mbpoint: Float? = null, invitemsgid: String? = null, loaderStateListener: HttpLoader.HttpLoaderStateListener? = null): LiveData<Boolean> {
        return object : LiveData<Boolean>() {
            val started = AtomicBoolean(false)
            override fun onActive() {
                super.onActive()
                if (started.compareAndSet(false, true)) {
                    remoteDataSource.joinCircle(networkid, shareCode, feeid, mbpoint, invitemsgid, loaderStateListener, object : MyOkHttpListener<GsonBaseProtocol>() {
                        override fun success(tag: Any?, data: GsonBaseProtocol?) {
                            postValue(true)
                        }
                    })
                }
            }
        }

    }

    /**
     * 加入圈子
     *
     */
    fun subscribeMainEN(networkid: String, shareCode: String, deviceid: String, loaderStateListener: HttpLoader.HttpLoaderStateListener? = null): LiveData<Boolean> {
        return object : LiveData<Boolean>() {
            val started = AtomicBoolean(false)
            override fun onActive() {
                super.onActive()
                if (started.compareAndSet(false, true)) {
                    postValue(null)
                    remoteDataSource.subscribeMainEN(networkid, shareCode, deviceid, loaderStateListener, object : MyOkHttpListener<GsonBaseProtocol>() {
                        override fun success(tag: Any?, data: GsonBaseProtocol?) {
                            postValue(true)
                        }

                        override fun error(tag: Any?, baseProtocol: GsonBaseProtocol?) {
                            super.error(tag, baseProtocol)
                            postValue(false)
                        }
                    })
                }
            }
        }

    }


    /**
     * 修改圈子属性
     */
    fun setCharge(networkid: String?, deviceid: String? = null, feetype: String, basic_feeid: String?, enable: Boolean? = null, value: Float? = null, vadd_feeid: String? = null, loaderStateListener: HttpLoader.HttpLoaderStateListener? = null)
            : LiveData<Boolean> {
        return object : LiveData<Boolean>() {
            val started = AtomicBoolean(false)
            override fun onActive() {
                super.onActive()
                if (started.compareAndSet(false, true)) {
                    remoteDataSource.setCharge(networkid, deviceid, feetype, basic_feeid, enable, value, vadd_feeid, loaderStateListener, object : MyOkHttpListener<GsonBaseProtocol>() {
                        override fun success(tag: Any?, data: GsonBaseProtocol?) {
                            postValue(true)
                        }

                        override fun error(tag: Any?, baseProtocol: GsonBaseProtocol?) {
                            super.error(tag, baseProtocol)
                            postValue(false)
                        }
                    })
                }
            }
        }
    }

    /**
     * 用户获取已付费资源
     */
    fun getFeeRecords(networkid: String, feetype: String? = null, deviceid: String? = null, status: String? = null, loaderStateListener: HttpLoader.HttpLoaderStateListener? = null)
            : LiveData<List<CircleFeeRecords.Record>> {
        return object : LiveData<List<CircleFeeRecords.Record>>() {
            val started = AtomicBoolean(false)
            override fun onActive() {
                super.onActive()
                if (started.compareAndSet(false, true)) {
                    remoteDataSource.getFeeRecords(networkid, feetype, deviceid, status, loaderStateListener, object : MyOkHttpListener<CircleFeeRecords>() {
                        override fun success(tag: Any?, data: CircleFeeRecords?) {
                            postValue(data?.data?.list)
                        }
                    })
                }
            }
        }
    }

    /**
     * 管理员获取收费
     */
    fun getManagerFees(networkid: String? = null, feetype: String? = null, deviceid: String? = null, loaderStateListener: HttpLoader.HttpLoaderStateListener? = null)
            : LiveData<List<CircleManagerFees.Fee>> {
        return object : LiveData<List<CircleManagerFees.Fee>>() {
            val started = AtomicBoolean(false)
            override fun onActive() {
                super.onActive()
                if (started.compareAndSet(false, true)) {
                    remoteDataSource.getManagerFees(networkid, feetype, deviceid, loaderStateListener, object : MyOkHttpListener<CircleManagerFees>() {
                        override fun success(tag: Any?, data: CircleManagerFees?) {
                            val fees: ArrayList<CircleManagerFees.Fee> = ArrayList()
                            data?.data?.list?.forEach {
                                val type = it
                                it.fees?.forEach {
                                    it.feetype = type.feetype
                                    it.titleUpper = type.title
                                    it.owner_share = type.owner_share
                                    fees.add(it)
                                }
                            }
                            postValue(fees)
                        }
                    })
                }
            }
        }
    }

    /**
     * 管理员获取设备分成(new)
     */
    fun getShare(networkid: String? = null, deviceid: String? = null, loaderStateListener: HttpLoader.HttpLoaderStateListener? = null)
            : LiveData<List<CircleShareFees.Fee>> {
        return object : LiveData<List<CircleShareFees.Fee>>() {
            val started = AtomicBoolean(false)
            override fun onActive() {
                super.onActive()
                if (started.compareAndSet(false, true)) {
                    remoteDataSource.getShare(networkid, deviceid, loaderStateListener, object : MyOkHttpListener<CircleShareFees>() {
                        override fun success(tag: Any?, data: CircleShareFees?) {
                            postValue(data?.data?.list)
                        }
                    })
                }
            }
        }
    }

    /**
     * 管理员获取设备分成(new)
     * @param owner_share    //拥有者分成,范围0~owner_max
     */
    fun setShare(networkid: String? = null, deviceid: String? = null, owner_share: Float, loaderStateListener: HttpLoader.HttpLoaderStateListener? = null)
            : LiveData<Boolean> {
        return object : LiveData<Boolean>() {
            val started = AtomicBoolean(false)
            override fun onActive() {
                super.onActive()
                if (started.compareAndSet(false, true)) {
                    remoteDataSource.setShare(networkid, deviceid, owner_share, loaderStateListener, object : MyOkHttpListener<GsonBaseProtocol>() {
                        override fun success(tag: Any?, data: GsonBaseProtocol?) {
                            postValue(true)
                        }
                    })
                }
            }
        }
    }


    /**
     * 修改流量付费方式
     */
    fun alterFlowFeePayer(deviceid: String, chargetype: Int, loaderStateListener: HttpLoader.HttpLoaderStateListener? = null)
            : LiveData<Boolean> {
        return object : LiveData<Boolean>() {
            val started = AtomicBoolean(false)
            override fun onActive() {
                super.onActive()
                if (started.compareAndSet(false, true)) {
                    val loader = object : V2AgApiHttpLoader(GsonBaseProtocol::class.java) {
                        init {
                            action = "setdevicecharge"
                            bodyMap = ConcurrentHashMap()
                            put("ticket", CMAPI.getInstance().baseInfo.ticket)
                            put("chargetype", chargetype)
                            put("deviceid", deviceid)
                        }
                    }
                    loader.setHttpLoaderStateListener(loaderStateListener)
                    loader.executor(object : MyOkHttpListener<GsonBaseProtocol>() {
                        override fun success(tag: Any?, data: GsonBaseProtocol?) {
                            postValue(true)
                        }

                        override fun error(tag: Any?, baseProtocol: GsonBaseProtocol?) {
                            super.error(tag, baseProtocol)
                            postValue(false)
                        }
                    })
                }
            }
        }
    }

}