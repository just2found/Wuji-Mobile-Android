package net.linkmate.app.ui.simplestyle.dynamic.related

import android.content.Context
import android.content.Intent
import android.text.SpannableString
import android.text.Spanned
import android.text.TextUtils
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.recyclerview.widget.RecyclerView
import io.weline.repo.files.constant.AppConstants
import kotlinx.android.synthetic.main.item_dynamic_related.view.*
import kotlinx.android.synthetic.main.layout_empty_view.view.*
import net.linkmate.app.R
import net.linkmate.app.base.MyApplication
import net.linkmate.app.data.model.dynamic.getDetailTime
import net.linkmate.app.ui.simplestyle.dynamic.CommentEvent
import net.linkmate.app.ui.simplestyle.dynamic.detial.DynamicDetailActivity
import net.linkmate.app.view.ViewHolder
import net.linkmate.app.view.adapter.QuickAriaActiveAdapter
import net.sdvn.common.vo.DynamicRelated

/**与我相关
 * @author Raleigh.Luo
 * date：21/2/3 17
 * describe：
 */
class RelatedAdapter(private val context: Context, private val viewModel: RelatedViewModel) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val NO_DATA_TYPE = -1//无数据
    private val DEFAULT_TYPE = 0//正常数据
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        when (viewType) {
            NO_DATA_TYPE -> {
                val layout = R.layout.layout_empty_view
                val view =
                        LayoutInflater.from(context).inflate(layout, null, false)
                val layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.MATCH_PARENT)
                view.layoutParams = layoutParams
                with(view) {
                    //去掉下拉提示
                    textView.setText("")
                }
                return ViewHolder(view)
            }
            else -> {
                val layout = R.layout.item_dynamic_related
                val view =
                        LayoutInflater.from(context).inflate(layout, null, false)
                val layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT)
                view.layoutParams = layoutParams
                return QuickAriaActiveAdapter.ViewHolder(view)
            }
        }

    }

    override fun getItemViewType(position: Int): Int {
        when {
            (getAbouts()?.size ?: 0) == 0 -> {
                return NO_DATA_TYPE
            }
            else -> {
                return DEFAULT_TYPE
            }
        }
    }

    override fun getItemCount(): Int {
        val size = getAbouts()?.size ?: 0
        return if (size == 0) 1 else size
    }

    private fun getAbouts(): List<DynamicRelated>? {
        return viewModel.relateds?.value
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (getItemViewType(position) == DEFAULT_TYPE) {
            with(holder.itemView) {
                ivPortrait.setImageResource(R.drawable.icon_defaultuser)
                val related = getAbouts()?.get(position)
                related?.let {
                    var comment: CommentEvent? = null//评论id
                    tvName.setText(it.username)
                    tvCommentTime.setText(it.getDetailTime())
                    tvCommentTime.visibility = View.VISIBLE

                    if (related.type == related.getCommentType()) {//评论消息
                        //注意dynamicAutoIncreaseId 需跳转到详情页面赋值，此处没有处理该值
                        comment = CommentEvent(0, it.momentID ?: 0, -1, it.relatedId
                                ?: 0, it.uid ?: "", it.username
                                ?: "", 0)
                        tvComment.setStartDrawable(null)
                        var content: String = ""
                        var replyText = MyApplication.getContext().getString(R.string.reply)
                        if (TextUtils.isEmpty(it.targetUID)) {
                            content = String.format("%s", it.content)
                        } else {
                            content = String.format("%s%s：%s", replyText, it.targetUserName, it.content)
                        }
                        val spannableString = SpannableString(content)

                        /**--回复人 点击 进入个人主页--------------------------------------------**/
                        if (!TextUtils.isEmpty(it.targetUID)) {
                            val start = replyText.length
                            val end = replyText.length + (it.targetUserName?.length ?: 0)
                            spannableString.setSpan(ForegroundColorSpan(MyApplication.getContext().resources.getColor(R.color.dynamic_name_color)), start, end, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
                        }
                        tvComment.setText(spannableString);
                    } else if (related.type == related.getLikeType()) {//点赞消息
                        tvComment.setText("")
                        tvComment.setStartDrawable(this@RelatedAdapter.context.getDrawable(R.drawable.icon_like))
                    } else {//未知消息
                        tvName.setText(R.string.the_dynamic_is_deleted)
                        tvCommentTime.visibility = View.GONE
                        tvComment.setStartDrawable(null)
                        tvComment.setText(R.string.the_dynamic_is_deleted)
                    }

                    val momentId = it.momentID
                    holder.itemView.setOnClickListener {
                        if (related.type == related.getLikeType() || related.type == related.getCommentType()) {
                            val intent = Intent(this@RelatedAdapter.context, DynamicDetailActivity::class.java)
                                    .putExtra(DynamicDetailActivity.DYNAMIC_ID, momentId)

                                    .putExtra(AppConstants.SP_FIELD_NETWORK, viewModel.networkId)
                                    .putExtra(AppConstants.SP_FIELD_DEVICE_ID, viewModel.deviceId)
                                    .putExtra(AppConstants.SP_FIELD_DEVICE_IP, viewModel.deviceIP)
                            comment?.let {
                                intent.putExtra(DynamicDetailActivity.SCROLL_COMMENT, it)
                            }
                            this@RelatedAdapter.context.startActivity(intent)
                        }
                    }
                    true
                }

            }
        }
    }
}