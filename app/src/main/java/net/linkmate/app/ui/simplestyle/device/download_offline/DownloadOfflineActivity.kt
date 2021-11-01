package net.linkmate.app.ui.simplestyle.device.download_offline

import android.os.Bundle
import androidx.navigation.fragment.NavHostFragment
import net.linkmate.app.R
import net.linkmate.app.base.BaseActivity
import net.linkmate.app.ui.nas.helper.SelectTypeFragmentArgs
import net.linkmate.app.view.TipsBar

class DownloadOfflineActivity : BaseActivity() {

    private lateinit var navHostFragment: NavHostFragment
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_download_offline)
        navHostFragment = NavHostFragment.create(R.navigation.download_offline_nav,
                SelectTypeFragmentArgs(deviceId).toBundle())
        supportFragmentManager.beginTransaction()
                .replace(R.id.fragment, navHostFragment)
                .setPrimaryNavigationFragment(navHostFragment)
                .commitAllowingStateLoss()
    }


    override fun onBackPressed() {
        if (NavHostFragment.findNavController(navHostFragment).currentDestination!!.id == R.id.indexFragment) {
            finish()
        }
        super.onBackPressed()
    }

    override fun getTipsBar(): TipsBar? {
        return null
    }

}