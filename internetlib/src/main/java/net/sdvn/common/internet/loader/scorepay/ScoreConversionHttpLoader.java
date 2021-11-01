package net.sdvn.common.internet.loader.scorepay;

import net.sdvn.cmapi.CMAPI;
import net.sdvn.common.internet.core.GsonBaseProtocol;
import net.sdvn.common.internet.core.V2AgApiHttpLoader;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by LSW on 2020/2/26.
 * 货币换算积分
 */

public class ScoreConversionHttpLoader extends V2AgApiHttpLoader {

    public ScoreConversionHttpLoader(Class<? extends GsonBaseProtocol> parseClass) {
        super(parseClass);
    }

    public void setParams(String currency) {
        setAction("currencytombpoint");
        this.bodyMap = new ConcurrentHashMap<>();
        put("ticket", CMAPI.getInstance().getBaseInfo().getTicket());
        put("currency", currency);
//        bodyMap.put("amount", amount);
    }
}
