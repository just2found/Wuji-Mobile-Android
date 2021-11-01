package net.linkmate.app.ui.nas.samba

import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.TextUtils
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.View
import android.widget.CompoundButton
import android.widget.Switch
import android.widget.TextView
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import kotlinx.android.synthetic.main.fragment_samba.*
import kotlinx.android.synthetic.main.fragment_samba.mSwitch
import kotlinx.android.synthetic.main.fragment_samba.tvFunctionDescriptionContent
import kotlinx.android.synthetic.main.fragment_samba.tvStatusText
import net.linkmate.app.R
import net.linkmate.app.ui.fragment.BaseFragment
import net.linkmate.app.util.ToastUtils
import net.sdvn.cmapi.CMAPI
import net.sdvn.nascommon.constant.AppConstants

/**SAMBA功能
 * @author Raleigh.Luo
 * date：21/6/5 14
 * describe：
 */
class SAMBAFragment : BaseFragment() {
    private val viewModel: SAMBAViewModel by activityViewModels()
    override fun initView(view: View, savedInstanceState: Bundle?) {
        isStatusCheckedOnly = true
        lanScanVisibleCheckedOnly = true
        //是否时管理员或所有者
        val isAdmin = requireActivity().intent.getBooleanExtra(AppConstants.SP_FIELD_DEVICE_IS_ADMIN, false)
        //初始化状态
        initStatus(isAdmin, mSwitch, tvStatusText)
//        initStatus(isAdmin,mAnonymousAccessSwitch,tvAnonymousAccessText)
        initStatus(isAdmin, mLanScanVisibleSwitch, tvLanScanVisibleText)
        tvSettingPassword.setOnClickListener {
            //跳转到设置密码页面
            findNavController().navigate(SAMBAFragmentDirections.enterSettingPassword())
        }
        //显示功能说明
        showFunctionDescribe()
        initObserver()
    }

    override fun onResume() {
        super.onResume()
        //避免返回时，默认的check事件触发监听器
        mSwitch.setOnCheckedChangeListener(checkedListener)
        mLanScanVisibleSwitch.setOnCheckedChangeListener(checkedLanScanVisibleListener)
        isStatusCheckedOnly = false
        lanScanVisibleCheckedOnly = false
    }

    private fun initStatus(isAdmin: Boolean, mSwitch: Switch, tvStatusText: TextView) {
        if (isAdmin) {//管理员，限制状态按钮，可配置
            mSwitch.isEnabled = true
            mSwitch.visibility = View.VISIBLE
            tvStatusText.visibility = View.GONE
        } else {//普通用户，显示状态文字
            mSwitch.isEnabled = false
            mSwitch.visibility = View.INVISIBLE
            tvStatusText.visibility = View.VISIBLE
        }
    }

    //用于过滤非用户主动触发事件
    private var isStatusCheckedOnly = true
    private val checkedListener = CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
        if (!isStatusCheckedOnly) {//用户手动触发，请求更改配置
            viewModel.startConfig(isChecked)
        }
    }

    //用于过滤非用户主动触发事件
    private var lanScanVisibleCheckedOnly = true
    private val checkedLanScanVisibleListener = CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
        if (!lanScanVisibleCheckedOnly) {//用户手动触发，请求更改配置
            if (CMAPI.getInstance().isConnected) {//连接成功才请求
                viewModel.loading(true)
                viewModel.configLanScanVisible(isChecked)
            } else {
                ToastUtils.showToast(R.string.network_not_available)
                //恢复
                lanScanVisibleCheckedOnly = true
                mLanScanVisibleSwitch.isChecked = viewModel.lanScanVisible.value ?: false
                lanScanVisibleCheckedOnly = false
            }
        }
    }

    private fun initObserver() {
        //协议功能是否开启
        viewModel.functionOpened.observe(this, Observer {
            if (tvStatusText.visibility == View.GONE) {
                isStatusCheckedOnly = true
                mSwitch.isChecked = it
                isStatusCheckedOnly = false
            } else {
                tvStatusText.setText(if (it) R.string.enabled else R.string.forbidden)
            }
        })
        //局域网是否可见
        viewModel.lanScanVisible.observe(this, Observer {
            if (tvLanScanVisibleText.visibility == View.GONE) {
                lanScanVisibleCheckedOnly = true
                mLanScanVisibleSwitch.isChecked = it
                lanScanVisibleCheckedOnly = false
            } else {
                tvLanScanVisibleText.setText(if (it) R.string.enabled else R.string.forbidden)
            }
        })
        //配置协议服务结果
        viewModel.configServiceResult.observe(this, Observer {
            viewModel.loading(false)
        })

        //配置局域网可见结果
        viewModel.configLanScanVisibleResult.observe(this, Observer {
            viewModel.loading(false)
        })

        //获取协议服务状态
        viewModel.getServiceStatus.observe(this, Observer {
            checkLoadingCompleted()
        })
        //获取局域网是否可见状态
        viewModel.getLanScanVisible.observe(this, Observer {
            checkLoadingCompleted()
        })
        //获取用户名
        viewModel.getUserName.observe(this, Observer {
            checkLoadingCompleted()
            tvUserName.setText(it)
        })
    }

    override fun onDestroyView() {
        //页面被下一个destation替换时，需移除原有监听,避免重复监听
        viewModel.functionOpened.removeObservers(this)
        viewModel.lanScanVisible.removeObservers(this)
        viewModel.configServiceResult.removeObservers(this)
        viewModel.configLanScanVisibleResult.removeObservers(this)
        viewModel.getServiceStatus.removeObservers(this)
        viewModel.getLanScanVisible.removeObservers(this)
        viewModel.getUserName.removeObservers(this)
        super.onDestroyView()
    }

    /**
     * 检查是否已加载完成
     */
    private fun checkLoadingCompleted() {
        //非管理员，只需要加载userName
        if (viewModel.getLanScanVisible.value != null &&
                viewModel.getServiceStatus.value != null &&
                viewModel.getUserName.value != null)
            viewModel.loading(false)
    }

    /**
     * 显示功能说明
     */
    private fun showFunctionDescribe() {
        val ip = requireActivity().intent.getStringExtra(AppConstants.SP_FIELD_DEVICE_IP)
        val Windows = "\\\\" + ip
        val Mac = "smb://$ip"
        val text = String.format(getString(R.string.samba_function_content), Windows, Mac)
        val startIndexWindows = text.indexOf(Windows)
        val startIndexMac = text.indexOf(Mac)
        val describe = SpannableString(text)
        describe.setSpan(ForegroundColorSpan(resources.getColor(R.color.colorPrimary)),
                startIndexWindows, startIndexWindows + Windows.length, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
        describe.setSpan(ForegroundColorSpan(resources.getColor(R.color.colorPrimary)),
                startIndexMac, startIndexMac + Mac.length, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
        tvFunctionDescriptionContent.setText(describe)
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_samba
    }
}