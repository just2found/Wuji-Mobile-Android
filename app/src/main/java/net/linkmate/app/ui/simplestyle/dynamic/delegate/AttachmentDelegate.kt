package net.linkmate.app.ui.simplestyle.dynamic.delegate

import android.content.Context
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import net.linkmate.app.ui.simplestyle.dynamic.DynamicBaseViewModel
import net.sdvn.common.vo.DynamicAttachment

/** 附件显示代理
 * @author Raleigh.Luo
 * date：20/11/25 21
 * describe：
 */
abstract class AttachmentDelegate<T> {
    companion object {
        @JvmStatic
        fun create(viewModel: DynamicBaseViewModel, tvAttach: TextView): AttachmentDelegate<DynamicAttachment> {
            return AttachmentDelegateImpl(viewModel,tvAttach)
        }

        @JvmStatic
        fun create(viewModel: DynamicBaseViewModel, itemView: View, ivAttachLogo: ImageView, tvAttachName: TextView, tvAttachDetail: TextView): AttachmentDelegate<DynamicAttachment> {
            return AttachmentDetailDelegateImpl(viewModel, itemView, ivAttachLogo, tvAttachName, tvAttachDetail)
        }
    }

    abstract fun show(obj: T)

    abstract fun setDefaultListener(context: Context, obj: T)
}