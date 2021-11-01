package net.linkmate.app.util.business;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Environment;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import com.yanzhenjie.permission.AndPermission;

import net.linkmate.app.R;
import net.linkmate.app.bean.DeviceBean;
import net.linkmate.app.manager.DevManager;
import net.linkmate.app.util.DialogUtil;
import net.linkmate.app.util.FormatUtils;
import net.linkmate.app.util.ToastUtils;
import net.linkmate.app.view.DataItemLayout;
import net.sdvn.cmapi.CMAPI;
import net.sdvn.cmapi.LocalDeviceStatus;
import net.sdvn.cmapi.global.Constants;
import net.sdvn.cmapi.util.ClipboardUtils;
import net.sdvn.common.ErrorCode;
import net.sdvn.common.internet.core.GsonBaseProtocol;
import net.sdvn.common.internet.core.HttpLoader;
import net.sdvn.common.internet.listener.ResultListener;
import net.sdvn.common.internet.loader.BindDeviceHttpLoader;
import net.sdvn.nascommon.iface.Result;
import net.sdvn.nascommon.rx.RxWork;
import net.sdvn.nascommon.utils.AnimUtils;
import net.sdvn.nascommon.utils.DialogUtils;

import java.io.File;
import java.util.Calendar;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import io.weline.devhelper.DevTypeHelper;
import timber.log.Timber;

public class DeviceLocalDialogUtil extends RxWork implements HttpLoader.HttpLoaderStateListener {

    private final Context context;
    private final DeviceBean bean;
    private final int position;
    private String rx;
    private String tx;
    private String dltRx;
    private String dltTx;
    private String delay;
    private String deration;

    public DeviceLocalDialogUtil(Context context, DeviceBean bean, int position) {
        this.context = context;
        this.bean = bean;
        this.position = position;
    }

    private Dialog localDevDialog;
    private View sv;
    @NonNull
    private View loadingView;

    private DataItemLayout dilName;
    private DataItemLayout dilOwner;
    private DataItemLayout dilAddTime;
    private DataItemLayout dilVersion;
    private DataItemLayout dilId;
    private DataItemLayout dilSn;
    private DataItemLayout dilStatus;
    private DataItemLayout dilVip;
    private DataItemLayout dilLanIp;

    //local data
    private View localDataLayout;
    private View localDataBack;
    private View localDataRefresh;
    private DataItemLayout dilUptime;
    private DataItemLayout dilRx;
    private DataItemLayout dilTx;
    private DataItemLayout dilDltRx;
    private DataItemLayout dilDltTx;
    private DataItemLayout dildelay;

    private Switch remoteSwitch;
    private TextView btnUpgrade;
    private TextView btnLocalData;
    private TextView btnLogcat;
    private TextView btnReboot;
    private TextView btnBind;

    private int dialogLevel;

    private boolean onBackPress() {
        return onBackPress(sv, localDataLayout);
    }

    private boolean onBackPress(View visibView, View... goneViews) {
        if (dialogLevel <= 0 || localDevDialog == null || !localDevDialog.isShowing()) {
            dialogLevel = 0;
            return false;
        } else {
            if (dialogLevel == 2) {
                for (View view : goneViews) {
                    if (view != null)
                        view.setVisibility(View.GONE);
                }
                visibView.setVisibility(View.VISIBLE);
            } else if (dialogLevel == 1) {
                localDevDialog.dismiss();
            }
            dialogLevel--;
            return true;
        }
    }

