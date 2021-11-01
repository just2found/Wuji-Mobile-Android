package net.sdvn.common.internet.loader;


import android.content.Context;
import android.text.TextUtils;

import androidx.annotation.NonNull;

import net.sdvn.cmapi.CMAPI;
import net.sdvn.common.internet.core.GsonBaseProtocol;
import net.sdvn.common.internet.core.V2AgApiHttpLoader;

import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by yun on 2018/1/23.
 */

public class GetNewsHttpLoader extends V2AgApiHttpLoader {
    public GetNewsHttpLoader(Class<? extends GsonBaseProtocol> parseClass) {
        super(parseClass);
        setAction("getnews");
    }

    public void setParams(@NonNull Context context, String messageNewDate, long timestamp) {

        this.bodyMap = new ConcurrentHashMap<>();
        Locale curLocale = context.getResources().getConfiguration().locale;
        String language = curLocale.getLanguage();
        String country = curLocale.getCountry();//"CN""TW"
        if ("zh".equals(language)) {
            put("lang", "cn");
        } else {
            put("lang", "en");
        }
        if (timestamp != -1) {
            put("timestamp", Long.toString(timestamp));
        } else if (!TextUtils.isEmpty(messageNewDate)) {
            put("date", messageNewDate);
        }
        put("ticket", CMAPI.getInstance().getBaseInfo().getTicket());
    }

}
