package net.linkmate.app.ui.nas.safe_box.list


import android.app.Activity
import android.content.Intent
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import io.weline.repo.net.V5ObserverImpl
import io.weline.repo.repository.V5Repository
import libs.source.common.livedata.Resource
import net.linkmate.app.ui.function_ui.choicefile.base.OneOSFilterType
import net.linkmate.app.ui.nas.safe_box.control.SafeBoxControlActivity
import net.linkmate.app.ui.nas.safe_box.control.SafeBoxModel
import net.sdvn.common.internet.utils.LoginTokenUtil
import net.sdvn.nascommon.LibApp
import net.sdvn.nascommon.SessionManager
import net.sdvn.nascommon.fileserver.constants.SharePathType
import net.sdvn.nascommon.iface.GetSessionListener
import net.sdvn.nascommon.model.FileOrderTypeV2
import net.sdvn.nascommon.model.oneos.BaseResultModel
import net.sdvn.nascommon.model.oneos.DataFile
import net.sdvn.nascommon.model.oneos.tansfer_safebox.SafeBoxDownloadManager
import net.sdvn.nascommon.model.oneos.tansfer_safebox.SafeBoxUploadManager
import net.sdvn.nascommon.model.oneos.transfer.TransferManager
import net.sdvn.nascommon.model.oneos.user.LoginSession
import net.sdvn.nascommon.model.oneos.vo.FileListModel
import net.sdvn.nascommon.model.phone.LocalFileType
import net.sdvn.nascommon.repository.NasRepository


/**
 * 保险箱文件展示相关，
 */
class SafeBoxNasFileModel : ViewModel() {

    private var nasRepository: NasRepository = NasRepository(SessionManager.getInstance().userId, LibApp.instance.getAppExecutors())
    val mSessionLiveData = MutableLiveData<LoginSession>()


    private var downLoadCount = 0
    private var upLoadCount = 0
    var paths: ArrayList<DataFile>? = null
    var fileType: LocalFileType = LocalFileType.DOWNLOAD
    var targetFilePath: String? = null//移入的保存路径

    val transCountData by lazy {
        MutableLiveData<Int>()
    }
    private val mCountListener by lazy {
        TransferManager.OnTransferCountListener { isDownload, count ->
            if (isDownload) {
                downLoadCount = count
            } else {
                upLoadCount = count
            }
            transCountData.postValue(upLoadCount + downLoadCount)
        }
    }


    fun initCount() {
        SafeBoxDownloadManager.addOnTransferCountListener(mCountListener)
        SafeBoxUploadManager.addOnTransferCountListener(mCountListener)
    }


    //路径类型
    fun loadFilesFormServer(deviceId: String, path: String, page: Int = 0, sharePathType: Int = SharePathType.SAFE_BOX.type,
                            orderTypeV2: FileOrderTypeV2 = FileOrderTypeV2.time_desc): LiveData<Resource<BaseResultModel<FileListModel>>> {
        val liveData = MediatorLiveData<Resource<BaseResultModel<FileListModel>>>()
        getLoginSession(liveData, deviceId) { loginSession ->
            mSessionLiveData.postValue(loginSession)
            liveData.addSource(nasRepository.loadChoiceFilesFormServer(devId = deviceId, session = loginSession.session!!,
                    sharePathType = sharePathType, path = path, orderTypeV2 = orderTypeV2,
                    filterList = OneOSFilterType.ALL.getFilterList(), page = page)) {
                liveData.postValue(it)
            }
        }
        return liveData
    }


    fun moveInSafeBox(devID: String, toDir: String, sharePathType: Int, list: List<String>): LiveData<Resource<Any>> {
        val liveData = MutableLiveData<Resource<Any>>()
        getLoginSession(liveData, devID) { loginSession ->
            val v5ObserverImpl = V5ObserverImpl(devID, liveData)
            V5Repository.INSTANCE().moveFile(loginSession.id
                    ?: "", loginSession.ip, LoginTokenUtil.getToken(),
                    share_path_type = sharePathType,
                    toDir = toDir, des_path_type = SharePathType.SAFE_BOX.type, path = list
            ).subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(v5ObserverImpl)
        }
        return liveData
    }

    fun moveSafeBoxTo(devID: String, toDir: String, des_path_type: Int, list: List<String>): LiveData<Resource<Any>> {
        val liveData = MutableLiveData<Resource<Any>>()
        getLoginSession(liveData, devID) { loginSession ->
            val v5ObserverImpl = V5ObserverImpl(devID, liveData)
            V5Repository.INSTANCE().moveFile(loginSession.id
                    ?: "", loginSession.ip, LoginTokenUtil.getToken(),
                    share_path_type = SharePathType.SAFE_BOX.type,
                    toDir = toDir, des_path_type = des_path_type, path = list
            ).subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(v5ObserverImpl)
        }
        return liveData
    }


    fun copySafeBoxTo(devID: String, toDir: String, des_path_type: Int, list: List<String>): LiveData<Resource<Any>> {
        val liveData = MutableLiveData<Resource<Any>>()
        getLoginSession(liveData, devID) { loginSession ->
            val v5ObserverImpl = V5ObserverImpl(devID, liveData)
            V5Repository.INSTANCE().copyFile(loginSession.id
                    ?: "", loginSession.ip, LoginTokenUtil.getToken(),
                    share_path_type = SharePathType.SAFE_BOX.type,
                    toDir = toDir, des_path_type = des_path_type, path = list
            ).subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(v5ObserverImpl)
        }
        return liveData
    }


    private fun <T> getLoginSession(liveData: MutableLiveData<Resource<T>>, devID: String, next: (loginSession: LoginSession) -> Unit) {
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

    fun goToLogin(activity: Activity, devID: String) {
        activity.startActivity(Intent(activity, SafeBoxControlActivity::class.java)
                .putExtra(io.weline.repo.files.constant.AppConstants.SP_FIELD_DEVICE_ID, devID)
                .putExtra(SafeBoxModel.SAFE_BOX_TYPE_KEY, SafeBoxModel.LOGIN_TYPE)
        )
        activity.finish()
    }
}