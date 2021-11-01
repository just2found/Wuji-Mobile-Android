package net.sdvn.common.internet.loader;

import net.sdvn.cmapi.CMAPI;
import net.sdvn.common.internet.core.GsonBaseProtocol;
import net.sdvn.common.internet.core.V2AgApiHttpLoader;

import java.util.concurrent.ConcurrentHashMap;


/**
 * Created by LSW on 2020/9/18.
 * 领取积分
 */

public class ClaimDeviceHttpLoader extends V2AgApiHttpLoader {

    public ClaimDeviceHttpLoader(Class<? extends GsonBaseProtocol> parseClass) {
        super(parseClass);
    }

    public void setParams(String deviceid) {
        setAction("claimdevice");
        this.bodyMap = new ConcurrentHashMap<>();
        put("ticket", CMAPI.getInstance().getBaseInfo().getTicket());
        put("deviceid", deviceid);
    }
}
