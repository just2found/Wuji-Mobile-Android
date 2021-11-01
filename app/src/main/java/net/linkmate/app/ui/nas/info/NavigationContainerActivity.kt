package net.linkmate.app.ui.nas.info

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.navigation.fragment.NavHostFragment
import net.linkmate.app.R
import net.linkmate.app.ui.nas.safe_box.control.SafeBoxModel
import net.linkmate.app.ui.nas.safe_box.list.SafeBoxNasFileActivity
import net.linkmate.app.ui.nas.safe_box.list.SafeBoxQuickCloudNavFragmentArgs
import net.sdvn.nascommon.BaseActivity


//通用的NavigationContainer容器类
class NavigationContainerActivity : BaseActivity() {


    companion object {

        const val NAVIGATION_ID = "navigationId"

        fun startContainerActivity(navigationId: Int, devId: String, context: Context) {
            val intent = Intent(context, NavigationContainerActivity::class.java)
                .putExtra(io.weline.repo.files.constant.AppConstants.SP_FIELD_DEVICE_ID, devId)
                .putExtra(NAVIGATION_ID, navigationId)
            context.startActivity(intent)
        }
    }


    private lateinit var navHostFragment: NavHostFragment

    var navigationId: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initFullScreen()
        navigationId = intent.getIntExtra(NAVIGATION_ID, navigationId ?: -1)
        navigationId?.let { navId ->
            navHostFragment = NavHostFragment.create(
                navId,
                SafeBoxQuickCloudNavFragmentArgs(deviceId!!).toBundle()
            )//注意这里的
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment, navHostFragment)
                .setPrimaryNavigationFragment(navHostFragment)
                .commitAllowingStateLoss()
        }
    }

    override fun getLayoutId(): Int {
        return R.layout.activity_navigation_container
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == SafeBoxModel.CLOSE_ACTIVITY) {
            finish()
        }
    }





}