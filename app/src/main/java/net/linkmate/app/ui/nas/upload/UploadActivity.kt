package net.linkmate.app.ui.nas.upload

import android.os.Bundle
import androidx.navigation.fragment.NavHostFragment
import net.linkmate.app.R
import net.sdvn.nascommon.BaseActivity
import net.sdvn.nascommon.model.phone.LocalFileType


/**
 * Created by yun
 */

class UploadActivity : BaseActivity() {
    private lateinit var navHostFragment: NavHostFragment
    private var fileType: LocalFileType = LocalFileType.DOWNLOAD
    private var path: String? = null
//    private var uploadFragment: UploadNavFragment? = null

    override fun getLayoutId(): Int {
        return R.layout.activity_nas_upload
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initView()
//        initFragment()

    }

    override fun onResume() {
        super.onResume()
//        uploadFragment!!.changeFragmentByType(deviceId, fileType, path, pathType)
    }

    private fun initView() {
        val intent = intent
        if (intent != null) {
            fileType = intent.getSerializableExtra("fileType") as LocalFileType
            path = intent.getStringExtra("path")
//            pathType = intent.getIntExtra("pathType",-1)
        }
        navHostFragment = NavHostFragment.create(R.navigation.files_upload,
                UploadNavFragmentArgs(deviceId,fileType,path).toBundle())
        supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_content, navHostFragment)
                .setPrimaryNavigationFragment(navHostFragment)
                .commitAllowingStateLoss()
    }

//    private fun initFragment() {
//        uploadFragment = supportFragmentManager.findFragmentByTag("upload") as UploadNavFragment?
//        if (uploadFragment == null)
//            uploadFragment = UploadNavFragment()
//        val transaction = supportFragmentManager.beginTransaction()
//        transaction.replace(R.id.fragment_content, uploadFragment!!, "upload")
//        transaction.commitAllowingStateLoss()
//        uploadFragment!!.changeFragmentByType(deviceId, fileType, path,pathType)
//    }

//    override fun onBackPressed() {
//        if (uploadFragment != null && uploadFragment!!.onBackPressed()) {
//            return
//        }
//        super.onBackPressed()
//    }

    companion object {
        private val TAG = UploadActivity::class.java.simpleName
    }

}
