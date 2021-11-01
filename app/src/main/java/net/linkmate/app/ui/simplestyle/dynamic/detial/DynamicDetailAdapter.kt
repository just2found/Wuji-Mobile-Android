package net.linkmate.app.ui.simplestyle.dynamic.detial

import android.view.*
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_dynamic_detail_attach_simplestyle.view.*
import kotlinx.android.synthetic.main.item_dynamic_detial_comment_simplestyle.view.*
import kotlinx.android.synthetic.main.item_dynamic_time_simplestyle.view.*
import net.linkmate.app.R
import net.linkmate.app.data.model.dynamic.getDetailTime
import net.linkmate.app.ui.simplestyle.dynamic.DynamicBaseViewModel
import net.linkmate.app.ui.simplestyle.dynamic.DynamicItemAapter
import net.linkmate.app.ui.simplestyle.dynamic.delegate.AttachmentDelegate
import net.linkmate.app.ui.simplestyle.dynamic.delegate.CommentDelegate
import net.linkmate.app.ui.simplestyle.dynamic.getScreenLocationY
import net.sdvn.common.vo.DynamicAttachment
import net.sdvn.common.vo.DynamicComment

/** 动态详情适配器
 * @author Raleigh.Luo
 * date：20/11/25 14
 * describe：
 * @param marginStart 其它
 * @param itemMarginStart 项边界
 */
