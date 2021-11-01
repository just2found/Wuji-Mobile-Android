package net.linkmate.app.ui.simplestyle.dynamic.delegate

import android.content.Context
import android.text.TextUtils
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import libs.source.common.utils.Utils
import net.linkmate.app.R
import net.linkmate.app.base.MyApplication
import net.linkmate.app.ui.simplestyle.dynamic.DynamicBaseViewModel
import net.linkmate.app.util.ToastUtils
import net.sdvn.cmapi.util.ClipboardUtils
import net.sdvn.common.vo.DynamicAttachment

/** 动态详情 附件代理
 * @author Raleigh.Luo
 * date：20/11/26 10
 * describe：
 */
class AttachmentDetailDelegateImpl(private val viewModel: DynamicBaseViewModel,
                                   private val itemView: View,
                                   private val ivAttachLogo: ImageView,
                                   private val tvAttachName: TextView,
                                   private val tvAttachDetail: TextView) : AttachmentDelegate<DynamicAttachment>() {

    override fun show(attach: DynamicAttachment) {
        tvAttachName.setText(attach.name?:attach.url)
//        ivAttachLogo
        tvAttachDetail.setText(String.format("%s，%s", attach.size, attach.cost))
    }

    override fun setDefaultListener(context: Context, attach: DynamicAttachment) {
        /**--单项点击复制--------------------------------------------**/
        itemView.setOnClickListener {
            if (!Utils.isFastClick(it)) {
                clipString(attach.url?:"")
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