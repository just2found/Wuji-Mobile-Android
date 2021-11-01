package net.linkmate.app.ui.nas.upload

import android.app.Application
import android.text.TextUtils
import android.view.View
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.async
import libs.source.common.livedata.Resource
import net.sdvn.nascommon.SessionManager
import net.sdvn.nascommon.model.FileListChangeObserver
import net.sdvn.nascommon.model.FileManageAction
import net.sdvn.nascommon.model.PathTypeCompat
import net.sdvn.nascommon.model.oneos.DataFile
import net.sdvn.nascommon.model.phone.LocalFile
import net.sdvn.nascommon.model.phone.LocalFileManage
import net.sdvn.nascommon.model.phone.LocalFileType
import net.sdvn.nascommon.model.phone.LocalSortTask
import net.sdvn.nascommon.utils.SDCardUtils
import timber.log.Timber
import java.io.File
import java.util.*

/**
 * Description:
 * @author  admin
 * CreateDate: 2021/5/20
 */

class LocalFileViewModel(val app: Application) : AndroidViewModel(app) {
    private val mSDCardList: MutableList<File> = mutableListOf()

    init {
        viewModelScope.async {
            val sdCardList = SDCardUtils.getSDCardList()
            if (sdCardList != null) {
                mSDCardList.clear()
                mSDCardList.addAll(sdCardList)
            }
        }.getCompletionExceptionOrNull()
    }

    private val _localFilesLiveData = MutableLiveData<Resource<List<LocalFile>>>()
    private val mFileList = ArrayList<LocalFile>()
    private val mSelectedList: ArrayList<DataFile> = ArrayList()

    fun getFileList(mFileType: LocalFileType, mSearchFilter: String? = null, dir: File? = null): LiveData<Resource<List<LocalFile>>> {
        mFileList.clear()
        if (!TextUtils.isEmpty(mSearchFilter)) {
            if (mFileType == LocalFileType.PRIVATE) {
                if (dir != null) {
                    val fileList = ArrayList<LocalFile>()
                    searchDownloadDir(mSearchFilter, fileList, dir)
                    mFileList.addAll(fileList)
                }
                _localFilesLiveData.postValue(Resource.success(mFileList))
            } else {
                val task = LocalSortTask(app, null, mFileType, mSearchFilter, object : LocalSortTask.onLocalSortListener {
                    override fun onStart(type: LocalFileType) {}

                    override fun onComplete(type: LocalFileType, fileList: List<LocalFile>, sectionList: List<String>) {
                        mFileList.addAll(fileList)
                        _localFilesLiveData.postValue(Resource.success(mFileList))
                    }
                })
                task.execute(0)
            }
        } else {
            if (mFileType == LocalFileType.PRIVATE) {
                if (dir == null) {
                    for (f in mSDCardList) {
                        mFileList.add(LocalFile(f))
                    }
                } else {
                    val files = dir.listFiles { f -> !f.isHidden }
                    if (null != files) {

                        val downloadPath = SessionManager.getInstance().defaultDownloadPath
                        for (f in files) {
                            val file = LocalFile(f)
                            file.isDownloadDir = (downloadPath != null && f.absolutePath.startsWith(downloadPath))
                            mFileList.add(file)
                        }
                    }
                }
                _localFilesLiveData.postValue(Resource.success(mFileList))
            } else {
                Timber.d("---------File type: $mFileType")
                val task = LocalSortTask(app, null, mFileType, mSearchFilter, object : LocalSortTask.onLocalSortListener {
                    override fun onStart(type: LocalFileType) {
                        //                baseActivity.showLoading();
                    }

                    override fun onComplete(type: LocalFileType, fileList: List<LocalFile>, sectionList: List<String>) {
                        Timber.d("onComplete: fileList ================ $fileList")
                        mFileList.clear()
                        mFileList.addAll(fileList)
                        _localFilesLiveData.postValue(Resource.success(mFileList))
                    }
                })
                task.execute(0)
            }
        }
        return _localFilesLiveData
    }

    fun searchDownloadDir(mSearchFilter: String?, fileList: ArrayList<LocalFile>, dir: File) {
        if (dir.isDirectory) {
            val files = dir.listFiles()
            if (null != files) {
                for (file in files) {
                    searchDownloadDir(mSearchFilter, fileList, file)
                }
            }
        } else {
            if (dir.name.contains(mSearchFilter!!)) {
                fileList.add(LocalFile(dir))
            }
        }
    }

    fun getSDCardList(): List<File> {
        return mSDCardList
    }

    fun getSelectList(): ArrayList<DataFile> {
        return mSelectedList
    }

    fun uploadFile(fragmentActivity: FragmentActivity, view: View, devId: String, path: String, sharePathType: Int): MutableLiveData<Resource<Int>> {
        val liveData = MutableLiveData<Resource<Int>>()
        val fileManage = LocalFileManage(fragmentActivity, view,
                object : LocalFileManage.OnManageCallback {
                    override fun onComplete(isSuccess: Boolean) {
                        FileListChangeObserver.getInstance().FileListChange()
                        liveData.postValue(if (isSuccess) {
                            Resource.success(null)
                        } else {
                            Resource.error(null, null)
                        }
                        )
                    }

                    override fun onStart(resStrId: Int) {
                        liveData.postValue(Resource.loading(resStrId))
                    }
                })
        val compatPath =PathTypeCompat.getAllStrPath(sharePathType,path)
        fileManage.setUploadPath(devId, compatPath)
        val selected: MutableList<LocalFile> = mutableListOf()
        for (dataFile in getSelectList()) {
            if (dataFile is LocalFile) {
                selected.add(dataFile)
            }
        }
        fileManage.manage(LocalFileType.PRIVATE, FileManageAction.UPLOAD, selected)
        return liveData
    }
}