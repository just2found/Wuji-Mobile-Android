package net.linkmate.app.ui.nas.helper

import android.content.Intent
import android.os.Bundle
import com.alibaba.android.arouter.facade.annotation.Route
import net.linkmate.app.R
import net.sdvn.nascommon.BaseActivity
import net.sdvn.nascommon.model.oneos.OneOSFile

/**
 * Created by yun on 18/05/03.
 */
@Route(path = "/nas/file_view", group = "nas")
class FileViewActivity : BaseActivity() {
    override fun getLayoutId(): Int {
        return R.layout.activity_fragment_container
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handlerIntent(intent)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        handlerIntent(intent)
    }

    private fun handlerIntent(intent: Intent?) {
        val file = intent?.getSerializableExtra(key_file) as? OneOSFile
        val groupId = intent?.getLongExtra(key_groupId, -1)
        if (file != null && !deviceId.isNullOrEmpty()) {
            supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.fragment_container, FileViewFragment.newInstance(deviceId, file,groupId?:-1))
                    .commitAllowingStateLoss()
        } else {
            finish()
        }
    }

    companion object {
        const val key_file = "file"
        const val key_groupId = "groupId"
    }
}


