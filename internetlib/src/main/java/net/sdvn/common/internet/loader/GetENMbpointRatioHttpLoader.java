package net.sdvn.common.internet.loader;

import net.sdvn.common.internet.core.GsonBaseProtocol;
import net.sdvn.common.internet.core.V2AgApiHttpLoader;

import java.util.concurrent.ConcurrentHashMap;

public class GetENMbpointRatioHttpLoader extends V2AgApiHttpLoader {
    /**
     * 构造传递监听的接口，以及需要被解析的字节码文件
     *
     * @param parseClass
     */
    public GetENMbpointRatioHttpLoader(Class<? extends GsonBaseProtocol> parseClass) {
        super(parseClass);
        setAction("getenmbpointtratio");
    }

    public void setParams(String language, long unreadtime, int pos, int pagesize, String ticket) {
        //       "lg":"ch",
        //"unreadtime": 1590481847991,
        //"pos": 1,
        //"pagesize": 10
        this.bodyMap = new ConcurrentHashMap<>();
        put("ticket", ticket);
        put("lg", language);
        put("unreadtime", unreadtime * 1000);
        put("pos", pos);
        put("pagesize", pagesize);
    }
}
