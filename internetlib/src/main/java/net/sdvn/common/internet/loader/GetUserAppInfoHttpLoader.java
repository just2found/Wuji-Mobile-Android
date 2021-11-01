package net.sdvn.common.internet.loader;

import androidx.annotation.NonNull;

import net.sdvn.cmapi.CMAPI;
import net.sdvn.common.internet.core.GsonBaseProtocol;
import net.sdvn.common.internet.core.V1AgApiHttpLoader;
import net.sdvn.common.internet.presenter.V1AgApiService;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import io.reactivex.Observable;
import okhttp3.ResponseBody;
import retrofit2.Retrofit;

/**
 * Created by LSW on 2018/4/18.
 */

public class GetUserAppInfoHttpLoader extends V1AgApiHttpLoader {
    private Map<String, Object> bodyMap;

    /**
     * 构造传递监听的接口，以及需要被解析的字节码文件
     *
     * @param parseClass
     */
    public GetUserAppInfoHttpLoader(Class<? extends GsonBaseProtocol> parseClass) {
        super(parseClass);
        setParams();
    }

    public void setParams() {
        this.bodyMap = new ConcurrentHashMap<>();
       put("action", "getuserappinfo");
       put("userid", CMAPI.getInstance().getBaseInfo().getUserId());
    }

    @Override
    public Observable<ResponseBody> structureObservable(@NonNull Retrofit mRetrofit) {
        V1AgApiService v1AgapiServcie = mRetrofit.create(V1AgApiService.class);
        return v1AgapiServcie.request(getMap(), this.bodyMap);
    }
}
