package net.sdvn.common.internet.loader.scorepay;

import net.sdvn.cmapi.CMAPI;
import net.sdvn.common.internet.core.GsonBaseProtocol;
import net.sdvn.common.internet.core.V2AgApiHttpLoader;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by LSW on 2020/2/26.
 * 用户充值记录
 */

public class UserRechargeRecordHttpLoader extends V2AgApiHttpLoader {

    public UserRechargeRecordHttpLoader(Class<? extends GsonBaseProtocol> parseClass) {
        super(parseClass);
    }

    public void setParams(int pageNumber, int pageSize) {
        setAction("getuserorderbill");
        this.bodyMap = new ConcurrentHashMap<>();
        put("ticket", CMAPI.getInstance().getBaseInfo().getTicket());
        put("pageNumber", pageNumber);
        put("pageSize", pageSize);
    }
}
