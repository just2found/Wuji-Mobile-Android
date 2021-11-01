package net.linkmate.app.ui.nas.dnla

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
import kotlinx.android.synthetic.main.activity_dnla.tipsBar as mTipsBar

/**DLNA功能入口
 * @author Raleigh.Luo
 * date：21/6/4 18
 * describe：
 */
class DNLAActivity : BaseActivity() {
    private val viewModel: DNLAViewModel by viewModels()
    private lateinit var navController: NavController
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dnla)
        //设备id
        viewModel.deviceId = intent.getStringExtra(AppConstants.SP_FIELD_DEVICE_ID)
        viewModel.startGetConfigScanPath(intent.getBooleanExtra(AppConstants.SP_FIELD_DEVICE_IS_ADMIN, false))
        initView()
    }

    private fun initView() {
        //初始化标题
        itb_tv_title.setText(R.string.settting_join_way)
        itb_tv_title.setTextColor(resources.getColor(R.color.title_text_color))
        itb_iv_left.visibility = View.VISIBLE
        itb_iv_left.setImageResource(R.drawable.icon_return)
        itb_iv_left.setOnClickListener({ onBackPressed() })

        navController = Navigation.findNavController(this, R.id.fragment)
        navController.addOnDestinationChangedListener { controller, destination, arguments ->
            //fragment切换时，全局标题更改
            if (destination.id == R.id.settingScanPath) {
                itb_tv_title.setText(R.string.dnla_scanning_path_setting)
            } else {
                itb_tv_title.setText(R.string.DNLA)
            }
        }
        viewModel.cancelLoading.observe(this, Observer {
            if (it != null) {//用户手动取消进度条
                //取消任务
                viewModel.dispose()
                //关闭进度条
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

    /**
     * 监听cancelLoading事件
     */
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
        if (id == null || id == R.id.DLNA) {
            finish()
        } else {
            super.onBackPressed()
        }
    }

}