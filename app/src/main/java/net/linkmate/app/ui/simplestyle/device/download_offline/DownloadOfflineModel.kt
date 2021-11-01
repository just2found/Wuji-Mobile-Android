package net.linkmate.app.ui.simplestyle.device.download_offline

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.weline.repo.data.model.BaseProtocol
import io.weline.repo.files.constant.AppConstants
import io.weline.repo.files.data.SharePathType
import io.weline.repo.net.V5Observer
import io.weline.repo.repository.V5Repository
import libs.source.common.AppExecutors
import libs.source.common.livedata.Resource
import libs.source.common.utils.SPUtilsN
import net.linkmate.app.ui.simplestyle.device.download_offline.data.AddTaskResult
import net.linkmate.app.ui.simplestyle.device.download_offline.data.OfflineDLTaskResult
import net.linkmate.app.ui.simplestyle.device.download_offline.data.OfflineDownLoadTask
import net.sdvn.common.internet.utils.LoginTokenUtil
import net.sdvn.nascommon.LibApp
import net.sdvn.nascommon.SessionManager
import net.sdvn.nascommon.constant.OneOSAPIs
import net.sdvn.nascommon.iface.GetSessionListener
import net.sdvn.nascommon.model.oneos.BaseResultModel
import net.sdvn.nascommon.model.oneos.OneOSFile
import net.sdvn.nascommon.model.oneos.OneOSFileType
import net.sdvn.nascommon.model.oneos.api.file.OneOSSearchAPI
import net.sdvn.nascommon.model.oneos.user.LoginSession
import net.sdvn.nascommon.model.oneos.vo.FileListModel
import net.sdvn.nascommon.repository.NasRepository
import org.json.JSONArray
import org.json.JSONObject
import org.view.libwidget.log.L
import java.util.*
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit


//离线下载的
class DownloadOfflineModel : ViewModel() {

    var mShowItem: OfflineDownLoadTask? = null
    private var nasRepository: NasRepository = NasRepository(SessionManager.getInstance().userId, LibApp.instance.getAppExecutors())

    var savePath = ""
    var saveSharePathType = -1//如果值为-1表示未设置过
    var mLoginSession: LoginSession? = null

    fun isSetSavePath(): Boolean {
        return saveSharePathType != -1
    }

    companion object {
        const val CMD_APPOINT_SUSPEND = 0         // 0 暂停指定id的下载任务
        const val CMD_APPOINT_START = 1            // 1 恢复指定id的下载任务
        const val CMD_SUSPEND = 2           // 2 暂停所有下载任务
        const val CMD_START = 3                // 3 恢复所有下载任务
        const val CMD_APPOINT_DELETE = 4                // 4 移除指定id下载任务
        const val CMD_DELETE = 5 //移除所有下载任务

        const val OFFLINE_DOWNLOAD_PATH = "offline_download_path" //移除所有下载任务
        const val OFFLINE_DOWNLOAD_TYPE = "offline_download_type" //移除所有下载任务

    }


    fun setSavePath(path: String, type: Int, devId: String) {
        SPUtilsN.put(OFFLINE_DOWNLOAD_PATH + devId, path)
        SPUtilsN.put(OFFLINE_DOWNLOAD_TYPE + devId, type)
        savePath = path
        saveSharePathType = type
    }

    fun initSavePath(devId: String) {
        savePath = SPUtilsN.get(OFFLINE_DOWNLOAD_PATH + devId, "")
        saveSharePathType = SPUtilsN.get(OFFLINE_DOWNLOAD_TYPE + devId, -1)
    }


