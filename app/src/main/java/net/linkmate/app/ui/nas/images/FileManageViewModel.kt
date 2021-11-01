package net.linkmate.app.ui.nas.images

import android.net.Uri
import android.view.View
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.alibaba.android.arouter.launcher.ARouter
//import io.weline.mediaplayer.IntentUtil
//import io.weline.mediaplayer.PlayerActivity
import libs.source.common.livedata.Resource
import libs.source.common.utils.ToastHelper
import net.linkmate.app.R
import net.linkmate.app.manager.SDVNManager
import net.sdvn.nascommon.SessionManager
import net.sdvn.nascommon.constant.AppConstants
import net.sdvn.nascommon.constant.OneOSAPIs
import net.sdvn.nascommon.iface.GetSessionListener
import net.sdvn.nascommon.model.FileManageAction
import net.sdvn.nascommon.model.PathTypeCompat
import net.sdvn.nascommon.model.oneos.*
import net.sdvn.nascommon.model.oneos.user.LoginSession
import net.sdvn.nascommon.utils.DialogUtils
import net.sdvn.nascommon.utils.FileUtils
import java.io.File

/**
 *
 * @Description: FileManage
 * @Author: todo2088
 * @CreateDate: 2021/3/4 17:35
 */
class FileManageViewModel : ViewModel() {

    private val _liveDataAction: MutableLiveData<Resource<FileManageAction>> = MutableLiveData()
    val liveDataAction: LiveData<Resource<FileManageAction>> = _liveDataAction

    fun manage(devId: String, mActivity: FragmentActivity, owner: LifecycleOwner,
               type: OneOSFileType,
               rootView: View,
               selectedList: List<OneOSFile>, action: FileManageAction) {

        if (selectedList.isNullOrEmpty()) {
            ToastHelper.showToast(R.string.tip_select_file)
            return
        }
        checkStatusAndGetSession(devId, object : GetSessionListener() {
            override fun onSuccess(url: String?, loginSession: LoginSession) {
                val fileManage = OneOSFileManage(mActivity, null, loginSession, rootView,
                        OneOSFileManage.OnManageCallback { isSuccess ->
                            if (isSuccess) {
                                _liveDataAction.postValue(Resource.success(action))
                            } else {
                                _liveDataAction.postValue(Resource.error(null, action))
                            }
                        })
                fileManage.manage(type, action, selectedList)
            }

            override fun onFailure(url: String?, errorNo: Int, errorMsg: String?) {
                super.onFailure(url, errorNo, errorMsg)
                _liveDataAction.postValue(Resource.error(null, action))
            }
        })
        _liveDataAction.postValue(Resource.loading(action))
    }

    fun showNewFolder(fragmentActivity: FragmentActivity, devId: String, path: String, fileType: OneOSFileType, rootView: View) {
        checkStatusAndGetSession(devId, object : GetSessionListener() {
            override fun onSuccess(url: String?, loginSession: LoginSession) {
                val fileManage = OneOSFileManage(fragmentActivity, null, loginSession, rootView,
                        OneOSFileManage.OnManageCallback { isSuccess ->
                            if (isSuccess) {

                            }
                        })


                val share_path_type = PathTypeCompat.getSharePathType(fileType)
                fileManage.manage(FileManageAction.MKDIR, path, share_path_type.type)
            }
        })
    }

    fun checkStatusAndGetSession(devId: String, listener: GetSessionListener) {

        if (!SDVNManager.instance.isConnected()) {
            ToastHelper.showToast(R.string.network_not_available)
            return
        }
        SessionManager.getInstance().getLoginSession(devId, listener)
    }

