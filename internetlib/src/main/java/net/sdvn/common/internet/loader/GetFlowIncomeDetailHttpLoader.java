package net.sdvn.common.internet.loader;

import android.text.TextUtils;

import androidx.annotation.Nullable;

import net.sdvn.cmapi.CMAPI;
import net.sdvn.common.internet.core.GsonBaseProtocol;
import net.sdvn.common.internet.core.V2AgApiHttpLoader;

import java.util.concurrent.ConcurrentHashMap;

public class GetFlowIncomeDetailHttpLoader extends V2AgApiHttpLoader {

    public GetFlowIncomeDetailHttpLoader(Class<? extends GsonBaseProtocol> parseClass) {
        super(parseClass);
    }

    //{
    //"ticket":"563040147737735-e88046e5-e769-4f6f-a252-6bf5060156e3",
    //"deviceid":"561543696187404",
    //"billdate":"9652141223112",
    //"pageNumber":3,
    //"pageSize":10
    //}
    public void setParams(@Nullable String deviceid, String billdate,
                          int pageNumber, int pageSize) {
        setAction("getflowincomedetail");
        this.bodyMap = new ConcurrentHashMap<>();
        put("ticket", CMAPI.getInstance().getBaseInfo().getTicket());
        if (deviceid != null)
            put("deviceid", deviceid);
        put("billdate", billdate);
        put("pageNumber", pageNumber);
        put("pageSize", pageSize);
    }
    public void setParams2(@Nullable String deviceid, @Nullable String buserid, String billdate,
                          int pageNumber, int pageSize) {
        setAction("getflowincomedetail");
        this.bodyMap = new ConcurrentHashMap<>();
        put("ticket", CMAPI.getInstance().getBaseInfo().getTicket());
        if (deviceid != null)
            put("deviceid", deviceid);
        if (!TextUtils.isEmpty(buserid)) put("buserid", buserid);//过滤用户
        put("billdate", billdate);
        put("pageNumber", pageNumber);
        put("pageSize", pageSize);
    }

}
