package net.linkmate.app.ui.simplestyle.dynamic

import android.text.TextUtils
import androidx.arch.core.util.Function
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.switchMap
import net.linkmate.app.repository.DynamicRepository
import net.linkmate.app.service.DynamicQueue
import net.linkmate.app.ui.simplestyle.BriefCacheViewModel
import net.linkmate.app.ui.simplestyle.dynamic.delegate.db.DBDelegete
import net.linkmate.app.util.ToastUtils
import net.sdvn.cmapi.CMAPI
import net.sdvn.common.internet.core.GsonBaseProtocol
import net.sdvn.common.internet.utils.LoginTokenUtil
import net.sdvn.common.vo.DynamicComment
import net.sdvn.common.vo.DynamicLike

/** 动态公共VM
 * @author Raleigh.Luo
 * date：20/12/25 13
 * describe：
 */
open class DynamicBaseViewModel : BriefCacheViewModel() {
    protected val repository = DynamicRepository()

    /**
     * 保存最新，系统无法获取时，保留的是上一次
     */
    protected var currentNickName: String = ""
        get() {
            //        val bean = UserInfoManager.getInstance().userInfoBean
//        return if (TextUtils.isEmpty(bean.nickname)) bean.loginname else bean.nickname
            val nickname = CMAPI.getInstance().baseInfo.account //断开时会为null
            if (!TextUtils.isEmpty(nickname) && field != nickname) field = nickname
            return field
        }

    /**
     * 检查LoginToken,必须先获取
     */
    protected fun checkLoginToken(callBack: Function<Boolean, Void?>) {
        if (TextUtils.isEmpty(LoginTokenUtil.getToken())) {//Token优先处理
            LoginTokenUtil.getLoginToken(object : LoginTokenUtil.TokenCallback {
                override fun error(protocol: GsonBaseProtocol?) {
                    ToastUtils.showToast(protocol?.errmsg)
                    callBack.apply(false)
                }

                override fun success(token: String?) {
                    callBack.apply(true)
                }
            })

        } else {
            callBack.apply(true)
        }
    }

    fun initReplayCommentEvent(replayCommentEvent: CommentEvent? = null) {
        replayCommentEvent?.let {
            if (!TextUtils.isEmpty(it.hint)) {
                _commentEvent.value = it
            }
        }
    }

    /**--评论事件触发----------------------------------------------------------**/
    //是否正在触发评论事件
    private val _isTouchCommentEventing = MutableLiveData<Boolean>(false)
    val isTouchCommentEventing: LiveData<Boolean> = _isTouchCommentEventing

    /**--评论事件触发----------------------------------------------------------**/
    protected var _commentEvent: MutableLiveData<CommentEvent> = MutableLiveData()
    val commentEvent: LiveData<CommentEvent> = _commentEvent

    fun updateCommentEvent(event: CommentEvent) {
        startTouchCommentEvent()
        _commentEvent.value = event
    }

    fun clearCommentEvent() {
        _commentEvent.value = null
    }


    fun endTouchCommentEvent() {
        _isTouchCommentEventing.value = false
    }

    fun startTouchCommentEvent() {
        _isTouchCommentEventing.value = true
    }

    /**--发布评论----------------------------------------------------------*/
    fun startPublishComment(content: String, dynamicId: Long? = null, dynamicAutoIncreaseId: Long? = null) {
        //本地数据不支持发布评论功能
        if (dynamicId == null || dynamicId == -1L) return
        var commentId: Long? = null
        var targetUID: String? = null
        var targetUserName: String? = null
        var mDynamicId: Long? = dynamicId
        var mDynamicAutoIncreaseId: Long? = dynamicAutoIncreaseId
        _commentEvent.value?.let {
            mDynamicId = it.dynamicId
            mDynamicAutoIncreaseId = it.dynamicAutoIncreaseId
            commentId = it.commentId
            targetUID = it.targetUID
            targetUserName = it.targetUserName
            //恢复数据
            _commentEvent.value = null
        }

        val comment = DynamicComment()
        comment.momentID = mDynamicId
        comment.uid = DynamicQueue.mLastUserId
        comment.username = currentNickName
        comment.targetUID = targetUID
        comment.targetUserName = targetUserName
        comment.content = content
        DBDelegete.commentDelegete.updateLocal(comment, mDynamicAutoIncreaseId
                ?: 0L, Function {

            DynamicQueue.push(DynamicQueue.COMMENT_TYPE, it)
            null
        })
    }

    /**--删除动态----------------------------------------------------------**/
    fun startDeleteDynamic(dynamicAutoIncreaseId: Long) {
        DBDelegete.dynamicDelegete.deleteLocal(dynamicAutoIncreaseId, Function {
            DynamicQueue.push(DynamicQueue.DELETE_DYNAMIC_TYPE, it)
            deletedDynamic()
            null
        })
    }

    /**
     * 删除了动态
     */
    open fun deletedDynamic() {}


    /**--删除评论----------------------------------------------------------**/
    private val _deleteCommentId = MutableLiveData<Long>()
    private var commentAutoIncreaseId = 0L
    private var dynamicAutoIncreaseId = 0L
    val deleteCommentResult = _deleteCommentId.switchMap {
        repository.deleteComment(DynamicQueue.deviceId, DynamicQueue.deviceIP, it, commentAutoIncreaseId, dynamicAutoIncreaseId)
    }

    fun startDeleteComment(commentId: Long, commentAutoIncreaseId: Long, dynamicAutoIncreaseId: Long) {
        if (commentId == -1L) {//本地数据
            DynamicQueue.containsAndRemove(DynamicQueue.COMMENT_TYPE, commentAutoIncreaseId)
            //直接删除
            DBDelegete.commentDelegete.delete(commentAutoIncreaseId)
        } else {
            this.commentAutoIncreaseId = commentAutoIncreaseId
            this.dynamicAutoIncreaseId = dynamicAutoIncreaseId
            _deleteCommentId.value = commentId
        }

    }

    /**--点赞----------------------------------------------------------**/
    fun startLikeDynamic(dynamicId: Long, dynamicAutoIncreaseId: Long) {
        //不支持未发布成功的动态点赞
        if (dynamicId == -1L) return
        val like = DynamicLike()
        like.momentID = dynamicId
        like.uid = DynamicQueue.mLastUserId
        like.username = currentNickName
        DBDelegete.likeDelegete.updateLocal(like, dynamicAutoIncreaseId, Function {
            DynamicQueue.push(DynamicQueue.LIKE_TYPE, it)
            null
        })
    }

    /**--取消点赞----------------------------------------------------------**/
    fun startUnLikeDynamic(dynamicId: Long, likeAutoIncreaseId: Long) {
        //不支持未发布成功的动态取消点赞
        if (dynamicId == -1L) return
        DBDelegete.likeDelegete.deleteLocal(likeAutoIncreaseId, Function {
            DynamicQueue.push(DynamicQueue.UNLIKE_TYPE, it)
            null
        })
    }
}