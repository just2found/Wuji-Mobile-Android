package io.weline.repo.files

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.switchMap
import com.google.gson.reflect.TypeToken
import io.reactivex.Observable
import io.weline.repo.RepoApi
import io.weline.repo.files.NasCompat.getSharePathType
import io.weline.repo.files.constant.AppConstants
import io.weline.repo.files.constant.AppConstants.HS_ANDROID_TV_PORT
import io.weline.repo.files.constant.HttpErrorNo
import io.weline.repo.files.constant.OneOSAPIs
import io.weline.repo.files.data.*
import libs.source.common.AppExecutors
import libs.source.common.livedata.*
import libs.source.common.livedata.ApiHTTPErrNO.STATUS_CODE_THROWABLE
import libs.source.common.utils.DiskLruCacheHelper
import libs.source.common.utils.GsonUtils
import libs.source.common.utils.RateLimiter
import net.sdvn.common.internet.OkHttpClientIns
import okhttp3.HttpUrl
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import timber.log.Timber
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicLong

class NasNetworkSource constructor(private val repoApi: RepoApi, private val appExecutors: AppExecutors) : NasApis {

    private val repoListRateLimit = RateLimiter<String>(1, TimeUnit.SECONDS)
    private val repoSessionRateLimit = RateLimiter<String>(119, TimeUnit.MINUTES)
    private val mapSessions = hashMapOf<String, LoginSession>()

