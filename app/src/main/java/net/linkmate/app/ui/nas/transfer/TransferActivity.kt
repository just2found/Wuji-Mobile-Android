package net.linkmate.app.ui.nas.transfer

import android.os.Bundle
import net.linkmate.app.R
import net.linkmate.app.ui.nas.transfer_r.TransferFragmentR
import net.sdvn.nascommon.BaseActivity

class TransferActivity : BaseActivity() {
    override fun getLayoutId(): Int {
        return R.layout.activity_nas_transfer
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_content, TransferFragmentR.newInstance(deviceId))
                    .commitAllowingStateLoss()
        }
    }
}
