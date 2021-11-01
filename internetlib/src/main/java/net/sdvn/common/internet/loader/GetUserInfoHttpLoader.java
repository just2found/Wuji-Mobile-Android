package net.sdvn.common.internet.loader;

import net.sdvn.cmapi.CMAPI;
import net.sdvn.common.internet.core.GsonBaseProtocol;
import net.sdvn.common.internet.core.V2AgApiHttpLoader;

import java.util.concurrent.ConcurrentHashMap;

public class GetUserInfoHttpLoader extends V2AgApiHttpLoader {

    public GetUserInfoHttpLoader(Class<? extends GsonBaseProtocol> parseClass) {
        super(parseClass);
        setAction("getuserinfo");
        this.bodyMap = new ConcurrentHashMap<>();
        put("ticket", CMAPI.getInstance().getBaseInfo().getTicket());
    }
}