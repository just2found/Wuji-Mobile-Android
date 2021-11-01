package net.sdvn.nascommon.utils;

import android.Manifest;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.telephony.SmsManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;

import com.yanzhenjie.permission.Action;
import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.Rationale;
import com.yanzhenjie.permission.RequestExecutor;

import net.sdvn.cmapi.util.ToastUtil;
import net.sdvn.nascommon.model.UiUtils;
import net.sdvn.nascommon.utils.log.Logger;
import net.sdvn.nascommonlib.R;

import java.util.ArrayList;
import java.util.List;

public class SmsUtils extends BroadcastReceiver implements LifecycleObserver {
    private final static String TAG = SmsUtils.class.getSimpleName();
    public final String SENT_SMS_ACTION = "SENT_SMS_ACTION";
    public final String DELIVERED_SMS_ACTION = "DELIVERED_SMS_ACTION";
    private FragmentActivity mActivity;
    Lifecycle lifecycle;
    boolean enable;

    public void setLifecycle(Lifecycle lifecycle) {
        this.lifecycle = lifecycle;
        this.lifecycle.addObserver(this);
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    public void unregister() {
        Logger.p(Logger.Level.INFO, true, TAG, "Lifecycle.Event.ON_DESTROY");
        if (enable) {
            mActivity.unregisterReceiver(this);
            lifecycle.removeObserver(this);
            enable = false;
        }
    }

    @Override
    public void onReceive(Context context, @NonNull Intent intent) {
        if (SENT_SMS_ACTION.equals(intent.getAction())) {
            switch (getResultCode()) {
                case Activity.RESULT_OK:
                    if (enable)
                        Toast.makeText(mActivity,
                                mActivity.getString(R.string.send_sms_success), Toast.LENGTH_SHORT)
                                .show();
                    return;
                case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                    break;
                case SmsManager.RESULT_ERROR_RADIO_OFF:
                    break;
                case SmsManager.RESULT_ERROR_NULL_PDU:
                    break;
                default:
                    break;
            }
            if (enable)
                ToastUtil.showToast(mActivity, mActivity.getString(R.string.send_sms_error));
        } else if (DELIVERED_SMS_ACTION.equals(intent.getAction())) {
            if (enable)
                Toast.makeText(mActivity, mActivity.getString(R.string.receive_sms_success), Toast.LENGTH_SHORT)
                        .show();

        }
    }

    private SmsUtils(FragmentActivity mActivity) {
        this.mActivity = mActivity;
        setLifecycle(mActivity.getLifecycle());
    }


    public static SmsUtils init(@NonNull FragmentActivity activity) {
        return new SmsUtils(activity);
    }

    /**
     * 邀请成为设备用户，分享或不分享文件
     */
    public void sendInviteSms(String number, String msg) {
        checkSmsPermissions(number, msg);
    }

    /**
     * 分享文件
     */
    public void sendShareSms(String number, String msg) {
        checkSmsPermissions(number, msg);
    }

    private void checkSmsPermissions(final String number, final String msg) {
        final String permission;
        permission = Manifest.permission.SEND_SMS;
        AndPermission.with(mActivity)
                .runtime()
                .permission(permission)
                .rationale(new Rationale<List<String>>() {
                    @Override
                    public void showRationale(Context context, List<String> strings, RequestExecutor requestExecutor) {
                        ToastUtil.showToast(mActivity, mActivity.getString(R.string.permission_denied_sms));
                    }
                })
                .onDenied(new Action<List<String>>() {
                    @Override
                    public void onAction(List<String> strings) {
                        showSettings();
                    }
                })
                .onGranted(new Action<List<String>>() {
                    @Override
                    public void onAction(List<String> strings) {
                        if (AndPermission.hasPermissions(mActivity, permission)) {
                            sendSMS(number, msg);
                        } else {
                            showSettings();
                        }
                    }
                })
                .start();
    }

    private void showSettings() {
        UiUtils.showSettings(mActivity, false);
    }


    private void sendSMS(String number, String message) {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(SENT_SMS_ACTION);
        intentFilter.addAction(DELIVERED_SMS_ACTION);
        mActivity.registerReceiver(this, intentFilter);
        enable = true;

        Intent sentIntent = new Intent(SENT_SMS_ACTION);
        PendingIntent sentPI = PendingIntent.getBroadcast(mActivity, 0, sentIntent,
                0);

        Intent deliverIntent = new Intent(DELIVERED_SMS_ACTION);
        PendingIntent deliverPI = PendingIntent.getBroadcast(mActivity, 0,
                deliverIntent, 0);

        try {
            //获取短信管理器
            SmsManager smsManager = SmsManager.getDefault();
            //拆分短信内容（手机短信长度限制）
            Logger.LOGI(TAG, "sms message :" + message);
            ArrayList<String> divideContents = smsManager.divideMessage(message);
            ArrayList<PendingIntent> deliverPIs = new ArrayList<>();
            deliverPIs.add(deliverPI);
            ArrayList<PendingIntent> sentPIs = new ArrayList<>();
            sentPIs.add(sentPI);
            smsManager.sendMultipartTextMessage(number, null, divideContents, sentPIs, deliverPIs);
        } catch (Exception e) {
            ToastUtil.showToast(mActivity, mActivity.getString(R.string.send_sms_error));
        }
    }
}
