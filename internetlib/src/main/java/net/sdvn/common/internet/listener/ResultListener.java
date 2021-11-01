package net.sdvn.common.internet.listener;


import androidx.annotation.Nullable;

import net.sdvn.common.internet.core.GsonBaseProtocol;

public interface ResultListener<T extends GsonBaseProtocol> extends BaseResultListener {
    void success(@Nullable Object tag, T data);//请求成功
}
