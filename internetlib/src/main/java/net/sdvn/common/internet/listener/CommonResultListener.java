package net.sdvn.common.internet.listener;

import net.sdvn.common.internet.core.GsonBaseProtocol;

/**
 * Created by yun on 2018/3/27.
 */

public abstract class CommonResultListener<T extends GsonBaseProtocol> implements ResultListener<T> {
    @Override
    public void error(Object tag, GsonBaseProtocol baseProtocol) {

    }
}
