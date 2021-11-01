package net.sdvn.common

import android.app.Application
import android.content.Context
import android.text.TextUtils
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import com.getkeepsafe.relinker.ReLinker
import io.objectbox.BoxStore
import io.objectbox.android.AndroidObjectBrowser
import io.objectbox.android.ObjectBoxDataSource
import io.objectbox.android.ObjectBoxLiveData
import io.objectbox.kotlin.query
import net.sdvn.cmapi.CMAPI
import net.sdvn.common.internet.BuildConfig
import net.sdvn.common.vo.*
import timber.log.Timber

/** 动态数据库
 * 根据用户创建数据库，且只保留当前网络的数据
 * 数据库路径依据 userId/dynamic
 * @author Raleigh.Luo
 * date：21/1/5 17
 * describe：
 */
class DynamicDBHelper {
    private val DB_DIR_NAME = "dynamic"
    private val DEBUG = BuildConfig.DEBUG || BuildConfig.isTestOnly
    private var sBoxStore: BoxStore? = null
    var currentUserId: String? = null
        private set(value) {
            field = value
        }

    companion object {
        const val TAG = "DynamicDB"

        @Volatile
        private var instance: DynamicDBHelper? = null

        @JvmStatic
        fun INSTANCE(App: Application): DynamicDBHelper? {
            val userId = CMAPI.getInstance().baseInfo.userId
            instance?.let {//已存在实例
                if (TextUtils.isEmpty(userId) && !TextUtils.isEmpty(it.currentUserId)) {//VPN隧道被抢占,还是用原来的

                } else if (it.currentUserId != userId) {
                    //切换了用户，重新生成实例
                    instance?.getBoxStore()?.close()
                    instance = null
                    instance = DynamicDBHelper(App)
                }
                true
            } ?: let {//未存在实例
                //必须用户已登录
                if (!TextUtils.isEmpty(userId)) {
                    instance = DynamicDBHelper(App)
                }

            }
            return instance
        }

    }

    private constructor(App: Application) {
        val userId = CMAPI.getInstance().baseInfo.userId
        if (TextUtils.isEmpty(currentUserId) || currentUserId != userId) {//切换用户时，更新数据库
            val context: Context = App.getApplicationContext()
            currentUserId = userId
            //目录 用户id 下创建dynamic
            //路径 userId/networkId/dynamic
            val OBJECT_BOX_INTERNET = if (BuildConfig.DEBUG) currentUserId + "_debug" else currentUserId
            val objectBox = context.getDatabasePath(OBJECT_BOX_INTERNET)
            sBoxStore = null
            sBoxStore = MyObjectBox.builder()
                    .androidContext(context.applicationContext)
                    .baseDirectory(objectBox)
                    .name(DB_DIR_NAME)
                    .androidReLinker(ReLinker.Logger { message -> Timber.tag("ReLinker").d(message) })
                    .build()
            if (DEBUG && sBoxStore != null) {
                val started = AndroidObjectBrowser(sBoxStore).start(context)
                Timber.d("ObjectBrowser Started: $started")
                //            adb forward tcp:8090 tcp:8090
//            http://localhost:8090/index.html
                Timber.d("Using ObjectBox %s (%s)", BoxStore.getVersion(), BoxStore.getVersionNative())
            }
        }
    }


    fun getBoxStore(): BoxStore? {
        return sBoxStore
    }

    fun exit() {
        if (sBoxStore != null) sBoxStore = null
    }

    /**
     * 清空当前用户数据库
     */
    fun clear() {
        sBoxStore?.let {
            it.removeAllObjects()
        }
    }
}