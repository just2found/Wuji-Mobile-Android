package net.linkmate.app.ui.nas.share

import android.os.Bundle

import net.linkmate.app.R
import net.sdvn.nascommon.BaseActivity

class ShareActivity : BaseActivity() {
    override fun getLayoutId(): Int {
        return R.layout.activity_nas_upload
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(getLayoutId())
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_content, ShareV2Fragment.newInstance(deviceId))
                    .commitAllowingStateLoss()
        }
    }
}
