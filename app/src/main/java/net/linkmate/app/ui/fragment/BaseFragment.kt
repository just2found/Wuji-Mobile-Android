package net.linkmate.app.ui.fragment

import android.app.Dialog
import android.content.BroadcastReceiver
import android.content.DialogInterface
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.annotation.CallSuper
import androidx.annotation.ColorInt
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import libs.source.common.utils.AndUtils
import net.linkmate.app.R
import net.linkmate.app.base.BaseActivity
import net.linkmate.app.base.BaseActivity.LoadingStatus
import net.linkmate.app.manager.SDVNManager
import net.linkmate.app.receiver.DevNetworkBroadcastReceiver
import net.linkmate.app.receiver.DevNetworkBroadcastReceiver.DevNetworkChangedObserver
import net.linkmate.app.ui.activity.LoginActivity
import net.linkmate.app.util.UIUtils
import net.linkmate.app.view.TipsBar
import net.sdvn.cmapi.CMAPI
import net.sdvn.cmapi.global.Constants
import net.sdvn.cmapi.protocal.ConnectStatusListenerPlus
import net.sdvn.nascommon.iface.RefWatcherProvider
import net.sdvn.nascommon.receiver.NetworkStateManager
import net.sdvn.nascommon.utils.DialogUtils
import net.sdvn.nascommon.utils.DialogUtils.OnDialogClickListener
import net.sdvn.nascommonlib.BuildConfig
import timber.log.Timber

abstract class BaseFragment() : Fragment() {
    private var compositeDisposable: CompositeDisposable? = null
    fun addDisposable(disposable: Disposable) {
        if (compositeDisposable == null) {
            compositeDisposable = CompositeDisposable()
        }
        compositeDisposable!!.add(disposable)
    }

    fun dispose() {
        if (compositeDisposable != null) compositeDisposable!!.dispose()
    }

