package net.linkmate.app.ui.nas.group.list

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.navigation.fragment.NavHostFragment
import io.weline.repo.files.data.SharePathType
import net.linkmate.app.R
import net.linkmate.app.ui.nas.safe_box.control.SafeBoxModel
import net.linkmate.app.ui.nas.safe_box.list.SafeBoxQuickCloudNavFragmentArgs
import net.sdvn.nascommon.BaseActivity
import org.view.libwidget.log.L

class GroupSpaceNasFileActivity : BaseActivity() {


    companion object {
        fun startActivity(context: Context, devId: String, groupId: Long) {
            context.startActivity(
                Intent(context, GroupSpaceNasFileActivity::class.java)
                    .putExtra(
                        io.weline.repo.files.constant.AppConstants.SP_FIELD_DEVICE_ID,
                        devId
                    ).putExtras(
                        GroupSpaceQuickCloudNavFragmentArgs(
                            devId,
                            groupId,
                            SharePathType.GROUP.type,
                            "/"
                        ).toBundle()
                    )
            )
        }
    }

    private lateinit var navHostFragment: NavHostFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initFullScreen()

        navHostFragment = NavHostFragment.create(
            R.navigation.group_space_list_nav,
            intent.extras
        )//注意这里的
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

    override fun onBackPressed() {
        super.onBackPressed()
    }


}