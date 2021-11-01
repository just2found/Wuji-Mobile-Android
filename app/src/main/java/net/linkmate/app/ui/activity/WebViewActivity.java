package net.linkmate.app.ui.activity;

import android.app.Application;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.webkit.DownloadListener;
import android.webkit.JsResult;
import android.webkit.SslErrorHandler;
import android.webkit.URLUtil;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import net.linkmate.app.BuildConfig;
import net.linkmate.app.R;
import net.linkmate.app.base.BaseActivity;
import net.linkmate.app.view.TipsBar;
import net.sdvn.cmapi.util.LogUtils;
import net.sdvn.nascommon.utils.EmptyUtils;
import net.sdvn.nascommon.utils.ToastHelper;
import net.sdvn.nascommon.utils.log.Logger;
import net.sdvn.nascommon.widget.TitleBackLayout;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.cert.CertificateFactory;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class WebViewActivity extends BaseActivity {
    private static final String TAG = WebViewActivity.class.getSimpleName();
    public static final String WEB_VIEW_EXTRA_NAME_URL = "Url";
    public static final String WEB_VIEW_EXTRA_NAME_TITLE = "Title";
    public static final String WEB_VIEW_EXTRA_NAME_HASTITLELAYOUT = "hasTitleLayout";

    @Nullable
    private String mLoadUrl = null;
    @Nullable
    private String mTitle = null;
    private WebView mWebView;
    private ProgressBar mProgressBar;
    private boolean mHasTitleLayout;
    private SwipeRefreshLayout mRefreshLayout;
    private TitleBackLayout mTitleLayout;
    private View topView;
    private View mRlContent;

    public static void open(@NonNull Context activity, String title, String url) {
        open(activity, title, url, true);
    }

    public static void open(@NonNull Context context, String title, String url, boolean hasTitleLayout) {
        Intent intent2 = new Intent(context, WebViewActivity.class);
        intent2.putExtra(WebViewActivity.WEB_VIEW_EXTRA_NAME_TITLE, title);
        intent2.putExtra(WebViewActivity.WEB_VIEW_EXTRA_NAME_URL, url);
        intent2.putExtra(WebViewActivity.WEB_VIEW_EXTRA_NAME_HASTITLELAYOUT, hasTitleLayout);
        if (context instanceof Application) {
            intent2.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        context.startActivity(intent2);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webview);
        topView = findViewById(R.id.layout_title);

        Intent intent = getIntent();
        if (intent != null) {
            mLoadUrl = intent.getStringExtra(WEB_VIEW_EXTRA_NAME_URL);
            mTitle = intent.getStringExtra(WEB_VIEW_EXTRA_NAME_TITLE);
            mHasTitleLayout = intent.getBooleanExtra(WEB_VIEW_EXTRA_NAME_HASTITLELAYOUT, true);
        }

        if (EmptyUtils.isEmpty(mLoadUrl)) {
            this.finish();
            return;
        }
//        KeyBoardListener.getInstance(this).init();
        initView();
    }

    @Override
    protected View getTopView() {
        if (mHasTitleLayout) {
            return topView;
        } else {
            return mRlContent;
        }
    }

    @Nullable
    @Override
    protected TipsBar getTipsBar() {
        return findViewById(R.id.tipsBar);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    /**
     * Init View By ID
     */
    private void initView() {
        mRlContent = findViewById(R.id.rl_content);
        mTitleLayout = findViewById(R.id.layout_title);
        mTitleLayout.setOnClickBack(this);
        mTitleLayout.setBackTitle(mTitle);
        mTitleLayout.setVisibility(mHasTitleLayout ? View.VISIBLE : View.GONE);
        mProgressBar = findViewById(R.id.progressbar);
        mRefreshLayout = findViewById(R.id.swipe_refresh_layout);
        mRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

//                mWebView.loadUrl("javascript:window.location.reload( true )");
                mWebView.reload();
                LogUtils.d(TAG, "onRefresh: reload");
            }
        });
        mRefreshLayout.getViewTreeObserver().addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener() {
            @Override
            public void onScrollChanged() {
                mRefreshLayout.setEnabled(mWebView.getScrollY() == 0);
            }
        });
        mRefreshLayout.setNestedScrollingEnabled(true);
        mWebView = findViewById(R.id.web_view);
        mWebView.loadUrl(mLoadUrl);
        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(@NonNull WebView view, String url) {
                view.loadUrl(url);

                return true;
            }
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mWebView.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }
        mWebView.setWebChromeClient(new WebChromeClient());
        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                if (mRefreshLayout.isRefreshing())
                    mRefreshLayout.setRefreshing(false);
                if (!mWebView.getSettings().getLoadsImagesAutomatically()) {
                    mWebView.getSettings().setLoadsImagesAutomatically(true);
                }
                mWebView.clearHistory();

            }

            @Override
            public void onPageCommitVisible(WebView view, String url) {
                super.onPageCommitVisible(view, url);
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                if (!mRefreshLayout.isRefreshing())
                    mRefreshLayout.setRefreshing(true);
            }

            @Override
            public void onReceivedSslError(@NonNull WebView view, @NonNull SslErrorHandler handler, @NonNull SslError error) {
                Logger.LOGE(TAG, "error " + error.toString());
                if (BuildConfig.DEBUG) {
                    handler.proceed();
                } else {
                    if (error.getPrimaryError() == SslError.SSL_DATE_INVALID ||
                            error.getPrimaryError() == SslError.SSL_EXPIRED ||
                            error.getPrimaryError() == SslError.SSL_IDMISMATCH ||
                            error.getPrimaryError() == SslError.SSL_UNTRUSTED)
                        handlerError(handler, view.getUrl());
                    else
                        handler.cancel();
                }
            }
        });
        mWebView.setDownloadListener(new DownloadListener() {

            @Override
            public void onDownloadStart(String url, String userAgent, String contentDisposition,
                                        String mimetype, long contentLength) {
                // Uri uri = Uri.parse(url);
                // Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                // startActivity(intent);

                String fileName = URLUtil.guessFileName(url, contentDisposition, mimetype);
                Logger.LOGD(TAG, ">>>> Download, Url=" + url + "; Agent=" + userAgent + "; Content="
                        + contentDisposition + "; MimeType=" + mimetype + "; Len=" + contentLength
                        + "; Guess FileName=" + fileName);

                DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
                request.setMimeType(mimetype);
                request.allowScanningByMediaScanner();
                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName);
                DownloadManager dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
                if (dm != null) {
                    dm.enqueue(request);
                    ToastHelper.showToast(R.string.start_download_file);
                }
            }
        });

        WebSettings settings = mWebView.getSettings();
        settings.setJavaScriptEnabled(false); // 支持js
