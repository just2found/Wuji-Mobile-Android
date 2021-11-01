package net.sdvn.common.internet.loader;


import net.sdvn.cmapi.CMAPI;
import net.sdvn.common.internet.core.GsonBaseProtocol;
import net.sdvn.common.internet.core.V2AgApiHttpLoader;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by yun on 2018/1/23.
 */

public class BindNetworkHttpLoader extends V2AgApiHttpLoader {
    public BindNetworkHttpLoader(Class<? extends GsonBaseProtocol> parseClass) {
        super(parseClass);
    }

    public void setParams(String sharecode) {
        setAction("scansharenetwork");
        this.bodyMap = new ConcurrentHashMap<>();
        put("sharecode", sharecode);
        put("ticket", CMAPI.getInstance().getBaseInfo().getTicket());
    }

}
