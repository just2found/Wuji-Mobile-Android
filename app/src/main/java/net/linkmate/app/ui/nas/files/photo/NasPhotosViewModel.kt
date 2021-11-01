package net.linkmate.app.ui.nas.files.photo

import android.app.Application
import android.util.Log
import android.widget.ImageView
import androidx.annotation.Keep
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map
import com.bumptech.glide.Glide
import io.weline.repo.files.data.DataPhotosTimelineYearSummary
import libs.source.common.AppExecutors
import libs.source.common.livedata.Resource
import libs.source.common.livedata.Status
import net.linkmate.app.R
import net.linkmate.app.ui.nas.NasAndroidViewModel
import net.linkmate.app.ui.nas.images.IPhotosViewModel
import net.linkmate.app.ui.nas.images.ImageViewType
import net.linkmate.app.ui.nas.images.OneFileModel
import net.linkmate.app.ui.nas.images.OneFilePagesModel
import net.linkmate.app.ui.viewmodel.GenFileUrl
import net.linkmate.app.util.ToastUtils
import net.sdvn.cmapi.CMAPI
import net.sdvn.nascommon.SessionManager
import net.sdvn.nascommon.fileserver.constants.SharePathType
import net.sdvn.nascommon.iface.GetSessionListener
import net.sdvn.nascommon.model.oneos.BaseResultModel
import net.sdvn.nascommon.model.oneos.comp.OneOSFileCTTimeComparator
import net.sdvn.nascommon.model.oneos.user.LoginSession
import net.sdvn.nascommon.model.oneos.vo.FileListModel
import net.sdvn.nascommon.repository.NasRepository
import net.sdvn.nascommon.utils.FileUtils

/**
 * @author Raleigh.Luo
 * date：21/5/10 11
 * describe：
 */
class NasPhotosViewModel @Keep constructor(val app: Application, val mDevId: String) : NasAndroidViewModel(app, mDevId), IPhotosViewModel<OneFileModel> {

    private var mLoginSession: LoginSession? = null
    private var year: Long? = null
    private val mNasRepository: NasRepository = NasRepository(SessionManager.getInstance().userId, AppExecutors.instance)

    // 月 日
    private val _liveData = MediatorLiveData<Resource<List<OneFileModel>>>()
    val liveData: LiveData<Resource<List<OneFileModel>>> = _liveData

    //   汇总 年
    private val _liveDataSummary = MediatorLiveData<Resource<List<OneFileModel>>>()
    val liveDataSummary: LiveData<Resource<List<OneFileModel>>> = _liveDataSummary

    //视图类型
    private val _viewTypeLiveData = MutableLiveData<ImageViewType>(ImageViewType.DAY)
    val viewType: LiveData<ImageViewType> = _viewTypeLiveData

    fun selectType(type: ImageViewType) {
        _viewTypeLiveData.postValue(type)
    }

    fun loadImage(imageView: ImageView, item: OneFileModel) {
        loadImage(imageView, item.devId, item.getPathType(), item.getPath())
    }

    fun loadImage(imageView: ImageView, devId: String, pathType: Int, path: String) {
        val model: Any? = GenFileUrl.getGlideModeTb(devId, pathType, path)
        Glide.with(imageView.context)
                .asBitmap()
                .centerCrop()
                .load(model)
                .placeholder(R.drawable.image_placeholder)
                .error(R.drawable.icon_device_img)
                .into(imageView)
    }


    private val mFileListModel: OneFilePagesModel<OneFileModel> = OneFilePagesModel()

    private var source: LiveData<Resource<BaseResultModel<FileListModel>>>? = null

    /**
     * 防止并发请求或未回调时，造成_liveData多数据源回调addSource
     */
    @Volatile
    private var currentRequestTime: Long = 0

    /**
     * 首次加载数据
     */
    fun loadPhotosTimeline(devId: String, viewType: ImageViewType,
                           vararg type: SharePathType = arrayOf(SharePathType.USER, SharePathType.PUBLIC),
                           page: Int = 0, year: Long? = null) {
        val requestTime = System.currentTimeMillis()
        currentRequestTime = requestTime
        SessionManager.getInstance().getLoginSession(devId, object : GetSessionListener() {
            override fun onSuccess(url: String?, loginSession: LoginSession) {
                if (currentRequestTime != requestTime) {//防止并发请求或未回调时，造成_liveData多数据源回调addSource
                    return
                }
                mLoginSession = loginSession
                if (source != null) _liveData.removeSource(source!!)
                source = mNasRepository.loadPhotosTimeline(devId, loginSession.session, *type, page = page, year = year)
                _liveData.addSource(source!!) { resource ->
                    when (resource.status) {
                        Status.SUCCESS -> {
                            val data = resource.data
                            if (data?.isSuccess == true) {
                                val fmt = getTimeFmt(viewType)
                                val newFiles = mutableListOf<OneFileModel>()
                                data.data?.let { model ->
                                    mFileListModel.pages = model.pages
                                    mFileListModel.total = model.total
                                    mFileListModel.page = model.page
                                    model.files?.sortedWith(OneOSFileCTTimeComparator())
                                            ?.forEach { file ->
                                                val letter = FileUtils.formatTime(file.cttime * 1000, fmt)
                                                if (!mFileListModel.mSectionLetters.contains(letter)) {
                                                    mFileListModel.mSectionLetters.add(letter)
                                                    file.section = mFileListModel.index
                                                    mFileListModel.index++
                                                } else {
                                                    file.section = mFileListModel.mSectionLetters.indexOf(letter)
                                                }
                                                newFiles.add(OneFileModel(viewType.ordinal, 0,
                                                        "", ""
                                                ).apply {
                                                    this.devId = devId
                                                    this.share_path_type = file.share_path_type
                                                    this.setPath(file.getPath())
                                                    this.setName(file.getName())
                                                    this.setSize(file.getSize())
                                                    this.setTime(file.getTime())
                                                    this.cttime = file.cttime
                                                })
                                            }
                                }
//                                mFileListModel.files.addAll(newFiles)
                                _liveData.postValue(Resource.success(newFiles))

                            } else {
                                _liveData.postValue(Resource.error(data?.error?.msg
                                        ?: resource.message, null, data?.error?.code
                                        ?: resource.code))
                            }
                        }
                        Status.ERROR -> {
                            _liveData.postValue(Resource.error(resource.message, null, resource.code))
                        }
                        Status.LOADING -> {
                            _liveData.postValue(Resource.loading())
                        }
                    }
                }
            }

            override fun onFailure(url: String?, errorNo: Int, errorMsg: String?) {
                super.onFailure(url, errorNo, errorMsg)
                _liveData.postValue(Resource.error(errorMsg, null, errorNo))
            }
        })

    }

