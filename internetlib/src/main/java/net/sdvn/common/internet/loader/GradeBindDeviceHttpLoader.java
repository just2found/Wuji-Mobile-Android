package net.sdvn.common.internet.loader;

import net.sdvn.cmapi.CMAPI;
import net.sdvn.common.internet.core.GsonBaseProtocol;
import net.sdvn.common.internet.core.V2AgApiHttpLoader;

import java.util.concurrent.ConcurrentHashMap;

public class GradeBindDeviceHttpLoader extends V2AgApiHttpLoader {

    public GradeBindDeviceHttpLoader(Class<? extends GsonBaseProtocol> parseClass) {
        super(parseClass);
    }

    public void setParams(String userid, String deviceid, int mgrlevel) {
        setAction("gradebinddevice");
        this.bodyMap = new ConcurrentHashMap<>();
        put("ticket", CMAPI.getInstance().getBaseInfo().getTicket());
        put("userid", userid);
        put("deviceid", deviceid);
        put("mgrlevel", mgrlevel);
    }
}
