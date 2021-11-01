package net.sdvn.common.internet.loader;

import net.sdvn.cmapi.CMAPI;
import net.sdvn.common.internet.core.GsonBaseProtocol;
import net.sdvn.common.internet.core.V2AgApiHttpLoader;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by LSW on 2019/7/24.
 * 获取网络成员
 */

public class GetNetMembersHttpLoader extends V2AgApiHttpLoader {

    public GetNetMembersHttpLoader(Class<? extends GsonBaseProtocol> parseClass) {
        super(parseClass);
    }

    public void setParams(String networkid) {
        setAction("getmembers");
        this.bodyMap = new ConcurrentHashMap<>();
       put("ticket", CMAPI.getInstance().getBaseInfo().getTicket());
       put("networkid", networkid);
    }
}
