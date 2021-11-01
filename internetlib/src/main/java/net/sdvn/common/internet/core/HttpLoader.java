package net.sdvn.common.internet.core;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.Gson;

import net.sdvn.common.internet.NetConfig;
import net.sdvn.common.internet.OkHttpClientIns;
import net.sdvn.common.internet.SdvnHttpErrorNo;
import net.sdvn.common.internet.listener.BaseResultListener;
import net.sdvn.common.internet.listener.ResultListener;

import java.lang.reflect.Type;
import java.net.SocketTimeoutException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import okhttp3.HttpUrl;
import okhttp3.ResponseBody;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;


/**
 * Created by yun at 2018/01/14
 */
public abstract class HttpLoader {
    public static final String TAG = "httpLoader";
    protected Class<? extends GsonBaseProtocol> parseClass;
    protected Retrofit mRetrofit;
    protected HttpLoaderStateListener mHttpLoaderStateListener;
    protected Object tag;
    private CompositeDisposable compositeDisposable;
    private Scheduler mScheduler;
    private boolean isAsync = true;

    public void setAsync(boolean isAsync) {
        this.isAsync = isAsync;
    }

    /**
     * 构造传递监听的接口，以及需要被解析的字节码文件
     *
     * @param parseClass
     */
    public HttpLoader(Class<? extends GsonBaseProtocol> parseClass) {
        this.parseClass = parseClass;
        //构建retrofit
        mRetrofit = buildRetrofit();
    }

