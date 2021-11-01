package net.sdvn.nascommon.model.oneos.backup;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;

public class BackupBroadcast extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, @NonNull Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())
            /* || Intent.ACTION_USER_PRESENT.equals(intent.getAction()) */) {
            // Intent startServiceIntent = new Intent(context, BackupService.class);
            // startServiceIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            // context.startService(startServiceIntent);
        }
    }
}