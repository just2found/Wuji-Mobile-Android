package net.linkmate.app.ui.scan

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import kotlinx.android.synthetic.main.fragment_apply_logon.*
import libs.source.common.livedata.Resource
import libs.source.common.livedata.Status
import net.linkmate.app.R
import net.linkmate.app.ui.fragment.BaseFragment
import net.sdvn.cmapi.global.Constants
import org.view.libwidget.singleClick

/**
 *
 * @Description: 客户端登录认证
 * @Author: todo2088
 * @CreateDate: 2021/2/22 21:08
 */
class ApplyLogonFragment : BaseFragment() {
    private val navArgs: ApplyLogonFragmentArgs by navArgs()
    private val scanViewModel by viewModels<ScanViewModel>({
        requireActivity()
    })

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        scanViewModel.liveDataHttpLoad.observe(this, Observer<Resource<Any>> { (status, data) ->
            if (status === Status.SUCCESS) {
                requireActivity().finish()
            }
        })
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_apply_logon
    }

    override fun initView(view: View, savedInstanceState: Bundle?) {
        val parseScanResult = scanViewModel.parseScanResult(navArgs.scanResult)
        val ot = parseScanResult["ot"] as? String
        if (!ot.isNullOrEmpty()) {
            tv_operator_tips.text = when (ot) {
                Constants.OT_ANDROID.toString(),
                Constants.DT_ANDROID.toString() -> {
                    resources.getString(R.string.conf_login_atv)
                }
                Constants.OT_OSX.toString(),
                Constants.DT_MACOS.toString() -> {
                    resources.getString(R.string.conf_login_mac)
                }
                Constants.OT_WINDOWS.toString(),
                Constants.DT_WINDOWS.toString() -> {
                    resources.getString(R.string.conf_login_win)
                }
                else -> {
                    ""
                }
            }
//            tv_operator_tips.text = "${resources.getString(R.string.app_name)}  $otStr${resources.getString(R.string.apply_logon_confirm)} "
        }
        tv_confirm.setText(R.string.login)
        tv_confirm.singleClick {
            scanViewModel.requestSignIn(parseScanResult)
        }
        tv_cancel.singleClick {
            findNavController().popBackStack()
        }

    }


}