package net.linkmate.app.ui.activity.nasApp.deviceDetial

import android.content.Context
import android.graphics.Point
import android.os.Bundle
import android.text.TextUtils
import android.view.*
import android.widget.CompoundButton
import androidx.appcompat.app.AlertDialog
import androidx.arch.core.util.Function
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import io.weline.devhelper.DevTypeHelper.isOneOSNas
import kotlinx.android.synthetic.main.dialog_device_layout.*
import kotlinx.android.synthetic.main.include_dialog_device_bottom.*
import kotlinx.android.synthetic.main.include_dialog_device_header.*
import net.linkmate.app.R
import net.linkmate.app.bean.DeviceBean
import net.linkmate.app.data.ScoreHelper
import net.linkmate.app.manager.DevManager
import net.linkmate.app.ui.activity.nasApp.deviceDetial.adapter.DeviceBaseAdapter
import net.linkmate.app.ui.activity.nasApp.deviceDetial.adapter.DeviceDetailAdapter
import net.linkmate.app.util.CheckStatus
import net.sdvn.cmapi.CMAPI
import net.sdvn.common.vo.BriefModel
import net.sdvn.nascommon.SessionManager

/**
 * @author Raleigh.Luo
 * date：20/7/23 15
 * describe：
 */
class DeviceDetailFragment : Fragment() {

    //activity生命周期viewMdole
    val viewModel: DeviceViewModel by activityViewModels()

    //fragment生命周期viewMdole
    lateinit var fragmentViewModel: DeviceDetailViewModel
    val params by navArgs<DeviceDetailFragmentArgs>()
    private var adapter: DeviceBaseAdapter<DeviceDetailViewModel>? = null
    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.dialog_device_layout, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel.setLoadingStatus(false)
        adapter = FunctionHelper.initFragmentPanel(params.function, this,
                viewModel, findNavController())
        fragmentViewModel = adapter!!.fragmentViewModel
        fragmentViewModel.function = params.function

        with(viewModel.device) {
            fragmentViewModel.updateViewStatusParams(headerTitle = name)
        }

        initEvent()
        initObserver()
        recyclerView.layoutManager = FullyLinearLayoutManager(requireContext())
        recyclerView.adapter = adapter
        recyclerView.itemAnimator = null
        adapter?.onCheckedChangeListener = onCheckedChangeListener
        adapter?.onItemClickListener = onItemClickListener
        adapter?.onItemLongClickListener = onItemLongClickListener
        checkUpdateFirmware()


