package net.linkmate.app.ui.activity.circle.circleDetail.adapter.device

import android.content.DialogInterface
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.arch.core.util.Function
import androidx.fragment.app.Fragment
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
import net.linkmate.app.ui.activity.circle.circleDetail.adapter.DialogBaseAdapter
import net.linkmate.app.ui.viewmodel.DevCommonViewModel
import net.linkmate.app.util.ToastUtils
import net.linkmate.app.view.HintDialog
import net.sdvn.common.internet.core.HttpLoader
import net.sdvn.common.repo.BriefRepo
import net.sdvn.common.repo.NetsRepo
import net.sdvn.nascommon.SessionManager
import net.sdvn.nascommon.utils.SPUtils

/**选择主EN设备
 * @author Raleigh.Luo
 * date：20/8/14 11
 * describe：
 */
class CircleSetMainENDeviceAdapter(context: Fragment, val viewModel: CircleDetialViewModel, fragmentViewModel: CircleSetMainENDeviceViewModel)
    : DialogBaseAdapter<CircleSetMainENDeviceViewModel>(context, fragmentViewModel) {
    //记录上个被选择的位置o
    private var mLastCheckedPosition: Int = -1


    //初始化
    init {
        initObserver()
        fragmentViewModel.startRequestRemoteSource()
    }

    private var mainENDeviceId: String? = null

    /**
     * 观察器
     */
    private fun initObserver() {
        fragmentViewModel.remoteEnDevices.observe(context, Observer {
//            if (it?.size ?: 0 == 0) {//没有数据，请求本地数据
//                viewModel.setLoadingStatus(true)
//                fragmentViewModel.startLocalEnDevices()
//            } else {
//                mLastCheckedPosition = -1
//                notifyDataSetChanged()
//            }
            if (it.size > 0) mainENDeviceId = it.get(0).deviceid
            viewModel.setLoadingStatus(true)
            fragmentViewModel.startLocalEnDevices()
            mLastCheckedPosition = -1
        })

        fragmentViewModel.ownLocalEnDevices.observe(context, Observer {
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
        fragmentViewModel.setMainENServerResult.observe(context, Observer {
            if (it) {
                //刷新当前网络EN服务器信息
                DevManager.getInstance().refreshENServerData(Function {
                    //刷新网络
                    NetsRepo.refreshNetList()
                            .setHttpLoaderStateListener(object : HttpLoader.HttpLoaderStateListener {
                                override fun onLoadComplete() {
                                    viewModel.setLoadingStatus(false)
//                                    viewModel.startRequestCircleDetail()
                                    ToastUtils.showToast(R.string.setting_success)
                                    viewModel.toFinishActivity()
                                }

                                override fun onLoadStart(disposable: Disposable?) {
                                }

                                override fun onLoadError() {
                                    viewModel.setLoadingStatus(false)
//                                    viewModel.startRequestCircleDetail()
                                    ToastUtils.showToast(R.string.setting_success)
                                    viewModel.toFinishActivity()
                                }
                            })
                    null
                })
            } else {
                viewModel.setLoadingStatus(false)
            }
        })
    }

    /**
     * 创建提示Dialog
     */
    private var hintDialog: HintDialog? = null
        get() {
            if (field == null) {
                when (fragmentViewModel.function) {
                    FunctionHelper.CIRCLE_SETTING_MAIN_EN_DEVICE -> {//设置主EN
                        field = HintDialog.newInstance(context.getString(R.string.confirm_selected_en_device_title),
                                context.getString(R.string.transfer_en_device_hint), R.color.red,
                                confrimText = context.getString(R.string.confirm),
                                cancelText = context.getString(R.string.cancel)
                        )
                                .setOnClickListener(View.OnClickListener {
                                    if (it.id == R.id.positive) {//确定按钮
                                        if (mLastCheckedPosition >= 0) {
                                            //请求 设置为圈子的主EN
                                            val position: Int = mLastCheckedPosition
                                            fragmentViewModel.startSetMainENServer(fragmentViewModel.getDeviceId(position))

                                        }
                                    }
                                })
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


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
                LayoutInflater.from(context.requireContext()).inflate(R.layout.dialog_device_type, null, false)
        view.layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return fragmentViewModel.ownLocalEnDevices.value?.size ?: 0
    }

    private val mDevCommonViewModel = DevCommonViewModel()
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        with(holder.itemView) {
            setPaddingRelative(paddingLeft, if (position == 0) paddingLeft else 0, paddingLeft, paddingLeft)
            var devClass: Int? = null
            var devName: String? = null
            var isMainEn: Boolean = false
            val device = fragmentViewModel.ownLocalEnDevices.value?.get(position)
            devClass = device?.devClass
            devName = device?.devName
            mainENDeviceId?.let {
                isMainEn = device?.devId == mainENDeviceId
            }

            val defualtIcon = devClass?.let {
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
            tvUserName.visibility = View.GONE
            cbSelectDevice.setOnCheckedChangeListener(null)
            var isChecked = false
            if (mLastCheckedPosition == -1 && isMainEn) {
                isChecked = isMainEn
                cbSelectDevice.isChecked = isChecked
            } else {
                cbSelectDevice.isChecked = mLastCheckedPosition == position
            }
            //最后一项不显示底部线条
            viewItemFoot.visibility = if (position == itemCount - 1) View.GONE else View.VISIBLE
            if (isChecked && mLastCheckedPosition == -1) {
                //初始化
                mLastCheckedPosition = position
            }
            cbSelectDevice.setOnCheckedChangeListener { compoundButton, b ->
                if (b) {//选择了其他
                    //选择了一项，设置底部按钮可用
                    if (fragmentViewModel.viewStatusParams.value?.bottomIsEnable ?: false == false) {
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

    override fun internalItemLongClick(view: View, position: Int) {
    }

    override fun internalItemClick(view: View, position: Int) {
        if (view.id == R.id.btnBottomConfirm) {//底部按钮
            if (fragmentViewModel.function == FunctionHelper.CIRCLE_SETTING_MAIN_EN_DEVICE) {
                //取消合作 提示
                if (hintDialog?.dialog?.isShowing != true) {
                    hintDialog?.show(context.requireActivity().getSupportFragmentManager(), "hintDialog")
                }
            }
        }
    }


}