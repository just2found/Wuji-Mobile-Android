package net.linkmate.app.base;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.VpnService;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProviders;

import net.linkmate.app.R;
import net.linkmate.app.manager.SDVNManager;
import net.linkmate.app.receiver.DevNetworkBroadcastReceiver;
import net.linkmate.app.ui.activity.LoginActivity;
import net.linkmate.app.ui.activity.MainActivity;
import net.linkmate.app.ui.activity.SplashActivity;
import net.linkmate.app.ui.viewmodel.SdvnStatusViewModel;
import net.linkmate.app.util.MySPUtils;
import net.linkmate.app.util.NetworkUtils;
import net.linkmate.app.util.ToastUtils;
import net.linkmate.app.util.UIUtils;
import net.linkmate.app.view.TipsBar;
import net.linkmate.app.view.notify.LoadingDialog;
import net.sdvn.cmapi.BaseInfo;
import net.sdvn.cmapi.CMAPI;
import net.sdvn.cmapi.global.Constants;
import net.sdvn.cmapi.protocal.ConnectStatusListenerPlus;
import net.sdvn.cmapi.protocal.ResultListener;
import net.sdvn.common.ErrorCode;
import net.sdvn.nascommon.constant.AppConstants;
import net.sdvn.nascommon.utils.DialogUtils;

import org.greenrobot.eventbus.EventBus;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import libs.source.common.utils.AndUtils;
import libs.source.common.utils.ThreadUtils;
import timber.log.Timber;

public abstract class BaseActivity extends AppCompatActivity {
    private LoadingDialog mProgressDialog;
    private boolean isShowEnable;
    private ConnectStatusListenerPlus mConnectStatusListener = new ConnectStatusListenerPlus() {
        @Override
        public void onAuthenticated() {
            if (status != LoadingStatus.DEFUALT || !showTipsBar(R.string.loading, false)) {
                showLoading(R.string.loading_data);
            }
        }

        @Override
        public void onConnected() {
            if (status != LoadingStatus.DEFUALT || !showTipsBar(R.string.loading, false)) {
                showLoading(R.string.loading_data);
            }
        }

        @Override
        public void onConnecting() {
            if (status != LoadingStatus.DEFUALT || !showTipsBar(R.string.loading, false)) {
                showLoading(R.string.connecting);
            }
            BaseActivity.this.onConnecting();
        }

        @Override
        public void onDisconnecting() {
            BaseActivity.this.onConnecting();
            if (status != LoadingStatus.DEFUALT || !showTipsBar(R.string.loading, false)) {
                showLoading(R.string.disconnecting);
            }

        }

        @Override
        public void onEstablished() {
            setStatus(LoadingStatus.DEFUALT);
            hideTipsBar();
            dismissLoading();
            BaseActivity.this.onEstablished();
        }

        @Override
        public void onDisconnected(int reason) {

            LoadingStatus mStatus = LoadingStatus.DEFUALT;
            if (status == LoadingStatus.CHANGE_ACCOUNT) {
                mStatus = LoadingStatus.LOGIN;
            } else if (status == LoadingStatus.LOGIN_AGIN) {
                mStatus = LoadingStatus.LOGIN_AGIN;
            }
            setStatus(mStatus);
            dismissLoading();
            BaseActivity.this.onDisconnected();
            int dr = CMAPI.getInstance().getBaseInfo().getDisconnectReason();
            if (dr == Constants.DR_KO_USER_REMOVED) {
                autoLogin();
            }
        }
    };
    protected String deviceId;
    protected SdvnStatusViewModel mSdvnStatusViewModel;

    protected void onConnecting() {
    }

    protected void onEstablished() {
    }

    protected void onDisconnected() {
    }

    protected void autoLogin() {
        boolean netAvailable = NetworkUtils.checkNetwork(this);
        BaseInfo baseInfo = CMAPI.getInstance().getBaseInfo();
        String account = baseInfo != null ? baseInfo.getAccount() : "";
        boolean AutoLogin = !TextUtils.isEmpty(account) &&
                MySPUtils.getBoolean(MyConstants.IS_LOGINED);
        if (AutoLogin && netAvailable
                && VpnService.prepare(this) == null
                && CMAPI.getInstance().isDisconnected()) {
//            CMAPI.getInstance().setLoginAsHost(AppConfig.host);
            if (mSdvnStatusViewModel != null) {
                mSdvnStatusViewModel.toLogin(account, ""
                        , new ResultListener() {
                            @Override
                            public void onError(int reason) {
                                if (Constants.DR_BY_USER != reason &&
                                        Constants.DR_MISSING_INFO != reason &&
                                        Constants.DR_UNSET != reason) {
                                    ToastUtils.showToast(ErrorCode.dr2String(reason));
                                }
                            }
                        });
            }
        }
    }

