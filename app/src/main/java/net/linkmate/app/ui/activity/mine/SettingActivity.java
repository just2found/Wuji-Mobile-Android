package net.linkmate.app.ui.activity.mine;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.arch.core.util.Function;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleEventObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.chad.library.adapter.base.BaseQuickAdapter;

import net.linkmate.app.BuildConfig;
import net.linkmate.app.R;
import net.linkmate.app.base.BaseActivity;
import net.linkmate.app.base.BaseViewModel;
import net.linkmate.app.base.MyApplication;
import net.linkmate.app.base.MyConstants;
import net.linkmate.app.manager.DevManager;
import net.linkmate.app.manager.PrivilegeManager;
import net.linkmate.app.net.RetrofitSingleton;
import net.linkmate.app.service.DynamicQueue;
import net.linkmate.app.ui.activity.LoginActivity;
import net.linkmate.app.ui.activity.ThemeActivity;
import net.linkmate.app.util.DialogUtil;
import net.linkmate.app.util.Dp2PxUtils;
import net.linkmate.app.util.NetworkUtils;
import net.linkmate.app.util.ToastUtils;
import net.linkmate.app.util.WindowUtil;
import net.linkmate.app.view.ActivityItemLayout;
import net.linkmate.app.view.TipsBar;
import net.linkmate.app.view.adapter.AccountPopRVAdapter;
import net.sdvn.cmapi.CMAPI;
import net.sdvn.cmapi.global.Constants;
import net.sdvn.cmapi.protocal.ResultListener;
import net.sdvn.common.ErrorCode;
import net.sdvn.nascommon.SessionManager;
import net.sdvn.nascommon.constant.AppConstants;
import net.sdvn.nascommon.db.SPHelper;
import net.sdvn.nascommon.iface.Callback;
import net.sdvn.nascommon.utils.DialogUtils;
import net.sdvn.nascommon.utils.FileUtils;
import net.sdvn.nascommon.utils.SPUtils;
import net.sdvn.nascommon.utils.ToastHelper;
import net.sdvn.nascommon.utils.Utils;

import java.util.Objects;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import io.weline.repo.SessionCache;

public class SettingActivity extends BaseActivity {
    private ImageView ivLeft;
    private TextView tvTitle;
    private RelativeLayout rlTitle;
    private Switch switch1;
    //    @BindView(R.id.switch2)
//    Switch switch2;
    private
    Switch switch3;
    private ActivityItemLayout ailSafeOption;
    private ActivityItemLayout ail_clear_cache;
    private ActivityItemLayout ail_about;
//    @BindView(R.id.setting_switch_dlt)
//    Switch switchDlt;
//    @BindView(R.id.setting_switch_leakproof)
//    Switch switchLeakproof;
//    @BindView(R.id.tv_settings_inet_access_settings)
//    View toVpnSettings;

    //    private boolean mHideNet;
    private boolean mShowRemark;
    private boolean spShowRemark;
    //    private List<String> optionList;
//    private int mCurrOption;
    private boolean isBackToLogin;
    private boolean isSwitch;
    private boolean switchLogin;
    private PopupWindow popWin;
    private String switchAccount;
    private View mSettingAilAdvancedSettings;
    private View mSettingAilAbout;
    private View mAccountAilDelete;
    private View mAccountAilSwitch;
    private View mSettingTheme;
    private View mSettingAilClearCache;
    private View mSettingAilDownloadPath;
    private BaseViewModel viewModel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        viewModel = new ViewModelProvider(this).get(BaseViewModel.class);
        bindView(this.getWindow().getDecorView());
        tvTitle.setTextColor(getResources().getColor(R.color.title_text_color));
        tvTitle.setText(R.string.title_settings);
        ivLeft.setVisibility(View.VISIBLE);
        ivLeft.setImageResource(R.drawable.icon_return);

        ivLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        switch1.setChecked(SPHelper.get(AppConstants.SP_FIELD_ONLY_WIFI_CARE, true));
        switch1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SPHelper.put(AppConstants.SP_FIELD_ONLY_WIFI_CARE, isChecked);
            }
        });

