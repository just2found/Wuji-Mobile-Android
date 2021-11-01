package net.linkmate.app.ui.nas.files

import android.content.ComponentCallbacks2
import android.content.Intent
import android.os.Bundle
import android.view.View
import com.bumptech.glide.Glide
import net.linkmate.app.R
import net.sdvn.nascommon.BaseActivity
import net.sdvn.nascommon.constant.AppConstants
import net.sdvn.nascommon.utils.log.Logger

class PictureSelectActivity : BaseActivity() {

    private var fragment: PictureSelectFragment? = null

    override fun getLayoutId(): Int {
        return R.layout.activity_nas_details
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val bundle = genBundle(intent)
        if (savedInstanceState == null) {
            fragment = PictureSelectFragment()
            fragment!!.arguments = bundle
            supportFragmentManager
                    .beginTransaction()
                    .add(R.id.fragment_container, fragment!!)
                    .commitAllowingStateLoss()
        } else {
            fragment?.arguments = bundle
        }
    }

    private fun genBundle(intent: Intent): Bundle {
        val bundle = Bundle()
        deviceId = intent.getStringExtra(AppConstants.SP_FIELD_DEVICE_ID)
        if (deviceId != null) {
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
            val intExtra = intent.getIntExtra("count", Int.MAX_VALUE)
            bundle.putInt("count", intExtra)
        }
        return bundle
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        if (intent != null) {
            fragment?.arguments = genBundle(intent)
        }
    }

    fun onBackPressed(view: View) {
        finish()
    }

    override fun onBackPressed() {
        if (fragment != null) {
            if (fragment!!.onBackPressed())
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
