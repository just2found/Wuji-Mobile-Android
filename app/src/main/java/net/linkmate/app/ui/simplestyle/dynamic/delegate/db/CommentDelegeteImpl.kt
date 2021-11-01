package net.linkmate.app.ui.simplestyle.dynamic.delegate.db

import androidx.arch.core.util.Function
import io.objectbox.TxCallback
import net.linkmate.app.base.MyApplication
import net.linkmate.app.service.DynamicQueue
import net.sdvn.common.DynamicDBHelper
import net.sdvn.common.internet.BuildConfig
import net.sdvn.common.vo.Dynamic
import net.sdvn.common.vo.DynamicComment
import timber.log.Timber

/**
 * @author Raleigh.Luo
 * date：21/1/19 14
 * describe：
 */
class CommentDelegeteImpl : DBDelegete<DynamicComment>() {
    /**
     * 点赞成功后 更新
     * @param networkId 需传入networkId，避免数据产生混乱
     */
    override fun update(autoIncreaseId: Long, data: DynamicComment?) {
        data?.let {
            getBoxStore()?.let {
                it.runInTx(Runnable {
                    val olderData = it.boxFor(DynamicComment::class.java).get(autoIncreaseId)
                    data.autoIncreaseId = autoIncreaseId
                    //关系绑定
                    val dynamic = olderData.dynamic.target
                    dynamic.CommentsPO.add(data)
                    it.boxFor(Dynamic::class.java).put(dynamic)

                })
            }
        }
    }

    /**
     * 新增评论  异步  且为本地数据，非服务器同步数据
     * @param callback 返回新增数据的autoIncreaseId
     */
    override fun updateLocal(data: DynamicComment, dynamicAutoIncreaseId: Long, callback: Function<Long, Void>) {
        getBoxStore()?.let {
            var autoIncreaseId = -1L
            Timber.d("comment insert, dynamicAutoIncreaseId= $dynamicAutoIncreaseId")
            it.runInTxAsync(Runnable {
                data.id = -1
                data.createAt = System.currentTimeMillis() / 1000

                val dynamic = getBoxStore()?.boxFor(Dynamic::class.java)?.get(dynamicAutoIncreaseId)
                dynamic?.CommentsPO?.add(data)
                it.boxFor(Dynamic::class.java)?.put(dynamic)


                //put后，autoIncreaseId会自动更新
                autoIncreaseId = data.autoIncreaseId
                if (BuildConfig.DEBUG) {
                    Timber.d("comment insert local success, autoIncreaseId= $autoIncreaseId")
                }
            }, TxCallback { result, error ->
                if (BuildConfig.DEBUG) {
                    if (error != null) Timber.d("comment insert local fail,error= ${error} ->" + this.javaClass.toString())
                }
                //成功
                if (autoIncreaseId != -1L) callback.apply(autoIncreaseId)
            })
        }
    }

    override fun delete(autoIncreaseId: Long) {
        getBoxStore()?.let {
            it.runInTxAsync(Runnable {
                //先删除关系，再删除数据
                val comment = it.boxFor(DynamicComment::class.java).get(autoIncreaseId)
                //删除关系,UI监听更新
                val dynamic = comment.dynamic.target
                dynamic.LikesPO.removeById(autoIncreaseId)
                it.boxFor(Dynamic::class.java).put(dynamic)
                //删除数据
                it.boxFor(DynamicComment::class.java).remove(autoIncreaseId)
            }, TxCallback { result, error ->
                if (BuildConfig.DEBUG && error != null) Timber.d("remove like error$error ->" + DynamicDBHelper.TAG)
            })
        }
    }

    override fun check(autoIncreaseId: Long, operateType: String, callback: Function<DynamicComment, Void>) {
        val userId = DynamicQueue.mLastUserId
        val networkId = DynamicQueue.mLastNetworkId
        DynamicDBHelper.INSTANCE(MyApplication.getInstance())?.getBoxStore()?.let {
            val comment = it.boxFor(DynamicComment::class.java)?.get(autoIncreaseId)
            val dynamic = comment?.dynamic?.target
            if (dynamic != null) {
                if (DynamicQueue.isCurrentDynamicTask(dynamic.networkId, dynamic.deviceId, userId)) {
                    callback.apply(comment)
                } else {
                    delete(autoIncreaseId)
                }
            } else {
                delete(autoIncreaseId)
            }
        }
    }
}