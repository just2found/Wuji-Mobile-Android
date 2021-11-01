package net.linkmate.app.ui.activity.circle.circleDetail.adapter.device

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.arch.core.util.Function
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Observer
import com.rxjava.rxlife.RxLife
import io.reactivex.disposables.Disposable
import io.weline.devhelper.IconHelper
import kotlinx.android.synthetic.main.dialog_device_item_detail.view.*
import kotlinx.android.synthetic.main.dialog_device_item_switch.view.*
import net.linkmate.app.R
import net.linkmate.app.base.MyConstants
import net.linkmate.app.data.model.CircleDevice
import net.linkmate.app.manager.DevManager
import net.linkmate.app.ui.activity.circle.circleDetail.CircleBenefitsActivity
import net.linkmate.app.ui.activity.circle.circleDetail.CircleDetialViewModel
import net.linkmate.app.ui.activity.circle.circleDetail.FunctionHelper
import net.linkmate.app.ui.activity.circle.circleDetail.adapter.DialogBaseAdapter
import net.sdvn.common.internet.core.HttpLoader
import net.sdvn.common.repo.NetsRepo
import net.sdvn.nascommon.SessionManager
import net.sdvn.nascommon.utils.SPUtils

/**二级弹框 EN设备详情
 * @author Raleigh.Luo
 * date：20/8/18 09
 * describe：
 */
class CircleENDeviceDetailAdapter(context: Fragment, val viewModel: CircleDetialViewModel, fragmentViewModel: CircleENDeviceDetailViewModel)
    : DialogBaseAdapter<CircleENDeviceDetailViewModel>(context, fragmentViewModel) {


    init {
        if (context.requireActivity().intent.hasExtra(FunctionHelper.EXTRA_ENTITY))
            fragmentViewModel.enDevice = context.requireActivity().intent.getSerializableExtra(FunctionHelper.EXTRA_ENTITY) as CircleDevice.Device?
        //上层没有给值，直接关闭
        if (fragmentViewModel.enDevice == null) viewModel.toFinishActivity()
        fragmentViewModel.updateViewStatusParams(headerIcon = IconHelper.getIconByeDevClass(fragmentViewModel.enDevice?.deviceclass
                ?: -1, true, true),
                headerTitle = fragmentViewModel.enDevice?.devicename, headerDescribe = fragmentViewModel.enDevice?.loginname)
        initObserver()
        //显示备注名
        if (SPUtils.getBoolean(MyConstants.SP_SHOW_REMARK_NAME, true)) {
            val deviceModel = SessionManager.getInstance()
                    .getDeviceModel(fragmentViewModel.enDevice?.deviceid ?: "")
            if (deviceModel != null) {
                fragmentViewModel.updateViewStatusParams(headerTitle = deviceModel.devName)
                deviceModel.devNameFromDB
                        .`as`(RxLife.`as`(context))
                        .subscribe({ s: String? ->
                            fragmentViewModel.updateViewStatusParams(headerTitle = s)
                        }) { throwable: Throwable? -> }
            }
        }
    }

    private fun initObserver() {
        fragmentViewModel.enableENDeviceResult.observe(context, Observer {
            if (it) {//启用或禁用成功
                //刷新EN服务器
                DevManager.getInstance().refreshENServerData(Function {
                    NetsRepo.refreshNetList()////启用或禁用成功
                            .setHttpLoaderStateListener(object : HttpLoader.HttpLoaderStateListener{
                                override fun onLoadComplete() {
                                    viewModel.setLoadingStatus(false)
                                    fragmentViewModel.enDevice?.changeEnable()
                                    //更新UI
                                    notifyDataSetChanged()
                                    context.requireActivity().setResult(FragmentActivity.RESULT_OK)
                                }

                                override fun onLoadStart(disposable: Disposable?) {
                                }

                                override fun onLoadError() {
                                    viewModel.setLoadingStatus(false)
                                    fragmentViewModel.enDevice?.changeEnable()
                                    //更新UI
                                    notifyDataSetChanged()
                                    context.requireActivity().setResult(FragmentActivity.RESULT_OK)
                                }
                            })
                    null
                })
            } else {
                viewModel.setLoadingStatus(false)
                notifyDataSetChanged()
            }
        })
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layout = if (viewType == TYPE_STATUS) R.layout.dialog_device_item_status
        else if (viewType == TYPE_SWITCH) R.layout.dialog_device_item_switch
        else R.layout.dialog_device_item_detail
        val view =
                LayoutInflater.from(context.requireContext()).inflate(layout, null, false)
        view.layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        //暂时去掉我的收益
//        return if (fragmentViewModel.enDevice?.isNormal() == true) 3 else 2
        return 1
    }

    override fun getItemViewType(position: Int): Int {
//        return if (position == 0) TYPE_SWITCH else if (position == 1 && fragmentViewModel.enDevice?.isNormal() == true) TYPE_SWITCH else TYPE_DEFALUT
        return if (position == 0) TYPE_SWITCH else TYPE_DEFALUT
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        when (getItemViewType(position)) {
//            TYPE_STATUS -> {
//                with(holder.itemView) {
//                    tvStatusTitle.setText(R.string.status)
//                    tvStatusContent.text = fragmentViewModel.enDevice?.getStatusName()
//                }
//            }
            TYPE_SWITCH -> {
                with(holder.itemView) {
                    mSwitch.text = context.getText(R.string.is_enable_en_server)
                    mSwitch.setOnCheckedChangeListener(null)
                    mSwitch.isChecked = fragmentViewModel.enDevice?.enable ?: false
                    mSwitch.setOnCheckedChangeListener { compoundButton, b ->
                        fragmentViewModel.startEnableENDevice(b)
                    }
                }
            }
            else -> {
                with(holder.itemView) {
                    val detailMenu = FunctionHelper.getMenu(FunctionHelper.CIRCLE_BENEFITS)
                    ivDeviceDetailIcon.setImageResource(detailMenu.icon)
                    ivDeviceDetailIcon.visibility = if (detailMenu.icon == 0) View.GONE else View.VISIBLE
                    ivDeviceDetailTitle.setText(detailMenu.title)
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

    override fun internalItemClick(view: View, position: Int) {
        if(position>=0){
            if (getItemViewType(position) == TYPE_DEFALUT) {//查看收益
                context.requireActivity().startActivity(Intent(context.requireContext(), CircleBenefitsActivity::class.java)
                        .putExtra(FunctionHelper.EXTRA, fragmentViewModel.enDevice?.deviceid))
            }
        }
    }

    override fun internalItemLongClick(view: View, position: Int) {
    }

}