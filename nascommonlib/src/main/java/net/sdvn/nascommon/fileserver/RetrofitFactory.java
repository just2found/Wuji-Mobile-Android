package net.sdvn.nascommon.fileserver;

import net.sdvn.common.internet.OkHttpClientIns;

import java.util.concurrent.TimeUnit;

import libs.source.common.livedata.ReqStringResGsonConverterFactory;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public final class RetrofitFactory {
    private final static boolean debug = false;

    private RetrofitFactory() {
    }

    public static Retrofit createRetrofit(String host) {
        if (debug) {
            return createMockRetrofit(host);
        } else {
            return createProductRetrofit(host);
        }
    }

    private static Retrofit createProductRetrofit(String host) {
        return new Retrofit.Builder()
                .baseUrl(host)
                .client(OkHttpClientIns.getApiClient())
                .addConverterFactory(ReqStringResGsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build();
    }

    public static Retrofit createMockRetrofit(String host) {
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        return new Retrofit.Builder()
                .baseUrl(host)
                .client(new OkHttpClient.Builder()
                        .connectTimeout(3, TimeUnit.SECONDS)
                        .readTimeout(15, TimeUnit.SECONDS)
                        .writeTimeout(15, TimeUnit.SECONDS)
                        .addInterceptor(loggingInterceptor)
                        .build())
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build();
    }
}