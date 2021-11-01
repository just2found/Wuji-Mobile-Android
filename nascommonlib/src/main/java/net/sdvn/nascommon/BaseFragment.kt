package net.sdvn.nascommon

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.fragment.app.Fragment
import libs.source.common.utils.AndUtils
import net.sdvn.nascommon.constant.AppConstants
import net.sdvn.nascommon.iface.RefWatcherProvider
import net.sdvn.nascommon.model.UiUtils
import net.sdvn.nascommon.receiver.NetworkStateManager.Companion.instance
import net.sdvn.nascommon.receiver.NetworkStateManager.OnNetworkStateChangedListener
import net.sdvn.nascommonlib.BuildConfig
import net.sdvn.nascommonlib.R
import timber.log.Timber

abstract class BaseFragment : Fragment(), OnNetworkStateChangedListener {
    var devId: String? = null
    protected var isNetAvailable = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            devId = it.getString(AppConstants.SP_FIELD_DEVICE_ID)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(getLayoutResId(), container, false)
    }

    private fun initNoStatusBar() {
        val topView: View? = getTopView()
        initStatusBarPadding(topView)
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
                UiUtils.getStatusBarOffsetPx(topView.context).also {
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


    /**
     * 用于实现了沉浸式状态栏的子类界面预留顶部空间
     * 不能把topview的height写死
     * @return 子类重写并返回需要预留空间的控件
     */
    protected open fun getTopView(): View? {
        return null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView(view, savedInstanceState)
    }

    open fun initView(view: View, savedInstanceState: Bundle?) {
        initView(view)
    }

    override fun onStart() {
        super.onStart()
        initNoStatusBar()
        instance.addNetworkStateChangedListener(this)
    }

    override fun onResume() {
        super.onResume()
        refreshStatusBar()
    }

    override fun onStop() {
        super.onStop()
        instance.removeNetworkStateChangedListener(this)
    }

    /**
     * @return 布局资源id
     */
    abstract fun getLayoutResId(): Int

    /**
     * 初始化View
     */
    abstract fun initView(view: View)
    open fun onBackPressed(): Boolean {
        return false
    }

    override fun onNetworkChanged(isAvailable: Boolean, isWifiAvailable: Boolean) {
        isNetAvailable = isAvailable
    }

    override fun onStatusConnection(statusCode: Int) {}
    override fun onDestroy() {
        super.onDestroy()
        if (BuildConfig.DEBUG && context != null &&
            requireContext().applicationContext is RefWatcherProvider
        ) {
            val refWatcher = (requireContext().applicationContext as RefWatcherProvider).refWatcher
            refWatcher?.watch(this, this.javaClass.name)
        }
    }
}