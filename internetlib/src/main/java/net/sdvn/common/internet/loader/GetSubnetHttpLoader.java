package net.sdvn.common.internet.loader;

import net.sdvn.cmapi.CMAPI;
import net.sdvn.common.internet.core.GsonBaseProtocol;
import net.sdvn.common.internet.core.V2AgApiHttpLoader;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by yun on 2018/1/23.
 */

public class GetSubnetHttpLoader extends V2AgApiHttpLoader {
    public GetSubnetHttpLoader(Class<? extends GsonBaseProtocol> parseClass) {
        super(parseClass);
        setAction("getsubnet");
    }

    public void setParams(String deviceid) {
        this.bodyMap = new ConcurrentHashMap<>();
//        put("dappid", CMAPI.getInstance().getAppId());
        put("deviceid", deviceid);
        put("ticket", CMAPI.getInstance().getBaseInfo().getTicket());
    }
}
