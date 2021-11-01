package net.sdvn.nascommon.model.oneos.api;



import net.sdvn.nascommon.constant.OneOSAPIs;
import net.sdvn.nascommon.model.http.OnHttpRequestListener;
import net.sdvn.nascommon.utils.EmptyUtils;
import net.sdvn.nascommon.utils.log.Logger;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * OneSpace OS Get Device Mac Address API
 * <p/>
 * Created by gaoyun@eli-tech.com on 2016/1/8.
 */
public class OneOSGetMacAPI extends BaseAPI {
    private static final String TAG = OneOSGetMacAPI.class.getSimpleName();

    private OnGetMacListener listener;

    public OneOSGetMacAPI(String ip, String port, boolean isHttp) {
        super(ip, OneOSAPIs.NET_GET_MAC, "infowire");
    }

    public void setOnGetMacListener(OnGetMacListener listener) {
        this.listener = listener;
    }

    public void getMac() {
        Map<String, Object> params = new HashMap<>();
        params.put("iface", "eth0");
        setParams(params);
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
                    String mac = null;
                    try {
                        mac = new JSONObject(result).getString("MacAddr");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    if (EmptyUtils.isEmpty(mac)) {
                        listener.onFailure(url, -1, "Response Mac Address is NULL");
                    } else {
                        mac = mac.toUpperCase();
                        listener.onSuccess(url, mac);
                    }
                }
            }

            @Override
            public void onFailure(String url, int httpCode, int errorNo, String strMsg) {
                if (listener != null) {
                    listener.onFailure(url, errorNo, strMsg);
                }
            }
        });

    }


    public interface OnGetMacListener {
        void onStart(String url);

        void onSuccess(String url, String mac);

        void onFailure(String url, int errorNo, String errorMsg);
    }
}
