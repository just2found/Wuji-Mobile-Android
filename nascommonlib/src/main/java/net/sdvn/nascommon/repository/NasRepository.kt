package net.sdvn.nascommon.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import com.google.gson.reflect.TypeToken
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.weline.repo.SessionCache
import io.weline.repo.api.V5_ERR_SESSION_EXP
import io.weline.repo.files.data.DataPhotosTimelineYearSummary
import libs.source.common.AppExecutors
import libs.source.common.livedata.ApiHTTPErrNO.STATUS_CODE_THROWABLE
import libs.source.common.livedata.ApiResponse
import libs.source.common.livedata.ApiSuccessResponse
import libs.source.common.livedata.LiveDataCallAdapterFactory
import libs.source.common.livedata.Resource
import libs.source.common.utils.RateLimiter
import net.sdvn.common.internet.OkHttpClientIns
import net.sdvn.common.internet.utils.LoginTokenUtil
import net.sdvn.nascommon.SessionManager
import net.sdvn.nascommon.constant.AppConstants
import net.sdvn.nascommon.constant.AppConstants.HS_ANDROID_TV_PORT
import net.sdvn.nascommon.constant.HttpErrorNo
import net.sdvn.nascommon.constant.OneOSAPIs
import net.sdvn.nascommon.db.objecbox.DeviceInfo
import net.sdvn.nascommon.fileserver.constants.SharePathType
import net.sdvn.nascommon.iface.Callback
import net.sdvn.nascommon.iface.Result
import net.sdvn.nascommon.model.FileManageAction
import net.sdvn.nascommon.model.FileOrderTypeV2
import net.sdvn.nascommon.model.oneos.ActionResultModel
import net.sdvn.nascommon.model.oneos.BaseResultModel
import net.sdvn.nascommon.model.oneos.OneOSFile
import net.sdvn.nascommon.model.oneos.OneOSFileType
import net.sdvn.nascommon.model.oneos.user.LoginSession
import net.sdvn.nascommon.model.oneos.vo.FileListModel
import net.sdvn.nascommon.repository.base.BodyBuilder
import net.sdvn.nascommon.repository.base.NasService
import net.sdvn.nascommon.repository.base.NetworkBoundResource
import net.sdvn.nascommon.utils.FileUtils
import net.sdvn.nascommon.utils.GsonUtils
import okhttp3.HttpUrl
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import timber.log.Timber
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList
import kotlin.collections.HashMap


class NasRepository constructor(private val userId: String, private val appExecutors: AppExecutors) {

    private val repoListRateLimit = RateLimiter<String>(1, TimeUnit.SECONDS)

