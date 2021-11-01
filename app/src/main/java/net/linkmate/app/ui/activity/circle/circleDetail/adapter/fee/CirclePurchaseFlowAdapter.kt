package net.linkmate.app.ui.activity.circle.circleDetail.adapter.fee

import android.content.DialogInterface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Observer
import kotlinx.android.synthetic.main.dialog_circle_type.view.*
import net.linkmate.app.R
import net.linkmate.app.ui.activity.circle.circleDetail.CircleDetialViewModel
import net.linkmate.app.ui.activity.circle.circleDetail.adapter.DialogBaseAdapter
import net.linkmate.app.util.FormDialogUtil
import net.linkmate.app.view.FormRowLayout
import net.linkmate.app.view.HintDialog

/** 选购流量
 * @author Raleigh.Luo
 * date：20/10/17 11
 * describe：
 */
class CirclePurchaseFlowAdapter(context: Fragment, val viewModel: CircleDetialViewModel, fragmentViewModel: CirclePurchaseFlowViewModel)
    : DialogBaseAdapter<CirclePurchaseFlowViewModel>(context, fragmentViewModel) {
    init {
        initObserver()
    }

    private var isNotCircleManager = false
    private fun initObserver() {
        if (viewModel.circleDetail.value == null) {//非圈子管理进入
            isNotCircleManager = true
            viewModel.startRequestCircleDetail()
        }
        viewModel.circleDetail.observe(context, Observer {
            fragmentViewModel.updateViewStatusParams(headerTitle = it.networkname)
        })
        fragmentViewModel.feeType.observe(context, Observer {

        })
        fragmentViewModel.fees.observe(context, Observer {
            notifyDataSetChanged()
        })
        fragmentViewModel.purchaseResult.observe(context, Observer {
            if (it) {
                if (isNotCircleManager) {
                    //	提交成功后刷新列表中的圈子状态
                    context.requireActivity().setResult(FragmentActivity.RESULT_OK)
                    viewModel.toFinishActivity()
                } else {
                    //刷新圈子列表
                    context.requireActivity().setResult(FragmentActivity.RESULT_OK)
                    //表示 从圈子管理中进入,刷新
                    viewModel.startRequestCircleDetail()
                    viewModel.toBackPress()
                }
            }
        })
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
                LayoutInflater.from(context.requireContext()).inflate(R.layout.dialog_circle_type, null, false)
        view.layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return fragmentViewModel.fees.value?.size ?: 0
    }

    //记录上个被选择的位置
    private var mLastCheckedPosition: Int = -1

    private fun getSubPanel(title: String, content: String): FormRowLayout {
        val frl = FormRowLayout(context.requireContext())
        frl.title.text = title
        frl.content.text = content
        return frl
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        with(holder.itemView) {
            llCircleTypeForm.removeAllViews()
            cbCircleTypeCheck.setOnCheckedChangeListener(null)
            setPaddingRelative(paddingLeft, if (position == 0) paddingLeft else 0, paddingLeft, paddingLeft)
            fragmentViewModel.fees.value?.get(position)?.let {
                tvCircleTypeName.text = it.title
                llCircleTypeForm.addView(getSubPanel(context.getString(R.string.use_life), it.getDurationText()))
                /**
                 * 对
                feettype为流量费类型flow-net或flow-dev或flow-netdev,
                且
                feeperiod为实时流量flow
                的收费项:
                1.在选购流量、我的收费项中，不显示其所需积分；
                2.在设置收费项中，列表上不显示其所需积分；数据中的value值分别对应其基本费、增值费的流量单价，在选择收费项设置的弹窗内为基本费、增值费的value加上 GB / 积分 的单位
                 */
                if (!it.isFilterFlow(fragmentViewModel.feeType.value?.feetype ?: "")) {
                    llCircleTypeForm.addView(getSubPanel(context.getString(R.string.required_points), it.getValueText()))
                }

                var isChecked = mLastCheckedPosition == position
                cbCircleTypeCheck.isChecked = isChecked
                cbCircleTypeCheck.setBackgroundResource(
                        if (cbCircleTypeCheck.isChecked) R.drawable.bg_item_dev_stroke else R.drawable.bg_item_dev)
                ivTypeChecked.visibility = if (cbCircleTypeCheck.isChecked) View.VISIBLE else View.GONE
                cbCircleTypeCheck.setOnCheckedChangeListener { compoundButton, b ->
                    if (b) {
                        //底部按钮可用
                        if (fragmentViewModel.viewStatusParams.value?.bottomIsEnable ?: false == false) {
                            fragmentViewModel.updateViewStatusParams(bottomIsEnable = true)
                        }
                        val tempLastPositon = mLastCheckedPosition
                        mLastCheckedPosition = position
                        //取消上一个勾选
                        notifyItemChanged(tempLastPositon)
                    } else {
                        compoundButton.isChecked = true
                    }
                    cbCircleTypeCheck.setBackgroundResource(
                            if (cbCircleTypeCheck.isChecked) R.drawable.bg_item_dev_stroke else R.drawable.bg_item_dev)
                    ivTypeChecked.visibility = if (cbCircleTypeCheck.isChecked) View.VISIBLE else View.GONE
                }
            }
        }
    }

    override fun internalItemClick(view: View, position: Int) {
        if (view.id == R.id.btnBottomConfirm) {//购买
            fragmentViewModel.feeType.value?.let {
                if (it.expireable ?: false == true) {//已购买过流量的圈子用户,有生效中的
                    if (hintDialog?.dialog?.isShowing != true) {
                        hintDialog?.show(context.requireActivity().getSupportFragmentManager(), "hintDialog")
                    }
                } else {
                    fragmentViewModel.expire_renew = null
                    showConfirmDialog()
                }
            }
        }
    }

    private fun showConfirmDialog() {
        if (mLastCheckedPosition == -1) return
        fragmentViewModel.fees.value?.get(mLastCheckedPosition)?.let {
            val dates: MutableList<FormRowLayout.FormRowDate> = ArrayList()
            dates.add(FormRowLayout.FormRowDate(context.getString(R.string.circle_name), viewModel.circleDetail.value?.networkname))
            dates.add(FormRowLayout.FormRowDate(context.getString(R.string.owner), viewModel.circleDetail.value?.getFullName()))
            dates.add(FormRowLayout.FormRowDate(context.getString(R.string.use_life), it.getDurationText()))
            if (!it.isFilterFlow(fragmentViewModel.feeType.value?.feetype ?: "")) {
                dates.add(FormRowLayout.FormRowDate(context.getString(R.string.required_points), it.getValueText()))
            }
            FormDialogUtil.showSelectDialog(context.requireContext(), it.title?:"", dates,
                    context.getString(R.string.confirm), { v, dialog ->
                dialog.dismiss()
                fragmentViewModel.startPurchase(it)
            }, context.getString(R.string.cancel)) { v, dialog -> dialog.dismiss() }
        }
    }

    private var hintDialog: HintDialog? = null
        get() {
            if (field == null) {
                field = HintDialog.newInstance(context.getString(R.string.purchase_flow_expireable_title),
                        context.getString(R.string.purchase_flow_expireable_content),
                        R.color.red,
                        confrimText = context.getString(R.string.continuous_use),
                        cancelText = context.getString(R.string.replace_now)
                        )
                        .setOnClickListener(View.OnClickListener {
                            if (it.id == R.id.positive) {//确定按钮
                                fragmentViewModel.expire_renew = true
                            } else {
                                fragmentViewModel.expire_renew = false

                            }
                            showConfirmDialog()
                        })
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