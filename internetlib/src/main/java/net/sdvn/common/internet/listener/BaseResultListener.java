package net.sdvn.common.internet.listener;

import androidx.annotation.Nullable;

import net.sdvn.common.internet.core.GsonBaseProtocol;

/**
 * Created by yun on 18/04/23.
 */

public interface BaseResultListener {
    void error(@Nullable Object tag, GsonBaseProtocol baseProtocol);//参数错误
}