    companion object {
        fun provideNasService(devId: String, isV5: Boolean = false, isUseLiveData: Boolean = true): NasService {
            val ip = SessionManager.getInstance().getDeviceVipById(devId)
            val builder = if (isV5)
                HttpUrl.Builder()
                        .scheme("http")
                        .host(ip)
                        .port(HS_ANDROID_TV_PORT)
            else HttpUrl.Builder()
                    .scheme("http")
                    .host(ip)


            val retrofitBuilder = Retrofit.Builder()
                    .baseUrl(builder.build())
                    .addConverterFactory(GsonConverterFactory.create())
            if (isUseLiveData)
                retrofitBuilder.addCallAdapterFactory(LiveDataCallAdapterFactory.create())
            return retrofitBuilder
                    .client(OkHttpClientIns.getApiClient())
                    .build()
                    .create(NasService::class.java)

        }

        fun map(method: String, session: String?, params: Map<String, Any>?): Map<String, Any> {
            val bodyBuilder = BodyBuilder(method)
            if (!session.isNullOrEmpty()) {
                bodyBuilder.addSession(session)
            }
            if (!params.isNullOrEmpty()) {
                bodyBuilder.addParams(params)
            }
            return bodyBuilder.build()
        }

        fun accessAndroidTv(deviceId: String, devId: String): Observable<Resource<Result<*>>> {

            return Observable.create<Resource<Result<*>>> { emitter ->
                val map = NasRepository.map("access", null,
                        mapOf<String, Any>("token" to LoginTokenUtil.getToken()))
                Timber.d("access params: ${(map)}")
                provideNasService(devId, true).access(map)
                        .observeForever {
                            Timber.d("access response : $it")
                            when (it) {
                                is ApiSuccessResponse -> {
                                    if (it.body.isSuccess) {
                                        val loginSession = LoginSession(deviceId)
                                        loginSession.session = it.body.data.session
                                        loginSession.userInfo = it.body.data.user
                                        loginSession.deviceInfo = DeviceInfo(deviceId)
                                        emitter.onNext(Resource.success(Result(it)))
                                    } else {
                                        val error = it.body.error
                                        emitter.onNext(Resource.error(error?.msg
                                                ?: "unknow", Result<Any>(null), error?.code
                                                ?: STATUS_CODE_THROWABLE))
                                    }
                                }
                                else -> {
                                    emitter.onNext(Resource.error("unknow", Result<Any>(null)))
                                }
                            }
                        }

            }.subscribeOn(AndroidSchedulers.mainThread())

        }

        fun getFilterTypeByFileType(filter: OneOSFileType): List<OneOSFileType> {
            val arrayList = ArrayList<OneOSFileType>()
            return when (filter) {
                OneOSFileType.PRIVATE -> {
                    arrayList.add(OneOSFileType.ALL)
                    arrayList
                }
                OneOSFileType.PUBLIC -> {
                    arrayList.add(OneOSFileType.ALL)
                    arrayList
                }
                OneOSFileType.SAFE -> {
                    arrayList.add(OneOSFileType.ALL)
                    arrayList
                }
                OneOSFileType.GROUP -> {
                    arrayList.add(OneOSFileType.ALL)
                    arrayList
                }
                OneOSFileType.RECYCLE -> {
                    arrayList.add(OneOSFileType.ALL)
                    arrayList
                }
                OneOSFileType.EXTERNAL_STORAGE -> {
                    arrayList.add(OneOSFileType.ALL)
                    arrayList
                }
                OneOSFileType.FAVORITES -> {
                    arrayList.add(OneOSFileType.ALL)
                    arrayList
                }
                OneOSFileType.DOCUMENTS -> {
                    arrayList.add(OneOSFileType.TXT)
                    arrayList.add(OneOSFileType.DOC)
                    arrayList.add(OneOSFileType.XLS)
                    arrayList.add(OneOSFileType.PPT)
                    arrayList.add(OneOSFileType.PDF)
                    arrayList
                }
                OneOSFileType.OFFLINE_DOWNLOAD -> {
                    arrayList.add(OneOSFileType.DIR)
                    arrayList.add(OneOSFileType.TORRENT)
                    arrayList
                }
                else -> {
                    arrayList.add(filter)
                    arrayList
                }
            }
        }

        fun getFilterByFileType(filter: OneOSFileType): List<String> {
            val arrayList = ArrayList<String>()
            return when (filter) {
                OneOSFileType.PRIVATE -> {
                    arrayList.add(OneOSFileType.ALL.serverTypeName)
                    arrayList
                }
                OneOSFileType.PUBLIC -> {
                    arrayList.add(OneOSFileType.ALL.serverTypeName)
                    arrayList
                }
                OneOSFileType.SAFE -> {
                    arrayList.add(OneOSFileType.ALL.serverTypeName)
                    arrayList
                }
                OneOSFileType.RECYCLE -> {
                    arrayList.add(OneOSFileType.ALL.serverTypeName)
                    arrayList
                }
                OneOSFileType.EXTERNAL_STORAGE -> {
                    arrayList.add(OneOSFileType.ALL.serverTypeName)
                    arrayList
                }
                OneOSFileType.FAVORITES -> {
                    arrayList.add(OneOSFileType.ALL.serverTypeName)
                    arrayList
                }
                OneOSFileType.DOCUMENTS -> {
                    arrayList.add("txt")
                    arrayList.add("doc")
                    arrayList.add("xls")
                    arrayList.add("ppt")
                    arrayList.add("pdf")
                    arrayList
                }
                OneOSFileType.OFFLINE_DOWNLOAD -> {
                    arrayList.add("dir")
                    arrayList.add("bt")
                    arrayList
                }
                else -> {
                    arrayList.add(filter.serverTypeName)
                    arrayList
                }
            }
        }


    }


