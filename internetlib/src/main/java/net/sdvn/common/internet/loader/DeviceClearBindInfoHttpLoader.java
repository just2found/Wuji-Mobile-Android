package net.sdvn.common.internet.loader;

import net.sdvn.cmapi.CMAPI;
import net.sdvn.common.internet.core.GsonBaseProtocol;
import net.sdvn.common.internet.core.V1AgApiHttpLoader;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by yun on 18/04/23.
 */

public class DeviceClearBindInfoHttpLoader extends V1AgApiHttpLoader {


    /**
     * 构造传递监听的接口，以及需要被解析的字节码文件
     *
     * @param parseClass
     */
    public DeviceClearBindInfoHttpLoader(Class<? extends GsonBaseProtocol> parseClass) {
        super(parseClass);
    }

    /**
     * "action":"cleardeviceuser",
     * "userid":"560134577432",
     * "token":"563040147737735-e88046e5-e769-4f6f-a252-6bf5060156e3",
     * "deviceid":"658432984212576",
     */
    public void setParams(String deviceId) {
        bodyMap = new ConcurrentHashMap<>();
        put("action", "cleardeviceuser");
        put("userid", CMAPI.getInstance().getBaseInfo().getUserId());
        put("token", CMAPI.getInstance().getBaseInfo().getTicket());
        put("deviceid", deviceId);
        setTag(deviceId);
    }

//    @Override
//    public Observable<ResponseBody> structureObservable(Retrofit mRetrofit) {
//        V1AgApiService v1AgapiServcie = mRetrofit.create(V1AgApiService.class);
//        return v1AgapiServcie.request(getMap(), this.bodyMap);
//    }
}
