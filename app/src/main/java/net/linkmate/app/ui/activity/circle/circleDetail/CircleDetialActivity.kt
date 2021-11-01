package net.linkmate.app.ui.activity.circle.circleDetail

//getTipsBar 与tipsBar的get方法 kotlin 方法重名，所以需要重命名
import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import androidx.activity.viewModels
import androidx.core.os.bundleOf
import androidx.lifecycle.Observer
import androidx.navigation.Navigation
import kotlinx.android.synthetic.main.activity_circle_detail.*
import net.linkmate.app.R
import net.linkmate.app.base.BaseActivity
import net.linkmate.app.base.BaseViewModel
import net.linkmate.app.util.UIUtils
import net.linkmate.app.view.ProgressDialog
import net.linkmate.app.view.TipsBar
import kotlinx.android.synthetic.main.activity_circle_detail.tipsBar as mTipsBar

/**
 * @author Raleigh.Luo
 * date：20/8/13 17
 * describe：
 */
class CircleDetialActivity : BaseActivity() {
    val viewModel: CircleDetialViewModel by viewModels()
    private var mProgressDialog: ProgressDialog? = null

    companion object {
        fun startActivityForResult(activity: Activity, intent: Intent, requestCode: Int) {
            intent.setClass(activity, CircleDetialActivity::class.java)
            activity.startActivityForResult(intent, requestCode)
            activity.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        }

        fun startActivity(activity: Activity, intent: Intent) {
            intent.setClass(activity, CircleDetialActivity::class.java)
            activity.startActivity(intent)
            activity.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (intent.hasExtra(FunctionHelper.NETWORK_ID)) {
            viewModel.networkId = intent.getStringExtra(FunctionHelper.NETWORK_ID)
            //请求圈子详情
            viewModel.startRequestCircleDetail()
        }

        setContentView(R.layout.activity_circle_detail)
        initNoStatusBar()
        initObersver()
        val navController = Navigation.findNavController(this, R.id.fragment_nav_host)
        //指定了跳转页面，重新设置默认FragmentDirection参数
        if (intent.hasExtra(FunctionHelper.FUNCTION)) {
            val function = intent.getIntExtra(FunctionHelper.FUNCTION, 0)
            navController.navigate(R.id.enterDetial, bundleOf(FunctionHelper.FUNCTION to function))
        }


    }

    private fun initNoStatusBar() {
        // style的windowTranslucentNavigation设置为false后，状态栏无法达到沉浸效果
        // 设置UI FLAG 让布局能占据状态栏的空间，达到沉浸效果
        val option = (View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE)
        window.decorView.systemUiVisibility = option
        //头部宽度 height-24解决弹框顶部标题空隙不准确问题
        root.setPadding(0, UIUtils.getStatueBarHeight(this) - resources.getDimensionPixelSize(R.dimen.common_24), 0, 0)
        //修改状态栏为全透明
        window.setStatusBarColor(Color.TRANSPARENT);
        window.setNavigationBarColor(Color.TRANSPARENT);

    }

    override fun getTopView(): View? {
        return null
    }

    override fun getTipsBar(): TipsBar? {
        return mTipsBar
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
                        if (!it.isAdded && !it.isVisible()
                                && !it.isRemoving()) {
                            it.update(true)
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
        viewModel.circleDetail.observe(this, Observer {
            if (!TextUtils.isEmpty(it.getMainENDeviceId())) {
                viewModel.startGetCircleBrief(it.getMainENDeviceId()!!)
            } else {
                viewModel.startGetCircleBrief(null)
            }
        })
        viewModel.activityResult.observe(this, Observer {
            if (it.requestCode == FunctionHelper.CIRCLE_BRIEF) {
                //从简介页面返回，刷新头像
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
    }

    override fun onBackPressed() {
        viewModel.setLoadingStatus(false)
        //第一个页面
        val firstFunction = intent.getIntExtra(FunctionHelper.FUNCTION, FunctionHelper.CIRCLE_MANANGER)
        if (intent.getIntExtra(FunctionHelper.FUNCTION, 0) != FunctionHelper.CIRCLE_MANANGER) {//非圈子管理，单独跳进来的
            val navController = Navigation.findNavController(this, R.id.fragment_nav_host)
            //当前页面的function
            val currentPageFunction = navController.currentBackStackEntry?.arguments?.get(FunctionHelper.FUNCTION)
                    ?: 0
            if (currentPageFunction == firstFunction) {
                //已经返回到指定的第一个页面，直接finish
                finish()
            } else {
                super.onBackPressed()
            }
            finish()
        } else {
            super.onBackPressed()
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        viewModel.updateActivityResult(requestCode, resultCode, data)
    }

    override fun onResume() {
        super.onResume()
        viewModel.setSecondaryDialogShow(false)
    }

    override fun finish() {
        super.finish()
        viewModel.setLoadingStatus(false)
        overridePendingTransition(android.R.anim.fade_in, R.anim.device_slide_out_to_bottom);
    }
}