    //新的文件选择功能，所以这里默认就是V5的
    fun loadChoiceFilesFormServer(devId: String, session: String, sharePathType: Int,
                                  path: String, page: Int = 0,
                                  isShowHidden: Boolean = false, filterList: List<String>,
                                  orderTypeV2: FileOrderTypeV2 = FileOrderTypeV2.time_desc,
                                  groupId:Long? =-1)
            : LiveData<Resource<BaseResultModel<FileListModel>>> {
        val params: MutableMap<String, Any> = HashMap()
        val showHidden = if (isShowHidden) {
            1
        } else {
            0
        }
        params["path"] = path
        params["share_path_type"] = sharePathType
        params["show_hidden"] = showHidden
        params["page"] = page
        params["num"] = AppConstants.PAGE_SIZE
        params["order"] = orderTypeV2.name
        params["ftype"] = filterList
        if(groupId!=null && groupId>0)
        {
            params["groupid"] = groupId
        }
        return loadFiles(devId, map("list", session, params), true)
    }



    fun loadFilesFromServer(devId: String, session: String?, type: OneOSFileType,
                            path: String?, page: Int = 0,
                            showHidden: Boolean = false, filter: OneOSFileType,
                            isV5: Boolean = false, orderTypeV2: FileOrderTypeV2 = FileOrderTypeV2.time_desc,
                            pathType: IntArray? = null, groupId:Long? =-1)
            : LiveData<Resource<BaseResultModel<FileListModel>>> {
        //正确的V5判断
        val isV5True = SessionCache.instance.isV5(devId) ?: false
        val isNasV3 = SessionCache.instance.isNasV3(devId) ?: false

        val params: MutableMap<String, Any> = HashMap()
        if (!path.isNullOrEmpty()) {
            params["path"] = if ((isV5True || isV5) && !path.startsWith(OneOSAPIs.ONE_OS_PRIVATE_ROOT_DIR)) {
                path.substring(path.indexOfFirst { it.toString() == OneOSAPIs.ONE_OS_PRIVATE_ROOT_DIR })
            } else path
        } else {
            if (isV5True || isV5) {
                params["path"] = "/"
            }
        }
        if (type != OneOSFileType.PICTURE) {
            params["sort"] = "1"
            params["order"] = orderTypeV2.name
        } else {
            params["sort"] = "5"
            params["order"] = "cttime_desc"
        }

        if(groupId!=null && groupId>0)
        {
            params["groupid"] = groupId
        }
        if (isV5True) {
            params["share_path_type"] =
                    if (pathType != null)
                        pathType
                    else
                        when (type) {
                            OneOSFileType.PUBLIC -> {
                                SharePathType.PUBLIC.type
                            }
                            OneOSFileType.PRIVATE -> {
                                SharePathType.USER.type
                            }
                            OneOSFileType.RECYCLE -> {
                                SharePathType.USER.type
                            }
                            OneOSFileType.SAFE -> {
                                SharePathType.SAFE_BOX.type
                            }
                            OneOSFileType.EXTERNAL_STORAGE -> {
                                SharePathType.EXTERNAL_STORAGE.type
                            }
                            else -> {
                                if (isNasV3) {
                                    listOf<Int>(SharePathType.USER.type,
                                            SharePathType.PUBLIC.type,
                                        SharePathType.GROUP.type)
                                } else {
                                    //分类
                                    SharePathType.USER.type
                                }
                            }
                        }

            params["ftype"] = getFilterByFileType(filter)
        } else {
            params["ftype"] = when (filter) {
                OneOSFileType.PRIVATE -> OneOSFileType.ALL.serverTypeName
                OneOSFileType.PUBLIC -> OneOSFileType.ALL.serverTypeName
                OneOSFileType.RECYCLE -> OneOSFileType.ALL.serverTypeName
                OneOSFileType.SAFE -> OneOSFileType.ALL.serverTypeName
                OneOSFileType.EXTERNAL_STORAGE -> OneOSFileType.ALL.serverTypeName
                OneOSFileType.DOCUMENTS -> {
                    val docs = ArrayList<String>()
                    docs.add("txt")
                    docs.add("doc")
                    docs.add("xls")
                    docs.add("ppt")
                    docs.add("pdf")

                    docs
                }

                else -> filter.serverTypeName
            }
        }


        params["show_hidden"] = if (showHidden) {
            1
        } else {
            0
        }
        if (page >= 0) {
            params["page"] = page
            params["num"] = AppConstants.PAGE_SIZE
        }
        if (isV5True) {
            val action = when (type) {
                OneOSFileType.PRIVATE -> "list"
                OneOSFileType.PUBLIC -> "list"
                OneOSFileType.RECYCLE -> "list"
                OneOSFileType.EXTERNAL_STORAGE -> "list"
                OneOSFileType.SAFE -> "list"
                else -> {
                    "listdb"
                }
            }
            return loadFiles(devId, map(action, session, params), true)

        } else {
            if (isV5) {
                params["share_path_type"] = SharePathType.PUBLIC.type
                params["ftype"] = OneOSFileType.ALL.serverTypeName
            }
            Timber.d("loadfiles:${params}")
            return loadFiles(devId, map(
                    when (type) {
                        OneOSFileType.PRIVATE -> "list"
                        OneOSFileType.PUBLIC -> "list"
                        OneOSFileType.RECYCLE -> "list"
                        OneOSFileType.EXTERNAL_STORAGE -> "list"
                        else -> {
                            "listdb"
                        }
                    }, session, params), isV5)
        }
    }


