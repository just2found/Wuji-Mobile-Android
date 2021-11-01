package net.linkmate.app.util.business;

import androidx.annotation.Nullable;

import net.sdvn.common.internet.core.HttpLoader;
import net.sdvn.common.internet.core.V2AgApiHttpLoader;
import net.sdvn.common.internet.listener.ResultListener;
import net.sdvn.common.internet.loader.GetAccountInfoHttpLoader;
import net.sdvn.common.internet.protocol.AccountPrivilegeInfo;

import java.util.List;

public class PrivilegeUtil {
    private static final int VIRTUAL_NET_SERVICE_INFO = 0x1;
    private static final int VIRTUAL_NODE_INFO = 0x2;
    private static final int DEVICE_USED_INFO = 0x4;

    public static V2AgApiHttpLoader getVNetServiceInfo(HttpLoader.HttpLoaderStateListener stateListener,
                                          ResultListener<AccountPrivilegeInfo> resultListener) {
        return getAccountInfo(VIRTUAL_NET_SERVICE_INFO, null, stateListener, resultListener);
    }

    public static V2AgApiHttpLoader getVNodeInfo(@Nullable List<String> vnodeid,
                                    HttpLoader.HttpLoaderStateListener stateListener,
                                    ResultListener<AccountPrivilegeInfo> resultListener) {
        return getAccountInfo(VIRTUAL_NODE_INFO, vnodeid, stateListener, resultListener);
    }

    public static V2AgApiHttpLoader getDeviceUsedInfo(HttpLoader.HttpLoaderStateListener stateListener,
                                          ResultListener<AccountPrivilegeInfo> resultListener) {
        return getAccountInfo(DEVICE_USED_INFO, null, stateListener, resultListener);
    }

    public static V2AgApiHttpLoader getAccountInfo(HttpLoader.HttpLoaderStateListener stateListener,
                                      ResultListener<AccountPrivilegeInfo> resultListener) {
        return getAccountInfo(VIRTUAL_NET_SERVICE_INFO | VIRTUAL_NODE_INFO | DEVICE_USED_INFO,
                null, stateListener, resultListener);
    }

    private static V2AgApiHttpLoader getAccountInfo(int flag, @Nullable List<String> vnodeid,
                                                    HttpLoader.HttpLoaderStateListener stateListener,
                                                    ResultListener<AccountPrivilegeInfo> resultListener) {
        GetAccountInfoHttpLoader httpLoader = new GetAccountInfoHttpLoader(AccountPrivilegeInfo.class);
        httpLoader.setParams(flag, vnodeid);
        httpLoader.setHttpLoaderStateListener(stateListener);
        httpLoader.executor(resultListener);
        return httpLoader;
    }
}
