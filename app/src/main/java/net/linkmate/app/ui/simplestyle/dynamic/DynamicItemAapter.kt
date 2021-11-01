package net.linkmate.app.ui.simplestyle.dynamic

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_dynamic_attach_simplestyle.view.*
import kotlinx.android.synthetic.main.item_dynamic_comment_simplestyle.view.*
import kotlinx.android.synthetic.main.item_dynamic_time_simplestyle.view.*
import net.linkmate.app.R
import net.linkmate.app.data.model.dynamic.getRecentlyTime
import net.linkmate.app.service.DynamicQueue
import net.linkmate.app.ui.simplestyle.dynamic.delegate.AttachmentDelegate
import net.linkmate.app.ui.simplestyle.dynamic.delegate.CommentDelegate
import net.linkmate.app.ui.simplestyle.dynamic.delegate.DeleteDelegate
import net.linkmate.app.ui.simplestyle.dynamic.delegate.LikeDelegate
import net.sdvn.cmapi.CMAPI
import net.sdvn.common.vo.Dynamic
import net.sdvn.common.vo.DynamicAttachment
import net.sdvn.common.vo.DynamicComment
import net.sdvn.common.vo.DynamicLike

/**
 * @author Raleigh.Luo
 * date：20/11/21 15
 * describe：
 */
