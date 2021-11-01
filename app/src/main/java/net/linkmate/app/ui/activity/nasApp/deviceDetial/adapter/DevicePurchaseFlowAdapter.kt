package net.linkmate.app.ui.activity.nasApp.deviceDetial.adapter

import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.arch.core.util.Function
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import io.weline.repo.files.constant.AppConstants
import kotlinx.android.synthetic.main.dialog_circle_type.view.*
import net.linkmate.app.R
import net.linkmate.app.base.DevBoundType
import net.linkmate.app.manager.DevManager
import net.linkmate.app.ui.activity.nasApp.deviceDetial.DeviceViewModel
import net.linkmate.app.ui.activity.nasApp.deviceDetial.FunctionHelper
import net.linkmate.app.util.FormDialogUtil
import net.linkmate.app.view.FormRowLayout
import net.linkmate.app.view.HintDialog
import net.sdvn.common.data.model.CircleDevice
import net.sdvn.common.internet.core.GsonBaseProtocol
import net.sdvn.common.internet.listener.ResultListener
import net.sdvn.common.internet.protocol.HardWareInfo
import net.sdvn.nascommon.iface.EBRefreshHardWareDevice
import org.greenrobot.eventbus.EventBus

/**购买流量/购买加入费 使用费
 *
 * DEVICE_PURCHASE_FLOW
 * DEVICE_PURCHASE_JOIN
 *
 * @author Raleigh.Luo
 * date：20/10/18 17
 * describe：
 */
class DevicePurchaseFlowAdapter(context: Fragment, fragmentViewModel: DevicePurchaseFlowViewModel,
                                viewModel: DeviceViewModel)
    : DeviceBaseAdapter<DevicePurchaseFlowViewModel>(context, fragmentViewModel, viewModel) {

    init {
        fragmentViewModel.updateViewStatusParams(bottomIsEnable = false)
        if (context.requireActivity().intent.hasExtra(AppConstants.SP_FIELD_DEVICE_ID))
            fragmentViewModel.deviceId = context.requireActivity().intent.getStringExtra(AppConstants.SP_FIELD_DEVICE_ID)
        else {
            fragmentViewModel.deviceId = viewModel.device.id
        }
        if (context.requireActivity().intent.hasExtra(AppConstants.SP_FIELD_DEVICE_NAME)) {
            fragmentViewModel.updateViewStatusParams(headerTitle = context.requireActivity().intent.getStringExtra(AppConstants.SP_FIELD_DEVICE_NAME))
        }
        initObserver()
    }

    private fun initObserver() {
        fragmentViewModel.fees.observe(context, Observer {
            notifyDataSetChanged()
        })
        fragmentViewModel.feeType.observe(context, Observer {

        })
        fragmentViewModel.purchaseResult.observe(context, Observer {
            if (it) {
                viewModel.setLoadingStatus(true)
                var refreshENServerCompleted = false
                var initHardWareListCompleted = false
                val close = {
                    if (refreshENServerCompleted && initHardWareListCompleted) {
                        viewModel.setLoadingStatus(false)
                        if (TextUtils.isEmpty(viewModel.device.id)) {//非设备管理进入
                            viewModel.toFinishActivity()
                        } else {
                            viewModel.toBackPress()
                        }
                    }
                }
                //刷新数据
                DevManager.getInstance().refreshENServerData(Function {
                    refreshENServerCompleted = true
                    close()
                    null
                })
                DevManager.getInstance().initHardWareList(object : ResultListener<HardWareInfo> {
                    override fun error(tag: Any?, baseProtocol: GsonBaseProtocol?) {
                        initHardWareListCompleted = true
                        close()
                    }

                    override fun success(tag: Any?, data: HardWareInfo?) {
                        initHardWareListCompleted = true
                        close()
                    }
                })
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

    //记录上个被选择的位置o
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
                if (!it.isFilterFlow(fragmentViewModel.feeType.value?.feetype ?: ""))
                    llCircleTypeForm.addView(getSubPanel(context.getString(R.string.required_points), it.getValueText()))

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
            dates.add(FormRowLayout.FormRowDate(context.getString(R.string.dev_name), fragmentViewModel.viewStatusParams.value?.headerTitle))
            var name = viewModel.device.hardData?.nickname
            if (TextUtils.isEmpty(name)) name = viewModel.device.ownerName
            dates.add(FormRowLayout.FormRowDate(context.getString(R.string.owner), name))
            dates.add(FormRowLayout.FormRowDate(context.getString(R.string.use_life), it.getDurationText()))
            if (!it.isFilterFlow(fragmentViewModel.feeType.value?.feetype ?: ""))
                dates.add(FormRowLayout.FormRowDate(context.getString(R.string.required_points), it.getValueText()))
            FormDialogUtil.showSelectDialog(context.requireContext(),
                    if (fragmentViewModel.function == FunctionHelper.DEVICE_PURCHASE_FLOW) R.string.purchase_flow_fee_ways
                    else R.string.device_purchase_join_fees
                    , dates,
                    R.string.confirm, { v, dialog ->
                dialog.dismiss()
                fragmentViewModel.startPurchase(it)
            }, R.string.cancel) { v, dialog -> dialog.dismiss() }
        }
    }

    private var hintDialog: HintDialog? = null
        get() {
            if (field == null) {
                field = HintDialog.newInstance(context.getString(R.string.purchase_flow_expireable_title),
                        context.getString(R.string.purchase_flow_expireable_content),
                        R.color.red,
                        confrimText = context.getString(R.string.continuous_use),
                        cancelText = context.getString(R.string.replace_now))
                        .setOnClickListener(View.OnClickListener {
                            if (it.id == R.id.positive) {//确定按钮
                                fragmentViewModel.expire_renew = true
                            } else {
                                fragmentViewModel.expire_renew = false
                            }
                            showConfirmDialog()
                        })
            }
            return field
        }

    override fun internalItemLongClick(view: View, position: Int) {
    }
}