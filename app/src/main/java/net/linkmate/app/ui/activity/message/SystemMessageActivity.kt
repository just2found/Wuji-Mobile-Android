package net.linkmate.app.ui.activity.message

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import kotlinx.android.synthetic.main.include_title_bar.*
import net.linkmate.app.R
import net.linkmate.app.base.BaseActivity
import net.linkmate.app.ui.fragment.SystemMsgFragment
import net.linkmate.app.view.TipsBar

class SystemMessageActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_system_message2)
        itb_tv_title.setText(R.string.system_msg)
        itb_iv_left.isVisible = true
        itb_iv_left.setOnClickListener {
            onBackPressed()
        }
        itb_iv_left.setImageResource(R.drawable.icon_return);
        itb_tv_title.setTextColor(getResources().getColor(R.color.title_text_color));
        val tag = SystemMsgFragment::class.java.name
        val systemMsgFragment = supportFragmentManager.findFragmentByTag(tag) ?: SystemMsgFragment.newInstance(false)
        supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, systemMsgFragment, tag)
                .commitNowAllowingStateLoss()
    }

    override fun getTopView(): View {
        return itb_rl
    }

    override fun getTipsBar(): TipsBar? {
        return findViewById(R.id.tipsBar)
    }

}