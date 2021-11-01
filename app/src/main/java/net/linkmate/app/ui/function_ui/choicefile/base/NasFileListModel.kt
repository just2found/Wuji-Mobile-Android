package net.linkmate.app.ui.function_ui.choicefile.base

import android.content.Context
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.weline.repo.files.constant.AppConstants
import io.weline.repo.files.data.SharePathType
import libs.source.common.livedata.Resource
import net.linkmate.app.R
import net.linkmate.app.ui.function_ui.choicefile.base.NasFileConstant.CONTAIN_EXT_STORAGE
import net.linkmate.app.ui.function_ui.choicefile.base.NasFileConstant.CONTAIN_PUBLIC
import net.linkmate.app.ui.function_ui.choicefile.base.NasFileConstant.CONTAIN_SAFE
import net.linkmate.app.ui.function_ui.choicefile.base.NasFileConstant.CONTAIN_USER
import net.sdvn.nascommon.LibApp
import net.sdvn.nascommon.SessionManager
import net.sdvn.nascommon.iface.GetSessionListener
import net.sdvn.nascommon.model.FileManageAction
import net.sdvn.nascommon.model.oneos.BaseResultModel
import net.sdvn.nascommon.model.oneos.OneOSFile
import net.sdvn.nascommon.model.oneos.OneOSFileManage
import net.sdvn.nascommon.model.oneos.user.LoginSession
import net.sdvn.nascommon.model.oneos.vo.FileListModel
import net.sdvn.nascommon.repository.NasRepository

/**
create by: 86136
create time: 2021/5/11 16:45
Function description:
 */

open class NasFileListModel : ViewModel() {

    private var nasRepository: NasRepository = NasRepository(SessionManager.getInstance().userId, LibApp.instance.getAppExecutors())
    val mSessionLiveData = MutableLiveData<LoginSession>()

    fun getRootList(context: Context, type: Int): MutableList<OneOSFile> {
        val list = mutableListOf<OneOSFile>()
        if (type and CONTAIN_USER == CONTAIN_USER) {
            val privateOneOSFile = OneOSFile()
            privateOneOSFile.type = "dir"
            privateOneOSFile.share_path_type = SharePathType.USER.type
            privateOneOSFile.setPath("/")
            privateOneOSFile.setTime(System.currentTimeMillis() / 1000)
            privateOneOSFile.setName(context.getString(R.string.root_dir_name_private))
            list.add(privateOneOSFile)
        }
        if (type and CONTAIN_PUBLIC == CONTAIN_PUBLIC) {
            val publicOneOSFile = OneOSFile()
            publicOneOSFile.type = "dir"
            publicOneOSFile.setPath("/")
            publicOneOSFile.setTime(System.currentTimeMillis() / 1000)
            publicOneOSFile.share_path_type = SharePathType.PUBLIC.type
            publicOneOSFile.setName(context.getString(R.string.root_dir_name_public))
            list.add(publicOneOSFile)
        }
        if (type and CONTAIN_SAFE == CONTAIN_SAFE) {
            val safeOneOSFile = OneOSFile()
            safeOneOSFile.type = "dir"
            safeOneOSFile.setPath("/")
            safeOneOSFile.setTime(System.currentTimeMillis() / 1000)
            safeOneOSFile.share_path_type = SharePathType.SAFE_BOX.type
            safeOneOSFile.setName(context.getString(R.string.root_dir_name_safe_box))
            list.add(safeOneOSFile)
        }
        if (type and CONTAIN_EXT_STORAGE == CONTAIN_EXT_STORAGE) {
            val safeOneOSFile = OneOSFile()
            safeOneOSFile.type = "dir"
            safeOneOSFile.setPath("/")
            safeOneOSFile.setTime(System.currentTimeMillis() / 1000)
            safeOneOSFile.share_path_type = SharePathType.EXTERNAL_STORAGE.type
            safeOneOSFile.setName(context.getString(R.string.external_storage))
            list.add(safeOneOSFile)
        }
        return list

    }

    //LoginSession方便使用类
    fun <T> getLoginSession(liveData: MutableLiveData<Resource<T>>, devID: String, next: (loginSession: LoginSession) -> Unit) {
        SessionManager.getInstance().getLoginSession(devID,
                object : GetSessionListener(false) {
                    override fun onSuccess(url: String, data: LoginSession) {
                        next(data)
                    }

                    override fun onFailure(url: String, errorNo: Int, errorMsg: String) {
                        liveData.postValue(Resource.error("", null, errorNo))
                    }
                })
    }


    //LoginSession方便使用类
    fun devId2LoginSession(devID: String, next: (loginSession: LoginSession?) -> Unit) {
        SessionManager.getInstance().getLoginSession(devID,
                object : GetSessionListener(false) {
                    override fun onSuccess(url: String, data: LoginSession) {
                        next(data)
                    }

                    override fun onFailure(url: String, errorNo: Int, errorMsg: String) {
                        next(null)
                    }
                })
    }

    //创建新的文件夹
    fun createFolder(activity: FragmentActivity, path: String, type: Int, callBack: OneOSFileManage.OnManageCallback) {
        if (mSessionLiveData.value != null) {
            callBack.onComplete(false)
        } else {
            val fileManage = OneOSFileManage(activity, null, mSessionLiveData.value!!, null, callBack)
            fileManage.manage(FileManageAction.MKDIR, path, type)
        }
    }


    //路径类型
    fun loadChoiceFilesFormServer(deviceId: String, sharePathType: Int, path: String,
                                  filter: OneOSFilterType = OneOSFilterType.ALL, page: Int = 0, num: Int = AppConstants.PAGE_SIZE,  groupId:Long?=null): LiveData<Resource<BaseResultModel<FileListModel>>> {
        val liveData = MediatorLiveData<Resource<BaseResultModel<FileListModel>>>()
        getLoginSession(liveData, deviceId) { loginSession ->
            mSessionLiveData.postValue(loginSession)
            liveData.addSource(nasRepository.loadChoiceFilesFormServer(devId = deviceId, session = loginSession.session!!,
                    sharePathType = sharePathType, path = path,groupId=groupId,
                    filterList = filter.getFilterList(), page = page)) {
                liveData.postValue(it)
            }
        }
        return liveData
    }

}