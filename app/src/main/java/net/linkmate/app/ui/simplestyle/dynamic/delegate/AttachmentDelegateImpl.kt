package net.linkmate.app.ui.simplestyle.dynamic.delegate

import android.content.Context
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.TextUtils
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.View
import android.widget.TextView
import libs.source.common.utils.Utils
import net.linkmate.app.R
import net.linkmate.app.base.MyApplication
import net.linkmate.app.ui.simplestyle.dynamic.DynamicBaseViewModel
import net.linkmate.app.util.ToastUtils
import net.sdvn.cmapi.util.ClipboardUtils
import net.sdvn.common.vo.DynamicAttachment

/** 动态列表 附件代理 实现
 * @author Raleigh.Luo
 * date：20/11/25 21
 * describe：
 */
internal class AttachmentDelegateImpl(private val viewModel: DynamicBaseViewModel, private val tvAttach: TextView) : AttachmentDelegate<DynamicAttachment>(){
    override fun show(attach: DynamicAttachment) {
        val name = attach.name?:attach.url
        val content = String.format("%s (%s，%s)", name, attach.size, attach.cost)
        val spannableString = SpannableString(content)

        /**--标题点击事件 进入下载--------------------------------------------**/
        spannableString.setSpan(object : ClickableSpan() {
            override fun onClick(p0: View) {
                ToastUtils.showToast("click " + (name))
            }

            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                //设置显示的字体颜色
                ds.setColor(MyApplication.getContext().resources.getColor(R.color.dynamic_name_color))
                ds.isUnderlineText = false
            }
        }, 0, name?.length?:0, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        tvAttach.setText(spannableString);
        tvAttach.setMovementMethod(LinkMovementMethod.getInstance())
    }

    override fun setDefaultListener(context: Context, attach: DynamicAttachment) {
        tvAttach.setOnClickListener {
            if (!Utils.isFastClick(it)) {
                clipString(tvAttach.text.toString())
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