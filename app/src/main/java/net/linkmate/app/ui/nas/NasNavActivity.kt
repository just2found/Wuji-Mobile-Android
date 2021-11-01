package net.linkmate.app.ui.nas

import android.os.Bundle
import androidx.fragment.app.Fragment
import libs.source.common.utils.ToastHelper
import net.linkmate.app.R
import net.linkmate.app.ui.nas.helper.LANAccessSettingFragment
import net.linkmate.app.ui.nas.helper.LANAccessSettingFragmentArgs
import net.sdvn.nascommon.BaseActivity
import net.sdvn.nascommon.constant.AppConstants

/**
 *
 * @Description: nas 相关界面
 * @Author: todo2088
 * @CreateDate: 2021/3/10 16:26
 */
class NasNavActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (deviceId.isNullOrEmpty()) {
            ToastHelper.showLongToast(R.string.tip_params_error)
            finish()
            return
        }

        val functionId = intent.getIntExtra(AppConstants.FUNCTION_ID, 0)
        val fragment = getFragmentByFunctionId(functionId)
        if (fragment != null) {
            supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_nav_host, fragment)
                    .commitAllowingStateLoss()
        }
    }

    override fun getLayoutId(): Int {
        return R.layout.activity_nas_nav
    }
    //根据id 显示界面
    private fun getFragmentByFunctionId(functionId: Int): Fragment? {
        return when (functionId) {
            DEVICE_LAN_ACCESS -> {
                LANAccessSettingFragment().apply {
                    this.arguments = LANAccessSettingFragmentArgs(deviceId!!).toBundle()
                }
            }
            else -> {
                null
            }
        }
    }

    companion object {
        //局域网访问
        const val DEVICE_LAN_ACCESS = 0x113
    }
}