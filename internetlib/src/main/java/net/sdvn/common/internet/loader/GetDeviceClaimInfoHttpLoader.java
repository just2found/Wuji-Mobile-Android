package net.sdvn.common.internet.loader;

import android.content.Context;

import net.sdvn.cmapi.CMAPI;
import net.sdvn.common.internet.core.GsonBaseProtocol;
import net.sdvn.common.internet.core.V2AgApiHttpLoader;

import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;

import timber.log.Timber;

/**
 * Created by LSW on 2020/9/18.
 * 获取领取积分的的内容清单
 */

public class GetDeviceClaimInfoHttpLoader extends V2AgApiHttpLoader {

    public GetDeviceClaimInfoHttpLoader(Class<? extends GsonBaseProtocol> parseClass) {
        super(parseClass);
    }

    public void setParams(Context context, String deviceid) {
        setAction("getdeviceclaim");
        this.bodyMap = new ConcurrentHashMap<>();
        put("ticket", CMAPI.getInstance().getBaseInfo().getTicket());
        put("deviceid", deviceid);
        put("lang", getLanguage(context));
    }

    public String getLanguage(Context ctx) {
        Locale curLocale = ctx.getResources().getConfiguration().locale;
        String language = curLocale.getLanguage();
        String country = curLocale.getCountry();//"CN""TW"
        String script = curLocale.getScript();
        Timber.d("language : " + language + " country : " + country + " script : " + script);
        if ("zh".equals(language)) {
            if ("cn".equals(country.toLowerCase()) && !"hant".equals(script.toLowerCase())) {
                return "cn";
            } else {
                return "tw";
            }
        }
        return language;
    }

}
