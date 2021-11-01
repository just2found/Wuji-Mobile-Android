package net.linkmate.app.repository

import androidx.lifecycle.LiveData
import net.linkmate.app.base.MyOkHttpListener
import net.linkmate.app.data.remote.CircleShareRemoteDataSource
import net.sdvn.common.internet.core.GsonBaseProtocol
import net.sdvn.common.internet.core.HttpLoader
import net.sdvn.common.internet.protocol.ShareCode
import java.util.concurrent.atomic.AtomicBoolean

/**圈子分享仓库
 * @author Raleigh.Luo
 * date：20/8/19 13
 * describe：
 */
class CircleShareRepository {
    private val remoteDataSource = CircleShareRemoteDataSource()
    fun getNetworkShareCode(networkid: String, loaderStateListener: HttpLoader.HttpLoaderStateListener? = null): LiveData<ShareCode> {
        return object : LiveData<ShareCode>() {
            val started = AtomicBoolean(false)
            override fun onActive() {
                super.onActive()
                if (started.compareAndSet(false, true)) {
                    remoteDataSource.getNetworkShareCode(networkid, loaderStateListener, object : MyOkHttpListener<ShareCode>() {
                        override fun success(tag: Any?, data: ShareCode?) {
                            postValue(data)
                        }
                    })
                }
            }
        }
    }


    fun setNetworkConfirm(networkid: String, provide_confirm: Boolean? = null , join_confirm: Boolean? = null , loaderStateListener: HttpLoader.HttpLoaderStateListener? = null): LiveData<Boolean> {
        return object : LiveData<Boolean>() {
            val started = AtomicBoolean(false)
            override fun onActive() {
                super.onActive()
                if (started.compareAndSet(false, true)) {
                    remoteDataSource.setNetworkConfirm(networkid, provide_confirm,join_confirm,loaderStateListener, object : MyOkHttpListener<GsonBaseProtocol>() {
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