package net.linkmate.app.ui.nas.search

import android.app.Application
import androidx.annotation.Keep
import androidx.lifecycle.*
import io.weline.repo.api.V5_ERR_ERROR_PARAMS
import libs.source.common.AppExecutors
import libs.source.common.livedata.Resource
import libs.source.common.livedata.Status
import net.linkmate.app.R
import net.linkmate.app.ui.nas.NasAndroidViewModel
import net.linkmate.app.ui.nas.images.IPhotosViewModel
import net.linkmate.app.ui.nas.images.OneFilePagesModel
import net.sdvn.common.repo.AccountRepo
import net.sdvn.nascommon.SessionManager
import net.sdvn.nascommon.iface.GetSessionListener
import net.sdvn.nascommon.model.FileOrderTypeV2
import net.sdvn.nascommon.model.oneos.OneOSFile
import net.sdvn.nascommon.model.oneos.OneOSFileType
import net.sdvn.nascommon.model.oneos.comp.OneOSFileCTTimeComparator
import net.sdvn.nascommon.model.oneos.comp.OneOSFileTimeComparatorV2
import net.sdvn.nascommon.model.oneos.user.LoginSession
import net.sdvn.nascommon.repository.NasRepository
import timber.log.Timber

/**
 *
 * @Description:
 * @Author: todo2088
 * @CreateDate: 2021/3/6 15:38
 */
class FileSearchViewModel @Keep constructor(val app: Application, val mDevId: String) : NasAndroidViewModel(app, mDevId), IPhotosViewModel<OneOSFile>, LifecycleOwner {

    private var isSearch: Boolean = true
    private val nasRepository = NasRepository(AccountRepo.getUserId(), AppExecutors.instance)
    private var lastSearchFilter: String? = null
    private var lastFileType: OneOSFileType? = null
    private var lastPathTypes: IntArray? = null
    private var lastPath: String? = null
    private val _searchLiveData = MutableLiveData<Resource<List<OneOSFile>>>()
    val searchLiveData: LiveData<Resource<List<OneOSFile>>> = _searchLiveData
    var mGroupId :Long?=null
    private var mLifecycleRegistry: LifecycleRegistry = LifecycleRegistry(this)

    init {
        mLifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
    }

    fun searchFiles(devId: String, fileType: OneOSFileType, searchFilter: String,
                    pathType: IntArray, path: String? = null, page: Int = 0,
                    orderTypeV2: FileOrderTypeV2 = FileOrderTypeV2.time_desc)
            : LiveData<Resource<List<OneOSFile>>> {
        val mediatorLiveData = MediatorLiveData<Resource<List<OneOSFile>>>()
        SessionManager.getInstance().getLoginSession(devId, object : GetSessionListener() {
            override fun onSuccess(url: String?, loginSession: LoginSession) {
                val searchFile = when (fileType) {
                    OneOSFileType.FAVORITES -> {
                        loginSession.userInfo?.favoriteId?.let {
                            nasRepository.tagFiles(devId, loginSession.session, it, path,
                                    fileType = NasRepository.getFilterTypeByFileType(fileType),
                                    isV5 = true, pattern = searchFilter, page = page, orderTypeV2 = orderTypeV2,groupId= mGroupId)
                        } ?: liveData { Resource.error("Unknow", null, V5_ERR_ERROR_PARAMS) }
                    }
                    else -> {

                        nasRepository.searchFile(devId, loginSession.session, path, fileType = NasRepository.getFilterTypeByFileType(fileType),
                                sharePathType = pathType.toList(), isV5 = true, pattern = searchFilter, page = page, orderTypeV2 = orderTypeV2,groupId= mGroupId)
                    }
                }

                mediatorLiveData.addSource(searchFile) {
                    when (it.status) {
                        Status.SUCCESS -> {
                            isSearch = true
                            lastSearchFilter = searchFilter
                            lastFileType = fileType
                            lastPath = path
                            lastPathTypes = pathType
                            if (it.data?.isSuccess == true) {
                                val newFiles = mutableListOf<OneOSFile>()
                                it?.data?.data?.let { model ->
                                    mFileListModel.pages = model.pages
                                    mFileListModel.total = model.total
                                    mFileListModel.page = model.page
                                    model.files?.takeUnless { it.isNullOrEmpty() }
                                            ?.sortedWith(OneOSFileCTTimeComparator())
                                            ?.forEach { file ->
                                                newFiles.add(file)
                                            }
                                }
                                mFileListModel.files.addAll(newFiles)
                                _searchLiveData.postValue(Resource.success(newFiles))
                                mediatorLiveData.postValue(Resource.success(newFiles))

                            } else if (it.data?.error != null) {
                                _searchLiveData.postValue(Resource.error(it.data?.error?.msg, null, it.data?.error?.code))
                                mediatorLiveData.postValue(Resource.error(it.data?.error?.msg, null, it.data?.error?.code))
                            } else {
                                _searchLiveData.postValue(Resource.error(it.message, null, it.code))
                                mediatorLiveData.postValue(Resource.error(it.message, null, it.code))
                            }

                        }
                        Status.ERROR -> {
                            mediatorLiveData.postValue(Resource.error(it.message, null, it.code))
                        }
                        Status.LOADING -> {
                            mediatorLiveData.postValue(Resource.loading())
                        }
                    }
                }


            }

            override fun onFailure(url: String?, errorNo: Int, errorMsg: String?) {
                super.onFailure(url, errorNo, errorMsg)
                mediatorLiveData.postValue(Resource.error(errorMsg, null, errorNo))
            }
        })

        return mediatorLiveData
    }

