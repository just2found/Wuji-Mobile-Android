package net.sdvn.common.internet.loader;


import androidx.annotation.Nullable;

import net.sdvn.cmapi.CMAPI;
import net.sdvn.common.internet.core.GsonBaseProtocol;
import net.sdvn.common.internet.core.V2AgApiHttpLoader;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by yun on 2018/3/19.
 * <p>
 * 举例: https://app.memenet.net:8445/v2/agapi/subscribedevice?partid=xxxx&appid=xxxxxxx
 * 说明	请求绑定源设备
 * 请求包体
 * <p>
 * {
 * "t2":"d9wc90dfyagd-d8d",
 * "ticket":"563040147737735-e88046e5-e769-4f6f-a252-6bf5060156e3",
 * "touserid":"5630186728980243"
 * }
 * 说明：
 * t2: t2
 * ticket: 接口调用者登录中下发的ticket
 * touserid: 指定用户分享的userid (设备端上请求绑定时，如果是指定用户分享才需要传递)
 * 返回包体:
 * <p>
 * {
 * "result":0,
 * "errmsg":"success",
 * "data":{
 * "t1":"",
 * "deviceid":""
 * }
 * }
 * 说明:
 * t1: 代表分享文件的标识T1
 * deviceid: 源设备id
 * <p>
 * : https://app.memenet.net:8445/v2/agapi/cancelsubscribedevice?partid=xxxx&appid=xxxxxxx
 * 说明	请求取消绑定源设备
 * 请求包体
 * <p>
 * {
 * "t2":"d9wc90dfyagd-d8d",
 * "ticket":"563040147737735-e88046e5-e769-4f6f-a252-6bf5060156e3",
 * "deviceid":""
 * }
 * 说明：
 * t2: 与t1匹配的分享标识T2
 * ticket: 接口调用者登录中下发的ticket
 * deviceid: 源设备device id
 * 返回包体:
 * <p>
 * {
 * "result":0,
 * "errmsg":"success"
 * }
 */

public class SubscribeDeviceHttpLoader extends V2AgApiHttpLoader {

    /**
     * 构造传递监听的接口，以及需要被解析的字节码文件
     *
     * @param parseClass
     */
    public SubscribeDeviceHttpLoader(Class<? extends GsonBaseProtocol> parseClass) {
        super(parseClass);
    }

    public void subscribe(String t2, @Nullable String touserId) {
        setAction("subscribedevice");
        this.bodyMap = new ConcurrentHashMap<>();
        put("t2", t2);
        put("ticket", CMAPI.getInstance().getBaseInfo().getTicket());
        if (touserId != null && touserId.length() > 0)
            put("touserid", touserId);
    }

    public void unsubscribe(String t2, @Nullable String deviceId) {
        setAction("cancelsubscribedevice");
        this.bodyMap = new ConcurrentHashMap<>();
        put("t2", t2);
        put("ticket", CMAPI.getInstance().getBaseInfo().getTicket());
        if (deviceId != null && deviceId.length() > 0)
            put("deviceid", deviceId);
    }
}
