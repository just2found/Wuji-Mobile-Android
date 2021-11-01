package net.sdvn.common.internet.loader;

import androidx.annotation.Nullable;

import net.sdvn.cmapi.CMAPI;
import net.sdvn.common.internet.core.GsonBaseProtocol;
import net.sdvn.common.internet.core.V2AgApiHttpLoader;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by lsw on 2019/12/24.
 */

public class GetAccountInfoHttpLoader extends V2AgApiHttpLoader {
    public GetAccountInfoHttpLoader(Class<? extends GsonBaseProtocol> parseClass) {
        super(parseClass);
        setAction("getaccountinfo");
    }

    public void setParams(int flag, @Nullable List<String> vnodeid) {
        this.bodyMap = new ConcurrentHashMap<>();
        put("ticket", CMAPI.getInstance().getBaseInfo().getTicket());
        put("flag", flag);
        if (vnodeid == null)
            vnodeid = new ArrayList<>();
        put("vnodeid", vnodeid);
    }
}
