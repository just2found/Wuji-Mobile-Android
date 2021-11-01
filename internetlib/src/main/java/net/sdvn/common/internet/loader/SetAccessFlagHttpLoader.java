package net.sdvn.common.internet.loader;

import net.sdvn.cmapi.CMAPI;
import net.sdvn.common.internet.core.GsonBaseProtocol;
import net.sdvn.common.internet.core.V2AgApiHttpLoader;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by yun on 2018/1/23.
 */

public class SetAccessFlagHttpLoader extends V2AgApiHttpLoader {
    public SetAccessFlagHttpLoader(Class<? extends GsonBaseProtocol> parseClass) {
        super(parseClass);
        setAction("setaccessflag");
    }

    public void setParams(String deviceid, boolean accessinternet, boolean accesssubnet) {
        this.bodyMap = new ConcurrentHashMap<>();
        put("ticket", CMAPI.getInstance().getBaseInfo().getTicket());
        put("deviceid", deviceid);
        put("accessinternet", accessinternet);
        put("accesssubnet", accesssubnet);
    }

}
