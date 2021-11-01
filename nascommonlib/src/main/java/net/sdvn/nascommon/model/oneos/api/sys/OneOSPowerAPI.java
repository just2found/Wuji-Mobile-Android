package net.sdvn.nascommon.model.oneos.api.sys;

import androidx.annotation.NonNull;

import net.sdvn.nascommon.constant.OneOSAPIs;
import net.sdvn.nascommon.model.http.OnHttpRequestListener;
import net.sdvn.nascommon.model.oneos.api.BaseAPI;
import net.sdvn.nascommon.model.oneos.user.LoginSession;
import net.sdvn.nascommon.utils.log.Logger;

import java.util.HashMap;
import java.util.Map;

/**
 * OneSpace Device Power Control API
 * <p/>
 * Created by gaoyun@eli-tech.com on 2016/1/8.
 */
public class OneOSPowerAPI extends BaseAPI {
    private static final String TAG = OneOSPowerAPI.class.getSimpleName();

    private OnPowerListener listener;

    public OneOSPowerAPI(@NonNull LoginSession loginSession) {
        super(loginSession, OneOSAPIs.SYSTEM_SYS);
    }

    public void setOnPowerListener(OnPowerListener listener) {
        this.listener = listener;
    }

    public void power(final boolean isPowerOff) {
//        url = genOneOSAPIUrl(OneOSAPIs.SYSTEM_SYS);
//        Logger.p(Level.DEBUG, Logd.DEBUG, TAG, "Power OneSpace: " + url);
        String method = isPowerOff ? "halt" : "reboot";
        Map<String, Object> params = new HashMap<>();
        setMethod(method);
        httpRequest.post(oneOsRequest, new OnHttpRequestListener() {
            @Override
            public void onStart(String url) {
                if (listener != null) {
                    listener.onStart(url);
                }
            }

            @Override
            public void onSuccess(String url, String result) {
                Logger.p(Logger.Level.DEBUG, Logger.Logd.OSAPI, TAG, "Response Data:" + result);
                if (listener != null) {
                    listener.onSuccess(url, isPowerOff);
                }
            }

            @Override
            public void onFailure(String url, int httpCode, int errorNo, String strMsg) {
                Logger.LOGE(TAG, "Response Data: " + errorNo + " : " + strMsg);
                if (listener != null) {
                    listener.onFailure(url, errorNo, strMsg);
                }
            }
        });


    }

    public interface OnPowerListener {
        void onStart(String url);

        void onSuccess(String url, boolean isPowerOff);

        void onFailure(String url, int errorNo, String errorMsg);
    }
}
