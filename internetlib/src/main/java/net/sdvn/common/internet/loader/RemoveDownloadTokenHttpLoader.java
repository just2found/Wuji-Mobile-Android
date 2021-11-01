package net.sdvn.common.internet.loader;

import net.sdvn.common.internet.core.GsonBaseProtocol;
import net.sdvn.common.internet.core.V2AgApiHttpLoader;

import java.util.concurrent.ConcurrentHashMap;

public class RemoveDownloadTokenHttpLoader extends V2AgApiHttpLoader {

    /**
     * 构造传递监听的接口，以及需要被解析的字节码文件
     *
     * @param parseClass
     */
    public RemoveDownloadTokenHttpLoader(Class<? extends GsonBaseProtocol> parseClass) {
        super(parseClass);
        setAction("removedownloadtoken");
    }

    public void setParams(String token, String downloadtoken) {
        this.bodyMap = new ConcurrentHashMap<>();
        put("token", token);
        put("downloadtoken", downloadtoken);
    }

}