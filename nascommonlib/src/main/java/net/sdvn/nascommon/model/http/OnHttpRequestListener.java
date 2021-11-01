package net.sdvn.nascommon.model.http;

import androidx.annotation.Keep;

/**
 * @author gaoyun@eli-tech.com
 * @date 2018/03/19
 */
@Keep
public interface OnHttpRequestListener {
    void onStart(String url);

    void onSuccess(String url, String result);

    void onFailure(String url, int httpCode, int errorNo, String strMsg);
}
