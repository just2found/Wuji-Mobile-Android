package net.linkmate.app.ui.nas.dnla

import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.CompoundButton
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import kotlinx.android.synthetic.main.fragment_dnla.*
import net.linkmate.app.R
import net.linkmate.app.ui.fragment.BaseFragment
import net.sdvn.nascommon.constant.AppConstants

/**DLNA功能页面
 * @author Raleigh.Luo
 * date：21/6/5 10
 * describe：
 */
class DNLAFragment : BaseFragment() {
    private val viewModel: DNLAViewModel by activityViewModels()
    override fun initView(view: View, savedInstanceState: Bundle?) {
        isStatusCheckedOnly = true
        tvSettingScanPath.setOnClickListener {
            findNavController().navigate(DNLAFragmentDirections.enterSettingScanPath())
        }
        //是否是管理员或所有者
        val isAdmin = requireActivity().intent.getBooleanExtra(AppConstants.SP_FIELD_DEVICE_IS_ADMIN, false)

        if (isAdmin) {//管理员或所有者，显示状态配置按钮
            mSwitch.isEnabled = true
            mSwitch.visibility = View.VISIBLE
            tvStatusText.visibility = View.GONE
            settingScanPathGroup.visibility = View.VISIBLE
        } else {//普通用户限制状态文字
            mSwitch.isEnabled = false
            mSwitch.visibility = View.INVISIBLE
            tvStatusText.visibility = View.VISIBLE
            settingScanPathGroup.visibility = View.GONE
        }
        initObserver()
    }

    override fun onResume() {
        super.onResume()
        //避免返回时，默认的check事件触发监听器
        mSwitch.setOnCheckedChangeListener(checkedListener)
        isStatusCheckedOnly = false
    }

    //用于过滤非用户主动触发事件
    private var isStatusCheckedOnly = true
    private val checkedListener = CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
        if (!isStatusCheckedOnly) {//用户手动触发，请求更改配置
            viewModel.startConfig(isChecked)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initObserver()
    }

    override fun onDestroyView() {
        //页面被下一个destation替换时，需移除原有监听,避免重复监听
        viewModel.functionOpened.removeObservers(this)
        viewModel.isScanExternalStoragePath.removeObservers(this)
        viewModel.isScanPublicPath.removeObservers(this)
        viewModel.configServiceResult.removeObservers(this)
        viewModel.getConfigScanPath.removeObservers(this)
        viewModel.getServiceStatus.removeObservers(this)
        super.onDestroyView()
    }

    private fun initObserver() {
        //协议功能是否开启
        viewModel.functionOpened.observe(this, Observer {
            if (tvStatusText.visibility == View.GONE) {
                //控制 非用户主动事件
                isStatusCheckedOnly = true
                mSwitch.isChecked = it
                isStatusCheckedOnly = false
            } else {
                //显示
                tvStatusText.setText(if (it) R.string.enabled else R.string.forbidden)
            }
        })
        //是扫描外部存储
        viewModel.isScanExternalStoragePath.observe(this, Observer {
            showScanPath()
        })
        //是否扫描公共空间
        viewModel.isScanPublicPath.observe(this, Observer {
            showScanPath()
        })
        //配置协议服务结果
        viewModel.configServiceResult.observe(this, Observer {
            viewModel.loading(false)
        })
        //获取配置扫描路径
        viewModel.getConfigScanPath.observe(this, Observer {
            checkGetConfigsCompleted()
        })
        //获取协议服务路径
        viewModel.getServiceStatus.observe(this, Observer {
            checkGetConfigsCompleted()
        })
    }

    /**
     * 检查是否获取配置信息已加载完成
     */
    private fun checkGetConfigsCompleted() {
        //非管理员，只需要加载userName
        if (viewModel.getServiceStatus.value != null &&
                viewModel.getServiceStatus.value != null)
            viewModel.loading(false)
    }


    /**
     * 显示可扫描的路径
     */
    private fun showScanPath() {
        var path = ""
        if (viewModel.isScanPublicPath.value == true) {
            path = getString(R.string.root_dir_name_public)
        }
        if (viewModel.isScanExternalStoragePath.value == true) {
            if (TextUtils.isEmpty(path)) {
                path = getString(R.string.external_storage)
            } else {
                path += "、" + getString(R.string.external_storage)
            }
        }
        tvScanPath.setText(getString(R.string.current_scan_path) + path)
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_dnla
    }
}
