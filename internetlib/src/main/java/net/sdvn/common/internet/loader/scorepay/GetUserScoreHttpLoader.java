package net.sdvn.common.internet.loader.scorepay;

import net.sdvn.cmapi.CMAPI;
import net.sdvn.common.internet.core.GsonBaseProtocol;
import net.sdvn.common.internet.core.V2AgApiHttpLoader;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by LSW on 2020/2/26.
 * 获取用户积分
 */

public class GetUserScoreHttpLoader extends V2AgApiHttpLoader {

    public GetUserScoreHttpLoader(Class<? extends GsonBaseProtocol> parseClass) {
        super(parseClass);
        setAction("getusermbpoint");
        this.bodyMap = new ConcurrentHashMap<>();
        put("ticket", CMAPI.getInstance().getBaseInfo().getTicket());
    }

//    public void setParams(String networkname) {
//        setAction("applymbpointvnode");
//        this.bodyMap = new ConcurrentHashMap<>();
//        bodyMap.put("ticket", CMAPI.getInstance().getBaseInfo().getTicket());
//    }
}
