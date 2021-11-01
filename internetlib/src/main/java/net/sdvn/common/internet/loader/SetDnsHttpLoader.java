package net.sdvn.common.internet.loader;

import net.sdvn.cmapi.CMAPI;
import net.sdvn.common.internet.core.GsonBaseProtocol;
import net.sdvn.common.internet.core.V2AgApiHttpLoader;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by yun on 2018/1/23.
 */

public class SetDnsHttpLoader extends V2AgApiHttpLoader {
    public SetDnsHttpLoader(Class<? extends GsonBaseProtocol> parseClass) {
        super(parseClass);
        setAction("setdns");
    }

    public void setParams(String deviceid, String dns) {
        this.bodyMap = new ConcurrentHashMap<>();
        put("ticket", CMAPI.getInstance().getBaseInfo().getTicket());
        put("deviceid", deviceid);
        put("dns", dns);
    }

//    @Override
//    public Observable<ResponseBody> structureObservable(@NonNull Retrofit mRetrofit) {
//        V1AgApiService v1AgapiServcie = mRetrofit.create(V1AgApiService.class);
//        ObjectHelper.requireNonNull(this.bodyMap, "request body is null");
//        return v1AgapiServcie.setDns(getMap(), this.bodyMap);
//    }
}
