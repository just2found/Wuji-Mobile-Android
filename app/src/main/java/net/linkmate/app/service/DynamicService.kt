package net.linkmate.app.service

import android.app.IntentService
import android.content.Intent
import net.linkmate.app.repository.DynamicRepository
import timber.log.Timber
import androidx.arch.core.util.Function
import net.linkmate.app.ui.simplestyle.dynamic.delegate.db.DBDelegete
import net.sdvn.cmapi.CMAPI
import net.sdvn.common.internet.BuildConfig

/** 子线程 动态任务处理，保证依次执行
 * @author Raleigh.Luo
 * date：21/1/23 13
 * describe：
 */
class DynamicService : IntentService("DynamicService") {
    private val repository = DynamicRepository()

    override fun onHandleIntent(p0: Intent?) {
        if (BuildConfig.DEBUG) Timber.d(DynamicQueue.TAG + "-> DynamicService onHandleIntent")
        try {
            while (!DynamicQueue.isEmpty()) {
                val messageInfo: String? = DynamicQueue.poll()
                //去重复
                DynamicQueue.containsAndRemove(messageInfo ?: "")
                if (null != messageInfo) {
                    this.dispatch(messageInfo)
                }
                if (!(CMAPI.getInstance().isEstablished || CMAPI.getInstance().isConnected)) {//停止
                    break
                }
            }
        } catch (e: Exception) {
        }
        DynamicQueue.close()
    }

    private fun dispatch(message: String) {
        val messageSplit = message.split("/")
        if (messageSplit.size < 2) return
        val autoIncreseId = messageSplit[1].toLong()
        //请求失败，重新添加到队列中
        val addAgainToQueue = Function<Boolean, Void> {
            if (it) {//后台没有响应,重新添加到队列
                if (BuildConfig.DEBUG) Timber.d(DynamicQueue.TAG + "-> addAgainToQueue = $message ")
                DynamicQueue.push(message)
            }
            null
        }

        when (messageSplit[0]) {
            DynamicQueue.LIKE_TYPE -> {
                DBDelegete.likeDelegete.check(autoIncreseId, messageSplit[0], Function {
                    repository.likeRx(it, addAgainToQueue)
                    null
                })

            }
            DynamicQueue.UNLIKE_TYPE -> {
                DBDelegete.likeDelegete.check(autoIncreseId, messageSplit[0], Function {
                    repository.unLikeRx(it, addAgainToQueue)
                    null
                })
            }
            DynamicQueue.COMMENT_TYPE -> {
                DBDelegete.commentDelegete.check(autoIncreseId, messageSplit[0], Function {
                    repository.publishCommentRx(it, addAgainToQueue)
                    null
                })


            }
            DynamicQueue.DELETE_DYNAMIC_TYPE -> {
                DBDelegete.dynamicDelegete.check(autoIncreseId, messageSplit[0], Function {
                    repository.deleteDynamicRx(autoIncreseId, it.ID
                            ?: 0L, addAgainToQueue)
                    null
                })

            }
            DynamicQueue.PUBLISH_DYNAMIC_TYPE -> {
                DBDelegete.dynamicDelegete.check(autoIncreseId, messageSplit[0], Function {
                    repository.publishDynamicRx(it, null)
                    null
                })

            }
            else -> {

            }
        }
    }
}
