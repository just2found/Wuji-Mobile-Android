package net.sdvn.common.internet.loader.scorepay;

import android.content.Context;

import net.sdvn.cmapi.CMAPI;
import net.sdvn.common.internet.core.GsonBaseProtocol;
import net.sdvn.common.internet.core.V2AgApiHttpLoader;

import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by LSW on 2020/2/26.
 * 积分消耗记录
 */

public class ScoreUseRecordHttpLoader extends V2AgApiHttpLoader {

    public ScoreUseRecordHttpLoader(Class<? extends GsonBaseProtocol> parseClass) {
        super(parseClass);
    }

    public void setParams(Context ctx, int pageNumber, int pageSize) {
        setAction("getmbpointspendbill");
        this.bodyMap = new ConcurrentHashMap<>();
        put("ticket", CMAPI.getInstance().getBaseInfo().getTicket());
        put("pageNumber", pageNumber);
        put("pageSize", pageSize);

        Locale curLocale = ctx.getResources().getConfiguration().locale;
        String language = curLocale.getLanguage();
        String country = curLocale.getCountry();//"CN""TW"
        String script = curLocale.getScript();
        if ("zh".equals(language)) {
            if ("cn".equals(country.toLowerCase()) && !"hant".equals(script.toLowerCase())) {
                put("lang", "zh");
            } else {
                put("lang", "tw");
            }
        } else {
            put("lang", language);
        }
    }
}
