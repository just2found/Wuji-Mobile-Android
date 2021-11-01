package net.linkmate.app.ui.activity.circle.circleDetail.adapter.device

import android.content.Intent
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Observer
import com.rxjava.rxlife.RxLife
import io.weline.devhelper.IconHelper
import kotlinx.android.synthetic.main.dialog_circle_en_device.view.*
import kotlinx.android.synthetic.main.dialog_circle_en_device.view.ivDeviceIcon
import kotlinx.android.synthetic.main.dialog_circle_en_device.view.tvUserName
import kotlinx.android.synthetic.main.dialog_circle_en_device.view.viewItemFoot
import kotlinx.android.synthetic.main.dialog_device_type.view.*
import net.linkmate.app.R
import net.linkmate.app.base.MyConstants
import net.linkmate.app.manager.UserInfoManager
import net.linkmate.app.ui.activity.circle.circleDetail.CircleDetialActivity
import net.linkmate.app.ui.activity.circle.circleDetail.CircleDetialViewModel
import net.linkmate.app.ui.activity.circle.circleDetail.FunctionHelper
import net.linkmate.app.ui.activity.circle.circleDetail.adapter.DialogBaseAdapter
import net.linkmate.app.ui.viewmodel.DevCommonViewModel
import net.linkmate.app.view.HintDialog
import net.sdvn.nascommon.SessionManager
import net.sdvn.nascommon.utils.SPUtils

/**
 * @author Raleigh.Luo
 * date：20/10/12 16
 * describe：
 */