    fun getSharePathType(type: OneOSFileType): SharePathType {
        return when (type) {
            OneOSFileType.PUBLIC -> SharePathType.PUBLIC
            OneOSFileType.PRIVATE -> SharePathType.USER
            OneOSFileType.RECYCLE -> SharePathType.USER
            else -> {
                SharePathType.VIRTUAL
            }
        }
    }


    fun loadFilesFromServerDB(devId: String, session: String?, type: OneOSFileType,
                              path: String?, page: Int, isV5: Boolean = false,
                              orderTypeV2: FileOrderTypeV2 = FileOrderTypeV2.time_desc)
            : LiveData<Resource<BaseResultModel<FileListModel>>> {
        return loadFilesFromServer(devId, session, type, path, page, false, type, isV5, orderTypeV2)

    }


    fun loadFilesFromServerDir(devId: String, session: String?, type: OneOSFileType,
                               path: String, page: Int = -1, showHidden: Boolean = false,
                               isV5: Boolean = false, orderTypeV2: FileOrderTypeV2)
            : LiveData<Resource<BaseResultModel<FileListModel>>> {
        return loadFilesFromServer(devId, session, type, path, page, showHidden, type, isV5, orderTypeV2)

    }


    private fun loadFiles(devId: String, map: Map<String, Any>, isV5: Boolean = false): LiveData<Resource<BaseResultModel<FileListModel>>> {
        val tmpMutableMap = map.toMutableMap()
        tmpMutableMap.remove("session")
        val key = userId + devId + tmpMutableMap.toString()
        Timber.d("mapBody:$map")
        Timber.d("key :$key")
        return object : NetworkBoundResource<BaseResultModel<FileListModel>, BaseResultModel<FileListModel>>(appExecutors) {
            override fun saveCallResult(item: BaseResultModel<FileListModel>) {
                if (!item.isSuccess) {
                    dispatcherError(devId, item)
                }
                FileUtils.putDiskCache(key, GsonUtils.encodeJSON(item))
            }

            override fun shouldFetch(data: BaseResultModel<FileListModel>?): Boolean {
                return (data == null || !data.isSuccess || repoListRateLimit.shouldFetch(key))
            }

            override fun loadFromDb(): LiveData<BaseResultModel<FileListModel>?> {
                Timber.d("loadFromDb -->%s", Thread.currentThread())
                val mutableLiveData = MutableLiveData<BaseResultModel<FileListModel>?>()
                appExecutors.diskIO().execute(Runnable {
                    val diskCache = FileUtils.getDiskCache(key)
                    Timber.d("diskCache files: $diskCache")
                    val model = if (diskCache.isNullOrEmpty()) {
                        null
                    } else {
                        GsonUtils.decodeJSONCatchException<BaseResultModel<FileListModel>>(diskCache, object : TypeToken<BaseResultModel<FileListModel>>() {}.type)
                    }
                    mutableLiveData.postValue(model)
                })
                return mutableLiveData
            }

            override fun createCall(): LiveData<ApiResponse<BaseResultModel<FileListModel>>> {
                return if (isV5)
                    provideNasService(devId, isV5).loadFileList(map)
                else provideNasService(devId).loadFiles(map)
            }

            override fun onFetchFailed() {
                repoListRateLimit.reset(key)
            }
        }.asLiveData()
    }

