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
import android.widget.TextView
import net.linkmate.app.R
import net.linkmate.app.base.MyApplication
import net.linkmate.app.service.DynamicQueue
import net.linkmate.app.ui.simplestyle.dynamic.DynamicBaseViewModel
import net.linkmate.app.util.ToastUtils
import net.sdvn.cmapi.CMAPI
import net.sdvn.cmapi.util.ClipboardUtils
import net.sdvn.common.vo.DynamicComment
import net.sdvn.nascommon.utils.DialogUtils

/**
 * @author Raleigh.Luo
 * date：20/11/26 10
 * describe：
 */
class CommentDelegateImpl(private val viewModel: DynamicBaseViewModel, private val itemView: View, private val tvComment: TextView) : CommentDelegate<DynamicComment>() {

    override fun show(comment: DynamicComment) {
        val context = MyApplication.getContext()
        var content: String = ""
        var replyText = context.getString(R.string.reply)
        if (TextUtils.isEmpty(comment.targetUID)) {
            content = String.format("%s：%s", comment.username, comment.content)
        } else {
            content = String.format("%s%s%s：%s", comment.username, replyText, comment.targetUserName, comment.content)
        }
        val spannableString = SpannableString(content)
        /**--评论人 点击 进入个人主页--------------------------------------------**/
//        spannableString.setSpan(object : ClickableSpan() {
//            override fun onClick(p0: View) {
////                ToastUtils.showToast("click " + comment.username)
//            }
//
//            override fun updateDrawState(ds: TextPaint) {
//                super.updateDrawState(ds)
//                //设置显示的字体颜色
//                ds.setColor(context.resources.getColor(R.color.friend_circle_name_color))
//                //加粗
//                ds.isFakeBoldText = true
//                ds.isUnderlineText = false
//            }
//        }, 0, comment.username?.length ?: 0, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        spannableString.setSpan(ForegroundColorSpan(MyApplication.getContext().resources.getColor(R.color.dynamic_name_color)),
                0, comment.username?.length ?: 0, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
        spannableString.setSpan(object : StyleSpan(Typeface.NORMAL) {
            override fun updateDrawState(ds: TextPaint?) {
                super.updateDrawState(ds)
                ds?.isFakeBoldText = true
                ds?.isUnderlineText = false
            }
        }, 0, comment.username?.length ?: 0, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)

        /**--回复人 点击 进入个人主页--------------------------------------------**/
        if (!TextUtils.isEmpty(comment.targetUID)) {
            val replyStart = (comment.username?.length ?: 0)
            val replayNameStart = replyStart + replyText.length
            val replayNameEnd = replayNameStart + (comment.targetUserName?.length ?: 0)
            //被回复人
//            spannableString.setSpan(object : ClickableSpan() {
//                override fun onClick(p0: View) {
////                    ToastUtils.showToast("click " + comment.targetUserName)
//                }
//
//                override fun updateDrawState(ds: TextPaint) {
//                    super.updateDrawState(ds)
//                    //设置显示的字体颜色
//                    ds.setColor(context.resources.getColor(R.color.friend_circle_name_color)) //加粗
//                    ds.isFakeBoldText = true
//                    ds.isUnderlineText = false
//                }
//            }, replayNameStart, replayNameEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

            spannableString.setSpan(ForegroundColorSpan(MyApplication.getContext().resources.getColor(R.color.dynamic_name_color)),
                    replayNameStart, replayNameEnd, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
            spannableString.setSpan(object : StyleSpan(Typeface.NORMAL) {
                override fun updateDrawState(ds: TextPaint?) {
                    super.updateDrawState(ds)
                    ds?.isFakeBoldText = true
                    ds?.isUnderlineText = false
                }
            }, replayNameStart, replayNameEnd, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)

        }
        tvComment.setText(spannableString);
//        tvComment.setMovementMethod(LinkMovementMethod.getInstance())
    }

    override fun setDefaultListener(context: Context, position: Int, dynamicId: Long, dynamicAutoIncreaseId: Long, comment: DynamicComment) {

//        /**--长按复制--------------------------------------------**/
        CMAPI.getInstance()
        //仅圈主&动态发布者 删除评论
        if (isOwnComment(comment.uid) || DynamicQueue.isCircleOwner || isOwnDynamic(comment.dynamic.target.UID)) {
            tvComment.setOnLongClickListener {
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
        } else {
            tvComment.setOnLongClickListener(null)
        }

//
//        if (!isOwnComment(comment.uid)) {//别人发布的评论
//            tvComment.setOnClickListener {
//                //项底部在屏幕中的Y 坐标
//                val itemBottomY = itemView.getScreenLocationY() + itemView.measuredHeight
//                //加一点边距
//                val event = CommentEvent(position, dynamicId,  dynamicAutoIncreaseId,comment.id
//                        ?: 0, comment.uid ?: "", comment.username
//                        ?: "", itemBottomY)
//                viewModel.updateCommentEvent(event)
//            }
//        } else {//TODO 临时
//
//            tvComment.setOnClickListener {
//                DialogUtils.showConfirmDialog(context, R.string.tips, R.string.delete_comment_confirm_hint,
//                        R.string.confirm, R.string.cancel, object : DialogUtils.OnDialogClickListener {
//                    override fun onClick(dialog: DialogInterface?, isPositiveBtn: Boolean) {
//                        dialog?.dismiss()
//                        if (isPositiveBtn) {
//                            viewModel.startDeleteComment(comment.id ?: 0L,comment.autoIncreaseId, dynamicAutoIncreaseId)
//                        }
//                    }
//                })
//            }
//        }
    }


    private fun clipString(content: String) {
        if (!TextUtils.isEmpty(content)) {
            val context = MyApplication.getContext()
            ClipboardUtils.copyToClipboard(context, content)
            ToastUtils.showToast(context.getString(R.string.Copied).toString() + content)
        }
    }
}