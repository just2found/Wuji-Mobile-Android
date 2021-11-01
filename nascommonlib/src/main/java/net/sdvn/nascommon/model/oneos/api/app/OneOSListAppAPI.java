package net.sdvn.nascommon.model.oneos.api.app;

import androidx.annotation.NonNull;

import net.sdvn.nascommon.constant.HttpErrorNo;
import net.sdvn.nascommon.constant.OneOSAPIs;
import net.sdvn.nascommon.model.http.OnHttpRequestListener;
import net.sdvn.nascommon.model.oneos.OneOSPluginInfo;
import net.sdvn.nascommon.model.oneos.api.BaseAPI;
import net.sdvn.nascommon.model.oneos.user.LoginSession;
import net.sdvn.nascommon.utils.log.Logger;
import net.sdvn.nascommonlib.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Objects;

/**
 * OneSpace OS List Plugins API
 * <p/>
 * Created by gaoyun@eli-tech.com on 2016/02/23.
 */
public class OneOSListAppAPI extends BaseAPI {
    private static final String TAG = OneOSListAppAPI.class.getSimpleName();

    private OnListPluginListener listener;

    public OneOSListAppAPI(@NonNull LoginSession loginSession) {
        super(loginSession, OneOSAPIs.APP_API, "list");
    }

    public void setOnListPluginListener(OnListPluginListener listener) {
        this.listener = listener;
    }

    public void list() {
//        url = genOneOSAPIUrl(OneOSAPIs.APP_API);
//        Map<String, Object> params = new HashMap<>();
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
                        ArrayList<OneOSPluginInfo> mPlugList = new ArrayList<>();
                        JSONArray jsonArray = new JSONArray(result);
                        for (int i = 0; i < jsonArray.length(); i++) {
                            final JSONObject jsonObject = jsonArray.getJSONObject(i);
                            if (jsonObject != null)
                                if (Objects.equals(jsonObject.getString("local"), "true")) {
                                    OneOSPluginInfo info = new OneOSPluginInfo(jsonObject);
                                    mPlugList.add(info);
                                }
                        }
                        Logger.LOGE(TAG, "Count: " + mPlugList.size());
                        listener.onSuccess(url, mPlugList);
                    } catch (JSONException e) {
                        e.printStackTrace();
                        listener.onFailure(url, HttpErrorNo.ERR_JSON_EXCEPTION, context.getResources().getString(R.string.error_json_exception));
                    }
                }
            }


            @Override
            public void onFailure(String url, int httpCode, int errorNo, String strMsg) {
                Logger.LOGE(TAG, "Response Data: ErrorNo=" + errorNo + " ; ErrorMsg=" + strMsg);
                if (listener != null) {
                    listener.onFailure(url, errorNo, strMsg);
                }
            }
        });

    }

    public interface OnListPluginListener {
        void onStart(String url);

        void onSuccess(String url, ArrayList<OneOSPluginInfo> plugins);

        void onFailure(String url, int errorNo, String errorMsg);
    }
}
