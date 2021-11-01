package net.linkmate.app.ui.nas.cloud

import android.Manifest
import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.AdapterView
import net.linkmate.app.ui.activity.WebViewActivity
import net.linkmate.app.ui.nas.helper.FilesCommonHelper
import net.linkmate.app.ui.nas.upload.UploadActivity
import net.sdvn.nascommon.SessionManager
import net.sdvn.nascommon.constant.AppConstants
import net.sdvn.nascommon.constant.OneOSAPIs
import net.sdvn.nascommon.iface.Callback
import net.sdvn.nascommon.iface.GetSessionListener
import net.sdvn.nascommon.model.UiUtils
import net.sdvn.nascommon.model.UiUtils.isHans
import net.sdvn.nascommon.model.UiUtils.isHant
import net.sdvn.nascommon.model.oneos.user.LoginSession
import net.sdvn.nascommon.model.phone.LocalFileType
import net.sdvn.nascommon.utils.PermissionChecker
import net.sdvn.nascommon.viewmodel.FilesViewModel
import net.sdvn.nascommon.widget.TypePopupView
import timber.log.Timber

/** 

Created by admin on 2020/8/22,13:27

 */
class UploadItemClickListener(val mDevId: String,
                              var mUploadPopView: TypePopupView,
                              val filesCommonViewModel: FilesCommonHelper,
                              val mFilesViewModel: FilesViewModel) :
        AdapterView.OnItemClickListener {
    override fun onItemClick(parent: AdapterView<*>, view: View, position: Int, id: Long) {
        if (filesCommonViewModel.checkNetworkStatus()) return
        val context: Context = view.context
        Timber.d("onItemClick: position=$position")
        var fileType = LocalFileType.PRIVATE
        when (position) {
            0 -> fileType = LocalFileType.PICTURE
            1 -> fileType = LocalFileType.VIDEO
            2 -> fileType = LocalFileType.AUDIO
            3 -> fileType = LocalFileType.DOC
            4 -> fileType = LocalFileType.PRIVATE
            5 -> {
                mFilesViewModel.getDeviceDisplayModel(devId = mDevId)?.mRefOSFileView
                        ?.get()?.showNewFolder()
                mUploadPopView.dismiss()
                return
            }
            6 -> {
                SessionManager.getInstance().getLoginSession(mDevId, object : GetSessionListener() {
                    override fun onSuccess(url: String?, loginSession: LoginSession) {
                        val url = OneOSAPIs.PREFIX_HTTP + loginSession.ip
                        val intent = Intent(context, WebViewActivity::class.java)
                        var region = when {
                            isHans() -> "zh-CN"
                            isHant() -> "zh-TW"
                            else -> "en"
                        }

                        intent.putExtra(WebViewActivity.WEB_VIEW_EXTRA_NAME_URL, "$url/transmission/web?lang=$region")
                        intent.putExtra(WebViewActivity.WEB_VIEW_EXTRA_NAME_TITLE, "Transmssion")
                        intent.putExtra(WebViewActivity.WEB_VIEW_EXTRA_NAME_HASTITLELAYOUT, false)

                        context.startActivity(intent)
                        mUploadPopView.dismiss()
                    }
                })
                return
            }
        }
        val curPath = mFilesViewModel.getPath(mDevId)
        val finalFileType = fileType

        PermissionChecker.checkPermission(view.context, Callback {
            val intent = Intent(context, UploadActivity::class.java)
            intent.putExtra("fileType", finalFileType)
            if (!curPath.isNullOrEmpty())
                intent.putExtra("path", curPath)
            if (!mDevId.isNullOrEmpty())
                intent.putExtra(AppConstants.SP_FIELD_DEVICE_ID, mDevId)
            context.startActivity(intent)
            mUploadPopView.dismiss()
        }, Callback {
            UiUtils.showStorageSettings(view.context)
        }, Manifest.permission.READ_EXTERNAL_STORAGE)

    }

}