package net.linkmate.app.ui.simplestyle.dynamic.delegate

import android.view.View
import android.widget.TextView
import androidx.fragment.app.FragmentManager
import net.linkmate.app.R
import net.linkmate.app.base.MyApplication
import net.linkmate.app.service.DynamicQueue
import net.linkmate.app.ui.simplestyle.dynamic.DynamicBaseViewModel
import net.linkmate.app.view.HintDialog
import net.sdvn.common.vo.Dynamic

/**
 * @author Raleigh.Luo
 * date：20/12/28 14
 * describe：
 */
class DeleteDelegateImpl(val viewModel: DynamicBaseViewModel, val tvDelete: TextView) : DeleteDelegate<Dynamic>() {
    private var hintDialog: HintDialog? = null
        get() {
            if (field == null) {
                val context = MyApplication.getContext()
                field = HintDialog.newInstance(title = context.getString(R.string.tips),
                        content = context.getString(R.string.delete_dynamic_confirm_hint),
                        confrimText = context.getString(R.string.confirm),
                        cancelText = context.getString(R.string.cancel))
                        .setOnClickListener(View.OnClickListener {
                            if (it.id == R.id.positive) {//确定按钮
                                viewModel.startDeleteDynamic(optDynamicAutoIncreaseId)
                            }
                        })
            }
            return field
        }

    private var optDynamicAutoIncreaseId: Long = 0


    override fun show(manager: FragmentManager?, obj: Dynamic?) {
        obj?.let {
            if (DynamicQueue.isCircleOwner || DynamicQueue.mLastUserId == obj.UID) {//仅圈主可删除
                tvDelete.visibility = View.VISIBLE
                tvDelete.setOnClickListener {
                    optDynamicAutoIncreaseId = obj.autoIncreaseId
                    hintDialog?.show(manager!!, "hintDialog")
                }
            } else {
                tvDelete.visibility = View.GONE
                tvDelete.setOnClickListener(null)
            }
        } ?: let {
            tvDelete.visibility = View.GONE
            tvDelete.setOnClickListener(null)
        }
    }
}