    fun loadPhotosTimeline(devId: String, session: String?, vararg type: SharePathType,
                           page: Int, orderTypeV2: FileOrderTypeV2 = FileOrderTypeV2.time_desc, month: Long? = null, year: Long? = null, day: Long? = null
    ): LiveData<Resource<BaseResultModel<FileListModel>>> {
        val params: MutableMap<String, Any> = HashMap()
//        share_path_typeint/array否0 用户目录（默认） 1 绝对目录  2 public目录
//        orderstring否详见排序结构pageint否获取的页数（从0开始，默认为0）
//        monthnumber否某个月份的 unix 秒值，获取某月份的文件（默认 无）
//        yearnumber否某个月份的 unix 秒值，获取某天的文件（默认 无）
//        daynumber否某个月份的 unix 秒值，获取某年的文件（默认 无）
        params["share_path_type"] = type.map { it.type }
        params["order"] = orderTypeV2.name
        params["num"] = AppConstants.PAGE_SIZE
        params["page"] = page
        if (month != null)
            params["month"] = month
        if (year != null)
            params["year"] = year
        if (day != null)
            params["day"] = day

        return loadFiles(devId, map(method = "photo_timeline", session = session, params = params), true)
    }

    fun searchFile(devId: String, session: String?,
                   path: String? = null,
                   pattern: String,
                   fileType: List<OneOSFileType>,
                   sharePathType: List<Int>,
                   isV5: Boolean, page: Int = 0,
                   orderTypeV2: FileOrderTypeV2 = FileOrderTypeV2.time_desc,
                   groupId: Long?=-1)

            : LiveData<Resource<BaseResultModel<FileListModel>>> {
        val params = mutableMapOf<String, Any>()
        if (!path.isNullOrEmpty())
            params["path"] = path
        params["share_path_type"] = sharePathType
        params["pattern"] = pattern
        params["ftype"] = fileType.map { it.serverTypeName }
        params["cmd"] = FileManageAction.SEARCH.getActionName()
        params["num"] = AppConstants.PAGE_SIZE
        params["page"] = page
        if(groupId!=null&& groupId>0)
        {
            params["groupid"] = groupId
        }
        params["order"] = orderTypeV2.name
        return loadFiles(devId, map("manage", session, params), isV5)
    }