    private View.OnClickListener listener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.device_local_upgrade:
                    showUpgradeDialog();
                    break;
                case R.id.device_local_lldrd:
                    sv.setVisibility(View.GONE);
                    localDataLayout.setVisibility(View.VISIBLE);
                    dialogLevel++;
                    refreshStatus();
                    break;
                case R.id.lldrd_btn_refresh:
                    refreshStatus();
                    break;
                case R.id.lldrd_iv_back:
                    onBackPress();
                    break;
                case R.id.device_local_get_logcat:
                    logOperation();
                    break;
                case R.id.device_local_btn_bind:
                    if (DevTypeHelper.isIzzbieOne(bean.getDevClass())) {
                        izzbieBindDevice();
                    } else {
                        if (bean.getLocalData() != null) {
                            String deviceSn = bean.getLocalData().getDeviceSn();
                            String appId = bean.getLocalData().getAppId();
                            otherBindDevice(v, deviceSn, appId);
                        } else {
                            ToastUtils.showToast(R.string.get_real_time_status_info_fail);
                        }
                    }
                    break;
                case R.id.device_local_reboot:
                    showPowerDialog(false);
                    break;
            }
        }
    };

    private void showPowerDialog(final boolean isPowerOff) {
        int contentRes = isPowerOff ? R.string.confirm_power_off_device : R.string.confirm_reboot_device;
        DialogUtils.showConfirmDialog(context, R.string.tips, contentRes, R.string.confirm, R.string.cancel, new DialogUtils.OnDialogClickListener() {
            @Override
            public void onClick(DialogInterface dialog, boolean isPositiveBtn) {
                if (isPositiveBtn) {
                    int result = CMAPI.getInstance().rebootDevice(bean.getVip());
                    if (result == Constants.CE_SUCC) {
                        ToastUtils.showToast(R.string.success_reboot_device);
                    } else {
                        ToastUtils.showToast(ErrorCode.error2String(result));
                        Timber.d("reboot result : %s", result);
                    }
                }
            }
        });
    }

    public void showDetailDialog() {
        if (localDevDialog != null && localDevDialog.isShowing()) {
            return;
        }
        final View view = LayoutInflater.from(context).inflate(R.layout.layout_dialog_device_local, null);
        localDevDialog = new Dialog(context, R.style.DialogTheme);
        localDevDialog.setContentView(view);
        localDevDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                dispose();
                if (isCollectingLogs) {
                    CMAPI.getInstance().stopGetLocalDeviceLog();
                    ToastUtils.showToast(R.string.collecting_logs_stop);
                }
            }
        });

        initView(view);

        dilName.setText(bean.getName());
        dilVersion.setText(bean.getVersion());
        dilId.setText(bean.getId());
        dilSn.setText(bean.getDeviceSn());

        if (bean.getHardData() != null) {
            dilOwner.setVisibility(View.VISIBLE);
            dilAddTime.setVisibility(View.VISIBLE);
            dilOwner.setText(bean.getOwner());
            dilAddTime.setText(bean.getDatetime());
        }


        if (bean.getHardData() == null && bean.getLocalData() != null && bean.isOnline()) {
            btnBind.setVisibility(View.VISIBLE);
        }
        if (bean.getHardData() != null && bean.isOnline() &&
                (bean.getHardData().isOwner() || bean.getHardData().isAdmin())) {
            btnReboot.setVisibility(View.VISIBLE);
            if ((bean.getFeature() & Constants.DF_FUNCTION_REMOTE_MAINT) > 0) {
                remoteSwitch.setVisibility(View.VISIBLE);
            }
            btnLocalData.setVisibility(View.VISIBLE);
            refreshStatus();
        }
        if (bean.getLocalData() != null) {
            String deviceIp = bean.getLocalData().getDeviceIp();
            dilLanIp.setVisibility(TextUtils.isEmpty(deviceIp) ? View.GONE : View.VISIBLE);
            dilLanIp.setText(deviceIp);
            String vip = bean.getLocalData().getVip();
            dilVip.setVisibility(TextUtils.isEmpty(vip) ? View.GONE : View.VISIBLE);
            dilVip.setText(vip);

            dilStatus.setVisibility(View.VISIBLE);
            int status = bean.getLocalData().getStatus();
            int disconnectReason = bean.getLocalData().getDisconnectReason();
            int preAuthStep = bean.getLocalData().getPreAuthStep();
            int preAuthCode = bean.getLocalData().getPreAuthCode();
            int preDisconnectReason = bean.getLocalData().getPreDisconnectReason();
            String str = int2HexString(status) +
                    int2HexString(disconnectReason) +
                    int2HexString(preAuthStep) +
                    int2HexString(preAuthCode) +
                    int2HexString(preDisconnectReason);
            dilStatus.setText(str);
        } else {
            dilStatus.setVisibility(View.GONE);
            dilLanIp.setVisibility(View.GONE);
            dilVip.setVisibility(View.GONE);
        }

        localDevDialog.show();
        sv.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                DisplayMetrics metrics = context.getResources().getDisplayMetrics();
                Window window = localDevDialog.getWindow();
                if (window != null) {
                    WindowManager.LayoutParams params = window.getAttributes();
                    params.width = (int) (metrics.widthPixels * 0.80);
                    window.setAttributes(params);
                }
                sv.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });
        dialogLevel = 1;
    }

    private void initView(View view) {
        sv = view.findViewById(R.id.device_local_sv);
        loadingView = view.findViewById(R.id.layout_loading);

        dilName = view.findViewById(R.id.device_local_dil_name);
        dilOwner = view.findViewById(R.id.device_local_dil_owner);
        dilAddTime = view.findViewById(R.id.device_local_dil_add_time);
        dilVersion = view.findViewById(R.id.device_local_dil_version);
        dilId = view.findViewById(R.id.device_local_dil_id);
        dilSn = view.findViewById(R.id.device_local_dil_sn);
        dilStatus = view.findViewById(R.id.device_local_dil_status);
        dilVip = view.findViewById(R.id.device_local_dil_vip);
        dilLanIp = view.findViewById(R.id.device_local_dil_lanip);

        remoteSwitch = view.findViewById(R.id.switch_local_remote);
        btnUpgrade = view.findViewById(R.id.device_local_upgrade);
        btnLocalData = view.findViewById(R.id.device_local_lldrd);
        btnLogcat = view.findViewById(R.id.device_local_get_logcat);
        btnReboot = view.findViewById(R.id.device_local_reboot);
        btnBind = view.findViewById(R.id.device_local_btn_bind);
        view.findViewById(R.id.device_local_iv_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                localDevDialog.dismiss();
            }
        });


        btnUpgrade.setOnClickListener(listener);
        btnLocalData.setOnClickListener(listener);
        btnLogcat.setOnClickListener(listener);
        btnBind.setOnClickListener(listener);
        btnReboot.setOnClickListener(listener);
