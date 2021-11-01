package net.sdvn.common.internet.core;

import androidx.annotation.Keep;
import androidx.annotation.Nullable;

@Keep
public class GsonBaseProtocolV2<T> extends GsonBaseProtocol {

    @Nullable
    public T data;

}
