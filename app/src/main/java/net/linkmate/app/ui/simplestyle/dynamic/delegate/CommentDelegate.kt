package net.linkmate.app.ui.simplestyle.dynamic.delegate

import android.content.Context
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import net.linkmate.app.service.DynamicQueue
import net.linkmate.app.ui.simplestyle.dynamic.DynamicBaseViewModel
import net.sdvn.cmapi.CMAPI
import net.sdvn.common.vo.DynamicComment

/**
 * @author Raleigh.Luo
 * date：20/11/26 10
 * describe：
 */
abstract class CommentDelegate<T> {
    companion object {
        @JvmStatic
        fun create(viewModel: DynamicBaseViewModel, itemView: View, tvComment: TextView): CommentDelegate<DynamicComment> {
            return CommentDelegateImpl(viewModel, itemView,tvComment)
        }

        @JvmStatic
        fun create(viewModel: DynamicBaseViewModel, itemView: View, ivPortrait: ImageView, tvCommentTime: TextView, tvName: TextView, tvComment: TextView): CommentDelegate<DynamicComment> {
            return CommentDetailDelegateImpl(viewModel, itemView, ivPortrait, tvCommentTime, tvName, tvComment)
        }
    }

    abstract fun show(obj: T)

    abstract fun setDefaultListener(context: Context, position: Int, dynamicId: Long, dynamicAutoIncreaseId:Long, obj: T)

    //是否是自己发布的评论
    protected fun isOwnComment(userId: String?): Boolean{
        return DynamicQueue.mLastUserId == userId
    }
    //是否是自己发布的评论
    protected fun isOwnDynamic(userId: String?): Boolean{
        return CMAPI.getInstance().baseInfo.userId == userId
    }
}