    @CallSuper
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Timber.tag(javaClass.simpleName).d("onCreate lifecycle")
        SDVNManager.instance.liveDataConnectionStatus.observe(
            this,
            Observer { currentStatus: Int ->
                onStatusChange(currentStatus)
            }
        )
    }

    private fun onStatusChange(currentStatus: Int) {
        when (currentStatus) {
            Constants.CS_UNKNOWN, Constants.CS_PREPARE -> {
            }
            Constants.CS_CONNECTED -> mConnectStatusListener.onConnected()
            Constants.CS_CONNECTING, Constants.CS_WAIT_RECONNECTING -> mConnectStatusListener.onConnecting()
            Constants.CS_DISCONNECTING -> mConnectStatusListener.onDisconnecting()
            Constants.CS_ESTABLISHED -> mConnectStatusListener.onEstablished()
            Constants.CS_AUTHENTICATED -> mConnectStatusListener.onAuthenticated()
            Constants.CS_DISCONNECTED -> mConnectStatusListener.onDisconnected(
                0
            )
            else -> {
            }
        }
        Timber.tag("{SDVN}")
            .i("currentFragment: ${javaClass.simpleName}  currentStatus:${currentStatus}")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(getLayoutId(), container, false)
    }

    protected open fun initStatusBarPadding(topView: View?) {
        val fragmentActivity = activity
        if (fragmentActivity != null) {
            // style的windowTranslucentNavigation设置为false后，状态栏无法达到沉浸效果
            // 设置UI FLAG 让布局能占据状态栏的空间，达到沉浸效果
            var option = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            fragmentActivity.window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            fragmentActivity.window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            if (AndUtils.isLightColor(titleColor())) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    option = option or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                }
            } else {
                option = option or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            }
            fragmentActivity.window.decorView.systemUiVisibility = option
            topView?.setPadding(
                topView.paddingLeft,
                UIUtils.getStatueBarHeight(topView.context).also {
                    if (BuildConfig.DEBUG) {
                        Timber.d("StatusBarOffsetPx :$it")
                    }
                },
                topView.paddingRight,
                topView.paddingBottom
            )
        }
    }

    open fun isLightColor() =
        AndUtils.isLightColor(titleColor())

    @ColorInt
    open fun titleColor() = resources.getColor(R.color.bg_title_start_color)

    protected open fun refreshStatusBar() {
        val topView = getTopView()
        val fragmentActivity = activity
        if (fragmentActivity != null && topView != null) {
            val option = intArrayOf(fragmentActivity.window.decorView.systemUiVisibility)
            fragmentActivity.window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            fragmentActivity.window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)

            AndUtils.calcStatusBarViewPrimaryColor(
                topView,
                titleColor()
            ) { data: Int? ->
                if (AndUtils.isLightColor(data!!)) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        option[0] = option[0] or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                    }
                } else {
                    option[0] = option[0] or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                }
                fragmentActivity.window.decorView.systemUiVisibility = option[0]
            }
        }
    }

    protected fun initNoStatusBar() {
        val topView = getTopView()
        initStatusBarPadding(topView)
    }

    protected fun resetNoStatusBar() {
        val topView = getTopView()
        if (activity != null && topView != null) {
            topView.setPadding(
                topView.paddingLeft, topView.paddingTop,
                topView.paddingRight, topView.paddingBottom
            )
        }
    }

    /**
     * 用于实现了沉浸式状态栏的子类界面预留顶部空间
     *
     * @return 子类重写并返回需要预留空间的控件
     */

    protected open fun getTopView(): View? {
        return null
    }

    override fun onDestroyView() {
        super.onDestroyView()
        releaseView()
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)
        Timber.tag(javaClass.simpleName).d("initView lifecycle")
        initView(view, savedInstanceState)
        refreshData()
    }

    open fun refreshData() {}
    protected abstract fun getLayoutId(): Int
    protected abstract fun initView(
        view: View,
        savedInstanceState: Bundle?
    )

    protected fun releaseView() {
        Timber.tag(this.javaClass.simpleName).d("releaseView lifecycle")
    }

    override fun onDestroy() {
        dispose()
        super.onDestroy()
        if (BuildConfig.DEBUG && (context != null) && requireContext().applicationContext is RefWatcherProvider) {
            val refWatcher =
                (requireContext().applicationContext as RefWatcherProvider).refWatcher
            refWatcher?.watch(this, this.javaClass.name)
        }
    }

    private var isNetworkConnected = true
    override fun onStart() {
        super.onStart()
        initNoStatusBar()
        DevNetworkBroadcastReceiver.getInstance().registerObserver(mDevNetworkChangedObserver)
        //        CMAPI.getInstance().addConnectionStatusListener(mConnectStatusListener);
    }

    override fun onResume() {
        super.onResume()
        refreshStatusBar()
        if (activity != null) isNetworkConnected =
            NetworkStateManager.instance.isNetAvailable()
        val statusValue = SDVNManager.instance.liveDataConnectionStatus.value
        Timber.tag("{SDVN}").i("currentFragment:" + javaClass.simpleName + " onResume")
        statusValue?.let { onStatusChange(it) }
        Timber.tag(javaClass.simpleName).d("onResume lifecycle")
    }

    override fun onStop() {
        super.onStop()
        DevNetworkBroadcastReceiver.getInstance().unregisterObserver(mDevNetworkChangedObserver)
        //        CMAPI.getInstance().removeConnectionStatusListener(mConnectStatusListener);
    }

    //是否初始化过，仅一次，设置监听器
    private var isInitTipsBar = false

    //返回登录提示框
    private var mExitDialog: Dialog? = null
    private fun checkTipsBar(): Boolean {
        val tipsBar = getHomeTipsBar()
        var result = tipsBar != null
        if (activity is BaseActivity && (activity as BaseActivity?)!!.status != LoadingStatus.DEFUALT) {
            //有dialog不显示
            result = false
        }
        if (result && !isInitTipsBar) {
            isInitTipsBar = true
            tipsBar!!.setCloseClickListener(View.OnClickListener { tipsBar.visibility = View.GONE })
            tipsBar.setLinkClickListener(object : View.OnClickListener {
                override fun onClick(view: View) {
                    if (CMAPI.getInstance().isDisconnected) {
                        //退出登录
                        startActivity(
                            Intent(
                                activity,
                                LoginActivity::class.java
                            )
                        )
                    } else {
                        mExitDialog = DialogUtils.showConfirmDialog(
                            activity,
                            0,
                            R.string.stop_connect,
                            R.string.confirm,
                            R.string.cancel,
                            object : OnDialogClickListener {
                                override fun onClick(
                                    dialog: DialogInterface,
                                    isPositiveBtn: Boolean
                                ) {
                                    if (isPositiveBtn) {
                                        //未连接前可取消
                                        if (!isNetworkConnected || CMAPI.getInstance().baseInfo
                                                .status != Constants.CS_CONNECTED
                                        ) {
                                            CMAPI.getInstance().cancelLogin()
                                            //退出登录
                                            startActivity(
                                                Intent(
                                                    activity,
                                                    LoginActivity::class.java
                                                )
                                            )
                                        }
                                    }
                                }
                            })
                    }
                }
            })
        }
        return result
    }

    /**
     * 显示标题提示
     * 优先显示网络连接失败
     */
    private fun showTipsBar(title: String, cancelable: Boolean): Boolean {
        val isExitTipsBar = checkTipsBar()
        if (isExitTipsBar) {
            getHomeTipsBar()!!.setWarning(if (isNetworkConnected) title else getString(R.string.network_not_available))
        }
        return isExitTipsBar
    }

    /**
     * 显示标题提示
     * 优先显示网络连接失败
     */
    private fun showTipsBar(title: Int, cancelable: Boolean): Boolean {
        val isExitTipsBar = checkTipsBar()
        if (isExitTipsBar) {
            getHomeTipsBar()!!.setWarning(if (isNetworkConnected) title else R.string.network_not_available)
        }
        return isExitTipsBar
    }

    /**
     * 隐藏标题提示
     */
    private fun hideTipsBar(): Boolean {
        if (getHomeTipsBar() != null) {
            getHomeTipsBar()!!.visibility = View.GONE
            return true
        } else {
            return false
        }
    }

    fun setStatus(status: LoadingStatus?) {
        if (activity != null) {
            (requireActivity() as BaseActivity).status = status
        }
    }

    open fun dismissLoading() {
        hideTipsBar()
        if (mExitDialog != null && mExitDialog!!.isShowing) mExitDialog!!.dismiss()
    }

    fun showLoading(title: Int) {
        showTipsBar(title, false)
    }

    protected open fun getHomeTipsBar(): TipsBar? {
        return null
    }


    private val mDevNetworkChangedObserver: DevNetworkChangedObserver =
        object : DevNetworkChangedObserver {
            override fun update(
                receiver: BroadcastReceiver,
                arg: Any
            ) {
                isNetworkConnected = arg as Boolean
                if (!isNetworkConnected && (getHomeTipsBar() != null) && (getHomeTipsBar()!!.visibility == View.VISIBLE)) {
                    showTipsBar(R.string.network_not_available, true)
                }
            }
        }
    private val mConnectStatusListener: ConnectStatusListenerPlus =
        object : ConnectStatusListenerPlus {
            override fun onAuthenticated() {
                showLoading(R.string.loading_data)
            }

            override fun onConnected() {
                showLoading(R.string.loading_data)
            }

            override fun onConnecting() {
                showLoading(R.string.connecting)
            }

            override fun onDisconnecting() {
                showLoading(R.string.disconnecting)
            }

            override fun onEstablished() {
                dismissLoading()
            }

            override fun onDisconnected(reason: Int) {
                showLoading(R.string.disconnected)
            }
        }
}
