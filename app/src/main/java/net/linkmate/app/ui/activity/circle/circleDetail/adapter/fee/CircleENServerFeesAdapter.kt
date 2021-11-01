package net.linkmate.app.ui.activity.circle.circleDetail.adapter.fee

import android.content.DialogInterface
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import kotlinx.android.synthetic.main.dialog_device_item_status.view.*
import net.linkmate.app.R
import net.linkmate.app.ui.activity.circle.circleDetail.CircleDetialViewModel
import net.linkmate.app.ui.activity.circle.circleDetail.adapter.DialogBaseAdapter
import net.linkmate.app.util.ToastUtils
import net.linkmate.app.view.EditDialog

/**
 * @author Raleigh.Luo
 * date：20/10/20 20
 * describe：
 */
class CircleENServerFeesAdapter(context: Fragment, val viewModel: CircleDetialViewModel, fragmentViewModel: CircleENServerFeesViewModel)
    : DialogBaseAdapter<CircleENServerFeesViewModel>(context, fragmentViewModel) {
    init {
        initObserver()
    }

    private fun initObserver() {
        fragmentViewModel.startRemoteRequest(viewModel.networkId)
        fragmentViewModel.fees.observe(context, Observer {
            notifyDataSetChanged()
        })
        fragmentViewModel.setShareResult.observe(context, Observer {
            //设置分成结果
            if (it) {
                fragmentViewModel.startRemoteRequest(viewModel.networkId)
            }

        })
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
                LayoutInflater.from(context.requireContext()).inflate(R.layout.dialog_device_item_status, null, false)
        view.layoutParams =
                ConstraintLayout.LayoutParams(
                        ConstraintLayout.LayoutParams.MATCH_PARENT,
                        ConstraintLayout.LayoutParams.WRAP_CONTENT
                )
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return if (fragmentViewModel.fees.value?.size ?: 0 > 0) 1 else 0
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        with(holder.itemView) {
            tvStatusTitle.text = context.getString(R.string.setting_en_setting_fees)
            val share = fragmentViewModel.fees.value?.get(0)?.owner_share ?: 0f
            val s = String.format("%.2f",share)
            tvStatusContent.text = "${s}%"
            ivStatusEdit.visibility = View.VISIBLE

            setOnClickListener {
                //是否已经被拦截处理
                val isInterceptor = onItemClickListener?.onClick(it, position) ?: false
                //没有拦截，则可以内部处理
                if (!isInterceptor) internalItemClick(it, position)
            }

        }
    }

    override fun internalItemClick(view: View, position: Int) {
        if (position >= 0) {
            fragmentViewModel.fees.value?.get(0)?.let {
                editDialog?.update(context.getString(R.string.setting_en_setting_fees_title),
                        originalText = String.format("%.2f",it.owner_share?:0f),
                        bottomHint = context.getString(R.string.max_owner_share_hint) + " " + it.owner_max.toString() + "%",
                        maxValue = it.owner_max,
                        confrimText = context.getString(R.string.confirm),
                        cancelText = context.getString(R.string.cancel),
                        inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL or InputType.TYPE_NUMBER_VARIATION_NORMAL
                )
                if (editDialog?.dialog?.isShowing != true) {
                    editDialog?.show(context.requireActivity().supportFragmentManager, "point")
                }
            }
        }
    }

    private var editDialog: EditDialog? = null
        get() {
            if (field == null) {
                //设置只能输入数字
                field = EditDialog.newInstance()
                field?.onClickListener = View.OnClickListener {
                    if(it.id == R.id.positive){
                        field?.getContent()?.let {
                            fragmentViewModel.startSetShare(it.toFloat())
                        }?:let{
                            ToastUtils.showToast(context.getString(R.string.input_max_owner_share_error_hint))
                        }
                    }

                }
                field?.onDismissListener = DialogInterface.OnDismissListener {
                    viewModel.setSecondaryDialogShow(false)
                }
                field?.onShowListener = DialogInterface.OnShowListener {
                    viewModel.setSecondaryDialogShow(true)
                }
            }
            return field
        }

    override fun internalItemLongClick(view: View, position: Int) {
    }
}


