package net.linkmate.app.ui.simplestyle.dynamic.delegate.db

import androidx.arch.core.util.Function
import io.objectbox.TxCallback
import net.linkmate.app.base.MyApplication
import net.linkmate.app.service.DynamicQueue
import net.sdvn.common.DynamicDBHelper
import net.sdvn.common.internet.BuildConfig
import net.sdvn.common.vo.Dynamic
import net.sdvn.common.vo.DynamicLike
import timber.log.Timber

/**
 * @author Raleigh.Luo
 * date：21/1/19 14
 * describe：
 */
class LikeDelegeteImpl : DBDelegete<DynamicLike>() {
    /**
     * 点赞成功后 更新
     * @param networkId 需传入networkId，避免数据产生混乱
     */
    override fun update(autoIncreaseId: Long, data: DynamicLike?) {
        data?.let {
            getBoxStore()?.let {
                it.runInTx(Runnable {
                    val box = it.boxFor(DynamicLike::class.java)
                    val olderData = box.get(autoIncreaseId)
                    data.autoIncreaseId = autoIncreaseId
                    val dynamic = olderData.dynamic.target
                    //关系绑定,更新动态后自动更新like
                    dynamic.LikesPO.add(data)
                    it.boxFor(Dynamic::class.java).put(dynamic)
                })
            }
        }
    }

    /**
     * 新增点赞  异步  且为本地数据，非服务器同步数据
     * @param callback 返回新增数据的autoIncreaseId
     */
    override fun updateLocal(data: DynamicLike, dynamicAutoIncreaseId: Long, callback: Function<Long, Void>) {
        getBoxStore()?.let {
            var autoIncreaseId = -1L
            it.runInTxAsync(Runnable {
                val dynamic = getBoxStore()?.boxFor(Dynamic::class.java)?.get(dynamicAutoIncreaseId)
                val like = dynamic?.LikesPO?.find {
                    it.uid == DynamicQueue.mLastUserId
                }
                if (like != null) {//已有点赞
                    DynamicQueue.containsAndRemove(DynamicQueue.UNLIKE_TYPE, like.autoIncreaseId)
                    //有
                    like?.isDeleted = false
                    it.boxFor(DynamicLike::class.java)?.put(like)

                    val dynamic = like?.dynamic?.target

                    it.boxFor(Dynamic::class.java)?.put(dynamic)
                } else {
                    data.id = -1
                    data.updateAt = System.currentTimeMillis() / 1000

                    dynamic?.LikesPO?.add(data)
                    it.boxFor(Dynamic::class.java)?.put(dynamic)
                    //put后，autoIncreaseId会自动更新
                    autoIncreaseId = data.autoIncreaseId
                }
            }, TxCallback { result, error ->
                //成功
                if (autoIncreaseId != -1L) callback.apply(autoIncreaseId)
            })
        }
    }

    /**
     * 取消点赞  同步  且为本地数据，非服务器同步数据
     * @param callback 返回点赞数据的autoIncreaseId
     */
    override fun deleteLocal(autoIncreaseId: Long, callback: Function<Long, Void>) {
        if (DynamicQueue.containsAndRemove(DynamicQueue.LIKE_TYPE, autoIncreaseId)) {
            //还未点赞成功，就取消点赞
            delete(autoIncreaseId)
        } else {
            getBoxStore()?.let {
                it.runInTxAsync(Runnable {
                    val like = it.boxFor(DynamicLike::class.java)?.get(autoIncreaseId)
                    like?.isDeleted = true
                    it.boxFor(DynamicLike::class.java)?.put(like)

                    val dynamic = like?.dynamic?.target

                    it.boxFor(Dynamic::class.java)?.put(dynamic)
                    //put后，autoIncreaseId会自动更新
                }, TxCallback { result, error ->
                    callback.apply(autoIncreaseId)
                })
            }
        }

    }

    override fun delete(autoIncreaseId: Long) {
        getBoxStore()?.let {
            it.runInTxAsync(Runnable {
                //先删除关系，再删除数据
                val like = it.boxFor(DynamicLike::class.java).get(autoIncreaseId)
                //删除关系 UI监听更新
                val dynamic = like.dynamic.target
                dynamic.LikesPO.removeById(autoIncreaseId)
                it.boxFor(Dynamic::class.java).put(dynamic)
                //删除数据
                it.boxFor(DynamicLike::class.java).remove(autoIncreaseId)
            }, TxCallback { result, error ->
                if (BuildConfig.DEBUG && error != null) Timber.d(DynamicQueue.TAG + "-> remove like error$error")
            })
        }
    }

    override fun check(autoIncreaseId: Long, operateType: String, callback: Function<DynamicLike, Void>) {
        val userId = DynamicQueue.mLastUserId
        val networkId = DynamicQueue.mLastNetworkId
        DynamicDBHelper.INSTANCE(MyApplication.getInstance())?.getBoxStore()?.let {
            val like = it.boxFor(DynamicLike::class.java)?.get(autoIncreaseId)
            val dynamic = like?.dynamic?.target
            if (dynamic != null) {
                if (DynamicQueue.isCurrentDynamicTask(dynamic.networkId, dynamic.deviceId, userId)) {
                    if (operateType == DynamicQueue.UNLIKE_TYPE && like.id == -1L) {//是取消赞操作，且为本地数据（未发布成功），直接删除
                        delete(autoIncreaseId)
                    } else {
                        callback.apply(like)
                    }
                } else {
                    delete(autoIncreaseId)
                }
            } else {
                delete(autoIncreaseId)
            }
        }
    }
}