    companion object {
        fun provideNasService(address: String, isV5: Boolean = false, isUseLiveData: Boolean = true): NasService {
            val builder = if (isV5)
                HttpUrl.Builder()
                        .scheme(OneOSAPIs.SCHME_HTTP)
                        .host(address)
                        .port(HS_ANDROID_TV_PORT)
            else HttpUrl.Builder()
                    .scheme(OneOSAPIs.SCHME_HTTP)
                    .host(address)

            val retrofitBuilder = Retrofit.Builder()
                    .baseUrl(builder.build())
                    .addConverterFactory(GsonConverterFactory.create())
                    .addConverterFactory(ReqStringResGsonConverterFactory.create())
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .addCallAdapterFactory(LiveDataCallAdapterFactory.create())
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

    }

    override fun loadFilesFromServer(devId: String, type: OneOSFileType,
                                     path: String?, page: Int,
                                     showHidden: Boolean, filter: OneOSFileType
                                     , order: String?)
            : LiveData<Resource<BaseResultModel<FileListModel>>> {

        return object : MediatorResource<BaseResultModel<FileListModel>, LoginSession>() {
            override fun fun2Call(loginSession: LoginSession): LiveData<Resource<BaseResultModel<FileListModel>>> {
                val params: MutableMap<String, Any> = HashMap()
                if (!path.isNullOrEmpty()) {
                    params["path"] = if (loginSession.isV5 && path.startsWith(OneOSAPIs.ONE_OS_PUBLIC_ROOT_DIR)) {
                        path.replaceFirst(OneOSAPIs.ONE_OS_PUBLIC_ROOT_DIR, OneOSAPIs.ONE_OS_PRIVATE_ROOT_DIR)
                    } else path
                } else {
                    if (loginSession.isV5) {
                        params["path"] = "/"
                    }
                }
                if (order.isNullOrEmpty()) {
                    if (type != OneOSFileType.PICTURE) {
                        params["sort"] = "1"
                        params["order"] = "time_desc"
                    } else {
                        params["sort"] = "5"
                        params["order"] = "cttime_desc"
                    }
                } else {
                    params["order"] = order
                }


                if (loginSession.isV5) {
                    params["share_path_type"] = getSharePathType(type).type
                }
                params["ftype"] = when (filter) {
                    OneOSFileType.PRIVATE -> OneOSFileType.ALL.serverTypeName
                    OneOSFileType.PUBLIC -> OneOSFileType.ALL.serverTypeName
                    OneOSFileType.RECYCLE -> OneOSFileType.ALL.serverTypeName
                    OneOSFileType.DOC -> {
                        val arrayList = ArrayList<String>()
                        arrayList.add("txt")
                        arrayList.add("doc")
                        arrayList.add("xls")
                        arrayList.add("ppt")
                        arrayList.add("pdf")

                        arrayList
                    }

                    else -> filter.serverTypeName
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
                return loadFiles(devId, map(if (loginSession.isV5) {
                    "list"
                } else {
                    when (type) {
                        OneOSFileType.PRIVATE -> "list"
                        OneOSFileType.PUBLIC -> "list"
                        OneOSFileType.RECYCLE -> "list"
                        OneOSFileType.IMAGE_TIMELINE -> "recent"
                        else -> {
                            "listdb"
                        }
                    }
                }, loginSession.session, params), loginSession.isV5)
            }

            override fun fun1Call(): LiveData<Resource<LoginSession>> {
                return access(devId)
            }

        }.asLiveData()
    }

    override fun genThumbnailUrl(devId: String, path: String, type: OneOSFileType): String? {
        mapSessions[devId]?.let {
            return OneOSAPIs.genThumbnailUrl(it, path, getSharePathType(type))
        } ?: return null
    }

    override fun genDownloadUrl(devId: String, path: String, type: OneOSFileType): String? {
        mapSessions[devId]?.let {
            return OneOSAPIs.genDownloadUrl(it, path, getSharePathType(type))
        } ?: return null
    }

    override fun loadTagFilesLD(devId: String, tagId: Int, page: Int): LiveData<Resource<BaseResultModel<FileListModel>>> {
        return object : MediatorResource<BaseResultModel<FileListModel>, LoginSession>() {
            override fun fun2Call(loginSession: LoginSession): LiveData<Resource<BaseResultModel<FileListModel>>> {
                return loadTagFiles(devId, map("tag", loginSession.session,
                        mapOf(Pair("cmd", "files"), Pair("id", tagId), Pair("page", page))),
                        loginSession.isV5)
            }

            override fun fun1Call(): LiveData<Resource<LoginSession>> {
                return access(devId)
            }

        }.asLiveData()
    }

    override fun loadTagFiles(devId: String, tagId: Int, page: Int): Observable<Resource<BaseResultModel<FileListModel>>> {
        return accessRx(devId).flatMap {
            val address = repoApi.getAddress(devId)
            if (address.isNullOrEmpty()) {
                Observable.just(Resource.error("get address is null", null, HttpErrorNo.ERR_ONESERVER_DEVICE_OFFLINE))
            } else {
                if (it.status == Status.SUCCESS) {
                    val map = map("tag", it.data!!.session, mapOf(Pair("cmd", "files"), Pair("id", tagId), Pair("page", page)))
                    if (it.data!!.isV5) {
                        provideNasService(address, it.data!!.isV5).getTagFilesV5(map)
                    } else {
                        provideNasService(address, it.data!!.isV5).getTagFiles(map)
                    }.flatMap { baseResultModel ->
                        Observable.just(if (baseResultModel.isSuccess) {
                            Resource.success(baseResultModel)
                        } else {
                            Resource.error(baseResultModel.error?.msg
                                    ?: "", null, baseResultModel.error?.code
                                    ?: STATUS_CODE_THROWABLE)
                        })
                    }
                } else {
                    Observable.just(Resource.error(it.message ?: "", null, it.code
                            ?: STATUS_CODE_THROWABLE))
                }
            }
        }
    }

    override fun getFileTagsLD(devId: String): LiveData<Resource<BaseResultModel<List<FileTag>>>> {
        return object : MediatorResource<BaseResultModel<List<FileTag>>, LoginSession>() {
            override fun fun2Call(loginSession: LoginSession): LiveData<Resource<BaseResultModel<List<FileTag>>>> {
                val map = map("tags", loginSession.session,
                        mapOf(Pair("cmd", "list")))
                val liveData = if (loginSession.isV5)
                    provideNasService(loginSession.address, loginSession.isV5).getTagsLDV5(map)
                else provideNasService(loginSession.address).getTagsLD(map)

                return liveData.switchMap { response ->
                    val resource = when (response) {
                        is ApiSuccessResponse -> {
                            val data = response.body
                            if (data.isSuccess) {
                                Resource.success(data)
                            } else {
                                onError(data, devId)
                                Resource.error(data.error?.msg ?: "", data)
                            }
                        }
                        else -> {
                            Resource.error("null or error", null, HttpErrorNo.UNKNOWN_EXCEPTION)
                        }
                    }
                    MutableLiveData(resource)
                }
            }

            override fun fun1Call(): LiveData<Resource<LoginSession>> {
                return access(devId)
            }

        }.asLiveData()
    }

    override fun getFileTags(devId: String): Observable<Resource<BaseResultModel<List<FileTag>>>> {
        return accessRx(devId).flatMap {
            val address = repoApi.getAddress(devId)
            if (address.isNullOrEmpty()) {
                Observable.just(Resource.error("get address is null", null, HttpErrorNo.ERR_ONESERVER_DEVICE_OFFLINE))
            } else {
                if (it.status == Status.SUCCESS) {
                    val map = map("tags", it.data!!.session, mapOf(Pair("cmd", "list")))
                    if (it.data!!.isV5) {
                        provideNasService(address, it.data!!.isV5).getTagsV5(map)
                    } else {
                        provideNasService(address, it.data!!.isV5).getTags(map)
                    }.flatMap { baseResultModel ->
                        Observable.just(if (baseResultModel.isSuccess) {
                            Resource.success(baseResultModel)
                        } else {
                            Resource.error(baseResultModel.error?.msg
                                    ?: "", null, baseResultModel.error?.code
                                    ?: STATUS_CODE_THROWABLE)
                        })
                    }
                } else {
                    Observable.just(Resource.error(it.message ?: "", null, it.code
                            ?: STATUS_CODE_THROWABLE))
                }
            }
        }
    }

    private fun loadTagFiles(devId: String, map: Map<String, Any>, isV5: Boolean = false): LiveData<Resource<BaseResultModel<FileListModel>>> {
        val tmpMutableMap = map.toMutableMap()
        tmpMutableMap.remove("session")
        val key = repoApi.getUserId() + devId + tmpMutableMap.toString()
        Timber.d("mapBody:$map")
        Timber.d("key :$key")
        val address = repoApi.getAddress(devId)
        if (address.isNullOrEmpty()) {
            return MutableLiveData<Resource<BaseResultModel<FileListModel>>>().apply {
                postValue(Resource.error("get address is null", null, HttpErrorNo.ERR_ONESERVER_DEVICE_OFFLINE))
            }
        }
        return object : NetworkBoundResource<BaseResultModel<FileListModel>, BaseResultModel<FileListModel>>(appExecutors) {
            override fun saveCallResult(item: BaseResultModel<FileListModel>) {
                if (item.isSuccess) {
                    DiskLruCacheHelper.putDiskCache(key, GsonUtils.encodeJSON(item))
                } else {
                    onError(item, devId)
                }
            }

            override fun shouldFetch(data: BaseResultModel<FileListModel>?): Boolean {
                return (data == null || !data.isSuccess || data.data == null || repoListRateLimit.shouldFetch(key))
            }

            override fun loadFromDb(): LiveData<BaseResultModel<FileListModel>?> {
                val mutableLiveData = MutableLiveData<BaseResultModel<FileListModel>?>()
                appExecutors.diskIO().execute {
                    val diskCache = DiskLruCacheHelper.getDiskCache(key)
                    Timber.d("diskCache files: $diskCache")
                    val model = if (diskCache.isNullOrEmpty()) {
                        null
                    } else {
                        GsonUtils.decodeJSONCatchException<BaseResultModel<FileListModel>>(diskCache, object : TypeToken<BaseResultModel<FileListModel>>() {}.type)
                    }
                    mutableLiveData.postValue(model)
                }
                return mutableLiveData
            }

            override fun createCall(): LiveData<ApiResponse<BaseResultModel<FileListModel>>> {
                return if (isV5)
                    provideNasService(address, isV5).getTagFilesLDV5(map)
                else provideNasService(address).getTagFilesLD(map)
            }

            override fun onFetchFailed() {
                repoListRateLimit.reset(key)
            }
        }.asLiveData()
    }

    private fun loadFiles(devId: String, map: Map<String, Any>, isV5: Boolean = false): LiveData<Resource<BaseResultModel<FileListModel>>> {
        val tmpMutableMap = map.toMutableMap()
        tmpMutableMap.remove("session")
        val key = repoApi.getUserId() + devId + tmpMutableMap.toString()
        Timber.d("mapBody:$map")
        Timber.d("key :$key")
        val address = repoApi.getAddress(devId)
        if (address.isNullOrEmpty()) {
            return MutableLiveData<Resource<BaseResultModel<FileListModel>>>().apply {
                postValue(Resource.error("get address is null", null, HttpErrorNo.ERR_ONESERVER_DEVICE_OFFLINE))
            }
        }
        return object : NetworkBoundResource<BaseResultModel<FileListModel>, BaseResultModel<FileListModel>>(appExecutors) {
            override fun saveCallResult(item: BaseResultModel<FileListModel>) {
                if (item.isSuccess) {
                    DiskLruCacheHelper.putDiskCache(key, GsonUtils.encodeJSON(item))
                } else {
                    onError(item, devId)
                }
            }

            override fun shouldFetch(data: BaseResultModel<FileListModel>?): Boolean {
                return (data == null || !data.isSuccess || data.data == null || repoListRateLimit.shouldFetch(key))
            }

            override fun loadFromDb(): LiveData<BaseResultModel<FileListModel>?> {
                val mutableLiveData = MutableLiveData<BaseResultModel<FileListModel>?>()
                appExecutors.diskIO().execute {
                    val diskCache = DiskLruCacheHelper.getDiskCache(key)
                    Timber.d("diskCache files: $diskCache")
                    val model = if (diskCache.isNullOrEmpty()) {
                        null
                    } else {
                        GsonUtils.decodeJSONCatchException<BaseResultModel<FileListModel>>(diskCache, object : TypeToken<BaseResultModel<FileListModel>>() {}.type)
                    }
                    mutableLiveData.postValue(model)
                }
                return mutableLiveData
            }

            override fun createCall(): LiveData<ApiResponse<BaseResultModel<FileListModel>>> {
                return if (isV5)
                    provideNasService(address, isV5).loadFileList(map)
                else provideNasService(address).loadFiles(map)
            }

            override fun onFetchFailed() {
                repoListRateLimit.reset(key)
            }
        }.asLiveData()
    }


    override fun searchFile(devId: String,
                            path: String?,
                            pattern: String,
                            pathType: OneOSFileType,
                            fileType: OneOSFileType,
                            showHidden: Boolean
    ): LiveData<Resource<BaseResultModel<FileListModel>>> {

        return object : MediatorResource<BaseResultModel<FileListModel>, LoginSession>() {
            override fun fun2Call(loginSession: LoginSession): LiveData<Resource<BaseResultModel<FileListModel>>> {
                val params = mutableMapOf<String, Any>()
                val sharePathType = getSharePathTypeByOneOsFileType(pathType)
                if (!path.isNullOrEmpty())
                    params["path"] = path
                params["share_path_type"] = sharePathType.type
                params["pattern"] = pattern
                params["ftype"] = fileType.serverTypeName
                params["cmd"] = FileManageAction.SEARCH.getActionName()
                params["show_hidden"] = if (showHidden) 1 else 0
                return loadFiles(loginSession.devId, map("manage", loginSession.session, params), loginSession.isV5)
            }

            override fun fun1Call(): LiveData<Resource<LoginSession>> {
                return access(devId)
            }

        }.asLiveData()
    }

    private fun getSharePathTypeByOneOsFileType(fileType: OneOSFileType): SharePathType {
        return when (fileType) {
            OneOSFileType.PUBLIC -> SharePathType.PUBLIC
            OneOSFileType.PRIVATE -> SharePathType.USER
            else -> SharePathType.VIRTUAL
        }
    }


    fun manageFile(devId: String, cmd: FileManageAction,
                   path: List<String>, toDir: String? = null,
                   action: Any? = null, newName: String? = null,
                   sharePathType: SharePathType? = null)
            : LiveData<Resource<ActionResultModel<OneOSFile?>>> {
        val (params: MutableMap<String, Any>, timeout) = buildParams(path, cmd, toDir, action, newName, sharePathType)
        return object : MediatorResource<ActionResultModel<OneOSFile?>, LoginSession>() {
            override fun fun2Call(type: LoginSession): LiveData<Resource<ActionResultModel<OneOSFile?>>> {
                return manageFiles(cmd, devId, map("manage", type.session, params), timeout, type.isV5)
            }

            override fun fun1Call(): LiveData<Resource<LoginSession>> {
                return access(devId)
            }

        }.asLiveData()
    }

    private fun buildParams(paths: List<String>,
                            cmd: FileManageAction,
                            toDir: String?,
                            action: Any?,
                            newName: String?,
                            sharePathType: SharePathType? = null): Pair<MutableMap<String, Any>, Long> {
        val params: MutableMap<String, Any> = HashMap()
        if (!paths.isNullOrEmpty())
            params["path"] = paths
        val toLowerCaseCmd = cmd.getActionName()
        params["cmd"] = toLowerCaseCmd.replace("_", "")
        toDir?.let {
            params["todir"] = toDir
        }
        action?.let {
            params["action"] = action
        }
        if (sharePathType != null)
            params["share_path_type"] = sharePathType.type
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

    private fun manageFiles(cmd: FileManageAction, devId: String, map: Map<String, Any>
                            , timeout: Long, isV5: Boolean): LiveData<Resource<ActionResultModel<OneOSFile?>>> {
        val result = MediatorLiveData<Resource<ActionResultModel<OneOSFile?>>>()
        result.postValue(Resource.loading(null))
        val address = repoApi.getAddress(devId)
        if (address.isNullOrEmpty()) {
            result.postValue(Resource.error("get address is null", null, HttpErrorNo.ERR_ONESERVER_DEVICE_OFFLINE))
            return result
        }
        val provideNasService = provideNasService(address, isV5 = isV5)
        val apiSource = if (isV5) {
            provideNasService.manageFiles(map)
        } else {
            provideNasService.manageFilesOneOS(map)
        }
        result.addSource(apiSource) { response ->
            result.removeSource(apiSource)
            val resource = when (response) {
                is ApiSuccessResponse -> {
                    val data = response.body
                    if (data.isSuccess) {
                        Resource.success(data)
                    } else {
                        onError(data, devId)
                        Resource.error(data.error?.msg ?: "", data)
                    }
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

    private fun onError(data: BaseResultModel<*>, devId: String) {
        when (data.error?.code) {
            HttpErrorNo.ERR_ONE_NO_LOGIN,
            HttpErrorNo.ERR_ONE_SESSION_EXPIRED -> {
                repoSessionRateLimit.reset(devId)
            }
            else -> {

            }
        }
    }

    override fun access(devId: String): LiveData<Resource<LoginSession>> {
        val result = MediatorLiveData<Resource<LoginSession>>()
        val loginSession1 = mapSessions[devId]
        if (loginSession1 != null &&
                !loginSession1.session.isNullOrEmpty() &&
                !repoSessionRateLimit.shouldFetch(devId)) {
            result.postValue(Resource.success(loginSession1))
        } else {
            val token = repoApi.getToken()
            val address = repoApi.getAddress(devId)
            if (address.isNullOrEmpty()) {
                result.postValue(Resource.error("get addressis null", null, HttpErrorNo.ERR_ONESERVER_DEVICE_OFFLINE))
            } else if (token.isNullOrEmpty()) {
                Timber.d("token is null req delay")
                retryDelay(2000, Runnable {
                    if (result.hasActiveObservers()) {
                        result.addSource(access(devId)) {
                            result.postValue(it)
                        }
                    }
                })
            } else {
                val isV5 = repoApi.isV5(devId)
                val map = map(if (isV5) {
                    "access"
                } else {
                    "menet"
                }, null,
                        mapOf("token" to token))
                Timber.d("access params: ${(map)}")
                val provideNasService = provideNasService(address, isV5)
                val access = if (isV5) {
                    provideNasService.access(map)
                } else {
                    provideNasService.menet(map)
                }
                result.addSource(access) {
                    result.removeSource(access)
                    Timber.d("access response : $it")
                    when (it) {
                        is ApiSuccessResponse -> {
                            if (it.body.isSuccess) {
                                val session = it.body.data.session
                                val loginSession = LoginSession(devId, address, session)
                                loginSession.isV5 = isV5
                                loginSession.userInfo = it.body.data.userInfo
                                mapSessions.put(devId, loginSession)
                                result.postValue(Resource.success(loginSession))
                            } else {
                                val error = it.body.error
                                if ((error?.code == HttpErrorNo.ERR_ONE_REQUEST &&
                                                "无效的Token" == error?.msg) ||
                                        error?.code == -40001) {
                                    mapSessions.get(devId)?.session = null
                                    repoApi.onTokenError(error.code, error.msg)
                                }
                                result.postValue(Resource.error(error?.msg
                                        ?: "unknow", null, error?.code
                                        ?: STATUS_CODE_THROWABLE))
                            }
                        }
                        else -> {
                            result.postValue(Resource.error("unknow", null))
                        }
                    }
                }
            }
        }
        return result
    }

    fun accessRx(devId: String): Observable<Resource<LoginSession>> {
        val loginSession1 = mapSessions[devId]
        if (loginSession1 != null &&
                !loginSession1.session.isNullOrEmpty() &&
                !repoSessionRateLimit.shouldFetch(devId)) {
            return Observable.just(Resource.success(loginSession1))
        } else {
            val address = repoApi.getAddress(devId)
            if (address.isNullOrEmpty()) {
                return Observable.just(Resource.error("get addressis null", null, HttpErrorNo.ERR_ONESERVER_DEVICE_OFFLINE))
            } else {
                return Observable.create<String> { emitter ->
                    val token = repoApi.getToken()
                    if (token.isNullOrEmpty()) {
                        Timber.d("token is null req delay")
                        emitter.onError(NullPointerException("token is null req delay"))
                    } else {
                        emitter.onNext(token)
                    }
                }.retryWhen {
                    val counter = AtomicLong()
                    it.takeWhile { counter.getAndIncrement() < 3 }
                            .flatMap {
                                Observable.timer(counter.get(), TimeUnit.SECONDS)
                            }
                }.flatMap { token: String ->
                    val isV5 = repoApi.isV5(devId)
                    val map = map(if (isV5) {
                        "access"
                    } else {
                        "menet"
                    }, null, mapOf("token" to token))
                    Timber.d("access params: ${(map)}")
                    val provideNasService = provideNasService(address, isV5)
                    if (isV5) {
                        provideNasService.accessRx(map)
                    } else {
                        provideNasService.menetRx(map)
                    }.flatMap {
                        Observable.just(if (it.isSuccess) {
                            val session = it.data.session
                            val loginSession = LoginSession(devId, address, session)
                            loginSession.isV5 = isV5
                            loginSession.userInfo = it.data.userInfo
                            mapSessions.put(devId, loginSession)
                            Resource.success(loginSession)
                        } else {
                            val error = it.error
                            if ((error?.code == HttpErrorNo.ERR_ONE_REQUEST &&
                                            "无效的Token" == error?.msg) ||
                                    error?.code == -40001) {
                                mapSessions.get(devId)?.session = null
                                repoApi.onTokenError(error.code, error.msg)
                            }
                            Resource.error(error?.msg
                                    ?: "unknow", null, error?.code
                                    ?: STATUS_CODE_THROWABLE)
                        })
                    }
                }


            }
        }
    }

    private fun retryDelay(delay: Long, run: Runnable) {
        appExecutors.networkIO().execute {
            Thread.sleep(delay)
            appExecutors.mainThread().execute(run)
        }
    }


    override fun attr(devId: String, path: String, sharePathType: SharePathType?): LiveData<Resource<ActionResultModel<OneOSFile?>>> {
        return manageFile(devId, cmd = FileManageAction.ATTRIBUTES, path = listOf(path), sharePathType = sharePathType)
    }

    override fun delete(devId: String, paths: List<String>, isShift: Boolean, sharePathType: SharePathType?): LiveData<Resource<ActionResultModel<OneOSFile?>>> {
        return manageFile(devId, cmd = (if (isShift) {
            FileManageAction.DELETE_SHIFT
        } else {
            FileManageAction.DELETE
        }), path = paths, sharePathType = sharePathType)
    }

    override fun rename(devId: String, path: String, newName: String, sharePathType: SharePathType?): LiveData<Resource<ActionResultModel<OneOSFile?>>> {
        return manageFile(devId, FileManageAction.RENAME, path = listOf(path), newName = newName, sharePathType = sharePathType)
    }

    override fun mkdir(devId: String, path: String, sharePathType: SharePathType?): LiveData<Resource<ActionResultModel<OneOSFile?>>> {
        return manageFile(devId, FileManageAction.MKDIR, path = listOf(path), sharePathType = sharePathType)
    }

    override fun copy(devId: String, paths: List<String>, toDir: String, sharePathType: SharePathType?): LiveData<Resource<ActionResultModel<OneOSFile?>>> {
        return manageFile(devId, FileManageAction.COPY, path = (paths), newName = toDir, sharePathType = sharePathType)
    }

    override fun move(devId: String, paths: List<String>, toDir: String, sharePathType: SharePathType?): LiveData<Resource<ActionResultModel<OneOSFile?>>> {
        return manageFile(devId, FileManageAction.MOVE, path = (paths), newName = toDir, sharePathType = sharePathType)
    }

}

abstract class MediatorResource<ResultType, RequestType>() {
    private val result = MediatorLiveData<Resource<ResultType>>()

    private fun run() {
        val source = fun1Call()
        result.addSource(source) {
            if (it.status == Status.SUCCESS) {
                result.addSource(fun2Call(it.data!!)) {
                    result.postValue(it)
                }
            } else if (it.status == Status.ERROR) {
                result.postValue(Resource.error(it.message ?: "Unknown", null, it.code
                        ?: STATUS_CODE_THROWABLE))
            }
        }
    }

    abstract fun fun2Call(type: RequestType): LiveData<Resource<ResultType>>

    abstract fun fun1Call(): LiveData<Resource<RequestType>>

    fun asLiveData(): LiveData<Resource<ResultType>> {
        run()
        return result
    }
}