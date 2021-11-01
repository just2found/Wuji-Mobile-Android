package net.sdvn.nascommon.model.oneos.api.file;

import androidx.annotation.NonNull;

import net.sdvn.nascommon.constant.HttpErrorNo;
import net.sdvn.nascommon.constant.OneOSAPIs;
import net.sdvn.nascommon.model.http.OnHttpRequestListener;
import net.sdvn.nascommon.model.oneos.api.BaseAPI;
import net.sdvn.nascommon.model.oneos.user.LoginSession;
import net.sdvn.nascommonlib.R;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Administrator on 2017/11/16.
 */

public class OneOSListPlaceAPI extends BaseAPI {

    private static final String TAG = "OneOSListPlaceAPI";
    private OnPlaceListListener listener;

    public OneOSListPlaceAPI(@NonNull LoginSession loginSession) {
        super(loginSession, OneOSAPIs.FILE_API, "locations");
    }

    public void setListener(OnPlaceListListener listener) {
        this.listener = listener;
    }

    public void list() {
//        url = genOneOSAPIUrl(OneOSAPIs.FILE_API);
        Map<String, Object> params = new HashMap<>();
        params.put("type", "city");
        setParams(params);
        httpRequest.post(oneOsRequest, new OnHttpRequestListener() {
            @Override
            public void onStart(String url) {

            }

            @Override
            public void onSuccess(String url, String result) {
//                Logger.p(Logger.Level.DEBUG, Logger.Logd.OSAPI, TAG, "Response Data:" + result);
                if (listener != null) {
                    try {

                        JSONArray cityList = new JSONArray(result);
                        listener.onSuccess(url, cityList);

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

    public interface OnPlaceListListener {

        void onSuccess(String url, JSONArray cityList);

        void onFailure(String url, int errorNo, String errorMsg);
    }
}
