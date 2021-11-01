package net.linkmate.app.ui.fragment.ads

import android.graphics.Color
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import androidx.activity.viewModels
import kotlinx.android.synthetic.main.include_bottom_container.*
import net.linkmate.app.R
import net.linkmate.app.base.BaseActivity
import net.linkmate.app.util.UIUtils
import net.linkmate.app.view.TipsBar

/**
 *
 * @Description: 首页广告弹窗
 * @Author: todo2088
 * @CreateDate: 2021/3/2 17:41
 */
class HomeAdsActivity : BaseActivity() {
    private val adsViewModel by viewModels<AdsViewModel>()

    override fun getTipsBar(): TipsBar? {
        return null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.include_bottom_container)
        initNoStatusBar()
        val tag = "ads_fragment";
        val ads_fragment = supportFragmentManager.findFragmentByTag(tag);
        val adsFragment = ads_fragment as? AdsImageFragment ?: AdsImageFragment();
        supportFragmentManager.beginTransaction()
                .replace(R.id.bottom_container, adsFragment, tag)
                .commitAllowingStateLoss();
        adsViewModel.parseIntent(intent)
    }

    private fun initNoStatusBar() {
//         style的windowTranslucentNavigation设置为false后，状态栏无法达到沉浸效果
//         设置UI FLAG 让布局能占据状态栏的空间，达到沉浸效果
        val option = (View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE)
        window.decorView.systemUiVisibility = option
        //头部宽度
        bottom_container.setPadding(0, UIUtils.getStatueBarHeight(this) - resources.getDimensionPixelSize(R.dimen.common_24), 0, 0)
        //修改状态栏为全透明
        window.setStatusBarColor(Color.TRANSPARENT);
        window.setNavigationBarColor(Color.TRANSPARENT);
    }

    override fun onBackPressed() {

    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK && event?.action == KeyEvent.ACTION_DOWN) {
            return false
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(0, R.anim.fragment_fade_exit)
    }
}