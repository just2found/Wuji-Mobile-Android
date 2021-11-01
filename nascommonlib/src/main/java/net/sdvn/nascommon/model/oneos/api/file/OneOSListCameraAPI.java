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

import java.util.HashMap;
import java.util.Map;

/**
 * OneSpace OS Get Device Mac Address API
 * <p/>
 * Created by gaoyun@eli-tech.com on 2016/1/8.
 */
public class OneOSListCameraAPI extends BaseAPI {
    private static final String TAG = OneOSListCameraAPI.class.getSimpleName();

    private OnListCameraListener listener;

    public OneOSListCameraAPI(@NonNull LoginSession loginSession) {
        super(loginSession, OneOSAPIs.FILE_API, "camera");
    }

    public void setListener(OnListCameraListener listener) {
        this.listener = listener;
    }

    public void list() {
        Map<String, Object> params = new HashMap<>();
//        url = genOneOSAPIUrl(OneOSAPIs.FILE_API);
        params.put("cmd", "list");
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
                    try {
                        JSONArray jsonArray = new JSONArray(result);
                        listener.onSuccess(url, jsonArray);
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


    public interface OnListCameraListener {
        void onStart(String url);

        void onSuccess(String url, JSONArray cameraList);

        void onFailure(String url, int errorNo, String errorMsg);
    }
}
