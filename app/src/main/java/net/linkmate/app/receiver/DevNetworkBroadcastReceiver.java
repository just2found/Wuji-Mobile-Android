package net.linkmate.app.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import androidx.annotation.NonNull;

import net.linkmate.app.base.MyApplication;

import java.util.ArrayList;

import static android.net.ConnectivityManager.CONNECTIVITY_ACTION;

/**
 * Created by yun on 2017/11/17.
 */

public class DevNetworkBroadcastReceiver extends BroadcastReceiver {

    public DevNetworkBroadcastReceiver() {
        if (observers == null)
            observers = new ArrayList<>();
    }

    private static class InstanceHolder {
        private static final DevNetworkBroadcastReceiver instance = new DevNetworkBroadcastReceiver();
    }

    public static DevNetworkBroadcastReceiver getInstance() {
        return InstanceHolder.instance;
    }

    public final String TAG = DevNetworkBroadcastReceiver.this.getClass().getSimpleName();
    private ArrayList<DevNetworkChangedObserver> observers;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (equals(CONNECTIVITY_ACTION, intent.getAction())) {
            ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetworkInfo = null;
            if (connectivityManager != null) {
                activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            }
            setChanged();
            boolean isConnected = activeNetworkInfo != null && activeNetworkInfo.isConnected();
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
//                    connectivityManager!=null&&connectivityManager.getActiveNetwork()!=null) {
//                NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(connectivityManager.getActiveNetwork());
//                isConnected = capabilities != null && capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) && capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED);
//            }
            if (isConnected) {
                MyApplication.refreshNetworkTypeState();
                notifyAllObservers(true);
            } else
                notifyAllObservers(false);
        }
    }

    /**
     * 注册网络监听
     */
    public void registerReceiver(@NonNull Context context) {
        IntentFilter intentFilter = new IntentFilter(CONNECTIVITY_ACTION);
        context.registerReceiver(InstanceHolder.instance, intentFilter);
    }

    /**
     * 取消网络监听
     */
    public void unregisterReceiver(@NonNull Context context) {
        context.unregisterReceiver(InstanceHolder.instance);
    }


    private void notifyAllObservers(boolean isConnected) {
        notifyObservers(isConnected);
    }

    public void notifyObservers(Object arg) {

        DevNetworkChangedObserver[] arrLocal;

        synchronized (this) {
            if (!hasChanged())
                return;
            arrLocal = observers.toArray(new DevNetworkChangedObserver[observers.size()]);
            clearChanged();
        }

        for (int i = arrLocal.length - 1; i >= 0; i--)
            arrLocal[i].update(this, arg);
    }

    public boolean equals(Object a, Object b) {
        return (a == b) || (a != null && a.equals(b));
    }

    public interface DevNetworkChangedObserver {
        void update(BroadcastReceiver receiver, Object arg);
    }

    /**
     * 注册网络变化Observer
     */
    public void registerObserver(DevNetworkChangedObserver observer) {
        addObserver(observer);
    }

    /**
     * 取消网络变化Observer的注册
     */
    public void unregisterObserver(DevNetworkChangedObserver observer) {
        deleteObserver(observer);
    }

    public synchronized void addObserver(DevNetworkChangedObserver o) {
        if (o == null)
            throw new NullPointerException();
        if (!observers.contains(o)) {
            observers.add(o);
        }
    }

    public synchronized void deleteObserver(DevNetworkChangedObserver o) {
        observers.remove(o);
    }

    protected synchronized void setChanged() {
        changed = true;
    }

    protected synchronized void clearChanged() {
        changed = false;
    }

    public synchronized boolean hasChanged() {
        return changed;
    }

    public synchronized int countObservers() {
        return observers.size();
    }

    private boolean changed = false;
}
