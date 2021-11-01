package net.sdvn.common.internet.loader;

import net.sdvn.cmapi.CMAPI;
import net.sdvn.common.internet.core.GsonBaseProtocol;
import net.sdvn.common.internet.core.V1AgApiHttpLoader;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by yun on 2018/3/29.
 */

public class SetDeviceCommentHttpLoader extends V1AgApiHttpLoader {

    /**
     * 构造传递监听的接口，以及需要被解析的字节码文件
     *
     * @param parseClass
     */
    public SetDeviceCommentHttpLoader(Class<? extends GsonBaseProtocol> parseClass) {
        super(parseClass);
    }

    /**
     * @param comment  备注
     * @param deviceId 设备iD
     */
    public void setParams(String comment, String deviceId) {
        this.bodyMap = new ConcurrentHashMap<>();
        put("action", "setdevicecomment");
        put("userid", CMAPI.getInstance().getBaseInfo().getUserId());
        put("token", CMAPI.getInstance().getBaseInfo().getTicket());
        put("deviceid", deviceId);
        put("comment", comment);
    }

//    @Override
//    public Observable<ResponseBody> structureObservable(Retrofit mRetrofit) {
//        V1AgApiService v1AgapiServcie = mRetrofit.create(V1AgApiService.class);
////        bodyMap.put("sig", getSignEncrypt());
//        return v1AgapiServcie.request(getMap(), this.bodyMap);
//    }
}
