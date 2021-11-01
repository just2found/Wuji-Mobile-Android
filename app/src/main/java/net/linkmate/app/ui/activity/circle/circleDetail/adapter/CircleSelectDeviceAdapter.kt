package net.linkmate.app.ui.activity.circle.circleDetail.adapter

import android.content.DialogInterface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Observer
import com.rxjava.rxlife.RxLife
import io.reactivex.disposables.Disposable
import io.weline.devhelper.IconHelper
import kotlinx.android.synthetic.main.dialog_device_type.view.*
import net.linkmate.app.R
import net.linkmate.app.base.MyConstants
import net.linkmate.app.manager.DevManager
import net.linkmate.app.ui.activity.circle.circleDetail.CircleDetialViewModel
import net.linkmate.app.ui.activity.circle.circleDetail.FunctionHelper
import net.linkmate.app.util.FormDialogUtil
import net.linkmate.app.util.ToastUtils
import net.linkmate.app.view.FormRowLayout
import net.linkmate.app.view.HintDialog
import net.sdvn.cmapi.CMAPI
import net.sdvn.common.internet.core.HttpLoader
import net.sdvn.common.repo.NetsRepo
import net.sdvn.common.vo.BindDeviceModel
import net.sdvn.nascommon.SessionManager
import net.sdvn.nascommon.utils.SPUtils

/**选择设备／EN设备
 * @author Raleigh.Luo
 * date：20/8/14 11
 * describe：
 */