    public static String encodeSHA_256(String source) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] result = md.digest(source.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : result) {
                int number = (b & 0xff);
                String str = Integer.toHexString(number);
                if (str.length() == 1) {
                    sb.append("0");
                }
                sb.append(str);
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            //can't reach
            return "";
        }
    }

    /**
     * Check network
     *
     * @param context
     * @return
     */
    public static boolean checkNetwork(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager == null ? null : connectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }

    protected void addDisposable(@NonNull Disposable disposable) {
        if (compositeDisposable == null) {
            compositeDisposable = new CompositeDisposable();
        }
        compositeDisposable.add(disposable);
    }

    public void cancel() {
        if (compositeDisposable != null) compositeDisposable.dispose();
    }

    public void setSchedulers(Scheduler scheduler) {
        mScheduler = scheduler;
    }

    /**
     * 构建Retrofit
     *
     * @return
     */
    private Retrofit buildRetrofit() {
        Retrofit.Builder builder = new Retrofit.Builder();
        HttpUrl httpUrl = getHttpUrl();
        builder.baseUrl(httpUrl)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create());
        if (OkHttpClientIns.getApiClient() != null)
            builder.client(OkHttpClientIns.getApiClient());
        return builder.build();
    }

    @NonNull
    protected HttpUrl getHttpUrl() {
        return new HttpUrl
                .Builder()
                .scheme(NetConfig.schema)
                .host(NetConfig.host())
                .port(NetConfig.port)
                .build();
    }

    /**
     * 具体构建Observable
     *
     * @param mRetrofit
     * @return
     */
    public abstract Observable<ResponseBody> structureObservable(Retrofit mRetrofit);

    public void executor(@NonNull ResultListener resultListener) {
        executor(null, resultListener);
    }

    public void executor(@Nullable Object obj, @NonNull ResultListener resultListener) {
        executor(obj, null, resultListener);
    }

    /*
      Type type = new TypeToken<GsonBaseProtocolV2<DataPages<DataEnMbPointMsg>>>() {
      }.getType();

      object : TypeToken<GsonBaseProtocolV2<Ads>>() {
        }.getType()
     */
    public void executor(@NonNull final Type typeOfT, @NonNull ResultListener resultListener) {
        executor(null, typeOfT, resultListener);
    }

    public void setHttpLoaderStateListener(HttpLoaderStateListener loaderStateListener) {
        mHttpLoaderStateListener = loaderStateListener;
    }

    /**
     * 集体执行逻辑
     *
     * @param resultListener 结果监听
     */
    public void executor(@Nullable Object obj, @Nullable final Type typeOfT, @NonNull final BaseResultListener resultListener) {
        final Object tag;
        if (obj == null) {
            tag = this.tag;
        } else {
            tag = (obj);
        }
        if (!checkNetwork(OkHttpClientIns.getContext())) {//如果没网
            GsonBaseProtocol baseProtocol = new GsonBaseProtocol();
            baseProtocol.result = SdvnHttpErrorNo.EC_NOT_NET;
            baseProtocol.errmsg = "No Network";
            resultListener.error(tag, baseProtocol);
            return;
        }

        //承载类   被观察者
        Observable<ResponseBody> mObservable = structureObservable(mRetrofit);
        Observer<ResponseBody> observer = new Observer<ResponseBody>() {
            @Override
            public void onError(@NonNull Throwable e) {
                e.printStackTrace();
                GsonBaseProtocol baseProtocol = new GsonBaseProtocol();
                baseProtocol.result = SdvnHttpErrorNo.EC_REQUEST;
                if (e instanceof SocketTimeoutException) {
                    baseProtocol.result = SdvnHttpErrorNo.EC_TIME_OUT;
                }
                baseProtocol.errmsg = e.getMessage();
                resultListener.error(tag, baseProtocol);
                if (mHttpLoaderStateListener != null)
                    mHttpLoaderStateListener.onLoadError();
            }

            @Override
            public void onComplete() {
                if (mHttpLoaderStateListener != null)
                    mHttpLoaderStateListener.onLoadComplete();
            }

            @Override
            public void onSubscribe(@NonNull Disposable disposable) {
                addDisposable(disposable);
                if (Thread.currentThread().getId() == Looper.getMainLooper().getThread().getId()) {
                    if (mHttpLoaderStateListener != null)
                        mHttpLoaderStateListener.onLoadStart(disposable);
                } else {//非主线程，切换线程
                    AndroidSchedulers.mainThread().scheduleDirect(new Runnable() {
                        @Override
                        public void run() {
                            //showLoading();
                            if (mHttpLoaderStateListener != null)
                                mHttpLoaderStateListener.onLoadStart(disposable);
                        }
                    });
                }
            }

            @Override
            public void onNext(@NonNull ResponseBody mResponseBody) {
                try {
                    dispatchResult(mResponseBody, resultListener, tag, typeOfT);
                } catch (Exception e) {
                    e.printStackTrace();
                    GsonBaseProtocol baseProtocol = new GsonBaseProtocol();
                    baseProtocol.result = SdvnHttpErrorNo.EC_REQUEST;
                    baseProtocol.errmsg = e.getMessage();
                    resultListener.error(tag, baseProtocol);
                }
            }
        };
        Consumer<Disposable> onSubscribe = new Consumer<Disposable>() {
            @Override
            public void accept(@NonNull final Disposable disposable) {
            }
        };
        if (isAsync) {
            mObservable.subscribeOn(Schedulers.io())
                    .observeOn(getScheduler())
                    .doOnSubscribe(onSubscribe)
                    .subscribe(observer);//主线程中执行
        } else {
            mObservable.doOnSubscribe(onSubscribe)
                    .subscribe(observer);
        }
    }

    private Scheduler getScheduler() {
        if (mScheduler != null) return mScheduler;
        return AndroidSchedulers.mainThread();
    }

    protected void setTag(Object tag) {
        this.tag = tag;
    }

    /**
     * 处理服务器返回的数据
     *
     * @param mResponseBody
     * @param resultListener
     * @param typeOfT
     */
    protected void dispatchResult(@NonNull ResponseBody mResponseBody, @NonNull BaseResultListener resultListener, Object tag, Type typeOfT) throws Exception {
        //获得json数据
        String resultString = mResponseBody.string();
//        LogUtils.e(TAG, "返回数据:" + resultString);
        Gson gson = new Gson();
        GsonBaseProtocol baseProtocol = gson.fromJson(resultString, parseClass);
        if (baseProtocol != null) {
            if ((baseProtocol.result) != 0) {
                //说明有错误信息
                resultListener.error(tag, baseProtocol);
            } else {
                if (typeOfT != null) {
                    baseProtocol = gson.fromJson(resultString, typeOfT);
                } else {
                    baseProtocol = gson.fromJson(resultString, parseClass);
                }
                if (resultListener instanceof ResultListener) {
                    ((ResultListener) resultListener).success(tag, baseProtocol);
                }
            }
        } else {
            throw new Exception("data is null");
        }
    }

    @Keep
    public interface HttpLoaderStateListener {
        void onLoadStart(Disposable disposable);

        void onLoadComplete();

        void onLoadError();
    }
}
