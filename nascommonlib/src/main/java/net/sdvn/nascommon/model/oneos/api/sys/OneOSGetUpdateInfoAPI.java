package net.sdvn.nascommon.model.oneos.api.sys;

import androidx.annotation.NonNull;

import net.sdvn.nascommon.constant.HttpErrorNo;
import net.sdvn.nascommon.constant.OneOSAPIs;
import net.sdvn.nascommon.model.http.OnHttpRequestListener;
import net.sdvn.nascommon.model.oneos.SdvnUpdateInfo;
import net.sdvn.nascommon.model.oneos.UpdateInfo;
import net.sdvn.nascommon.model.oneos.api.BaseAPI;
import net.sdvn.nascommon.model.oneos.user.LoginSession;
import net.sdvn.nascommon.utils.GsonUtils;

import org.json.JSONObject;

public class OneOSGetUpdateInfoAPI extends BaseAPI {
    public OneOSGetUpdateInfoAPI(@NonNull LoginSession loginSession) {
        super(loginSession, OneOSAPIs.SYSTEM_SYS);
    }

    private OnUpdateInfoListener onUpdateInfoListener;

    public void get() {
        setMethod("update");
        httpRequest.post(oneOsRequest, new OnHttpRequestListener() {
            @Override
            public void onStart(String url) {
                if (onUpdateInfoListener != null) {
                    onUpdateInfoListener.onStart(url);
                }
            }

            @Override
            public void onSuccess(String url, @NonNull String result) {

                try {
                    JSONObject jsonObject = new JSONObject(result);
                    if (jsonObject.has("enabled")
                            && jsonObject.has("result")) {
                        SdvnUpdateInfo updateInfo1 = GsonUtils.decodeJSON(result, SdvnUpdateInfo.class);
                        if (updateInfo1.getResult() == 0) {
                            UpdateInfo updateInfo = new UpdateInfo();
                            updateInfo.setNeedup(updateInfo1.isEnabled());
                            updateInfo.setOnline(true);
                            if (updateInfo1.isEnabled() && updateInfo1.getFiles() != null && updateInfo1.getFiles().size() > 0)
                                updateInfo.setUrl(updateInfo1.getFiles().get(0).getDownloadurl());
                            if (onUpdateInfoListener != null) {
                                onUpdateInfoListener.onSuccess(url, updateInfo);
                            }
                        } else {
                            if (onUpdateInfoListener != null) {
                                onUpdateInfoListener.onFailure(url, updateInfo1.getResult(), updateInfo1.getErrmsg());
                            }
                        }
                    } else {
                        if (result.startsWith("[") && result.endsWith("]"))
                            result = result.substring(1, result.length() - 1);
                        UpdateInfo updateInfo = GsonUtils.decodeJSON(result, UpdateInfo.class);
                        if (onUpdateInfoListener != null) {
                            int verno = loginSession.getOneOSInfo().getVerno();
                            if (updateInfo.getVerno() > 0 && verno < updateInfo.getVerno()) {
                                updateInfo.setNeedup(true);
                            }
                            onUpdateInfoListener.onSuccess(url, updateInfo);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    if (onUpdateInfoListener != null) {
                        onUpdateInfoListener.onFailure(url, HttpErrorNo.ERR_JSON_EXCEPTION, e.getMessage());
                    }
                }

            }

            @Override
            public void onFailure(String url, int httpCode, int errorNo, String strMsg) {
                if (onUpdateInfoListener != null) {
                    onUpdateInfoListener.onFailure(url, errorNo, strMsg);
                }
            }
        });
    }

    public void setOnUpdateInfoListener(OnUpdateInfoListener onUpdateInfoListener) {
        this.onUpdateInfoListener = onUpdateInfoListener;
    }

    public interface OnUpdateInfoListener {
        void onStart(String url);

        void onSuccess(String url, UpdateInfo updateInfo);

        void onFailure(String url, int errorNo, String errorMsg);
    }
}