class CircleOwnDeviceAdapter(context: Fragment, val viewModel: CircleDetialViewModel, fragmentViewModel: CircleOwnDeviceViewModel)
    : DialogBaseAdapter<CircleOwnDeviceViewModel>(context, fragmentViewModel) {
    //    val userId = CMAPI.getInstance().baseInfo.userId

    //初始化
    init {
        if (viewModel.circleDetail.value?.ischarge ?: false) {//付费圈子
            //设置头部显示圈子名称
            fragmentViewModel.updateViewStatusParams(headerTitle = viewModel.circleDetail.value?.networkname,
                    bottomTitle = context.getString(R.string.add_en_server),
                    bottomAddTitle = context.getString(R.string.add_circle_new_device))
        } else {//免费圈子，旧圈子
            //设置头部显示圈子名称
            fragmentViewModel.updateViewStatusParams(headerTitle = viewModel.circleDetail.value?.networkname,
                    bottomTitle = context.getString(R.string.add_circle_new_device))
        }
        initObserver()
        fragmentViewModel.startGetDevices()
    }

    /**
     * 观察器
     */
    private fun initObserver() {
        fragmentViewModel.devices.observe(context, Observer {
            fragmentViewModel.startGetDeviceBriefs(it)
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
        viewModel.activityResult.observe(context, Observer {
            if (it.resultCode == FragmentActivity.RESULT_OK) {
                when (it.requestCode) {
                    FunctionHelper.CIRCLE_OWN_DEVICE_DETAIL -> {
                        //刷新圈子详情－取消EN后，需更新EN数量－及时更新限制问题
                        viewModel.startRequestCircleDetail()
                        //刷新数据
                        fragmentViewModel.startGetDevices()
                    }
                    FunctionHelper.SELECT_EN_DEVICE,
                    FunctionHelper.SELECT_OWN_DEVICE
                    -> {
                        //刷新数据
                        fragmentViewModel.startGetDevices()
                    }
                }
            }
        })
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
                LayoutInflater.from(context.requireContext()).inflate(R.layout.dialog_circle_en_device, null, false)
        view.layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return fragmentViewModel.devices.value?.size ?: 0
    }
    private val mDevCommonViewModel = DevCommonViewModel()
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        with(holder.itemView) {
            val index = position
            setPaddingRelative(paddingLeft, if (index == 0) paddingLeft else 0, paddingLeft, paddingLeft)
            fragmentViewModel.devices.value?.get(index)?.let {
                val defualtIcon = it?.deviceclass?.let {
                    IconHelper.getIconByeDevClass(it, true, true)
                } ?: let {
                    io.weline.devhelper.R.drawable.icon_device_wz
                }

                if(getTag()!=it.deviceid){//不同设备，清空tag
                    ivDeviceIcon.setTag(null)
                    tvENName.setTag(null)
                    setTag(it.deviceid)
                }
                if (ivDeviceIcon.getTag() == null) ivDeviceIcon.setImageResource(defualtIcon)
                viewModel.loadBrief(it.deviceid ?: "", fragmentViewModel.getBrief(it.deviceid
                        ?: ""),
                        ivImage = ivDeviceIcon, defalutImage = defualtIcon)
                if(tvENName.getTag()==null)  tvENName.setText(it?.devicename)
                if (SPUtils.getBoolean(MyConstants.SP_SHOW_REMARK_NAME, true)) {
                    val deviceModel = SessionManager.getInstance()
                            .getDeviceModel(it?.deviceid?:"")
                    if (deviceModel != null) {
                        if (tvENName.getTag() == null) tvENName.setText(deviceModel.devName)
                        deviceModel.devNameFromDB
                                .`as`(RxLife.`as`(holder.itemView))
                                .subscribe({ s: String? ->
                                    if (tvENName.getTag() != s) {
                                        tvENName.setText(s)
                                    }
                                    tvENName.setTag(tvENName.text.toString())
                                }) { throwable: Throwable? -> }
                    } else {
                        val text = it.devicename
                        if (tvENName.getTag() != text) {
                            tvENName.setText(text)
                            tvENName.setTag(text)
                        }
                    }
                } else {
                    val text = it.devicename
                    if (tvENName.getTag() != text) {
                        tvENName.setText(text)
                        tvENName.setTag(text)
                    }
                }
                val bean = UserInfoManager.getInstance().userInfoBean
                //显示自己的帐号
                tvUserName.text = if (TextUtils.isEmpty(bean?.nickname)) (bean?.loginname?:"") else bean?.nickname
                //最后一项不显示底部线条
                viewItemFoot.visibility = if (position == itemCount - 1) View.GONE else View.VISIBLE

                if (it.isen ?: false && it.srvprovide ?: false && it.isEnable() ?: true == false) {//EN服务器禁用
                    tvForbidden.visibility = View.VISIBLE
                    tvForbidden.setTextColor(context.resources.getColor(R.color.red))
//                            tvForbidden.setTextColor(context.resources.getColor(R.color.darker_gray))
                    tvForbidden.text = it.getStatusName()
                } else {
                    tvForbidden.text = ""
                    tvForbidden.visibility = View.GONE
                }
                ivPartner.visibility = if (it.isen ?: false && it.srvprovide ?: false) View.VISIBLE else View.GONE
                if (it.isen ?: false) {
                    setOnClickListener {
                        //是否已经被拦截处理
                        val isInterceptor = onItemClickListener?.onClick(it, position) ?: false
                        //没有拦截，则可以内部处理
                        if (!isInterceptor) internalItemClick(it, position)
                    }
                } else {
                    setOnClickListener(null)
                }

            }
        }
    }

    override fun internalItemLongClick(view: View, position: Int) {
    }

    override fun internalItemClick(view: View, position: Int) {

        if (viewModel.circleDetail.value?.isNormalUser() ?: false) {//当前用户必须为正常用户
            when {
                (view.id == R.id.btnBottomAdd || (view.id == R.id.btnBottomConfirm && TextUtils.isEmpty(fragmentViewModel.viewStatusParams.value?.bottomAddTitle))) -> {
                    viewModel.circleDetail.value?.let {
//                        if (it.isNormalUser() && fragmentViewModel.devices.value?.size ?: 0 < viewModel.circleDetail.value?.getAccountDeviceMax() ?: 0) {
                            //已转移设备数未达到每个用户上限 才可加入
                            //叠加弹框阴影效果
                            viewModel.setSecondaryDialogShow(true)
                            //二级弹框－选择EN设备
                        CircleDetialActivity.startActivityForResult(context.requireActivity(),Intent(context.requireContext(), CircleDetialActivity::class.java)
                                    .putExtra(FunctionHelper.FUNCTION, FunctionHelper.SELECT_OWN_DEVICE)
                                    .putExtra(FunctionHelper.NETWORK_ID, viewModel.networkId)
                                    .putExtra("is_manager", viewModel.circleDetail.value?.isOwner() ?: false || viewModel.circleDetail.value?.isManager() ?: false)
                                    .putExtra("filter_deviceIds", fragmentViewModel.getDeviceIds())
                                    , FunctionHelper.SELECT_OWN_DEVICE)
//                        } else {
//                            if (hintDialog?.dialog?.isShowing != true) {
//                                hintDialog?.update(context.getString(if (it.isNormalUser()) R.string.over_max_account_device_hint else R.string.account_exception))
//                                hintDialog?.show(context.requireActivity().getSupportFragmentManager(), "hintDialog")
//                            }
//                        }
                    }


                }
                view.id == R.id.btnBottomConfirm -> {
                    viewModel.circleDetail.value?.let {
                        val current = it.networkprops?.provide_count ?: 0
                        var max = 0
                        val maxENNumber = it.networkprops?.network_scale?.filter {
                            it.key == "provide_max"
                        }
                        if (maxENNumber != null && maxENNumber.size > 0) {
                            max = maxENNumber?.get(0)?.value?.toIntOrNull() ?: 0
                        }
//                        if (it.isNormalUser() && current < max) {//当前用户为非正常用户,禁用添加按钮
                            viewModel.setSecondaryDialogShow(true)
                            //二级弹框－选择EN设备
                        CircleDetialActivity.startActivityForResult(context.requireActivity(),Intent(context.requireContext(), CircleDetialActivity::class.java)
                                    .putExtra(FunctionHelper.FUNCTION, FunctionHelper.SELECT_EN_DEVICE)
                                    .putExtra(FunctionHelper.NETWORK_ID, viewModel.networkId)
                                    .putExtra("filter_deviceIds", fragmentViewModel.getEnDeviceIds())
                                    .putExtra("is_manager", viewModel.circleDetail.value?.isOwner() ?: false || viewModel.circleDetail.value?.isManager() ?: false)
                                    .putExtra("network_name", viewModel.circleDetail.value?.networkname)
                                    .putExtra("network_owner", viewModel.circleDetail.value?.getFullName())
                                    , FunctionHelper.SELECT_EN_DEVICE)
//                        } else {
//                            if (hintDialog?.dialog?.isShowing != true) {
//                                hintDialog?.update(context.getString(if (it.isNormalUser()) R.string.over_max_account_device_hint else R.string.account_exception))
//                                hintDialog?.show(context.requireActivity().getSupportFragmentManager(), "hintDialog")
//                            }
//                        }
                    }
                }

                else -> {
                    if (position >= 0) {
                        fragmentViewModel.devices.value?.get(position)?.let {
                            if (it.srvprovide ?: false && it.isen ?: false) {//en服务器才有详情
                                CircleDetialActivity.startActivityForResult(context.requireActivity(),Intent(context.requireContext(), CircleDetialActivity::class.java)
                                        .putExtra(FunctionHelper.FUNCTION, FunctionHelper.CIRCLE_OWN_DEVICE_DETAIL)
                                        .putExtra(FunctionHelper.EXTRA_ENTITY, fragmentViewModel.devices.value?.get(position))
                                        .putExtra(FunctionHelper.NETWORK_ID, viewModel.networkId)
                                        , FunctionHelper.CIRCLE_OWN_DEVICE_DETAIL)
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * 创建提示Dialog
     */
    private var hintDialog: HintDialog? = null
//        get() {
//            if (field == null) {
//                field = HintDialog.newInstance(context.getString(R.string.over_max_en_hint))
//                        .setOnClickListener(View.OnClickListener {
//                        })
//                field?.onDismissListener = DialogInterface.OnDismissListener {
//                    viewModel.setSecondaryDialogShow(false)
//                }
//                field?.onShowListener = DialogInterface.OnShowListener {
//                    viewModel.setSecondaryDialogShow(true)
//                }
//            }
//            return field
//        }

}