package net.linkmate.app.ui.simplestyle.device.disk


import android.os.Bundle
import android.view.View
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import kotlinx.android.synthetic.main.include_title_bar.*
import net.linkmate.app.R
import net.linkmate.app.base.BaseActivity
import net.linkmate.app.ui.nas.helper.SelectTypeFragmentArgs
import net.linkmate.app.ui.nas.safe_box.control.SafeBoxInitFragmentArgs
import net.linkmate.app.ui.nas.safe_box.control.SafeBoxModel
import net.linkmate.app.ui.nas.safe_box.control.SafeBoxSetFragmentArgs
import net.linkmate.app.ui.nas.safe_box.control.VerifyPasswordFragmentArgs
import net.linkmate.app.view.TipsBar
import org.view.libwidget.singleClick

//磁盘空间管理类
class DiskSpaceActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_disk_space)
        itb_iv_left.visibility = View.VISIBLE
        itb_iv_left.setImageResource(R.drawable.icon_return)
        itb_iv_left.singleClick {
            onBackPressed()
        }

        //根据DISK_SPACE_MODEL_KEY 确定导航的第一个界面
        val type = intent.getIntExtra(DiskSpaceModel.DISK_SPACE_MODEL_KEY, DiskSpaceModel.SPACE_MANAGEMENT)
        findNavController(R.id.placeholder_view).apply {
            setGraph(
                navInflater.inflate(R.navigation.disk_manage_nav).apply {
                    startDestination = when (type) {
                        DiskSpaceModel.SPACE_MANAGEMENT -> R.id.diskSpaceFragment
                        DiskSpaceModel.FORMAT -> R.id.diskManageFragment
                        else -> R.id.diskSpaceFragment
                    }
                }, when (type) {
                    DiskSpaceModel.SPACE_MANAGEMENT -> {
                        DiskSpaceFragmentArgs(deviceId!!).toBundle()
                    }
                    DiskSpaceModel.FORMAT -> {
                        DiskManageFragmentArgs(deviceId).toBundle()
                    }
                    else -> {
                        DiskSpaceFragmentArgs(deviceId).toBundle()
                    }
                }
            )
        }
        findNavController(R.id.placeholder_view).addOnDestinationChangedListener(onDestinationChangedListener)
    }

    override fun onBackPressed() {
        //只有正在格式化的情况下，不能切换Fragment Activity
        if (findNavController(R.id.placeholder_view).currentDestination?.label != DiskLoadFragment::class.java.simpleName) {
            super.onBackPressed()
        } else {
            finish()
        }
    }


    private val onDestinationChangedListener =
        NavController.OnDestinationChangedListener { controller, destination, arguments ->
            if (destination.label == DiskSpaceFragment::class.java.simpleName) {//首页
                itb_tv_title.text = getString(R.string.device_space)
            } else {
                itb_tv_title.text = getString(R.string.choice_disk_mode)
            }
        }

    override fun getTipsBar(): TipsBar? {
        return findViewById(R.id.tipsBar)
    }

    override fun getTopView(): View {
        return itb_rl
    }

}