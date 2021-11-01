package net.linkmate.app.ui.activity.circle.circleDetail.adapter

import android.content.DialogInterface
import android.content.Intent
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Observer
import kotlinx.android.synthetic.main.dialog_device_item_status.view.*
import kotlinx.android.synthetic.main.dialog_device_item_switch.view.*
import net.linkmate.app.R
import net.linkmate.app.base.MyConstants
import net.linkmate.app.data.model.CircleManagerFees
import net.linkmate.app.ui.activity.circle.circleDetail.CircleDetialViewModel
import net.linkmate.app.ui.activity.circle.circleDetail.FunctionHelper
import net.linkmate.app.view.EditDialog


/** 设置付费详情
 * @author Raleigh.Luo
 * date：20/8/17 20
 * describe：
 */
class CircleSettingFeesDetialAdapter(context: Fragment, val viewModel: CircleDetialViewModel,
                                     fragmentViewModel: CircleSettingFeesDetialViewModel)
    : DialogBaseAdapter<CircleSettingFeesDetialViewModel>(context, fragmentViewModel) {

    init {
        if (context.requireActivity().intent.hasExtra(FunctionHelper.EXTRA_ENTITY)) {
            fragmentViewModel.fee = context.requireActivity().intent.getSerializableExtra(FunctionHelper.EXTRA_ENTITY) as CircleManagerFees.Fee
            fragmentViewModel.updateViewStatusParams(headerTitle = fragmentViewModel.fee?.title)
            initObserver()
        } else { //没有传值，直接返回
            viewModel.toFinishActivity()
        }
    }

    private val isEnableEdit = fragmentViewModel.fee?.vaddable ?: false

    private fun initObserver() {
        fragmentViewModel.alterVaddResult.observe(context, Observer {
            editDialog?.dismiss()
            if (it) {
                context.requireActivity().setResult(FragmentActivity.RESULT_OK,
                        Intent().putExtra("vadd_value", fragmentViewModel.alterVaddValue.value))
                viewModel.toFinishActivity()
            }
        })
        fragmentViewModel.enableResult.observe(context, Observer {
            if (it) {
                context.requireActivity().setResult(FragmentActivity.RESULT_OK,
                        Intent().putExtra("enable", fragmentViewModel.isEnable.value))
                viewModel.toFinishActivity()
            } else {
                notifyDataSetChanged()
            }
        })

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layout = if (viewType == TYPE_SWITCH) R.layout.dialog_device_item_switch else R.layout.dialog_device_item_status
        val view =
                LayoutInflater.from(context.requireContext()).inflate(layout, null, false)
        view.layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT)
        return ViewHolder(view)
    }

    override fun getItemViewType(position: Int): Int {

        when {
            isEnableEdit -> {//显示启动禁用
                return if (position == 0) TYPE_SWITCH else TYPE_STATUS
            }
            else -> {//不显示启动禁用
                return TYPE_STATUS
            }
        }
    }

    /**
     * 是否显示基础费
     *
     * :实时流量不显示基础费用
     */
    private fun isShowBaseFee(): Boolean {
        //是否是实时流量
        val isRealTimeFlow = fragmentViewModel.fee?.isFilterFlow() ?: false
        return !isRealTimeFlow
    }

    /**
     * 是否显示增值费
     */
    private fun isShowVaddFee(): Boolean {
        return fragmentViewModel.fee?.vadd != null
    }

    override fun getItemCount(): Int {

        //是否显示启用禁用开关
        val enableSwitchItem = if (isEnableEdit) 1 else 0
        //是否显示基础费- 实时流量不显示基础费用
        val baseFeeItem = if (isShowBaseFee()) 1 else 0
        //是否显示增值费
        val vaddFeeItem = if (isShowVaddFee()) 1 else 0

        return enableSwitchItem + baseFeeItem + vaddFeeItem
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        when (getItemViewType(position)) {
            TYPE_SWITCH -> {
                with(holder.itemView) {
                    mSwitch.text = context.getText(R.string.is_enable_this_way)
                    mSwitch.isChecked = fragmentViewModel.fee?.enable ?: false
                    mSwitch.setOnCheckedChangeListener { compoundButton, b ->
                        fragmentViewModel.startEnable(b)
                    }
                }
            }
            TYPE_STATUS -> {
                with(holder.itemView) {
                    if (isShowBaseFee() && position == if (isEnableEdit) 1 else 0) {
                        ivStatusEdit.setTag(position)
                        tvStatusTitle.setText(R.string.basic_fee)
                        val unit = if (fragmentViewModel.fee?.isFilterFlow() ?: false)
                            context.getString(R.string.fmt_traffic_unit_price2)
                                    .replace("\$TRAFFIC\$", MyConstants.DEFAULT_UNIT) else context.getString(R.string.score)
                        tvStatusContent.setText(fragmentViewModel.fee?.basic?.getValueText() + " " + unit)
                    } else {
                        ivStatusEdit.setTag(position)
                        tvStatusTitle.setText(R.string.increment_fee)
                        val unit = if (fragmentViewModel.fee?.isFilterFlow() ?: false)
                            context.getString(R.string.fmt_traffic_unit_price2)
                                    .replace("\$TRAFFIC\$", MyConstants.DEFAULT_UNIT) else context.getString(R.string.score)
                        tvStatusContent.setText(fragmentViewModel.fee?.vadd?.getValueText() + " " + unit)
                        //默认要为true
                        ivStatusEdit.visibility = if (fragmentViewModel.fee?.editable == true) View.VISIBLE else View.GONE
                    }
                    ivStatusEdit.setOnClickListener {
                        //是否已经被拦截处理
                        val isInterceptor = onItemClickListener?.onClick(it, position) ?: false
                        //没有拦截，则可以内部处理
                        if (!isInterceptor) internalItemClick(it, position)
                    }
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
                    if (it.id == R.id.positive) {
                        field?.getContent()?.let {
                            fragmentViewModel.startAlterVaddValue(it.toFloat())
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

    override fun internalItemClick(view: View, position: Int) {
        if (view.id == R.id.ivStatusEdit) {
            editDialog?.update(context.getString(R.string.increment_fee), context.getString(R.string.increment_fee),
                    fragmentViewModel.fee?.vadd?.getValueText(),

                    inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL or InputType.TYPE_NUMBER_VARIATION_NORMAL,
                    maxValue = 10000000000f)
            if (editDialog?.dialog?.isShowing != true) {
                editDialog?.show(context.requireActivity().supportFragmentManager, "point")
            }
        }
    }

    override fun internalItemLongClick(view: View, position: Int) {
    }
}