package net.sdvn.common.internet.loader;

import androidx.annotation.NonNull;

import net.sdvn.common.internet.core.GsonBaseProtocol;
import net.sdvn.common.internet.core.V1AgApiHttpLoader;
import net.sdvn.common.internet.presenter.V1AgApiService;

import java.util.concurrent.ConcurrentHashMap;

import io.reactivex.Observable;
import okhttp3.ResponseBody;
import retrofit2.Retrofit;

/**
 * Created by LSW on 2018/4/18.
 * 申请应用登录Token
 */

public class ApplyLoginTokenHttpLoader extends V1AgApiHttpLoader {
    /**
     * 构造传递监听的接口，以及需要被解析的字节码文件
     *
     * @param parseClass
     */
    public ApplyLoginTokenHttpLoader(Class<? extends GsonBaseProtocol> parseClass) {
        super(parseClass);
    }

    public void setParams(String ticket) {
        this.bodyMap = new ConcurrentHashMap<>();
//        String ticket = CMAPI.getInstance().getBaseInfo().getTicket();
        put("ticket", ticket);
    }

    public void setParams(String ticket, String loginName) {
        this.bodyMap = new ConcurrentHashMap<>();
//        String ticket = CMAPI.getInstance().getBaseInfo().getTicket();
        put("ticket", ticket);
        if (loginName != null)
            put("loginname", loginName);
    }

    @Override
    public Observable<ResponseBody> structureObservable(@NonNull Retrofit mRetrofit) {
        V1AgApiService v1AgapiServcie = mRetrofit.create(V1AgApiService.class);
        return v1AgapiServcie.requestV2("applylogontoken", getMap(), this.bodyMap);
    }
}