    //进行任务的开始暂停删除等操作
    fun optTaskStatus(devcId: String, id: String, btsubfile: List<String>?, cmd: Int): LiveData<Resource<Int?>> {
        val liveData = MutableLiveData<Resource<Int?>>()
        SessionManager.getInstance().getLoginSession(devcId, object : GetSessionListener() {
            override fun onSuccess(url: String?, loginSession: LoginSession?) {
                if (loginSession == null) {
                    liveData.postValue(Resource.error("", null))
                    return
                }
                val observer = object : V5Observer<Any>(loginSession.id ?: "") {
                    override fun success(result: BaseProtocol<Any>) {
                        if (result.result) {
                            result.data?.let {
                                try {
                                    val gson = Gson()
                                    val dataStr = gson.toJson(result.data)
                                    val objectType = object : TypeToken<AddTaskResult>() {}.type
                                    val data = gson.fromJson<AddTaskResult>(dataStr, objectType)
                                    liveData.postValue(Resource.success(data.code))
                                } catch (e: Exception) {
                                    liveData.postValue(Resource.error("", null))
                                }
                            }
                        } else {
                            result.error?.let {
                                liveData.postValue(Resource.error("", it.code))
                            }
                        }
                    }

                    override fun fail(result: BaseProtocol<Any>) {
                        liveData.postValue(Resource.error("", null))
                    }

                    override fun isNotV5() {
                        liveData.postValue(Resource.error("", null))
                    }

                    override fun retry(): Boolean {
                        V5Repository.INSTANCE().optTaskStatus(loginSession.id
                                ?: "", loginSession.ip, LoginTokenUtil.getToken(),
                                id, btsubfile, cmd, this)
                        return true
                    }
                }
                V5Repository.INSTANCE().optTaskStatus(loginSession.id
                        ?: "", loginSession.ip, LoginTokenUtil.getToken(),
                        id, btsubfile, cmd, observer)
            }

            override fun onFailure(url: String?, errorNo: Int, errorMsg: String?) {
            }
        })
        return liveData
    }


    //查询当前正在进行的任务
    fun queryTaskList(devcId: String): LiveData<Resource<OfflineDLTaskResult>> {
        val liveData = MutableLiveData<Resource<OfflineDLTaskResult>>()
        SessionManager.getInstance().getLoginSession(devcId, object : GetSessionListener() {
            override fun onSuccess(url: String?, loginSession: LoginSession?) {
                if (loginSession == null) {
                    liveData.postValue(Resource.error("", null))
                    return
                }
                mLoginSession = loginSession
                val lastTime = System.currentTimeMillis()
                val observer = object : V5Observer<Any>(loginSession.id ?: "") {
                    override fun success(result: BaseProtocol<Any>) {
                        L.i((System.currentTimeMillis() - lastTime).toString(), "success", "DownloadOfflineModel", "nwq", "2021/3/23");
                        if (result.result) {
                            if (result.data == null) {
                                liveData.postValue(Resource.success(null))
                            } else {
                                try {
                                    val gson = Gson()
                                    val dataStr = gson.toJson(result.data)
                                    val objectType = object : TypeToken<OfflineDLTaskResult>() {}.type
                                    val data = gson.fromJson<OfflineDLTaskResult>(dataStr, objectType)
                                    liveData.postValue(Resource.success(data))
                                } catch (e: Exception) {
                                    liveData.postValue(Resource.error("", null))
                                }
                            }

                        } else {
                            result.error?.let {
                                liveData.postValue(Resource.error("", null))
                            }
                        }
                    }

                    override fun fail(result: BaseProtocol<Any>) {
                        liveData.postValue(Resource.error("", null, result.error?.code ?: 0))
                    }

                    override fun isNotV5() {
                        liveData.postValue(Resource.error("", null, 0))
                    }

                    override fun retry(): Boolean {
                        loginSession.session?.let {
                            V5Repository.INSTANCE().queryTaskList(
                                    loginSession.id
                                            ?: "", loginSession.ip, LoginTokenUtil.getToken(), this)
                        }
                        return true
                    }

                }
                V5Repository.INSTANCE().queryTaskList(loginSession.id
                        ?: "", loginSession.ip, LoginTokenUtil.getToken(), observer)
            }

        })
        return liveData
    }


