package net.sdvn.nascommon.model.oneos.api.share;

import androidx.annotation.NonNull;

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

public class OneOSNotifyShareFileAPI extends BaseAPI {
    private static final String TAG = OneOSNotifyShareFileAPI.class.getSimpleName();

    private NotifyDownloadListener listener;

    public OneOSNotifyShareFileAPI(@NonNull LoginSession loginSession) {
        super(loginSession, OneOSAPIs.SHARE_API);
    }

    public void setResultListener(NotifyDownloadListener listener) {
        this.listener = listener;
    }

    public void download(String shareToken, String downloadToken, String sourceDevId, String name, String username, String todir) {
//        url = genOneOSAPIUrl(OneOSAPIs.SHARE_API);
        Map<String, Object> params = new HashMap<>();
//        params.put("session", session);
        params.put("token", downloadToken);
        params.put("share_token", shareToken);
        params.put("deviceid", sourceDevId);
        params.put("todir", todir);
        params.put("name", name);
        params.put("username", username);
        setParams(params);
        setMethod("notify");
        httpRequest.setParseResult(false);
        httpRequest.post(oneOsRequest, onHttpListener);
//        httpUtils.postJson(url, new RequestBody("notify", session, params), onHttpListener);
    }

    public void delete(String... downloadToken) {
//        url = genOneOSAPIUrl(OneOSAPIs.SHARE_API);
        Map<String, Object> params = new HashMap<>();
//        params.put("session", session);
        params.put("cmd", "down");
        params.put("token", (downloadToken));
        setMethod("delete");
        setParams(params);
        httpRequest.setParseResult(false);
        httpRequest.post(oneOsRequest, onHttpListener);
//        httpUtils.postJson(url, new RequestBody("delete", session, params), onHttpListener);
    }

    public void pause(String downloadToken) {
//        url = genOneOSAPIUrl(OneOSAPIs.SHARE_API);
        Map<String, Object> params = new HashMap<>();
//        params.put("session", session);
        params.put("cmd", "down");
        params.put("token", downloadToken);
        setMethod("pause");
        setParams(params);
        httpRequest.setParseResult(false);
        httpRequest.post(oneOsRequest, onHttpListener);
//        httpUtils.postJson(url, new RequestBody("pause", session, params), onHttpListener);
    }

    public void resume(String downloadToken) {
//        url = genOneOSAPIUrl(OneOSAPIs.SHARE_API);
        Map<String, Object> params = new HashMap<>();
//        params.put("session", session);
        params.put("cmd", "down");
        params.put("token", downloadToken);
        setMethod("resume");
        setParams(params);
        httpRequest.setParseResult(false);
        httpRequest.post(oneOsRequest, onHttpListener);
//        httpUtils.postJson(url, new RequestBody("resume", session, params), onHttpListener);
    }

    public void completeUp(String shareToken) {
        Map<String, Object> params = new HashMap<>();
//        params.put("session", session);
        params.put("cmd", "up");
        params.put("token", shareToken);
        setMethod("complete");
        setParams(params);
        httpRequest.setParseResult(false);
        httpRequest.post(oneOsRequest, onHttpListener);
    }

    public void cancelUp(String... shareToken) {
        Map<String, Object> params = new HashMap<>();
//        params.put("session", session);
        params.put("cmd", "up");
        params.put("token", (shareToken));
        setMethod("delete");
        setParams(params);
        httpRequest.setParseResult(false);
        httpRequest.post(oneOsRequest, onHttpListener);
    }

    @NonNull
    OnHttpRequestListener onHttpListener = new OnHttpRequestListener() {

        @Override
        public void onStart(String url) {

        }

        @Override
        public void onSuccess(String url, String result) {
            Logger.p(Logger.Level.DEBUG, Logger.Logd.SHARE, TAG, "onSuccess:" + result);
            if (listener != null) {
                try {
                    JSONObject json = new JSONObject(result);
                    boolean ret = json.getBoolean("result");
                    if (ret) {
                        if (json.has("data") && !json.isNull("data")) {
                            JSONObject datajson = json.getJSONObject("data");
                        }
                        listener.onSuccess(url, oneOsRequest.getParams().getMethod());
                    } else {
                        JSONObject errJson = json.getJSONObject("error");
                        int errorNo = errJson.getInt("code");
                        String msg = errJson.getString("msg");
                        listener.onFailure(url, oneOsRequest.getParams().getMethod(), errorNo, msg);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    listener.onFailure(url, oneOsRequest.getParams().getMethod(), HttpErrorNo.ERR_JSON_EXCEPTION, context.getResources().getString(R.string.error_json_exception));
                }
            }
        }

        @Override
        public void onFailure(String url, int httpCode, int errorNo, String strMsg) {
            Logger.p(Logger.Level.ERROR, Logger.Logd.SHARE, TAG, "onFailure: " + errorNo + " : " + strMsg);
            if (listener != null) {
                listener.onFailure(url, oneOsRequest.getParams().getMethod(), errorNo, strMsg);
            }
        }
    };


    public interface NotifyDownloadListener {
        void onSuccess(String url, String method);

        void onFailure(String url, String method, int errorNo, String errorMsg);
    }
}
