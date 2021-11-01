package net.linkmate.app.repository

import androidx.lifecycle.LiveData
import net.linkmate.app.base.MyOkHttpListener
import net.linkmate.app.data.model.CircleDevice
import net.linkmate.app.data.remote.CircleDeviceRemoteDataSource
import net.sdvn.cmapi.CMAPI
import net.sdvn.common.internet.core.GsonBaseProtocol
import net.sdvn.common.internet.core.HttpLoader
import java.util.concurrent.atomic.AtomicBoolean

/** 圈子设备仓库
 * @author Raleigh.Luo
 * date：20/10/14 15
 * describe：
 */
class CircleDeviceRepository {
    private val remoteDataSource = CircleDeviceRemoteDataSource()
    fun applyToPartner(networkid: String, deviceid: String, join_fee: String, loaderStateListener: HttpLoader.HttpLoaderStateListener? = null)
            : LiveData<Boolean> {
        return object : LiveData<Boolean>() {
            val started = AtomicBoolean(false)
            override fun onActive() {
                super.onActive()
                if (started.compareAndSet(false, true)) {
                    remoteDataSource.applyToPartner(networkid, deviceid, join_fee, loaderStateListener,
                            object : MyOkHttpListener<GsonBaseProtocol>() {
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

    fun getENDevices(networkid: String, loaderStateListener: HttpLoader.HttpLoaderStateListener? = null
    ): LiveData<List<CircleDevice.Device>> {
        return object : LiveData<List<CircleDevice.Device>>() {
            val started = AtomicBoolean(false)
            override fun onActive() {
                super.onActive()
                if (started.compareAndSet(false, true)) {
                    remoteDataSource.getENDevices(networkid, loaderStateListener, object : MyOkHttpListener<CircleDevice>() {
                        override fun success(tag: Any?, data: CircleDevice?) {
                            postValue(data?.data?.list)
                        }
                    })
                }
            }
        }
    }

    fun getMainENServer(networkid: String, loaderStateListener: HttpLoader.HttpLoaderStateListener? = null
    ): LiveData<List<CircleDevice.Device>> {
        return object : LiveData<List<CircleDevice.Device>>() {
            val started = AtomicBoolean(false)
            override fun onActive() {
                super.onActive()
                if (started.compareAndSet(false, true)) {
                    remoteDataSource.getENDevices(networkid, loaderStateListener, object : MyOkHttpListener<CircleDevice>() {
                        override fun success(tag: Any?, data: CircleDevice?) {
                            postValue(data?.data?.list?.filter {
                                it.srvmain == true
                            })
                        }
                    })
                }
            }
        }
    }

    /**
     * 启用或禁用EN设备
     */
    fun enableENDevice(networkid: String, deviceid: String, enable: Boolean, loaderStateListener: HttpLoader.HttpLoaderStateListener? = null): LiveData<Boolean> {
        return object : LiveData<Boolean>() {
            val started = AtomicBoolean(false)
            override fun onActive() {
                super.onActive()
                if (started.compareAndSet(false, true)) {
                    remoteDataSource.enableENDevice(networkid, deviceid, enable, loaderStateListener, object : MyOkHttpListener<GsonBaseProtocol>() {
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
     * 取消合作
     */
    fun cancelCooperation(networkid: String, deviceid: String, loaderStateListener: HttpLoader.HttpLoaderStateListener? = null): LiveData<Boolean> {
        return object : LiveData<Boolean>() {
            val started = AtomicBoolean(false)
            override fun onActive() {
                super.onActive()
                if (started.compareAndSet(false, true)) {
                    remoteDataSource.cancelCooperation(networkid, deviceid, loaderStateListener, object : MyOkHttpListener<GsonBaseProtocol>() {
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

    fun getDevices(networkid: String, loaderStateListener: HttpLoader.HttpLoaderStateListener? = null
    ): LiveData<ArrayList<CircleDevice.Device>> {
        return object : LiveData<ArrayList<CircleDevice.Device>>() {
            val started = AtomicBoolean(false)
            override fun onActive() {
                super.onActive()
                if (started.compareAndSet(false, true)) {
                    remoteDataSource.getDevices(networkid, loaderStateListener, object : MyOkHttpListener<CircleDevice>() {
                        override fun success(tag: Any?, data: CircleDevice?) {
                            val list: ArrayList<CircleDevice.Device> = ArrayList()
                            data?.data?.list?.let {
                                list.addAll(it)
                            }
                            postValue(list)
                        }
                    })
                }
            }
        }
    }

    /**
     *  将我的设备加入到圈子
     */
    fun deviceJoinCircle(networkid: String, deviceid: String, loaderStateListener: HttpLoader.HttpLoaderStateListener? = null
    ): LiveData<Boolean> {
        return object : LiveData<Boolean>() {
            val started = AtomicBoolean(false)
            override fun onActive() {
                super.onActive()
                if (started.compareAndSet(false, true)) {
                    remoteDataSource.deviceJoinCircle(networkid, deviceid, loaderStateListener, object : MyOkHttpListener<GsonBaseProtocol>() {
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

    fun setENServer(networkid: String, deviceid: String, feeid: String? = null, mbpoint: Float? = null,
                    loaderStateListener: HttpLoader.HttpLoaderStateListener? = null): LiveData<Boolean> {
        return object : LiveData<Boolean>() {
            val started = AtomicBoolean(false)
            override fun onActive() {
                super.onActive()
                if (started.compareAndSet(false, true)) {
                    remoteDataSource.setENServer(networkid, deviceid, feeid, mbpoint, CMAPI.getInstance().baseInfo.userId, loaderStateListener, object : MyOkHttpListener<GsonBaseProtocol>() {
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
     * 将我的设备设置为主EN服务器
     * @param srvmain 是否为主EN服务器
     */
    fun setMainENServer(networkid: String, deviceid: String, loaderStateListener: HttpLoader.HttpLoaderStateListener? = null
                        , srvmain: Boolean = true): LiveData<Boolean> {
        return object : LiveData<Boolean>() {
            val started = AtomicBoolean(false)
            override fun onActive() {
                super.onActive()
                if (started.compareAndSet(false, true)) {
                    remoteDataSource.setMainENServer(networkid, deviceid, srvmain, loaderStateListener, object : MyOkHttpListener<GsonBaseProtocol>() {
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