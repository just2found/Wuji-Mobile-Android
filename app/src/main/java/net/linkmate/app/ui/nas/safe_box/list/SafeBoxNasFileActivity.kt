package net.linkmate.app.ui.nas.safe_box.list

import android.content.Intent
import android.os.Bundle
import androidx.navigation.fragment.NavHostFragment
import net.linkmate.app.R
import net.linkmate.app.ui.nas.safe_box.control.SafeBoxModel
import net.sdvn.nascommon.BaseActivity
import org.view.libwidget.log.L

class SafeBoxNasFileActivity : BaseActivity() {


    private lateinit var navHostFragment: NavHostFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initFullScreen()
        L.i(deviceId, "onCreate", "SafeBoxNasFileActivity", "nwq", "2021/5/13");
        navHostFragment = NavHostFragment.create(R.navigation.safe_box_list_nav,
                SafeBoxQuickCloudNavFragmentArgs(deviceId!!).toBundle())//注意这里的
        supportFragmentManager.beginTransaction()
                .replace(R.id.fragment, navHostFragment)
                .setPrimaryNavigationFragment(navHostFragment)
                .commitAllowingStateLoss()
    }

    override fun getLayoutId(): Int {
        return R.layout.activity_safe_box_nas_file
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == SafeBoxModel.CLOSE_ACTIVITY) {
            finish()
        }
    }

}