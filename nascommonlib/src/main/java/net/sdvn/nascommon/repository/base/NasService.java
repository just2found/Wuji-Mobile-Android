package net.sdvn.nascommon.repository.base;

import androidx.lifecycle.LiveData;

import net.sdvn.nascommon.constant.OneOSAPIs;
import net.sdvn.nascommon.model.DataSessionUser;
import net.sdvn.nascommon.model.oneos.ActionResultModel;
import net.sdvn.nascommon.model.oneos.BaseResultModel;
import net.sdvn.nascommon.model.oneos.OneOSFile;
import net.sdvn.nascommon.model.oneos.OneStat;
import net.sdvn.nascommon.model.oneos.vo.FileListModel;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

import io.reactivex.Observable;
import io.weline.repo.files.data.DataPhotosTimelineYearSummary;
import libs.source.common.livedata.ApiResponse;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface NasService {
    @GET(OneOSAPIs.SYSTEM_STAT)
    Observable<OneStat> getSystemStat();

    @POST(OneOSAPIs.FILE_API)
    LiveData<ApiResponse<BaseResultModel<FileListModel>>> loadFiles(@Body Map<String, Object> map);

    @POST("file")
    LiveData<ApiResponse<BaseResultModel<FileListModel>>> loadFileList(@Body Map<String, Object> map);

    @POST("file")
    @NotNull LiveData<ApiResponse<ActionResultModel<OneOSFile>>> manageFiles(@Body Map<String, Object> map);

    @POST("file")
    @NotNull Call<ActionResultModel<OneOSFile>> manageFiles2(@Body Map<String, Object> map);

    @POST("user")
    LiveData<ApiResponse<BaseResultModel<DataSessionUser>>> access(@Body Map<String, Object> map);

    @POST("file")
    LiveData<ApiResponse<BaseResultModel<List<DataPhotosTimelineYearSummary>>>> loadPhotosTimelineSummary(@NotNull @Body Map<String, Object> map);
}