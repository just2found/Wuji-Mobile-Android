package net.sdvn.common.internet.presenter;

import androidx.annotation.NonNull;

import java.util.HashMap;
import java.util.Map;

import io.reactivex.Observable;
import okhttp3.ResponseBody;
import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.QueryMap;


/**
 * Created by yun on 2018/1/17.
 */

public interface V1AgApiService {


    @NonNull
    @POST("v1/agapi")
    Observable<ResponseBody> request(
            @QueryMap HashMap<String, String> map
            , @Body Map<String, Object> body
    );


    @NonNull
    @POST("v2/agapi/{action}")
    Observable<ResponseBody> requestV2(
            @Path("action") String method,
            @QueryMap HashMap<String, String> map
            , @Body Map<String, Object> body
    );

    @NonNull
    @POST("v2/updatesrv/checkupdate")
    Observable<ResponseBody> checkUpdate(
            @Body Map<String, Object> body
    );


}