    fun tagFiles(devId: String, session: String?,
                 tagId: Int,
                 path: String? = null,
                 pattern: String? = null,
                 fileType: List<OneOSFileType>,
                 isV5: Boolean, page: Int = 0,
                 orderTypeV2: FileOrderTypeV2 = FileOrderTypeV2.time_desc,
                 groupId: Long?=-1
    )
            : LiveData<Resource<BaseResultModel<FileListModel>>> {
        val params = mutableMapOf<String, Any>()
        if (!path.isNullOrEmpty())
            params["path"] = path
//        params["share_path_type"] = sharePathType
        if (!pattern.isNullOrEmpty())
            params["pattern"] = pattern
        params["ftype"] = fileType.map { it.serverTypeName }
        params["cmd"] = "files"
        params["id"] = tagId
        params["num"] = AppConstants.PAGE_SIZE
        params["page"] = page
        params["order"] = orderTypeV2.name
        if(groupId!=null&& groupId>0)
        {
            params["groupid"] = groupId
        }
        return loadFiles(devId, map("tag", session, params), isV5)
    }

    private fun getSharePathTypeByOneOsFileType(fileType: OneOSFileType): SharePathType {
        return when (fileType) {
            OneOSFileType.PUBLIC -> SharePathType.PUBLIC
            OneOSFileType.PRIVATE -> SharePathType.USER
            else -> SharePathType.VIRTUAL
        }
    }

    fun manageFile(devId: String, session: String?, cmd: FileManageAction,
                   vararg path: String, toDir: String? = null,
                   action: Any? = null, newName: String? = null)
            : LiveData<Resource<ActionResultModel<OneOSFile?>>> {
        val (params: MutableMap<String, Any>, timeout) = buildParams(path, cmd, toDir, action, newName)
        return manageFiles(cmd, devId, map("manage", session, params), timeout)
    }

    fun manageFile2(devId: String, session: String?, cmd: FileManageAction,
                    path: String, toDir: String? = null,
                    action: Any? = null, newName: String? = null, callback: Callback<Resource<ActionResultModel<OneOSFile?>>>) {
        manageFile2(devId = devId, session = session, cmd = cmd, path = Collections.singletonList(path), toDir = toDir, action = action, newName = newName, callback = callback)
    }

    fun manageFile2(devId: String, session: String?, cmd: FileManageAction,
                    path: List<String>, toDir: String? = null,
                    action: Any? = null, newName: String? = null, callback: Callback<Resource<ActionResultModel<OneOSFile?>>>) {

        val (params: MutableMap<String, Any>, timeout) = buildParams(path.toTypedArray(), cmd, toDir, action, newName)
        val map = map("manage", session, params)
        try {
            val manageFiles2 = provideNasService(devId, isV5 = true, isUseLiveData = false).manageFiles2(map).execute()
            if (manageFiles2.isSuccessful) {
                val data = manageFiles2.body()
                if (data?.isSuccess != true) {
                    dispatcherError(devId, data)
                }
                data?.action = cmd
                callback.result(Resource.success(data))
            } else {
                val data = ActionResultModel<OneOSFile?>()
                data.action = cmd
                callback.result(Resource.error("failure", data, manageFiles2.code()))
            }
        } catch (e: Exception) {
            val data = ActionResultModel<OneOSFile?>()
            data.action = cmd
            callback.result(Resource.error(e.message ?: "unknow", data))
        }

    }

    fun buildParams(path: Array<out String>,
                    cmd: FileManageAction,
                    toDir: String?,
                    action: Any?,
                    newName: String?): Pair<MutableMap<String, Any>, Long> {
        val params: MutableMap<String, Any> = HashMap()
        if (!path.isNullOrEmpty())
            params["path"] = path
        val toLowerCaseCmd = cmd.getActionName()
        params["cmd"] = toLowerCaseCmd.replace("_", "")
        toDir?.apply {
            params["todir"] = this
        }
        action?.apply {
            params["action"] = action
        }
        params["share_path_type"] = SharePathType.PUBLIC.type
        if (!newName.isNullOrEmpty()) {
            params["newname"] = newName
        }
        val timeout = TimeUnit.SECONDS.toMillis(when (action) {
            FileManageAction.DELETE,
            FileManageAction.DELETE_SHIFT -> 15
            FileManageAction.MOVE,
            FileManageAction.COPY -> 20
            else -> 3
        })
        return Pair(params, timeout)
    }

