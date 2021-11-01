package net.linkmate.app.ui.nas.user

import android.os.Bundle
import androidx.navigation.fragment.NavHostFragment
import net.linkmate.app.R
import net.sdvn.nascommon.BaseActivity

class UserManageActivity : BaseActivity() {

    private lateinit var navHostFragment: NavHostFragment

    override fun getLayoutId(): Int {
        return R.layout.nas_activity_user_manage
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) {
            navHostFragment = NavHostFragment.create(R.navigation.user_nav,
                    deviceId?.let { UserManageFragmentArgs(it).toBundle() })
            supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_content, navHostFragment)
                    .setPrimaryNavigationFragment(navHostFragment)
                    .commitAllowingStateLoss()
        }
    }
}