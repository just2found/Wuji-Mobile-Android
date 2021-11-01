package net.sdvn.common.internet.loader;


import net.sdvn.cmapi.CMAPI;
import net.sdvn.common.internet.core.GsonBaseProtocol;
import net.sdvn.common.internet.core.V2AgApiHttpLoader;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by yun on 2018/1/23.
 */
public class HardWareDevicesHttpLoader extends V2AgApiHttpLoader {
//    private HardWareDevicesRequestBody body;

    /**
     * 构造传递监听的接口，以及需要被解析的字节码文件
     *
     * @param parseClass
     */
    public HardWareDevicesHttpLoader(Class<? extends GsonBaseProtocol> parseClass) {
        super(parseClass);
        setParams();
    }


    public void setParams() {
//        body = new HardWareDevicesRequestBody();
//        body.action = "gethardwarelist";
//        body.token = CMAPI.getInstance().getBaseInfo().getTicket();
//        body.userid = CMAPI.getInstance().getBaseInfo().getAccount();
        bodyMap = new ConcurrentHashMap<>();
//        put("action", "gethardwarelist");
//        put("token", CMAPI.getInstance().getBaseInfo().getTicket());
        setAction("getbinddevices");
        put("ticket", CMAPI.getInstance().getBaseInfo().getTicket());
//        put("token", LoginTokenUtil.getToken());

    }

//    @Override
//    public Observable<ResponseBody> structureObservable(Retrofit mRetrofit) {
//        V1AgApiService v1AgapiServcie = mRetrofit.create(V1AgApiService.class);
//        ObjectHelper.requireNonNull(this.body, "request body is null");
////        body.sig = getSignEncrypt();
//        return v1AgapiServcie.request(getMap(), this.body);
//    }

}
