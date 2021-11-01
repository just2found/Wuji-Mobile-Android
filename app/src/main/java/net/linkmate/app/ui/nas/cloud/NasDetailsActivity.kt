package net.linkmate.app.ui.nas.cloud

import android.Manifest
import android.content.ComponentCallbacks2
import android.content.Intent
import android.os.Bundle
import android.view.View
import com.bumptech.glide.Glide
import net.linkmate.app.R
import net.sdvn.nascommon.BaseActivity
import net.sdvn.nascommon.constant.AppConstants
import net.sdvn.nascommon.db.SPHelper
import net.sdvn.nascommon.iface.Callback
import net.sdvn.nascommon.model.UiUtils
import net.sdvn.nascommon.utils.PermissionChecker
import net.sdvn.nascommon.utils.log.Logger
import timber.log.Timber

class NasDetailsActivity : BaseActivity() {

    private var mCloudVPFragment: CloudVPFragment? = null

    override fun getLayoutId(): Int {
        return R.layout.activity_nas_details
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val bundle = genBundle(intent)
        if (savedInstanceState == null) {
            mCloudVPFragment = CloudVPFragment()
            mCloudVPFragment!!.arguments = bundle
            supportFragmentManager
                    .beginTransaction()
                    .add(R.id.fragment_container, mCloudVPFragment!!)
                    .commitAllowingStateLoss()
        } else {
            mCloudVPFragment?.arguments = bundle
        }
    }

    private fun genBundle(intent: Intent): Bundle {
        val bundle = Bundle()
        deviceId = intent.getStringExtra(AppConstants.SP_FIELD_DEVICE_ID)
        if (deviceId != null) {
            SPHelper.put(AppConstants.SP_FIELD_DEVICE_ID, deviceId)
            Logger.LOGD(this, "share>> save devId ", deviceId)
            bundle.putString(AppConstants.SP_FIELD_DEVICE_ID, deviceId)
            val path = intent.getStringExtra(AppConstants.SP_FIELD_DEVICE_PATH)
            if (!path.isNullOrEmpty()) {
                bundle.putString(AppConstants.SP_FIELD_DEVICE_PATH, path)
                Logger.LOGD(this, "share>> save path ", path)
                intent.putExtra(AppConstants.SP_FIELD_DEVICE_PATH, "")
            }
            val type = intent.getSerializableExtra(AppConstants.SP_FIELD_FILE_TYPE)
            if (type != null) {
                bundle.putSerializable(AppConstants.SP_FIELD_FILE_TYPE, type)
                Logger.LOGD(this, "share>> save type ", type)
            }
        }
        return bundle
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        if (intent != null) {
            mCloudVPFragment?.arguments = genBundle(intent)
        }
    }

    override fun onStart() {
        super.onStart()
        val readExternalStorage = Manifest.permission.READ_EXTERNAL_STORAGE
        PermissionChecker.checkPermission(this,
                Callback { Timber.d("granted permission") },
                Callback { UiUtils.showStorageSettings(this) },
                readExternalStorage)

    }

    fun onBackPressed(view: View) {
        finish()
    }

    override fun onBackPressed() {
        if (mCloudVPFragment != null) {
            if (mCloudVPFragment!!.onBackPressed())
                return
        }
        super.onBackPressed()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        Glide.get(applicationContext).trimMemory(ComponentCallbacks2.TRIM_MEMORY_COMPLETE)
        Glide.get(applicationContext).trimMemory(ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN)
        System.gc()
    }
}
