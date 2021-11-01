package net.linkmate.app.ui.nas.helper

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.NavHostFragment
import net.linkmate.app.R
import net.linkmate.app.ui.nas.cloud.VP2QuickCloudNavFragmentArgs
import net.sdvn.nascommon.BaseFragment
import net.sdvn.nascommon.constant.AppConstants
import net.sdvn.nascommon.model.oneos.OneOSFileType
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import timber.log.Timber
import java.util.*

/** 

Created by admin on 2020/8/20,11:24

 */
class FilePlaceHolderFragment : BaseFragment() {
    private lateinit var navHostFragment: NavHostFragment
    private var isComePoster = false
    private var isComeCircle = false
    private var path = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Timber.d("share>>  onCreate $devId")
        arguments?.let {
            isComePoster = it.getBoolean("isComePoster",false)
            isComeCircle = it.getBoolean("isComeCircle",false)
            path = it.getString("path","")
        }
    }

    override fun getLayoutResId(): Int {
        return R.layout.fragment_file_place_holder
    }

    override fun initView(view: View) {
        Timber.d("share>>  initView $devId")
        navHostFragment = NavHostFragment.create(R.navigation.files_nav,
                SelectTypeFragmentArgs(devId,isComePoster,isComeCircle,path).toBundle())
        childFragmentManager.beginTransaction()
                .replace(R.id.fragment_nav_host, navHostFragment)
                .setPrimaryNavigationFragment(navHostFragment)
                .commitAllowingStateLoss()
    }

    override fun onStart() {
        super.onStart()
        Timber.d("share>>  onStart $devId")
        EventBus.getDefault().register(this)
    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
    }

    override fun onResume() {
        super.onResume()
        switchToSelected()
    }

    private fun switchToSelected() {
        arguments?.let {
            devId = it.getString(AppConstants.SP_FIELD_DEVICE_ID)
            val path = it.getString(AppConstants.SP_FIELD_DEVICE_PATH)
            val type = it.getSerializable(AppConstants.SP_FIELD_FILE_TYPE)
            if (!path.isNullOrEmpty()) {
                Timber.d("share>> vp path : $path")
                val args = Bundle()
                args.putString(AppConstants.SP_FIELD_DEVICE_ID, devId)
                args.putString(AppConstants.SP_FIELD_DEVICE_PATH, path)
                args.putSerializable(AppConstants.SP_FIELD_FILE_TYPE, type)
                it.putString(AppConstants.SP_FIELD_DEVICE_PATH, "")
                it.putSerializable(AppConstants.SP_FIELD_FILE_TYPE, null)
                Timber.d("share>>vp apply by eventbus path : $path")
                EventBus.getDefault().postSticky(args)
            }
        }
        Timber.d("share>>  onStart $devId")
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    fun onEvent(args: Bundle) {
        args.let {
            val devId = it.getString(AppConstants.SP_FIELD_DEVICE_ID)
            if (Objects.equals(devId, this@FilePlaceHolderFragment.devId)) {
                val path = it.getString(AppConstants.SP_FIELD_DEVICE_PATH)
                val type = it.getSerializable(AppConstants.SP_FIELD_FILE_TYPE)
                if (type != null) {
                    Timber.d("share>>vp2 receive by eventbus path : $path")
//                    setFileType(type as OneOSFileType, path, DisplayMode.ALL)
                    navHostFragment.navController.navigate(R.id.global_to_VP2QuickCloudNavFragment,
                            VP2QuickCloudNavFragmentArgs(devId!!, type as OneOSFileType, path).toBundle())
                    EventBus.getDefault().removeStickyEvent(args)
                }
            }
            Timber.d("share>>  onEvent $devId")
        }
    }

    companion object {
        fun newInstance(devId: String): BaseFragment {
            val fragment = FilePlaceHolderFragment()
            val args = Bundle()
            args.putString(AppConstants.SP_FIELD_DEVICE_ID, devId)
            fragment.arguments = args
            return fragment
        }
    }

}