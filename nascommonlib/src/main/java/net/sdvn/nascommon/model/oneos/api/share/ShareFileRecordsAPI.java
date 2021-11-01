package net.sdvn.nascommon.model.oneos.api.share;

import androidx.annotation.NonNull;

import net.sdvn.nascommon.constant.HttpErrorNo;
import net.sdvn.nascommon.constant.OneOSAPIs;
import net.sdvn.nascommon.iface.EventListener;
import net.sdvn.nascommon.model.http.OnHttpRequestListener;
import net.sdvn.nascommon.model.oneos.api.BaseAPI;
import net.sdvn.nascommon.model.oneos.user.LoginSession;
import net.sdvn.nascommon.utils.log.Logger;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class ShareFileRecordsAPI extends BaseAPI {
    private EventListener<Map<String, Integer>> mMapEventListener;

    public ShareFileRecordsAPI(@NonNull LoginSession loginSession) {
        super(loginSession, OneOSAPIs.SHARE_API, "records");
    }

    public void query() {
        HashMap<String, Object> params = new HashMap<>();
        params.put("cmd", "up");
        setParams(params);
        httpRequest.post(oneOsRequest, new OnHttpRequestListener() {
            @Override
            public void onStart(String url) {
                if (mMapEventListener != null)
                    mMapEventListener.onStart(url);
            }

            @Override
            public void onSuccess(String url, String result) {
                Logger.p(Logger.Level.DEBUG, Logger.Logd.DEBUG, "ShareFileRecordsAPI", result);
                try {
                    HashMap<String, Integer> map = new HashMap<>();
                    JSONObject jsonObject = new JSONObject(result);
                    Iterator<String> keys = jsonObject.keys();
                    while (keys.hasNext()) {
                        String key = keys.next();
                        int count = jsonObject.getInt(key);
                        map.put(key, count);
                    }
                    if (mMapEventListener != null) {
                        mMapEventListener.onSuccess(url, map);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    if (mMapEventListener != null)
                        mMapEventListener.onFailure(url, HttpErrorNo.ERR_JSON_EXCEPTION, "");
                }
            }

            @Override
            public void onFailure(String url, int httpCode, int errorNo, String strMsg) {
                if (mMapEventListener != null)
                    mMapEventListener.onFailure(url, errorNo, strMsg);
            }
        });
    }

    public void setMapEventListener(EventListener<Map<String, Integer>> mapEventListener) {
        mMapEventListener = mapEventListener;
    }
}
