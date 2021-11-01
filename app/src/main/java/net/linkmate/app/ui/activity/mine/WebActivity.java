package net.linkmate.app.ui.activity.mine;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.webkit.JsResult;
import android.webkit.SslErrorHandler;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import net.linkmate.app.BuildConfig;
import net.linkmate.app.R;
import net.linkmate.app.base.BaseActivity;
import net.linkmate.app.view.TipsBar;
import net.linkmate.app.view.helper.KeyBoardListener;
import net.sdvn.app.config.AppConfig;
import net.sdvn.cmapi.util.LogUtils;

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
import timber.log.Timber;


public class WebActivity extends BaseActivity implements ViewTreeObserver.OnScrollChangedListener {

    private final String TAG = WebActivity.this.getClass().getSimpleName();
    private ImageView ivLeft;
    private TextView tvTitle;
    private RelativeLayout itbRl;
    private ProgressBar mProgressBar;
    private WebView mWebView;
    private SwipeRefreshLayout mRefreshLayout;
    private String sllType;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        setTheme(R.style.AppCompatThemeDark);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web);
        bindView(this.getWindow().getDecorView());
        onEvent();
        mRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mWebView.reload();
            }
        });
        mRefreshLayout.getViewTreeObserver().addOnScrollChangedListener(this);
        mRefreshLayout.setNestedScrollingEnabled(true);
        KeyBoardListener.getInstance(this).init();

        mWebView.clearCache(true);
        mWebView.getSettings().setAllowFileAccess(false);
        mWebView.getSettings().setAllowContentAccess(false);
        mWebView.getSettings().setAllowFileAccessFromFileURLs(false);
        mWebView.getSettings().setAllowUniversalAccessFromFileURLs(false);
    }

    @Override
    public void onScrollChanged() {
        mRefreshLayout.setEnabled(mWebView.getScrollY() == 0);
    }

    @Override
    protected void onDestroy() {
        mWebView.getSettings().setJavaScriptEnabled(false);
        mWebView.clearHistory();
        super.onDestroy();
    }

    @Nullable
    @Override
    protected TipsBar getTipsBar() {
        return findViewById(R.id.tipsBar);
    }

    public void onEvent() {
        Intent data = getIntent();
        sllType = data.getStringExtra("sllType");
        String title = data.getStringExtra("title");
        String url = data.getStringExtra("url");
        boolean enableScript = data.getBooleanExtra("enableScript", true);
        boolean connectionState = data.getBooleanExtra("ConnectionState", true);
        setConnectionState(connectionState);

        tvTitle.setTextColor(getResources().getColor(R.color.title_text_color));
        tvTitle.setText(title);
        ivLeft.setVisibility(View.VISIBLE);
        ivLeft.setImageResource(R.drawable.icon_return);

        ivLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        mWebView.clearCache(true);
        mWebView.clearHistory();


        WebSettings settings = mWebView.getSettings();
        settings.setJavaScriptEnabled(enableScript);
        //设置自适应屏幕，两者合用
        settings.setUseWideViewPort(false); //将图片调整到适合webview的大小
        settings.setLoadWithOverviewMode(true); // 缩放至屏幕的大小
        settings.setSupportZoom(true);
        settings.setAppCacheEnabled(true);

        // DomStorage设置。
        settings.setDatabaseEnabled(true);
        settings.setDomStorageEnabled(true);
        if (Build.VERSION.SDK_INT >= 19) {
            mWebView.getSettings().setLoadsImagesAutomatically(true);
        } else {
            mWebView.getSettings().setLoadsImagesAutomatically(false);
        }

        //其他细节操作
//        settings.setAllowContentAccess(true);
//        settings.setAllowFileAccess(true);
        settings.setCacheMode(WebSettings.LOAD_NO_CACHE); //webview中缓存
        settings.setJavaScriptCanOpenWindowsAutomatically(true); //支持通过JS打开新窗口
        settings.setLoadsImagesAutomatically(true); //支持自动加载图片
        settings.setDefaultTextEncodingName("utf-8");//设置编码格式
        settings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);//支持内容从新布局
        mWebView.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
        mWebView.setHorizontalScrollBarEnabled(false);
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
            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                LogUtils.e(TAG, "error " + error.toString());
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

        Timber.d(url);
        mWebView.loadUrl(url);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK) && mWebView.canGoBack()) {
            mWebView.goBack();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }


    @Override
    public void onBackPressed() {
        if (mWebView.canGoBack()) {
            mWebView.goBack();// 返回前一个页面
            return;
        }
        super.onBackPressed();
    }

    private void bindView(View bindSource) {
        ivLeft = bindSource.findViewById(R.id.itb_iv_left);
        tvTitle = bindSource.findViewById(R.id.itb_tv_title);
        itbRl = bindSource.findViewById(R.id.itb_rl);
        mProgressBar = bindSource.findViewById(R.id.act_web_progressbar);
        mWebView = bindSource.findViewById(R.id.act_web_webview);
        mRefreshLayout = bindSource.findViewById(R.id.act_web_swipe_refresh);
    }


    private class WebChromeClient extends android.webkit.WebChromeClient {
        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            if (newProgress > 66) {
                if (mRefreshLayout.isRefreshing())
                    mRefreshLayout.setRefreshing(false);
            }
            if (newProgress >= 95) {
                mProgressBar.setVisibility(View.GONE);
            } else {
                if (mProgressBar.getVisibility() == View.GONE)
                    mProgressBar.setVisibility(View.VISIBLE);
                mProgressBar.setProgress(newProgress);
            }
            super.onProgressChanged(view, newProgress);
        }

        @Override
        public boolean onJsAlert(WebView view, String url, final String message, JsResult result) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(WebActivity.this, message, Toast.LENGTH_SHORT).show();

                }
            });
            result.confirm();
            return true;
        }
    }

    private void handlerError(final SslErrorHandler handler, String url) {
        OkHttpClient.Builder builder;
        try {

            String fileName1 = AppConfig.ssl_app_file_name_1;
            String fileName2 = AppConfig.ssl_app_file_name_2;
            if ("app".equals(sllType)) {
                fileName1 = AppConfig.ssl_app_file_name_1;
                fileName2 = AppConfig.ssl_app_file_name_2;
            } else if ("home".equals(sllType)) {
                fileName1 = AppConfig.ssl_home_file_name_1;
                fileName2 = AppConfig.ssl_home_file_name_2;
            }
            InputStream is1 = getAssets().open(fileName1);
            InputStream is2 = getAssets().open(fileName2);

//            builder = setCertificates(new OkHttpClient.Builder(),
////                    getAssets().open("thawte_root.cer"),
////                    getAssets().open("thawte_root_ca.cer"),
////                    getAssets().open("DigCert_root.cer"),
////                    getAssets().open("thawte_2018.cer"),
////                    getAssets().open("certum_trusted_network_root.cer"),
////                    getAssets().open("wosign.cer")
//                    is1,
//                    is2
//            );

            List<InputStream> streams = new ArrayList<>();
            streams.add(is1);
            streams.add(is2);
            builder = setCertificates(new OkHttpClient.Builder(), streams);

            Request request = new Request.Builder().url(url)
                    .build();
            builder.build().newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    LogUtils.e("ok error>>", e.getMessage());
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(WebActivity.this, getResources().getString(R.string.ec_request), Toast.LENGTH_SHORT).show();
                        }
                    });
                    handler.cancel();
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
//                    LogUtils.e("ok sucess  >> ", response.body().string());
                    handler.proceed();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    //OkHttp3.9版本
//    public OkHttpClient.Builder setCertificates(OkHttpClient.Builder client, InputStream... streams) {
//        try {
//            CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
//            KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
//            keyStore.load(null);
//            int index = 0;
//            for (InputStream certificate : streams) {
//                String certificateAlias = Integer.toString(index++);
//                keyStore.setCertificateEntry(certificateAlias, certificateFactory.generateCertificate(certificate));
//                try {
//                    if (certificate != null)
//                        certificate.close();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//            SSLContext sslContext = SSLContext.getInstance("TLS");
//            TrustManagerFactory trustManagerFactory =
//                    TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
//            trustManagerFactory.init(keyStore);
//            sslContext.init(null, trustManagerFactory.getTrustManagers(), new SecureRandom());
//            SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();
//            X509TrustManager trustManager =
//                    Platform.get().trustManager(sslSocketFactory);
//            client.sslSocketFactory(sslSocketFactory, trustManager);
//        } catch (IllegalAccessError | Exception e) {
//            e.printStackTrace();
//        }
//        return client;
//    }

    //OkHttp3.11以后的版本
    public OkHttpClient.Builder setCertificates(OkHttpClient.Builder client, List<InputStream> streams) {
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
}
