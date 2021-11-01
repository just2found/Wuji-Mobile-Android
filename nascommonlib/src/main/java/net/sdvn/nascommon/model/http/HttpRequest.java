package net.sdvn.nascommon.model.http;

import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import net.sdvn.common.internet.OkHttpClientIns;
import net.sdvn.nascommon.constant.HttpErrorNo;
import net.sdvn.nascommon.utils.EmptyUtils;
import net.sdvn.nascommon.utils.GsonUtils;
import net.sdvn.nascommonlib.BuildConfig;

import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.FileNameMap;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URLEncoder;
import java.util.Map;

import okhttp3.CacheControl;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import timber.log.Timber;

import static net.sdvn.nascommon.utils.log.Logger.LOGE;
import static net.sdvn.nascommon.utils.log.Logger.LOGI;


/**
 * @author gaoyun@eli-tech.com
 * @date 2018/03/19
 */
public class HttpRequest {
    private static final String TAG = HttpRequest.class.getSimpleName();
    private static final long DEFAULT_TIME_OUT = 30; // 30s
    private static final MediaType MEDIA_TYPE_JSON = MediaType.parse("application/json; charset=utf-8");
    public static final int ERROR_CONNECT_FAILED = -404;
    private long timeout;

    private OkHttpClient okHttpClient;
    private OnHttpRequestListener listener;
    private Handler mHandler;
    private int retryCount = 0;
    private boolean parse = true;
    private boolean backOnUI = true;
    private boolean execOnSameThread = false;
    private CacheControl mCacheControl;
    private Call call;

    public HttpRequest() {
        okHttpClient = OkHttpClientIns.getApiClient();
        mHandler = new Handler(Looper.getMainLooper());
    }

    public HttpRequest(OnHttpRequestListener listener) {
        this();
        this.listener = listener;
    }

    public HttpRequest(OnHttpRequestListener listener, long timeout) {
        this(listener);
        this.timeout = timeout;
    }

    public void setOnHttpRequestListener(OnHttpRequestListener listener) {
        this.listener = listener;
    }

    @NonNull
    public HttpRequest setCacheControl(CacheControl cacheControl) {
        this.mCacheControl = cacheControl;
        return this;
    }

    public void post(@NonNull OneOsRequest oneOsRequest, OnHttpRequestListener listener) {
        setOnHttpRequestListener(listener);
        postJson(oneOsRequest);
    }

    public void postJson(@NonNull OneOsRequest oneOsRequest) {
        LOGI(TAG, "Http Post Request: " + oneOsRequest.toString());
        String url = oneOsRequest.url();
        if (null != listener) {
            listener.onStart(url);
        }
        try {
            RequestBody body = RequestBody.create(MEDIA_TYPE_JSON, oneOsRequest.params());
            Request.Builder builder = new Request.Builder().url(url).post(body);
            if (mCacheControl != null) {
                builder.cacheControl(mCacheControl);
            }
            Request request = builder.build();
            dispatchRequest(request);
        } catch (Exception e) {
            if (null != listener) {
                listener.onFailure(url, -400, HttpErrorNo.UNKNOWN_EXCEPTION, e.getMessage());
            }
        }

    }

    private void dispatchRequest(Request request) {
        call = okHttpClient.newCall(request);
        String url = request.url().toString();
        if (execOnSameThread) {
            MyCallback myCallback = new MyCallback(url);
            try {
                Response response = call.execute();
                myCallback.onResponse(call, response);
            } catch (IOException e) {
                myCallback.onFailure(call, e);
            }
        } else {
            call.enqueue(new MyCallback(url));
        }
    }

    public void get(@NonNull String url) {
        LOGI(TAG, "Http Get Request: {url:" + url + ", method:GET}");
        Request request = new Request.Builder().url(url).build();
        if (null != listener) {
            listener.onStart(url);
        }
        dispatchRequest(request);
    }

