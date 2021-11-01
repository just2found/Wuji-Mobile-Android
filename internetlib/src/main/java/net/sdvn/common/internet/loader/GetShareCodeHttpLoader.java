package net.sdvn.common.internet.loader;


import net.sdvn.cmapi.CMAPI;
import net.sdvn.common.internet.core.GsonBaseProtocol;
import net.sdvn.common.internet.core.V1AgApiHttpLoader;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by yun on 2018/1/24.
 */

public class GetShareCodeHttpLoader extends V1AgApiHttpLoader {
//    GetShareCodeRequestBody body;

    /**
     * 构造传递监听的接口，以及需要被解析的字节码文件
     *
     * @param parseClass
     */
    public GetShareCodeHttpLoader(Class<? extends GsonBaseProtocol> parseClass) {
        super(parseClass);
    }


    public void setParams(String device_id) {
//        body = new GetShareCodeRequestBody();
//        body.action = "getsharecode";
//        body.deviceid = device_id;
////        body.userid = CMAPI.getInstance().getBaseInfo().getAccount();
//        body.token = CMAPI.getInstance().getBaseInfo().getTicket();

        bodyMap = new ConcurrentHashMap<>();
        put("action", "getsharecode");
        put("deviceid", device_id);
        put("token", CMAPI.getInstance().getBaseInfo().getTicket());

    }

//    @Override
//    public Observable<ResponseBody> structureObservable(Retrofit mRetrofit) {
//        V1AgApiService v1AgapiServcie = mRetrofit.create(V1AgApiService.class);
//        ObjectHelper.requireNonNull(this.body, "request body is null");
////        body.sig = getSignEncrypt();
//        return v1AgapiServcie.request(getMap(), this.body);
//    }

}