class CircleSelectDeviceAdapter(context: Fragment, val viewModel: CircleDetialViewModel, fragmentViewModel: CircleSelectDeviceViewModel)
    : DialogBaseAdapter<CircleSelectDeviceViewModel>(context, fragmentViewModel) {

    //上一层传过来已选中的deviceId
    private var defaultSelectedDeviceId: String? = null

    /**
     * extra:
     * is_manager  //是否是管理员或所有者
     * filter_device  过滤已有设备列表
     * network_name  圈子名称
     * network_owner 圈子所有者
     */
    init {
        //获取上一层传过来已选中的deviceId
        if (context.requireActivity().intent.hasExtra(FunctionHelper.EXTRA))
            defaultSelectedDeviceId = context.requireActivity().intent.getStringExtra(FunctionHelper.EXTRA)
        fragmentViewModel.isManager = context.requireActivity().intent.getBooleanExtra("is_manager", false)
        if (context.requireActivity().intent.hasExtra("filter_deviceIds"))
            fragmentViewModel.filterDeviceIds = context.requireActivity().intent.getSerializableExtra("filter_deviceIds") as List<String>?
        initObserver()
        fragmentViewModel.startGetApplyEnServerFee(null)
    }

    private fun initObserver() {
        viewModel.setLoadingStatus(true)
        fragmentViewModel.startFilterDevices()
        fragmentViewModel.ownFilteredDevices.observe(context, Observer {
            fragmentViewModel.startGetDeviceBriefs(it)
            viewModel.setLoadingStatus(false)
            notifyItemRangeChanged(0, itemCount, arrayListOf(1))
        })

        fragmentViewModel.deviceBriefs.observe(context, Observer {
            fragmentViewModel.initDeviceBriefsMap()
        })
        fragmentViewModel.refreshAdapter.observe(context, Observer {
            if (it == -1) {//刷新所有
                notifyItemRangeChanged(0, itemCount, arrayListOf(1))
            }
        })

        fragmentViewModel.applyEnServerFeeResult.observe(context, Observer {
            viewModel.setLoadingStatus(false)
            fragmentViewModel.startGetEnServerFee.value?.let {
                showConfirmDialog()
            }
        })
        fragmentViewModel.deviceJoinCircleResult.observe(context, Observer {
            if (it) {
                ToastUtils.showToast(context.getString(R.string.transfer_device_successful))
                context.requireActivity().setResult(FragmentActivity.RESULT_OK)
                viewModel.toFinishActivity()
            }
        })
        fragmentViewModel.setENServerResult.observe(context, Observer {
            if (it) {
                DevManager.getInstance().refreshENServerData(androidx.arch.core.util.Function {
//刷新EN服务器
                    NetsRepo.refreshNetList()//设置为En服务器
                            .setHttpLoaderStateListener(object : HttpLoader.HttpLoaderStateListener {
                                override fun onLoadComplete() {
                                    viewModel.setLoadingStatus(false)
                                    ToastUtils.showToast(context.getString(
                                            if (fragmentViewModel.isManager) R.string.add_en_service_successful else R.string.apply_en_success))
                                    context.requireActivity().setResult(FragmentActivity.RESULT_OK)
                                    viewModel.toFinishActivity()
                                }

                                override fun onLoadStart(disposable: Disposable?) {
                                }

                                override fun onLoadError() {
                                    viewModel.setLoadingStatus(false)
                                    ToastUtils.showToast(context.getString(
                                            if (fragmentViewModel.isManager) R.string.add_en_service_successful else R.string.apply_en_success))
                                    context.requireActivity().setResult(FragmentActivity.RESULT_OK)
                                    viewModel.toFinishActivity()
                                }

                            })
                    null
                })//设置为En服务器
            } else {
                viewModel.setLoadingStatus(false)
            }
        })
    }

    private fun showConfirmDialog() {
        fragmentViewModel.applyEnServerFeeResult.value?.let {
            val device: BindDeviceModel = fragmentViewModel.startGetEnServerFee.value!!
            if (it.size == 0) return
            val dates: MutableList<FormRowLayout.FormRowDate> = ArrayList()
            dates.add(FormRowLayout.FormRowDate(context.getString(R.string.circle_name), context.requireActivity().intent.getStringExtra("network_name")))
            dates.add(FormRowLayout.FormRowDate(context.getString(R.string.owner), context.requireActivity().intent.getStringExtra("network_owner")))
            dates.add(FormRowLayout.FormRowDate(context.getString(R.string.device), device.devName))
            dates.add(FormRowLayout.FormRowDate(context.getString(R.string.vadded_fee), it.get(0).getOwnShareText()))
            dates.add(FormRowLayout.FormRowDate(context.getString(R.string.service_fees), it.get(0).getValueText()))
            if (it.get(0).deposit ?: 0f > 0f) {//有质押金才显示
                dates.add(FormRowLayout.FormRowDate(context.getString(R.string.deposit_value), it.get(0).getDepositText()))
            }
            dates.add(FormRowLayout.FormRowDate(context.getString(R.string.required_points), it.get(0).getTotalText()))
            FormDialogUtil.showSelectDialog(context.requireContext(), R.string.add_en_server, dates,
                    R.string.confirm, { v, dialog ->
                dialog.dismiss()
                fragmentViewModel.startSetENServer(device.devId)
            }, R.string.cancel) { v, dialog -> dialog.dismiss() }
        }
    }

    private var hintDialog: HintDialog? = null
        get() {
            if (field == null) {
                field = HintDialog.newInstance(context.getString(R.string.confirm_selected_en_device_title),
                        if (fragmentViewModel.function == FunctionHelper.SELECT_EN_DEVICE) context.getString(R.string.transfer_en_device_hint) else null,
                        R.color.red,
                        confrimText = context.getString(R.string.confirm),
                        cancelText = context.getString(R.string.cancel))
                        .setOnClickListener(View.OnClickListener {
                            if (it.id == R.id.positive) {//确定按钮
                                fragmentViewModel.ownFilteredDevices.value?.get(mLastCheckedPosition)?.let {
                                    val id = it.devId
                                    when (fragmentViewModel.function) {
                                        FunctionHelper.SELECT_EN_DEVICE -> {//选择EN  - 新增
                                               if (fragmentViewModel.isManager) {//获取提供服务的费用并展示，管理员跳过此步骤
                                                fragmentViewModel.startSetENServer(id)
                                            } else {
                                                fragmentViewModel.startGetApplyEnServerFee(it)
                                            }
                                        }
                                        FunctionHelper.SELECT_OWN_DEVICE -> {//选择我的设备  - 新增
                                            fragmentViewModel.startDeviceJoinCircle(id)
                                        }
                                    }
                                }
                            }
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


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
                LayoutInflater.from(context.requireContext()).inflate(R.layout.dialog_device_type, null, false)
        view.layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return fragmentViewModel.ownFilteredDevices.value?.size ?: 0
    }

    //记录上个被选择的位置
    private var mLastCheckedPosition: Int = -1
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        with(holder.itemView) {
            setPaddingRelative(paddingLeft, if (position == 0) paddingLeft else 0, paddingLeft, paddingLeft)
            cbSelectDevice.setOnCheckedChangeListener(null)
            val device = fragmentViewModel.ownFilteredDevices.value?.get(position)
            val defualtIcon = device?.devClass?.let {
                IconHelper.getIconByeDevClass(it, true, true)
            } ?: let {
                io.weline.devhelper.R.drawable.icon_device_wz
            }
            if (getTag() != device?.devId) {//不同设备，清空tag
                ivDeviceIcon.setTag(null)
                tvDeviceName.setTag(null)
                setTag(device?.devId)
            }
            if (ivDeviceIcon.getTag() == null) ivDeviceIcon.setImageResource(defualtIcon)
            viewModel.loadBrief(device?.devId ?: "", fragmentViewModel.getBrief(device?.devId
                    ?: ""),
                    ivImage = ivDeviceIcon, defalutImage = defualtIcon)

            if (tvDeviceName.getTag() == null) tvDeviceName.setText(device?.devName)
            if (SPUtils.getBoolean(MyConstants.SP_SHOW_REMARK_NAME, true)) {
                val deviceModel = SessionManager.getInstance()
                        .getDeviceModel(device?.devId ?: "")
                if (deviceModel != null) {
                    if (tvDeviceName.getTag() == null) tvDeviceName.setText(deviceModel.devName)
                    deviceModel.devNameFromDB
                            .`as`(RxLife.`as`(holder.itemView))
                            .subscribe({ s: String? ->
                                if (tvDeviceName.getTag() != s) {
                                    tvDeviceName.setText(s)
                                }
                                tvDeviceName.setTag(tvDeviceName.text.toString())
                            }) { throwable: Throwable? -> }
                } else {
                    val text = device?.devName
                    if (tvDeviceName.getTag() != text) {
                        tvDeviceName.setText(text)
                        tvDeviceName.setTag(text)
                    }
                }
            } else {
                val text = device?.devName
                if (tvDeviceName.getTag() != text) {
                    tvDeviceName.setText(text)
                    tvDeviceName.setTag(text)
                }
            }

//            when (fragmentViewModel.function) {
//                FunctionHelper.SELECT_EN_DEVICE -> {//创建圈子-选择EN设备 确认
            tvUserName.text = CMAPI.getInstance().baseInfo.account
            //最后一项不显示底部线条
            viewItemFoot.visibility = if (position == itemCount - 1) View.GONE else View.VISIBLE
//                }
//            }
            //默认勾选选项
            val isChecked = defaultSelectedDeviceId == device?.devId
            if (mLastCheckedPosition == -1) {
                cbSelectDevice.isChecked = false
                if (isChecked) {
                    fragmentViewModel.updateViewStatusParams(bottomIsEnable = true)
                    mLastCheckedPosition = position
                }
            } else {
                cbSelectDevice.isChecked = mLastCheckedPosition == position
            }

            cbSelectDevice.setOnCheckedChangeListener { compoundButton, b ->
                if (b) {//选择了其他网络
                    if (fragmentViewModel.viewStatusParams.value?.bottomIsEnable != true) {
                        fragmentViewModel.updateViewStatusParams(bottomIsEnable = true)
                    }
                    val tempLastPositon = mLastCheckedPosition
                    mLastCheckedPosition = position
                    //取消上一个勾选
                    notifyItemChanged(tempLastPositon)
                } else {
                    //不能取消自己
                    cbSelectDevice.isChecked = true
                }
            }
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int, payloads: MutableList<Any>) {
        onBindViewHolder(holder, position)
    }

    override fun internalItemClick(view: View, position: Int) {
        if (view.id == R.id.btnBottomConfirm) {//底部按钮
            if (hintDialog?.dialog?.isShowing != true) {
                hintDialog?.show(context.requireActivity().getSupportFragmentManager(), "hintDialog")
            }
        }
    }

    override fun internalItemLongClick(view: View, position: Int) {
    }
}