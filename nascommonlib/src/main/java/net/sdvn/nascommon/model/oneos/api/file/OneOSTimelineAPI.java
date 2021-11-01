package net.sdvn.nascommon.model.oneos.api.file;

import androidx.annotation.NonNull;

import net.sdvn.nascommon.constant.HttpErrorNo;
import net.sdvn.nascommon.constant.OneOSAPIs;
import net.sdvn.nascommon.model.http.OnHttpRequestListener;
import net.sdvn.nascommon.model.oneos.api.BaseAPI;
import net.sdvn.nascommon.model.oneos.user.LoginSession;
import net.sdvn.nascommon.utils.log.Logger;
import net.sdvn.nascommonlib.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Administrator on 2017/11/16.
 */

public class OneOSTimelineAPI extends BaseAPI {

    private static final String TAG = "OneOSTimelineAPI";
    private OnTimelineListListener listener;

    public OneOSTimelineAPI(@NonNull LoginSession loginSession) {
        super(loginSession, OneOSAPIs.FILE_API);
    }

    public void setListener(OnTimelineListListener listener) {
        this.listener = listener;
    }

    public void list() {
//        url = genOneOSAPIUrl(OneOSAPIs.FILE_API);
        Map<String, Object> params = new HashMap<>();
        params.put("ftype", "pic");
        setParams(params);
        setMethod("timeline");
        httpRequest.setParseResult(false);
        httpRequest.post(oneOsRequest, new OnHttpRequestListener() {
            @Override
            public void onStart(String url) {

            }

            @Override
            public void onSuccess(String url, String result) {
                Logger.p(Logger.Level.DEBUG, Logger.Logd.OSAPI, TAG, "Response Data:" + result);
                if (listener != null) {
                    try {
                        JSONObject json = new JSONObject(result);
                        boolean ret = json.getBoolean("result");
                        if (ret) {
                            JSONArray timelimeList = json.getJSONArray("data");
                            ArrayList arrayList = new ArrayList();
                            for (int i = 0; i < timelimeList.length(); i++) {
//                                arrayList.add(timelimeList.get(i).);
                            }

                            //listener.onSuccess(url, cityList);
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

            }
        });

    }

    public interface OnTimelineListListener {

        void onSuccess(String url, JSONArray cityList);

        void onFailure(String url, int errorNo, String errorMsg);
    }
}