        ivHeaderClose.setOnClickListener {
            //没有被拦截
            if (adapter?.interceptFinishActivity() ?: false == false)
                viewModel.toFinishActivity()
        }
        ivHeaderBack.setOnClickListener {
            //没有被拦截
            if (adapter?.interceptBackPressed() ?: false == false)
                viewModel.toBackPress()

        }
        //点击顶部空白位置关闭页面
        vEmpty.setOnClickListener {
            viewModel.toFinishActivity()
        }
        //点击顶部空白位置关闭页面
        vEmptyItemHeight.setOnClickListener {
            viewModel.toFinishActivity()
        }
        //加设备默认图标
        Glide.with(ivHeaderImage)
                .load(DeviceBean.getIcon(viewModel.device))
                .into(ivHeaderImage)
    }

    /**
     * 检查是否需要升级固件
     */
    private fun checkUpdateFirmware() {
        if (params.function == FunctionHelper.DEVICE_DETAIL) {
            with(viewModel.device) {
                if (isOnline && isNas) {
                    val deviceModel = SessionManager.getInstance().getDeviceModel(id)
                    deviceModel?.let {
                        val isM3 = isOneOSNas(deviceModel.devClass)
                        if (deviceModel.isOwner || deviceModel.isAdmin) {
                            if (isM3) {
                                fragmentViewModel.getUpdateInfo(deviceModel)
                            } else {
                                fragmentViewModel.getUpdateInfoM8(deviceModel)
                            }
                        }
                    }

                }
            }
        }
    }

    private fun initEvent() {
        btnBottomConfirm.setOnClickListener {
            adapter?.internalItemClick(it, -1)
        }
        flBottom.setOnClickListener {
            adapter?.internalItemClick(it, -1)
        }
    }

    private fun initObserver() {

        viewModel.cancelRequest.observe(viewLifecycleOwner, Observer {
            if (it) {
                //用户手动取消了请求
                fragmentViewModel.dispose()
            }
        })
        fragmentViewModel.viewStatusParams.observe(viewLifecycleOwner, Observer {
            initViewStatus(it)
        })
        viewModel.refreshDeviceName.observe(viewLifecycleOwner, Observer {
            fragmentViewModel.updateViewStatusParams(headerTitle = it)
        })
        if (params.function == FunctionHelper.DEVICE_DETAIL) {
            fragmentViewModel.mUpdateInfo.observe(viewLifecycleOwner, Observer {
                it.data?.let {
                    if (adapter is DeviceDetailAdapter) {
                        (adapter as DeviceDetailAdapter).addUpdateFirmwareItem()
                    }
                }
            })
            fragmentViewModel.mUpdateInfoM8.observe(viewLifecycleOwner, Observer {
                if (adapter is DeviceDetailAdapter) {
                    (adapter as DeviceDetailAdapter).addUpdateFirmwareItem()
                }
            })
        }
        viewModel.deviceBrief.observe(viewLifecycleOwner, Observer {
            if (!TextUtils.isEmpty(viewModel.device.id)) {
                var brief: BriefModel? = null
                if (it != null && it.size > 0) {
                    brief = it.get(0)
                }
                viewModel.loadBrief(viewModel.device.id, brief,
                        ivImage = ivHeaderImage,
                        defalutImage = DeviceBean.getIcon(viewModel.device),
                        isLoadOneDeviceBrief = true)
            }
        })
    }

    /**
     * 初始化头部副标题和底部按钮
     */
    private fun initViewStatus(statusParams: FunctionHelper.ViewStatusParams) {
        setBottomPanelStatus(statusParams)
        statusParams.headerTitle?.let {
            tvHeaderTitle.text = statusParams.headerTitle
        }
        tvHeaderDescribe.text = statusParams.headerDescribe
        tvHeaderDescribe.visibility = if (TextUtils.isEmpty(statusParams.headerDescribe)) View.GONE else View.VISIBLE
        ivHeaderBack.visibility = statusParams.headBackButtonVisibility
        if (statusParams.headerIcon == 0) {
            ivHeaderImage.visibility = View.GONE
        } else if (statusParams.headerIcon == -1) {
            ivHeaderImage.visibility = View.VISIBLE
        } else {
            ivHeaderImage.visibility = View.VISIBLE
            ivHeaderImage.setImageResource(statusParams.headerIcon)
        }
    }

    /**
     * 初始化底部按钮
     */
    private fun setBottomPanelStatus(params: FunctionHelper.ViewStatusParams) {
        flBottom.visibility = if (TextUtils.isEmpty(params.bottomTitle)) View.GONE else View.VISIBLE
        btnBottomConfirm.isEnabled = params.bottomIsEnable
        btnBottomAdd.isEnabled = params.bottomAddIsEnable
        flBottom.isEnabled = params.bottomIsEnable
        params.bottomTitle?.let {
            if (params.bottomIsFullButton) {//蓝色按钮
                btnBottomConfirm.text = params.bottomTitle
                btnBottomConfirm.visibility = View.VISIBLE
                tvBottom.visibility = View.GONE
            } else {
                tvBottom.text = params.bottomTitle
                tvBottom.visibility = View.VISIBLE
                btnBottomConfirm.visibility = View.GONE
            }
        }
        params.bottomAddTitle?.let {
            btnBottomAdd.text = params.bottomAddTitle
            btnBottomAdd.visibility = View.VISIBLE

            if (!TextUtils.isEmpty(params.bottomTitle)) {//底部有两个按钮时，得重新设置下高度
                val wm = context
                        ?.getSystemService(Context.WINDOW_SERVICE) as WindowManager
                val defaultDisplay: Display = wm.getDefaultDisplay()
                val point = Point()
                defaultDisplay.getSize(point)
                recyclerView.mHeight = Math.ceil((point.y * 3.toDouble() / 5.toDouble())).toInt()
            }
        }
    }

    private val onItemClickListener = object : DeviceBaseAdapter.OnItemClickListener {
        override fun onClick(view: View, position: Int): Boolean {
            /**
             * 返回true: 可拦截adapter适配内部点击事件响应
             * 返回false：不拦截
             */
            return false
        }

    }

    //正在检查节点
    private var nodeChecking = false
    private val onCheckedChangeListener = object : CompoundButton.OnCheckedChangeListener {
        override fun onCheckedChanged(p0: CompoundButton, isChecked: Boolean) {
            if (!nodeChecking) {
                nodeChecking = true
                choiceNode(p0, isChecked)
                nodeChecking = false
            }
        }
    }

    private val onItemLongClickListener = object : DeviceBaseAdapter.OnItemLongClickListener {
        override fun onLongClick(view: View, position: Int): Boolean {
            /**
             * 返回true: 可拦截adapter适配内部点击事件响应
             * 返回false：不拦截
             */
            return false
        }
    }

    /**
     * 选择节点
     */
    fun choiceNode(switchSnEnable: CompoundButton, isChecked: Boolean) {
        val normal = {
            val runnable = Runnable {
                viewModel.setLoadingStatus(true)
                switchSnEnable.postDelayed({ viewModel.setLoadingStatus(false) }, 3000)
            }
            if (isChecked && !CMAPI.getInstance().baseInfo.hadSelectedSn(viewModel.device.getId())
                    || !isChecked && CMAPI.getInstance().baseInfo.hadSelectedSn(viewModel.device.getId())) if (CMAPI.getInstance().baseInfo.hadSelectedSn(viewModel.device.getId())) {
                if (CMAPI.getInstance().config.isNetBlock
                        && CMAPI.getInstance().baseInfo.snIds.size == 1) {
                    AlertDialog.Builder(requireContext())
                            .setMessage(R.string.tips_internet_access_sn_cancel)
                            .setPositiveButton(R.string.confirm) { dialog, which ->
                                runnable.run()
                                if (CMAPI.getInstance().removeSmartNode(viewModel.device.getId())) {
                                    switchSnEnable.setChecked(false)
                                    dialog.dismiss()
                                    DevManager.getInstance().notifyDeviceStateChanged()
                                }
                            }
                            .setNegativeButton(R.string.cancel) { dialog, which -> dialog.dismiss() }
                            .show()
                } else {
                    if (CMAPI.getInstance().removeSmartNode(viewModel.device.getId())) {
                        switchSnEnable.setChecked(false)
                        runnable.run()
                    }
                }
            } else {
                //节点只许单选
                if (CMAPI.getInstance().selectSmartNode(viewModel.device.getId())) {
                    switchSnEnable.setChecked(true)
                    runnable.run()
                }
            }
            DevManager.getInstance().notifyDeviceStateChanged()
        }

        if (isChecked) {
            if (viewModel.device.isDevDisable() && viewModel.device.devDisableReason == 1) {//没有积分，需要购买
                switchSnEnable.setChecked(false)
                ScoreHelper.showNeedMBPointDialog(context)
                return
            }
            CheckStatus.checkDeviceStatus(requireActivity(), requireActivity().supportFragmentManager, viewModel.device, Function {
                if (it) {//状态正常
                    normal()
                } else {//状态异常
                    switchSnEnable.setChecked(false)
                }
                null
            }, Function {
                if (it) {//状态异常，且进行了下一步,点击了确定，关闭页面
                    viewModel.toFinishActivity()
                }
                null
            })
        } else {
            normal()
        }
    }


    override fun onDestroy() {
        adapter?.onDestory()
        super.onDestroy()

    }
}
