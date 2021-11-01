package net.sdvn.nascommon.iface;

public interface EventListener<T> {
    void onStart(String url);

    void onSuccess(String url, T data);

    void onFailure(String url, int errorNo, String errorMsg);

}