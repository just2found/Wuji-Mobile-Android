package net.sdvn.common.internet.loader;


import androidx.annotation.NonNull;

import net.sdvn.cmapi.CMAPI;
import net.sdvn.common.internet.core.GsonBaseProtocol;
import net.sdvn.common.internet.core.V2AgApiHttpLoader;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by yun on 2018/1/23.
 */

public class V2BindDevicesHttpLoader extends V2AgApiHttpLoader {
    /**
     * 构造传递监听的接口，以及需要被解析的字节码文件
     *
     * @param parseClass
     */
    public V2BindDevicesHttpLoader(Class<? extends GsonBaseProtocol> parseClass) {
        super(parseClass);
        setAction("binddevices");
    }

    public void setParams(@NonNull List<String> userIds, String device_sn, String dappid) {
        this.bodyMap = new ConcurrentHashMap<>();
        setAction("binddevices");
        put("action", "binddevices");
        put("userid", userIds.toArray());
        put("dappid", dappid);
        put("devicesn", device_sn);
        put("token", CMAPI.getInstance().getBaseInfo().getTicket());
    }

    /**
     * @param type       {Type}
     *                   定方式 scan: 扫描二维码绑定  sharecode 输入分享码绑定
     *                   如果是scan方式，需要传入 deviceid
     *                   如果是sharecode方式，需传入 sharecode 分享码
     * @param device_sn  设备序列号
     * @param share_code 分享码
     * @param dappid
     */
    public void setParams(String type, String device_sn, String share_code, String dappid) {
        this.bodyMap = new ConcurrentHashMap();
        setAction("binddevice");
//        put("action", "binddevice");
        put("type", type);
        put("dappid", dappid);
        put("devicesn", device_sn);
        put("sharecode", share_code);
        put("ticket", CMAPI.getInstance().getBaseInfo().getTicket());
    }
}
