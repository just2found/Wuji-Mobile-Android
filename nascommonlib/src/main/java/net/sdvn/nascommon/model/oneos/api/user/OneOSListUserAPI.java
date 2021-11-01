package net.sdvn.nascommon.model.oneos.api.user;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;

import com.google.gson.reflect.TypeToken;

import net.sdvn.nascommon.constant.HttpErrorNo;
import net.sdvn.nascommon.constant.OneOSAPIs;
import net.sdvn.nascommon.model.http.OnHttpRequestListener;
import net.sdvn.nascommon.model.oneos.OneOSUser;
import net.sdvn.nascommon.model.oneos.api.BaseAPI;
import net.sdvn.nascommon.model.oneos.user.LoginSession;
import net.sdvn.nascommon.utils.GsonUtils;
import net.sdvn.nascommon.utils.log.Logger;
import net.sdvn.nascommonlib.BuildConfig;
import net.sdvn.nascommonlib.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import timber.log.Timber;

/**
 * OneSpace OS Get Device Mac Address API
 * <p/>
 * Created by gaoyun@eli-tech.com on 2016/1/8.
 */
public class OneOSListUserAPI extends BaseAPI {
    private static final String TAG = OneOSListUserAPI.class.getSimpleName();

    private OnListUserListener listener;

    public OneOSListUserAPI(@NonNull LoginSession loginSession) {
        super(loginSession, OneOSAPIs.USER, "list");
    }

    public void setOnListUserListener(OnListUserListener listener) {
        this.listener = listener;
    }


    public void list() {
        Map<String, Object> params = new HashMap<>();
//        url = genOneOSAPIUrl(OneOSAPIs.USER);
        params.put("type", "all");
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
                        JSONObject datajson = new JSONObject(result);
                        List<OneOSUser> userList = new ArrayList<>();
                        String jsonArray = datajson.getString("users");
                        if (BuildConfig.DEBUG) {
                            Timber.d("users: %s", jsonArray);
                        }
                        List<OneOSUser> users = GsonUtils.decodeJSON(jsonArray, new TypeToken<List<OneOSUser>>() {
                        }.getType());
                        if (users != null) {
                            userList.addAll(users);
                            if (BuildConfig.DEBUG) {
                                Timber.d("users: %s", users.toString());
                            }
                        }
//                        for (int i = 0; i < jsonArray.length(); i++) {
//                            JSONObject json = jsonArray.getJSONObject(i);
//                            String name = json.getString("username");
//                            int uid = json.getInt("uid");
//                            int gid = json.getInt("gid");
//                            int isAdmin = json.getInt("admin");
//                            String mark = null;
//                            if (json.has("mark"))
//                                mark = json.getString("mark");
//                            userList.add(new OneOSUser(name, uid, gid, isAdmin, mark));
//                        }
                        listener.onSuccess(url, userList);

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

    @Keep
    public interface OnListUserListener {
        void onStart(String url);

        void onSuccess(String url, List<OneOSUser> users);

        void onFailure(String url, int errorNo, String errorMsg);
    }
}