    public void get(String url, @Nullable Map<String, String> params) {
        LOGI(TAG, "Http Get Request: {url:" + url + ", method:Get, params:" + (null == params ? "null" : params.toString()) + "}");
        String query = paramsToStr(params);
        if (!EmptyUtils.isEmpty(query)) {
            url += "?" + query;
        }
        Request request = new Request.Builder().url(url).build();
        if (null != listener) {
            listener.onStart(url);
        }
        dispatchRequest(request);
    }


    public void post(@NonNull String url, @Nullable Map<String, Object> params) {
        LOGI(TAG, "Http Post Request: {url:" + url + ", method:POST, params:" + (null == params ? "null" : params.toString()) + "}");
        RequestBody body = RequestBody.create(MEDIA_TYPE_JSON, paramsToJson(params));
        Request request = new Request.Builder().url(url).post(body).build();
        if (null != listener) {
            listener.onStart(url);
        }
        dispatchRequest(request);
    }

    public void patch(@NonNull String url, @Nullable Map<String, Object> params) {
        LOGI(TAG, "Http Patch Request: {url:" + url + ", method:PATCH, params:" + (null == params ? "null" : params.toString()) + "}");
        RequestBody body = RequestBody.create(MEDIA_TYPE_JSON, paramsToJson(params));
        Request request = new Request.Builder().url(url).patch(body).build();
        okHttpClient.newCall(request).enqueue(new MyCallback(url));
        if (null != listener) {
            listener.onStart(url);
        }
    }

    public void delete(@NonNull String url) {
        LOGI(TAG, "Http Delete Request: {url:" + url + ", method:DELETE}");
        Request request = new Request.Builder().url(url).delete().build();
        okHttpClient.newCall(request).enqueue(new MyCallback(url));
        if (null != listener) {
            listener.onStart(url);
        }
    }

    public void delete(@NonNull String url, @Nullable Map<String, Object> params) {
        LOGI(TAG, "Http Delete Request: {url:" + url + ", method:DELETE, params:" + (null == params ? "null" : params.toString()) + "}");
        RequestBody body = RequestBody.create(MEDIA_TYPE_JSON, paramsToJson(params));
        Request request = new Request.Builder().url(url).delete(body).build();
        okHttpClient.newCall(request).enqueue(new MyCallback(url));
        if (null != listener) {
            listener.onStart(url);
        }
    }

