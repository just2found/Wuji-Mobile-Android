package net.sdvn.common.internet.loader;


import net.sdvn.cmapi.CMAPI;
import net.sdvn.common.internet.core.GsonBaseProtocol;
import net.sdvn.common.internet.core.V1AgApiHttpLoader;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by yun on 2018/3/19.
 * <p>
 * request
 * {
 * "action":"setenableshare",
 * "userid":"563018672898052",
 * "deviceid":""658432984212576,
 * "token":"563040147737735-e88046e5-e769-4f6f-a252-6bf5060156e3",
 * "sig":"30db206bfd3fea7ef0db929998642c8ea54cc7042a779c5a0d9897358f6e9505",
 * "enableshare":"false"
 * }
 * <p>
 * response
 * <p>
 * {
 * "result":0,
 * "errmsg":"success"
 * }
 */

public class EnableShareHttpLoader extends V1AgApiHttpLoader {

    /**
     * 构造传递监听的接口，以及需要被解析的字节码文件
     *
     * @param parseClass
     */
    public EnableShareHttpLoader(Class<? extends GsonBaseProtocol> parseClass) {
        super(parseClass);
    }

    /**
     * @param enableshare 是否需要验证
     * @param deviceId    设备iD
     */
    public void setParams(boolean enableshare, String deviceId) {
        this.bodyMap = new ConcurrentHashMap<>();
        put("action", "setenableshare");
        put("userid", CMAPI.getInstance().getBaseInfo().getUserId());
        put("token", CMAPI.getInstance().getBaseInfo().getTicket());
        put("deviceid", deviceId);
        put("enableshare", enableshare ? "true" : "false");
    }

//    @Override
//    public Observable<ResponseBody> structureObservable(Retrofit mRetrofit) {
//        V1AgApiService v1AgapiServcie = mRetrofit.create(V1AgApiService.class);
////        bodyMap.put("sig", getSignEncrypt());
//        return v1AgapiServcie.request(getMap(), this.bodyMap);
//    }
}
