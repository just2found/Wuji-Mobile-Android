package net.sdvn.nascommon.model.oneos.api;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;

import net.sdvn.common.internet.OkHttpClientIns;
import net.sdvn.nascommon.constant.OneOSAPIs;
import net.sdvn.nascommon.model.http.HttpRequest;
import net.sdvn.nascommon.model.http.OneOsRequest;
import net.sdvn.nascommon.model.http.RequestBody;
import net.sdvn.nascommon.model.oneos.user.LoginSession;
import net.sdvn.nascommon.utils.FileUtils;
import net.sdvn.nascommon.utils.GsonUtils;
import net.sdvn.nascommon.utils.Utils;
import net.sdvn.nascommon.utils.log.Logger;

import java.util.Map;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import okhttp3.HttpUrl;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import timber.log.Timber;

/**
 * @author yun
 */
public abstract class BaseAPI {
    private static final String TAG = BaseAPI.class.getSimpleName();

    protected Context context;
    protected HttpRequest httpRequest;
    protected LoginSession loginSession;
    protected OneOsRequest oneOsRequest;

    private BaseAPI() {
        init();
        oneOsRequest = new OneOsRequest();
        oneOsRequest.setParams(new RequestBody());
    }


    public BaseAPI(String address, String action) {
        this();
        oneOsRequest.setAddress(address);
        oneOsRequest.setAction(action);
    }

    public BaseAPI(String address, String action, String method) {
        this();
        oneOsRequest.setAddress(address);
        oneOsRequest.setAction(action);
        oneOsRequest.getParams().setMethod(method);
    }

    public BaseAPI(LoginSession loginSession, String action, String method) {
        this(loginSession.getIp(), action, method);
        this.loginSession = loginSession;
        oneOsRequest.getParams().setSession(loginSession.getSession());
    }

    public BaseAPI(LoginSession loginSession, String action) {
        this(loginSession.getIp(), action);
        this.loginSession = loginSession;
        oneOsRequest.getParams().setSession(loginSession.getSession());
    }

    public void setMethod(String method) {
        oneOsRequest.getParams().setMethod(method);
    }

    public void setParams(Map<String, Object> params) {
        oneOsRequest.getParams().setParams(params);
    }

    private void init() {
        this.context = Utils.getApp();
        this.httpRequest = new HttpRequest();
    }

    public void setBackOnUI(boolean callbackOnUIThread) {
        this.httpRequest.setBackOnUI(callbackOnUIThread);
    }

    public void setExecOnSameThread(boolean onSameThread) {
        this.httpRequest.setExecOnSameThread(onSameThread);
    }

    public String url() {
        return null != oneOsRequest ? oneOsRequest.url() : "";
    }

    @NonNull
    public String params() {
        return null != oneOsRequest ? oneOsRequest.params() : "{}";
    }

    public Context getContext() {
        return context;
    }

    @NonNull
    public String getString(@StringRes int resId) {
        return context.getString(resId);
    }


    public void saveData(String data) {
        saveData(genKey(), data);
    }

    protected String genKey() {
        final RequestBody params = oneOsRequest.getParams();
        return loginSession.getId() + oneOsRequest.getAction()
                + (params != null ? (params.getMethod()
                + GsonUtils.encodeJSONCatchEx(params.getParams())) : "{}");
    }

    public void saveData(String key, String data) {
        Logger.LOGD(TAG, "put disk thread -->" + Thread.currentThread());
        try {
            final Disposable subscribe = Observable.just(true)
                    .subscribeOn(Schedulers.io())
                    .subscribe(new Consumer<Boolean>() {
                        @Override
                        public void accept(Boolean aBoolean) {
                            final boolean cache = FileUtils.putDiskCache(key, data);
                            Logger.LOGD(TAG, "exec disk cache result:" + cache);
                            Logger.LOGD(TAG, "exec disk cache thread :" + Thread.currentThread());

                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Observable<String> getLocalData(final String key) {
        return Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(@NonNull ObservableEmitter<String> emitter) {
                String data = FileUtils.getDiskCache(key);
                Logger.LOGE("{SDVNtest}", key);
                emitter.onNext(data);
                emitter.onComplete();
            }
        });
    }

    public void cancel() {
        if (httpRequest != null)
            httpRequest.cancel();
    }

    public static Retrofit createProductRetrofit(String host) {
        return new Retrofit.Builder()
                .baseUrl(getHost(host))
                .client(OkHttpClientIns.getApiClient())
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build();
    }

    public static String getHost(String ip) {
        if (ip == null) {
            ip = "";
            Timber.e("HttpFileService:getHost host==null");
        }
        return new HttpUrl.Builder()
                .scheme(OneOSAPIs.SCHEME)
                .host(ip)
                .port(OneOSAPIs.ONE_APIS_DEFAULT_PORT)
                .build()
                .toString();
    }

}
