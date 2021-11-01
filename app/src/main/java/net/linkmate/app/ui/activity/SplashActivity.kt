package net.linkmate.app.ui.activity

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.View
import libs.source.common.livedata.Status
import net.linkmate.app.R
import net.linkmate.app.base.BaseActivity
import net.linkmate.app.ui.fragment.privacy.PrivacyFragment
import net.linkmate.app.util.MySPUtils
import net.linkmate.app.view.TipsBar
import net.sdvn.nascommon.iface.OnResultListener


class SplashActivity : BaseActivity() {
    private val mHideHandler = Handler()

    override fun onCreate(savedInstanceState: Bundle?) {
        setConnectionState(false)
        super.onCreate(savedInstanceState)
        // style的windowTranslucentNavigation设置为false后，状态栏无法达到沉浸效果
        // 设置UI FLAG 让布局能占据状态栏的空间，达到沉浸效果
        // style的windowTranslucentNavigation设置为false后，状态栏无法达到沉浸效果
        // 设置UI FLAG 让布局能占据状态栏的空间，达到沉浸效果
        val option = (View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE)
        window.decorView.systemUiVisibility = option
        setContentView(R.layout.activity_splash)
        if (intent.flags and Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT != 0) {
            finish()
            return
        }
//
//        val boolean = MySPUtils.getBoolean(this@SplashActivity, MySPUtils.FIELD_IS_AGREE_PRIVACY_POLICY, false)
//        if (!boolean) {
//            val newInstance = PrivacyFragment()
//            newInstance.setOnResultListener(OnResultListener {
//                if (it.status == Status.SUCCESS) {
//                    MySPUtils.saveBoolean(this@SplashActivity, MySPUtils.FIELD_IS_AGREE_PRIVACY_POLICY, true)
//                    next()
//                } else {
//                    finish()
//                }
//            })
//            supportFragmentManager.beginTransaction()
//                    .replace(R.id.activity_container, newInstance)
//                    .commit()
//
//        } else {
            mHideHandler.postDelayed({
                next()
            }, UI_ANIMATION_DELAY.toLong())
//        }
    }

    private fun next() {
        ThemeActivity.enterMainInstance(this)
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        mHideHandler.removeCallbacksAndMessages(null)
    }

    override fun getTipsBar(): TipsBar? {
        return null
    }

    companion object {
        private const val UI_ANIMATION_DELAY = 1000
    }
}
