package net.sdvn.common.internet.loader;

import net.sdvn.cmapi.CMAPI;
import net.sdvn.common.internet.core.GsonBaseProtocol;
import net.sdvn.common.internet.core.V2AgApiHttpLoader;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by LSW on 2020/2/26.
 * 获取用户积分
 */

public class TransferScoreHttpLoader extends V2AgApiHttpLoader {

    public TransferScoreHttpLoader(Class<? extends GsonBaseProtocol> parseClass) {
        super(parseClass);
    }

    public void setParams(String transferin_userif, double mbpoint) {
        setAction("transfermbpoint");
        this.bodyMap = new ConcurrentHashMap<>();
        put("ticket", CMAPI.getInstance().getBaseInfo().getTicket());
        put("transferout_userid", CMAPI.getInstance().getBaseInfo().getUserId());
        put("transferin_userid", transferin_userif);
        put("mbpoint", mbpoint);
    }
}
