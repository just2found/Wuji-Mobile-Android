package net.sdvn.nascommon.model.oneos.api;

import androidx.annotation.NonNull;

import net.sdvn.nascommon.constant.HttpErrorNo;
import net.sdvn.nascommon.constant.OneOSAPIs;
import net.sdvn.nascommon.model.http.OnHttpRequestListener;
import net.sdvn.nascommon.model.oneos.user.LoginSession;
import net.sdvn.nascommon.utils.log.Logger;
import net.sdvn.nascommonlib.R;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * OneSpace OS API to Get Device SSUDP Client ID
 * <p/>
 * Created by gaoyun@eli-tech.com on 2016/07/04.
 */
public class OneOSSsudpClientIDAPI extends BaseAPI {
    private static final String TAG = OneOSSsudpClientIDAPI.class.getSimpleName();

    private OnClientIDListener listener;
    private String dev, name;

    public OneOSSsudpClientIDAPI(@NonNull LoginSession loginSession) {
        super(loginSession, OneOSAPIs.SYSTEM_SSUDP_CID);
    }

    public OneOSSsudpClientIDAPI(String ip, String port) {
        super(ip, OneOSAPIs.SYSTEM_SSUDP_CID);
    }

    public void setOnClientIDListener(OnClientIDListener listener) {
        this.listener = listener;
    }

    public void query() {
//        url = genOneOSAPIUrl(OneOSAPIs.SYSTEM_SSUDP_CID);
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
                            listener.onSuccess(url, json.getString("cid"));
                        } else {
                            // {"errno":-1,"msg":"list error","result":false}
                            int errorNo = json.getInt("errno");
                            String msg = json.has("msg") ? json.getString("msg") : null;
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
                if (listener != null) {
                    listener.onFailure(url, errorNo, strMsg);
                }
            }
        });


    }

    public interface OnClientIDListener {
        void onStart(String url);

        void onSuccess(String url, String cid);

        void onFailure(String url, int errorNo, String errorMsg);
    }
}
