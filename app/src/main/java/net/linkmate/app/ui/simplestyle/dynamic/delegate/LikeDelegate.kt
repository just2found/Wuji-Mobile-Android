package net.linkmate.app.ui.simplestyle.dynamic.delegate

import android.content.Context
import net.linkmate.app.ui.simplestyle.dynamic.DynamicBaseViewModel
import net.linkmate.app.view.CusTextView
import net.sdvn.common.vo.DynamicLike

/**
 * @author Raleigh.Luo
 * date：20/12/28 13
 * describe：
 */
abstract class LikeDelegate<T> {
    companion object{
        fun create(viewModel: DynamicBaseViewModel, tvLike: CusTextView):LikeDelegate<List<DynamicLike>>{
            return LikeDelegateImpl(viewModel,tvLike)
        }
    }

    abstract fun show(dynamicId: Long?,obj: T?)

    abstract fun setDefaultListener(context: Context, dynamicId: Long?, dynamicAutoIncreaseId: Long?, obj: T?)
}