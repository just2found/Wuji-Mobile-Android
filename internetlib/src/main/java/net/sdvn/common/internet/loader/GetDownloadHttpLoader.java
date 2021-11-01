package net.sdvn.common.internet.loader;

import androidx.annotation.Nullable;

import net.sdvn.common.internet.core.GsonBaseProtocol;
import net.sdvn.common.internet.core.V2AgApiHttpLoader;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by LSW on 2018/4/18.
 * 申请下载分享文件
 */

public class GetDownloadHttpLoader extends V2AgApiHttpLoader {
    /**
     * 构造传递监听的接口，以及需要被解析的字节码文件
     *
     * @param parseClass
     */
    public GetDownloadHttpLoader(Class<? extends GsonBaseProtocol> parseClass) {
        super(parseClass);
        setAction("getdownload");
    }

    public void setParams(String token, String sharetoken, @Nullable String deviceid) {
        this.bodyMap = new ConcurrentHashMap<>();
        put("token", token);
        put("sharetoken", sharetoken);
        if (deviceid != null && deviceid.length() > 0)
            put("deviceid", deviceid);
    }

}
