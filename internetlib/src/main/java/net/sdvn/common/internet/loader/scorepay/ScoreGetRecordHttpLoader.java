package net.sdvn.common.internet.loader.scorepay;

import android.content.Context;

import net.sdvn.cmapi.CMAPI;
import net.sdvn.common.internet.core.GsonBaseProtocol;
import net.sdvn.common.internet.core.V2AgApiHttpLoader;
import net.sdvn.common.internet.utils.Utils;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by LSW on 2020/2/26.
 * 积分获取记录
 */

public class ScoreGetRecordHttpLoader extends V2AgApiHttpLoader {

    public ScoreGetRecordHttpLoader(Class<? extends GsonBaseProtocol> parseClass) {
        super(parseClass);
    }

    public void setParams(Context ctx, int pageNumber, int pageSize) {
        setAction("getmbpointgainbill");
        this.bodyMap = new ConcurrentHashMap<>();
        put("ticket", CMAPI.getInstance().getBaseInfo().getTicket());
        put("pageNumber", pageNumber);
        put("pageSize", pageSize);

        put("lang", Utils.getLanguage(ctx));
    }

}
