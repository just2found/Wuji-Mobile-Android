package net.linkmate.app.ui.nas.safe_box.control

import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.navigation.findNavController
import net.linkmate.app.R
import net.linkmate.app.ui.nas.safe_box.control.SafeBoxModel.Companion.CONTROL
import net.linkmate.app.ui.nas.safe_box.control.SafeBoxModel.Companion.INITIALIZATION
import net.linkmate.app.ui.nas.safe_box.control.SafeBoxModel.Companion.LOGIN_TYPE
import net.sdvn.nascommon.BaseActivity

class SafeBoxControlActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initFullScreen()
        val option = intArrayOf(window.decorView.systemUiVisibility)
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            option[0] = option[0] or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }
        window.decorView.systemUiVisibility = option[0]

        val type = intent.getIntExtra(SafeBoxModel.SAFE_BOX_TYPE_KEY, CONTROL)
        findNavController(R.id.nav_fragment).apply {
            setGraph(navInflater.inflate(R.navigation.safe_box_control_nav).apply {
                startDestination = when (type) {
                    LOGIN_TYPE -> R.id.verifyPasswordFragment
                    INITIALIZATION -> R.id.initFragment
                    else -> R.id.setFragment
                }
            }, when (type) {
                LOGIN_TYPE -> {
                    VerifyPasswordFragmentArgs(deviceId!!, LOGIN_TYPE).toBundle()
                }
                INITIALIZATION -> {
                    SafeBoxInitFragmentArgs(deviceId).toBundle()
                }
                else -> {
                    SafeBoxSetFragmentArgs(deviceId).toBundle()
                }
            })
        }
    }

    override fun getLayoutId(): Int {
        return R.layout.activity_safe_box_control
    }


}