    fun openFile(devId: String, fragmentActivity: FragmentActivity, view: View, dataFile: DataFile,groupId:Long?=null) {
        if (!SDVNManager.instance.isConnected()) {
            ToastHelper.showToast(R.string.network_not_available)
            return
        }
        val file: OneOSFile = dataFile as OneOSFile
        if (file.isEncrypt()) {
            DialogUtils.showNotifyDialog(fragmentActivity, R.string.tips, R.string.error_open_encrypt_file, R.string.ok, null)
            return
        }
        SessionManager.getInstance().getLoginSession(devId, object : GetSessionListener() {
            override fun onSuccess(tag: String?, loginSession: LoginSession) {
                if (file.isPicture) {
                    openOSPicture(devId, fragmentActivity, file,groupId)
                } else if (file.isVideo || file.isAudio) {
                    val url = OneOSAPIs.genOpenUrl(loginSession, file,groupId)
                    var uri = Uri.parse(url)
                    if (file.hasLocalFile()) uri = FileUtils.getFileProviderUri(file.localFile!!)
                    openMedia(fragmentActivity, url, file, uri)
                } else if (file.isExtract &&(groupId==null || groupId<=0)) {//TODO 需要群组空间也能
                    OneOSFileManage(fragmentActivity, null, loginSession, view, OneOSFileManage.OnManageCallback {

                    }).doOnlineExtract(file)
                } else {
                    val toPath = SessionManager.getInstance().getDefaultDownloadPathByID(devId, file)
                    val localPath = toPath + File.separator + file.getName()
                    val localFile = File(localPath)
                    if (localFile.exists() && localFile.isFile && localFile.length() == file.getSize()) {
                        val uri = Uri.parse(localPath)
                        FileUtils.openFileByOtherApp(fragmentActivity, uri, file.getName()) { e: Throwable?, e2: Any? -> ToastHelper.showLongToastSafe(R.string.operate_failed) }
                    } else {
                        openOtherFile(devId, fragmentActivity, file,groupId)
                    }
                }
            }
        })
    }

    private fun openOtherFile(devId: String, fragmentActivity: FragmentActivity, file: OneOSFile,groupId:Long?=null) {
        if(groupId!=null&& groupId>0)
        {
            ARouter.getInstance().build("/nas/file_view", "nas")
                .withSerializable("file", file)
                .withLong("groupId", groupId)
                .withString(AppConstants.SP_FIELD_DEVICE_ID, devId)
                .navigation(fragmentActivity)
        }else{
            ARouter.getInstance().build("/nas/file_view", "nas")
                .withSerializable("file", file)
                .withString(AppConstants.SP_FIELD_DEVICE_ID, devId)
                .navigation(fragmentActivity)
        }

    }

    private fun openMedia(fragmentActivity: FragmentActivity, url: String?, file: OneOSFile, uri: Uri?) {
//        if (BuildConfig.DEBUG) {
//            val intent = Intent(fragmentActivity, PlayerActivity::class.java)
//            val mediaItems: MutableList<MediaItem> = ArrayList()
//            val mediaItem = MediaItem.Builder()
//                    .setUri(url)
//                    .setMimeType(MIMETypeUtils.getMIMEType(file.getName()))
//                    .build()
//            mediaItems.add(mediaItem)
//            IntentUtil.addToIntent(mediaItems, intent)
//            fragmentActivity.startActivity(intent)
//        } else {
        FileUtils.openFileByOtherApp(fragmentActivity, uri, file.getName()) { e: Throwable?, e2: Any? -> ToastHelper.showLongToastSafe(R.string.operate_failed) }
//        }
    }

    private fun openOSPicture(devId: String, fragmentActivity: FragmentActivity, file: OneOSFile,groupId:Long?=null) {

        FileInfoHolder.getInstance().save(FileInfoHolder.PIC, listOf(file))
        if(groupId!=null&& groupId>0)
        {
            ARouter.getInstance().build("/nas/pic_view", "nas")
                .withSerializable("FileType", OneOSFileType.PICTURE)
                .withString("PictureList", FileInfoHolder.PIC)
                .withInt("StartIndex", 0)
                .withLong("groupId", groupId)
                .withString(AppConstants.SP_FIELD_DEVICE_ID, devId)
                .navigation(fragmentActivity)
        }else{
            ARouter.getInstance().build("/nas/pic_view", "nas")
                .withSerializable("FileType", OneOSFileType.PICTURE)
                .withString("PictureList", FileInfoHolder.PIC)
                .withInt("StartIndex", 0)
                .withString(AppConstants.SP_FIELD_DEVICE_ID, devId)
                .navigation(fragmentActivity)
        }

    }

}