    /**
     * 1.第一次登录 可取消  －通过初始化值为true
     * 2.退出登录 不可取消
     * 3.切换虚拟网 不可取消
     * 4.进去nas设备 可取消
     * 5.切换账号 可取消
     * 6.重新登录 如修改密码 （需重新登录）
     * <p>
     * 每次连接成功后，设置默认值defualt
     */
    private LoadingStatus status = LoadingStatus.DEFUALT;

    public enum LoadingStatus {
        DEFUALT,
        LOGIN,//登录
        LOGIN_OUT,//退出登录
        CHANGE_ACCOUNT,//切换账号
        CHANGE_VIRTUAL_NETWORK,//切换虚拟网
        ACCESS_NAS,//访问nas
        LOGIN_AGIN,//重新登录
        SUBSCRIBE_MAIN_EN//订阅主EN
    }

    /**
     * 子类当前ViewModel
     *
     * @return
     */
    protected BaseViewModel getViewModel() {
        return null;
    }

    /**
     * loading dialog 进行了取消操作
     *
     * @param cancelStatus
     */
    private void cancelLoading(LoadingStatus cancelStatus) {
        if (getViewModel() != null) getViewModel().cancelLoading(cancelStatus);
    }

    public void setStatus(LoadingStatus status) {
        this.status = status;
    }


    public LoadingStatus getStatus() {
        return status;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            //处理权限修改时导致的UI异常（或控件空指针、闪退）
            Intent intent = new Intent(this, SplashActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return;
        }
        mSdvnStatusViewModel = ViewModelProviders.of(this).get(SdvnStatusViewModel.class);
        Intent intent = getIntent();
        if (intent != null)
            deviceId = intent.getStringExtra(AppConstants.SP_FIELD_DEVICE_ID);
        if (this instanceof MainActivity || this instanceof net.linkmate.app.ui.simplestyle.MainActivity) {
            if (CMAPI.getInstance().getBaseInfo().getStatus() == Constants.CS_CONNECTED) {
                //已经登录过了
                setStatus(LoadingStatus.DEFUALT);
            } else {
                setStatus(LoadingStatus.LOGIN);
            }
        }
        SDVNManager.getInstance().liveDataConnectionStatus.observe(this, this::onStatusChange);

    }