    public String paramsToJson(Map<String, Object> params) {
        try {
            return GsonUtils.encodeJSON(params);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @NonNull
    public String paramsToStr(@Nullable Map<String, String> params) {
        StringBuilder s = new StringBuilder();
        if (null != params) {
            int pos = 0;
            for (String key : params.keySet()) {
                if (pos > 0) {
                    s.append("&");
                }
                try {
                    s.append(String.format("%s=%s", key, URLEncoder.encode(params.get(key), "utf-8")));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                pos++;
            }
        }

        return s.toString();
    }

    public void setParseResult(boolean parse) {
        this.parse = parse;
    }

    public void setBackOnUI(boolean backOnUI) {
        this.backOnUI = backOnUI;
    }

    public void setExecOnSameThread(boolean onSameThread) {
        this.execOnSameThread = onSameThread;
    }

    private class MyCallback implements Callback {
        private String url;

        MyCallback(String url) {
            this.url = url;
        }

        @Override
        public void onFailure(@NotNull Call call, @NotNull IOException ex) {
            String msg = ex.toString();
            LOGE(TAG, "Request failed: " + msg, ex);
            if (call.request().body() != null && BuildConfig.DEBUG) {
                try {
                    LOGE(TAG, "Request failed requestBody: ", call.request());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (null == listener) {
                return;
            }
            int httpCode = ERROR_CONNECT_FAILED;
            if (ex instanceof SocketTimeoutException) {
                httpCode = HttpErrorNo.ERR_REQUEST_TIMEOUT;
            }
            callback(url, httpCode, httpCode, msg);
        }

        @Override
        public void onResponse(@NotNull Call call, @NotNull final Response response) {
            if (null == listener) {
                return;
            }

            final int code = response.code();

            if (!response.isSuccessful()) {
                LOGE(TAG, "Request error, code: " + code + ", message: " + response.message());
                callback(url, code, code, response.message());
                return;
            }

            int errCode;
            String errMsg;
            try {
                String respBody = response.body().string();
//               LOGI(TAG, "Request success, code: " + code + ", message: " + respBody);
                JSONObject json = new JSONObject(respBody);
                if (parse) {
                    boolean result = json.getBoolean("result");
                    if (result) {
                        if (json.has("data") && !json.isNull("data"))
                            callback(url, json.getString("data"));
                        else
                            callback(url, null);
                        return;
                    }
                } else {
                    try {
                        boolean result = json.getBoolean("result");
                        if (result) {
                            callback(url, respBody);
                            return;
                        }
                    } catch (Exception ignore) {
                    }
                    try {
                        JSONObject err = json.getJSONObject("error");
                    } catch (Exception ignore) {
                        callback(url, respBody);
                        return;
                    }
                }

                JSONObject err = json.getJSONObject("error");
                errCode = err.getInt("code");
                errMsg = err.getString("msg");
            } catch (Exception e) {
                e.printStackTrace();
                errCode = HttpErrorNo.ERR_ONE_REQUEST;
                errMsg = e.getMessage();
            }

            callback(url, code, errCode, errMsg);
        }
    }

    public void callbackOnUIThread(Runnable runnable) {
        mHandler.post(runnable);
    }

    private void callback(final String url, final String result) {
        if (backOnUI)
            callbackOnUIThread(new Runnable() {
                @Override
                public void run() {
                    listener.onSuccess(url, result);
                }
            });
        else
            listener.onSuccess(url, result);
    }

    private void callback(final String url, final int httpCode, final int errCode, final String errMsg) {
        if (backOnUI)
            callbackOnUIThread(new Runnable() {
                @Override
                public void run() {
                    if (listener != null)
                        listener.onFailure(url, httpCode, errCode, errMsg);
                }
            });
        else {
            if (listener != null) {
                listener.onFailure(url, httpCode, errCode, errMsg);
            }
        }

    }

    @NonNull
    public RequestBody buildMultipartBody(@NonNull Map<String, Object> paramsMap) {
        MultipartBody.Builder builder = new MultipartBody.Builder().setType(MultipartBody.FORM);
        //追加参数
        for (String key : paramsMap.keySet()) {
            Object object = paramsMap.get(key);
            if (!(object instanceof File)) {
                builder.addFormDataPart(key, object.toString());
            } else {
                File file = (File) object;
                builder.addFormDataPart(key, file.getName(),
                        RequestBody.create(MediaType.parse(getMediaType(file.getName())), file));
            }
        }
        //创建RequestBody
        return builder.build();
    }

    @NonNull
    public RequestBody buildMultipartBody(@NonNull Map<String, Object> paramsMap,
                                          ProgressRequestBody.ProgressInterceptor interceptor) {
        MultipartBody.Builder builder = new MultipartBody.Builder().setType(MultipartBody.FORM);

        for (Map.Entry<String, Object> entry : paramsMap.entrySet()) {
            Object object = entry.getValue();
            String key = entry.getKey();
            if (object == null) continue;
            if ((object instanceof File)) {
                File file = (File) object;
                builder.addFormDataPart(key, file.getName(),
                        new ProgressRequestBody(RequestBody.create(MediaType.parse(getMediaType(file.getName())), file),
                                interceptor));
            } else {
                builder.addFormDataPart(key, object.toString());
            }
        }

        //创建RequestBody
        return builder.build();
    }

    /**
     * 根据文件的名称判断文件的Mine值
     */
    public String getMediaType(String fileName) {
        Timber.tag("getMediaType").d("fileName: %s", fileName);
        String contentTypeFor = "application/octet-stream";
        try {
            FileNameMap map = HttpURLConnection.getFileNameMap();
            contentTypeFor = map.getContentTypeFor(fileName);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return contentTypeFor;
    }

    public void cancel() {
        if (call != null && !call.isCanceled() && call.isExecuted())
            call.cancel();
    }
}
