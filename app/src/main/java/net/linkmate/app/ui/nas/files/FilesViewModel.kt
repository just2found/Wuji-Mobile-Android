package net.linkmate.app.ui.nas.files

import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import libs.source.common.AppExecutors
import libs.source.common.livedata.Resource
import libs.source.common.livedata.Status
import net.sdvn.nascommon.SessionManager
import net.sdvn.nascommon.iface.GetSessionListener
import net.sdvn.nascommon.model.oneos.BaseResultModel
import net.sdvn.nascommon.model.oneos.OneOSFileType
import net.sdvn.nascommon.model.oneos.user.LoginSession
import net.sdvn.nascommon.model.oneos.vo.FileListModel
import net.sdvn.nascommon.repository.NasRepository
import net.sdvn.nascommon.viewmodel.RxViewModel

/** 

Created by admin on 2020/10/29,15:07

 */
class FilesViewModel : RxViewModel() {
    private val mNasRepository: NasRepository = NasRepository(SessionManager.getInstance().userId, AppExecutors.instance)
    val liveDataSession = MutableLiveData<LoginSession>()
    val liveData = MediatorLiveData<Resource<BaseResultModel<FileListModel>>>()
    fun loadImages(devId: String, page: Int = 0) {
        SessionManager.getInstance().getLoginSession(devId, object : GetSessionListener() {
            override fun onSuccess(url: String?, loginSession: LoginSession) {
                liveDataSession.postValue(loginSession)
                val loadFilesFromServerDB =
                        mNasRepository.loadFilesFromServerDB(devId, loginSession.session,
                                OneOSFileType.PICTURE, null, page, loginSession.isV5)
                liveData.addSource(loadFilesFromServerDB) { t ->
                    liveData.postValue(t)
                }
            }

            override fun onFailure(url: String?, errorNo: Int, errorMsg: String?) {
                super.onFailure(url, errorNo, errorMsg)
                liveData.postValue(Resource.error("获取session错误", null, errorNo))

            }
        })
    }

    fun loadMoreImages(devId: String) {
        val nextPage = liveData.value?.data?.data?.nextPage() ?: 0
        SessionManager.getInstance().getLoginSession(devId, object : GetSessionListener() {
            override fun onSuccess(url: String?, loginSession: LoginSession) {
                liveDataSession.postValue(loginSession)
                val loadFilesFromServerDB =
                        mNasRepository.loadFilesFromServerDB(devId, loginSession.session,
                                OneOSFileType.PICTURE, null, nextPage, loginSession.isV5)
                liveData.addSource(loadFilesFromServerDB) { t ->
                    if (t.status == Status.SUCCESS) {
                        val filesOld = liveData.value?.data?.data?.files
                        val filesNew = t?.data?.data?.files
                        if (filesOld != null && filesNew != null) {
                            filesOld.addAll(filesNew)
                            t.data?.data?.files = filesOld
                        }
                        liveData.postValue(t)
                    }
                }
            }

            override fun onFailure(url: String?, errorNo: Int, errorMsg: String?) {
                super.onFailure(url, errorNo, errorMsg)
                liveData.postValue(Resource.error("获取session错误", null, errorNo))
            }
        })
    }
}