    //通过资源文件添加任务
    fun addTask(devId: String, oneOSFile: OneOSFile, share_path_type: Int = oneOSFile.share_path_type): LiveData<Resource<Int?>> {
        val liveData = MutableLiveData<Resource<Int?>>()
        SessionManager.getInstance().getLoginSession(devId, object : GetSessionListener() {
            override fun onSuccess(url: String?, loginSession: LoginSession?) {
                if (loginSession == null) {
                    liveData.postValue(Resource.error("", null))
                    return
                }
                val observer = object : V5Observer<Any>(loginSession.id ?: "") {
                    override fun success(result: BaseProtocol<Any>) {
                        if (result.result) {
                            result.data?.let {
                                try {
                                    val gson = Gson()
                                    val dataStr = gson.toJson(result.data)
                                    val objectType = object : TypeToken<AddTaskResult>() {}.type
                                    val data = gson.fromJson<AddTaskResult>(dataStr, objectType)
                                    liveData.postValue(Resource.success(data.code))
                                } catch (e: Exception) {
                                    liveData.postValue(Resource.error("", null))
                                }
                            }
                        } else {
                            result.error?.let {
                                liveData.postValue(Resource.error("", it.code))
                            }
                        }
                    }

                    override fun fail(result: BaseProtocol<Any>) {
                        liveData.postValue(Resource.error("", null))
                    }

                    override fun isNotV5() {
                        liveData.postValue(Resource.error("", null))
                    }

                    override fun retry(): Boolean {
                        loginSession.session?.let {
                            V5Repository.INSTANCE().addDownloadOfflineTask(
                                    loginSession.id
                                            ?: "", loginSession.ip, LoginTokenUtil.getToken(),
                                    it,
                                    null, savePath, saveSharePathType, oneOSFile.getPath(), share_path_type, this)
                        }
                        return true
                    }
                }
                loginSession.session?.let {
                    V5Repository.INSTANCE().addDownloadOfflineTask(loginSession.id
                            ?: "", loginSession.ip, LoginTokenUtil.getToken(), it,
                            null, savePath, saveSharePathType, oneOSFile.getPath(), share_path_type, observer)
                }
            }


            override fun onFailure(url: String?, errorNo: Int, errorMsg: String?) {
            }
        })
        return liveData
    }

    //通过URL添加下载任务
    fun addTask1(devId: String, urlHttp: String): LiveData<Resource<Int?>> {
        val liveData = MutableLiveData<Resource<Int?>>()
        SessionManager.getInstance().getLoginSession(devId, object : GetSessionListener() {
            override fun onSuccess(url: String?, loginSession: LoginSession?) {
                if (loginSession == null) {
                    liveData.postValue(Resource.error("", null))
                    return
                }
                val observer = object : V5Observer<Any>(loginSession.id ?: "") {
                    override fun success(result: BaseProtocol<Any>) {
                        if (result.result) {
                            result.data?.let {
                                try {
                                    val gson = Gson()
                                    val dataStr = gson.toJson(result.data)
                                    val objectType = object : TypeToken<AddTaskResult>() {}.type
                                    val data = gson.fromJson<AddTaskResult>(dataStr, objectType)
                                    liveData.postValue(Resource.success(data.code))
                                } catch (e: Exception) {
                                    liveData.postValue(Resource.error("", null))
                                }
                            }
                        } else {
                            result.error?.let {
                                liveData.postValue(Resource.error("", it.code, it.code))
                            }
                        }
                    }

                    override fun fail(result: BaseProtocol<Any>) {
                        liveData.postValue(Resource.error("", null, result?.error?.code ?: 0))
                    }

                    override fun isNotV5() {
                        liveData.postValue(Resource.error("", null, 0))
                    }

                    override fun retry(): Boolean {
                        loginSession.session?.let {
                            V5Repository.INSTANCE().addDownloadOfflineTask(
                                    loginSession.id
                                            ?: "", loginSession.ip, LoginTokenUtil.getToken(),
                                    it,
                                    urlHttp, savePath, saveSharePathType, null, null, this)
                        }
                        return true
                    }
                }
                loginSession.session?.let {
                    V5Repository.INSTANCE().addDownloadOfflineTask(loginSession.id
                            ?: "", loginSession.ip, LoginTokenUtil.getToken(), it,
                            urlHttp, savePath, saveSharePathType, null, null, observer)
                }
            }


            override fun onFailure(url: String?, errorNo: Int, errorMsg: String?) {
            }
        })
        return liveData
    }

