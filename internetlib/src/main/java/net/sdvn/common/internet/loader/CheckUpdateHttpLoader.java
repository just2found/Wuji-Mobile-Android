package net.sdvn.common.internet.loader;

import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import net.sdvn.cmapi.CMAPI;
import net.sdvn.common.internet.BuildConfig;
import net.sdvn.common.internet.NetConfig;
import net.sdvn.common.internet.core.GsonBaseProtocol;
import net.sdvn.common.internet.core.HttpLoader;
import net.sdvn.common.internet.presenter.V1AgApiService;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import io.reactivex.Observable;
import io.reactivex.internal.functions.ObjectHelper;
import okhttp3.HttpUrl;
import okhttp3.ResponseBody;
import retrofit2.Retrofit;

public class CheckUpdateHttpLoader extends HttpLoader {
    protected Map<String, Object> bodyMap;

    /**
     * 构造传递监听的接口，以及需要被解析的字节码文件
     *
     * @param parseClass
     */
    public CheckUpdateHttpLoader(Class<? extends GsonBaseProtocol> parseClass) {
        super(parseClass);
    }

    /**
     * {
     * "partnerid": "Y1DMATNYSMZPOKC3R8NJ",
     * "appid": "CN6SDL3H5K4UL55YP77L",
     * "deviceclass": 203138,
     * "version": "5.1.2"
     * }
     */
    public void setParams(String version) {
        this.bodyMap = new ConcurrentHashMap<>();
        put("partnerid", (CMAPI.getInstance().getPartnerId()));
        put("appid", (CMAPI.getInstance().getAppId()));
        if (BuildConfig.DEBUG) {
            put("deviceclass", 203138);
            put("version", "5.1.2");
        } else {
            put("deviceclass", CMAPI.getInstance().getDevClass());
            put("version", version);
        }
    }

    @NonNull
    @Override
    protected HttpUrl getHttpUrl() {
        return new HttpUrl
                .Builder()
                .scheme(NetConfig.schema)
                .host(NetConfig.host())
                .port(NetConfig.port2)
                .build();
    }

    protected void put(String key, @Nullable Object obj) {
        if (bodyMap != null) {
            if (!TextUtils.isEmpty(key) && obj != null) {
                bodyMap.put(key, obj);
            }
        }
    }

    @Override
    public Observable<ResponseBody> structureObservable(Retrofit mRetrofit) {
        V1AgApiService service = mRetrofit.create(V1AgApiService.class);
        ObjectHelper.requireNonNull(this.bodyMap, "request body is null");
        return service.checkUpdate(this.bodyMap);
    }
}
