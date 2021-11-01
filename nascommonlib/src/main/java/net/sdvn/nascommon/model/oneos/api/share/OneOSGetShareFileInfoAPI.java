package net.sdvn.nascommon.model.oneos.api.share;

import androidx.annotation.NonNull;

import com.google.gson.reflect.TypeToken;

import net.sdvn.nascommon.constant.OneOSAPIs;
import net.sdvn.nascommon.model.http.OnHttpRequestListener;
import net.sdvn.nascommon.model.oneos.ShareFileInfo;
import net.sdvn.nascommon.model.oneos.api.BaseAPI;
import net.sdvn.nascommon.model.oneos.user.LoginSession;
import net.sdvn.nascommon.utils.GsonUtils;
import net.sdvn.nascommon.utils.log.Logger;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OneOSGetShareFileInfoAPI extends BaseAPI {
    private static final String TAG = OneOSGetShareFileInfoAPI.class.getSimpleName();

    private GetDownloadShareFileInfoListener listener;

    public OneOSGetShareFileInfoAPI(@NonNull LoginSession loginSession) {
        super(loginSession, OneOSAPIs.SHARE_API, "list");
    }

    public void setResultListener(GetDownloadShareFileInfoListener listener) {
        this.listener = listener;
    }

    public void getList(boolean isDown) {
        String cmd = isDown ? "down" : "up";
        Map<String, Object> params = new HashMap<>();
//        params.put("session", session);
        params.put("cmd", cmd);
        setParams(params);
        httpRequest.post(oneOsRequest, new OnHttpRequestListener() {
            @Override
            public void onStart(String url) {

            }

            @Override
            public void onSuccess(String url, String result) {
                Logger.p(Logger.Level.DEBUG, Logger.Logd.SHARE, TAG, "onSuccess Data:" + result);
                if (listener != null) {
                    List<ShareFileInfo> list = null;

                    Type type = new TypeToken<List<ShareFileInfo>>() {
                    }.getType();
                    try {
                        list = GsonUtils.decodeJSON(result, type);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    listener.onSuccess(url, list != null ? list : new ArrayList<ShareFileInfo>());

                }
            }

            @Override
            public void onFailure(String url, int httpCode, int errorNo, String strMsg) {
                Logger.p(Logger.Level.ERROR, Logger.Logd.SHARE, TAG, "onFailure Data: " + errorNo + " : " + strMsg);
                if (listener != null) {
                    listener.onFailure(url, errorNo, strMsg);
                }
            }
        });

    }

    public interface GetDownloadShareFileInfoListener {
        void onSuccess(String url, List<ShareFileInfo> list);

        void onFailure(String url, int errorNo, String errorMsg);
    }
}
