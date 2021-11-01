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
import net.sdvn.nascommon.utils.log.Logger;
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
public class OneOSListDBAPI extends BaseAPI {
    private static final String TAG = OneOSListDBAPI.class.getSimpleName();

    private OnFileListListener listener;
    @NonNull
    private OneOSFileType type = null;
    private final int uid;
    private String path;

    public OneOSListDBAPI(@NonNull LoginSession loginSession, @NonNull OneOSFileType type) {
        super(loginSession, OneOSAPIs.FILE_API, "listdb");
//        this.session = loginSession.getSession();
        this.type = type;
        this.uid = loginSession.getUserInfo().getUid();
    }

    public OneOSListDBAPI(@NonNull LoginSession loginSession, @NonNull OneOSFileType type, String path) {
        super(loginSession, OneOSAPIs.FILE_API, "listdb");
//        this.session = loginSession.getSession();
        this.path = path;
        this.type = type;
        this.uid = loginSession.getUserInfo().getUid();
    }

    public void setOnFileListListener(OnFileListListener listener) {
        this.listener = listener;
    }


    public void list(int page) {
        buildRequest(page);
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

    private void buildRequest(int page) {
        Logger.p(Logger.Level.DEBUG, Logger.Logd.DEBUG, TAG, "do list");
//        url = genOneOSAPIUrl(OneOSAPIs.FILE_API);
        Map<String, Object> params = new HashMap<>();
        if (type != OneOSFileType.PICTURE) {
            params.put("sort", "1");
        } else {
            params.put("sort", "5");
        }
        params.put("path", path);
        params.put("num", AppConstants.PAGE_SIZE);
        params.put("page", page);

        params.put("order", "time_desc");
        if (OneOSFileType.DOCUMENTS == type) {
            ArrayList<String> arrayList = new ArrayList<>();
            arrayList.add("txt");
            arrayList.add("doc");
            arrayList.add("xls");
            arrayList.add("ppt");
            arrayList.add("pdf");
            params.put("ftype", arrayList);

        } else {
            params.put("ftype", type.getServerTypeName());
        }
        setParams(params);
    }

    public void loadLocalData(int page) {
        buildRequest(page);
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
                if (listener != null) {
                    listener.onSuccess(url, type, path, total, pages, page, files);
                }
                if (url != null) {
                    saveData(result);
                }
            } else {
                JSONObject errJson = json.getJSONObject("error");
                int errorNo = errJson.getInt("code");
                String msg = errJson.getString("msg");
                if (listener != null) {
                    listener.onFailure(url, type, path, errorNo, msg);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (listener != null) {
                listener.onFailure(url, type, path, HttpErrorNo.ERR_JSON_EXCEPTION, context.getResources().getString(R.string.error_json_exception));
            }
        }
    }


}
