package net.sdvn.common.internet.core;

/**
 *  
 * <p>
 * Created by admin on 2020/10/18,01:07
 */
public abstract class InitParamsV2AgApiHttpLoader extends V2AgApiHttpLoader {
    /**
     * 构造传递监听的接口，以及需要被解析的字节码文件
     *
     * @param parseClass
     */
    public InitParamsV2AgApiHttpLoader(Class<? extends GsonBaseProtocol> parseClass) {
        super(parseClass);
        initParams();
    }

    public abstract void initParams(Object... objs);

}
