package net.linkmate.app.ui.nas.samba

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import kotlinx.android.synthetic.main.include_title_bar.*
import net.linkmate.app.R
import net.linkmate.app.base.BaseActivity
import net.linkmate.app.base.BaseViewModel
import net.linkmate.app.view.TipsBar
import net.sdvn.nascommon.constant.AppConstants
import kotlinx.android.synthetic.main.activity_samba.tipsBar as mTipsBar

/**SAMBA功能入口
 * @author Raleigh.Luo
 * date：21/6/5 14
 * describe：
 */
class SAMBAActivity : BaseActivity() {
    private val viewModel: SAMBAViewModel by viewModels()
    private lateinit var navController: NavController
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_samba)
        //设备id
        viewModel.deviceId = intent.getStringExtra(AppConstants.SP_FIELD_DEVICE_ID)
        //是否是管理员或所有者
        viewModel.isAdmin = intent.getBooleanExtra(AppConstants.SP_FIELD_DEVICE_IS_ADMIN, false)
        initView()
    }

    private fun initView() {
        itb_tv_title.setText(R.string.settting_join_way)
        itb_tv_title.setTextColor(resources.getColor(R.color.title_text_color))
        itb_iv_left.visibility = View.VISIBLE
        itb_iv_left.setImageResource(R.drawable.icon_return)
        itb_iv_left.setOnClickListener({ onBackPressed() })

        navController = Navigation.findNavController(this, R.id.fragment)
        navController.addOnDestinationChangedListener { controller, destination, arguments ->
            if (destination.id == R.id.settingPassword) {
                itb_tv_title.setText(R.string.device_lan_access)
            } else {
                itb_tv_title.setText(R.string.SAMBA)
            }
        }
        /**
         * 监听用户主动取消进度条事件
         */
        viewModel.cancelLoading.observe(this, Observer {
            if (it != null) {//关闭进度条，取消请求
                viewModel.dispose()
                dismissLoading()
                //重置,避免页面间相互影响
                viewModel.cancelLoading(null)
            }
        })

        //显示或关闭进度条 统一管理
        viewModel.loading.observe(this, Observer {
            if (it) {
                showLoading(R.string.loading, true)
            } else {
                dismissLoading()
            }
        })

    }

    override fun getViewModel(): BaseViewModel {
        return viewModel
    }

    override fun getTipsBar(): TipsBar? {
        return mTipsBar
    }

    override fun getTopView(): View {
        return itb_rl
    }

    override fun onEstablished() {
        super.onEstablished()
        if (!viewModel.isGetInitConfigSuccess()) {
            //初始化配置未请求成功，重新请求
            viewModel.startGetConfig()
        }
    }

    override fun onBackPressed() {
        val id = navController.currentDestination?.id
        if (id == null || id == R.id.Samba) {
            finish()
        } else {
            super.onBackPressed()
        }
    }
}