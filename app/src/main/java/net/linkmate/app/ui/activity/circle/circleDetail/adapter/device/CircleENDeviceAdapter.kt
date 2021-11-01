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
import kotlinx.android.synthetic.main.dialog_circle_en_device_count.view.*
import kotlinx.android.synthetic.main.dialog_device_type.view.*
import net.linkmate.app.R
import net.linkmate.app.base.MyConstants
import net.linkmate.app.ui.activity.circle.circleDetail.CircleDetialActivity
import net.linkmate.app.ui.activity.circle.circleDetail.CircleDetialViewModel
import net.linkmate.app.ui.activity.circle.circleDetail.FunctionHelper
import net.linkmate.app.ui.activity.circle.circleDetail.adapter.DialogBaseAdapter
import net.linkmate.app.ui.viewmodel.DevCommonViewModel
import net.linkmate.app.view.HintDialog
import net.sdvn.nascommon.SessionManager
import net.sdvn.nascommon.utils.SPUtils

/**en设备管理 列表
 * @author Raleigh.Luo
 * date：20/8/14 16
 * describe：
 */
class CircleENDeviceAdapter(context: Fragment, val viewModel: CircleDetialViewModel,
                            fragmentViewModel: CircleENDeviceViewModel)
    : DialogBaseAdapter<CircleENDeviceViewModel>(context,fragmentViewModel) {

    //初始化
    init {
        //设置头部显示圈子名称
        fragmentViewModel.updateViewStatusParams(headerTitle = viewModel.circleDetail.value?.networkname)
        initObserver()
        //开始请求N列表数据
        fragmentViewModel.startRequestRemoteSource()
    }

    /**
     * 观察器
     */
    private fun initObserver() {
        fragmentViewModel.enDevices.observe(context, Observer {
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
            if (it.requestCode == FunctionHelper.CIRCLE_EN_DEVICE_DETAIL
                    && it.resultCode == FragmentActivity.RESULT_OK) {
                //刷新数据
                fragmentViewModel.startRequestRemoteSource()
                //重置
                operatePosition = -1
            } else if (it.requestCode == FunctionHelper.SELECT_EN_DEVICE
                    && it.resultCode == FragmentActivity.RESULT_OK) {
                fragmentViewModel.startRequestRemoteSource()
            }
        })
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == 0) TYPE_EN_COUNT else TYPE_DEFALUT
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layout = if (viewType == TYPE_EN_COUNT) R.layout.dialog_circle_en_device_count else R.layout.dialog_circle_en_device
        val view =
                LayoutInflater.from(context.requireContext()).inflate(layout, null, false)
        view.layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return (fragmentViewModel.enDevices.value?.size ?: 0) + 1
    }
    private val mDevCommonViewModel = DevCommonViewModel()
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        when (getItemViewType(position)) {
            TYPE_EN_COUNT -> {
                with(holder.itemView) {
                    val current = viewModel.circleDetail.value?.networkprops?.provide_count ?: 0
                    tvCurrentENNumber.text = current.toString()
                    val maxENNumber = viewModel.circleDetail.value?.networkprops?.network_scale?.filter {
                        it.key == "provide_max"
                    }
                    if (maxENNumber != null && maxENNumber.size > 0) {
                        tvMaxENNumber.text = maxENNumber?.get(0)?.value ?: "0"
                    } else {
                        tvMaxENNumber.text = "0"
                    }
                }

            }
            TYPE_DEFALUT -> {
                with(holder.itemView) {
                    val index = position - 1
                    setPaddingRelative(paddingLeft, if (index == 0) paddingLeft else 0, paddingLeft, paddingLeft)
                    fragmentViewModel.enDevices.value?.get(index)?.let {
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
                        if (ivDeviceIcon.getTag() == null) ivDeviceIcon.setImageResource(defualtIcon)
                        viewModel.loadBrief(it.deviceid ?: "", fragmentViewModel.getBrief(it?.deviceid
                                ?: ""),
                                ivImage = ivDeviceIcon, defalutImage = defualtIcon)

                        tvUserName.text = if (TextUtils.isEmpty(it.nickname)) it.loginname else it.nickname
                        //最后一项不显示底部线条
                        viewItemFoot.visibility = if (position == itemCount - 1) View.GONE else View.VISIBLE
                        if (it.enable ?: false) {
                            tvForbidden.text = ""
                            tvForbidden.visibility = View.GONE
                        } else {
                            tvForbidden.visibility = View.VISIBLE
                            tvForbidden.setTextColor(context.resources.getColor(R.color.red))
//                            tvForbidden.setTextColor(context.resources.getColor(R.color.darker_gray))
                            tvForbidden.text = it.getStatusName()
                        }
                        setOnClickListener {
                            //是否已经被拦截处理
                            val isInterceptor = onItemClickListener?.onClick(it, position) ?: false
                            //没有拦截，则可以内部处理
                            if (!isInterceptor) internalItemClick(it, position)
                        }
                    }
                }
            }
        }
    }

    //记录当前操作的位置
    private var operatePosition = -1
    override fun internalItemClick(view: View, position: Int) {
        if (view.id == R.id.btnBottomConfirm) {//新增En设备
            viewModel.circleDetail.value?.let {
//                val current = it.networkprops?.provide_count ?: 0
//                var max = 0
//                val maxENNumber = it.networkprops?.network_scale?.filter {
//                    it.key == "provide_max"
//                }
//                if (maxENNumber != null && maxENNumber.size > 0) {
//                    max = maxENNumber?.get(0)?.value?.toIntOrNull() ?: 0
//                }
//                if (it.isNormalUser() && current < max) {
                //叠加弹框阴影效果
                viewModel.setSecondaryDialogShow(true)
                //二级弹框－选择EN设备
                val intent = Intent(context.requireContext(), CircleDetialActivity::class.java)
                        .putExtra(FunctionHelper.FUNCTION, FunctionHelper.SELECT_EN_DEVICE)
                        .putExtra(FunctionHelper.NETWORK_ID, viewModel.networkId)
                        .putExtra("network_name", viewModel.circleDetail.value?.networkname)
                        .putExtra("network_owner", viewModel.circleDetail.value?.getFullName())
                        .putExtra("filter_deviceIds", fragmentViewModel.getDeviceIds())
                        .putExtra("is_manager", viewModel.circleDetail.value?.isOwner() ?: false || viewModel.circleDetail.value?.isManager() ?: false)

                CircleDetialActivity.startActivityForResult(context.requireActivity(),intent,FunctionHelper.SELECT_EN_DEVICE)
//                } else {
//                    if (hintDialog?.dialog?.isShowing != true) {
//                        hintDialog?.update(context.getString(
//                                if (it.isNormalUser()) R.string.over_max_account_device_hint else R.string.account_exception))
//                        hintDialog?.show(context.requireActivity().getSupportFragmentManager(), "hintDialog")
//                    }
//                }
            }


        } else {
            if(position - 1 >= 0){
                fragmentViewModel.enDevices.value?.get(position - 1)?.let {
                    operatePosition = position
                    //叠加弹框阴影效果
                    viewModel.setSecondaryDialogShow(true)
                    //二级弹框－详情
                    val intent = Intent(context.requireContext(), CircleDetialActivity::class.java)
                            .putExtra(FunctionHelper.FUNCTION, FunctionHelper.CIRCLE_EN_DEVICE_DETAIL)
                            .putExtra(FunctionHelper.NETWORK_ID, viewModel.networkId)
                            .putExtra(FunctionHelper.EXTRA_ENTITY, fragmentViewModel.enDevices.value?.get(position - 1))
                    CircleDetialActivity.startActivityForResult(context.requireActivity(),intent,FunctionHelper.CIRCLE_EN_DEVICE_DETAIL)
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

    override fun internalItemLongClick(view: View, position: Int) {
    }

}