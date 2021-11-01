package net.sdvn.common.internet.loader;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import net.sdvn.cmapi.CMAPI;
import net.sdvn.cmapi.util.LogUtils;
import net.sdvn.common.internet.core.GsonBaseProtocol;
import net.sdvn.common.internet.core.V1AgApiHttpLoader;
import net.sdvn.common.internet.listener.BaseResultListener;
import net.sdvn.common.internet.listener.ListResultListener;
import net.sdvn.common.internet.listener.ResultListener;
import net.sdvn.common.internet.protocol.UnbindDeviceResult;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import okhttp3.ResponseBody;

public class UnbindDeviceHttpLoader extends V1AgApiHttpLoader {
    //    private UnbindDeviceRequestBody body;
    private boolean isSingle;

    public UnbindDeviceHttpLoader() {
        super(GsonBaseProtocol.class);
    }

    public void unbind(String deviceId, List<String> userids, @NonNull ListResultListener<UnbindDeviceResult> resultListener) {
//        this.body = new UnbindDeviceRequestBody();
//
//        body.action = "deviceunbind";
//        body.deviceid = deviceId;
////        body.userid = CMAPI.getInstance().getBaseInfo().getAccount();
//        body.baindusers = userids;
//        body.token = CMAPI.getInstance().getBaseInfo().getTicket();
////        body.dappid = Constants.CONFIG_APPID;
        isSingle = false;

        this.bodyMap = new ConcurrentHashMap<>();
        put("action", "deviceunbind");
        put("baindusers", userids);
        put("token", CMAPI.getInstance().getBaseInfo().getTicket());
        put("deviceid", deviceId);
        Type type = new TypeToken<List<UnbindDeviceResult>>() {
        }.getType();
        executor(type, resultListener);
    }

    public void unbindSingle(String deviceId, String userid, @NonNull ResultListener<UnbindDeviceResult> resultListener) {
//        this.body = new UnbindDeviceRequestBody();
//        body.action = "deviceunbind";
//        body.deviceid = deviceId;
////        body.userid = CMAPI.getInstance().getBaseInfo().getAccount();
//        body.baindusers = Collections.singletonList(userid);
//        body.token = CMAPI.getInstance().getBaseInfo().getTicket();
////        body.dappid = Constants.CONFIG_APPID;
//        executor(body, resultListener);
//        isSingle = true;

        isSingle = true;
        this.bodyMap = new ConcurrentHashMap<>();
        put("action", "deviceunbind");
        put("baindusers", Collections.singletonList(userid));
        put("token", CMAPI.getInstance().getBaseInfo().getTicket());
        put("deviceid", deviceId);
        Type type = new TypeToken<UnbindDeviceResult>() {
        }.getType();
        executor(type, resultListener);
    }


//    @Override
//    public Observable<ResponseBody> structureObservable(Retrofit mRetrofit) {
//        V1AgApiService v1AgapiServcie = mRetrofit.create(V1AgApiService.class);
//        ObjectHelper.requireNonNull(this.body, "request body is null");
////        body.sig = getSignEncrypt();
//        return v1AgapiServcie.request(getMap(), this.body);
//    }


    /**
     * 处理服务器返回的数据
     *
     * @param mResponseBody
     * @param resultListener
     * @param typeOfT
     */
    @Override
    protected void dispatchResult(@NonNull ResponseBody mResponseBody, @NonNull BaseResultListener resultListener, Object tag, Type typeOfT) throws Exception {
        //获得json数据
        String resultString = mResponseBody.string();
        LogUtils.e(TAG, "返回数据:" + resultString);

        Results fromJson = new Gson().fromJson(resultString, Results.class);
        if (fromJson.results != null) {
            List<UnbindDeviceResult> results = fromJson.results;
            if (results.size() == 0)
                throw new Exception("data is null");

            if (isSingle) {
                if (results.size() == 1) {
                    if (results.get(0).result == 0)
                        ((ResultListener) resultListener).success(tag, results.get(0));
                    else
                        resultListener.error(tag, results.get(0));
                    return;
                }
            }
            List<UnbindDeviceResult> successResults = new ArrayList<>();
            List<UnbindDeviceResult> errorResults = new ArrayList<>();
            for (UnbindDeviceResult result : results) {
                if (result.result == 0) {
                    successResults.add(result);
                } else {
                    errorResults.add(result);
                }
            }
            if (results.size() >= 1) {
                if (resultListener instanceof ListResultListener) {
                    ((ListResultListener) resultListener).success(tag, successResults);
                    ((ListResultListener) resultListener).error(tag, errorResults);
                }

            }
        }
    }

    @Keep
    public static class Results {
        public List<UnbindDeviceResult> results;

    }

}
