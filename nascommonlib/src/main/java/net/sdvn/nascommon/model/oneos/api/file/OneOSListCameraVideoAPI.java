package net.sdvn.nascommon.model.oneos.api.file;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import net.sdvn.nascommon.constant.HttpErrorNo;
import net.sdvn.nascommon.constant.OneOSAPIs;
import net.sdvn.nascommon.model.http.OnHttpRequestListener;
import net.sdvn.nascommon.model.oneos.OneOSCameraVideo;
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
 * OneSpace OS Get File List API
 * <p/>
 * Created by gaoyun@eli-tech.com on 2016/1/14.
 */
public class OneOSListCameraVideoAPI extends BaseAPI {
    private static final String TAG = OneOSListCameraVideoAPI.class.getSimpleName();
    private final int uid;

    private OnVideoListListener listener;
    @Nullable
    private String path = null;

    public OneOSListCameraVideoAPI(@NonNull LoginSession loginSession, String path) {
        super(loginSession, OneOSAPIs.FILE_API, "camera");
        this.path = path;
        this.uid = loginSession.getUserInfo().getUid();
    }

    public void setListener(OnVideoListListener listener) {
        this.listener = listener;
    }

    public void list(@NonNull final String date) {
//        url = genOneOSAPIUrl(OneOSAPIs.FILE_API);
        Map<String, Object> params = new HashMap<>();
        params.put("path", path);
        params.put("date", date);
        params.put("cmd", "time");
        setParams(params);
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
                            ArrayList<OneOSCameraVideo> videos = new ArrayList<OneOSCameraVideo>();
                            if (json.has("data")) {
                                JSONArray jsonArray = json.getJSONArray("data");
                                for (int i = 0; i < jsonArray.length(); i++) {
                                    JSONObject item = jsonArray.getJSONObject(i);
                                    String hour = item.getString("hour");
                                    JSONArray itemArray = item.getJSONArray("mins");
                                    for (int j = 0; j < itemArray.length(); j++) {
                                        String min = itemArray.getJSONObject(j).getString("name");
                                        String fullTime = date.replace("-", "") + hour + min;
                                        String path = itemArray.getJSONObject(j).getString("path");
                                        OneOSCameraVideo videoItem = new OneOSCameraVideo();
                                        videoItem.setTime(fullTime);
                                        videoItem.setVideoPath(path);
                                        videos.add(videoItem);
                                    }
                                }

                            }
                            listener.onSuccess(url, path, videos);
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
                Logger.LOGE(TAG, "Response Data: ErrorNo=" + errorNo + " ; ErrorMsg=" + strMsg);
                if (listener != null) {
                    listener.onFailure(url, errorNo, strMsg);
                }
            }
        });

    }


    public interface OnVideoListListener {
        void onStart(String url);

        void onSuccess(String url, String path, ArrayList<OneOSCameraVideo> videos);

        void onFailure(String url, int errorNo, String errorMsg);
    }
}
