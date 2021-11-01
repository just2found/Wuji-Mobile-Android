package net.linkmate.app.data;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;

import net.sdvn.common.internet.NetConfig;
import net.sdvn.common.internet.OkHttpClientIns;
import net.sdvn.common.internet.protocol.GetUserInfoResultBean;

import java.util.HashMap;
import java.util.Map;

import libs.source.common.livedata.ApiResponse;
import libs.source.common.livedata.LiveDataCallAdapterFactory;
import okhttp3.HttpUrl;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.QueryMap;

public interface V2AgApiService {
    @NonNull
    @POST("v2/agapi/getuserinfo")
    LiveData<ApiResponse<GetUserInfoResultBean>> getUserInfo(
            @QueryMap HashMap<String, String> map
            , @Body Map<String, Object> body
    );

    static V2AgApiService build() {
        return buildRetrofit().create(V2AgApiService.class);
    }

    static Retrofit buildRetrofit() {
        Retrofit.Builder builder = new Retrofit.Builder();
        HttpUrl httpUrl = getHttpUrl();
        builder.baseUrl(httpUrl)
                .addCallAdapterFactory(LiveDataCallAdapterFactory.Companion.create())
                .addConverterFactory(GsonConverterFactory.create());
        if (OkHttpClientIns.getApiClient() != null)
            builder.client(OkHttpClientIns.getApiClient());
        return builder.build();
    }

    static HttpUrl getHttpUrl() {
        return new HttpUrl
                .Builder()
                .scheme(NetConfig.schema)
                .host(NetConfig.host())
                .port(NetConfig.port)
                .build();
    }
}
