package net.linkmate.app.ui.fragment.main;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import net.linkmate.app.BuildConfig;
import net.linkmate.app.R;
import net.linkmate.app.ui.fragment.BaseFragment;
import net.sdvn.nascommon.model.UiUtils;

public class StoreFragment extends BaseFragment {
    private WebView mWebView;

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_store;
    }

    @Override
    protected void initView(View view, Bundle savedInstanceState) {
        bindView(view);
    }

    @Override
    protected View getTopView() {
//        if (getActivity() != null) {
//            mWebView.setPadding(0, UIUtils.getStatueBarHeight(getActivity()), 0, 0);
//        }
        return mWebView;
    }

    @Override
    public void refreshData() {
        super.refreshData();
        WebSettings webSettings = mWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);//只从网络加载
        webSettings.setAllowFileAccess(false);
        webSettings.setAllowFileAccessFromFileURLs(false);
        webSettings.setAllowUniversalAccessFromFileURLs(false);
        mWebView.setScrollBarStyle(View.SCROLLBARS_OUTSIDE_OVERLAY);//取消滚动条

        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return super.shouldOverrideUrlLoading(view, url);
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                return super.shouldOverrideUrlLoading(view, request);
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                super.onReceivedError(view, request, error);
                if (!UiUtils.isEn()) {
                    mWebView.loadUrl("file:///android_asset/index.html");
                } else {
                    mWebView.loadUrl("file:///android_asset/index-en.html");
                }
            }

        });
        if (BuildConfig.DEBUG) {
            mWebView.loadUrl("https://www.izzbie.com/buy");
        } else {
            mWebView.loadUrl("https://store.izzbie.com/");
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mWebView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mWebView.onPause();
    }

    @Override
    public void onDestroy() {
        if (mWebView != null) {
            ((ViewGroup) mWebView.getParent()).removeView(mWebView);
            mWebView.stopLoading();
            mWebView.destroy();
            mWebView.setWebChromeClient(null);
            mWebView.setWebViewClient(null);
            mWebView = null;
        }
        super.onDestroy();
    }

    private void bindView(View bindSource) {
        mWebView = bindSource.findViewById(R.id.store_wv);
    }
}
