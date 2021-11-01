package net.sdvn.nascommon.model.oneos.api.file;

import androidx.annotation.NonNull;

import com.google.gson.reflect.TypeToken;

import net.sdvn.nascommon.constant.HttpErrorNo;
import net.sdvn.nascommon.constant.OneOSAPIs;
import net.sdvn.nascommon.model.http.OnHttpRequestListener;
import net.sdvn.nascommon.model.oneos.OneOSFile;
import net.sdvn.nascommon.model.oneos.api.BaseAPI;
import net.sdvn.nascommon.model.oneos.user.LoginSession;
import net.sdvn.nascommon.utils.EmptyUtils;
import net.sdvn.nascommon.utils.GsonUtils;
import net.sdvn.nascommon.utils.log.Logger;
import net.sdvn.nascommonlib.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Created by Administrator on 2017/11/16.
 */

public class OneOSListCityFileAPI extends BaseAPI {
    private static final String TAG = OneOSListCityFileAPI.class.getSimpleName();
    private JSONArray cityList;
    private OnFileListListener listener;
    @NonNull
    private ArrayList<OneOSFile> fileList = new ArrayList<>();
    private int postTry = 0;

    public OneOSListCityFileAPI(@NonNull LoginSession loginSession, JSONArray cityList) {
        super(loginSession, OneOSAPIs.FILE_API, "location");
        this.cityList = cityList;
    }

    public void setOnFileListListener(OnFileListListener listener) {
        this.listener = listener;
    }

    public void getFileList() {
        for (int i = 0; i < cityList.length(); i++) {
            try {
                String city = String.valueOf(cityList.get(i));
                list(i, city);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public void list(final int cityNum, String city) {
//        url = genOneOSAPIUrl(OneOSAPIs.FILE_API);
        Map<String, Object> params = new HashMap<>();
        Locale locale = Locale.getDefault();
        String lan = locale.getLanguage() + "-" + locale.getCountry();
        Logger.p(Logger.Level.DEBUG, Logger.Logd.OSAPI, TAG, "request language = " + lan);
        params.put("type", "city");
        params.put("location", city);
        params.put("language", lan);
        setParams(params);
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
                            ArrayList<OneOSFile> files = null;
                            if (json.has("data")) {
                                JSONObject datajson = json.getJSONObject("data");
                                Type type = new TypeToken<List<OneOSFile>>() {
                                }.getType();
                                files = GsonUtils.decodeJSON(datajson.getString("files"), type);
                                if (!EmptyUtils.isEmpty(files)) {
                                    for (OneOSFile file : files) {
                                        if (!file.isDirectory()) {
                                            file.setSection(cityNum);
                                        }
                                        file.updateInfo();
                                    }
                                }
                            }
                            fileList.addAll(files);
                            postTry++;
                            Logger.p(Logger.Level.DEBUG, Logger.Logd.DEBUG, TAG, "postTry ==== " + postTry);
                            Logger.p(Logger.Level.DEBUG, Logger.Logd.DEBUG, TAG, "cityList length === " + cityList.length());
                            if (postTry == cityList.length()) {
                                listener.onSuccess(url, fileList);
                            }
                        } else {
                            JSONObject errJson = json.getJSONObject("error");
                            int errorNo = errJson.getInt("code");
                            String msg = errJson.getString("msg");

                            listener.onFailure(url, errorNo, msg);
                        }


                    } catch (JSONException e) {
                        e.printStackTrace();
                        listener.onFailure(url, HttpErrorNo.ERR_JSON_EXCEPTION, context.getResources().getString(R.string.error_json_exception));
                    } catch (Exception e) {
                        e.printStackTrace();
                        listener.onFailure(url, HttpErrorNo.UNKNOWN_EXCEPTION, context.getResources().getString(R.string.error_json_exception));

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

    public interface OnFileListListener {

        void onSuccess(String url, ArrayList<OneOSFile> files);

        void onFailure(String url, int errorNo, String errorMsg);
    }
}
