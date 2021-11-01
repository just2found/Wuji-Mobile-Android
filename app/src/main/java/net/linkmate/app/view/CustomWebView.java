package net.linkmate.app.view;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Build;
import android.util.AttributeSet;
import android.webkit.WebView;

/**
 * 修复android 5.XX  webview bugs
 * android.content.res.Resources$NotFoundException
 * <p>
 * String resource ID #0x2040003
 * <p>
 * 解析原始
 * 1 android.view.InflateException:Binary XML file line #6: Error inflating class android.webkit.WebView
 * 2 android.view.LayoutInflater.createView(LayoutInflater.java:642)
 */
public class CustomWebView extends WebView {
    public CustomWebView(Context context) {
        super(getFixedContext(context));
    }

    public CustomWebView(Context context, AttributeSet attrs) {
        super(getFixedContext(context), attrs);
    }

    public CustomWebView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(getFixedContext(context), attrs, defStyleAttr);
    }

    public static Context getFixedContext(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return context.createConfigurationContext(new Configuration());
        } else {
            return context;
        }
    }
}