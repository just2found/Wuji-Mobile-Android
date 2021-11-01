package net.sdvn.common.internet.loader;

import androidx.annotation.NonNull;

import net.sdvn.cmapi.CMAPI;
import net.sdvn.common.internet.core.GsonBaseProtocol;
import net.sdvn.common.internet.core.V2AgApiHttpLoader;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by LSW on 2019/7/24.
 * 删除网络成员
 */

public class RemoveNetMembersHttpLoader extends V2AgApiHttpLoader {

    public RemoveNetMembersHttpLoader(Class<? extends GsonBaseProtocol> parseClass) {
        super(parseClass);
    }

    public void setParams(@NonNull String networkid, @NonNull List<String> userids) {
        setAction("removemembers");
        this.bodyMap = new ConcurrentHashMap<>();
        String ticket = CMAPI.getInstance().getBaseInfo().getTicket();
        put("ticket", ticket);
        put("networkid", networkid);
        put("members", userids);
    }

}
