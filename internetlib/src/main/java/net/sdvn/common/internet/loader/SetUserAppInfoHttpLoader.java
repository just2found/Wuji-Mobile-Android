package net.sdvn.common.internet.loader;

import net.sdvn.cmapi.CMAPI;
import net.sdvn.common.internet.core.GsonBaseProtocol;
import net.sdvn.common.internet.core.V1AgApiHttpLoader;
import net.sdvn.common.internet.presenter.V1AgApiService;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import io.reactivex.Observable;
import io.reactivex.annotations.NonNull;
import okhttp3.ResponseBody;
import retrofit2.Retrofit;

/**
 * Created by LSW on 2018/4/18.
 */

public class SetUserAppInfoHttpLoader extends V1AgApiHttpLoader {
    private Map<String, Object> bodyMap;

    /**
     * 构造传递监听的接口，以及需要被解析的字节码文件
     *
     * @param parseClass
     */
    public SetUserAppInfoHttpLoader(Class<? extends GsonBaseProtocol> parseClass) {
        super(parseClass);
    }

    public void setParams(@NonNull String info, @NonNull int version) {
        this.bodyMap = new ConcurrentHashMap<>();
        put("action", "setuserappinfo");
        put("userid", CMAPI.getInstance().getBaseInfo().getUserId());
        put("info", info);
        put("token", CMAPI.getInstance().getBaseInfo().getTicket());
        put("version", version);
    }

    @Override
    public Observable<ResponseBody> structureObservable(@androidx.annotation.NonNull Retrofit mRetrofit) {
        V1AgApiService v1AgapiServcie = mRetrofit.create(V1AgApiService.class);
        return v1AgapiServcie.request(getMap(), this.bodyMap);
    }
}
