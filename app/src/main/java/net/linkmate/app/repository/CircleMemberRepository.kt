package net.linkmate.app.repository

import androidx.lifecycle.LiveData
import net.linkmate.app.base.MyOkHttpListener
import net.linkmate.app.data.model.CircleMember
import net.linkmate.app.data.model.CircleMember.Member
import net.linkmate.app.data.remote.CircleMemberRemoteDataSource
import net.sdvn.common.internet.core.GsonBaseProtocol
import net.sdvn.common.internet.core.HttpLoader
import java.util.concurrent.atomic.AtomicBoolean

/**圈子成员用户仓库
 * @author Raleigh.Luo
 * date：20/8/18 18
 * describe：
 */
class CircleMemberRepository {
    private val remoteDataSource = CircleMemberRemoteDataSource()
    fun getMembers(networkid: String, loaderStateListener: HttpLoader.HttpLoaderStateListener? = null): LiveData<List<Member>> {
        return object : LiveData<List<Member>>() {
            val started = AtomicBoolean(false)
            override fun onActive() {
                super.onActive()
                if (started.compareAndSet(false, true)) {
                    remoteDataSource.getMembers(networkid, loaderStateListener, object : MyOkHttpListener<CircleMember>() {
                        override fun success(tag: Any?, data: CircleMember?) {
                            postValue(data?.data?.members)
                        }
                    })
                }
            }
        }

    }

    fun deleteMember(networkid: String, removeuserid: String, loaderStateListener: HttpLoader.HttpLoaderStateListener? = null): LiveData<Boolean> {
        return object : LiveData<Boolean>() {
            val started = AtomicBoolean(false)
            override fun onActive() {
                super.onActive()
                if (started.compareAndSet(false, true)) {
                    val removeuserids = ArrayList<String>()
                    removeuserids.add(removeuserid)
                    remoteDataSource.deleteMembers(networkid, removeuserids, loaderStateListener, object : MyOkHttpListener<GsonBaseProtocol>() {
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
     * 改变网络成员级别
     * @param uselevel 要更改的级别 1-manager 2-user
     */
    fun gradeMember(networkid:String, userid:String,uselevel:Int,loaderStateListener: HttpLoader.HttpLoaderStateListener?)
            : LiveData<Boolean> {
        return object : LiveData<Boolean>() {
            val started = AtomicBoolean(false)
            override fun onActive() {
                super.onActive()
                if (started.compareAndSet(false, true)) {
                    remoteDataSource.gradeMember(networkid, userid,uselevel, loaderStateListener, object : MyOkHttpListener<GsonBaseProtocol>() {
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