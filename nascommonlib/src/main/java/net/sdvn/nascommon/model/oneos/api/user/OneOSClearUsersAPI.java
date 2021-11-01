package net.sdvn.nascommon.model.oneos.api.user;

import androidx.annotation.NonNull;

import net.sdvn.nascommon.constant.OneOSAPIs;
import net.sdvn.nascommon.model.http.OnHttpRequestListener;
import net.sdvn.nascommon.model.oneos.api.BaseAPI;
import net.sdvn.nascommon.model.oneos.user.LoginSession;

/**
 * Created by yun on 2018/3/30.
 * { "method":"clear", "session":"xxx","params":{} }
 * 成功：{"result":true,"data":{}}
 * 失败： {"result":false, "error":{"code":xx,"msg":"xxxx"}}
 * code	msg
 * -40000	删除失败
 * -40003	权限拒绝，仅管理员可删除用户
 */

public class OneOSClearUsersAPI extends BaseAPI {
    private ClearUserListener<String> mClearUserListener;

    public OneOSClearUsersAPI(@NonNull LoginSession loginSession) {
        super(loginSession, OneOSAPIs.USER);
    }

    public void clear() {
        setMethod("clear");
        httpRequest.post(oneOsRequest, new OnHttpRequestListener() {
            @Override
            public void onStart(String url) {
                if (mClearUserListener != null)
                    mClearUserListener.onStart(url);
            }

            @Override
            public void onSuccess(String url, String result) {
                if (mClearUserListener != null)
                    mClearUserListener.onSuccess(url);
            }

            @Override
            public void onFailure(String url, int httpCode, int errCode, String errorMsg) {
                if (mClearUserListener != null)
                    mClearUserListener.onFailure(url, errCode, errorMsg);
            }
        });
    }

    public void setClearUserListener(ClearUserListener<String> clearUserListener) {
        mClearUserListener = clearUserListener;
    }


    public interface ClearUserListener<T> {
        void onStart(T url);

        void onSuccess(T url);

        void onFailure(T url, int errorNo, String errorMsg);
    }


}