    private fun manageFiles(cmd: FileManageAction, devId: String, map: Map<String, Any>, timeout: Long): LiveData<Resource<ActionResultModel<OneOSFile?>>> {
        val result = MediatorLiveData<Resource<ActionResultModel<OneOSFile?>>>()
        result.postValue(Resource.loading(null))
        val apiSource = provideNasService(devId, isV5 = true).manageFiles(map)
        result.addSource(apiSource) { response ->
            result.removeSource(apiSource)
            val resource = when (response) {
                is ApiSuccessResponse -> {
                    val body = response.body
                    if (!body.isSuccess) {
                        dispatcherError(devId, body)
                    }
                    Resource.success(body)
                }
                else -> {
                    Resource.error("null or error", null, HttpErrorNo.UNKNOWN_EXCEPTION)
                }
            }
            resource.data?.action = cmd
            result.postValue(resource)
        }
        return result

    }

    fun dispatcherError(devId: String, baseResultModel: BaseResultModel<*>?) {
        val code = baseResultModel?.error?.code
        if (code == HttpErrorNo.ERR_ONE_NO_LOGIN
                || code == V5_ERR_SESSION_EXP) {
            SessionManager.getInstance().removeSession(devId)
        }
    }

    fun loadPhotosTimelineSummary(devId: String, session: String?, vararg type: SharePathType)
            : LiveData<Resource<BaseResultModel<List<DataPhotosTimelineYearSummary>>>> {
        val flatMap = type.map<SharePathType, Int> { it.type }
        val map = map("photo_timeline_summary", session, params = hashMapOf("share_path_type" to flatMap))
        val tmpMutableMap = map.toMutableMap()
        tmpMutableMap.remove("session")
        val key = userId + devId + tmpMutableMap.toString()
        Timber.d("mapBody:$map")
        Timber.d("key :$key")
        return object : NetworkBoundResource<BaseResultModel<List<DataPhotosTimelineYearSummary>>, BaseResultModel<List<DataPhotosTimelineYearSummary>>>(appExecutors) {
            override fun saveCallResult(item: BaseResultModel<List<DataPhotosTimelineYearSummary>>) {
                if (!item.isSuccess) {
                    dispatcherError(devId, item)
                }
                FileUtils.putDiskCache(key, GsonUtils.encodeJSON(item))
            }

            override fun shouldFetch(data: BaseResultModel<List<DataPhotosTimelineYearSummary>>?): Boolean {
                return (data == null || !data.isSuccess || repoListRateLimit.shouldFetch(key))
            }

            override fun loadFromDb(): LiveData<BaseResultModel<List<DataPhotosTimelineYearSummary>>?> {
                val mutableLiveData = MutableLiveData<BaseResultModel<List<DataPhotosTimelineYearSummary>>?>()
                appExecutors.diskIO().execute(Runnable {
                    val diskCache = FileUtils.getDiskCache(key)
                    Timber.d("diskCache loadPhotosTimelineSummary: $diskCache")
                    val model = if (diskCache.isNullOrEmpty()) {
                        null
                    } else {
                        GsonUtils.decodeJSONCatchException<BaseResultModel<List<DataPhotosTimelineYearSummary>>>(diskCache, object : TypeToken<BaseResultModel<List<DataPhotosTimelineYearSummary>>>() {}.type)
                    }
                    mutableLiveData.postValue(model)
                })
                return mutableLiveData
            }

            override fun createCall(): LiveData<ApiResponse<BaseResultModel<List<DataPhotosTimelineYearSummary>>>> {
                return provideNasService(devId, true).loadPhotosTimelineSummary(map)

            }

            override fun onFetchFailed() {
                repoListRateLimit.reset(key)
            }
        }.asLiveData()
    }
}