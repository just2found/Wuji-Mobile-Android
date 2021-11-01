package net.sdvn.common.internet.loader.scorepay;

import net.sdvn.cmapi.CMAPI;
import net.sdvn.common.internet.core.GsonBaseProtocol;
import net.sdvn.common.internet.core.V2AgApiHttpLoader;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by LSW on 2020/2/26.
 * 货币换算积分
 */

public class GetPayOrderHttpLoader extends V2AgApiHttpLoader {

    public GetPayOrderHttpLoader(Class<? extends GsonBaseProtocol> parseClass) {
        super(parseClass);
    }

    //{
    //  "ticket":"563040147737735-e88046e5-e769-4f6f-a252-6bf5060156e3",
    //  "productid":"xxxx",
    //  "sku":"xxx",
    //  "paytype":"13", //支付方式（微信（24）、支付宝（13））
    //  "amount": "1", //订单数量，缺省值为1
    //  "totalfee" : "xxxxxxxxxxxxxxx",    //支付金额
    //  "feetype" : "xxxxxxxxxxxxxxx",    //货币类型 RMB/USD
    //  "mbpoint" : "xxxxxxxxxxxxxxx",    //积分值
    //  "ordername":  "xxxxxxxxxxxxxxx"   //订单名称
    //}
    public void setParams1(String sku, String paytype, String totalfee, String feetype,
                          String mbpoint, String ordername, String amount) {
        setAction("applyorder");
        this.bodyMap = new ConcurrentHashMap<>();
        put("ticket", CMAPI.getInstance().getBaseInfo().getTicket());
        put("sku", sku);
        put("paytype", paytype);
        put("amount", amount);//购买数量，默认为1,当amountmode=1时则可更改为整数
        put("totalfee", totalfee);
        put("feetype", feetype);
        put("mbpoint", mbpoint);
        put("ordername", ordername);
    }

    public void setParams(String sku, String paytype, String totalfee, String feetype,
                          String mbpoint, String ordername, String payappid) {
        setAction("applyorder");
        this.bodyMap = new ConcurrentHashMap<>();
        put("ticket", CMAPI.getInstance().getBaseInfo().getTicket());
        put("sku", sku);
        put("paytype", paytype);
        put("amount", "1");
        put("totalfee", totalfee);
        put("feetype", feetype);
        put("mbpoint", mbpoint);
        put("ordername", ordername);
        put("payappid", payappid);
    }
}
