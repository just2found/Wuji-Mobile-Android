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
import net.sdvn.nascommon.utils.FileUtils;
import net.sdvn.nascommon.utils.GsonUtils;
import net.sdvn.nascommon.utils.log.Logger;
import net.sdvn.nascommonlib.R;

import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2017/9/5.
 */

public class OneOSListRecentAPI extends BaseAPI {
    private static String TAG = OneOSListRecentAPI.class.getSimpleName();
    private OnRecentListListener listener;
    private int uid;

    public OneOSListRecentAPI(@NonNull LoginSession loginSession) {
        super(loginSession, OneOSAPIs.FILE_API, "recent");
//        this.session = loginSession.getSession();
        this.uid = loginSession.getUserInfo().getUid();
    }

    public void setOnRecentListListener(OnRecentListListener listener) {
        this.listener = listener;
    }


    public void recentList(int page) {

//        url = genOneOSAPIUrl(OneOSAPIs.FILE_API);
        Map<String, Object> params = new HashMap<>();
        params.put("page", String.valueOf(page));
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
                Logger.p(Logger.Level.DEBUG, Logger.Logd.OSAPI, TAG, "recentList Response Data:" + result);
                if (listener != null) {
                    try {
                        JSONObject json = new JSONObject(result);
                        boolean ret = json.getBoolean("result");
                        if (ret) {
                            ArrayList<OneOSFile> files = null;
                            int total = 0, page = 0, pages = 0;
                            if (json.has("data")) {
                                JSONObject datajson = json.getJSONObject("data");
                                total = datajson.getInt("total");
                                page = datajson.getInt("page");
                                pages = datajson.getInt("pages");

                                Type type = new TypeToken<List<OneOSFile>>() {
                                }.getType();
                                files = GsonUtils.decodeJSON(datajson.getString("files"), type);

                                if (!EmptyUtils.isEmpty(files)) {

                                    Iterator<OneOSFile> it = files.iterator();
                                    while (it.hasNext()) {
                                        OneOSFile file = it.next();
                                        if (file.isDirectory()) {
                                            it.remove();
                                        } else {
                                            file.updateInfo();
//                                            file.setFmtUDTime(FileUtils.fmtTimeByZone(file.getUdtime()));
//                                            file.setFmtCTTime(FileUtils.fmtTimeByZone(file.getCttime()));

                                            Logger.p(Logger.Level.DEBUG, Logger.Logd.DEBUG, TAG, "file = " + file.getName() + ", getLoginTime = " + FileUtils.fmtTimeByZone(file.getTime()));
                                        }
                                    }
                                    Logger.p(Logger.Level.DEBUG, Logger.Logd.DEBUG, TAG, "files ======" + files);
                                }

                            }
                            listener.onSuccess(url, total, pages, page, files);
                        } else {
                            JSONObject errJson = json.getJSONObject("error");
                            int errorNo = errJson.getInt("code");
                            String msg = errJson.getString("msg");
                            listener.onFailure(url, errorNo, msg);
                        }
                    } catch (Exception e) {
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


    public interface OnRecentListListener {

        void onStart(String url);

        void onSuccess(String url, int total, int pages, int page, ArrayList<OneOSFile> files);

        void onFailure(String url, int errorNo, String errorMsg);
    }
}
