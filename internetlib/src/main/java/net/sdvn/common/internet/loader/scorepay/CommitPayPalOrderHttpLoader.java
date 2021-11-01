package net.sdvn.common.internet.loader.scorepay;

import net.sdvn.cmapi.CMAPI;
import net.sdvn.common.internet.core.GsonBaseProtocol;
import net.sdvn.common.internet.core.V2AgApiHttpLoader;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by LSW on 2020/2/26.
 * 货币换算积分
 */

public class CommitPayPalOrderHttpLoader extends V2AgApiHttpLoader {

    public CommitPayPalOrderHttpLoader(Class<? extends GsonBaseProtocol> parseClass) {
        super(parseClass);
    }

    //ticket: 接口调用者登录中下发的ticket
    //transactionid: 交易单号
    //orderno: 客户订单号
    //paytype: 支付方式 24微信 13支付宝 31苹果IAP内购 Paypal(41)）
    //status: 订单状态(0:成功,1:失败)
    //}
    public void setParams(String transactionid, String orderno, int status) {
        setAction("paypalorder");
        this.bodyMap = new ConcurrentHashMap<>();
        put("ticket", CMAPI.getInstance().getBaseInfo().getTicket());
        put("transactionid", transactionid);
        put("orderno", orderno);
        put("paytype", 41);
        put("status", status);
    }
}
