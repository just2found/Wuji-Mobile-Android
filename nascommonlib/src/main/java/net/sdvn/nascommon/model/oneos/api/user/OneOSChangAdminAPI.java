package net.sdvn.nascommon.model.oneos.api.user;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import net.sdvn.nascommon.constant.OneOSAPIs;
import net.sdvn.nascommon.model.http.OnHttpRequestListener;
import net.sdvn.nascommon.model.oneos.api.BaseAPI;
import net.sdvn.nascommon.model.oneos.user.LoginSession;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by yun on 2018/3/27.
 */

public class OneOSChangAdminAPI extends BaseAPI {
    public OneOSChangAdminAPI(@NonNull LoginSession loginSession) {
        super(loginSession, OneOSAPIs.USER);
    }

    public void changAdmin(final String username, @Nullable final OnChangAdminListener callback) {
        Map<String, Object> params = new ConcurrentHashMap<>();
        params.put("username", username);
        setMethod("admin");
        setParams(params);
        httpRequest.setOnHttpRequestListener(new OnHttpRequestListener() {
            @Override
            public void onStart(String url) {
                if (callback != null)
                    callback.onStart(url);
            }

            @Override
            public void onSuccess(String url, String result) {
                if (callback != null) {
                    callback.onSuccess(url);
                }
            }

            @Override
            public void onFailure(String url, int httpCode, int errCode, String errorMsg) {
                if (callback != null)
                    callback.onFailure(url, errCode, errorMsg);
            }
        });
        httpRequest.postJson(oneOsRequest);

    }

    public interface OnChangAdminListener {
        void onStart(String url);

        void onSuccess(String url);

        void onFailure(String url, int errorNo, String errorMsg);
    }
}
