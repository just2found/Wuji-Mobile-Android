package net.sdvn.common.internet.loader;


import net.sdvn.cmapi.CMAPI;
import net.sdvn.common.internet.core.GsonBaseProtocol;
import net.sdvn.common.internet.core.V1AgApiHttpLoader;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by yun on 2018/1/23.
 */

public class DeviceSharedUsersHttpLoader extends V1AgApiHttpLoader {
//    private DeviceSharedUsersRequestBody body;

    /**
     * 构造传递监听的接口，以及需要被解析的字节码文件
     *
     * @param parseClass
     */
    public DeviceSharedUsersHttpLoader(Class<? extends GsonBaseProtocol> parseClass) {
        super(parseClass);

    }


    public void setParams(String deviceid) {
//        body = new DeviceSharedUsersRequestBody();
        bodyMap = new ConcurrentHashMap<>();
        put("action", "getshareuser");
//        put("userid", CMAPI.getInstance().getBaseInfo().getUserId());
        put("token", CMAPI.getInstance().getBaseInfo().getTicket());
        put("deviceid", deviceid);
//        body.action = "getshareuser";
//        body.token = CMAPI.getInstance().getBaseInfo().getTicket();
//        body.userid = CMAPI.getInstance().getBaseInfo().getUserId();
//        body.deviceid = deviceid;
        setTag(deviceid);
    }


    //    @Override
//    public Observable<ResponseBody> structureObservable(Retrofit mRetrofit) {
//        V1AgApiService v1AgapiServcie = mRetrofit.create(V1AgApiService.class);
//        ObjectHelper.requireNonNull(this.body, "request body is null");
//        return v1AgapiServcie.request(getMap(), this.body);
//    }

}
