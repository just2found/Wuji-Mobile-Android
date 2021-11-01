package net.sdvn.common.internet.loader;

import net.sdvn.cmapi.CMAPI;
import net.sdvn.common.internet.core.GsonBaseProtocol;
import net.sdvn.common.internet.core.V2AgApiHttpLoader;

import java.util.concurrent.ConcurrentHashMap;

public class ModifyNetWorkHttpLoader extends V2AgApiHttpLoader {

    public ModifyNetWorkHttpLoader(Class<? extends GsonBaseProtocol> parseClass) {
        super(parseClass);
    }

    public void setParams(String networkid, String networkname) {
        setAction("modifynetwork");
        this.bodyMap = new ConcurrentHashMap<>();
        put("ticket", CMAPI.getInstance().getBaseInfo().getTicket());
        put("networkid", networkid);
        put("networkname", networkname);
    }

}
