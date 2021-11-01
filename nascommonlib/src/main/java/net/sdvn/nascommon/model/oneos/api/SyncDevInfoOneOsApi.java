package net.sdvn.nascommon.model.oneos.api;

import androidx.annotation.NonNull;

import net.sdvn.nascommon.constant.OneOSAPIs;
import net.sdvn.nascommon.model.http.OnHttpRequestListener;
import net.sdvn.nascommon.model.oneos.user.LoginSession;

public class SyncDevInfoOneOsApi extends BaseAPI {
    public SyncDevInfoOneOsApi(@NonNull LoginSession loginSession) {
        super(loginSession, OneOSAPIs.SYSTEM_STAT, "sync_devinfo");
    }

    public void sync(OnHttpRequestListener listener) {
        httpRequest.post(oneOsRequest, listener);
    }
}
