package net.sdvn.nascommon.model.oneos.api.app;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import net.sdvn.nascommon.constant.HttpErrorNo;
import net.sdvn.nascommon.constant.OneOSAPIs;
import net.sdvn.nascommon.model.http.OnHttpRequestListener;
import net.sdvn.nascommon.model.oneos.api.BaseAPI;
import net.sdvn.nascommon.model.oneos.user.LoginSession;
import net.sdvn.nascommon.utils.log.Logger;
import net.sdvn.nascommonlib.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * OneSpace OS Manage App API
 * <p/>
 * Created by gaoyun@eli-tech.com on 2016/02/23.
 */
public class OneOSAppManageAPI extends BaseAPI {
    private static final String TAG = OneOSAppManageAPI.class.getSimpleName();

    private OnManagePluginListener listener;
    @Nullable
    private String method = null;

    public OneOSAppManageAPI(@NonNull LoginSession loginSession) {
        super(loginSession, OneOSAPIs.APP_API);
    }

    public void setOnManagePluginListener(OnManagePluginListener listener) {
        this.listener = listener;
    }

    public void state(String pack) {
        this.method = "stat";
        Map<String, Object> params = new ConcurrentHashMap<>();
        params.put("pack", pack);
        doManage(method, pack, params);
    }

    public void on(String pack) {
        this.method = "on";
        Map<String, Object> params = new ConcurrentHashMap<>();
        params.put("pack", pack);
        doManage(method, pack, params);

    }

    public void off(String pack) {
        this.method = "off";
        Map<String, Object> params = new ConcurrentHashMap<>();
        params.put("pack", pack);
        doManage(method, pack, params);
    }

    public void delete(String pack) {
        this.method = "delete";
        Map<String, Object> params = new ConcurrentHashMap<>();
        params.put("pack", pack);
        doManage(method, pack, params);
    }

    private void doManage(String method, final String pack, Map<String, Object> params) {
        setMethod(method);
        setParams(params);
        httpRequest.setOnHttpRequestListener(new OnHttpRequestListener() {
            @Override
            public void onStart(String url) {
                if (listener != null) {
                    listener.onStart(url);
                }
            }

            @Override
            public void onSuccess(String url, String result) {
                // super.onSuccess(result);
                Logger.p(Logger.Level.DEBUG, Logger.Logd.OSAPI, TAG, "Response Data:" + result);
                if (listener != null) {
                    try {
                        if ("stat".equals(OneOSAppManageAPI.this.method)) {
                            String state = new JSONObject(result).getString("stat");
                            listener.onSuccess(url, pack, OneOSAppManageAPI.this.method, "on".equalsIgnoreCase(state));
                        } else {
                            listener.onSuccess(url, pack, OneOSAppManageAPI.this.method, true);
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                        listener.onFailure(url, pack, HttpErrorNo.ERR_JSON_EXCEPTION, context.getResources().getString(R.string.error_json_exception));
                    }
                }
            }

            @Override
            public void onFailure(String url, int httpCode, int errCode, String errorMsg) {
                // super.onFailure(t, errorNo, strMsg);
                Logger.p(Logger.Level.ERROR, Logger.Logd.OSAPI, TAG, "Response Data: ErrorNo=" + errCode + " ; ErrorMsg=" + errorMsg);
                if (listener != null) {
                    listener.onFailure(url, pack, errCode, errorMsg);
                }
            }
        });
        httpRequest.postJson(oneOsRequest);


    }

    public interface OnManagePluginListener {
        void onStart(String url);

        void onSuccess(String url, String pack, String cmd, boolean ret);

        void onFailure(String url, String pack, int errorNo, String errorMsg);
    }
}
