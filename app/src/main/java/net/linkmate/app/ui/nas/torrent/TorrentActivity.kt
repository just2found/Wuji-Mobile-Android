package net.linkmate.app.ui.nas.torrent

import android.content.Context
import android.content.Intent
import android.os.Bundle
import net.linkmate.app.R
import net.sdvn.nascommon.BaseActivity
import net.sdvn.nascommon.constant.AppConstants

class TorrentActivity : BaseActivity() {
    override fun getLayoutId(): Int {
        return R.layout.activity_torrent
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) {
            val isOnlyDownload = intent.getBooleanExtra(ARG_IS_ONLY_DOWNLOAD, false)
            val btName = intent.getStringExtra("btName")
            supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_content, TorrentsFragment.newInstance(deviceId,btName, isOnlyDownload = isOnlyDownload))
                    .commitAllowingStateLoss()
        }
    }

    companion object {
        const val ARG_IS_ONLY_DOWNLOAD = "arg_is_only_download"

        @JvmStatic
        fun startActivityWithId(context: Context, devId: String? = null,name: String?, isOnlyDownload: Boolean = false) {
            val intent = Intent(context, TorrentActivity::class.java)
            if (devId != null) {
                intent.putExtra(AppConstants.SP_FIELD_DEVICE_ID, devId)
            }
            if (name != null) {
                intent.putExtra("btName", name)
            }
            intent.putExtra(ARG_IS_ONLY_DOWNLOAD, isOnlyDownload)
            context.startActivity(intent)
        }
    }
}