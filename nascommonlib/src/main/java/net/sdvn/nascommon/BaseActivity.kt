package net.sdvn.nascommon

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.PopupWindow
import androidx.annotation.ColorInt
import androidx.appcompat.app.AppCompatActivity
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import libs.source.common.utils.AndUtils
import net.sdvn.nascommon.constant.AppConstants
import net.sdvn.nascommon.iface.ILoadingCallback
import net.sdvn.nascommon.receiver.NetworkStateManager.Companion.instance
import net.sdvn.nascommon.receiver.NetworkStateManager.OnNetworkStateChangedListener
import net.sdvn.nascommon.utils.StatusBarUtils
import net.sdvn.nascommon.widget.LoadingView
import net.sdvn.nascommon.widget.TipView
import net.sdvn.nascommonlib.R

/**
 * Base Activity for OneSpace
 *
 *
 * Created by gaoyun@eli-tech.com on 2016/1/7.
 */
abstract class BaseActivity : AppCompatActivity(), ILoadingCallback {
    protected var mRootView: View? = null

    //    private SystemBarManager mTintManager;
    private val mLoadingView: LoadingView by lazy {
        LoadingView.getInstance()
    }
    private var mTipView: TipView? = null
    protected var isNetAvailable = false
    protected var isWifiAvailable = false

    //    protected boolean mIsNeedNetInitStatus = true;
    protected var deviceId: String? = null

