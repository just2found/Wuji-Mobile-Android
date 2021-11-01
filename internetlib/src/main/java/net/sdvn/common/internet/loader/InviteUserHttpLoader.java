package net.sdvn.common.internet.loader;

import net.sdvn.cmapi.CMAPI;
import net.sdvn.common.internet.core.GsonBaseProtocol;
import net.sdvn.common.internet.core.V1AgApiHttpLoader;

import java.util.concurrent.ConcurrentHashMap;

import io.reactivex.annotations.NonNull;

/**
 * Created by LSW on 2018/3/29.
 */

public class InviteUserHttpLoader extends V1AgApiHttpLoader {
//    private Map<String, Object> bodyMap;

    public InviteUserHttpLoader(Class<? extends GsonBaseProtocol> parseClass) {
        super(parseClass);
    }

    public void setParams(@NonNull String phone, @NonNull String deviceid, @NonNull String devicesn) {
        this.bodyMap = new ConcurrentHashMap<>();
        put("action", "inviteuser");
        put("userid", CMAPI.getInstance().getBaseInfo().getUserId());
        put("token", CMAPI.getInstance().getBaseInfo().getTicket());
        put("phone", phone);
        put("deviceid", deviceid);
        put("devicesn", devicesn);
    }

    public void setParams(@NonNull String phone, @NonNull String deviceid) {
        this.bodyMap = new ConcurrentHashMap<>();
        put("action", "inviteuser");
        put("userid", CMAPI.getInstance().getBaseInfo().getUserId());
        put("token", CMAPI.getInstance().getBaseInfo().getTicket());
        put("phone", phone);
        put("deviceid", deviceid);
//        put("devicesn", devicesn);
    }

//    @Override
//    public Observable<ResponseBody> structureObservable(Retrofit mRetrofit) {
//        V1AgApiService v1AgapiServcie = mRetrofit.create(V1AgApiService.class);
//        return v1AgapiServcie.request(getMap(), this.bodyMap);
//    }
}
