package net.sdvn.common.internet.loader;

import net.sdvn.common.internet.core.GsonBaseProtocol;
import net.sdvn.common.internet.core.V2AgApiHttpLoader;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by LSW on 2018/4/18.
 * 获取文件分享列表
 */

public class GetShareFilesHttpLoader extends V2AgApiHttpLoader {
    /**
     * 构造传递监听的接口，以及需要被解析的字节码文件
     *
     * @param parseClass
     */
    public GetShareFilesHttpLoader(Class<? extends GsonBaseProtocol> parseClass) {
        super(parseClass);
        setAction("getsharefiles");
    }

    public void setParams(String token) {
        this.bodyMap = new ConcurrentHashMap<>();
        put("token", token);
    }

}
