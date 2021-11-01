package net.sdvn.nascommon.model.oneos.api.file;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.reflect.TypeToken;

import net.sdvn.nascommon.constant.HttpErrorNo;
import net.sdvn.nascommon.constant.OneOSAPIs;
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

/**
 * OneSpace OS Get File List API
 * <p/>
 * Created by gaoyun@eli-tech.com on 2016/02/02.
 */
public class OneOSSearchAPI extends BaseAPI {
    private static final String TAG = OneOSSearchAPI.class.getSimpleName();

    private OnSearchFileListener listener;
    /**
     * public or private
     */
    @Nullable
    private String path = null;
    /**
     * order by toPath or date
     */
    @Nullable
    private String ftype = null;
    /**
     * search filter pattern
     */
    @Nullable
    private String pattern = null;
    @Nullable
    private String pdate1 = null, pdate2 = null;
//    private int uid;

    public OneOSSearchAPI(@NonNull LoginSession mLoginSession) {
        super(mLoginSession, OneOSAPIs.FILE_API);
//        this.session = mLoginSession.getSession();
//        this.uid = mLoginSession.getUserInfo().getUid();
    }

    public void setOnFileListListener(OnSearchFileListener listener) {
        this.listener = listener;
    }

    private void search() {
//        url = genOneOSAPIUrl(OneOSAPIs.FILE_API);
        Map<String, Object> params = new HashMap<>();
        params.put("path", path);
        params.put("ftype", ftype);
        params.put("pattern", pattern);
        if (!EmptyUtils.isEmpty(pdate1) && !EmptyUtils.isEmpty(pdate2)) {
            params.put("pdate1", pdate1);
            params.put("pdate2", pdate2);
        }
        setMethod("searchdb");
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
                    ArrayList<OneOSFile> files = getOneOSFiles(result);
                    if (files != null) {
                        listener.onSuccess(url, files);
                    } else {
                        listener.onFailure(url, HttpErrorNo.ERR_JSON_EXCEPTION, context.getResources().getString(R.string.error_json_exception));
                    }
                }

            }

            @Override
            public void onFailure(String url, int httpCode, int errorNo, String strMsg) {
                if (listener != null) {
                    listener.onFailure(url, errorNo, strMsg);
                }
            }
        });


    }

    public ArrayList<OneOSFile> getOneOSFiles(String result) {
        ArrayList<OneOSFile> files = null;
        try {
            JSONObject json = new JSONObject(result);
            if (json.has("files")) {
                Type type = new TypeToken<List<OneOSFile>>() {
                }.getType();
                files = GsonUtils.decodeJSON(json.getString("files"), type);
                if (!EmptyUtils.isEmpty(files)) {
                    for (OneOSFile file : files) {
                        file.updateInfo();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return files;
    }

    public void search(OneOSFileType fileType, String pattern) {
        this.path = OneOSFileType.getRootPath(fileType);
        this.ftype = "all";
        this.pattern = pattern;
        search();
    }

    public interface OnSearchFileListener {
        void onStart(String url);

        void onSuccess(String url, ArrayList<OneOSFile> files);

        void onFailure(String url, int errorNo, String errorMsg);
    }
}
