//package net.linkmate.app.util.business;
//
//import android.app.Dialog;
//import android.content.Context;
//import android.content.DialogInterface;
//import android.graphics.Bitmap;
//import android.text.TextUtils;
//import android.util.DisplayMetrics;
//import android.view.KeyEvent;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewTreeObserver;
//import android.view.Window;
//import android.view.WindowManager;
//import android.widget.Button;
//import android.widget.CompoundButton;
//import android.widget.EditText;
//import android.widget.ImageView;
//import android.widget.LinearLayout;
//import android.widget.ProgressBar;
//import android.widget.Switch;
//import android.widget.TextView;
//
//import androidx.annotation.NonNull;
//import androidx.appcompat.app.AlertDialog;
//import androidx.appcompat.app.AppCompatDialog;
//import androidx.lifecycle.Observer;
//import androidx.recyclerview.widget.LinearLayoutManager;
//import androidx.recyclerview.widget.RecyclerView;
//
//import com.chad.library.adapter.base.BaseQuickAdapter;
//
//import net.linkmate.app.R;
//import net.linkmate.app.base.MyConstants;
//import net.linkmate.app.base.MyOkHttpListener;
//import net.linkmate.app.bean.DeviceBean;
//import net.linkmate.app.manager.DevManager;
//import net.linkmate.app.manager.MessageManager;
//import net.linkmate.app.manager.NetManager;
//import net.linkmate.app.ui.activity.WebViewActivity;
//import net.linkmate.app.util.DialogUtil;
//import net.linkmate.app.util.ToastUtils;
//import net.linkmate.app.view.DataItemLayout;
//import net.linkmate.app.view.SubnetLayout;
//import net.linkmate.app.view.adapter.HomeNetRVAdapter;
//import net.linkmate.app.view.adapter.SubnetRVAdapter;
//import net.sdvn.cmapi.CMAPI;
//import net.sdvn.cmapi.Device;
//import net.sdvn.cmapi.Network;
//import net.sdvn.cmapi.util.ClipboardUtils;
//import net.sdvn.common.internet.core.GsonBaseProtocol;
//import net.sdvn.common.internet.core.HttpLoader;
//import net.sdvn.common.internet.listener.ResultListener;
//import net.sdvn.common.internet.loader.SetDeviceNameHttpLoader;
//import net.sdvn.common.internet.protocol.ShareCode;
//import net.sdvn.common.internet.protocol.SubnetList;
//import net.sdvn.common.internet.protocol.entity.SubnetEntity;
//import net.sdvn.nascommon.SessionManager;
//import net.sdvn.nascommon.constant.OneOSAPIs;
//import net.sdvn.nascommon.iface.GetSessionListener;
//import net.sdvn.nascommon.model.DeviceModel;
//import net.sdvn.nascommon.model.oneos.OneOSHardDisk;
//import net.sdvn.nascommon.model.oneos.UpdateInfo;
//import net.sdvn.nascommon.model.oneos.api.sys.OneOSSpaceAPI;
//import net.sdvn.nascommon.model.oneos.event.UpgradeProgress;
//import net.sdvn.nascommon.model.oneos.user.LoginSession;
//import libs.source.common.livedata.Resource;
//import libs.source.common.livedata.Status;
//import net.sdvn.nascommon.utils.AnimUtils;
//import net.sdvn.nascommon.utils.FileUtils;
//import net.sdvn.nascommon.utils.ToastHelper;
//import net.sdvn.nascommon.viewmodel.DeviceViewModel;
//import net.sdvn.nascommon.viewmodel.M3UpdateViewModel;
//
//import org.greenrobot.eventbus.EventBus;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Objects;
//
//import io.reactivex.disposables.CompositeDisposable;
//import io.reactivex.disposables.Disposable;
//import io.reactivex.functions.Consumer;
//
//public class DeviceDetailDialogUtil implements HttpLoader.HttpLoaderStateListener {
//
//    private final Context context;
//    private final DeviceBean bean;
//    private final int position;
//    private CompositeDisposable compositeDisposable;
//    private M3UpdateViewModel mM3UpdateViewModel;
//    private View mView;
//
//    public void addDisposable(@NonNull Disposable disposable) {
//        if (compositeDisposable == null) {
//            compositeDisposable = new CompositeDisposable();
//        }
//        compositeDisposable.add(disposable);
//    }
//
//    protected void dispose() {
//        if (compositeDisposable != null) compositeDisposable.dispose();
//    }
//
//    public DeviceDetailDialogUtil(Context context, DeviceBean bean, int position) {
//        this.context = context;
//        this.bean = bean;
//        this.position = position;
//    }
//
//    private Dialog detailDialog;
//    private View sv;
//    private View progressLayout;
//    private Switch switchSnEnable;
//    private View share;
//    private View accessView;
//    private View m3UpgradeBtn;
//    private View snSetting;
//    private View subnet;
//    private View loadingView;
//
//    private DataItemLayout dilName;
//    private DataItemLayout dilMarkName;
//    private DataItemLayout dilVip;
//    private DataItemLayout dilLip;
//    private DataItemLayout dilOwner;
//    private DataItemLayout dilVersion;
//    private DataItemLayout dilDomain;
//    private DataItemLayout dilLocation;
//    //M3 update
//    private View mLayoutUpgradeProgress;
//    private ProgressBar mProgressBarDownload;
//    private ProgressBar mProgressBarInstall;
//
//    //share view
//    private View shareLayout;
//    private View shareBack;
//    private Switch switchShare;
//    private ImageView imgShareQR;
//    private TextView tvShareCode;
//    private TextView tvShareTips;
//    private View shareImageContainer;
//    private Switch switchShareNeedAuth;
//    private Button shareBtn;
//
//    //sn view
//    private View snLayout;
//    private View snBack;
//    private View snSubmit;
//    private Switch internetSwitch;
//    private Switch dnsSwitch;
//    private LinearLayout dnsLayout;
//    private EditText dnsEdit1;
//    private EditText dnsEdit2;
//    private Switch subnetSwitch;
//    private LinearLayout subnetLayout;
//    private View addSubnet;
//
//    //subnet view
//    private View subLayout;
//
//    //networks 所处网络
//    private View locationNetworks;
//
//    //sn开关
//    private boolean snInternetEnable;
//    private boolean snDnsEnable;
//    private boolean snSubnetEnable;
//
//    private int dialogLevel;
//
//    private boolean onBackPress() {
//        return onBackPress(sv, shareLayout, snLayout, subLayout);
//    }
//
//    private boolean onBackPress(View visibleView, View... goneViews) {
//        if (dialogLevel <= 0 || detailDialog == null || !detailDialog.isShowing()) {
//            dialogLevel = 0;
//            return false;
//        } else {
//            if (dialogLevel == 2) {
//                for (View view : goneViews) {
//                    if (view != null)
//                        view.setVisibility(View.GONE);
//                }
//                if (visibleView != null)
//                    visibleView.setVisibility(View.VISIBLE);
//            } else if (dialogLevel == 1) {
//                detailDialog.dismiss();
//            }
//            dialogLevel--;
//            return true;
//        }
//    }
//
//    private View.OnClickListener listener = new View.OnClickListener() {
//        @Override
//        public void onClick(View v) {
//            switch (v.getId()) {
//                case R.id.device_des_btn_share:
//                    sv.setVisibility(View.GONE);
//                    shareLayout.setVisibility(View.VISIBLE);
//                    dialogLevel++;
//                    initShareViewDate();
//                    break;
//                case R.id.lsc_iv_back:
//                    onBackPress();
//                    break;
//                case R.id.device_des_btn_sn:
//                    sv.setVisibility(View.GONE);
//                    snLayout.setVisibility(View.VISIBLE);
//                    dialogLevel++;
//                    initSnViewDate();
//                    break;
//                case R.id.lns_iv_back:
//                    onBackPress();
//                    break;
//                case R.id.lns_btn_submit:
//                    submitSnSetting();
//                    break;
//                case R.id.lns_btn_add_subnet:
//                    addSubnetView(null);
//                    break;
//                case R.id.device_des_btn_subnet:
//                    showSubnet(mView);
//                    sv.setVisibility(View.GONE);
//                    dialogLevel++;
//                    break;
//                case R.id.device_des_btn_networks:
//                    showNetworks(mView);
//                    sv.setVisibility(View.GONE);
//                    dialogLevel++;
//                    break;
//                case R.id.ls_iv_back:
//                    onBackPress();
//                    break;
//            }
//        }
//    };
//
//
//    public void showDetailDialog() {
//        if (detailDialog != null && detailDialog.isShowing()) {
//            return;
//        }
//        mView = LayoutInflater.from(context).inflate(R.layout.layout_dialog_device_des, null);
//        detailDialog = new AppCompatDialog(context, R.style.DialogTheme);
//        detailDialog.setContentView(mView);
//
//        initView(mView);
//
//        dilName.setText(bean.getName());
//        dilVip.setText(bean.getVip());
//        dilLip.setText(bean.getPriIp());
//        dilOwner.setText(bean.getOwner());
//        dilVersion.setText(bean.getAppVersion());
//        dilDomain.setText(bean.getDomain());
//        //节点设备
//        if (bean.isOnline() && bean.getSelectable()) {
//            switchSnEnable.setVisibility(View.VISIBLE);
//            switchSnEnable.setChecked(CMAPI.getInstance().getBaseInfo().hadSelectedSn(bean.getId()) && bean.getSelectable());
//            switchSnEnable.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//                @Override
//                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//                    Runnable runnable = new Runnable() {
//                        @Override
//                        public void run() {
//                            onLoadStart(null);
//                            buttonView.postDelayed(new Runnable() {
//                                @Override
//                                public void run() {
//                                    onLoadComplete();
//                                }
//                            }, 3000);
//                        }
//                    };
//                    if ((isChecked && !CMAPI.getInstance().getBaseInfo().hadSelectedSn(bean.getId()))
//                            || (!isChecked && CMAPI.getInstance().getBaseInfo().hadSelectedSn(bean.getId())))
//
//                        if (CMAPI.getInstance().getBaseInfo().hadSelectedSn(bean.getId())) {
//                            if (CMAPI.getInstance().getConfig().isNetBlock()
//                                    && CMAPI.getInstance().getBaseInfo().getSnIds().size() == 1) {
//                                new AlertDialog.Builder(context)
//                                        .setMessage(R.string.tips_internet_access_sn_cancel)
//                                        .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
//                                            @Override
//                                            public void onClick(DialogInterface dialog, int which) {
//                                                runnable.run();
//                                                if (CMAPI.getInstance().removeSmartNode(bean.getId())) {
//                                                    switchSnEnable.setChecked(false);
//                                                    dialog.dismiss();
//                                                    DevManager.getInstance().notifyDeviceStateChanged();
//                                                }
//                                            }
//                                        })
//                                        .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
//                                            @Override
//                                            public void onClick(DialogInterface dialog, int which) {
//                                                switchSnEnable.setChecked(true);
//                                                dialog.dismiss();
//                                            }
//                                        })
//                                        .show();
//                            } else {
//                                if (CMAPI.getInstance().removeSmartNode(bean.getId())) {
//                                    switchSnEnable.setChecked(false);
//                                    runnable.run();
//                                }
//                            }
//                        } else {
//                            //节点只许单选
//                            if (CMAPI.getInstance().selectSmartNode(bean.getId())) {
//                                switchSnEnable.setChecked(true);
//                                runnable.run();
//                            }
//                        }
//                    DevManager.getInstance().notifyDeviceStateChanged();
//                }
//            });
//        }
//
//        dilLocation.setVisibility(View.GONE);
//        if (bean.getHardData() != null && !TextUtils.isEmpty(bean.getHardData().getLocation())) {
//            dilLocation.setVisibility(View.VISIBLE);
//            dilLocation.setText(bean.getHardData().getLocation());
//        }
//
//        if (bean.isOnline()) {
//            //绑定的设备
//            //管理员权限
//            if (bean.getMnglevel() == 0 || bean.getMnglevel() == 1 /*||
//                    Objects.equals(bean.getUserId(), CMAPI.getInstance().getBaseInfo().getUserId())*/) {
//                dilName.mIv.setVisibility(View.VISIBLE);
//                dilName.mIv.setImageResource(R.drawable.icon_edit);
//                dilName.mIv.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        rename();
//                    }
//                });
//
//                //节点设备
//                if (bean.getType() == 0) {
//                    //节点配置
//                    initSnView(mView);
//                }
//                //非终端设备
//                if (bean.getType() != 2) {
//                    //分享
//                    initShareView(mView);
//                }
//            }
//
//            //节点设备
//            if (bean.getType() == 0) {
//                //子网
//                List<Device.SubNet> subNets = bean.subNets;
//                if (subNets != null && subNets.size() > 0) {
//                    subnet.setVisibility(View.VISIBLE);
//                    subnet.setOnClickListener(listener);
//                } else {
//                    subnet.setVisibility(View.GONE);
//                }
//            }
//
//            if (bean.isNas()) {
//                //nas设备
//                progressLayout.setVisibility(View.VISIBLE);
//                dilMarkName.setVisibility(View.VISIBLE);
//                dilMarkName.mIv.setVisibility(View.VISIBLE);
//                dilMarkName.mIv.setImageResource(R.drawable.icon_edit);
//                mLayoutUpgradeProgress = mView.findViewById(R.id.layout_upgrade_progress);
//                mProgressBarDownload = mView.findViewById(R.id.progressBar_download);
//                mProgressBarInstall = mView.findViewById(R.id.progressBar_install);
//                dilMarkName.mIv.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        editMarkName();
//                    }
//                });
//                final DeviceModel deviceModel = SessionManager.getInstance().getDeviceModel(bean.getDeviceId());
//                if (deviceModel != null) {
//                    final Disposable subscribe = deviceModel.getDevNameFromDB().subscribe(new Consumer<String>() {
//                        @Override
//                        public void accept(String s) {
//                            if (dilMarkName != null)
//                                dilMarkName.setText(s);
//                        }
//                    });
//                    addDisposable(subscribe);
//                    checkUpdate(deviceModel);
//                }
//                accessView.setVisibility(View.VISIBLE);
//                accessView.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        SessionManager.getInstance().getLoginSession(deviceModel.getDevId(), new GetSessionListener(false) {
//                            @Override
//                            public void onSuccess(String url, LoginSession loginSession) {
//                                String uriString = OneOSAPIs.PREFIX_HTTP + bean.getDomain()
//                                        + String.format("?%s=%s", OneOSAPIs.SUFFIX_TOKEN, loginSession.getSession());
//                                WebViewActivity.open(v.getContext(), "", uriString, false);
//                            }
//
//                            @Override
//                            public void onFailure(String url, int errorNo, String errorMsg) {
//                                String uriString = OneOSAPIs.PREFIX_HTTP + bean.getDomain();
//                                WebViewActivity.open(v.getContext(), "", uriString, false);
//                            }
//                        });
//
//                    }
//                });
//                initSpaceLayout(mView);
//            }
//        } else {
//            dilVip.setVisibility(View.GONE);
//            dilLip.setVisibility(View.GONE);
//            dilVersion.setVisibility(View.GONE);
//            dilDomain.setVisibility(View.GONE);
//        }
//        List<String> networks = bean.getNetworks();
//        if (networks != null && networks.size() > 0) {
//            locationNetworks.setVisibility(View.VISIBLE);
//            locationNetworks.setOnClickListener(listener);
//        } else {
//            locationNetworks.setVisibility(View.GONE);
//        }
//        detailDialog.show();
//        sv.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
//            @Override
//            public void onGlobalLayout() {
//                DisplayMetrics metrics = context.getResources().getDisplayMetrics();
//                final Window window = detailDialog.getWindow();
//                if (window != null) {
//                    WindowManager.LayoutParams params = window.getAttributes();
//                    params.width = (int) (metrics.widthPixels * 0.80);
//                    window.setAttributes(params);
//                    sv.getViewTreeObserver().removeOnGlobalLayoutListener(this);
//                }
//            }
//        });
//        dialogLevel = 1;
//        detailDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
//            @Override
//            public void onDismiss(DialogInterface dialog) {
//                dismiss();
//            }
//        });
//    }
//
//    private void checkUpdate(DeviceModel deviceModel) {
//        String devId = deviceModel.getDevId();
//        if (deviceModel.isOnline() && deviceModel.isOwner()) {
//            mM3UpdateViewModel = new M3UpdateViewModel();
//            mM3UpdateViewModel.getUpdateInfo(deviceModel).(new Observer<Resource<UpdateInfo>>() {
//                @Override
//                public void onChanged(Resource<UpdateInfo> updateInfoResource) {
//                    if (updateInfoResource.getStatus() == Status.SUCCESS && updateInfoResource.getData() != null) {
//                        m3UpgradeBtn.setVisibility(View.VISIBLE);
//                        m3UpgradeBtn.setOnClickListener(new View.OnClickListener() {
//                            @Override
//                            public void onClick(View v) {
//                                mM3UpdateViewModel.update(v.getContext(), devId, updateInfoResource.getData()).observeForever(new Observer<Resource<Boolean>>() {
//                                    @Override
//                                    public void onChanged(Resource<Boolean> booleanResource) {
//                                        if (booleanResource.getStatus() == Status.SUCCESS) {
//                                            loadingView.setVisibility(View.GONE);
//                                            mLayoutUpgradeProgress.setVisibility(View.VISIBLE);
//                                            mM3UpdateViewModel.subUpgradeProgress().observeForever(new Observer<Resource<UpgradeProgress>>() {
//                                                @Override
//                                                public void onChanged(Resource<UpgradeProgress> upgradeProgressResource) {
//                                                    if (upgradeProgressResource.getStatus() == Status.SUCCESS) {
//                                                        final UpgradeProgress upgradeProgress = upgradeProgressResource.getData();
//
//                                                        if (upgradeProgress != null && "download".equalsIgnoreCase(upgradeProgress.getName())) {
//                                                            if (upgradeProgress.getPercent() >= 0)
//                                                                mProgressBarDownload.setProgress(upgradeProgress.getPercent());
//                                                            else {
//                                                                mLayoutUpgradeProgress.setVisibility(View.GONE);
//                                                                ToastHelper.showToast(R.string.device_upgrade_failed_by_download);
//                                                            }
//                                                        }
//                                                        if (upgradeProgress != null && "install".equalsIgnoreCase(upgradeProgress.getName())) {
//                                                            if (upgradeProgress.getPercent() >= 0) {
//                                                                mProgressBarInstall.setProgress(upgradeProgress.getPercent());
//                                                                if (upgradeProgress.getPercent() == 100) {
//                                                                    mM3UpdateViewModel.showPowerDialog(v.getContext(), devId, false);
//                                                                }
//                                                            } else {
//                                                                mLayoutUpgradeProgress.setVisibility(View.GONE);
//                                                                ToastHelper.showToast(R.string.device_upgrade_failed_by_install);
//                                                            }
//                                                        }
//                                                    }
//                                                }
//                                            });
//                                        } else if (booleanResource.getStatus() == Status.ERROR) {
//                                            loadingView.setVisibility(View.GONE);
//                                        } else if (booleanResource.getStatus() == Status.LOADING) {
//                                            loadingView.setVisibility(View.VISIBLE);
//                                        }
//                                    }
//                                });
//                            }
//                        });
//                    }
//                }
//            });
//
//        }
//    }
//
//    public void dismiss() {
//        if (mM3UpdateViewModel != null) {
//            mM3UpdateViewModel.onCleared();
//            mM3UpdateViewModel = null;
//        }
//        dispose();
//    }
//
//    private void initView(View view) {
//        sv = view.findViewById(R.id.device_des_sv);
//        progressLayout = view.findViewById(R.id.device_des_progress_layout);
//        switchSnEnable = view.findViewById(R.id.switch_sn_enable);
//        loadingView = view.findViewById(R.id.layout_loading);
//        accessView = view.findViewById(R.id.device_des_btn_by_browser);
//        m3UpgradeBtn = view.findViewById(R.id.device_des_upgrade);
//        locationNetworks = view.findViewById(R.id.device_des_btn_networks);
//        subnet = view.findViewById(R.id.device_des_btn_subnet);
//        view.findViewById(R.id.device_des_iv_back).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                detailDialog.dismiss();
//            }
//        });
//
//        dilName = view.findViewById(R.id.des_dil_name);
//        dilMarkName = view.findViewById(R.id.des_dil_mark_name);
//        dilVip = view.findViewById(R.id.des_dil_vip);
//        dilLip = view.findViewById(R.id.des_dil_lip);
//        dilOwner = view.findViewById(R.id.des_dil_owner);
//        dilVersion = view.findViewById(R.id.des_dil_version);
//        dilDomain = view.findViewById(R.id.des_dil_domain);
//        dilLocation = view.findViewById(R.id.des_dil_location);
//        view.setFocusableInTouchMode(true);
//        view.requestFocus();
//        view.setOnKeyListener(new View.OnKeyListener() {
//            @Override
//            public boolean onKey(View v, int keyCode, KeyEvent event) {
//                if (event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_BACK) {
//                    return onBackPress();
//                }
//                return false;
//            }
//        });
//        View.OnClickListener listener = v -> {
//            switch (v.getId()) {
//                case R.id.des_dil_name:
//                    clipString(dilName.mTvData.getText().toString().trim());
//                    break;
//                case R.id.des_dil_mark_name:
//                    clipString(dilMarkName.mTvData.getText().toString().trim());
//                    break;
//                case R.id.des_dil_vip:
//                    clipString(dilVip.mTvData.getText().toString().trim());
//                    break;
//                case R.id.des_dil_lip:
//                    clipString(dilLip.mTvData.getText().toString().trim());
//                    break;
//                case R.id.des_dil_owner:
//                    clipString(dilOwner.mTvData.getText().toString().trim());
//                    break;
//                case R.id.des_dil_version:
//                    clipString(dilVersion.mTvData.getText().toString().trim());
//                    break;
//                case R.id.des_dil_domain:
//                    clipString(dilDomain.mTvData.getText().toString().trim());
//                    break;
//                case R.id.des_dil_location:
//                    clipString(dilLocation.mTvData.getText().toString().trim());
//                    break;
//            }
//        };
//        dilName.setDataOnClickListener(listener);
//        dilMarkName.setDataOnClickListener(listener);
//        dilVip.setDataOnClickListener(listener);
//        dilLip.setDataOnClickListener(listener);
//        dilOwner.setDataOnClickListener(listener);
//        dilVersion.setDataOnClickListener(listener);
//        dilDomain.setDataOnClickListener(listener);
//        dilLocation.setDataOnClickListener(listener);
//    }
//
//    private void clipString(String content) {
//        if (context != null) {
//            ClipboardUtils.copyToClipboard(context, content);
//            ToastUtils.showToast(context.getString(R.string.Copied) + content);
//        }
//    }
//
//    private void rename() {
//        DialogUtil.showEditDialog(context, context.getString(R.string.rename), bean.getName(), "",
//                context.getString(R.string.confirm), new DialogUtil.OnDialogButtonClickListener() {
//
//                    @Override
//                    public void onClick(View v, final String strEdit, final Dialog dialog, boolean isCheck) {
//                        if (TextUtils.isEmpty(strEdit)) {
//                            AnimUtils.sharkEditText(context, v);
//                        } else {
//                            SetDeviceNameHttpLoader loader = new SetDeviceNameHttpLoader(GsonBaseProtocol.class);
//                            loader.setHttpLoaderStateListener(DeviceDetailDialogUtil.this);
//                            loader.setParams(bean.getDeviceId(), strEdit);
//                            loader.executor(new MyOkHttpListener() {
//                                @Override
//                                public void success(Object tag, GsonBaseProtocol gsonBaseProtocol) {
//                                    bean.setName(strEdit);
//                                    DevManager.getInstance().notifyDeviceStateChanged();
//                                    dilName.setText(strEdit);
//                                    dialog.dismiss();
//                                }
//                            });
//                        }
//                    }
//                },
//                context.getString(R.string.cancel), null);
//    }
//
//    private void editMarkName() {
//        if ((!bean.isOnline())) {
//            ToastUtils.showToast(R.string.device_offline);
//            return;
//        }
//        final DeviceViewModel deviceViewModel = new DeviceViewModel();
//        deviceViewModel.showDeviceName(context, bean.getDeviceId(), dilMarkName.mTvData.getText().toString().trim(),
//                newName -> {
//                    dilMarkName.setText(newName);
//                    DevManager.getInstance().notifyDeviceStateChanged();
//                });
//    }
//
//    private void initShareView(View view) {
//        share = view.findViewById(R.id.device_des_btn_share);
//        shareLayout = view.findViewById(R.id.layout_share_code);
//        shareBack = view.findViewById(R.id.lsc_iv_back);
//        switchShare = view.findViewById(R.id.lsc_switch_share);
//        shareImageContainer = view.findViewById(R.id.lsc_container);
//        imgShareQR = view.findViewById(R.id.lsc_iv_qr);
//        tvShareCode = view.findViewById(R.id.lsc_tv_share_code);
//        tvShareTips = view.findViewById(R.id.lsc_tv_tips);
//        switchShareNeedAuth = view.findViewById(R.id.lsc_switch_share_need_Auth);
//        shareBtn = view.findViewById(R.id.lsc_btn_share);
//
//
//        share.setVisibility(View.VISIBLE);
//        share.setOnClickListener(listener);
//        shareBack.setOnClickListener(listener);
//
//        switchShare.setChecked(bean.getHardData() != null && bean.getHardData().getEnableshare());
//        switchShareNeedAuth.setChecked(bean.getHardData() != null && bean.getHardData().isScanconfirm());
//        refreshShareView(bean.getHardData() != null && bean.getHardData().getEnableshare());
//
//        switchShare.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(CompoundButton buttonView, final boolean isChecked) {
//                ShareUtil.savedEnableShareState(bean.getDeviceId(), isChecked,
//                        DeviceDetailDialogUtil.this, new ResultListener() {
//                            @Override
//                            public void success(Object tag, GsonBaseProtocol data) {
//                                if (bean.getHardData() != null)
//                                    bean.getHardData().setEnableshare(isChecked);
//                                refreshShareView(isChecked);
//                                initShareViewDate();
//                            }
//
//                            @Override
//                            public void error(Object tag, GsonBaseProtocol baseProtocol) {
//                                switchShare.setChecked(bean.getHardData() != null && bean.getHardData().getEnableshare());
//                            }
//                        });
//            }
//        });
//        switchShareNeedAuth.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(CompoundButton buttonView, final boolean isChecked) {
//                ShareUtil.savedScanConfirmState(bean.getDeviceId(), isChecked,
//                        DeviceDetailDialogUtil.this, new ResultListener() {
//                            @Override
//                            public void success(Object tag, GsonBaseProtocol data) {
//                                if (bean.getHardData() != null)
//                                    bean.getHardData().setScanconfirm(isChecked);
//                            }
//
//                            @Override
//                            public void error(Object tag, GsonBaseProtocol baseProtocol) {
//                                switchShareNeedAuth.setChecked(bean.getHardData() != null && bean.getHardData().isScanconfirm());
//                            }
//                        });
//            }
//        });
//        shareBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                //保存二维码到本地
//                ShareUtil.saveAndShareImg(shareImageContainer, shareCode, null);
//            }
//        });
//    }
//
//    private String shareCode;
//
//    private void initShareViewDate() {
//        if (bean.getHardData() != null && bean.getHardData().getEnableshare()) {
//            //获取分享吗
//            ShareUtil.getDeviceShareCode(bean.getDeviceId(), this,
//                    new MyOkHttpListener<ShareCode>() {
//                        @Override
//                        public void success(Object tag, final ShareCode data) {
//                            //生成二维码
//                            shareCode = data.sharecode;
//                            ShareUtil.generateQRCode(imgShareQR, MyConstants.EVENT_CODE_HARDWAER_DEVICE, shareCode,
//                                    new ShareUtil.QRCodeResult() {
//                                        @Override
//                                        public void onGenerated(Bitmap bitmap, String tips) {
//                                            imgShareQR.setImageBitmap(bitmap);
//                                            tvShareCode.setText(shareCode);
//                                            tvShareTips.setText(tips);
//                                            MessageManager.getInstance().quickDelay();
//                                        }
//                                    });
//                        }
//                    });
//        }
//    }
//
//    private void refreshShareView(boolean shareable) {
//        if (shareable) {
//            shareImageContainer.setVisibility(View.VISIBLE);
//            switchShareNeedAuth.setVisibility(View.VISIBLE);
//            shareBtn.setEnabled(true);
//        } else {
//            shareImageContainer.setVisibility(View.GONE);
//            switchShareNeedAuth.setVisibility(View.GONE);
//            shareBtn.setEnabled(false);
//        }
//    }
//
//    private void initSnView(final View view) {
//        snLayout = view.findViewById(R.id.layout_node_setting);
//        snBack = view.findViewById(R.id.lns_iv_back);
//        snSetting = view.findViewById(R.id.device_des_btn_sn);
//        snSubmit = view.findViewById(R.id.lns_btn_submit);
//        internetSwitch = view.findViewById(R.id.lns_switch_access_internet);
//        dnsSwitch = view.findViewById(R.id.lns_switch_dns);
//        dnsLayout = view.findViewById(R.id.lns_layout_dns);
//        dnsEdit1 = view.findViewById(R.id.lns_edit_dns_1);
//        dnsEdit2 = view.findViewById(R.id.lns_edit_dns_2);
//        subnetSwitch = view.findViewById(R.id.lns_switch_subnet);
//        subnetLayout = view.findViewById(R.id.lns_layout_subnet);
//        addSubnet = view.findViewById(R.id.lns_btn_add_subnet);
//
//        snSetting.setVisibility(View.VISIBLE);
//        snSetting.setOnClickListener(listener);
//        snBack.setOnClickListener(listener);
//        addSubnet.setOnClickListener(listener);
//        snSubmit.setOnClickListener(listener);
//
//        refreshSNView();
//        internetSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//                refreshSNView();
//            }
//        });
//        dnsSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//                refreshSNView();
//            }
//        });
//        subnetSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//                refreshSNView();
//            }
//        });
//    }
//
//    private List<SubnetEntity> mSubnets = new ArrayList<>();
//
//    private void initSnViewDate() {
//        snInternetEnable = SmartNodeUtil.isAccessInternet(bean);
//        snDnsEnable = false;
//        snSubnetEnable = SmartNodeUtil.isAccessSubnet(bean);
//
//        String mDns = bean.getDns();
//        if (!TextUtils.isEmpty(mDns)) {
//            String[] split = mDns.split(",");
//            if (split.length > 0) {
//                dnsEdit1.setText(split[0]);
//                if (snInternetEnable) {
//                    dnsLayout.setVisibility(View.VISIBLE);
//                    snDnsEnable = true;
//                }
//            }
//            if (split.length > 1)
//                dnsEdit2.setText(split[1]);
//        }
//
//        internetSwitch.setChecked(snInternetEnable);
//        dnsSwitch.setChecked(snDnsEnable);
//        subnetSwitch.setChecked(snSubnetEnable);
//
//        SmartNodeUtil.getSubnet(bean, this,
//                new MyOkHttpListener<SubnetList>() {
//                    @Override
//                    public void success(Object tag, final SubnetList subnetList) {
//                        if (subnetList != null) {
//                            while (subnetLayout.getChildCount() > 1) {
//                                subnetLayout.removeViewAt(0);
//                            }
//                            mSubnets.clear();
//                            mSubnets.addAll(subnetList.getSubnet());
//                            if (mSubnets != null) {
//                                if (mSubnets.size() > 0) {
//                                    for (SubnetEntity entity : mSubnets) {
//                                        if (!TextUtils.isEmpty(entity.getNet()) ||
//                                                !TextUtils.isEmpty(entity.getMask())) {
//                                            addSubnetView(entity);
//                                        }
//                                    }
//                                } else {
//                                    addSubnetView(null);
//                                }
//                            }
//                        }
//                    }
//
//                    @Override
//                    public void error(Object tag, GsonBaseProtocol baseProtocol) {
//                        super.error(tag, baseProtocol);
//                        onBackPress();
//                    }
//                });
//    }
//
//    private void addSubnetView(SubnetEntity entity) {
//        final SubnetLayout subnet = new SubnetLayout(context);
//        if (entity != null) {
//            subnet.getEtIp().setText(entity.getNet());
//            subnet.getEtNetmask().setText(entity.getMask());
//        }
//        subnet.setRemoveListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (subnetLayout.getChildCount() == 2) {
//                    SubnetLayout child = (SubnetLayout) subnetLayout.getChildAt(0);
//                    child.getEtIp().setText("");
//                    child.getEtNetmask().setText("");
//                } else {
//                    subnetLayout.removeView(subnet);
//                }
//            }
//        });
//        subnetLayout.addView(subnet, subnetLayout.getChildCount() - 1);
//    }
//
//    private void submitSnSetting() {
//        final boolean accessInternet = internetSwitch.isChecked();
//        final boolean accessSubnet = subnetSwitch.isChecked();
//        final boolean isUsedDns = dnsSwitch.isChecked();
//        final StringBuffer dnsBuff = new StringBuffer();
//        if (accessInternet && isUsedDns) {
//            String dns1 = dnsEdit1.getText().toString().trim();
//            String dns2 = dnsEdit2.getText().toString().trim();
//            if (!TextUtils.isEmpty(dns1) || !TextUtils.isEmpty(dns2)) {
//                String dnsRegex = "^(((\\d{1,2})|(1\\d{2})|(2[0-4]\\d)|(25[0-5]))\\.){3}((\\d{1,2})|(1\\d{2})|(2[0-4]\\d)|(25[0-5]))$";
//                if (dns1.matches(dnsRegex)) {
//                    dnsBuff.append(dns1);
//                } else {
//                    ToastUtils.showToast(R.string.pls_enter_right_dns);
//                    dnsEdit1.requestFocus();
//                    return;
//                }
//                if (dns2.matches(dnsRegex)) {
//                    dnsBuff.append(",").append(dns2);
//                } else if (!TextUtils.isEmpty(dns2)) {
//                    ToastUtils.showToast(R.string.pls_enter_right_dns);
//                    dnsEdit2.requestFocus();
//                    return;
//                }
//            }
//        }
//        final List<SubnetEntity> subnet = new ArrayList<>();
//        if (accessSubnet) {
//            for (int i = 0; i < subnetLayout.getChildCount() - 1; i++) {
//                SubnetLayout child = (SubnetLayout) subnetLayout.getChildAt(i);
//                if (TextUtils.isEmpty(child.getEtIp().getText().toString().trim()) &&
//                        TextUtils.isEmpty(child.getEtNetmask().getText().toString().trim())) {
//                    continue;
//                }
//                String ip = child.getIp();
//                if (ip == null) {
//                    child.getEtIp().requestFocus();
//                    ToastUtils.showToast(R.string.pls_enter_right_subnet_ip);
//                    return;
//                }
//                child.getEtIp().setText(ip);
//                String netmask = child.getNetmask();
//                if (netmask == null) {
//                    child.getEtNetmask().requestFocus();
//                    ToastUtils.showToast(R.string.pls_enter_right_subnet_mask);
//                    return;
//                }
//                child.getEtNetmask().setText(netmask);
//                SubnetEntity entity = new SubnetEntity(ip, netmask);
//                if (!subnet.contains(entity)) {
//                    subnet.add(entity);
//                } else {
//                    child.getEtNetmask().requestFocus();
//                    ToastUtils.showToast(R.string.repeated_subnet);
//                    return;
//                }
//            }
//            if (subnet.size() == 0) {
//                ToastUtils.showToast(R.string.pls_configure_at_least_one_subnet);
//                return;
//            }
//        }
//        if (snInternetEnable != accessInternet || snSubnetEnable != accessSubnet) {
//            SmartNodeUtil.submitAccessFlag(bean, accessInternet, accessSubnet, this, new MyOkHttpListener() {
//                @Override
//                public void success(Object tag, GsonBaseProtocol data) {
//                    commitDnsAndSubnet(true, accessInternet, accessSubnet, isUsedDns, dnsBuff.toString(), subnet);
//                }
//            });
//        } else {
//            commitDnsAndSubnet(false, accessInternet, accessSubnet, isUsedDns, dnsBuff.toString(), subnet);
//        }
//    }
//
//    boolean submitDns;
//    boolean submitSubnet;
//
//    private void commitDnsAndSubnet(boolean isSubmitFlag, final boolean accessInternet, final boolean accessSubnet, boolean isUsedDns, String dns, List<SubnetEntity> subnet) {
//        boolean isCommit = false;
//        submitDns = false;
//        submitSubnet = false;
//        if (accessInternet) {
//            if (!bean.getDns().equals(dns)) {
//                SmartNodeUtil.submitDns(bean, dns, this, new MyOkHttpListener() {
//                    @Override
//                    public void success(Object tag, GsonBaseProtocol data) {
//                        submitDns = true;
//                        if (!accessSubnet || submitSubnet)
//                            detailDialog.dismiss();
//                    }
//                });
//                isCommit = true;
//            } else {
//                submitDns = true;
//            }
//        }
//        if (accessSubnet) {
//            if (!mSubnets.containsAll(subnet) || !subnet.containsAll(mSubnets)) {
//                SmartNodeUtil.submitSubnet(bean, subnet, this, new MyOkHttpListener() {
//                    @Override
//                    public void success(Object tag, GsonBaseProtocol data) {
//                        submitSubnet = true;
//                        if (!accessInternet || submitDns)
//                            detailDialog.dismiss();
//                    }
//                });
//                isCommit = true;
//            } else {
//                submitSubnet = true;
//            }
//        }
//        if (!isCommit) {
//            if (isSubmitFlag)
//                detailDialog.dismiss();
//            else
//                onBackPress();
//        }
//    }
//
//    private void refreshSNView() {
//        internetSwitch.setVisibility(View.VISIBLE);
//        dnsSwitch.setVisibility(View.GONE);
//        dnsLayout.setVisibility(View.GONE);
//        subnetSwitch.setVisibility(View.VISIBLE);
//        subnetLayout.setVisibility(View.GONE);
//        if (internetSwitch.isChecked()) {
//            dnsSwitch.setVisibility(View.VISIBLE);
//            if (dnsSwitch.isChecked()) {
//                dnsLayout.setVisibility(View.VISIBLE);
//            }
//        }
//        if (subnetSwitch.isChecked()) {
//            subnetLayout.setVisibility(View.VISIBLE);
//        }
//    }
//
//    private void showSubnet(final View view) {
//        subLayout = view.findViewById(R.id.layout_subnet);
//        subLayout.setVisibility(View.VISIBLE);
//        View subBack = subLayout.findViewById(R.id.ls_iv_back);
//        RecyclerView subRv = subLayout.findViewById(R.id.ls_rv);
//        TextView tvTitle = subLayout.findViewById(R.id.ls_tv_title);
//        tvTitle.setText(R.string.subnet);
//        subBack.setOnClickListener(listener);
//        SubnetRVAdapter adapter = new SubnetRVAdapter();
//        subRv.setLayoutManager(new LinearLayoutManager(context, RecyclerView.VERTICAL, false));
//        subRv.setItemAnimator(null);
//        subRv.setAdapter(adapter);
//        List<Device.SubNet> subNets = new ArrayList<>(bean.subNets);
//        Device.SubNet title = new Device.SubNet();
//        title.net = "IP";
//        title.mask = "Mask";
//        subNets.add(0, title);
//        adapter.setNewData(subNets);
//    }
//
//    private void showNetworks(View view) {
//        subLayout = view.findViewById(R.id.layout_subnet);
//        subLayout.setVisibility(View.VISIBLE);
//        View subBack = subLayout.findViewById(R.id.ls_iv_back);
//        RecyclerView subRv = subLayout.findViewById(R.id.ls_rv);
//        TextView tvTitle = subLayout.findViewById(R.id.ls_tv_title);
//        tvTitle.setText(R.string.network_location);
//        subBack.setOnClickListener(listener);
//        HomeNetRVAdapter adapter = new HomeNetRVAdapter(null);
//        adapter.setShowShare(false);
//        subRv.setLayoutManager(new LinearLayoutManager(context, RecyclerView.VERTICAL, false));
//        subRv.setItemAnimator(null);
//        subRv.setAdapter(adapter);
//        List<Network> networks = new ArrayList<>();
//        List<String> strings = bean.getNetworks();
//        List<Network> list = NetManager.getInstance().getNetBeans();
//        if (strings != null) {
//            for (Network network : list) {
//                for (String next : strings) {
//                    if (Objects.equals(network.getId(), next)) {
//                        networks.add(network);
//                        break;
//                    }
//                }
//                if (networks.size() == strings.size()) {
//                    break;
//                }
//            }
//        }
//        adapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
//            @Override
//            public void onItemClick(BaseQuickAdapter baseQuickAdapter, View view, int i) {
//                Network o = (Network) baseQuickAdapter.getData().get(i);
//                EventBus.getDefault().post(o);
//                detailDialog.dismiss();
//            }
//        });
//        adapter.setNewData(networks);
//    }
//
//    private void initSpaceLayout(View view) {
//        SessionManager.getInstance().getLoginSession(bean.getDeviceId(), new GetSessionListener() {
//
//            @Override
//            public void onSuccess(String url, final LoginSession data) {
//                OneOSSpaceAPI spaceAPI2 = new OneOSSpaceAPI(data);
//                spaceAPI2.setOnSpaceListener(new OneOSSpaceAPI.OnSpaceListener() {
//                    @Override
//                    public void onStart(String url) {
//                    }
//
//                    @Override
//                    public void onSuccess(String url, boolean isOneOSSpace, @NonNull OneOSHardDisk hd1, OneOSHardDisk hd2) {
//                        final long total = hd1.getTotal();
//                        final long free = hd1.getFree();
//                        final long used = hd1.getUsed();
//
//                        OneOSSpaceAPI spaceAPI = new OneOSSpaceAPI(data);
//                        spaceAPI.setOnSpaceListener(new OneOSSpaceAPI.OnSpaceListener() {
//                            @Override
//                            public void onStart(String url) {
//
//                            }
//
//                            @Override
//                            public void onSuccess(String url, boolean isOneOSSpace, @NonNull OneOSHardDisk hd1, OneOSHardDisk hd2) {
//                                String totalInfo;
//                                long total2 = hd1.getTotal();
//                                long used2 = hd1.getUsed();
//                                int progress;
//                                int progress2;
//                                totalInfo = FileUtils.fmtFileSize(total);
//                                progress = (int) (used2 * 100f / (total));
//                                progress2 = (int) (used * 100f / total);
//                                String usedInfo = FileUtils.fmtFileSize(used2);
//                                String usedOther = FileUtils.fmtFileSize(used - used2);
//                                ((TextView) view.findViewById(R.id.space_text)).setText(String.format("%s / %s", FileUtils.fmtFileSize(used), totalInfo));
//                                ((TextView) view.findViewById(R.id.text_wo)).setText(context.getString(R.string.used_by_me, usedInfo));
//                                ((TextView) view.findViewById(R.id.text_all)).setText(context.getString(R.string.other_used, usedOther));
//                                ((ProgressBar) view.findViewById(R.id.space_progress)).setProgress(progress);
//                                ((ProgressBar) view.findViewById(R.id.space_progress)).setSecondaryProgress(progress2);
//                            }
//
//                            @Override
//                            public void onFailure(String url, int errorNo, String errorMsg) {
//
//                            }
//                        });
//                        spaceAPI.query(false);
//                    }
//
//                    @Override
//                    public void onFailure(String url, int errorNo, String errorMsg) {
//                    }
//                });
//                spaceAPI2.query(true);
//            }
//
//        });
//    }
//
//    @Override
//    public void onLoadStart(Disposable disposable) {
//        if (loadingView != null)
//            loadingView.setVisibility(View.VISIBLE);
//    }
//
//    @Override
//    public void onLoadComplete() {
//        loadingView.setVisibility(View.GONE);
//    }
//
//    @Override
//    public void onLoadError() {
//        loadingView.setVisibility(View.GONE);
//    }
//}
