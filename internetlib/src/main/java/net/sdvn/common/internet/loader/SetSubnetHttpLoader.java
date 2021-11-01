package net.sdvn.common.internet.loader;

import net.sdvn.cmapi.CMAPI;
import net.sdvn.common.internet.core.GsonBaseProtocol;
import net.sdvn.common.internet.core.V2AgApiHttpLoader;
import net.sdvn.common.internet.protocol.entity.SubnetEntity;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by yun on 2018/1/23.
 */

public class SetSubnetHttpLoader extends V2AgApiHttpLoader {
    public SetSubnetHttpLoader(Class<? extends GsonBaseProtocol> parseClass) {
        super(parseClass);
        setAction("setsubnet");
    }

    public void setParams(String deviceid, List<SubnetEntity> subnet) {
        this.bodyMap = new ConcurrentHashMap<>();
//        put("dappid", CMAPI.getInstance().getAppId());
        put("deviceid", deviceid);
        put("ticket", CMAPI.getInstance().getBaseInfo().getTicket());
        put("subnet", subnet);
    }

}
