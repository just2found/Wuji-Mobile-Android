package net.linkmate.app.ui.nas.files

import android.Manifest
import android.app.Dialog
import android.content.*
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.bumptech.glide.Glide
import net.linkmate.app.R
import net.linkmate.app.ui.nas.helper.FilePlaceHolderFragment
import net.sdvn.nascommon.BaseActivity
import net.sdvn.nascommon.constant.AppConstants
import net.sdvn.nascommon.db.SPHelper
import net.sdvn.nascommon.iface.Callback
import net.sdvn.nascommon.model.UiUtils
import net.sdvn.nascommon.utils.DialogUtils
import net.sdvn.nascommon.utils.PermissionChecker
import net.sdvn.nascommon.utils.log.Logger
import net.sdvn.nascommon.viewmodel.DeviceViewModel
import timber.log.Timber

class V2NasDetailsActivity2 : BaseActivity() {

    private var dialogNoDevice: Dialog? = null
    private var mCloudVPFragment: FilePlaceHolderFragment? = null
    private val deviceViewModel by viewModels<DeviceViewModel>()

    override fun getLayoutId(): Int {
        return R.layout.activity_nas_details
    }

    private val broadcastReceiverRemoveDevice = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == AppConstants.LOCAL_BROADCAST_REMOVE_DEV) {
                val devId2 = intent.getStringExtra(AppConstants.SP_FIELD_DEVICE_ID)
                if (devId2 == deviceId) {
                    lifecycleScope.launchWhenResumed {
                        finish()
                    }
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
//        setTheme(R.style.AppCompatThemeDark)
        //动态切换状态栏，全局顶上状态栏
        super.onCreate(savedInstanceState)
        val bundle = genBundle(intent)
        if (savedInstanceState == null) {
            mCloudVPFragment = FilePlaceHolderFragment()
            mCloudVPFragment!!.arguments = bundle
            supportFragmentManager
                    .beginTransaction()
                    .add(R.id.fragment_container, mCloudVPFragment!!)
                    .commitAllowingStateLoss()
        } else {
            mCloudVPFragment?.arguments = bundle
        }
        deviceViewModel.liveDevices.observe(this, Observer {
            if (mCloudVPFragment is FilePlaceHolderFragment) {
                it.find { deviceModel ->
                    deviceModel.devId == deviceId
                }.also {
                    if (it?.isOnline == true) {
                        showOrDismiss(false)
                    } else {
                        showOrDismiss(true)
                    }
                } ?: kotlin.run {
                    showOrDismiss(true)
                }
            }
        })
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiverRemoveDevice, IntentFilter().apply {
            this.addAction(AppConstants.LOCAL_BROADCAST_REMOVE_DEV)
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiverRemoveDevice)
    }

    private fun showOrDismiss(isShow: Boolean) {
        if (isShow) {
            if (dialogNoDevice == null) {
                dialogNoDevice = DialogUtils.showNotifyDialog(this, null,
                        getString(R.string.tip_wait_for_service_connect), getString(R.string.confirm))
                { _, _ ->
                    finish()
                }
            }
        } else {
            dialogNoDevice?.dismiss()
        }
    }

    private fun genBundle(intent: Intent): Bundle {
        val bundle = Bundle()
        deviceId = intent.getStringExtra(AppConstants.SP_FIELD_DEVICE_ID)
        val isComeCircle = intent.getBooleanExtra("isComeCircle",false)
        bundle.putBoolean("isComeCircle", isComeCircle)
        bundle.putBoolean("isComePoster", true)
        bundle.putString("path", intent.getStringExtra("path")?:"")
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
    }
}
