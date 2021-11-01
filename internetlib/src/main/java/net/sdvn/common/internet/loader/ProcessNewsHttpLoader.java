package net.sdvn.common.internet.loader;


import net.sdvn.cmapi.CMAPI;
import net.sdvn.common.internet.core.GsonBaseProtocol;
import net.sdvn.common.internet.core.V2AgApiHttpLoader;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by yun on 2018/1/23.
 */

public class ProcessNewsHttpLoader extends V2AgApiHttpLoader {
    public ProcessNewsHttpLoader(Class<? extends GsonBaseProtocol> parseClass) {
        super(parseClass);
        setAction("processnews");
    }

    //    {"result":305,"process":"1","errmsg":"News has been processed."}
    public void setParams(String newsid, String process) {
        this.bodyMap = new ConcurrentHashMap<>();
        put("newsid", newsid);
        put("process", process);
        put("ticket", CMAPI.getInstance().getBaseInfo().getTicket());
    }

}
