package net.sdvn.common.internet.listener;

import net.sdvn.common.internet.core.GsonBaseProtocol;

import java.util.List;

/**
 * Created by yun on 2018/3/30.
 */

public abstract class ListResultListener<T extends GsonBaseProtocol> implements ResultListener<T> {

    //请求成功
    public abstract void success(Object tag, List<T> results);

    //参数错误
    public abstract void error(Object tag, List<T> mErrorResults);

    @Override
    public void success(Object tag, T data) {

    }

    @Override
    public void error(Object tag, GsonBaseProtocol baseProtocol) {

    }
}
