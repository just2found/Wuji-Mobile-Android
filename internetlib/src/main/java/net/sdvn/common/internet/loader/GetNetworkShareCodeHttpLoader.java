package net.sdvn.common.internet.loader;


import net.sdvn.cmapi.CMAPI;
import net.sdvn.common.internet.core.GsonBaseProtocol;
import net.sdvn.common.internet.core.V2AgApiHttpLoader;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by yun on 2018/1/24.
 */

public class GetNetworkShareCodeHttpLoader extends V2AgApiHttpLoader {

    /**
     * 构造传递监听的接口，以及需要被解析的字节码文件
     *
     * @param parseClass
     */
    public GetNetworkShareCodeHttpLoader(Class<? extends GsonBaseProtocol> parseClass) {
        super(parseClass);
    }


    public void setParams(String networkid) {
        setAction("applysharenetwork");
        bodyMap = new ConcurrentHashMap<>();
        put("networkid", networkid);
        put("ticket", CMAPI.getInstance().getBaseInfo().getTicket());
    }

}
