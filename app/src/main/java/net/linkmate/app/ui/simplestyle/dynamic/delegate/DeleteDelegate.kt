package net.linkmate.app.ui.simplestyle.dynamic.delegate

import android.widget.TextView
import androidx.fragment.app.FragmentManager
import net.linkmate.app.ui.simplestyle.dynamic.DynamicBaseViewModel
import net.sdvn.common.vo.Dynamic

/**
 * @author Raleigh.Luo
 * date：20/12/28 14
 * describe：
 */
abstract class DeleteDelegate<T> {
    companion object {
        @JvmStatic
        fun create(viewModel: DynamicBaseViewModel, tvDelete: TextView): DeleteDelegate<Dynamic> {
            return DeleteDelegateImpl(viewModel, tvDelete)
        }
    }

    abstract fun show(manager: FragmentManager?, obj: T?)
}