class DynamicDetailAdapter(private val context: FragmentActivity, private val viewModel: DynamicDetailViewModel, private var replayCommentId: Long?,
                           private val marginStart: Int = 0, private val itemMarginStart: Int = 0) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val HEADER_TYPE = 0
    private val BODY_TYPE = 1

    //附件
    private val ATTACH_VIEW_TYPE = 0

    //评论
    private val COMMENT_VIEW_TYPE = 1

    //时间／点赞
    private val TIME_VIEW_TYPE = 2

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        var layout = 0
        when (viewType) {
            ATTACH_VIEW_TYPE -> {
                layout = R.layout.item_dynamic_detail_attach_simplestyle
                val view =
                        LayoutInflater.from(context).inflate(layout, null, false)
                val layoutParams = FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.WRAP_CONTENT)
                layoutParams.marginStart = marginStart
                view.layoutParams = layoutParams
                with(view) {
                    //还原评论框
                    clAttachItem.setOnTouchListener(onTouchListener)
                }
                return AttachViewHolder(view, viewModel)
            }
            COMMENT_VIEW_TYPE -> {
                layout = R.layout.item_dynamic_detial_comment_simplestyle
                val view =
                        LayoutInflater.from(context).inflate(layout, null, false)
                val layoutParams = ConstraintLayout.LayoutParams(
                        ConstraintLayout.LayoutParams.MATCH_PARENT,
                        ConstraintLayout.LayoutParams.WRAP_CONTENT)
                layoutParams.marginStart = itemMarginStart
                view.layoutParams = layoutParams
                with(view) {
                    //还原评论框
                    setOnTouchListener(onTouchListener)
                }
                return CommentViewHolder(view, viewModel)
            }
            else -> {//TIME_VIEW_TYPE
                layout = R.layout.item_dynamic_time_simplestyle
                val view =
                        LayoutInflater.from(context).inflate(layout, null, false)
                val layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT)
                layoutParams.marginStart = marginStart
                view.layoutParams = layoutParams
                with(view) {
                    //还原评论框
                    setOnTouchListener(onTouchListener)
//                    ivComment.setOnTouchListener(onTouchListener)
                    tvLike.setOnTouchListener(onTouchListener)
                }
                return DynamicItemAapter.TimeViewHolder(view, viewModel)

            }
        }
    }

    /**
     * 还原评论框
     */
    private val onTouchListener = View.OnTouchListener { view, motionEvent ->
        viewModel.recoveryCommentDialog()
        false
    }

    private fun getAttachsSize(): Int {
        return viewModel.getDynamic()?.AttachmentsPO?.size ?: 0
    }

    private fun getCommentsSize(): Int {
        return viewModel.getDynamic()?.CommentsPO?.size ?: 0
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

    override fun getItemCount(): Int {
        // 时间
        return getAttachsSize() + getCommentsSize() + 1
    }

    private var isInitReplayCommentEvent = false

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (getItemViewType(position)) {
            ATTACH_VIEW_TYPE -> {//附件
                with(holder.itemView) {
                    val mViewHolder = holder as AttachViewHolder
                    val index = position
                    //第一项 显示间距
                    val bound = context.resources.getDimensionPixelSize(R.dimen.common_5)

                    var topPadding = 0
                    var bottomPadding = 0

                    if (index == 0) topPadding = bound //第一项
                    if (position + 1 == getAttachsSize()) bottomPadding = bound //最后一项
                    rootAttachItem.setPaddingRelative(bound, topPadding, bound, bottomPadding)

                    val attach = viewModel.getDynamic()?.AttachmentsPO?.get(index)!!
                    mViewHolder.attachmentDelegate.show(attach)
                    mViewHolder.attachmentDelegate.setDefaultListener(context, attach)
                }
            }
            COMMENT_VIEW_TYPE -> {//评论
                val index = position - getAttachsSize() - 1
                with(holder.itemView) {
                    val mViewHolder = holder as CommentViewHolder
                    val bound = context.resources.getDimensionPixelSize(R.dimen.common_5)
                    var topPadding = 0
                    var bottomPadding = 0
                    if (index == 0) topPadding = bound //评论第一项
                    if (position == itemCount - 1) bottomPadding = bound //评论最后一项
                    rootCommentItem.setPaddingRelative(0, topPadding, 0, bottomPadding)
                    viewModel.getDynamic()?.let {
                        val comment = it.CommentsPO?.get(index)!!

                        mViewHolder.commentDelegate.show(comment)
                        mViewHolder.commentDelegate.setDefaultListener(context, position, it.ID!!, it.autoIncreaseId, comment)

                        replayCommentId?.let {
                            if (isInitReplayCommentEvent == false && replayCommentId == comment.id) {
                                isInitReplayCommentEvent = true
                                val onGlobalLayoutListener = object : ViewTreeObserver.OnGlobalLayoutListener {
                                    override fun onGlobalLayout() {
                                        //滑动到指定评论位置，且移除监听器，只执行一次
                                        val itemBottomY = mViewHolder.itemView.getScreenLocationY() + mViewHolder.itemView.measuredHeight
                                        viewModel.defualtReplayCommentScreenY.value = itemBottomY
                                        mViewHolder.itemView.viewTreeObserver.removeOnGlobalLayoutListener(this)
                                    }
                                }
                                mViewHolder.itemView.viewTreeObserver.addOnGlobalLayoutListener(onGlobalLayoutListener)

                            }
                        }
                    }

                }
            }
            TIME_VIEW_TYPE -> {//时间／点赞
                val mViewHolder = holder as DynamicItemAapter.TimeViewHolder
                with(holder.itemView) {
                    val likes = viewModel.getDynamic()?.LikesPO
                    val id = viewModel.getDynamic()?.ID
                    val autoIncreaseId = viewModel.getDynamic()?.autoIncreaseId

                    mViewHolder.likeDelegate.show(id, likes)
                    mViewHolder.likeDelegate.setDefaultListener(context, id, autoIncreaseId, likes)
                    tvTime.setText(viewModel.getDynamic()?.getDetailTime())
//                    ivComment.visibility = View.GONE
                }
                mViewHolder.deleteDelegate.show(context.supportFragmentManager, viewModel.getDynamic())
            }
        }
    }


    class AttachViewHolder(view: View, viewModel: DynamicBaseViewModel) : RecyclerView.ViewHolder(view) {
        val attachmentDelegate: AttachmentDelegate<DynamicAttachment>

        init {
            with(itemView) {
                attachmentDelegate = AttachmentDelegate.create(viewModel, clAttachItem, ivAttachLogo, tvAttach, tvAttachDetail)
            }
        }
    }

    class CommentViewHolder(view: View, viewModel: DynamicBaseViewModel) : RecyclerView.ViewHolder(view) {
        val commentDelegate: CommentDelegate<DynamicComment>

        init {
            with(itemView) {
                commentDelegate = CommentDelegate.create(viewModel, clCommentItem, ivPortrait, tvCommentTime, tvName, tvComment)
            }
        }
    }
}