package net.sdvn.nascommon.fileserver;


import net.sdvn.nascommon.fileserver.data.DataCreate;
import net.sdvn.nascommon.fileserver.data.DataShareDir;
import net.sdvn.nascommon.fileserver.data.DataShareProgress;
import net.sdvn.nascommon.fileserver.data.DataShareVersion;
import net.sdvn.nascommon.fileserver.data.DataShared;
import net.sdvn.nascommon.fileserver.data.DataSharedInfo;

import java.util.Map;

import io.reactivex.Observable;
import okhttp3.ResponseBody;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface FileServerApiService {
    @POST("/{path}")
    Observable<FileShareBaseResult> request(@Path("path") String path, @Body Map<String, Object> bodyMap);

    @POST("/{path}")
    Observable<FileShareBaseResult<DataCreate>> create(@Path("path") String path, @Body Map<String, Object> bodyMap);

    @POST("/{path}")
    Observable<FileShareBaseResult> cancel(@Path("path") String path, @Body Map<String, Object> bodyMap);

    @POST("/{path}")
    Observable<FileShareBaseResult<DataShared>> getList(@Path("path") String path, @Body Map<String, Object> bodyMap);

    @POST("/{path}")
    Observable<FileShareBaseResult> download(@Path("path") String path, @Body Map<String, Object> bodyMap);

    @POST("/{path}")
    Observable<FileShareBaseResult<DataShareDir>> getShareDir(@Path("path") String path, @Body Map<String, Object> bodyMap);

    @POST("/{path}")
    Observable<FileShareBaseResult<DataShareProgress>> progress(@Path("path") String path, @Body Map<String, Object> bodyMap);

    @POST("/{path}")
    Observable<FileShareBaseResult<DataSharedInfo>> getSharedInfo(@Path("path") String path, @Body Map<String, Object> bodyMap);

    @POST("/{path}")
    Observable<ResponseBody> requestEncrypt(@Path("path") String path, @Body String body);

    @GET("/version")
    Observable<FileShareBaseResult<DataShareVersion>> version();

}
