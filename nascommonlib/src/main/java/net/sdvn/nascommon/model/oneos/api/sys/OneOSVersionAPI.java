package net.sdvn.nascommon.model.oneos.api.sys;

import androidx.annotation.NonNull;

import net.sdvn.nascommon.constant.HttpErrorNo;
import net.sdvn.nascommon.constant.OneOSAPIs;
import net.sdvn.nascommon.model.http.OnHttpRequestListener;
import net.sdvn.nascommon.model.oneos.OneOSInfo;
import net.sdvn.nascommon.model.oneos.api.BaseAPI;
import net.sdvn.nascommon.model.oneos.user.LoginSession;
import net.sdvn.nascommon.utils.log.Logger;
import net.sdvn.nascommonlib.R;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * OneSpace OS Get OneSpace OneOS version API
 * <p/>
 * Created by gaoyun@eli-tech.com on 2016/4/6.
 */
public class OneOSVersionAPI extends BaseAPI {
    private static final String TAG = OneOSVersionAPI.class.getSimpleName();

    private OnSystemVersionListener listener;

    public OneOSVersionAPI(@NonNull LoginSession loginSession) {
        super(loginSession, OneOSAPIs.SYSTEM_SYS);
    }

    public OneOSVersionAPI(String ip, String port, boolean isHttp) {
        super(ip, OneOSAPIs.SYSTEM_SYS);
    }

    public void setOnSystemVersionListener(OnSystemVersionListener listener) {
        this.listener = listener;
    }

    public void query() {
//        url = genOneOSAPIUrl(OneOSAPIs.SYSTEM_SYS);
//        Map<String, Object> params = new HashMap<>();
        //params.put("method", "getversion");
        setMethod("getversion");
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
//                Logger.p(LogLevel.DEBUG, Logd.DEBUG, TAG, "Response Data:" + result);
                if (listener != null) {
                    try {
                        JSONObject json = new JSONObject(result);
                        boolean ret = json.getBoolean("result");
                        if (ret) {
                            // {result, model, version, needup}
//                            {"data": {"build": "20180607", "model": "one2017", "product": "h2n2", "verno": 50000, "version": "5.0.0"}, "result": true}
                            //{"data": {"build": "20161223", "model": "one2017", "needup": false, "product": "h2n2", "version": "4.0.0"}, "error": {"code": -40004, "msg": "not found"}, "result": true}
                            JSONObject datajson = json.getJSONObject("data");
                            String model = datajson.getString("model");
                            String product = datajson.getString("product");
                            String version = datajson.getString("version");
                            String build = datajson.getString("build");
                            int verno = datajson.getInt("verno");
                            boolean needsUp = false;
                            if (datajson.has("needup"))
                                needsUp = datajson.getBoolean("needup");
                            //OneOSInfo info = new OneOSInfo(version, model, needsUp, product, build);
                            OneOSInfo info = new OneOSInfo(version, model, needsUp, product, build, verno);
                            listener.onSuccess(url, info);
                        } else {
                            Logger.LOGE(TAG, "Get OneOS Version Failed");
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
                if (listener != null) {
                    listener.onFailure(url, errorNo, strMsg);
                }
            }
        });

    }

    public interface OnSystemVersionListener {
        void onStart(String url);

        void onSuccess(String url, OneOSInfo info);

        void onFailure(String url, int errorNo, String errorMsg);
    }
}
