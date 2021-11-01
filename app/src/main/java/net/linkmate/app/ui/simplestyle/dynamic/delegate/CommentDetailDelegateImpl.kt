package net.linkmate.app.ui.simplestyle.dynamic.delegate

import android.content.Context
import android.content.DialogInterface
import android.graphics.Typeface
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.TextUtils
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import net.linkmate.app.R
import net.linkmate.app.base.MyApplication
import net.linkmate.app.data.model.dynamic.getDetailTime
import net.linkmate.app.service.DynamicQueue
import net.linkmate.app.ui.simplestyle.dynamic.CommentEvent
import net.linkmate.app.ui.simplestyle.dynamic.DynamicBaseViewModel
import net.linkmate.app.ui.simplestyle.dynamic.getScreenLocationY
import net.linkmate.app.util.ToastUtils
import net.sdvn.cmapi.util.ClipboardUtils
import net.sdvn.common.vo.DynamicComment
import net.sdvn.nascommon.utils.DialogUtils

/**
 * @author Raleigh.Luo
 * date：20/12/19 15
 * describe：
 */
class CommentDetailDelegateImpl(private val viewModel: DynamicBaseViewModel,
                                private val itemView: View,
                                private val ivPortrait: ImageView,
                                private val tvCommentTime: TextView,
                                private val tvName: TextView,
                                private val tvComment: TextView)
    : CommentDelegate<DynamicComment>() {
    override fun show(comment: DynamicComment) {
        tvName.setText(comment.username)
        var content: String = ""
        var replyText = MyApplication.getContext().getString(R.string.reply)
        if (TextUtils.isEmpty(comment.targetUID)) {
            content = String.format("%s", comment.content)
        } else {
            content = String.format("%s%s：%s", replyText, comment.targetUserName, comment.content)
        }
        val spannableString = SpannableString(content)

        /**--回复人 点击 进入个人主页--------------------------------------------**/
        if(!TextUtils.isEmpty(comment.targetUID)) {
            val start = replyText.length
            val end = replyText.length + (comment.targetUserName?.length ?: 0)

//            spannableString.setSpan(object : ClickableSpan() {
//                override fun onClick(p0: View) {
////                    viewModel.showToast("click " + comment.relayName)
//                }
//
//                override fun updateDrawState(ds: TextPaint) {
//                    super.updateDrawState(ds)
//                    //设置显示的字体颜色
//                    ds.setColor(MyApplication.getContext().resources.getColor(R.color.friend_circle_name_color))
//                    //粗体
//                    ds.isFakeBoldText = true
//                    ds.isUnderlineText = false
//                }
//            }, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            spannableString.setSpan(ForegroundColorSpan(MyApplication.getContext().resources.getColor(R.color.dynamic_name_color)),
                    start, end, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
            spannableString.setSpan(object :StyleSpan(Typeface.NORMAL){
                override fun updateDrawState(ds: TextPaint?) {
                    super.updateDrawState(ds)
                    ds?.isFakeBoldText = true
                    ds?.isUnderlineText = false
                }
            },start, end,Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
        }
        tvComment.setText(spannableString);
//        tvComment.setMovementMethod(LinkMovementMethod.getInstance())
        tvCommentTime.setText(comment.getDetailTime())
        //TODO
        ivPortrait.setImageResource(R.drawable.icon_default_user_new)

    }

    override fun setDefaultListener(context: Context, position: Int, dynamicId: Long, dynamicAutoIncreaseId: Long,comment: DynamicComment) {
        //仅圈主&动态发布者 删除评论
        if(isOwnComment(comment.uid)|| DynamicQueue.isCircleOwner || isOwnDynamic(comment.dynamic.target.UID)){
            itemView.setOnLongClickListener {
                DialogUtils.showConfirmDialog(context, R.string.tips, R.string.delete_comment_confirm_hint,
                        R.string.confirm, R.string.cancel, object : DialogUtils.OnDialogClickListener {
                    override fun onClick(dialog: DialogInterface?, isPositiveBtn: Boolean) {
                        dialog?.dismiss()
                        if (isPositiveBtn) {
                            viewModel.startDeleteComment(comment.id
                                    ?: 0L, comment.autoIncreaseId, dynamicAutoIncreaseId)
                        }
                    }
                })
                true
            }
        }else{
            itemView.setOnLongClickListener(null)
        }


        if (!isOwnComment(comment.uid)) {//别人发布的评论
            itemView.setOnClickListener {
                //项底部在屏幕上的Y坐标
                val itemBottomY = itemView.getScreenLocationY() + itemView.measuredHeight
//                        position无效
                val event = CommentEvent(position, dynamicId, dynamicAutoIncreaseId,comment.id
                        ?: 0, comment.uid ?: "", comment.username
                        ?: "", itemBottomY)
                viewModel.updateCommentEvent(event)
            }
        } else {
            itemView.setOnClickListener {
                DialogUtils.showConfirmDialog(context, R.string.tips, R.string.delete_comment_confirm_hint,
                        R.string.confirm, R.string.cancel, object : DialogUtils.OnDialogClickListener {
                    override fun onClick(dialog: DialogInterface?, isPositiveBtn: Boolean) {
                        dialog?.dismiss()
                        if (isPositiveBtn) {
                            viewModel.startDeleteComment(comment.id ?: 0L,comment.autoIncreaseId, dynamicAutoIncreaseId)
                        }
                    }
                })
            }
        }
    }

    private fun clipString(content: String) {
        if (!TextUtils.isEmpty(content)) {
            ClipboardUtils.copyToClipboard(MyApplication.getContext(), content)
            ToastUtils.showToast(MyApplication.getContext().getString(R.string.Copied).toString() + content)
        }
    }


}