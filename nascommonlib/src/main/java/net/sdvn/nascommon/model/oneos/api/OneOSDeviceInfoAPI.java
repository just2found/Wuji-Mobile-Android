package net.sdvn.nascommon.model.oneos.api;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

import net.sdvn.nascommon.constant.HttpErrorNo;
import net.sdvn.nascommon.constant.OneOSAPIs;
import net.sdvn.nascommon.model.http.OnHttpRequestListener;
import net.sdvn.nascommon.model.oneos.user.LoginSession;
import net.sdvn.nascommon.utils.GsonUtils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by yun on 18/05/22.
 */

public class OneOSDeviceInfoAPI extends BaseAPI {
    private static final String TAG = OneOSDeviceInfoAPI.class.getSimpleName();
    private OnDeviceInfoListener mListener;

    public OneOSDeviceInfoAPI(@NonNull LoginSession loginSession) {
        super(loginSession, OneOSAPIs.USER, "device");
    }

    public OneOSDeviceInfoAPI(String ip, String port, String session) {
        super(ip, OneOSAPIs.USER, "device");
        oneOsRequest.getParams().setSession(session);
    }
    //{"method":"device","session":"xxx","params":{"name":"xx","desc":"xxx"}}

    public void query() {
        httpRequest.post(oneOsRequest, new OnHttpRequestListener() {
            @Override
            public void onStart(String url) {
                if (mListener != null) {
                    mListener.onStart(url);
                }
            }

            @Override
            public void onSuccess(String url, String result) {
                SubInfo data = null;
                try {
                    data = GsonUtils.decodeJSON(result, SubInfo.class);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (data == null) {
                    if (mListener != null)
                        mListener.onFailure(url, HttpErrorNo.ERR_JSON_EXCEPTION, "json exception");
                }
                if (mListener != null) {
                    mListener.onSuccess(url, data);
                }
            }

            @Override
            public void onFailure(String url, int httpCode, int errCode, String errorMsg) {
                if (mListener != null) {
                    mListener.onFailure(url, errCode, errorMsg);
                }
            }
        });

    }

    public void update(@Nullable final String markName, @Nullable final String desc) {
        Map<String, Object> map = new ConcurrentHashMap<>();
        if (markName != null)
            map.put("name", markName);
        if (desc != null)
            map.put("desc", desc);
        setParams(map);
        httpRequest.post(oneOsRequest, new OnHttpRequestListener() {
            @Override
            public void onStart(String url) {
                if (mListener != null) {
                    mListener.onStart(url);
                }
            }

            @Override
            public void onSuccess(String url, String result) {
                SubInfo data = null;
                try {
                    data = GsonUtils.decodeJSON(result, SubInfo.class);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (data == null) {
                    if (mListener != null)
                        mListener.onFailure(url, HttpErrorNo.ERR_JSON_EXCEPTION, "json exception");
                }
                if (mListener != null) {
                    mListener.onSuccess(url, data);
                }
            }

            @Override
            public void onFailure(String url, int httpCode, int errCode, String errorMsg) {
                if (mListener != null) {
                    mListener.onFailure(url, errCode, errorMsg);
                }
            }
        });
    }

    public void setListener(OnDeviceInfoListener listener) {
        mListener = listener;
    }


    public interface OnDeviceInfoListener {
        void onStart(String url);

        void onSuccess(String url, SubInfo info);

        void onFailure(String url, int errorNo, String errorMsg);
    }

    @Keep
    public static class SubInfo {
        /**
         * name : xx
         * desc : xxx
         */
        @SerializedName("name")
        private String name;
        @SerializedName("desc")
        private String desc;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDesc() {
            return desc;
        }

        public void setDesc(String desc) {
            this.desc = desc;
        }

        @Override
        public String toString() {
            return "SubInfo{" +
                    "name='" + name + '\'' +
                    ", desc='" + desc + '\'' +
                    '}';
        }
    }

}
