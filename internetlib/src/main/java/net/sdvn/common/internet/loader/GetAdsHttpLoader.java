package net.sdvn.common.internet.loader;

import androidx.annotation.Nullable;

import net.sdvn.cmapi.CMAPI;
import net.sdvn.common.internet.core.GsonBaseProtocol;
import net.sdvn.common.internet.core.V2AgApiHttpLoader;

import java.util.concurrent.ConcurrentHashMap;

/**
 *  
 * <p>
 * Created by admin on 2020/7/30,17:27
 */
public class GetAdsHttpLoader extends V2AgApiHttpLoader {
    /**
     * 构造传递监听的接口，以及需要被解析的字节码文件
     *
     * @param parseClass
     */
    public GetAdsHttpLoader(Class<? extends GsonBaseProtocol> parseClass) {
        super(parseClass);
        setAction("getbanner");
    }

    public void setParams(@Nullable String type) {
        this.bodyMap = new ConcurrentHashMap<>();
        put("ticket", CMAPI.getInstance().getBaseInfo().getTicket());
        put("category", type);
    }


}
