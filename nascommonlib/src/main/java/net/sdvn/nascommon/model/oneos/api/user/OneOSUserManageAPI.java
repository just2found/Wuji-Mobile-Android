package net.sdvn.nascommon.model.oneos.api.user;

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
public class OneOSUserManageAPI extends BaseAPI {
    private static final String TAG = OneOSUserManageAPI.class.getSimpleName();

    private OnUserManageListener listener;
    @Nullable
    private String username = null;
    @Nullable
    private String method = null;

    public OneOSUserManageAPI(@NonNull LoginSession loginSession) {
        super(loginSession, OneOSAPIs.USER);
    }

    public OneOSUserManageAPI(String ip, String port, String session) {
        super(ip, OneOSAPIs.USER);
        oneOsRequest.getParams().setSession(session);
    }

    public void setOnUserManageListener(OnUserManageListener listener) {
        this.listener = listener;
    }

    private void manage(@Nullable Map<String, Object> params) {
        if (null == params) {
            params = new HashMap<>();
        }
//        url = genOneOSAPIUrl(OneOSAPIs.USER);
        params.put("username", username);
        setParams(params);
        setMethod(method);
        httpRequest.setParseResult(false);
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
                    try {
                        JSONObject json = new JSONObject(result);
                        boolean ret = json.getBoolean("result");
                        if (ret) {
                            listener.onSuccess(url, method);
                        } else {
                            JSONObject errJson = json.getJSONObject("error");
                            int errorNo = errJson.getInt("code");
                            String msg = errJson.getString("msg");
                            listener.onFailure(url, errorNo, msg);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        listener.onFailure(url, HttpErrorNo.ERR_JSON_EXCEPTION, context.getResources().getString(R.string.error_json_exception));
                    }
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

    public void add(String username, String password) {
        Map<String, Object> params = new HashMap<>();
        this.method = "add";
        this.username = username;
        params.put("password", password);
        manage(params);

    }

    public void add(String username, String password, boolean isAdmin) {
        Map<String, Object> params = new HashMap<>();
        this.method = "add";
        this.username = username;
        params.put("admin", isAdmin ? 1 : 0);
        params.put("password", password);
        manage(params);

    }

    public void delete(String username) {
        this.method = "delete";
        this.username = username;
        manage(null);

    }


    public void chpwd(String username, String password) {
        this.method = "update";
        this.username = username;
        Map<String, Object> params = new HashMap<>();
        params.put("password", password);
        manage(params);

    }

    public void chspace(String username, long space) {
        this.method = "space";
        this.username = username;
        Map<String, Object> params = new HashMap<>();
        params.put("space", space);
        manage(params);

    }


    public void addMarkName(String username, String markName) {
        this.method = "manage";
        this.username = username;
        Map<String, Object> params = new HashMap<>();
        params.put("cmd", "mark");
        params.put("mark", markName);
        manage(params);
    }

    public interface OnUserManageListener {
        void onStart(String url);

        void onSuccess(String url, String cmd);

        void onFailure(String url, int errorNo, String errorMsg);
    }
}
