package net.linkmate.app.base;

import net.linkmate.app.util.ToastUtils;
import net.sdvn.common.internet.core.GsonBaseProtocol;
import net.sdvn.common.internet.listener.ResultListener;

/**
 * Created by yun on 2018/3/27.
 */

public abstract class MyOkHttpListener<T extends GsonBaseProtocol> implements ResultListener<T> {
    @Override
    public void error(Object tag, GsonBaseProtocol baseProtocol) {
        ToastUtils.showError(baseProtocol.result, baseProtocol.errmsg);
    }
}