    fun findBtFile(devId: String, pattern: String = ".torrent", page: Int = 0, num: Int = 30): LiveData<Resource<ArrayList<OneOSFile>>> {
        val liveData = MutableLiveData<Resource<ArrayList<OneOSFile>>>()
        SessionManager.getInstance().getLoginSession(devId, object : GetSessionListener() {
            override fun onSuccess(url: String?, loginSession: LoginSession?) {
                if (loginSession == null) {
                    liveData.postValue(Resource.error("", null, 0))
                    return
                }
                val params = JSONObject()
                params.put("pattern", pattern)
                var ftype = JSONArray()
                ftype.put("bt")
                params.put("ftype", ftype)
                val jsonArray = JSONArray()
                jsonArray.put(SharePathType.USER.ordinal)
                jsonArray.put(SharePathType.PUBLIC.ordinal)
                params.put("share_path_type", jsonArray)
                params.put("page", page)
                params.put("num", num)
                params.put("order", "time_desc")
                val searchAPI = OneOSSearchAPI(loginSession)
                val observer = object : V5Observer<Any>(loginSession.id ?: "") {
                    override fun success(result: BaseProtocol<Any>) {
                        val list = searchAPI.getOneOSFiles(Gson().toJson(result.data))
                        liveData.postValue(Resource.success(list))
                    }

                    override fun fail(result: BaseProtocol<Any>) {
                        liveData.postValue(Resource.error("", null, result.error?.code ?: 0))
                    }

                    override fun isNotV5() {
                        liveData.postValue(Resource.error("", null, 0))
                    }

                }
                V5Repository.INSTANCE().searchFile(loginSession.id
                        ?: "", loginSession.ip, LoginTokenUtil.getToken(), params, observer)
            }

            override fun onFailure(url: String?, errorNo: Int, errorMsg: String?) {
                liveData.postValue(Resource.error(errorMsg ?: "", null, errorNo))
            }
        })
        return liveData
    }

    //路径 类型
    fun loadFilesFromServer(deviceId: String, type: OneOSFileType, path: String, filter: OneOSFileType = OneOSFileType.OFFLINE_DOWNLOAD, page: Int = 0): LiveData<Resource<BaseResultModel<FileListModel>>> {
        val liveData = MediatorLiveData<Resource<BaseResultModel<FileListModel>>>()
        SessionManager.getInstance().getLoginSession(deviceId, object : GetSessionListener() {
            override fun onSuccess(url: String?, loginSession: LoginSession) {
                liveData.addSource(nasRepository.loadFilesFromServer(devId = deviceId, session = loginSession.session, type = type, path = path, filter = filter, page = page)) {
                    liveData.postValue(it)
                }
            }

            override fun onFailure(url: String?, errorNo: Int, errorMsg: String?) {
                super.onFailure(url, errorNo, errorMsg)
                liveData.postValue(Resource.error(errorMsg, null, errorNo))
            }
        })
        return liveData
    }

    fun getLoginSession(devID: String?): LoginSession? {
        var loginSession = SessionManager.getInstance().getLoginSession(devID)
        if (loginSession != null && loginSession.isLogin) {
            return loginSession
        }
        val latch = CountDownLatch(1)
        AppExecutors.instance.mainThread().execute {
            SessionManager.getInstance().getLoginSession(devID!!,
                    object : GetSessionListener(false) {
                        override fun onSuccess(url: String, data: LoginSession) {
                            loginSession = data
                            latch.countDown()
                        }

                        override fun onFailure(url: String, errorNo: Int, errorMsg: String) {
                            latch.countDown()
                        }
                    })
        }
        try {
            latch.await(10, TimeUnit.SECONDS)
        } catch (e: InterruptedException) {
            loginSession = null
        }
        return loginSession
    }

    fun genThumbnailUrl(share_path_type: Int, oneOSPath: String): String? {
        mLoginSession?.let {
            return OneOSAPIs.genThumbnailUrlV5(share_path_type, it, oneOSPath)
        }
        return null
    }

}
