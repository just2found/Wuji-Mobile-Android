package net.sdvn.nascommon.model.oneos.api.sys;

import net.sdvn.nascommon.constant.HttpErrorNo;
import net.sdvn.nascommon.constant.OneOSAPIs;
import net.sdvn.nascommon.model.http.OnHttpRequestListener;
import net.sdvn.nascommon.model.oneos.api.BaseAPI;
import net.sdvn.nascommon.utils.log.Logger;
import net.sdvn.nascommonlib.R;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Administrator on 2017/6/29.
 */

public class OneOSHDInfoAPI extends BaseAPI {

    private static final String TAG = OneOSHDInfoAPI.class.getSimpleName();

    private OnHDInfoListener<String, Object> listener;
    private Object tag;

    public OneOSHDInfoAPI(String ip, String port) {
        super(ip, OneOSAPIs.SYSTEM_SYS, "hdinfo");
    }

    public void setTag(Object tag) {
        this.tag = tag;
    }

    public void setListener(OneOSHDInfoAPI.OnHDInfoListener<String, Object> listener) {
        this.listener = listener;
    }

    public void getHdInfo(String version) {

        Logger.p(Logger.Level.DEBUG, Logger.Logd.OSAPI, TAG, "———————————————————新版本版本 登录成功 判断是否要格式化 —————————————————");
//        Map<String, Object> params = new HashMap<>();
//        final String url = genOneOSAPIUrl(OneOSAPIs.SYSTEM_SYS);

        httpRequest.post(oneOsRequest, new OnHttpRequestListener() {
            @Override
            public void onStart(String url) {
                if (listener != null)
                    listener.onStart(url);
            }

            @Override
            public void onSuccess(String url, String result) {
                Logger.p(Logger.Level.DEBUG, Logger.Logd.OSAPI, TAG, "hdinfo = " + result);
                try {
                    JSONObject json = new JSONObject(result);
                    JSONObject jsonInfo = json.getJSONObject("info");
                    String errno = jsonInfo.getString("errno");
                    String count = jsonInfo.getString("count");
                    if (jsonInfo.has("dev2") && !jsonInfo.isNull("dev2")
                            && !"null".equals(jsonInfo.getString("dev2"))) {
                        count = "2";
                    }
                    if (listener != null)
                        listener.onSuccess(url, tag, errno, count);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(String url, int httpCode, int errorNo, String strMsg) {
                if (listener != null)
                    listener.onFailure(url, tag, HttpErrorNo.ERR_JSON_EXCEPTION, context.getResources().getString(R.string.error_json_exception));
            }
        });
    }


    public interface OnHDInfoListener<T, E> {
        void onStart(T url);

        void onSuccess(T url, E tag, String error, String count);

        void onFailure(T url, E tag, int errorNo, String errorMsg);
    }
}


