package net.sdvn.nascommon.viewmodel

import androidx.collection.ArraySet
import androidx.collection.arraySetOf
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.Observer
import com.google.gson.Gson
import io.reactivex.Observable
import io.reactivex.ObservableEmitter
import io.reactivex.ObservableOnSubscribe
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers
import io.weline.repo.SessionCache
import io.weline.repo.api.V5_ERR_DENIED_PERMISSION
import io.weline.repo.data.model.BaseProtocol
import io.weline.repo.net.V5Observer
import io.weline.repo.repository.V5Repository
import libs.source.common.AppExecutors
import libs.source.common.livedata.Resource
import libs.source.common.livedata.Status
import net.sdvn.common.internet.utils.LoginTokenUtil
import net.sdvn.nascommon.SessionManager
import net.sdvn.nascommon.constant.HttpErrorNo
import net.sdvn.nascommon.constant.HttpErrorNo.ERR_ONE_REQUEST
import net.sdvn.nascommon.constant.OneOSAPIs
import net.sdvn.nascommon.fileserver.constants.SharePathType
import net.sdvn.nascommon.iface.DisplayMode
import net.sdvn.nascommon.iface.GetSessionListener
import net.sdvn.nascommon.iface.Result
import net.sdvn.nascommon.model.FileOrderType
import net.sdvn.nascommon.model.FileOrderTypeV2
import net.sdvn.nascommon.model.FileViewerType
import net.sdvn.nascommon.model.PathTypeCompat
import net.sdvn.nascommon.model.oneos.BaseResultModel
import net.sdvn.nascommon.model.oneos.OneOSFile
import net.sdvn.nascommon.model.oneos.OneOSFileType
import net.sdvn.nascommon.model.oneos.api.file.OneOSSearchAPI
import net.sdvn.nascommon.model.oneos.api.file.OneOSSearchAPI.OnSearchFileListener
import net.sdvn.nascommon.model.oneos.user.LoginSession
import net.sdvn.nascommon.model.oneos.vo.FileListModel
import net.sdvn.nascommon.repository.NasRepository
import net.sdvn.nascommon.utils.EmptyUtils
import net.sdvn.nascommon.viewmodel.FilesContract.Presenter
import org.json.JSONArray
import org.json.JSONObject
import timber.log.Timber
import java.lang.ref.WeakReference
import java.util.*
import kotlin.collections.set

class FilesViewModel : RxViewModel(), Presenter<OneOSFile> {
    private val mModelHashMap = HashMap<String, DeviceDisplayModel>()
    private var compositeDisposable: CompositeDisposable? = null
    private val mNasRepository: NasRepository = NasRepository(SessionManager.getInstance().userId, AppExecutors.instance)


    override fun add(devId: String) {
        var deviceDisplayModel = mModelHashMap[devId]
        if (deviceDisplayModel == null) {
            deviceDisplayModel = DeviceDisplayModel(devId)
            mModelHashMap[devId] = deviceDisplayModel
        }
    }

    fun getDeviceDisplayModel(devId: String): DeviceDisplayModel? {
        return mModelHashMap[devId]
    }

    fun setOSFileView(devId: String, view: FilesContract.View<OneOSFile>) {
        var deviceDisplayModel = mModelHashMap[devId]
        if (deviceDisplayModel == null) {
            deviceDisplayModel = DeviceDisplayModel(devId)
            mModelHashMap[devId] = deviceDisplayModel
        }
        deviceDisplayModel.mRefOSFileView = WeakReference(view)
        view.onRestoreState(deviceDisplayModel)
    }

