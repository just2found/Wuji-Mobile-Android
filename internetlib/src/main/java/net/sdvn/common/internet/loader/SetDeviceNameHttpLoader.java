package net.sdvn.common.internet.loader;

import net.sdvn.cmapi.CMAPI;
import net.sdvn.common.internet.core.GsonBaseProtocol;
import net.sdvn.common.internet.core.V2AgApiHttpLoader;

import java.util.concurrent.ConcurrentHashMap;

public class SetDeviceNameHttpLoader extends V2AgApiHttpLoader {
    public SetDeviceNameHttpLoader(Class<? extends GsonBaseProtocol> parseClass) {
        super(parseClass);
    }

    public void setParams(String deviceid, String devicename) {
        setAction("setdevicename");
        this.bodyMap = new ConcurrentHashMap<>();
        put("deviceid", deviceid);
        put("devicename", devicename);
        put("ticket", CMAPI.getInstance().getBaseInfo().getTicket());
    }

}
