package io.weline.repo.files;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;

import java.util.List;
import java.util.Map;

import io.reactivex.Observable;
import io.weline.repo.files.constant.OneOSAPIs;
import io.weline.repo.files.data.ActionResultModel;
import io.weline.repo.files.data.BaseResultModel;
import io.weline.repo.files.data.DataSessionUser;
import io.weline.repo.files.data.FileListModel;
import io.weline.repo.files.data.FileTag;
import io.weline.repo.files.data.OneOSFile;
import libs.source.common.livedata.ApiResponse;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface NasService {

    @POST(OneOSAPIs.FILE_API)
    @NonNull
    LiveData<ApiResponse<BaseResultModel<FileListModel>>> loadFiles(@Body @NonNull Map<String, Object> map);

    @POST("file")
    @NonNull
    LiveData<ApiResponse<BaseResultModel<FileListModel>>> loadFileList(@Body @NonNull Map<String, Object> map);

    @POST("file")
    @NonNull
    LiveData<ApiResponse<ActionResultModel<OneOSFile>>> manageFiles(@Body @NonNull Map<String, Object> map);

    @POST(OneOSAPIs.FILE_API)
    @NonNull
    LiveData<ApiResponse<ActionResultModel<OneOSFile>>> manageFilesOneOS(@Body @NonNull Map<String, Object> map);

    //    { "method":"tag", "session":"xxx","params":{"cmd":"files","id":xx}}
//{"result":true,"data":{"total":1,"page":0,"pages":1,"files":[{"name":"test.png","path":"/
//    test.png","uid":1006,"gid":0,"size":13002,"time":1469088031,"type":"pic","perm":"rwx------
//    "}]}}
    @POST(OneOSAPIs.FILE_API)
    @NonNull
    Observable<BaseResultModel<FileListModel>> getTagFiles(@Body @NonNull Map<String, Object> map);

    @POST(OneOSAPIs.FILE_API)
    @NonNull
    LiveData<ApiResponse<BaseResultModel<FileListModel>>> getTagFilesLD(@Body @NonNull Map<String, Object> map);

    @POST("file")
    @NonNull
    Observable<BaseResultModel<FileListModel>> getTagFilesV5(@Body @NonNull Map<String, Object> map);

    @POST("file")
    @NonNull
    LiveData<ApiResponse<BaseResultModel<FileListModel>>> getTagFilesLDV5(@Body @NonNull Map<String, Object> map);

    @POST("file")
    @NonNull
    LiveData<ApiResponse<BaseResultModel<List<FileTag>>>> getTagsLDV5(@Body @NonNull Map<String, Object> map);

    @POST(OneOSAPIs.FILE_API)
    @NonNull
    LiveData<ApiResponse<BaseResultModel<List<FileTag>>>> getTagsLD(@Body @NonNull Map<String, Object> map);

    @POST("file")
    @NonNull
    Observable<BaseResultModel<List<FileTag>>> getTagsV5(@Body @NonNull Map<String, Object> map);

    @POST(OneOSAPIs.FILE_API)
    @NonNull
    Observable<BaseResultModel<List<FileTag>>> getTags(@Body @NonNull Map<String, Object> map);

    @POST("file")
    @NonNull
    LiveData<ApiResponse<BaseResultModel<FileTag>>> manageTagsLDV5(@Body @NonNull Map<String, Object> map);

    @POST(OneOSAPIs.FILE_API)
    @NonNull
    Observable<BaseResultModel<FileTag>> manageTagsLD(@Body @NonNull Map<String, Object> map);

    @POST("user")
    @NonNull
    LiveData<ApiResponse<BaseResultModel<DataSessionUser>>> access(@Body @NonNull Map<String, Object> map);

    @POST(OneOSAPIs.USER)
    @NonNull
    LiveData<ApiResponse<BaseResultModel<DataSessionUser>>> menet(@Body @NonNull Map<String, Object> map);

    @POST("user")
    @NonNull
    Observable<BaseResultModel<DataSessionUser>> accessRx(@Body @NonNull Map<String, Object> map);

    @POST(OneOSAPIs.USER)
    @NonNull
    Observable<BaseResultModel<DataSessionUser>> menetRx(@Body @NonNull Map<String, Object> map);

}