    override fun getFiles(devId: String, fileType: OneOSFileType, path: String?, page: Int, isLoadMore: Boolean, orderTypeV2: FileOrderTypeV2) {
        val deviceDisplayModel = mModelHashMap[devId]
        deviceDisplayModel?.let {
            addDisposable(getLoginSessionResultObservable(devId, it)
                    .subscribe({ result ->
                        if (result.isSuccess) {
                            val loginSession = result.data as LoginSession
                            val observer = Observer<Resource<BaseResultModel<FileListModel>>> { resource: Resource<BaseResultModel<FileListModel>?> ->
                                if (resource.status === Status.SUCCESS) {
                                    val data: BaseResultModel<FileListModel>? = resource.data
                                    if (data != null) {
                                        if (data.isSuccess) {
                                            dispatchResult(path, data.data?.total
                                                    ?: 0, data.data?.pages ?: 0,
                                                    data.data?.page
                                                            ?: 0, data.data?.files, deviceDisplayModel, fileType, isLoadMore)
                                        } else {
                                            dispatchError(data.error?.code, data.error?.msg, deviceDisplayModel)
                                        }
                                    }
                                } else if (resource.status === Status.ERROR) {
                                    dispatchError(HttpErrorNo.UNKNOWN_EXCEPTION, resource.message, deviceDisplayModel)
                                }
                            }
                            if (OneOSFileType.isDir(fileType)) { //                        final OneOSListDirAPI listDirAPI = new OneOSListDirAPI(loginSession, path);
//                        listDirAPI.setOnFileListListener(onFileListListener);
//                        listDirAPI.list(page);
                                mNasRepository.loadFilesFromServerDir(devId, loginSession.session,
                                        fileType, path
                                        ?: "", page, false, loginSession.isV5, orderTypeV2 = orderTypeV2)
                                        .observe((deviceDisplayModel), observer)
                            } else { //                        OneOSListDBAPI listDBAPI = new OneOSListDBAPI(loginSession, fileType);
//                        listDBAPI.setOnFileListListener(onFileListListener);
//                        listDBAPI.list(page);
                                mNasRepository.loadFilesFromServerDB(devId, loginSession.session,
                                        fileType, path, page, orderTypeV2 = orderTypeV2)
                                        .observe((deviceDisplayModel), observer)
                            }
                        } else {
                            dispatchError(result.code, result.msg, deviceDisplayModel)
                        }
                    }, { t -> Timber.e(t) }))
        }
    }

    private fun dispatchResult(path: String?, total: Int, pages: Int, page: Int,
                               files: List<OneOSFile>?,
                               deviceDisplayModel: DeviceDisplayModel?,
                               fileType: OneOSFileType, isLoadMore: Boolean) {
        deviceDisplayModel!!.curPath = path
        deviceDisplayModel.total = total
        deviceDisplayModel.mPage = page
        deviceDisplayModel.mPages = pages
        deviceDisplayModel.isSearch = false
        if (OneOSFileType.isDir(fileType)) {
            if (EmptyUtils.isEmpty(deviceDisplayModel.curPath)) {
                deviceDisplayModel.curPath = OneOSFileType.getRootPath(fileType)
            }
        }
        if (files != null) {
            if (isLoadMore && deviceDisplayModel.mOneOSFiles != null) {
                deviceDisplayModel.mOneOSFiles!!.addAll(files)
            } else {
                deviceDisplayModel.mOneOSFiles = arraySetOf<OneOSFile>().apply {
                    addAll(files)
                }
            }
        }
        if ((deviceDisplayModel.mRefOSFileView != null
                        && deviceDisplayModel.mRefOSFileView?.get() != null)) {
            if (isLoadMore) {
                deviceDisplayModel.mRefOSFileView?.get()?.onLoadMore(files)
            } else {
                deviceDisplayModel.mRefOSFileView?.get()?.onRefresh(false, files)
            }
        }
    }

