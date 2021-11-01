package net.sdvn.common.internet.core;

import androidx.annotation.NonNull;

import net.sdvn.common.Local;
import net.sdvn.common.internet.presenter.V1AgApiService;

import io.reactivex.Observable;
import io.reactivex.internal.functions.ObjectHelper;
import okhttp3.ResponseBody;
import retrofit2.Retrofit;

/**
 * Created by yun on 2018/1/17.
 */

public abstract class V2AgApiHttpLoader extends V1AgApiHttpLoader {
    private String action;

    /**
     * 构造传递监听的接口，以及需要被解析的字节码文件
     *
     * @param parseClass
     */
    public V2AgApiHttpLoader(Class<? extends GsonBaseProtocol> parseClass) {
        super(parseClass);
    }


    @Override
    public Observable<ResponseBody> structureObservable(@NonNull Retrofit mRetrofit) {
        V1AgApiService service = mRetrofit.create(V1AgApiService.class);
        ObjectHelper.requireNonNull(this.bodyMap, "request body is null");
        put("lg", Local.getApiLanguage());
        put("lang", Local.getApiLanguage());
        return service.requestV2(getAction(), getMap(), this.bodyMap);
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }
}