//        refreshStatus();

        initLocalRealDataView(view);

        view.setFocusableInTouchMode(true);
        view.requestFocus();
        view.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_BACK) {
                    return onBackPress();
                }
                return false;
            }
        });
        View.OnClickListener listener = v -> {
            switch (v.getId()) {
                case R.id.device_local_dil_name:
                    clipString(dilName.mTvData.getText().toString().trim());
                    break;
                case R.id.device_local_dil_owner:
                    clipString(dilOwner.mTvData.getText().toString().trim());
                    break;
                case R.id.device_local_dil_add_time:
                    clipString(dilAddTime.mTvData.getText().toString().trim());
                    break;
                case R.id.device_local_dil_version:
                    clipString(dilVersion.mTvData.getText().toString().trim());
                    break;
                case R.id.device_local_dil_id:
                    clipString(dilId.mTvData.getText().toString().trim());
                    break;
                case R.id.device_local_dil_sn:
                    clipString(dilSn.mTvData.getText().toString().trim());
                    break;
                case R.id.device_local_dil_status:
                    clipString(dilStatus.mTvData.getText().toString().trim());
                    break;
            }
        };
        dilName.setDataOnClickListener(listener);
        dilOwner.setDataOnClickListener(listener);
        dilAddTime.setDataOnClickListener(listener);
        dilVersion.setDataOnClickListener(listener);
        dilId.setDataOnClickListener(listener);
        dilSn.setDataOnClickListener(listener);
        dilStatus.setDataOnClickListener(listener);
    }

    private void clipString(String content) {
        if (context != null) {
            ClipboardUtils.copyToClipboard(context, content);
            ToastUtils.showToast(context.getString(R.string.Copied) + content);
        }
    }

    private void refreshStatus() {
        addDisposable(Observable.create(new ObservableOnSubscribe<Result<LocalDeviceStatus>>() {
            @Override
            public void subscribe(ObservableEmitter<Result<LocalDeviceStatus>> e) {
                if (!TextUtils.isEmpty(getValidIp())) {
                    LocalDeviceStatus status = CMAPI.getInstance().getLocalDeviceStatus(getValidIp());
                    if (status != null) {
                        e.onNext(new Result<LocalDeviceStatus>(status));
                        return;
                    }
                }
                e.onError(new NullPointerException("can find "));
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Result<LocalDeviceStatus>>() {
                    @Override
                    public void accept(final Result<LocalDeviceStatus> result) {
                        final LocalDeviceStatus status = result.data;
                        if (status != null) {
                            rx = FormatUtils.getSizeSpeedFormat(Long.valueOf(status.getRxSpeed())) +
                                    " (" + FormatUtils.getSizeFormat(Long.valueOf(status.getRxBytes())) + ")";
                            tx = FormatUtils.getSizeSpeedFormat(Long.valueOf(status.getTxSpeed())) +
                                    " (" + FormatUtils.getSizeFormat(Long.valueOf(status.getTxBytes())) + ")";
                            dltRx = FormatUtils.getSizeSpeedFormat(Long.valueOf(status.getDltRxSpeed())) +
                                    " (" + FormatUtils.getSizeFormat(Long.valueOf(status.getDltRxBytes())) + ")";
                            dltTx = FormatUtils.getSizeSpeedFormat(Long.valueOf(status.getDltTxSpeed())) +
                                    " (" + FormatUtils.getSizeFormat(Long.valueOf(status.getDltTxBytes())) + ")";
                            delay = status.getLatency() + " ms";
                            deration = FormatUtils.getUptime(Long.valueOf(status.getDuration()));

                            initLocalRealData();
                            if (remoteSwitch != null) {
                                remoteSwitch.setChecked(status.getRemoteMaint());
                                remoteSwitch.setEnabled(true);
                                remoteSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                                    @Override
                                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                                        if (!TextUtils.isEmpty(getValidIp())) {
                                            if (CMAPI.getInstance().switchRemoteMaint(getValidIp(), isChecked)) {
                                                refreshStatus();
                                                remoteSwitch.setEnabled(false);
                                            } else {
                                                ToastUtils.showToast(R.string.fail);
                                            }
                                        }
                                    }
                                });
                            }
                            if (!TextUtils.isEmpty(status.getNew_version())) {
                                btnUpgrade.setVisibility(View.VISIBLE);
                            }
                        } else {
                            ToastUtils.showToast(R.string.get_real_time_status_info_fail);
                        }
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        throwable.printStackTrace();
                        ToastUtils.showToast(R.string.get_real_time_status_info_fail);
                    }
                }));
    }

    private void initLocalRealDataView(View view) {
        localDataLayout = view.findViewById(R.id.layout_lldrd);
        localDataBack = view.findViewById(R.id.lldrd_iv_back);
        localDataRefresh = view.findViewById(R.id.lldrd_btn_refresh);

        dilUptime = view.findViewById(R.id.lldrd_dil_uptime);
        dilRx = view.findViewById(R.id.lldrd_dil_rx_bytes);
        dilTx = view.findViewById(R.id.lldrd_dil_tx_bytes);
        dilDltRx = view.findViewById(R.id.lldrd_dil_dlt_rx_bytes);
        dilDltTx = view.findViewById(R.id.lldrd_dil_dlt_tx_bytes);
        dildelay = view.findViewById(R.id.lldrd_dil_delay);

        localDataBack.setOnClickListener(listener);
        localDataRefresh.setOnClickListener(listener);

        initLocalRealData();
    }

    private void initLocalRealData() {
        if (dilUptime != null && dilRx != null && dilTx != null &&
                dilDltRx != null && dilDltTx != null && dildelay != null) {
            dilUptime.setText(deration);
            dilRx.setText(rx);
            dilTx.setText(tx);
            dilDltRx.setText(dltRx);
            dilDltTx.setText(dltTx);
            dildelay.setText(delay);
        }
    }

    private void showUpgradeDialog() {
        DialogUtil.showSelectDialog(context, context.getString(R.string.update_device_version),
                context.getString(R.string.yes), new DialogUtil.OnDialogButtonClickListener() {
                    @Override
                    public void onClick(View v, String strEdit, Dialog dialog, boolean isCheck) {
                        dialog.dismiss();
                        int result = CMAPI.getInstance().deviceUpgrade(getValidIp());
                        if (result != -911) {
                            // TODO: 2019/7/26 错误码CE
//                            ToastUtils.showToast(HttpErrorNo.ec2String(result));
                        }
                    }
                }, context.getString(R.string.no), null);
    }

    private String getValidIp() {
        if (bean.getLocalData() != null) {
            if (!TextUtils.isEmpty(bean.getLocalData().getDeviceIp())) {
                return bean.getLocalData().getDeviceIp();
            } else if (!TextUtils.isEmpty(bean.getLocalData().getVip())) {
                return bean.getLocalData().getVip();
            }
        }
        ToastUtils.showToast(R.string.jws_invalid_ip);
        return "";
    }

    private boolean isCollectingLogs;

    private void logOperation() {
        if (!isCollectingLogs) {
            if (bean.getLocalData() != null) {
                getLog(bean.getId(), bean.getLocalData().getDeviceIp());
            } else {
                Timber.d("local data is null");
            }
        } else {
            if (CMAPI.getInstance().stopGetLocalDeviceLog()) {
                isCollectingLogs = false;
                btnLogcat.setText(R.string.collect_log);
            }
        }
    }

    private void getLog(final String deviceId, final String ip) {
        AndPermission.with((FragmentActivity) context)
                .runtime()
                .permission(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE)
                .onGranted(permissions -> {
                    String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/SDVN/SDVN_log/";
                    File dir = new File(path);
                    if (!dir.exists()) {
                        dir.mkdirs();
                    }
                    Calendar cal = Calendar.getInstance();
                    String logName = path + "device_log_" + deviceId + "_" + cal.get(Calendar.YEAR)
                            + "_" + (cal.get(Calendar.MONTH) + 1)
                            + "_" + cal.get(Calendar.DAY_OF_MONTH) + ".txt";
                    if (CMAPI.getInstance().startGetLocalDeviceLog(ip, logName)) {
                        isCollectingLogs = true;
                        btnLogcat.setText(R.string.stop_collecting_logs);
                    } else {
                        ToastUtils.showToast(R.string.fail);
                    }
                })
                .onDenied(permissions -> {
                    btnLogcat.setEnabled(false);
                })
                .start();
    }

    private void izzbieBindDevice() {
        DialogUtil.showEditDialog(context, context.getString(R.string.bind_device),
                "", context.getString(R.string.input_regist_code),
                context.getString(R.string.commit), (v, code, dialog, isCheck) -> {
                    if (TextUtils.isEmpty(code)) {
                        AnimUtils.sharkEditText(context, v);
                    } else {
                        dialog.dismiss();
                        btnBind.setEnabled(false);
                        BindDeviceHttpLoader bindDeviceHttpLoader = new BindDeviceHttpLoader(GsonBaseProtocol.class);
                        bindDeviceHttpLoader.setHttpLoaderStateListener(this);
                        //izzbie绑定设备，输入注册码后拼入sn后面，一并传递
                        bindDeviceHttpLoader.setParams(BindDeviceHttpLoader.Type.TYPE_SCAN,
                                bean.getDeviceSn() + "." + code,
                                null, bean.getLocalData() != null ? bean.getLocalData().getAppId() : null);
                        bindDeviceHttpLoader.executor(new ResultListener() {
                            @Override
                            public void success(Object tag, GsonBaseProtocol mGsonBaseProtocol) {
                                btnBind.setVisibility(View.GONE);
                                DevManager.getInstance().initHardWareList(null);
                            }

                            @Override
                            public void error(Object tag, GsonBaseProtocol mErrorProtocol) {
                                btnBind.setEnabled(true);
                                ToastUtils.showError(mErrorProtocol.result);
                            }
                        });
                    }
                },
                context.getString(R.string.cancel), null);
    }

    private void otherBindDevice(View btnBind, String deviceSn, String appId) {
        btnBind.setEnabled(false);
        BindDeviceHttpLoader bindDeviceHttpLoader = new BindDeviceHttpLoader(GsonBaseProtocol.class);
        bindDeviceHttpLoader.setHttpLoaderStateListener(this);
        bindDeviceHttpLoader.setParams(BindDeviceHttpLoader.Type.TYPE_SCAN, deviceSn,
                null, appId);
        bindDeviceHttpLoader.executor(new ResultListener() {
            @Override
            public void success(Object tag, GsonBaseProtocol mGsonBaseProtocol) {
                btnBind.setVisibility(View.GONE);
                DevManager.getInstance().initHardWareList(null);
            }

            @Override
            public void error(Object tag, GsonBaseProtocol mErrorProtocol) {
                btnBind.setEnabled(true);
                ToastUtils.showError(mErrorProtocol.result);
            }
        });
    }


    @Override
    public void onLoadStart(Disposable disposable) {
        loadingView.setVisibility(View.VISIBLE);
    }

    @Override
    public void onLoadComplete() {
        loadingView.setVisibility(View.GONE);
    }

    @Override
    public void onLoadError() {
        loadingView.setVisibility(View.GONE);
    }

    @NonNull
    private String int2HexString(int i) {
        StringBuilder str = new StringBuilder();
        if (i >= 0 && i <= 0xF) {
            str.append("0").append(Integer.toHexString(i));
        } else if (i >= 0xF && i <= 0xFF) {
            str.append(Integer.toHexString(i));
        } else {
            str.append("00");
        }
        return str.toString().toUpperCase();
    }
}
