package net.sdvn.nascommon.utils;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.runtime.Permission;

import net.sdvn.nascommon.iface.Callback;
import net.sdvn.nascommon.model.permission.RuntimeRationale;
import net.sdvn.nascommonlib.R;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import timber.log.Timber;

public class PermissionChecker {
    private static final int REQUEST_CODE_SETTING = 1118;
    private static AtomicBoolean started = new AtomicBoolean(false);

    @SuppressLint("WrongConstant")
    public static void checkPermission(@NonNull Context context, Callback<List<String>> grantedRun,
                                       Callback<List<String>> deniedRun, @NonNull String... permissions) {
        if (started.compareAndSet(false, true)) {
            if (!AndPermission.hasPermissions(context, permissions)) {
                AndPermission.with(context)
                        .runtime()
                        .permission(permissions)
                        .onGranted(strings -> {
                            started.set(false);
                            if (grantedRun != null) grantedRun.result(strings);
                        })
                        .onDenied(strings -> {
                            if (AndPermission.hasAlwaysDeniedPermission(context, strings)) {
                                showSettingDialog(context, strings, deniedRun);
                            } else {
                                if (deniedRun != null) deniedRun.result(strings);
                                started.set(false);
                            }
                        })
                        .rationale(new RuntimeRationale())
                        .start();
            } else {
                if (grantedRun != null)
                    grantedRun.result(Arrays.asList(permissions));
                started.set(false);
            }
        } else {
            Timber.d("checker permission running");
        }
    }

    public static void showSettingDialog(Context context, final List<String> permissions, Callback<List<String>> deniedRun) {
        List<String> permissionNames = Permission.transformText(context, permissions);
        String message = context.getString(R.string.message_permission_always_failed,
                TextUtils.join("\n", permissionNames));

        new AlertDialog.Builder(context).setCancelable(false)
                .setTitle(R.string.title_dialog)
                .setMessage(message)
                .setPositiveButton(R.string.settings, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        setPermission(context);
                        started.set(false);
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        started.set(false);
                        if (deniedRun != null) {
                            deniedRun.result(permissions);
                        }
                    }
                })
                .show();
    }

    /**
     * Set permissions.
     */
    public static void setPermission(Context context) {
        AndPermission.with(context).runtime().setting().start(REQUEST_CODE_SETTING);
    }
}