    @SuppressLint("ObsoleteSdkInt")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initFullScreen()
        setContentView(getLayoutId())

//        StatusBarUtils.from(this)
//                .setLightStatusBar(false)
//                .setTransparentStatusBar(false)
//                .process()

//       Logger.LOGD(TAG, getRunningActivityName() + "==============baseActivity onCreate");
//        ActivityCollector.addActivity(this);
//        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
//            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
//                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
//        }
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            getWindow().setStatusBarColor(Color.TRANSPARENT);
//        }
//        if (Build.VERSION.SDK_INT >= 28) {
//            WindowManager.LayoutParams lp = getWindow().getAttributes();
//            lp.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
//            getWindow().setAttributes(lp);
//        }
        if (isNeedNetInitStatus()) instance.addNetworkStateChangedListener(mNetworkListener) else instance.addNetworkStateChangedListenerWithoutInitStatus(mNetworkListener)
        val intent = intent
        if (intent != null) deviceId = intent.getStringExtra(AppConstants.SP_FIELD_DEVICE_ID)
    }

    protected abstract fun getLayoutId(): Int

    protected open fun isNeedNetInitStatus(): Boolean {
        return true
    }

    protected var statusCode = 0
    private var compositeDisposable: CompositeDisposable? = null
    fun addDisposable(disposable: Disposable) {
        if (compositeDisposable == null) {
            compositeDisposable = CompositeDisposable()
        }
        compositeDisposable!!.add(disposable)
    }

    protected fun dispose() {
        if (compositeDisposable != null) compositeDisposable!!.dispose()
    }

    private val mNetworkListener: OnNetworkStateChangedListener = object : OnNetworkStateChangedListener {
        override fun onNetworkChanged(isAvailable: Boolean, isWifiAvailable: Boolean) {
            isNetAvailable = isAvailable
            this@BaseActivity.isWifiAvailable = isWifiAvailable
            onChanged(isAvailable, isWifiAvailable)
        }

        override fun onStatusConnection(statusCode: Int) {
            this@BaseActivity.statusCode = statusCode
            this@BaseActivity.onStatusConnection(statusCode)
        }
    }

    val isConnected: Boolean
        get() = instance.isEstablished()

    fun onChanged(isAvailable: Boolean, isWifiAvailable: Boolean) {}
    fun onStatusConnection(statusCode: Int) {}
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
    }

    fun initNoStatusBar() {
        val topView = getTopView()
        // style的windowTranslucentNavigation设置为false后，状态栏无法达到沉浸效果
        // 设置UI FLAG 让布局能占据状态栏的空间，达到沉浸效果
        var option = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        //            if (AndUtils.isLightColor(AndUtils.getStatusBarViewPrimaryColor(topView))) {
        if (AndUtils.isLightColor(titleColor())) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                option = option or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            }
        } else {
            option = option or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        }
        window.decorView.systemUiVisibility = option
        if (topView != null) {
            topView.setPadding(topView.paddingLeft, StatusBarUtils.getStatusBarOffsetPx(this),
                    topView.paddingRight, topView.paddingBottom)
        }
    }

    @ColorInt
    protected open fun titleColor(): Int {
        return resources.getColor(R.color.bg_title_start_color)
    }


    protected open fun refreshStatusBar() {
        val topView = getTopView()
        if (topView != null) {
            val option = intArrayOf(window.decorView.systemUiVisibility)
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            AndUtils.calcStatusBarViewPrimaryColor(topView, titleColor()) { data: Int? ->
                if (AndUtils.isLightColor(data!!)) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        option[0] = option[0] or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                    }
                } else {
                    option[0] = option[0] or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                }
                window.decorView.systemUiVisibility = option[0]
            }
        }
    }


    open fun getTopView(): View? {
        return null
    }

    override fun onStart() {
        super.onStart()
        initNoStatusBar()
    }

    override fun onPause() {
        super.onPause()
        //       Logger.LOGD(TAG, getRunningActivityName() + "==============baseActivity onPause");
    }

    override fun onResume() {
        super.onResume()
        refreshStatusBar()
    }

    override fun onDestroy() {
        mLoadingView.detachContext()
        if (mTipView != null) mTipView!!.dismiss()
        super.onDestroy()
        instance.removeNetworkStateChangedListener(mNetworkListener)
        //        ActivityCollector.removeActivity(this);
//       Logger.LOGD(TAG, getRunningActivityName() + "==============baseActivity onDestroy");
        dispose()
    }

    /**
     * Modify System Status Bar Style
     */
    protected fun initSystemBarStyle() {
//        initSystemBarStyle(R.color.black);
    }

    protected fun initFullScreen() {
        //5.x开始需要把颜色设置透明，否则导航栏会呈现系统默认的浅灰色
        //5.x开始需要把颜色设置透明，否则导航栏会呈现系统默认的浅灰色
        val decorView: View = window.decorView
        //两个 flag 要结合使用，表示让应用的主体内容占用系统状态栏的空间
        //两个 flag 要结合使用，表示让应用的主体内容占用系统状态栏的空间
        val option = (View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE)
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        decorView.systemUiVisibility = option
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.setStatusBarColor(Color.TRANSPARENT)
    }

    /**
     * Modify System Status Bar Style
     *
     * @param colorId Status Bar background color resource id
     */
    protected fun initSystemBarStyle(colorId: Int) {
//        if (null == mTintManager) {
//            mTintManager = new SystemBarManager(this);
//        }
//        mTintManager.setStatusBarTintEnabled(true);
//        mTintManager.setStatusBarTintResource(colorId);
//        mTintManager.setNavigationBarTintEnabled(true);
//        mTintManager.setNavigationBarTintResource(colorId);
    }

    override fun showLoading() {
        mLoadingView.show(this)
    }

    override fun showLoading(msgId: Int) {
        mLoadingView.show(this, msgId)
    }

    override fun showLoading(msgId: Int, isCancellable: Boolean) {
        mLoadingView.show(this, msgId, isCancellable)
    }

    /**
     * @param timeout :Unit second
     */
    fun showLoading(msgId: Int, timeout: Long, listener: DialogInterface.OnDismissListener?) {
        mLoadingView.show(this, msgId, timeout, listener)
    }

    fun showLoading(msgId: Int, isCancellable: Boolean, listener: DialogInterface.OnDismissListener?) {
        mLoadingView.show(this, msgId, isCancellable, -1, listener)
    }

    fun loadingIsShown(): Boolean {
        return mLoadingView.isShown
    }

    override fun dismissLoading() {
        mLoadingView.dismiss()
    }

    override fun showTipView(msgId: Int, isPositive: Boolean) {
        dismissLoading()
        if (!isFinishing) mTipView = TipView.show(this, mRootView, msgId, isPositive)
    }

    fun showTipView(msgId: Int, isPositive: Boolean, listener: PopupWindow.OnDismissListener?) {
        dismissLoading()
        if (!isFinishing) mTipView = TipView.show(this, mRootView, msgId, isPositive, listener)
    }

    override fun showTipView(msg: String, isPositive: Boolean) {
        dismissLoading()
        if (!isFinishing) mTipView = TipView.show(this, mRootView, msg, isPositive)
    }

    fun controlActivity(action: String?): Boolean {
        return false
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
    }

    /*
    private String getRunningActivityName() {
        ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        String runningActivity = activityManager.getRunningTasks(1).get(0).topActivity.getClassName();
        return runningActivity;
    }*/
    //    public <T extends View> T getView(int id) {
    //        return (T) findViewById(id);
    //    }
    override fun finish() {
        finish(TRANSION_FLAG_CODE_LEFT_RIGHT)
    }

    fun finish(flag: Int) {
        super.finish()
        when (flag) {
            TRANSION_FLAG_CODE_LEFT_RIGHT -> overridePendingTransition(R.anim.slide_left_in, R.anim.slide_right_out)
            TRANSION_FLAG_CODE_RIGHT_LEFT -> overridePendingTransition(R.anim.slide_right_in, R.anim.slide_left_out)
            TRANSION_FLAG_CODE_LEFT -> overridePendingTransition(0, R.anim.slide_right_out)
            TRANSION_FLAG_CODE_RIGHT -> overridePendingTransition(0, R.anim.slide_left_out)
            TRANSION_FLAG_CODE_FADE -> overridePendingTransition(R.anim.alpha_appear, R.anim.alpha_disappear)
        }
    }

    protected val activity: BaseActivity
        protected get() = this

    companion object {
        const val TRANSION_FLAG_CODE_LEFT = 0x1
        const val TRANSION_FLAG_CODE_RIGHT = 0x2
        const val TRANSION_FLAG_CODE_LEFT_RIGHT = 0x4
        const val TRANSION_FLAG_CODE_RIGHT_LEFT = 0x8
        const val TRANSION_FLAG_CODE_FADE = 0x32
        protected val TAG = BaseActivity::class.java.simpleName
    }
}