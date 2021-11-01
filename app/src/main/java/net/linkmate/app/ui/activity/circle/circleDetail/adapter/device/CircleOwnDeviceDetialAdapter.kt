package net.linkmate.app.ui.activity.circle.circleDetail.adapter.device

import android.content.DialogInterface
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
import kotlinx.android.synthetic.main.dialog_device_type.view.*
import net.linkmate.app.R
import net.linkmate.app.base.MyConstants
import net.linkmate.app.data.model.CircleDevice
import net.linkmate.app.manager.DevManager
import net.linkmate.app.ui.activity.circle.circleDetail.CircleBenefitsActivity
import net.linkmate.app.ui.activity.circle.circleDetail.CircleDetialViewModel
import net.linkmate.app.ui.activity.circle.circleDetail.FunctionHelper
import net.linkmate.app.ui.activity.circle.circleDetail.adapter.DialogBaseAdapter
import net.linkmate.app.util.ToastUtils
import net.linkmate.app.view.HintDialog
import net.sdvn.cmapi.CMAPI
import net.sdvn.common.internet.core.GsonBaseProtocol
import net.sdvn.common.internet.core.HttpLoader
import net.sdvn.common.internet.listener.ResultListener
import net.sdvn.common.internet.protocol.HardWareInfo
import net.sdvn.common.repo.NetsRepo
import net.sdvn.nascommon.SessionManager
import net.sdvn.nascommon.utils.SPUtils

/**
 * @author Raleigh.Luo
 * date：20/10/14 20
 * describe：
 */
class CircleOwnDeviceDetialAdapter(context: Fragment, val viewModel: CircleDetialViewModel, fragmentViewModel: CircleOwnDeviceViewModel)
    : DialogBaseAdapter<CircleOwnDeviceViewModel>(context, fragmentViewModel) {
    private val menus: ArrayList<FunctionHelper.DetailMenu> = ArrayList()
    private lateinit var device: CircleDevice.Device

    init {
        var data: CircleDevice.Device? = null
        if (context.requireActivity().intent.hasExtra(FunctionHelper.EXTRA_ENTITY)) {
            data = context.requireActivity().intent.getSerializableExtra(FunctionHelper.EXTRA_ENTITY) as CircleDevice.Device?
        }
        if (data == null) {
            viewModel.toFinishActivity()
        } else {
            initObserver()
            device = data
            getItemSources()
            fragmentViewModel.updateViewStatusParams(headerIcon = IconHelper.getIconByeDevClass(device.deviceclass
                    ?: -1, true, true),
                    headerTitle = device.devicename
                            ?: "", headerDescribe = CMAPI.getInstance().baseInfo.account)
            //显示备注名
            if (SPUtils.getBoolean(MyConstants.SP_SHOW_REMARK_NAME, true)) {
                val deviceModel = SessionManager.getInstance()
                        .getDeviceModel(device?.deviceid ?: "")
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

    }

    private fun initObserver() {
        fragmentViewModel.cancelCooperationResult.observe(context, Observer {//取消提供服务
            if (it) {
                val refresh = {
                    //刷新当前网络EN服务器信息
                    DevManager.getInstance().refreshENServerData(Function {
                        //刷新网络
                        NetsRepo.refreshNetList()
                                .setHttpLoaderStateListener(object : HttpLoader.HttpLoaderStateListener {
                                    override fun onLoadComplete() {
                                        viewModel.setLoadingStatus(false)
                                        context.requireActivity().setResult(FragmentActivity.RESULT_OK)
                                        viewModel.toFinishActivity()
                                    }

                                    override fun onLoadStart(disposable: Disposable?) {
                                    }

                                    override fun onLoadError() {
                                        viewModel.setLoadingStatus(false)
                                        context.requireActivity().setResult(FragmentActivity.RESULT_OK)
                                        viewModel.toFinishActivity()
                                    }
                                })
                        null
                    })
                }
                DevManager.getInstance().initHardWareList(object : ResultListener<HardWareInfo> {
                    override fun error(tag: Any?, baseProtocol: GsonBaseProtocol?) {
                        refresh()
                    }

                    override fun success(tag: Any?, data: HardWareInfo?) {
                        refresh()
                    }
                })

            }else{
                viewModel.setLoadingStatus(false)
            }
        })
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
                LayoutInflater.from(context.requireContext()).inflate(R.layout.dialog_device_item_detail, null, false)
        view.layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return menus.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        with(holder.itemView) {
            ivDeviceDetailIcon.setImageResource(0)
            ivDeviceDetailIcon.visibility = View.GONE
            ivDeviceDetailTitle.setText(menus.get(position).title)
            //查看收益 设置右箭头
//            ivDeviceDetailTitle.setEndDrawable(if (position == 0) context.getDrawable(R.drawable.gray_right_arrow) else null)
            setOnClickListener {
                //是否已经被拦截处理
                val isInterceptor = onItemClickListener?.onClick(it, position) ?: false
                //没有拦截，则可以内部处理
                if (!isInterceptor) internalItemClick(it, position)
            }

        }
    }

    override fun internalItemClick(view: View, position: Int) {
        if (position < 0) return
        if (menus.get(position).function == FunctionHelper.CIRCLE_BENEFITS) {//查看收益
            context.requireActivity().startActivity(Intent(context.requireContext(), CircleBenefitsActivity::class.java)
                    .putExtra(FunctionHelper.EXTRA, device.deviceid))
        } else {//取消EN
            //取消合作 提示
            if (hintDialog?.dialog?.isShowing != true) {
                hintDialog?.show(context.requireActivity().getSupportFragmentManager(), "hintDialog")
            }
        }
    }

    /**
     * 提示Dialog
     */
    private var hintDialog: HintDialog? = null
        get() {
            if (field == null) {
                field = HintDialog.newInstance(context.getString(R.string.cancel_en_device_partner_hint),
                        null, R.color.red,
                        confrimText = context.getString(R.string.confirm),
                        cancelText = context.getString(R.string.cancel))
                        .setOnClickListener(View.OnClickListener {
                            if (it.id == R.id.positive) {//确定按钮
                                fragmentViewModel.startCancelCooperation(device.deviceid
                                        ?: "")
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

    override fun internalItemLongClick(view: View, position: Int) {
    }

    fun getItemSources() {
        menus.clear()
        //暂时隐藏我的收益
//        menus.add(FunctionHelper.DetailMenu(FunctionHelper.CIRCLE_BENEFITS, context.getString(R.string.view_benefits)))
        if (device.isen ?: false)//是En设备
            menus.add(FunctionHelper.DetailMenu(FunctionHelper.CANCEL_EN_SERVER, context.getString(R.string.cancel_cooperation)))
    }

}