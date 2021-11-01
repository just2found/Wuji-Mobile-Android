package net.sdvn.common.internet;

import android.app.Application;
import android.content.Context;
import android.content.res.AssetManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import net.sdvn.common.internet.utils.HttpsUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import okhttp3.Cache;
import okhttp3.ConnectionPool;
import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import timber.log.Timber;


/**
 * Created by yun on 2018/1/11.
 */

public class OkHttpClientIns {
    private static final String HTTP_CACHE_DIR = "httpCache";
    private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();
    public static final int MAX_POOL_SIZE = CPU_COUNT * 2 + 1;          //最大线程池的数量
    private static final int KEEP_ALIVE_TIME = 6;        //存活的时间
    private static final TimeUnit UNIT = TimeUnit.MINUTES; //时间单位
    private final static boolean isHttpDebug = true;
    public static int CORE_POOL_SIZE = (CPU_COUNT + 1) >= 3 ? (CPU_COUNT + 1) / 3 : 1;
    private static OkHttpClient sApiClient;
    private static ConcurrentHashMap<String, List<Cookie>> cookieStore;
    private static Context mContext;
    @Nullable
    private static Handler mHandler;
    private static OkHttpClient transmitClient;
    private static String sHost;

    public static void init(Application application, String host) {
        mContext = application.getApplicationContext();
        setHost(host);
        if (cookieStore == null)
            cookieStore = new ConcurrentHashMap<>();
        OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder()
                //代理服务器的IP和端口号
//        builder.proxy(new Proxy(Proxy.Type.HTTP, new InetSocketAddress("127.0.0.1", 8080)));
//代理的鉴权账号密码
//        final String userName = "";
//        final String password = "";
//        builder.proxyAuthenticator(new Authenticator() {
//            @Override
//            public Request authenticate(Route route, Response response) throws IOException {
//                设置代理服务器账号密码
//                String credential = Credentials.basic(userName, password);
//                return response.request().newBuilder()
//                        .header("Proxy-Authorization", credential)
//                        .build();
//            }
//        });
                .cache(new Cache(new File(application.getCacheDir(), HTTP_CACHE_DIR), NetConfig.DEFAULT_CACHE_SIZE))
//                .addNetworkInterceptor(new NetCacheInterceptor())
//                .addInterceptor(new OfflineCacheInterceptor())
                .connectTimeout(15, TimeUnit.SECONDS)
                .writeTimeout(15, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .connectionPool(new ConnectionPool(MAX_POOL_SIZE / 2, KEEP_ALIVE_TIME, UNIT))
                .cookieJar(new CookieJar() {
                    @Override
                    public void saveFromResponse(@NonNull HttpUrl url, @NonNull List<Cookie> cookies) {
                        cookieStore.put(url.host(), cookies);
                    }

                    @NonNull
                    @Override
                    public List<Cookie> loadForRequest(@NonNull HttpUrl url) {
                        List<Cookie> cookies = cookieStore.get(url.host());
                        return cookies == null ? new ArrayList<Cookie>() : cookies;
                    }
                });
        if (BuildConfig.DEBUG && isHttpDebug) {
            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
            clientBuilder.addInterceptor(loggingInterceptor);
        }
        if (NetConfig.isPubTest) {
            //非自定义证书无需配置此项目
            HttpsUtils.SSLParams sslParams = HttpsUtils.getSslSocketFactory();
            clientBuilder.sslSocketFactory(sslParams.sSLSocketFactory, sslParams.trustManager);
            clientBuilder.hostnameVerifier(HttpsUtils.SafeHostnameVerifier);
        }
        sApiClient = clientBuilder.build();
    }

    private static void setTrustManager(Context context, OkHttpClient.Builder clientBuilder, String host) {
        List<InputStream> streams = new ArrayList<>();
        try {
            AssetManager assets = context.getAssets();
            String[] cers = assets.list("cers");
            if (cers != null) {
                for (String cer : cers) {
                    try {
                        if (host.endsWith(cer)) {
                            Timber.d("test cer: %s", cer);
                            streams.add(assets.open("cers/" + cer));
                            break;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
//        HttpsUtils.SSLParams sslParams = HttpsUtils.getSslSocketFactory(streams.toArray(new InputStream[0]));
        HttpsUtils.SSLParams sslParams = HttpsUtils.getSslSocketFactory(new HttpsUtils.SafeTrustManager(streams.get(0)));
        clientBuilder.sslSocketFactory(sslParams.sSLSocketFactory, sslParams.trustManager);
        clientBuilder.hostnameVerifier(HttpsUtils.SafeHostnameVerifier);
    }

    public static String getHost() {
        return /*"as.izzbie.com";*/sHost;
    }

    public static String setHost(String host) {
//        if (sApiClient != null && !Objects.equals(host, sHost)) {
//            OkHttpClient.Builder builder = sApiClient.newBuilder();
//            setTrustManager(mContext, builder, host);
//            sApiClient = builder.build();
//        }
        return sHost = host;//"192.168.1.76";
//        return host;
    }

    /**
     * Check network
     */
    public static boolean checkNetwork() {
        if (mContext == null) {
            throw new NullPointerException("Context is null! Are you init?");
        }
        ConnectivityManager connectivityManager = (ConnectivityManager) mContext
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager == null ? null : connectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }

    public static void cleanCooke() {
        OkHttpClient client = sApiClient;
        if (client != null) {
            if (cookieStore != null) {
                cookieStore.clear();
            }
        }

        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
            mHandler = null;
        }
    }

    public static OkHttpClient getApiClient() {
        return sApiClient;
    }

    public static Context getContext() {
        if (mContext == null) {
            throw new NullPointerException("pls init OkHttpClientIns");
        }
        return mContext;
    }

    public static OkHttpClient getTransmitClient() {
        if (transmitClient == null) {
            OkHttpClient.Builder builder = new OkHttpClient.Builder()
                    .connectTimeout(10, TimeUnit.SECONDS)
                    .writeTimeout(50, TimeUnit.SECONDS)
                    .readTimeout(50, TimeUnit.SECONDS)
                    .retryOnConnectionFailure(true)
                    .connectionPool(new ConnectionPool(Math.max(CORE_POOL_SIZE, 3),
                            2, TimeUnit.MINUTES));

            HttpsUtils.SSLParams sslParams = HttpsUtils.getSslSocketFactory();
            builder.sslSocketFactory(sslParams.sSLSocketFactory, sslParams.trustManager);
            builder.hostnameVerifier(HttpsUtils.UnSafeHostnameVerifier);
            if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
                loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.HEADERS);
                builder.addInterceptor(loggingInterceptor);
            }
            transmitClient = builder.build();
        }
        return transmitClient;
    }

    /**
     * new Interceptor() {
     *
     * @Override public Response intercept(Chain chain) throws IOException {
     * Request original = chain.request();
     * Request.Builder builder = original.newBuilder()
     * .header("Content-Type", "application/json; charset=UTF-8");
     * Request request = builder.build();
     * return chain.proceed(request);
     * }
     * <p>
     * }
     * <p>
     * 有网时候的缓存
     */
    static class NetCacheInterceptor implements Interceptor {
        @NonNull
        @Override
        public Response intercept(@NonNull Chain chain) throws IOException {
            Request request = chain.request();
            Response response = chain.proceed(request);
            int onlineCacheTime = 3;//在线的时候的缓存过期时间，如果想要不缓存，直接时间设置为0
            return response.newBuilder()
                    .header("Cache-Control", "public, max-age=" + onlineCacheTime)
                    .removeHeader("Pragma")
                    .build();
        }
    }

    /**
     * 没有网时候的缓存
     */
    static class OfflineCacheInterceptor implements Interceptor {
        @NonNull
        @Override
        public Response intercept(@NonNull Chain chain) throws IOException {
            Request request = chain.request();
            if (!checkNetwork()) {
                int offlineCacheTime = 5 * 60;//离线的时候的缓存的过期时间
                request = request.newBuilder()
                        .header("Cache-Control", "public, only-if-cached, max-stale=" + offlineCacheTime)
                        .build();
            }
            return chain.proceed(request);
        }
    }

}
