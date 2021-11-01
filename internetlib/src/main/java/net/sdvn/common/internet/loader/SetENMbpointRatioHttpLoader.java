package net.sdvn.common.internet.loader;

import net.sdvn.cmapi.CMAPI;
import net.sdvn.common.internet.core.GsonBaseProtocol;
import net.sdvn.common.internet.core.V2AgApiHttpLoader;

import java.util.concurrent.ConcurrentHashMap;

public class SetENMbpointRatioHttpLoader extends V2AgApiHttpLoader {
    /**
     * 构造传递监听的接口，以及需要被解析的字节码文件
     *
     * @param parseClass
     */
    public SetENMbpointRatioHttpLoader(Class<? extends GsonBaseProtocol> parseClass) {
        super(parseClass);
        setAction("setenmbpointtratio");
    }

    public void setParams(String deviceid, String ratio, Integer delaytime, Boolean isCancel) {
//        "ticket":"563040147737735-e88046e5-e769-4f6f-a252-6bf5060156e3",
//        "deviceid":"561543696187404",
//                "mbpointtratio":"2GB",
//                "delaytime": 100,
//                "cancel": true
        this.bodyMap = new ConcurrentHashMap<>();
        put("ticket", CMAPI.getInstance().getBaseInfo().getTicket());
        put("deviceid", deviceid);
        put("mbpointratio", ratio);
        put("delaytime", delaytime);
        if (isCancel)
            put("cancel", isCancel);
    }

//    gb2cratio

    public void setParams(String deviceid, Float ratio, Integer delaytime, Boolean isCancel) {
//        "ticket":"563040147737735-e88046e5-e769-4f6f-a252-6bf5060156e3",
//        "deviceid":"561543696187404",
//                "gb2cratio":0.34,
//                "delaytime": 100,
//                "cancel": true
        this.bodyMap = new ConcurrentHashMap<>();
        put("ticket", CMAPI.getInstance().getBaseInfo().getTicket());
        put("deviceid", deviceid);
        if (ratio != null) {
            put("gb2cratio", ratio);
        }
        put("delaytime", delaytime);
        if (isCancel)
            put("cancel", isCancel);
    }
}
