package net.sdvn.common.internet.loader;


import net.sdvn.cmapi.CMAPI;
import net.sdvn.common.internet.core.GsonBaseProtocol;
import net.sdvn.common.internet.core.V1AgApiHttpLoader;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by yun on 2018/1/23.
 */

public class BindDeviceHttpLoader extends V1AgApiHttpLoader {
//    private BindDeviceRequestBody body;

    /**
     * 构造传递监听的接口，以及需要被解析的字节码文件
     *
     * @param parseClass
     */
    public BindDeviceHttpLoader(Class<? extends GsonBaseProtocol> parseClass) {
        super(parseClass);
    }

    /**
     * @param type       {@link Type}
     *                   定方式 scan: 扫描二维码绑定  sharecode 输入分享码绑定
     *                   如果是scan方式，需要传入 deviceid
     *                   如果是sharecode方式，需传入 sharecode 分享码
     * @param device_sn  设备序列号
     * @param share_code 分享码
     * @param dappid
     */
    public void setParams(String type, String device_sn, String share_code, String dappid) {
//        this.body = new BindDeviceRequestBody();
        this.bodyMap = new ConcurrentHashMap<>();
        put("action", "binddevice");
        put("type", type);
        put("dappid", dappid);
        put("devicesn", device_sn);
        put("sharecode", share_code);
        put("token", CMAPI.getInstance().getBaseInfo().getTicket());
//        body.action = "binddevice";
//        body.type = type;
//        body.dappid = dappid;
//        body.devicesn = device_sn;
//        body.sharecode = share_code;
//        body.userid = CMAPI.getInstance().getBaseInfo().getUserId();
//        body.token = CMAPI.getInstance().getBaseInfo().getTicket();


    }

    public void setParams(List<String> userIds, String device_sn, String dappid) {
        this.bodyMap = new ConcurrentHashMap<>();
        put("action", "binddevices");
        put("userid", userIds);
        put("dappid", dappid);
        put("devicesn", device_sn);
        put("token", CMAPI.getInstance().getBaseInfo().getTicket());
    }

    public interface Type {
        String TYPE_SCAN = "scan";
        String TYPE_SHARE_CODE = "sharecode";
    }

//    @Override
//    public Observable<ResponseBody> structureObservable(Retrofit mRetrofit) {
//        V1AgApiService v1AgapiServcie = mRetrofit.create(V1AgApiService.class);
//        ObjectHelper.requireNonNull(this.body, "request body is null");
////        body.sig = getSignEncrypt();
//        return v1AgapiServcie.request(getMap(), this.body);
//    }

}
