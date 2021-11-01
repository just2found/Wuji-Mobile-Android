package net.sdvn.nascommon.model.oneos.api.file;

import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.google.gson.reflect.TypeToken;

import net.sdvn.nascommon.constant.AppConstants;
import net.sdvn.nascommon.constant.HttpErrorNo;
import net.sdvn.nascommon.constant.OneOSAPIs;
import net.sdvn.nascommon.iface.OnFileListListener;
import net.sdvn.nascommon.model.http.OnHttpRequestListener;
import net.sdvn.nascommon.model.oneos.OneOSFile;
import net.sdvn.nascommon.model.oneos.OneOSFileType;
import net.sdvn.nascommon.model.oneos.api.BaseAPI;
import net.sdvn.nascommon.model.oneos.user.LoginSession;
import net.sdvn.nascommon.utils.EmptyUtils;
import net.sdvn.nascommon.utils.GsonUtils;
import net.sdvn.nascommonlib.R;

import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * OneSpace OS Get File List API
 * <p/>
 * Created by gaoyun@eli-tech.com on 2016/1/14.
 */
public class OneOSListDirAPI extends BaseAPI {
    private static final String TAG = OneOSListDirAPI.class.getSimpleName();
    private final int uid;

    private OnFileListListener listener;
    private final String path;
    private OneOSFileType type = OneOSFileType.ALL;
    @NonNull
    private String pre = "storage/";

    //    { "method":"listdir", "session":"xxx", "params": {"ftype": "xxx", "num": 100, "page": 0,
//            "order": "xx"} }
    public OneOSListDirAPI(@NonNull LoginSession loginSession, @NonNull String  path) {
        super(loginSession, OneOSAPIs.FILE_API, "listdir");
        this.path = path;
        this.uid = loginSession.getUserInfo().getUid();
    }

    public void setOnFileListListener(OnFileListListener listener) {
        this.listener = listener;
    }

    public void list(int page) {
        list(type, page);
    }

    public void list(OneOSFileType ftype) {
        list(ftype, -1);
    }

    public void list(OneOSFileType ftype, int page) {
        buildRequest(ftype, page);
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
                // super.onSuccess(result);

//                Logger.p(Logger.Level.DEBUG, Logger.Logd.OSAPI, TAG, "Response Data:" + result);
                if (listener != null) {
                    parseResult(url, result, listener);
                }
            }

            @Override
            public void onFailure(String url, int httpCode, int errorNo, String strMsg) {
//                Logger.LOGE(TAG, "Response Data: ErrorNo=" + errorNo + " ; ErrorMsg=" + strMsg);
                if (listener != null) {
                    listener.onFailure(url, type, path, errorNo, strMsg);
                }
            }
        });
    }

    private void buildRequest(OneOSFileType ftype, int page) {
        this.type = ftype;
//        url = genOneOSAPIUrl(OneOSAPIs.FILE_API);
        Map<String, Object> params = new HashMap<>();
        params.put("path", path);
        params.put("ftype", type.getServerTypeName());
        if (loginSession.getOneOSInfo() != null && loginSession.getOneOSInfo().getVerno() > 51004) {
            params.put("show_hidden", 0);
        }
        if (page >= 0) {
            params.put("page", page);
            params.put("num", AppConstants.PAGE_SIZE);
            setMethod("list");
        } else {
            setMethod("list");
        }
        setParams(params);
    }

    public void loadLocalData(int page) {
        buildRequest(type, page);
        final Observable<String> localData = getLocalData(genKey());
        localData.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<String>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(String s) {
                        parseResult(null, s, listener);
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (listener != null) {
                            listener.onSuccess(null, type, path, 0, 0, 0, null);
                        }
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    public void parseResult(String url, String result, OnFileListListener listener) {
        try {
            if (url == null && TextUtils.isEmpty(result)) {
                if (listener != null) {
                    listener.onSuccess(url, type, path, 0, 0, 0, null);
                    return;
                }
            }
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
                        for (OneOSFile file : files) {
                           file.updateInfo();
                        }
                    }
                }
                if (listener != null)
                    listener.onSuccess(url, type, path, total, pages, page, files);
                if (url != null) {
                    saveData(result);
                }
            } else {
                JSONObject errJson = json.getJSONObject("error");
                int errorNo = errJson.getInt("code");
                String msg = errJson.getString("msg");
                if (listener != null)
                    listener.onFailure(url, type, path, errorNo, msg);
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (listener != null)
                listener.onFailure(url, type, path, HttpErrorNo.ERR_JSON_EXCEPTION, context.getResources().getString(R.string.error_json_exception));
        }
    }


}
