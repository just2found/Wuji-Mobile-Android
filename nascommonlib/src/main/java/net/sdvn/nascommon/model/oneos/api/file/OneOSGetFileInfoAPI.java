package net.sdvn.nascommon.model.oneos.api.file;

import androidx.annotation.NonNull;

import com.google.gson.reflect.TypeToken;

import net.sdvn.nascommon.constant.HttpErrorNo;
import net.sdvn.nascommon.constant.OneOSAPIs;
import net.sdvn.nascommon.model.http.OnHttpRequestListener;
import net.sdvn.nascommon.model.oneos.FileInfo;
import net.sdvn.nascommon.model.oneos.api.BaseAPI;
import net.sdvn.nascommon.model.oneos.user.LoginSession;
import net.sdvn.nascommon.utils.GsonUtils;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OneOSGetFileInfoAPI extends BaseAPI {
    private static final String TAG = OneOSGetFileInfoAPI.class.getSimpleName();

    private OnGetFileInfoListener listener;

    public OneOSGetFileInfoAPI(@NonNull LoginSession loginSession) {
        super(loginSession, OneOSAPIs.FILE_API, "info");
    }

    public void setOnGetFileInfoListener(OnGetFileInfoListener listener) {
        this.listener = listener;
    }

    public void getFileInfo(List<String> paths) {
        Map<String, Object> params = new HashMap<>();
        params.put("path", paths);
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
                if (listener != null) {
                    ArrayList<FileInfo> fileInfos = null;
                    Type type = new TypeToken<List<FileInfo>>() {
                    }.getType();
                    try {
                        fileInfos = GsonUtils.decodeJSON(result, type);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    if (fileInfos == null) {
                        listener.onFailure(url, HttpErrorNo.ERR_JSON_EXCEPTION, "json exception");
                    }
                    listener.onSuccess(url, fileInfos);
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


    public interface OnGetFileInfoListener {
        void onStart(String url);

        void onSuccess(String url, ArrayList<FileInfo> fileInfos);

        void onFailure(String url, int errorNo, String errorMsg);
    }
}
