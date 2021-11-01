package net.sdvn.common.internet.loader;

import android.text.TextUtils;

import net.sdvn.cmapi.CMAPI;
import net.sdvn.common.internet.core.GsonBaseProtocol;
import net.sdvn.common.internet.core.V2AgApiHttpLoader;

import java.util.concurrent.ConcurrentHashMap;

public class GetScoreStatHttpLoader extends V2AgApiHttpLoader {

    public GetScoreStatHttpLoader(Class<? extends GsonBaseProtocol> parseClass) {
        super(parseClass);
    }

    public void setParams(String peroid, String billdate,
                          int pageNumber, int pageSize) {
        setAction("getmbpointstat");
        this.bodyMap = new ConcurrentHashMap<>();
        put("ticket", CMAPI.getInstance().getBaseInfo().getTicket());
        put("period", peroid);
        if (!TextUtils.isEmpty(billdate))
            put("billdate", billdate);
        put("pageNumber", pageNumber);
        put("pageSize", pageSize);
    }

}