    private fun getTimeFmt(viewType: ImageViewType): String {
        return app.getString(if (viewType == ImageViewType.MONTH) {
            R.string.fmt_time_line
        } else {
            R.string.fmt_time_adapter_title2
        })
    }

    private val yearStr: String by lazy {
        getApplication<Application>().getString(R.string.year)
    }


    fun loadPhotosTimelineSummary(devId: String, vararg type: SharePathType = arrayOf(SharePathType.USER, SharePathType.PUBLIC)) {
        SessionManager.getInstance().getLoginSession(devId, object : GetSessionListener() {
            override fun onSuccess(url: String?, loginSession: LoginSession) {
                mLoginSession = loginSession
                _liveDataSummary.addSource(mNasRepository.loadPhotosTimelineSummary(devId, loginSession.session, *type)) { resource ->
                    when (resource.status) {
                        Status.SUCCESS -> {
                            val data = resource.data
                            if (data?.isSuccess == true) {
                                _liveDataSummary.postValue(Resource.success(data.data?.sortedByDescending {
                                    it.year
                                }?.map<DataPhotosTimelineYearSummary, OneFileModel> {
                                    OneFileModel(ImageViewType.YEAR.ordinal, 0,
                                            if (it.year > 0) {
                                                "${it.year}$yearStr"
                                            } else {
                                                app.getString(R.string.unknown)
                                            }, it.count.toString()
                                    ).apply {
                                        this.devId = devId
                                        this.share_path_type = it.osFile.share_path_type
                                        this.setPath(it.osFile.getPath())
                                        this.setName(it.osFile.getName())
                                        this.setSize(it.osFile.getSize())
                                        this.setTime(it.osFile.getTime())
                                        this.cttime = it.osFile.cttime
                                    }
                                }))
                            } else {
                                _liveDataSummary.postValue(Resource.error(data?.error?.msg
                                        ?: resource.message, null, data?.error?.code
                                        ?: resource.code))
                            }
                        }
                        Status.ERROR -> {
                            _liveDataSummary.postValue(Resource.error(resource.message, null, resource.code))
                        }
                        Status.LOADING -> {
                            _liveDataSummary.postValue(Resource.loading())
                        }
                    }

                }
            }

            override fun onFailure(url: String?, errorNo: Int, errorMsg: String?) {
                super.onFailure(url, errorNo, errorMsg)
                _liveDataSummary.postValue(Resource.error(errorMsg, null, errorNo))
            }
        })
    }


    fun loadImageMore(devId: String, viewType: ImageViewType, year: Long? = null) {
        loadPhotosTimeline(devId, viewType, page = mFileListModel.nextPage(), year = year)
    }

    override val liveDataPicFiles: LiveData<Resource<List<OneFileModel>>> = liveData.map { resource ->
        when (resource.status) {
            Status.SUCCESS -> {
                val map = resource.data
                Resource.success(map)
            }
            Status.ERROR -> {
                Resource.error(resource.message, null, resource.code)
            }
            Status.LOADING -> {
                Resource.loading(null)
            }
            else -> {
                Resource.loading(null)
            }
        }
    }

    override fun getPagesPicModel(): OneFilePagesModel<OneFileModel> {
        return OneFilePagesModel<OneFileModel>().apply {
            total = mFileListModel.total
            page = mFileListModel.page
            pages = mFileListModel.pages
            files = mFileListModel.files
                    .toMutableList()

        }
    }


    fun getPagesModel(): OneFilePagesModel<OneFileModel> {
        return mFileListModel
    }

    override fun loadImageMore() {
        loadImageMore(mDevId, viewType.value!!, year)
    }


    fun getScreenHeight(): Int {
        return getApplication<Application>().resources.displayMetrics.heightPixels
    }

    fun getScreenWidth(): Int {
        return getApplication<Application>().resources.displayMetrics.widthPixels
    }

    override fun getDeviceId(): String {
        return devId;
    }

    fun getLoginSession(): LoginSession? {
        return mLoginSession
    }

}