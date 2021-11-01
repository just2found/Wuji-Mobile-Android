package io.weline.repo.files

import androidx.lifecycle.LiveData
import io.reactivex.Observable
import io.weline.repo.files.data.*
import libs.source.common.livedata.Resource

interface NasApis {

    fun access(devId: String): LiveData<Resource<LoginSession>>

    fun move(devId: String,
             paths: List<String>,
             toDir: String,
             sharePathType: SharePathType?):
            LiveData<Resource<ActionResultModel<OneOSFile?>>>

    fun attr(devId: String,
             path: String,
             sharePathType: SharePathType?):
            LiveData<Resource<ActionResultModel<OneOSFile?>>>

    fun rename(devId: String,
               path: String,
               newName: String,
               sharePathType: SharePathType?):
            LiveData<Resource<ActionResultModel<OneOSFile?>>>

    fun mkdir(devId: String,
              path: String,
              sharePathType: SharePathType?):
            LiveData<Resource<ActionResultModel<OneOSFile?>>>

    fun copy(devId: String,
             paths: List<String>,
             toDir: String,
             sharePathType: SharePathType?):
            LiveData<Resource<ActionResultModel<OneOSFile?>>>

    fun delete(devId: String,
               paths: List<String>,
               isShift: Boolean = false,
               sharePathType: SharePathType?):
            LiveData<Resource<ActionResultModel<OneOSFile?>>>

    fun searchFile(devId: String,
                   path: String? = "/",
                   pattern: String,
                   pathType: OneOSFileType,
                   filter: OneOSFileType = OneOSFileType.ALL,
                   showHidden: Boolean = false):
            LiveData<Resource<BaseResultModel<FileListModel>>>

    fun loadFilesFromServer(devId: String,
                            type: OneOSFileType,
                            path: String?,
                            page: Int = -1,
                            showHidden: Boolean = false,
                            filter: OneOSFileType = OneOSFileType.ALL,
                            order: String? = null):
            LiveData<Resource<BaseResultModel<FileListModel>>>

    fun genThumbnailUrl(devId: String, path: String, type: OneOSFileType): String?

    fun genDownloadUrl(devId: String, path: String, type: OneOSFileType): String?

    fun loadTagFilesLD(devId: String, tagId: Int, page: Int = 0):
            LiveData<Resource<BaseResultModel<FileListModel>>>

    fun loadTagFiles(devId: String, tagId: Int, page: Int = 0):
            Observable<Resource<BaseResultModel<FileListModel>>>

    fun getFileTagsLD(devId: String): LiveData<Resource<BaseResultModel<List<FileTag>>>>
    fun getFileTags(devId: String): Observable<Resource<BaseResultModel<List<FileTag>>>>

}
