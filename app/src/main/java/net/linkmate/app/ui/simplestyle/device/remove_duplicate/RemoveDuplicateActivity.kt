package net.linkmate.app.ui.simplestyle.device.remove_duplicate

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.NavHostFragment
import net.linkmate.app.R
import net.linkmate.app.base.BaseActivity
import net.linkmate.app.ui.nas.helper.SelectTypeFragmentArgs
import net.linkmate.app.view.TipsBar

class RemoveDuplicateActivity : BaseActivity() {

    private lateinit var navHostFragment: NavHostFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_remove_duplicate)
        navHostFragment = NavHostFragment.create(R.navigation.remove_duplicate_nav,
                SelectTypeFragmentArgs(deviceId).toBundle())
        supportFragmentManager.beginTransaction()
                .replace(R.id.fragment, navHostFragment)
                .setPrimaryNavigationFragment(navHostFragment)
                .commitAllowingStateLoss()
    }


    override fun getTipsBar(): TipsBar? {
        return null
    }
}