//        mHideNet = spHideNet;
//        switch2.setChecked(spHideNet);
//        switch2.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//                mHideNet = isChecked;
//                SPUtils.setBoolean(MyConstants.SP_HIDENET, isChecked);
//            }
//        });

        spShowRemark = SPUtils.getBoolean(MyConstants.SP_SHOW_REMARK_NAME, true);
        mShowRemark = spShowRemark;
        switch3.setChecked(spShowRemark);
        switch3.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mShowRemark = isChecked;
                SPUtils.setBoolean(MyConstants.SP_SHOW_REMARK_NAME, isChecked);
            }
        });

        ail_about.setTips(BuildConfig.VERSION_NAME);

        initView();
        initEvent();
        viewModel.getCancelLoading().observe(this, new Observer<LoadingStatus>() {
            @Override
            public void onChanged(LoadingStatus loadingStatus) {
                if (loadingStatus == LoadingStatus.LOGIN) {//切换账号操作后取消登录，关闭页面
                    finish();
                }
            }
        });
    }

    @Override
    public BaseViewModel getViewModel() {
        return viewModel;
    }

    @Override
    protected View getTopView() {
        return rlTitle;
    }

    @Nullable
    @Override
    protected TipsBar getTipsBar() {
        return findViewById(R.id.tipsBar);
    }

    private void initView() {
    }

    private void initEvent() {

    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshCache();
    }

    private void refreshCache() {
        final Disposable subscribe = getCacheSize().subscribe(new Consumer<Long>() {
            @Override
            public void accept(Long aLong) throws Exception {
                if (aLong < 0) aLong = 0L;
                final String fileSize = FileUtils.fmtFileSize(aLong);
                ail_clear_cache.setTips(fileSize);
            }
        });
        getLifecycle().addObserver(new LifecycleEventObserver() {
            @Override
            public void onStateChanged(@NonNull LifecycleOwner source, @NonNull Lifecycle.Event event) {
                if (Lifecycle.Event.ON_DESTROY == event)
                    subscribe.dispose();
            }
        });
    }


    @Override
    public void onEstablished() {
        if (isSwitch) {
            if (Objects.equals(switchAccount, CMAPI.getInstance().getBaseInfo().getAccount())) {
                ToastHelper.showLongToastSafe(R.string.switch_success);
                finish();
            } else {
                ToastHelper.showLongToastSafe(R.string.operate_failed);
            }
        }
        isSwitch = false;
        switchLogin = false;
    }

    @Override
    public void onDisconnected() {
        if (isSwitch) {
            PrivilegeManager.getInstance().setPrompted(false);
            if (switchLogin) {
                return;
            }
            switchLogin = true;
            mSdvnStatusViewModel.toLogin(switchAccount, ""
                    , new ResultListener() {
                        @Override
                        public void onError(int reason) {
                            if (Constants.DR_BY_USER != reason &&
                                    Constants.DR_MISSING_INFO != reason &&
                                    Constants.DR_UNSET != reason) {
                                ToastUtils.showToast(ErrorCode.dr2String(reason));
                            }
                            if (reason == Constants.DR_INVALID_USER ||
                                    reason == Constants.DR_AUX_AUTH_DISMATCH ||
                                    reason == Constants.DR_DEVICE_DELETED ||
                                    reason == Constants.DR_DEVICE_ONLINE ||
                                    reason == Constants.DR_DEVICE_DISABLED ||
                                    reason == Constants.DR_MAX_DEVICE ||
                                    reason == Constants.DR_KO_USER_REMOVED ||
                                    reason == Constants.DR_KO_DEVICE_REMOVED ||
                                    reason == Constants.DR_KO_DEVICE_DELETED ||
                                    reason == Constants.DR_INVALID_AUTHORIZATION ||
                                    reason == Constants.DR_INVALID_SMS ||
                                    reason == Constants.DR_INVALID_TICKET ||
                                    reason == Constants.DR_TRY_TOO_MANY_TIMES ||
                                    reason == Constants.DR_INVALID_PASS) {
                                CMAPI.getInstance().cancelLogin();
                                Intent intent = new Intent(SettingActivity.this, LoginActivity.class);
                                intent.putExtra("dr", reason);
                                intent.putExtra(AppConstants.SP_FIELD_USERNAME, switchAccount);
                                startActivity(intent);
                                overridePendingTransition(R.anim.slide_left_in, R.anim.slide_right_out);
                                finish();
                            }
                        }
                    });
        } else if (isBackToLogin) {
            PrivilegeManager.getInstance().setPrompted(false);
            startActivity(new Intent(this, LoginActivity.class));
            overridePendingTransition(R.anim.slide_left_in, R.anim.slide_right_out);
            this.finish();
        } else {
            autoLogin();
        }
    }

    @Override
    public void onBackPressed() {
//        if (mHideNet != spHideNet) {
//            EventBus.getDefault().post(MyConstants.NET_SETTING_CHANGE);
//        }
        if (mShowRemark != spShowRemark) {
            DevManager.getInstance().initHardWareList(null);// ???  显示备注名 还是 设备名
        }
        super.onBackPressed();
    }

    private void onViewClicked(View view) {
        if (Utils.isFastClick(view)) return;
        switch (view.getId()) {
            case R.id.setting_ail_advanced_settings:
                startActivity(new Intent(this, AdvancedSettingsActivity.class));
                break;
            case R.id.setting_ail_about:
                startActivity(new Intent(this, AboutActivity.class));
                break;
            case R.id.account_ail_delete://退出登录
                logout();
                break;
            case R.id.account_ail_switch://切换账号
                showSwitchPop();
                break;
            case R.id.setting_theme://首页风格
                startActivity(new Intent(this, ThemeActivity.class));
                break;
        }
    }

    private void clearCache(View view) {
        DialogUtils.showConfirmDialog(view.getContext(), DialogUtils.RESOURCE_ID_NONE,
                R.string.confirm_clear_cache, R.string.confirm, R.string.cancel,
                new DialogUtils.OnDialogClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, boolean isPositiveBtn) {
                        if (isPositiveBtn) {
                            FileUtils.clearCache(MyApplication.getInstance(), new Callback<Boolean>() {
                                @Override
                                public void result(Boolean o) {
                                    if (!isDestroyed()) {
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                refreshCache();
                                            }
                                        });
                                    }
                                }
                            });
                            dialog.dismiss();
                        }
                    }
                });
    }

    public Observable<Long> getCacheSize() {
        return Observable.just(FileUtils.getCacheSize(this))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }


    private void setDownloadPath(View view) {
        if (Utils.isNotFastClick(view))
            startActivity(new Intent(this, SetDownloadPathActivity.class));
    }


    public void logout() {
        DialogUtil.showExtraSelectDialog(this, getString(R.string.tips_log_out_and_delete),
                getString(R.string.just_logout), new DialogUtil.OnDialogButtonClickListener() {
                    @Override
                    public void onClick(View v, String strEdit, Dialog dialog, boolean isCheck) {
                        DynamicQueue.INSTANCE.checkExistPublishingDynamic(SettingActivity.this, new Function<Boolean, Void>() {
                            @Override
                            public Void apply(Boolean input) {
                                if (input) {
                                    if (CMAPI.getInstance().getBaseInfo().getStatus() == Constants.CS_ESTABLISHED
                                            || CMAPI.getInstance().getBaseInfo().getStatus() == Constants.CS_CONNECTED) {
                                        setStatus(LoadingStatus.LOGIN_OUT);
                                        isBackToLogin = true;
                                        CMAPI.getInstance().disconnect();
                                        SessionManager.getInstance().logoutCurrentAccount();
                                        dialog.dismiss();
                                    } else {
                                        CMAPI.getInstance().cancelLogin();
                                        dialog.dismiss();
                                        //退出登录
                                        startActivity(new Intent(SettingActivity.this, LoginActivity.class));
                                    }
                                }
                                return null;
                            }
                        });

                    }
                },
                getString(R.string.cancel), null,
                getString(R.string.logout_and_delete), new DialogUtil.OnDialogButtonClickListener() {
                    @Override
                    public void onClick(View v, String strEdit, Dialog dialog, boolean isCheck) {
                        DynamicQueue.INSTANCE.checkExistPublishingDynamic(SettingActivity.this, new Function<Boolean, Void>() {
                            @Override
                            public Void apply(Boolean input) {
                                if (input) {
                                    if (CMAPI.getInstance().getBaseInfo().getStatus() == Constants.CS_ESTABLISHED
                                            || CMAPI.getInstance().getBaseInfo().getStatus() == Constants.CS_CONNECTED) {
                                        setStatus(LoadingStatus.LOGIN_OUT);
                                        isBackToLogin = true;
                                        CMAPI.getInstance().removeUser(CMAPI.getInstance().getBaseInfo().getAccount());
                                        CMAPI.getInstance().disconnect();
                                        SessionManager.getInstance().removeAccount();
                                        dialog.dismiss();
                                    } else {
                                        //退出登录
                                        CMAPI.getInstance().cancelLogin();
                                        CMAPI.getInstance().removeUser(CMAPI.getInstance().getBaseInfo().getAccount());
                                        dialog.dismiss();
                                        startActivity(new Intent(SettingActivity.this, LoginActivity.class));
                                    }
                                }
                                return null;
                            }
                        });

                    }
                });
    }

    private void showSwitchPop() {
        View contentView = LayoutInflater.from(this).inflate(R.layout.layout_switch_account, null, false);
        popWin = new PopupWindow(contentView, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, true);
        popWin.setOutsideTouchable(true);
        popWin.setTouchable(true);

        View.OnClickListener dismissListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                popWin.dismiss();
            }
        };
        View out = contentView.findViewById(R.id.pop_account_view_out);
        out.setOnClickListener(dismissListener);
        View cancel = contentView.findViewById(R.id.pop_account_tv_cancel);
        cancel.setOnClickListener(dismissListener);

        RecyclerView rv = contentView.findViewById(R.id.pop_account_rv);
        AccountPopRVAdapter adapter = new AccountPopRVAdapter();
        adapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter baseQuickAdapter, View view, int i) {
                if (NetworkUtils.checkNetwork(SettingActivity.this)) {
                    switchAccount = (String) baseQuickAdapter.getData().get(i);
                    switchAccount();
                } else {//无网络
                    ToastUtils.showToast(R.string.error_string_no_network);
                }
            }
        });
        LinearLayoutManager layout = new LinearLayoutManager(this, RecyclerView.VERTICAL, false);
        rv.setLayoutManager(layout);
        rv.setItemAnimator(null);

        //尾部留白
        View footerView = new View(this);
        adapter.addFooterView(footerView);
        ViewGroup.LayoutParams layoutParams = footerView.getLayoutParams();
        layoutParams.height = Dp2PxUtils.dp2px(this, 24);
        footerView.setLayoutParams(layoutParams);

        rv.setAdapter(adapter);
        adapter.setNewData(CMAPI.getInstance().getBaseInfo().getUserList());

        popWin.setAnimationStyle(R.style.BottomPopupWindow);
        popWin.showAtLocation(getWindow().getDecorView(), Gravity.BOTTOM, 0, 0);
        popWin.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                WindowUtil.hintShadow(SettingActivity.this);
            }
        });
        WindowUtil.showShadow(SettingActivity.this);
    }

    private void switchAccount() {
        if (!Objects.equals(switchAccount, CMAPI.getInstance().getBaseInfo().getAccount())) {
            DynamicQueue.INSTANCE.checkExistPublishingDynamic(this, new Function<Boolean, Void>() {
                @Override
                public Void apply(Boolean input) {
                    if (input) {
                        DialogUtil.showSelectDialog(SettingActivity.this,
                                String.format(getString(R.string.tips_switch_account), switchAccount),
                                getString(R.string.confirm), new DialogUtil.OnDialogButtonClickListener() {
                                    @Override
                                    public void onClick(View v, String strEdit, Dialog dialog, boolean isCheck) {
                                        SessionCache.Companion.getInstance().clear();
                                        RetrofitSingleton.Companion.getInstance().clear();
                                        setStatus(LoadingStatus.CHANGE_ACCOUNT);
                                        isSwitch = true;
                                        CMAPI.getInstance().disconnect();
                                        SessionManager.getInstance().logoutCurrentAccount();
                                        dialog.dismiss();
                                        if (popWin != null) {
                                            popWin.dismiss();
                                        }
                                    }
                                },
                                getString(R.string.cancel), null);
                    }
                    return null;
                }
            });
        }
    }

    private void bindView(View bindSource) {
        ivLeft = bindSource.findViewById(R.id.itb_iv_left);
        tvTitle = bindSource.findViewById(R.id.itb_tv_title);
        rlTitle = bindSource.findViewById(R.id.itb_rl);
        switch1 = bindSource.findViewById(R.id.switch1);
        switch3 = bindSource.findViewById(R.id.switch3);
        ailSafeOption = bindSource.findViewById(R.id.setting_ail_advanced_settings);
        ail_clear_cache = bindSource.findViewById(R.id.setting_ail_clear_cache);
        ail_about = bindSource.findViewById(R.id.setting_ail_about);
        mSettingAilAdvancedSettings = bindSource.findViewById(R.id.setting_ail_advanced_settings);
        mSettingAilAbout = bindSource.findViewById(R.id.setting_ail_about);
        mAccountAilDelete = bindSource.findViewById(R.id.account_ail_delete);
        mAccountAilSwitch = bindSource.findViewById(R.id.account_ail_switch);
        mSettingTheme = bindSource.findViewById(R.id.setting_theme);
        mSettingAilClearCache = bindSource.findViewById(R.id.setting_ail_clear_cache);
        mSettingAilDownloadPath = bindSource.findViewById(R.id.setting_ail_download_path);
        mSettingAilAdvancedSettings.setOnClickListener(v -> {
            onViewClicked(v);
        });
        mSettingAilAbout.setOnClickListener(v -> {
            onViewClicked(v);
        });
        mAccountAilDelete.setOnClickListener(v -> {
            onViewClicked(v);
        });
        mAccountAilSwitch.setOnClickListener(v -> {
            onViewClicked(v);
        });
        mSettingTheme.setOnClickListener(v -> {
            onViewClicked(v);
        });
        mSettingAilClearCache.setOnClickListener(v -> {
            clearCache(v);
        });
        mSettingAilDownloadPath.setOnClickListener(v -> {
            setDownloadPath(v);
        });
    }
}
