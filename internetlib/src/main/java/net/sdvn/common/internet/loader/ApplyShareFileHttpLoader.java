package net.sdvn.common.internet.loader;

import net.sdvn.common.internet.core.GsonBaseProtocol;
import net.sdvn.common.internet.core.V2AgApiHttpLoader;
import net.sdvn.common.internet.protocol.entity.ShareFileBean;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by LSW on 2018/4/18.
 * 请求盘外分享文件
 */

public class ApplyShareFileHttpLoader extends V2AgApiHttpLoader {
    /**
     * 构造传递监听的接口，以及需要被解析的字节码文件
     *
     * @param parseClass
     */
    public ApplyShareFileHttpLoader(Class<? extends GsonBaseProtocol> parseClass) {
        super(parseClass);
        setAction("applysharefile");
    }

    public void setParams(String token, List<ShareFileBean> files, String deviceid,
                          String from, String to, int timeout) {
        this.bodyMap = new ConcurrentHashMap<>();
       put("token", token);
       put("files", files);
       put("deviceid", deviceid);
       put("from", from);
       put("to", to);
       put("timeout", timeout);
    }

}
