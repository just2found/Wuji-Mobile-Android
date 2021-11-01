package net.sdvn.common.internet.loader;

import androidx.annotation.NonNull;

import net.sdvn.common.internet.core.GsonBaseProtocol;
import net.sdvn.common.internet.core.V1AgApiHttpLoader;
import net.sdvn.common.internet.presenter.V1AgApiService;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import io.reactivex.Observable;
import okhttp3.ResponseBody;
import retrofit2.Retrofit;

public class RemoveShareTokenHttpLoader extends V1AgApiHttpLoader {
    private String action;

    /**
     * 构造传递监听的接口，以及需要被解析的字节码文件
     *
     * @param parseClass
     */
    public RemoveShareTokenHttpLoader(Class<? extends GsonBaseProtocol> parseClass) {
        super(parseClass);
    }

    public void setParams(String token, Set<String> sharetokens) {
        this.bodyMap = new ConcurrentHashMap<>();
        this.action = "removesharetokens";
        put("token", token);
        put("sharetoken", (sharetokens));
    }

    public void setParams(String token, String sharetoken) {
        this.bodyMap = new ConcurrentHashMap<>();
        this.action = "removesharetoken";
        put("token", token);
        put("sharetoken", (sharetoken));
    }

    @Override
    public Observable<ResponseBody> structureObservable(@NonNull Retrofit mRetrofit) {
        V1AgApiService v1AgapiServcie = mRetrofit.create(V1AgApiService.class);
        return v1AgapiServcie.requestV2(action, getMap(), this.bodyMap);
    }
}