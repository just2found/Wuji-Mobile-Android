package net.linkmate.app.ui.activity.nasApp.deviceDetial

import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import androidx.activity.viewModels
import androidx.core.os.bundleOf
import androidx.lifecycle.Observer
import androidx.navigation.Navigation
import io.weline.repo.files.constant.AppConstants
import kotlinx.android.synthetic.main.activity_device_detail.*
import net.linkmate.app.R
import net.linkmate.app.base.BaseActivity
import net.linkmate.app.base.BaseViewModel
import net.linkmate.app.base.DevBoundType
import net.linkmate.app.bean.DeviceBean
import net.linkmate.app.manager.DevManager
import net.linkmate.app.ui.activity.circle.circleDetail.CircleDetialActivity
import net.linkmate.app.ui.activity.dev.DevBriefActivity
import net.linkmate.app.util.AccessDeviceTool
import net.linkmate.app.util.UIUtils
import net.linkmate.app.view.ProgressDialog
import net.linkmate.app.view.TipsBar
import kotlinx.android.synthetic.main.activity_circle_detail.tipsBar as mTipsBar

/** 设备详情 －弹框
 * @author Raleigh.Luo
 * date：20/7/23 15
 * describe：
 */
class DevicelDetailActivity : BaseActivity() {
    companion object{
        fun startActivityForResult(activity: Activity, intent: Intent, requestCode: Int){
            intent.setClass(activity, DevicelDetailActivity::class.java)
            activity.startActivityForResult(intent,requestCode)
            activity.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        }
        fun startActivity(activity: Activity, intent: Intent){
            intent.setClass(activity, DevicelDetailActivity::class.java)
            activity.startActivity(intent)
            activity.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        }
    }
    private var mProgressDialog: ProgressDialog? = null
    override fun onStatusChange(currentStatus: Int?) {
        //过滤掉父类的连接加载动画
    }

