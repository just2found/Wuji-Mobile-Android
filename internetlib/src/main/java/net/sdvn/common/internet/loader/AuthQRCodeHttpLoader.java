package net.sdvn.common.internet.loader;


import net.sdvn.cmapi.CMAPI;
import net.sdvn.common.internet.core.GsonBaseProtocol;
import net.sdvn.common.internet.core.V1AgApiHttpLoader;

import java.util.concurrent.ConcurrentHashMap;


/**
 * Created by yun on 2018/1/17.
 */

public class AuthQRCodeHttpLoader extends V1AgApiHttpLoader {

    /**
     * 构造传递监听的接口，以及需要被解析的字节码文件
     *
     * @param parseClass
     */
    public AuthQRCodeHttpLoader(Class<? extends GsonBaseProtocol> parseClass) {
        super(parseClass);
    }

    public void setParams(String uuid) {
//        this.body = new AuthQRCodeRequestBody();
//        body.action = "authqrcode";
//        //这个留着
//        body.userid = CMAPI.getInstance().getBaseInfo().getAccount();
//        body.token = CMAPI.getInstance().getBaseInfo().getTicket();
//        body.uuid = uuid;
        this.bodyMap = new ConcurrentHashMap<>();
        put("action", "authqrcode");
        put("userid", CMAPI.getInstance().getBaseInfo().getAccount()); //登录名称
        put("token", CMAPI.getInstance().getBaseInfo().getTicket());
        put("uuid", uuid);
    }

//    @Override
//    public Observable<ResponseBody> structureObservable(Retrofit mRetrofit) {
//        V1AgApiService v1AgapiServcie = mRetrofit.create(V1AgApiService.class);
////        ObjectHelper.requireNonNull(this.body, "request body is null");
////        body.sig = getSignEncrypt();
////        bodyMap.put("sig", getSignEncrypt());
//        return v1AgapiServcie.request(getMap(), this.bodyMap);
//    }


}
