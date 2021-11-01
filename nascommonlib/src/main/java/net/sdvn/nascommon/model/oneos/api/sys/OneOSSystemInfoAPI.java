package net.sdvn.nascommon.model.oneos.api.sys;

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

import java.util.HashMap;
import java.util.Map;

/**
 * OneSpace OS Get Device Mac Address API
 * <p/>
 * Created by gaoyun@eli-tech.com on 2016/1/8.
 */
public class OneOSSystemInfoAPI extends BaseAPI {
    private static final String TAG = OneOSSystemInfoAPI.class.getSimpleName();

    private OnSystemInfoListener listener;
    @Nullable
    private String dev, name;

    public OneOSSystemInfoAPI(@NonNull LoginSession loginSession) {
        super(loginSession, OneOSAPIs.SYSTEM_SYS);
    }

    public void setOnSystemInfoListener(OnSystemInfoListener listener) {
        this.listener = listener;
    }

    private void info(Map<String, Object> params) {
//        url = genOneOSAPIUrl(OneOSAPIs.SYSTEM_SYS);
        setParams(params);
        setMethod("getinfo");
        httpRequest.setParseResult(false);
        httpRequest.post(oneOsRequest, new OnHttpRequestListener() {
            @Override
            public void onStart(String url) {
                if (listener != null) {
                    listener.onStart(url, dev, name);
                }
            }

            @Override
            public void onSuccess(String url, String result) {
                Logger.p(Logger.Level.DEBUG, Logger.Logd.OSAPI, TAG, "Response Data:" + result);
                if (listener != null) {
                    try {
                        JSONObject json = new JSONObject(result);
                        boolean ret = json.getBoolean("result");
                        if (ret) {
                            listener.onSuccess(url, dev, name, result);
                        } else {
                            // {"errno":-1,"msg":"list error","result":false}
                            json = json.getJSONObject("error");
                            int errorNo = json.getInt("code");
                            String msg = json.has("msg") ? json.getString("msg") : null;
                            listener.onFailure(url, dev, name, errorNo, msg);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        listener.onFailure(url, dev, name, HttpErrorNo.ERR_JSON_EXCEPTION, context.getResources().getString(R.string.error_json_exception));
                    }
                }
            }

            @Override
            public void onFailure(String url, int httpCode, int errorNo, String strMsg) {
                if (listener != null) {
                    listener.onFailure(url, dev, name, errorNo, strMsg);
                }
            }
        });
    }

    public void query(String dev, @Nullable String name) {
        this.dev = dev;
        this.name = name;
        Map<String, Object> params = new HashMap<>();
        params.put("dev", dev);
        if (null != name) {
            params.put("name", name);
        }
        info(params);
    }

    public interface OnSystemInfoListener {
        void onStart(String url, String dev, String name);

        void onSuccess(String url, String dev, String name, String result);

        void onFailure(String url, String dev, String name, int errorNo, String errorMsg);
    }
}
