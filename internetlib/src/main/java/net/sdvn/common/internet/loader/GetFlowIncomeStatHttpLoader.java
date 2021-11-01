package net.sdvn.common.internet.loader;

import android.text.TextUtils;

import androidx.annotation.Nullable;

import net.sdvn.cmapi.CMAPI;
import net.sdvn.common.internet.core.GsonBaseProtocol;
import net.sdvn.common.internet.core.V2AgApiHttpLoader;

import java.util.concurrent.ConcurrentHashMap;

public class GetFlowIncomeStatHttpLoader extends V2AgApiHttpLoader {

    public GetFlowIncomeStatHttpLoader(Class<? extends GsonBaseProtocol> parseClass) {
        super(parseClass);
    }

    //{
    //"ticket":"563040147737735-e88046e5-e769-4f6f-a252-6bf5060156e3",
    //"deviceid":"561543696187404",
    //"peroid":"month",
    //"billdate":"",
    //"pageNumber":3,
    //"pageSize":10
    //}
    public void setParams(@Nullable String deviceid, String peroid, String billdate,
                          int pageNumber, int pageSize) {
        setAction("getflowincomestat");
        this.bodyMap = new ConcurrentHashMap<>();
        put("ticket", CMAPI.getInstance().getBaseInfo().getTicket());
        if (!TextUtils.isEmpty(deviceid))
            put("deviceid", deviceid);
        put("period", peroid);
        if (!TextUtils.isEmpty(billdate))
            put("billdate", billdate);
        put("pageNumber", pageNumber);
        put("pageSize", pageSize);
    }

    public void setParams2(@Nullable String deviceid, @Nullable String buserid, String peroid, String billdate,
                          int pageNumber, int pageSize) {
        setAction("getflowincomestat");
        this.bodyMap = new ConcurrentHashMap<>();
        put("ticket", CMAPI.getInstance().getBaseInfo().getTicket());
        if (!TextUtils.isEmpty(deviceid))
            put("deviceid", deviceid);
        put("period", peroid);
        if (!TextUtils.isEmpty(buserid)) put("buserid", buserid);//过滤用户
        if (!TextUtils.isEmpty(billdate))
            put("billdate", billdate);
        put("pageNumber", pageNumber);
        put("pageSize", pageSize);
    }

}
