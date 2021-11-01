package net.sdvn.nascommon.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;


public class LocalDeviceStateManager extends BroadcastReceiver {

    private static final String TAG = LocalDeviceStateManager.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent != null && intent.getAction() != null) {
            LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
//            Logger.LOGD(TAG, intent);
        }
    }

    public static LocalDeviceStateManager register(Context context) {
        LocalDeviceStateManager lbsm = new LocalDeviceStateManager();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        filter.addAction(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_USER_PRESENT);
        filter.addAction(Intent.ACTION_BATTERY_LOW);
        filter.addAction(Intent.ACTION_BATTERY_CHANGED);
        filter.addAction(Intent.ACTION_BATTERY_OKAY);
        context.registerReceiver(lbsm, filter);
        return lbsm;
    }

}
