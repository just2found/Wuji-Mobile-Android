package net.sdvn.nascommon.model.oneos.api.sys;

import androidx.annotation.NonNull;

import net.sdvn.nascommon.constant.OneOSAPIs;
import net.sdvn.nascommon.model.http.OnHttpRequestListener;
import net.sdvn.nascommon.model.oneos.api.BaseAPI;
import net.sdvn.nascommon.model.oneos.user.LoginSession;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class OneOSInstallUpdatePkgApi extends BaseAPI {
    public OneOSInstallUpdatePkgApi(@NonNull LoginSession loginSession) {
        super(loginSession, OneOSAPIs.SYSTEM_SYS);
    }

    public void install(String url, boolean online, OnHttpRequestListener listener) {
        setMethod("install");
        Map<String, Object> params = new ConcurrentHashMap<>();
        params.put("url", url);
        params.put("online", online);
        setParams(params);
        httpRequest.post(oneOsRequest, listener);
    }
}