    private fun getLoginSessionResultObservable(devId: String, deviceDisplayModel: DeviceDisplayModel): Observable<Result<*>> {
        return Observable.create<Result<*>>(object : ObservableOnSubscribe<Result<*>?> {
            @Throws(Exception::class)
            override fun subscribe(emitter: ObservableEmitter<Result<*>?>) {
                val loginSession = deviceDisplayModel.mLoginSession
                if (loginSession == null || !loginSession.isLogin) {
                    SessionManager.getInstance().getLoginSession(devId, object : GetSessionListener(false) {
                        override fun onSuccess(url: String, data: LoginSession) {
                            deviceDisplayModel.mLoginSession = data
                            emitter.onNext(Result(data))
                        }

                        override fun onFailure(url: String, errorNo: Int, errorMsg: String) {
                            super.onFailure(url, errorNo, errorMsg)
                            emitter.onNext(Result<Any>(errorNo, errorMsg))
                        }
                    })
                } else {
                    emitter.onNext(Result(loginSession))
                }
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
    }

    override fun searchFiles(devId: String, fileType: OneOSFileType, searchFilter: String) {
        val deviceDisplayModel = mModelHashMap[devId]
        deviceDisplayModel?.let {
            addDisposable(getLoginSessionResultObservable(devId, it)
                    .subscribe(object : Consumer<Result<*>> {
                        @Throws(Exception::class)
                        override fun accept(result: Result<*>) {
                            if (result.isSuccess) {
                                val loginSession = result.data as LoginSession
                                val listener = object : OnSearchFileListener {
                                    override fun onStart(url: String) {}
                                    override fun onSuccess(url: String, files: ArrayList<OneOSFile>) {
                                        if ((deviceDisplayModel.mRefOSFileView != null
                                                        && deviceDisplayModel.mRefOSFileView?.get() != null)) {
                                            deviceDisplayModel.mRefOSFileView?.get()!!.onRefresh(true, files)
                                        }
                                        deviceDisplayModel.isSearch = true
                                    }

                                    override fun onFailure(url: String, errorNo: Int, errorMsg: String) {
                                        dispatchError(errorNo, errorMsg, deviceDisplayModel)
                                    }
                                }
                                val searchAPI = OneOSSearchAPI(loginSession)
                                val params = JSONObject()
                                var share_path_type = SharePathType.USER.type//默认为个人
                                val nasV3 = SessionCache.instance.isNasV3(devId)
                                val ftype = JSONArray()
                                when (fileType) {
                                    OneOSFileType.PRIVATE -> {
                                        share_path_type = SharePathType.USER.type
                                        ftype.put("all")
                                    }
                                    OneOSFileType.PUBLIC -> {
                                        share_path_type = SharePathType.PUBLIC.type
                                        ftype.put("all")
                                    }
                                    OneOSFileType.RECYCLE -> {
                                        ftype.put("all")
                                        params.put("path", "/.recycle/")
                                    }
                                    OneOSFileType.DOCUMENTS -> {
                                        ftype.put("txt")
                                        ftype.put("doc")
                                        ftype.put("xls")
                                        ftype.put("ppt")
                                        ftype.put("pdf")
                                    }
                                    else -> {
                                        ftype.put(fileType.serverTypeName)
                                    }
                                }
                                //搜索关键字
                                params.put("pattern", searchFilter)
                                params.put("ftype", ftype)
                                val pathType = if (nasV3 && OneOSFileType.isDB(fileType)) {
                                    val pathTypes = JSONArray().put(SharePathType.USER.type).put(SharePathType.PUBLIC.type)
                                    pathTypes
                                } else {
                                    share_path_type
                                }
                                params.put("share_path_type", pathType)
                                val observer = object : V5Observer<Any>(loginSession.id ?: "") {
                                    override fun success(result: BaseProtocol<Any>) {
                                        listener.onSuccess("", searchAPI.getOneOSFiles(Gson().toJson(result.data)))
                                    }

                                    override fun fail(result: BaseProtocol<Any>) {
                                        listener.onFailure("", result.error?.code
                                                ?: 0, result.error?.msg ?: "")
                                    }

                                    override fun isNotV5() {
                                        searchAPI.setOnFileListListener(listener)
                                        searchAPI.search(fileType, searchFilter)
                                    }

                                }
                                V5Repository.INSTANCE().searchFile(loginSession.id
                                        ?: "", loginSession.ip, LoginTokenUtil.getToken(), params, observer)
//                                }
                            } else {
                                dispatchError(result.code, result.msg, deviceDisplayModel)
                            }
                        }
                    }))
        }
    }

    /**
     * 返回全路径，带 public
     */
    fun getPath(devId: String?): String? {
        val deviceDisplayModel = mModelHashMap[devId]
        var path: String?
        if (deviceDisplayModel != null
                && OneOSFileType.isDir(deviceDisplayModel.mFileType)
                && devId != null) {
            path = deviceDisplayModel.curPath
            if (SessionCache.instance.isV5(devId)) {
                val type = PathTypeCompat.getSharePathType(deviceDisplayModel.mFileType).type
                path = PathTypeCompat.getAllStrPath(type, path)
            }
        } else {
            path = OneOSAPIs.ONE_OS_PRIVATE_ROOT_DIR
        }
        return path
    }

    override fun loadLocalData(devId: String, curPath: String, orderTypeV2: FileOrderTypeV2) {
        val deviceDisplayModel = mModelHashMap.get(devId) ?: return
        addDisposable(getLoginSessionResultObservable(devId, deviceDisplayModel)
                .subscribe({ result ->
                    if (!result.isSuccess || ((curPath == deviceDisplayModel.curPath))) {
                        val loginSession = LoginSession(devId)
                        val path = curPath
                        val fileType = deviceDisplayModel.mFileType
                        val observer = Observer<Resource<BaseResultModel<FileListModel>>> { resource: Resource<BaseResultModel<FileListModel>?> ->
                            if (resource.status === Status.SUCCESS) {
                                val data: BaseResultModel<FileListModel>? = resource.data
                                if (data != null) {
                                    if (data.isSuccess) {
                                        dispatchResult(path, data.data.total, data.data.pages,
                                                data.data.page, data.data.files, deviceDisplayModel, fileType, false)
                                    } else {

                                        dispatchError(data.error?.code, data.error?.msg, deviceDisplayModel)
                                    }
                                }
                            } else if (resource.status === Status.ERROR) {
                                dispatchError(HttpErrorNo.UNKNOWN_EXCEPTION, resource.message, deviceDisplayModel)
                            }
                        }
                        val page = deviceDisplayModel.mPage
                        if (OneOSFileType.isDir(fileType)) {
                            mNasRepository.loadFilesFromServerDir(devId, loginSession.session,
                                    fileType, path, page, false, loginSession.isV5, orderTypeV2 = orderTypeV2)
                                    .observe(deviceDisplayModel, observer)
                        } else {
                            mNasRepository.loadFilesFromServerDB(devId, loginSession.session,
                                    fileType, path, page, orderTypeV2 = orderTypeV2)
                                    .observe(deviceDisplayModel, observer)
                        }
                    }
                }, { t -> Timber.e(t) }))
    }

    private fun dispatchError(errorNo: Int?, errorMsg: String?, deviceDisplayModel: DeviceDisplayModel?) {
        if (deviceDisplayModel!!.mRefOSFileView != null && deviceDisplayModel.mRefOSFileView?.get() != null) {
            val errNo = errorNo ?: ERR_ONE_REQUEST
            val resultMsg = HttpErrorNo.getResultMsg(true, errNo, errorMsg)
            val oneOSFileView = deviceDisplayModel.mRefOSFileView?.get()
            oneOSFileView?.onLoadFailed(resultMsg, errNo)
        }
        val deviceId = deviceDisplayModel.deviceId
        if ((errorNo == HttpErrorNo.ERR_ONE_NO_LOGIN
                        || errorNo == V5_ERR_DENIED_PERMISSION) && !deviceId.isNullOrEmpty()) {
            SessionManager.getInstance().removeSession(deviceId)
        }
    }

    override fun onCleared() {
        super.onCleared()
        for (stringDeviceDisplayModelEntry: Map.Entry<String, DeviceDisplayModel> in mModelHashMap.entries) {
            val displayModel: DeviceDisplayModel = stringDeviceDisplayModelEntry.value
            displayModel.let { it.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY) }
        }
        dispose()
    }

    fun searchFilesMore(devId: String) {

    }

    class DeviceDisplayModel(var deviceId: String) : LifecycleOwner {
        var isChangedType: Boolean = false
        private val mLifecycleRegistry: LifecycleRegistry = LifecycleRegistry(this)
        var deviceName: String? = null
        var curPath: String? = null
        var lastRefreshTime: Long = 0
        var mFileType = OneOSFileType.PRIVATE
            set(value) {
                if (mode == DisplayMode.ALL) {
                    if (value == OneOSFileType.PUBLIC) {
                        mode = DisplayMode.PUBLIC
                    }
                }
                if (field != value) {
                    initModel()
                }
                field = value
            }

        private fun initModel() {
            mPage = 0
            mPages = 0
            total = 0
            mOneOSFiles = null
            isSearch = false
        }

        var mPage = 0
        var mPages = 0
        var total = 0
        var mode = DisplayMode.ALL
        var orderType: FileOrderType? = null
        var mViewerType: FileViewerType? = null
        var mLoginSession: LoginSession? = null
            set(value) {
                if (value != null) {
                    field = value
                }
            }
        var mRefOSFileView: WeakReference<FilesContract.View<OneOSFile>>? = null
        var isSearch = false
        var mOneOSFiles: ArraySet<OneOSFile>? = null
        override fun getLifecycle(): Lifecycle {
            return mLifecycleRegistry
        }

        fun handleLifecycleEvent(event: Lifecycle.Event?) {
            event?.let {
                mLifecycleRegistry.handleLifecycleEvent(it)
            }
        }

    }

}