    /**
     * 全局viewModel
     */
    val viewModel: DeviceViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_device_detail)

        initNoStatusBar()
        initObersver()
        try {
            val intent = intent
            if (intent != null) deviceId = intent.getStringExtra(net.sdvn.nascommon.constant.AppConstants.SP_FIELD_DEVICE_ID)
            if (deviceId != null) {
                val bean = findDeviceBean(deviceId)
                bean?.let {
                    FunctionHelper.deviceBeanTemp = bean
                }
            }
            val position = intent.getIntExtra(FunctionHelper.POSITION, 0)
            val deviceBoundType = intent.getIntExtra(FunctionHelper.DEVICE_BOUND_TYPE, DevBoundType.ALL_BOUND_DEVICES)
            FunctionHelper.deviceBeanTemp?.let {
                viewModel.init(position, deviceBoundType, it)
                FunctionHelper.clear()
            } ?: let {//为了兼容旧逻辑，没有device就创建一个空对象
                val id = intent.getStringExtra(AppConstants.SP_FIELD_DEVICE_ID) ?: ""
                var device: DeviceBean? = null
                if (intent.hasExtra(AppConstants.SP_FIELD_DEVICE_ID)) {
                    device = findDeviceBean(deviceId)
                }
                if (device == null) {
                    device = DeviceBean("", "", 0, 0)
                    device.id = id
                }
                viewModel.init(position, deviceBoundType, device)
            } ?: let {//为了兼容旧逻辑，没有device就创建一个空对象
                viewModel.init(position, deviceBoundType, DeviceBean("", "", 0, 0))
            }
            loadHeaderIcon()

            val navController = Navigation.findNavController(this, R.id.fragment)
            //指定了跳转页面，重新设置默认FragmentDirection参数,
            //如跳转到分享： intent.putExtra(FunctionHelper.FUNCTION,FunctionHelper.DEVICE_SHARE);
            if (intent.hasExtra(FunctionHelper.FUNCTION)) {
                val function = intent.getIntExtra(FunctionHelper.FUNCTION, 0)
                navController.navigate(R.id.enterDetial, bundleOf(FunctionHelper.FUNCTION to function))
            }

        } catch (e: Exception) {
            e.printStackTrace()
            finish()
        }
    }

    /**
     * 加载头像
     */
    private fun loadHeaderIcon() {
        if (TextUtils.isEmpty(viewModel.device.id) && intent.hasExtra(AppConstants.SP_FIELD_DEVICE_ID) == true) {
            val deviceId = intent?.getStringExtra(AppConstants.SP_FIELD_DEVICE_ID)
                    ?: ""
            val device = findDeviceBean(deviceId)
            if (device != null) viewModel.device = device
        }
        if (!TextUtils.isEmpty(viewModel.device.id)) {
            viewModel.startGetDeviceBrief(viewModel.device.id)
        }

    }

    private fun findDeviceBean(deviceId: String): DeviceBean? {
        val device = DevManager.getInstance().deviceBeans.find {
            it.id == deviceId
        } ?: (let {
            DevManager.getInstance().boundDeviceBeans.find {
                it.id == deviceId
            }
        } ?: let {
            DevManager.getInstance().localDeviceBeans.find {
                it.id == deviceId
            }
        })
        return device
    }

    private fun initNoStatusBar() {
//         style的windowTranslucentNavigation设置为false后，状态栏无法达到沉浸效果
//         设置UI FLAG 让布局能占据状态栏的空间，达到沉浸效果
        val option = (View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE)
        window.decorView.systemUiVisibility = option
        //头部宽度
        root.setPadding(0, UIUtils.getStatueBarHeight(this) - resources.getDimensionPixelSize(R.dimen.common_24), 0, 0)
        //修改状态栏为全透明
        window.setStatusBarColor(Color.TRANSPARENT);
        window.setNavigationBarColor(Color.TRANSPARENT);
    }

    override fun getTipsBar(): TipsBar? {
        return mTipsBar
    }

    override fun getViewModel(): BaseViewModel {
        return viewModel
    }

    private fun initObersver() {
        viewModel.isLoading.observe(this, Observer {
            try {
                if (it) {
                    if (mProgressDialog == null) {
                        mProgressDialog = ProgressDialog()
                        mProgressDialog?.setOnClickListener(View.OnClickListener {
                            viewModel.cancelRequest()
                        })
                        mProgressDialog?.onDismissListener = DialogInterface.OnDismissListener {
                            viewModel.setSecondaryDialogShow(false)
                        }
                        mProgressDialog?.onShowListener = DialogInterface.OnShowListener {
                            viewModel.setSecondaryDialogShow(true)
                        }
                    }

                    mProgressDialog?.let {
                        it.update(viewModel.isLoadingCancelable)
                        if (!it.isAdded && !it.isVisible()
                                && !it.isRemoving()) {
                            it.show(supportFragmentManager, "ProgressDialog")
                        }
                    }
                } else {
                    mProgressDialog?.dismiss()
                }
            } catch (e: Exception) {
            }
        })
        viewModel.cancelRequest.observe(this, Observer {
            if (it) {//用户手动取消了请求
                viewModel.dispose()
            }
        })

        viewModel.toFinishActivity.observe(this, Observer {
            finish()
        })
        viewModel.toBackPress.observe(this, Observer {
            onBackPressed()
        })
        viewModel.isSecondaryDialogShow.observe(this, Observer {
            window.setBackgroundDrawableResource(if (it) R.color.transparent_dialog else android.R.color.transparent)
        })

        viewModel.cancelLoading.observe(this, Observer {
            if (it == BaseActivity.LoadingStatus.ACCESS_NAS) {
                mAccessDeviceTool?.cancel()
            }
        })
        viewModel.checkDeviceFormat.observe(this, Observer {
            if (mAccessDeviceTool == null) {
                mAccessDeviceTool = AccessDeviceTool(this)
            }
            val function = it
            mAccessDeviceTool?.accessDevice(viewModel.device, androidx.arch.core.util.Function {
                if (it) {
                    var owner = viewModel.device.hardData?.nickname
                    if (TextUtils.isEmpty(owner)) owner = viewModel.device.owner
                    DevBriefActivity.start(this,
                            viewModel.device.id,
                            viewModel.device.name,
                            owner ?: "",
                            viewModel.device.isOwner(),
                            viewModel.device.devClass, FunctionHelper.DEVICE_BRIEF
                    )
                }
                null
            })
        })

    }

    override fun onBackPressed() {
        viewModel.setLoadingStatus(false)
        //第一个页面
        val firstFunction = intent.getIntExtra(FunctionHelper.FUNCTION, FunctionHelper.DEVICE_DETAIL)
        if (firstFunction != FunctionHelper.DEVICE_DETAIL) {//有跳转到指定页面
            val navController = Navigation.findNavController(this, R.id.fragment)
            //当前页面的function
            val currentPageFunction = navController.currentBackStackEntry?.arguments?.get(FunctionHelper.FUNCTION)
                    ?: 0
            if (currentPageFunction == firstFunction) {
                //已经返回到指定的第一个页面，直接finish
                finish()
            } else {
                super.onBackPressed()
            }
        } else {
            super.onBackPressed()
        }


    }

    private
    var mAccessDeviceTool: AccessDeviceTool? = null

    override fun finish() {
        super.finish()
        viewModel.setLoadingStatus(false)
        overridePendingTransition(android.R.anim.fade_in, R.anim.device_slide_out_to_bottom);
    }

    override fun onDestroy() {
        mAccessDeviceTool?.cancel()
        super.onDestroy()
    }
}