    private fun getTimeFmt(fileType: OneOSFileType): String {
        return app.getString(when (fileType) {
            OneOSFileType.PICTURE -> {
                R.string.fmt_time_line
            }
            else -> {
                R.string.fmt_time_adapter_title2
            }
        })
    }

    override val liveDataPicFiles: LiveData<Resource<List<OneOSFile>>> = searchLiveData.map { resource ->
        when (resource.status) {
            Status.SUCCESS -> {
                val map = resource.data?.filter { it.isPicture }
                Resource.success(map)
            }
            Status.ERROR -> {
                Resource.error(resource.message, null, resource.code)
            }
            else -> {
                Resource.loading(null)
            }
        }
    }

    override fun getDeviceId(): String {
        return mDevId
    }

    private val mFileListModel: OneFilePagesModel<OneOSFile> = OneFilePagesModel()
    fun getPagesModel(): OneFilePagesModel<OneOSFile> {
        return mFileListModel
    }

    override fun getPagesPicModel(): OneFilePagesModel<OneOSFile> {
        return OneFilePagesModel<OneOSFile>().apply {
            total = mFileListModel.total
            page = mFileListModel.page
            pages = mFileListModel.pages
            files = mFileListModel.files.filter { it.isPicture }
                    .toMutableList()

        }
    }

    override fun loadImageMore() {
        loadMore(isSearch)?.observe(this, Observer {

        })
    }

    fun openDir(devId: String, path: String, fileType: OneOSFileType, pathType: IntArray, page: Int = 0): LiveData<Resource<List<OneOSFile>>> {
        val mediatorLiveData = MediatorLiveData<Resource<List<OneOSFile>>>()
        SessionManager.getInstance().getLoginSession(devId, object : GetSessionListener() {
            override fun onSuccess(url: String?, loginSession: LoginSession) {
                mediatorLiveData.addSource(nasRepository.loadFilesFromServer(devId, loginSession.session,
                        fileType, path, filter = fileType, page = page, pathType = pathType,groupId = mGroupId)) {
                    when (it.status) {
                        Status.SUCCESS -> {
                            isSearch = false
                            lastFileType = fileType
                            lastPath = path
                            lastPathTypes = pathType
                            if (it.data?.isSuccess == true) {
                                val newFiles = mutableListOf<OneOSFile>()
                                it?.data?.data?.let { model ->
                                    mFileListModel.pages = model.pages
                                    mFileListModel.total = model.total
                                    mFileListModel.page = model.page
                                    model.files?.sortedWith(OneOSFileTimeComparatorV2(false))
                                            ?.let { files ->
                                                newFiles.addAll(files)
                                            }
                                }
                                mFileListModel.files.addAll(newFiles)
                                _searchLiveData.postValue(Resource.success(newFiles))
                                mediatorLiveData.postValue(Resource.success(newFiles))

                            } else {
                                _searchLiveData.postValue(Resource.error(it.message, null, it.code))
                            }

                        }
                        Status.ERROR -> {
                            mediatorLiveData.postValue(Resource.error(it.message, null, it.code))
                        }
                        Status.LOADING -> {
                            mediatorLiveData.postValue(Resource.loading())
                        }
                    }
                }


            }

            override fun onFailure(url: String?, errorNo: Int, errorMsg: String?) {
                super.onFailure(url, errorNo, errorMsg)
                mediatorLiveData.postValue(Resource.error(errorMsg, null, errorNo))
            }
        })

        return mediatorLiveData
    }

    fun loadMore(isSearch: Boolean): LiveData<Resource<List<OneOSFile>>>? {
        return if (lastFileType != null && lastSearchFilter != null && lastPathTypes != null) {
            if (isSearch) {
                searchFiles(devId, lastFileType!!, lastSearchFilter!!, lastPathTypes!!, lastPath, mFileListModel.nextPage())
            } else {
                openDir(devId, lastPath!!, lastFileType!!, lastPathTypes!!, mFileListModel.nextPage())
            }
        } else {
            Timber.d("lastFileType = $lastFileType \n lastSearchFilter $lastSearchFilter \n lastPathTypes =$lastPathTypes")
            null
        }
    }

    override fun getLifecycle(): Lifecycle {
        return mLifecycleRegistry
    }

    override fun onCleared() {
        super.onCleared()
        mLifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
        mLifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    }

    fun indexOfItem(item: OneOSFile?): Int {
        return getPagesPicModel().files.indexOf(item)
    }
}