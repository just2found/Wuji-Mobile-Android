package net.linkmate.app.ui.nas.favorites

import android.app.Application
import androidx.annotation.Keep
import androidx.lifecycle.*
import libs.source.common.AppExecutors
import libs.source.common.livedata.Resource
import libs.source.common.livedata.Status
import net.linkmate.app.R
import net.linkmate.app.ui.nas.NasAndroidViewModel
import net.linkmate.app.ui.nas.helper.FileSortHelper
import net.linkmate.app.ui.nas.images.IPhotosViewModel
import net.linkmate.app.ui.nas.images.OneFilePagesModel
import net.sdvn.common.repo.AccountRepo
import net.sdvn.nascommon.SessionManager
import net.sdvn.nascommon.iface.GetSessionListener
import net.sdvn.nascommon.model.FileOrderTypeV2
import net.sdvn.nascommon.model.oneos.OneOSFile
import net.sdvn.nascommon.model.oneos.OneOSFileType
import net.sdvn.nascommon.model.oneos.user.LoginSession
import net.sdvn.nascommon.repository.NasRepository
import timber.log.Timber

/**
 *
 * @Description:
 * @Author: todo2088
 * @CreateDate: 2021/3/6 15:38
 */
class FileFavoritesViewModel @Keep constructor(val app: Application, val mDevId: String) : NasAndroidViewModel(app, mDevId), IPhotosViewModel<OneOSFile>, LifecycleOwner {

    private var isSearch: Boolean = true
    private val nasRepository = NasRepository(AccountRepo.getUserId(), AppExecutors.instance)
    private var lastSearchFilter: String? = null
    private var lastFileType: OneOSFileType? = null
    private var lastPathTypes: IntArray? = null
    private var lastPath: String? = null
    private var lastTagId: Int? = null
    private val _liveData = MutableLiveData<Resource<List<OneOSFile>>>()
    val liveData: LiveData<Resource<List<OneOSFile>>> = _liveData
    private var mLifecycleRegistry: LifecycleRegistry = LifecycleRegistry(this)

    init {
        mLifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
    }

    fun loadFiles(devId: String, tagId: Int, fileType: OneOSFileType, searchFilter: String? = null,
                  path: String? = null, page: Int = 0, orderTypeV2: FileOrderTypeV2 = FileOrderTypeV2.time_desc)
            : LiveData<Resource<List<OneOSFile>>> {
        val mediatorLiveData = MediatorLiveData<Resource<List<OneOSFile>>>()
        SessionManager.getInstance().getLoginSession(devId, object : GetSessionListener() {
            override fun onSuccess(url: String?, loginSession: LoginSession) {
                mediatorLiveData.addSource(nasRepository.tagFiles(devId, loginSession.session, tagId, path,
                        fileType = NasRepository.getFilterTypeByFileType(fileType),
                        isV5 = loginSession.isV5, pattern = searchFilter, page = page, orderTypeV2 = orderTypeV2)
                ) {
                    when (it.status) {
                        Status.SUCCESS -> {
                            isSearch = !searchFilter.isNullOrEmpty()
                            lastSearchFilter = searchFilter
                            lastFileType = fileType
                            lastPath = path
                            lastTagId = tagId
                            val data = it.data
                            if (data != null) {
                                if (data.isSuccess) {
                                    val newFiles = mutableListOf<OneOSFile>()
                                    it?.data?.data?.let { model ->
                                        mFileListModel.pages = model.pages
                                        mFileListModel.total = model.total
                                        mFileListModel.page = model.page
                                        model.files?.takeUnless { it.isNullOrEmpty() }
                                                ?.also {
                                                    FileSortHelper.sortWith(fileType, orderTypeV2, it)
                                                }
                                                ?.forEach { file ->
                                                    newFiles.add(file)
                                                }
                                    }
                                    mFileListModel.files.addAll(newFiles)
                                    _liveData.postValue(Resource.success(newFiles))
                                    mediatorLiveData.postValue(Resource.success(newFiles))
                                } else {
                                    _liveData.postValue(Resource.error(data.error?.msg, null, data.error?.code))
                                    mediatorLiveData.postValue(Resource.error(data.error?.msg, null, data.error?.code))
                                }
                            } else {
                                _liveData.postValue(Resource.error(it.message, null, it.code))
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

    override val liveDataPicFiles: LiveData<Resource<List<OneOSFile>>> = liveData.map { resource ->
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

    fun openDir(devId: String, path: String, fileType: OneOSFileType, pathType: IntArray,
                page: Int = 0, orderTypeV2: FileOrderTypeV2 = FileOrderTypeV2.time_desc): LiveData<Resource<List<OneOSFile>>> {
        val mediatorLiveData = MediatorLiveData<Resource<List<OneOSFile>>>()
        SessionManager.getInstance().getLoginSession(devId, object : GetSessionListener() {
            override fun onSuccess(url: String?, loginSession: LoginSession) {
                mediatorLiveData.addSource(nasRepository.loadFilesFromServer(devId, loginSession.session,
                        fileType, path, filter = fileType, page = page
                        , pathType = pathType, orderTypeV2 = orderTypeV2)) {
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
                                    model.files?.let { files ->
                                        FileSortHelper.sortWith(fileType, orderTypeV2, files)
                                        newFiles.addAll(files)
                                    }
                                }
                                mFileListModel.files.addAll(newFiles)
                                _liveData.postValue(Resource.success(newFiles))
                                mediatorLiveData.postValue(Resource.success(newFiles))

                            } else {
                                _liveData.postValue(Resource.error(it.message, null, it.code))
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
        return if (lastTagId != null && lastFileType != null) {
            if (isSearch) {
                loadFiles(devId, lastTagId!!, lastFileType!!, lastSearchFilter, lastPath, mFileListModel.nextPage())
            } else if (lastPathTypes != null) {
                openDir(devId, lastPath!!, lastFileType!!, lastPathTypes!!, mFileListModel.nextPage())
            } else {
                null
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
