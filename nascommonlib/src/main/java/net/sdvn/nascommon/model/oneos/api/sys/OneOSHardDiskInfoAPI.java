package net.sdvn.nascommon.model.oneos.api.sys;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import net.sdvn.nascommon.constant.HttpErrorNo;
import net.sdvn.nascommon.constant.OneOSAPIs;
import net.sdvn.nascommon.model.http.OnHttpRequestListener;
import net.sdvn.nascommon.model.oneos.OneOSHardDisk;
import net.sdvn.nascommon.model.oneos.api.BaseAPI;
import net.sdvn.nascommon.utils.log.Logger;
import net.sdvn.nascommonlib.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * OneSpace OS Get Hard Disk information
 * <p/>
 * Created by gaoyun@eli-tech.com on 2016/03/29.
 */
public class OneOSHardDiskInfoAPI extends BaseAPI {
    private static final String TAG = OneOSHardDiskInfoAPI.class.getSimpleName();

    private OnHDInfoListener listener;
    @Nullable
    private OneOSHardDisk hardDisk1, hardDisk2;

    public OneOSHardDiskInfoAPI(@NonNull String host) {
        super(host, OneOSAPIs.SYSTEM_SYS, "hdinfo");
    }

    public void setOnHDInfoListener(OnHDInfoListener listener) {
        this.listener = listener;
    }

    public void query(@Nullable OneOSHardDisk hd1, @Nullable OneOSHardDisk hd2) {
        if (null == hd1) {
            hd1 = new OneOSHardDisk();
        }
        if (null == hd2) {
            hd2 = new OneOSHardDisk();
        }
        this.hardDisk1 = hd1;
        this.hardDisk2 = hd2;
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
                    listener.onSuccess(url, getHDInfor(result,hardDisk1,hardDisk2), hardDisk1, hardDisk2);
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

    public String getHDInfor(String result, OneOSHardDisk hardDisk1, OneOSHardDisk hardDisk2) {
        String mode = null;
        try {
            JSONObject data = new JSONObject(result);
            JSONObject infoJson = data.getJSONObject("info");
            if(infoJson.has("mode")){
                mode = infoJson.getString("mode");
            }
            int count = infoJson.getInt("count");
            if (count > 0) {
                String hd1Name = hardDisk1.getName();
                String hd2Name = hardDisk2.getName();

                JSONArray hds = data.getJSONArray("hds");
                for (int i = 0; i < hds.length(); i++) {
                    JSONObject hd = hds.getJSONObject(i);
                    String name = hd.getString("name");
                    JSONObject info = hd.getJSONObject("info");
                    if (!info.has("Device Model")) continue;
                    String model = info.getString("Device Model").trim();
                    String serial = info.getString("Serial Number").trim();
                    String capacity = info.getString("User Capacity").trim();

                    if (null != hd1Name && hd1Name.equals(name)) {
                        hardDisk1.setModel(model);
                        hardDisk1.setSerial(serial);
                        hardDisk1.setCapacity(capacity);
                    } else if (null != hd2Name && hd2Name.equals(name)) {
                        hardDisk2.setModel(model);
                        hardDisk2.setSerial(serial);
                        hardDisk2.setCapacity(capacity);
                    } else {

                        if (i == 0) {
                            hardDisk1.setName(name);
                            hardDisk1.setModel(model);
                            hardDisk1.setSerial(serial);
                            hardDisk1.setCapacity(capacity);
                        } else {
                            hardDisk2.setName(name);
                            hardDisk2.setModel(model);
                            hardDisk2.setSerial(serial);
                            hardDisk2.setCapacity(capacity);
                        }
                    }
                }
            } else {
                hardDisk1 = null;
                hardDisk2 = null;
            }
        } catch (JSONException e) {
            e.printStackTrace();
            listener.onFailure("", HttpErrorNo.ERR_JSON_EXCEPTION,
                    context.getResources().getString(R.string.error_json_exception));
        }
        return mode;
    }

    public interface OnHDInfoListener {
        void onStart(String url);

        void onSuccess(String url, String model, OneOSHardDisk hd1, OneOSHardDisk hd2);

        void onFailure(String url, int errorNo, String errorMsg);
    }
}