class DynamicItemAapter(val context: FragmentActivity, val viewModel: DynamicViewModel) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    var upperPosition = -1
    var dynamic: Dynamic? = null

    //附件
    private val ATTACH_VIEW_TYPE = 0

    //评论
    private val COMMENT_VIEW_TYPE = 1

    //时间／点赞
    private val TIME_VIEW_TYPE = 2

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        var layout = R.layout.item_dynamic_time_simplestyle
        when (viewType) {
            ATTACH_VIEW_TYPE -> {
                layout = R.layout.item_dynamic_attach_simplestyle
                val view =
                        LayoutInflater.from(context).inflate(layout, null, false)
                view.layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT)
                return AttachViewHolder(view, viewModel)
            }
            COMMENT_VIEW_TYPE -> {
                layout = R.layout.item_dynamic_comment_simplestyle
                val view =
                        LayoutInflater.from(context).inflate(layout, null, false)
                view.layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT)
                return CommentViewHolder(view, viewModel)
            }
            else -> {//TIME_VIEW_TYPE
                layout = R.layout.item_dynamic_time_simplestyle
                val view =
                        LayoutInflater.from(context).inflate(layout, null, false)
                view.layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT)
                return TimeViewHolder(view, viewModel)
            }
        }

    }

    private fun getAttachsSize(): Int {
        return dynamic?.AttachmentsPO?.size ?: 0
    }

    private fun getCommentsSize(): Int {
        return dynamic?.CommentsPO?.size ?: 0
    }


    override fun getItemViewType(position: Int): Int {
        when {
            position < getAttachsSize() -> {//附件
                return ATTACH_VIEW_TYPE
            }
            position == getAttachsSize() -> { //时间／点赞
                return TIME_VIEW_TYPE
            }
            position > getAttachsSize() -> {//评论
                return COMMENT_VIEW_TYPE
            }
            else -> {
                return TIME_VIEW_TYPE
            }
        }
    }

    fun updateItems(upperPosition: Int, dynamic: Dynamic?) {
        this.upperPosition = upperPosition
        val count = itemCount

        this.dynamic = dynamic


        if (count == 0) {
            notifyDataSetChanged()
        } else {
            notifyItemRangeChanged(0, itemCount, arrayListOf(1))
        }
    }

    override fun getItemCount(): Int {
        // 时间


        return getAttachsSize() + getCommentsSize() + 1
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (getItemViewType(position)) {
            ATTACH_VIEW_TYPE -> {//附件
                with(holder.itemView) {
                    val mViewHolder = holder as AttachViewHolder
                    val index = position
                    flAttach.setPaddingRelative(0, if (index == 0) context.resources.getDimensionPixelSize(R.dimen.common_6) else 0, 0, 0)
                    //第一项 显示间距
//                    vSpace.visibility = if (index == 0) View.VISIBLE else View.GONE
                    val attach = dynamic?.AttachmentsPO?.get(index)!!
                    mViewHolder.attachmentDelegate.show(attach)
                    mViewHolder.attachmentDelegate.setDefaultListener(context, attach)
                }
            }
            COMMENT_VIEW_TYPE -> {//评论
                val index = position - getAttachsSize() - 1
                with(holder.itemView) {
                    val mViewHolder = holder as CommentViewHolder
                    var padding = context.resources.getDimensionPixelSize(R.dimen.common_6)
                    var topPadding = 0
                    var bottomPadding = 0
                    if (index == 0) topPadding = padding //评论第一项
                    if (position == itemCount - 1) bottomPadding = padding //评论最后一项
                    flComment.setPaddingRelative(0, topPadding, 0, bottomPadding)

                    val comment = dynamic?.CommentsPO?.get(index)!!
                    mViewHolder.commentDelegate.show(comment)
                    mViewHolder.commentDelegate.setDefaultListener(context, upperPosition, dynamic?.ID!!, dynamic?.autoIncreaseId!!, comment)
                    tvComment.setOnClickListener {
                        if (comment.uid != DynamicQueue.mLastUserId) {//非自己的评论，才能回复
                            val commentEvent = CommentEvent(0, comment.momentID ?: 0, -1, comment.id
                                    ?: 0, comment.uid ?: "", comment.username
                                    ?: "", 0)
                            viewModel.accessDynamicDetail(dynamic?.ID, commentEvent)
                        } else {
                            viewModel.accessDynamicDetail(dynamic?.ID)
                        }
                    }
                }
            }
            TIME_VIEW_TYPE -> {//时间／点赞
                val mViewHolder = holder as TimeViewHolder
                with(holder.itemView) {
                    mViewHolder.likeDelegate.show(dynamic?.ID, dynamic?.LikesPO)
                    mViewHolder.likeDelegate.setDefaultListener(context, dynamic?.ID, dynamic?.autoIncreaseId, dynamic?.LikesPO)
                    tvTime.setText(dynamic?.getRecentlyTime())
                    setOnClickListener {
                        viewModel.accessDynamicDetail(dynamic?.ID)
                    }
//                    ivComment.setOnClickListener {
//                        if (!Utils.isFastClick(it)) {
//                            val comment = CommentEvent(upperPosition, dynamic?.ID!!, dynamic?.autoIncreaseId!!)
//                            viewModel.updateCommentEvent(comment)
//                        }
//                    }
                }
//                mViewHolder.deleteDelegate.show(context.supportFragmentManager, dynamic)
            }

        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int, payloads: MutableList<Any>) {
        onBindViewHolder(holder, position)
    }

    class AttachViewHolder(view: View, viewModel: DynamicBaseViewModel) : RecyclerView.ViewHolder(view) {
        val attachmentDelegate: AttachmentDelegate<DynamicAttachment>

        init {
            with(itemView) {
                attachmentDelegate = AttachmentDelegate.create(viewModel, tvAttach)
            }
        }
    }

    class CommentViewHolder(view: View, viewModel: DynamicBaseViewModel) : RecyclerView.ViewHolder(view) {
        val commentDelegate: CommentDelegate<DynamicComment>

        init {
            with(itemView) {
                commentDelegate = CommentDelegate.create(viewModel, this, tvComment)
            }
        }
    }

    class TimeViewHolder(view: View, viewModel: DynamicBaseViewModel) : RecyclerView.ViewHolder(view) {

        val likeDelegate: LikeDelegate<List<DynamicLike>>
        val deleteDelegate: DeleteDelegate<Dynamic>

        init {
            with(itemView) {
                likeDelegate = LikeDelegate.create(viewModel, tvLike)
                deleteDelegate = DeleteDelegate.create(viewModel, tvDelete)
            }
        }
    }
}