//        settings.setUseWideViewPort(true); // 将图片调整到适合webview的大小
//        settings.setSupportZoom(false); // 支持缩放
//        settings.setLayoutAlgorithm(LayoutAlgorithm.NORMAL); // 支持内容重新布局
        settings.supportMultipleWindows(); // 多窗口
        // settings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK); //关闭webview中缓存
        settings.setAllowFileAccess(false); // 设置可以访问文件
        settings.setNeedInitialFocus(true); // 当webview调用requestFocus时为webview设置节点
        settings.setJavaScriptCanOpenWindowsAutomatically(true); // 支持通过JS打开新窗口
//        settings.setLoadWithOverviewMode(true); // 缩放至屏幕的大小
        settings.setLoadsImagesAutomatically(true); // 支持自动加载图片
        settings.setSupportZoom(true);
        settings.setBuiltInZoomControls(true); // 设置支持缩放
        settings.setDisplayZoomControls(false); // 显示缩放工具

        settings.setAllowContentAccess(false);
        settings.setDatabaseEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setAppCacheEnabled(true);
        settings.setSaveFormData(false);
        mWebView.clearCache(true);
        mWebView.getSettings().setAllowFileAccess(false);
        mWebView.getSettings().setAllowContentAccess(false);
        mWebView.getSettings().setAllowFileAccessFromFileURLs(false);
        mWebView.getSettings().setAllowUniversalAccessFromFileURLs(false);
//        settings.setUseWideViewPort(true);
//        settings.setLoadWithOverviewMode(true);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (mWebView.canGoBack() && mHasTitleLayout) {
                mWebView.goBack();// 返回上一页面
                return true;
            } else {
                finish();
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    private class WebChromeClient extends android.webkit.WebChromeClient {
        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            if (newProgress > 66) {
                if (mRefreshLayout.isRefreshing())
                    mRefreshLayout.setRefreshing(false);
            }
            if (newProgress == 100) {
                mProgressBar.setVisibility(View.GONE);
            } else {
                if (mProgressBar.getVisibility() == View.GONE)
                    mProgressBar.setVisibility(View.VISIBLE);
                mProgressBar.setProgress(newProgress);
            }

            super.onProgressChanged(view, newProgress);
        }

        @Override
        public boolean onJsAlert(WebView view, String url, final String message, @NonNull JsResult result) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(WebViewActivity.this, message, Toast.LENGTH_SHORT).show();
                }
            });
            result.confirm();
            return true;
        }
    }

    private void handlerError(@NonNull final SslErrorHandler handler, @NonNull String url) {
        OkHttpClient.Builder builder;
        try {
            String[] cers = getAssets().list("cers");
            Logger.LOGD(TAG, " cers : " + cers, cers.length);

            List<InputStream> streams = new ArrayList<>();
            for (String cer : cers) {
                streams.add(getAssets().open(cer));
            }
            builder = setCertificates(new OkHttpClient.Builder(), streams);
            Request request = new Request.Builder().url(url)
                    .build();
            builder.build().newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    LogUtils.e("ok error>>", e.getMessage());
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(WebViewActivity.this, getResources().getString(R.string.ec_request), Toast.LENGTH_SHORT).show();
                        }
                    });
                    handler.cancel();
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    Logger.LOGI("ok sucess  >> ", response.body().string());
                    handler.proceed();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    @NonNull
    public OkHttpClient.Builder setCertificates(@NonNull OkHttpClient.Builder client, @NonNull List<InputStream> streams) {
        try {
            CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
            KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            keyStore.load(null);
            int index = 0;
            for (InputStream certificate : streams) {
                String certificateAlias = Integer.toString(index++);
                keyStore.setCertificateEntry(certificateAlias, certificateFactory.generateCertificate(certificate));
                try {
                    if (certificate != null)
                        certificate.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            SSLContext sslContext = SSLContext.getInstance("TLS");
            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance
                    (TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init(keyStore);

            TrustManager[] trustManagers = trustManagerFactory.getTrustManagers();
            if (trustManagers.length != 1 || !(trustManagers[0] instanceof X509TrustManager)) {
                throw new IllegalStateException("Unexpected default trust managers:"
                        + Arrays.toString(trustManagers));
            }
            X509TrustManager trustManager = (X509TrustManager) trustManagers[0];
            sslContext.init(null, new TrustManager[]{trustManager}, null);
            SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

            client.sslSocketFactory(sslSocketFactory, trustManager);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return client;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mWebView.clearHistory();
    }
}