    protected void initFullScreen() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
    }


    protected void onStatusChange(Integer currentStatus) {
        if (isConnected) {
            isShowEnable = true;
            switch (currentStatus) {
                case Constants.CS_UNKNOWN:
                case Constants.CS_PREPARE:
                    // TODO: 2018/11/26 或需要重新登陆
                    break;
                case Constants.CS_CONNECTED:
                    mConnectStatusListener.onConnected();
                    break;
                case Constants.CS_CONNECTING:
                case Constants.CS_WAIT_RECONNECTING:
                    mConnectStatusListener.onConnecting();
                    break;
                case Constants.CS_DISCONNECTING:
                    mConnectStatusListener.onDisconnecting();
                    break;
                case Constants.CS_ESTABLISHED:
                    mConnectStatusListener.onEstablished();
                    break;
                case Constants.CS_AUTHENTICATED:
                    mConnectStatusListener.onAuthenticated();
                    break;
                case Constants.CS_DISCONNECTED:
                    mConnectStatusListener.onDisconnected(0);
                    break;
                default:
                    break;
            }
            Timber.tag("{SDVN}").i("currentActivity:" + getClass().getSimpleName() + "  currentStatus:" + currentStatus);
        }
    }

    private void initNoStatusBar() {
        View topView = getTopView();
        // style的windowTranslucentNavigation设置为false后，状态栏无法达到沉浸效果
        // 设置UI FLAG 让布局能占据状态栏的空间，达到沉浸效果
        int option = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        if (AndUtils.isLightColor(titleColor())) {
//            if (net.linkmate.app.BuildConfig.isLightColor) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                option |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            }
        } else {
            option |= View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
        }
        getWindow().getDecorView().setSystemUiVisibility(option);
        if (topView != null) {
            topView.setPadding(topView.getPaddingLeft(), UIUtils.getStatueBarHeight(this),
                    topView.getPaddingRight(), topView.getPaddingBottom());
        }
    }

    protected boolean isLightColor() {
        return AndUtils.isLightColor(titleColor());
    }


    @ColorInt
    protected int titleColor() {
        return getResources().getColor(R.color.bg_title_start_color);
    }

    protected void refreshStatusBar() {
        View topView = getTopView();
        if (topView != null) {
            final int[] option = {getWindow().getDecorView().getSystemUiVisibility()};
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            AndUtils.calcStatusBarViewPrimaryColor(topView, titleColor(), data -> {
                if (AndUtils.isLightColor(data)) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        option[0] |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
                    }
                } else {
                    option[0] |= View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
                }
                getWindow().getDecorView().setSystemUiVisibility(option[0]);
            });

        }
    }

    /**
     * 实现了沉浸式状态栏的子类界面可使用此方法预留顶部空间
     *
     * @return 子类重写并返回需要预留空间的控件
     */
    protected View getTopView() {
        return null;
    }

    private boolean isConnected = true;
    private boolean isNetworkConnected = true;

    protected void setConnectionState(boolean _isConnected) {
        isConnected = _isConnected;
    }

    private DevNetworkBroadcastReceiver.DevNetworkChangedObserver mDevNetworkChangedObserver = new DevNetworkBroadcastReceiver.DevNetworkChangedObserver() {
        @Override
        public void update(BroadcastReceiver receiver, Object arg) {
            isNetworkConnected = (boolean) arg;
            if (!isNetworkConnected && getTipsBar() != null && getTipsBar().getVisibility() == View.VISIBLE) {
                showTipsBar(R.string.error_string_no_network, true);
            }
        }
    };

    @Override
    protected void onStart() {
        super.onStart();
        isNetworkConnected = NetworkUtils.checkNetwork(this);
        initNoStatusBar();
        DevNetworkBroadcastReceiver.getInstance().registerObserver(mDevNetworkChangedObserver);
        if (isConnected) {
            isShowEnable = true;
        }
        onStatusChange(SDVNManager.getInstance().liveDataConnectionStatus.getValue());
//            int currentStatus = CMAPI.getInstance().getRealtimeInfo().getCurrentStatus();
//            switch (currentStatus) {
//                case Constants.CS_UNKNOWN:
//                case Constants.CS_PREPARE:
//                    // TODO: 2018/11/26 或需要重新登陆
//                    break;
//                case Constants.CS_CONNECTED:
//                    mConnectStatusListener.onConnected();
//                    break;
//                case Constants.CS_CONNECTING:
//                case Constants.CS_WAIT_RECONNECTING:
//                    mConnectStatusListener.onConnecting();
//                    break;
//                case Constants.CS_DISCONNECTING:
//                    mConnectStatusListener.onDisconnecting();
//                    break;
//                case Constants.CS_ESTABLISHED:
//                    mConnectStatusListener.onEstablished();
//                    break;
//                case Constants.CS_AUTHENTICATED:
//                    mConnectStatusListener.onAuthenticated();
//                    break;
//                case Constants.CS_DISCONNECTED:
//                    mConnectStatusListener.onDisconnected(0);
//                    break;
//                default:
//                    break;
//            }
//            Timber.tag("{SDVN}").i("currentActivity:" + getClass().getSimpleName() + "  currentStatus:" + currentStatus);
//            CMAPI.getInstance().addConnectionStatusListener(mConnectStatusListener);
//        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        DevNetworkBroadcastReceiver.getInstance().unregisterObserver(mDevNetworkChangedObserver);
        isShowEnable = false;
//        CMAPI.getInstance().removeConnectionStatusListener(mConnectStatusListener);
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshStatusBar();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        if (mProgressDialog != null && mProgressDialog.isShowing())
            mProgressDialog.dismiss();
        dispose();
        super.onDestroy();
    }

    public void disconnect() {
        showLoading(R.string.disconnecting);
        CMAPI.getInstance().disconnect();
    }

    protected void backToLogin() {
//        dismissLoading();
//        MySPUtils.saveBoolean(this, MyConstants.STATUS_LOGINED, false);
//        stopService(new Intent(this, FileRecvService.class));
//        startActivity(new Intent(this, LoginActivity.class));
//        overridePendingTransition(R.anim.slide_left_in, R.anim.slide_right_out);
//        this.finish();
    }

    //是否初始化过，仅一次，设置监听器
    private boolean isInitTipsBar = false;
    //返回登录提示框
    private Dialog mExitDialog = null;

    private boolean checkTipsBar() {
        TipsBar tipsBar = getTipsBar();
        boolean result = tipsBar != null;
        if (result && !isInitTipsBar) {
            isInitTipsBar = true;
            tipsBar.setCloseClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    tipsBar.setVisibility(View.GONE);
                }
            });
            tipsBar.setLinkClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (CMAPI.getInstance().isDisconnected()) {
                        //退出登录
                        startActivity(new Intent(BaseActivity.this, LoginActivity.class));
                    } else {
                        mExitDialog = DialogUtils.showConfirmDialog(BaseActivity.this, 0, R.string.stop_connect, R.string.confirm,
                                R.string.cancel, new DialogUtils.OnDialogClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, @NonNull boolean isPositiveBtn) {
                                        if (isPositiveBtn) {
                                            //未连接前可取消
                                            if (!isNetworkConnected || !CMAPI.getInstance().isConnected()) {
                                                CMAPI.getInstance().cancelLogin();
                                                //退出登录
                                                startActivity(new Intent(BaseActivity.this, LoginActivity.class));
                                            }
                                        }
                                    }
                                });
                    }
                }
            });
        }
        return result;
    }

    /**
     * 显示标题提示
     * 优先显示网络连接失败
     */
    private boolean showTipsBar(String title, boolean cancelable) {
        boolean isExitTipsBar = checkTipsBar();
        if (isExitTipsBar) {
            getTipsBar().setWarning(isNetworkConnected ? title : getString(R.string.network_not_available));
        }
        return isExitTipsBar;
    }

    /**
     * 显示标题提示
     * 优先显示网络连接失败
     */
    private boolean showTipsBar(int title, boolean cancelable) {
        boolean isExitTipsBar = checkTipsBar();
        if (isExitTipsBar) {
            getTipsBar().setWarning(isNetworkConnected ? title : R.string.network_not_available);
        }
        return isExitTipsBar;
    }

    /**
     * 隐藏标题提示
     */
    private boolean hideTipsBar() {
        boolean isExitTipsBar = checkTipsBar();
        if (isExitTipsBar) {
            getTipsBar().setVisibility(View.GONE);
        }
        if (mExitDialog != null && mExitDialog.isShowing()) mExitDialog.dismiss();
        return isExitTipsBar;
    }

    public void dismissLoading() {
        loading(false, "", false);
    }

    public void showLoading() {
        showLoading(R.string.loading);
    }

    public void showLoading(String text) {
        showLoading(text, false);
    }

    public void showLoading(String text, boolean cancelable) {
        loading(true, text, cancelable);
    }

    public void showLoading(@StringRes int resId) {
        showLoading(resId, false);
    }

    public void showLoading(@StringRes int resId, boolean cancelable) {
        showLoading(getString(resId), cancelable);
    }

    private void loading(boolean isShowing, String text, boolean cancelableFinal) {
        ThreadUtils.ensureRunOnMainThread(() -> {
            boolean cancelable = cancelableFinal;
            if (isShowing && isShowEnable) {
                if (mProgressDialog == null)
                    mProgressDialog = new LoadingDialog(this, cancelable, text);
                switch (status) {
                    case SUBSCRIBE_MAIN_EN:
                    case LOGIN:
                    case ACCESS_NAS:
                        cancelable = true;
                        break;
                    case CHANGE_ACCOUNT:
                    case LOGIN_OUT:
                    case CHANGE_VIRTUAL_NETWORK:
                        cancelable = false;
                        break;
                    default:
                        break;

                }
                mProgressDialog.setCancelVisibility(cancelable);
                if (cancelable) {
                    mProgressDialog.setOnCancelClickListener(v -> {
                        Timber.d("onLoadingCancel");
                        switch (status) {
                            case LOGIN:
                                DialogUtils.showConfirmDialog(this, 0, R.string.stop_connect, R.string.confirm,
                                        R.string.cancel, new DialogUtils.OnDialogClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, @NonNull boolean isPositiveBtn) {
                                                if (isPositiveBtn) {
                                                    cancelLoading(status);
                                                    CMAPI.getInstance().cancelLogin();
                                                    setStatus(LoadingStatus.DEFUALT);
                                                    if (!isNetworkConnected)
                                                        showTipsBar(R.string.error_string_no_network, true);
                                                    dismissLoading();
                                                }
                                            }
                                        });
                                break;
                            case ACCESS_NAS:
                                cancelLoading(status);
                                EventBus.getDefault().post(new StopLoginNasDevice());
                                setStatus(LoadingStatus.DEFUALT);
                                dismissLoading();
                                break;
                            case SUBSCRIBE_MAIN_EN:
                                cancelLoading(status);
                                setStatus(LoadingStatus.DEFUALT);
                                dismissLoading();
                                break;
                            default:
                                cancelLoading(status);
                                setStatus(LoadingStatus.DEFUALT);
                                dismissLoading();
                                break;

                        }
                    });
                }
                mProgressDialog.setTipMessage(text);
                mProgressDialog.show();
            } else {
                if (mProgressDialog != null && mProgressDialog.isShowing())
                    mProgressDialog.dismiss();
            }
        });
    }

    private CompositeDisposable compositeDisposable;

    public void addDisposable(@NonNull Disposable disposable) {
        if (compositeDisposable == null) {
            compositeDisposable = new CompositeDisposable();
        }
        compositeDisposable.add(disposable);
    }

    protected void dispose() {
        if (compositeDisposable != null) compositeDisposable.dispose();
    }

    @Nullable
    protected abstract TipsBar getTipsBar();

    public class StopLoginNasDevice {
    }
}
