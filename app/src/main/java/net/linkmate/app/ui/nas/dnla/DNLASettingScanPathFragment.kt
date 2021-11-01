package net.linkmate.app.ui.nas.dnla

import android.os.Bundle
import android.view.View
import android.widget.CompoundButton
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import io.weline.repo.api.V5HttpErrorNo
import kotlinx.android.synthetic.main.fragment_dnla_setting_scan_path.*
import kotlinx.android.synthetic.main.layout_empty_view.*
import net.linkmate.app.R
import net.linkmate.app.ui.fragment.BaseFragment
import net.linkmate.app.util.ToastUtils
import net.sdvn.cmapi.CMAPI
import net.sdvn.nascommon.SessionManager
import net.sdvn.nascommon.constant.AppConstants
import net.sdvn.nascommon.fileserver.constants.SharePathType
import net.sdvn.nascommon.iface.GetSessionListener
import net.sdvn.nascommon.model.oneos.user.LoginSession

/**DLNA设置扫描路径
 * @author Raleigh.Luo
 * date：21/6/5 11
 * describe：
 */
class DNLASettingScanPathFragment : BaseFragment() {
    private val activityViewModel: DNLAViewModel by activityViewModels()
    private val viewModel: DNLASettingScanPathViewModel by viewModels()
    override fun initView(view: View, savedInstanceState: Bundle?) {
        textView.visibility = View.GONE
        viewModel.deviceId = activityViewModel.deviceId
        mExternalStorageSwitch.setOnCheckedChangeListener(null)
        mPublishSwitch.setOnCheckedChangeListener(null)
        //初始化配置
        mPublishSwitch.isChecked = activityViewModel.isScanPublicPath.value ?: false
        mExternalStorageSwitch.isChecked = activityViewModel.isScanExternalStoragePath.value
                ?: false
        mExternalStorageSwitch.setOnCheckedChangeListener(ExternalStoragecheckedListener)
        mPublishSwitch.setOnCheckedChangeListener(publishCheckedListener)
        //是否支持外部存储功能
        val isSupportExternalStorage = activity?.intent?.getBooleanExtra("isSupportExternalStorage", false)
                ?: false

        if (CMAPI.getInstance().isConnected) {//连接成功才请求
            activityViewModel.loading(true)
            //移除LoginSession
            SessionManager.getInstance().removeSession(viewModel.deviceId)
            //检查是否支持外部存储，每次进行新请求LoginSession
            SessionManager.getInstance().getLoginSession(viewModel.deviceId, object : GetSessionListener() {
                override fun onSuccess(url: String?, loginSession: LoginSession?) {
                    val public = loginSession?.userInfo?.permissions?.find {
                        it.sharePathType == SharePathType.PUBLIC.type && it.isWriteable
                    }
                    if (public != null) {//公共目录有可写权限
                        mPublishSwitch.visibility = View.VISIBLE
                    }

                    val extStorage = loginSession?.userInfo?.permissions?.find {
                        it.sharePathType == SharePathType.EXTERNAL_STORAGE.type && it.isWriteable
                    }
                    if (isSupportExternalStorage && extStorage != null) {//支持外部存储且有可写权限
                        mExternalStorageSwitch.visibility = View.VISIBLE
                        vSwitchBottom.visibility = View.VISIBLE
                    }
                    if (public == null && extStorage == null) {//空界面显示
                        layout_empty.visibility = View.VISIBLE
                    }
                    activityViewModel.startGetConfigScanPath(requireActivity().intent.getBooleanExtra(AppConstants.SP_FIELD_DEVICE_IS_ADMIN, false))
                }

                override fun onFailure(url: String?, errorNo: Int, errorMsg: String?) {
                    activityViewModel.loading(false)
                    layout_empty.visibility = View.VISIBLE
                    super.onFailure(url, errorNo, errorMsg)
                    ToastUtils.showToast(V5HttpErrorNo.getResourcesId(errorNo))
                }
            })
        } else {
            layout_empty.visibility = View.VISIBLE
            ToastUtils.showToast(R.string.network_not_available)
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initObserver()
    }

    //用于过滤非用户主动触发事件
    private var isCheckedPublicOlny = false
    private val publishCheckedListener = CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
        if (!isCheckedPublicOlny) {//用户手动触发，请求更改配置
            if (CMAPI.getInstance().isConnected) {//连接成功才请求
                activityViewModel.loading(true)
                viewModel.configPublicPath(isChecked)
            } else {//未连接
                ToastUtils.showToast(R.string.network_not_available)
                //恢复switch状态
                recoveryPublicSwitch()
            }
        }
    }

    //用于过滤非用户主动触发事件
    private var isCheckedExternalStorageOlny = false
    private val ExternalStoragecheckedListener = CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
        if (!isCheckedExternalStorageOlny) {//用户手动触发，请求更改配置
            if (CMAPI.getInstance().isConnected) {//连接成功才请求
                activityViewModel.loading(true)
                viewModel.configExternalStoregePath(isChecked)
            } else {//未连接
                ToastUtils.showToast(R.string.network_not_available)
                //恢复switch状态
                recoveryExternalStorageSwitch()
            }
        }
    }

    /**
     * 恢复公共空间switch状态
     */
    private fun recoveryPublicSwitch() {
        //控制 非用户主动事件
        this.isCheckedPublicOlny = true
        mPublishSwitch.isChecked = activityViewModel.isScanPublicPath.value
                ?: false
        isCheckedPublicOlny = false
    }

    /**
     * 恢复外部存储switch状态
     */
    private fun recoveryExternalStorageSwitch() {
        //控制 非用户主动事件
        this.isCheckedExternalStorageOlny = true
        mExternalStorageSwitch.isChecked = activityViewModel.isScanExternalStoragePath.value
                ?: false
        isCheckedExternalStorageOlny = false
    }

    private fun initObserver() {
        activityViewModel.getConfigScanPath.observe(this, Observer {
            activityViewModel.loading(false)
            recoveryPublicSwitch()
            recoveryExternalStorageSwitch()
        })
        //配置外部存储扫描结果
        viewModel.configExtStoregePathResult.observe(this, Observer {
            activityViewModel.loading(false)
            if (it) {//请求成功，驱动activity页面更改状态
                activityViewModel.scanExternalStoragePathOpened(viewModel.isScanExternalStoragePath.value
                        ?: false)
            } else {//请求失败恢复
                recoveryExternalStorageSwitch()
            }
        })

        //配置公共空间扫描结果
        viewModel.configPublicPathResult.observe(this, Observer {
            activityViewModel.loading(false)
            if (it) {//请求成功，驱动activity页面更改状态
                activityViewModel.scanPublicPathOpened(viewModel.isScanPublicPath.value
                        ?: false)
            } else {//请求失败恢复
                recoveryPublicSwitch()
            }
        })

        //监听用户主动取消进度条事件，取消请求
        activityViewModel.cancelLoading.observe(this, Observer {
            if (it != null) viewModel.dispose()
        })
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_dnla_setting_scan_path
    }
}