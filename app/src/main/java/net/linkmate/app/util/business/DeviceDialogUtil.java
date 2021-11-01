package net.linkmate.app.util.business;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Pair;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.google.gson.Gson;

import net.linkmate.app.BuildConfig;
import net.linkmate.app.R;
import net.linkmate.app.base.MyConstants;
import net.linkmate.app.base.MyOkHttpListener;
import net.linkmate.app.bean.DeviceBean;
import net.linkmate.app.data.ScoreHelper;
import net.linkmate.app.manager.DevManager;
import net.linkmate.app.manager.MessageManager;
import net.linkmate.app.manager.NetManager;
import net.linkmate.app.ui.activity.WebViewActivity;
import net.linkmate.app.ui.activity.mine.DevFlowDetailsActivity;
import net.linkmate.app.ui.activity.mine.WebActivity;
import net.linkmate.app.ui.activity.nasApp.NasAppsActivity;
import net.linkmate.app.ui.nas.helper.HdManageActivity;
import net.linkmate.app.ui.nas.torrent.TorrentActivity;
import net.linkmate.app.ui.nas.user.UserManageActivity;
import net.linkmate.app.ui.viewmodel.M8CheckUpdateViewModel;
import net.linkmate.app.ui.viewmodel.TrafficPriceEditViewModel;
import net.linkmate.app.ui.viewmodel.TrafficPriceEditViewModelKt;
import net.linkmate.app.util.DialogUtil;
import net.linkmate.app.util.ToastUtils;
import net.linkmate.app.util.UIUtils;
import net.linkmate.app.view.DataItemLayout;
import net.linkmate.app.view.SubnetLayout;
import net.linkmate.app.view.adapter.DevUserMngRVAdapter;
import net.linkmate.app.view.adapter.HomeNetRVAdapter;
import net.linkmate.app.view.adapter.SubnetRVAdapter;
import net.sdvn.cmapi.BaseInfo;
import net.sdvn.cmapi.CMAPI;
import net.sdvn.cmapi.Device;
import net.sdvn.cmapi.DevicePrivateModel;
import net.sdvn.cmapi.Network;
import net.sdvn.cmapi.global.Constants;
import net.sdvn.cmapi.util.ClipboardUtils;
import net.sdvn.common.ErrorCode;
import net.sdvn.common.internet.core.GsonBaseProtocol;
import net.sdvn.common.internet.core.HttpLoader;
import net.sdvn.common.internet.listener.ListResultListener;
import net.sdvn.common.internet.listener.ResultListener;
import net.sdvn.common.internet.loader.GradeBindDeviceHttpLoader;
import net.sdvn.common.internet.loader.SetDeviceNameHttpLoader;
import net.sdvn.common.internet.loader.UnbindDeviceHttpLoader;
import net.sdvn.common.internet.protocol.ShareCode;
import net.sdvn.common.internet.protocol.SharedUserList;
import net.sdvn.common.internet.protocol.SubnetList;
import net.sdvn.common.internet.protocol.UnbindDeviceResult;
import net.sdvn.common.internet.protocol.entity.ShareUser;
import net.sdvn.common.internet.protocol.entity.SubnetEntity;
import net.sdvn.common.internet.utils.LoginTokenUtil;
import net.sdvn.common.repo.BriefRepo;
import net.sdvn.common.vo.BriefModel;
import net.sdvn.nascommon.LibApp;
import net.sdvn.nascommon.SessionManager;
import net.sdvn.nascommon.constant.AppConstants;
import net.sdvn.nascommon.constant.HttpErrorNo;
import net.sdvn.nascommon.constant.OneOSAPIs;
import net.sdvn.nascommon.iface.GetSessionListener;
import net.sdvn.nascommon.iface.Result;
import net.sdvn.nascommon.model.DeviceModel;
import net.sdvn.nascommon.model.UiUtils;
import net.sdvn.nascommon.model.oneos.OneOSHardDisk;
import net.sdvn.nascommon.model.oneos.OneOSInfo;
import net.sdvn.nascommon.model.oneos.UpdateInfo;
import net.sdvn.nascommon.model.oneos.api.sys.OneOSHardDiskInfoAPI;
import net.sdvn.nascommon.model.oneos.api.sys.OneOSPowerAPI;
import net.sdvn.nascommon.model.oneos.api.sys.OneOSSpaceAPI;
import net.sdvn.nascommon.model.oneos.api.user.OneOSClearUsersAPI;
import net.sdvn.nascommon.model.oneos.api.user.OneOSUserManageAPI;
import net.sdvn.nascommon.model.oneos.event.UpgradeProgress;
import net.sdvn.nascommon.model.oneos.user.LoginSession;
import net.sdvn.nascommon.utils.AnimUtils;
import net.sdvn.nascommon.utils.DialogUtils;
import net.sdvn.nascommon.utils.EmptyUtils;
import net.sdvn.nascommon.utils.FileUtils;
import net.sdvn.nascommon.utils.ToastHelper;
import net.sdvn.nascommon.utils.Utils;
import net.sdvn.nascommon.viewmodel.DeviceViewModel;
import net.sdvn.nascommon.viewmodel.M3UpdateViewModel;
import net.sdvn.nascommon.widget.AnimCircleProgressBar;
import net.sdvn.nascommon.widget.PopDialogFragment;

import org.greenrobot.eventbus.EventBus;
import org.jetbrains.annotations.NotNull;
import org.view.libwidget.SingleClickKt;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Single;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import io.weline.devhelper.DevTypeHelper;
import io.weline.repo.SessionCache;
import io.weline.repo.data.model.BaseProtocol;
import io.weline.repo.net.V5Observer;
import io.weline.repo.repository.V5Repository;
import io.weline.repo.torrent.BTHelper;
import libs.source.common.livedata.Resource;
import libs.source.common.livedata.Status;
import timber.log.Timber;

public class DeviceDialogUtil implements HttpLoader.HttpLoaderStateListener {

    private final Context context;
    private final DeviceBean bean;
    private final int position;
    private FragmentManager mFragmentManager;
    private CompositeDisposable compositeDisposable;
    private M3UpdateViewModel mM3UpdateViewModel;
    private View mView;
    private View.OnFocusChangeListener focusChangeListener = new View.OnFocusChangeListener() {
        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            if (hasFocus) {
                InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(v, InputMethodManager.SHOW_FORCED);
            }
        }
    };

    public void addDisposable(@NonNull Disposable disposable) {
        if (compositeDisposable == null) {
            compositeDisposable = new CompositeDisposable();
        }
        compositeDisposable.add(disposable);
    }

    protected void dispose() {
        if (compositeDisposable != null) compositeDisposable.dispose();
    }

    public DeviceDialogUtil(Context context, DeviceBean bean, int position, FragmentManager manager) {
        this.context = context;
        this.bean = bean;
        this.position = position;
        mFragmentManager = manager;
    }

    private PopDialogFragment detailDialog;
    private View sv;
    private Switch switchSnEnable;
    private View share;
    private View accessView;
    private View m3UpgradeBtn;
    private View snSetting;
    private View subnet;
    private View loadingView;
    private View btnDelete;
    private View btnGetScore;
    private View btnDevFlowMng;
    private View btnDevMng;
    private LinearLayout devMngLayout;
    private View devMngBack;

    private View llExtrlDil;
    private View llExpand;
    private TextView tvExpand;
    private View ivExpand;

    private DataItemLayout dilName;
    private DataItemLayout dilMarkName;
    private DataItemLayout dilTrafficPrice;
    private DataItemLayout dilVip;
    private DataItemLayout dilLip;
    private DataItemLayout dilOwner;
    private DataItemLayout dilVersion;
    private DataItemLayout dilDomain;
    private DataItemLayout dilLocation;
    private DataItemLayout dilAddTime;

    // isAdmin
    private View remoteManage;

    //    private DataItemLayout dilId;
    private DataItemLayout dilSn;
    //M3 update
    private View mLayoutUpgradeProgress;
    private ProgressBar mProgressBarDownload;
    private ProgressBar mProgressBarInstall;

    //share view
    private View shareLayout;
    private View shareBack;
    private Switch switchShare;
    private ImageView imgShareQR;
    private TextView tvShareCode;
    private TextView tvShareTips;
    private View shareImageContainer;
    private Switch switchShareNeedAuth;
    private Button shareBtn;

    //user manager view
    private View userMng;
    private View userMngLayout;
    private View userMngBack;
    //    private TextView userMngCheckAll;
    private RecyclerView lumRv;
    private DevUserMngRVAdapter userMngAdapter;
//    private View userMngDelete;

    //sn view
    private View snLayout;
    private View snBack;
    private View snSubmit;
    private Switch internetSwitch;
    private Switch dnsSwitch;
    private LinearLayout dnsLayout;
    private EditText dnsEdit1;
    private EditText dnsEdit2;
    private Switch subnetSwitch;
    private LinearLayout subnetLayout;
    private View addSubnet;

    //subnet view
    private View subLayout;

    //networks 所处网络
    private View locationNetworks;

    //sn开关
    private boolean snInternetEnable;
    private boolean snDnsEnable;
    private boolean snSubnetEnable;

    //M3 device view
    private View m3Layout;
    private View m3Space;
    private View m3SpaceBack;
    private View m3SpaceLayout;
    private AnimCircleProgressBar m3SpacePb;
    private TextView m3SpaceTvProgress;
    private TextView m3SpaceTvTotal;
    private TextView m3SpaceTvAviliable;
    private TextView m3SpaceTvUsed;
    private View m3SpaceIvHd;
    private View m3App;
    private View m3Reboot;
    private View m3Shutdown;

    private int dialogLevel;

    private boolean onBackPress() {
        return onBackPress(sv, devMngLayout, shareLayout, snLayout, subLayout, userMngLayout, m3SpaceLayout);
    }

    private boolean onBackPress(View visibleView, View visibleView2, View... goneViews) {
        if (dialogLevel <= 0 || detailDialog == null || !detailDialog.isShowing()) {
            dialogLevel = 0;
            return false;
        } else {
            if (dialogLevel == 3) {
                for (View view : goneViews) {
                    if (view != null)
                        view.setVisibility(View.GONE);
                }
                if (visibleView2 != null)
                    visibleView2.setVisibility(View.VISIBLE);
            } else if (dialogLevel == 2) {
                for (View view : goneViews) {
                    if (view != null)
                        view.setVisibility(View.GONE);
                }
                if (visibleView2 != null)
                    visibleView2.setVisibility(View.GONE);
                if (visibleView != null)
                    visibleView.setVisibility(View.VISIBLE);
            } else if (dialogLevel == 1) {
                dismiss();
            }
            dialogLevel--;
            return true;
        }
    }

    private View.OnClickListener listener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (Utils.isFastClick(v)) return;
            switch (v.getId()) {
                case R.id.device_des_btn_share:
                    sv.setVisibility(View.GONE);
                    shareLayout.setVisibility(View.VISIBLE);
                    dialogLevel++;
                    initShareViewDate();
                    break;
                case R.id.lsc_iv_back:
                    onBackPress();
                    break;
                case R.id.device_mng_des_btn_user_mng:
                    if (DevTypeHelper.isOneOSNas(bean.getDevClass()) && !bean.isDevDisable()) {
                        showNasUserManager(v.getContext());
                    } else {
                        sv.setVisibility(View.GONE);
                        userMngLayout.setVisibility(View.VISIBLE);
                        dialogLevel++;
                        initUserMngData();
                    }
                    break;
                case R.id.lum_iv_back:
                    onBackPress();
                    break;
//                case R.id.lum_btn_check_all:
//                    checkAllUsers();
//                    break;
//                case R.id.lum_btn_cancel_share:
//                    deleteUsers();
//                    break;
                case R.id.device_des_btn_sn:
                    devMngLayout.setVisibility(View.GONE);
                    snLayout.setVisibility(View.VISIBLE);
                    dialogLevel++;
                    initSnViewDate();
                    break;
                case R.id.lns_iv_back:
                    onBackPress();
                    break;
                case R.id.lns_btn_submit:
                    submitSnSetting();
                    break;
                case R.id.lns_btn_add_subnet:
                    addSubnetView(null);
                    break;
                case R.id.device_des_btn_subnet:
                    showSubnet(mView);
                    sv.setVisibility(View.GONE);
                    dialogLevel++;
                    break;
                case R.id.device_des_btn_get_score:
                    Locale curLocale = context.getResources().getConfiguration().locale;
                    String language = curLocale.getLanguage();
                    String script = curLocale.getScript();
                    String country = curLocale.getCountry();//"CN""TW"
                    String lang;
                    if ("zh".equals(language) &&
                            (!"cn".equals(country.toLowerCase()) || "hant".equals(script.toLowerCase()))) {
                        lang = "tw";
                    } else {
                        lang = language;
                    }
                    BaseInfo baseinfo = CMAPI.getInstance().getBaseInfo();
                    Intent i = new Intent(context, WebActivity.class);
                    i.putExtra("url", bean.getHardData().getGainmbp_url()
                            .replace("{0}", baseinfo.getTicket())
                            .replace("{1}", lang)
                            .replace("\\u0026", "&"));
                    i.putExtra("title", context.getString(R.string.receive_score));
                    i.putExtra("ConnectionState", true);
                    i.putExtra("enableScript", true);
                    i.putExtra("hasFullTitle", false);
                    i.putExtra("sllType", "app");
                    context.startActivity(i);
                    dismiss();
                    break;
                case R.id.device_des_btn_networks:
                    showNetworks(mView);
                    sv.setVisibility(View.GONE);
                    dialogLevel++;
                    break;
                case R.id.ls_iv_back:
                    onBackPress();
                    break;
                case R.id.device_mng_des_btn_space:
                    if (bean.isDevDisable()) {
                        ScoreHelper.showNeedMBPointDialog(context);
                    } else {
                        devMngLayout.setVisibility(View.GONE);
                        m3SpaceLayout.setVisibility(View.VISIBLE);
                        dialogLevel++;
                        initM3Space();
                    }
                    break;
                case R.id.lds_iv_hd:
                    onBackPress();
                    showHdView(v.getContext());
                    break;
                case R.id.device_mng_des_btn_app:
                    if (bean.isDevDisable()) {
                        ScoreHelper.showNeedMBPointDialog(context);
                    } else {
                        Intent intent = new Intent(context, NasAppsActivity.class);
                        intent.putExtra(AppConstants.SP_FIELD_DEVICE_ID, bean.getId());
                        context.startActivity(intent);
                        dismiss();
                    }
                    break;
                case R.id.device_mng_des_btn_reboot:
                    if (bean.isDevDisable()) {
                        ScoreHelper.showNeedMBPointDialog(context);
                    } else {
                        showPowerDialog(false);
                    }
                    break;
                case R.id.device_mng_des_btn_shutdown:
                    if (bean.isDevDisable()) {
                        ScoreHelper.showNeedMBPointDialog(context);
                    } else {
                        showPowerDialog(true);
                    }
                    break;
                case R.id.lds_iv_back:
                    onBackPress();
                    break;
                case R.id.device_mng_des_btn_delete:
                    deleteThisDevice();
                    break;
                case R.id.device_des_btn_flow:
                    Intent intent = new Intent(context, DevFlowDetailsActivity.class);
                    if (!TextUtils.isEmpty(bean.getId())) {
                        intent.putExtra("checkedDevId", bean.getId());
                    }
                    if (!TextUtils.isEmpty(bean.getName())) {
                        intent.putExtra("checkedDevName", bean.getName());
                    }
                    context.startActivity(intent);
                    dismiss();
                    break;
                case R.id.device_des_btn_mng:
                    sv.setVisibility(View.GONE);
                    devMngLayout.setVisibility(View.VISIBLE);
                    dialogLevel++;
                    break;
                case R.id.ldm_iv_back:
                    onBackPress();
                    break;
                case R.id.device_des_btn_torrents:
                    Intent intent2 = new Intent(context, TorrentActivity.class);
                    intent2.putExtra(AppConstants.SP_FIELD_DEVICE_ID, bean.getId());
                    context.startActivity(intent2);
                    dismiss();
                    break;
            }
        }
    };

    public void showDialog() {
        if (detailDialog != null && detailDialog.isShowing()) {
            return;
        }
        mView = LayoutInflater.from(context).inflate(R.layout.layout_dialog_device, null);
//        detailDialog = new AppCompatDialog(context, R.style.DialogTheme);
//        detailDialog.setContentView(mView);
        detailDialog = PopDialogFragment.newInstance(false, mView);
        detailDialog.show(mFragmentManager, DeviceDialogUtil.class.getSimpleName());
        initView(mView);

        dilName.setText(bean.getName());
        dilVip.setText(bean.getVip());
        dilOwner.setText(bean.getOwnerName());
        dilVersion.setText(bean.getAppVersion());
        dilDomain.setText(bean.getDomain());

        if (bean.getHardData() != null) {
            llExpand.setVisibility(View.VISIBLE);
            llExtrlDil.setVisibility(View.GONE);
            btnDelete.setVisibility(View.VISIBLE);
            btnDelete.setOnClickListener(listener);
            btnDevMng.setVisibility(View.VISIBLE);
            dilAddTime.setVisibility(View.VISIBLE);
            dilAddTime.setText(bean.getHardData().getDatetime());
            if (bean.getMnglevel() == 0 || bean.getMnglevel() == 1) {
//                dilId.setVisibility(View.VISIBLE);
                dilSn.setVisibility(View.VISIBLE);
//                dilId.setText(bean.getHardData().getDeviceid());
                dilSn.setText(bean.getHardData().getDevicesn());
            }
            if (!TextUtils.isEmpty(bean.getHardData().getGainmbp_url())) {
                btnGetScore.setVisibility(View.VISIBLE);
                btnGetScore.setOnClickListener(listener);
            } else {
                btnGetScore.setVisibility(View.GONE);
            }
            if (bean.getHardData().isEN() && bean.getMnglevel() == 0) {
                btnDevFlowMng.setVisibility(View.VISIBLE);
            }
        }


        //位置信息
        dilLocation.setVisibility(View.GONE);
        if (bean.getHardData() != null && !TextUtils.isEmpty(bean.getHardData().getLocation())) {
            dilLocation.setVisibility(View.VISIBLE);
            dilLocation.setText(bean.getHardData().getLocation());
        }

        if (bean.isOnline()) {
            //绑定的设备、管理员权限
            boolean isMngr = bean.getMnglevel() == 0 || bean.getMnglevel() == 1;
            boolean isSameAccount = isSameAccount();
            if (isMngr || isSameAccount) {
                String newVersion = (String) bean.getVersion();
                if (newVersion != null
                        && UiUtils.isNewVersion(MyConstants.REMOTE_MANAGER_VERSION, newVersion)
                        && UiUtils.isM8(bean.getDevClass())) {
                    remoteManage.setVisibility(View.VISIBLE);
                    SingleClickKt.singleClick(remoteManage, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (bean.isDevDisable()) {
                                ScoreHelper.showNeedMBPointDialog(context);
                            } else {
                                showRemoteManagement(v);
                            }
                        }
                    }, 300);
                }
                //lanIp
                dilLip.setVisibility(View.VISIBLE);
                dilLip.setText(bean.getPriIp());
                //节点设备
                if (bean.getType() == 0) {
                    //节点配置
                    initSnView(mView);
                }
            }
            if (isMngr) {
                if (bean.getMnglevel() == 0) {
                    dilName.mIv.setVisibility(View.VISIBLE);
                    dilName.mIv.setImageResource(R.drawable.icon_edit);
                    dilName.mIv.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            rename();
                        }
                    });
                }

                //非终端设备
                if (bean.getType() != 2) {
                    //分享
                    initShareView(mView);

                    //用户管理
                    initUserView(mView);
                }

                //M3设备
                if (bean.isNas()) {
                    final DeviceModel deviceModel = SessionManager.getInstance().getDeviceModel(bean.getId());
                    if (deviceModel != null) {
                        initM3View(mView);
                    }
                }
            }

            //节点设备
            if (bean.getType() == 0) {
                //子网
                List<Device.SubNet> subNets = bean.subNets;
                if (subNets != null && subNets.size() > 0) {
                    subnet.setVisibility(View.VISIBLE);
                    subnet.setOnClickListener(listener);
                } else {
                    subnet.setVisibility(View.GONE);
                }
                //节点设备,节点开关显示、节点选择的逻辑
                switchSnEnable.setVisibility(bean.getSelectable() ? View.VISIBLE : View.GONE);
                switchSnEnable.setChecked(CMAPI.getInstance().getBaseInfo().hadSelectedSn(bean.getId()) && bean.getSelectable());
                switchSnEnable.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if (isChecked && bean.isDevDisable()) {
                            buttonView.setChecked(false);
                            ScoreHelper.showNeedMBPointDialog(buttonView.getContext());
                            return;
                        }
                        Runnable runnable = new Runnable() {
                            @Override
                            public void run() {
                                onLoadStart(null);
                                buttonView.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        onLoadComplete();
                                    }
                                }, 3000);
                            }
                        };
                        if (CMAPI.getInstance().getBaseInfo().hadSelectedSn(bean.getId())) {
                            if (CMAPI.getInstance().getConfig().isNetBlock()
                                    && CMAPI.getInstance().getBaseInfo().getSnIds().size() == 1) {
                                new AlertDialog.Builder(context)
                                        .setMessage(R.string.tips_internet_access_sn_cancel)
                                        .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                runnable.run();
                                                if (CMAPI.getInstance().clearSmartNode()) {
                                                    switchSnEnable.setChecked(false);
                                                    dialog.dismiss();
                                                    DevManager.getInstance().notifyDeviceStateChanged();
                                                }
                                            }
                                        })
                                        .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                switchSnEnable.setChecked(true);
                                                dialog.dismiss();
                                            }
                                        })
                                        .show();
                            } else {
                                if (CMAPI.getInstance().clearSmartNode()) {
                                    switchSnEnable.setChecked(false);
                                    runnable.run();
                                }
                            }
                        } else {
                            //节点只许单选
                            if (CMAPI.getInstance().selectSmartNode(bean.getId())) {
                                switchSnEnable.setChecked(true);
                                runnable.run();
                            }
                        }
                        DevManager.getInstance().notifyDeviceStateChanged();
                    }
                });

            }

            if (bean.isNas()) {
                //nas设备
                mLayoutUpgradeProgress = mView.findViewById(R.id.layout_upgrade_progress);
                mProgressBarDownload = mView.findViewById(R.id.progressBar_download);
                mProgressBarInstall = mView.findViewById(R.id.progressBar_install);
                dilMarkName.mIv.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        editMarkName();
                    }
                });
                final DeviceModel deviceModel = SessionManager.getInstance().getDeviceModel(bean.getId());
                if (deviceModel != null) {
                    dilMarkName.setVisibility(View.VISIBLE);
                    dilMarkName.mIv.setVisibility(View.VISIBLE);
                    dilMarkName.mIv.setImageResource(R.drawable.icon_edit);
                    final Disposable subscribe = deviceModel.getDevNameFromDB().subscribe(new Consumer<String>() {
                        @Override
                        public void accept(String s) {
                            if (dilMarkName != null)
                                dilMarkName.setText(s);
                        }
                    });
                    addDisposable(subscribe);

                    boolean isM3 = DevTypeHelper.isOneOSNas(deviceModel.getDevClass());
                    if (deviceModel.isOwner() || deviceModel.isAdmin()) {
                        if (isM3) {
                            checkUpdate(deviceModel);
                        } else {
                            checkUpdateM8(deviceModel);
                        }
                    }
                    if (isM3) {
                        accessView.setVisibility(View.VISIBLE);
                    }
                    accessView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            SessionManager.getInstance().getLoginSession(deviceModel.getDevId(),
                                    new GetSessionListener(false) {

                                        @Override
                                        public void onSuccess(String url, LoginSession loginSession) {
                                            String uriString = OneOSAPIs.PREFIX_HTTP + bean.getDomain()
                                                    + String.format("?%s=%s", OneOSAPIs.SUFFIX_TOKEN,
                                                    loginSession.getSession());
                                            WebViewActivity.open(v.getContext(), "", uriString, false);
                                            dismiss();
                                        }

                                        @Override
                                        public void onFailure(String url, int errorNo, String errorMsg) {
                                            String uriString = OneOSAPIs.PREFIX_HTTP + bean.getDomain();
                                            WebViewActivity.open(v.getContext(), "", uriString, false);
                                            dismiss();
                                        }
                                    });

                        }
                    });
                    if (bean.getHardData() != null
                            && bean.getHardData().isEN()
                            && (bean.getHardData().getGb2cRatio() > 0 ||
                            EmptyUtils.isNotEmpty(bean.getHardData().getMbpointratio()))) {
                        String currentPrice = "";
                        if (bean.getHardData().getGb2cRatio() > 0) {
                            currentPrice = bean.getHardData().getGb2cRatio()
                                    + context.getString(R.string.fmt_traffic_unit_price2)
                                    .replace("$TRAFFIC$", MyConstants.DEFAULT_UNIT);
                        } else {
                            String mbpointratio = bean.getHardData().getMbpointratio();
                            currentPrice = context.getString(R.string.fmt_traffic_unit_price)
                                    .replace("$TRAFFIC$", mbpointratio);
                        }
                        dilTrafficPrice.setText(currentPrice);
                        dilTrafficPrice.setVisibility(View.VISIBLE);
                        dilTrafficPrice.mIv.setVisibility(bean.getHardData().isChangeRatioAble()
                                || TrafficPriceEditViewModelKt.isShouldShowTrafficTips(deviceModel.getDevId())
                                ? View.VISIBLE : View.GONE);
                        dilTrafficPrice.mIv.setImageResource(bean.getHardData().isOwner() ? R.drawable.icon_edit : R.drawable.icon_red_dot);
                        dilTrafficPrice.mIv.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (Utils.isFastClick(v)) {
                                    return;
                                }
                                showTrafficPriceEditView(deviceModel);
                            }
                        });
                    } else {
                        dilTrafficPrice.setVisibility(View.GONE);
                    }
                } else {
                    dilMarkName.setVisibility(View.GONE);
                    accessView.setVisibility(View.GONE);
                    dilTrafficPrice.setVisibility(View.GONE);
                }

            }

            View btnTorrents = mView.findViewById(R.id.device_des_btn_torrents);
            DeviceModel deviceModel = SessionManager.getInstance().getDeviceModel(bean.getId());
            boolean btServerAvailable = deviceModel != null && deviceModel.isBtServerAvailable();
            btnTorrents.setVisibility(BuildConfig.DEBUG && btServerAvailable ? View.VISIBLE : View.GONE);
            if (bean.isOnline()) {
                BTHelper.checkAvailable(bean.getVip(), booleanResult -> {
                    if (booleanResult.getStatus() == Status.SUCCESS) {
                        if (deviceModel != null)
                            deviceModel.setBtServerAvailable(true);
                        btnTorrents.setVisibility(BuildConfig.DEBUG ? View.VISIBLE : View.GONE);
                    }
                });
            }
            btnTorrents.setOnClickListener(listener);
        } else {
            dilVip.setVisibility(View.GONE);
            dilLip.setVisibility(View.GONE);
            dilVersion.setVisibility(View.GONE);
            dilDomain.setVisibility(View.GONE);
        }

        //当前网络
        List<String> networks = bean.getNetworks();
        if (networks != null && networks.size() > 0) {
            locationNetworks.setVisibility(View.VISIBLE);
            locationNetworks.setOnClickListener(listener);
        } else {
            locationNetworks.setVisibility(View.GONE);
        }

        sv.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                DisplayMetrics metrics = context.getResources().getDisplayMetrics();
                Dialog dialog = detailDialog.getDialog();
                if (dialog != null) {
                    final Window window = dialog.getWindow();
                    if (window != null) {
                        WindowManager.LayoutParams params = window.getAttributes();
                        params.width = (int) (metrics.widthPixels * 0.80);
                        window.setAttributes(params);
                        sv.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    }
                }
            }
        });
        dialogLevel = 1;
        detailDialog.addDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                dismiss();
            }
        });
    }

    private void showTrafficPriceEditView(DeviceModel bean) {
        ViewModelProviders.of(detailDialog.requireActivity()).get(TrafficPriceEditViewModel.class)
                .showEditView(context, bean.getDevId());
    }

    private void checkUpdateM8(DeviceModel deviceModel) {
        addDisposable(Single.create((SingleOnSubscribe<net.sdvn.cmapi.UpdateInfo>) emitter -> {
            net.sdvn.cmapi.UpdateInfo deviceUpdateInfo = CMAPI.getInstance().getDeviceUpdateInfo(deviceModel.getDevice().getVip());
            emitter.onSuccess(deviceUpdateInfo);
        }).subscribeOn(Schedulers.single())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(updateInfo -> {
                    if (updateInfo.getResult() == Constants.CE_SUCC
                            && UiUtils.isNewVersion(updateInfo.getVersion(), updateInfo.getNewVersion())) {
                        m3UpgradeBtn.setVisibility(View.VISIBLE);
                        m3UpgradeBtn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                ViewModelProviders.of(detailDialog.requireActivity()).get(M8CheckUpdateViewModel.class)
                                        .showDeviceItemUpgradeDetail(v.getContext(), new Pair<>(deviceModel, updateInfo));
                            }
                        });
                    }
                }, Timber::e));
    }

    private boolean isSameAccount() {
        return Objects.equals(bean.getUserId(), CMAPI.getInstance().getBaseInfo().getUserId());
    }

    private void showRemoteManagement(View v) {
        devMngLayout.setVisibility(View.GONE);
        dialogLevel++;
        subLayout = mView.findViewById(R.id.layout_subnet);
        subLayout.setVisibility(View.VISIBLE);
        View subBack = subLayout.findViewById(R.id.ls_iv_back);
        RecyclerView recyclerView = subLayout.findViewById(R.id.ls_rv);
        TextView tvTitle = subLayout.findViewById(R.id.ls_tv_title);
        final int[] status = {0};
        int title = R.string.view_remote_management;
        tvTitle.setText(title);
        DevicePrivateModel model = new DevicePrivateModel(bean.getVip());
        recyclerView.setLayoutManager(new LinearLayoutManager(context, RecyclerView.VERTICAL, false));
        recyclerView.setItemAnimator(null);
        List<DataItem> dataItems = new ArrayList<>();
        dataItems.add(new DataItem(R.string.switch_network));
        dataItems.add(new DataItem(R.string.select_smartnode));
        BaseQuickAdapter<DataItem, BaseViewHolder> adapter = new BaseQuickAdapter<DataItem, BaseViewHolder>
                (R.layout.item_text_btn) {
            @Override
            protected void convert(@NonNull BaseViewHolder baseViewHolder, DataItem dataItem) {
                baseViewHolder.setText(R.id.item_text, dataItem.strResId);
                baseViewHolder.addOnClickListener(R.id.item_text);
            }
        };
        adapter.setOnItemChildClickListener(new BaseQuickAdapter.OnItemChildClickListener() {
            @Override
            public void onItemChildClick(BaseQuickAdapter baseQuickAdapter, View view, int i) {
                DataItem o = (DataItem) baseQuickAdapter.getData().get(i);
                status[0] = o.strResId;
                switch (o.strResId) {
                    case R.string.switch_network:
                        showRemoteNetworks(recyclerView, tvTitle, model);
                        break;
                    case R.string.select_smartnode:
                        showRemoteSmartNodes(recyclerView, tvTitle, model);
                        break;
                }
            }
        });
        recyclerView.setAdapter(adapter);
        adapter.setNewData(dataItems);
        subBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (status[0] == 0) {
                    onBackPress();
                } else {
                    tvTitle.setText(title);
                    recyclerView.setAdapter(adapter);
                    status[0] = 0;
                }
            }
        });
    }

    public static class DataItem {
        int strResId;

        public DataItem(int strResId) {
            this.strResId = strResId;
        }
    }

    private void showRemoteSmartNodes(RecyclerView recyclerView, TextView tvTitle, DevicePrivateModel model) {
        tvTitle.setText(R.string.select_smartnode);
        BaseQuickAdapter<Device, BaseViewHolder> adapter = new BaseQuickAdapter<Device, BaseViewHolder>
                (R.layout.item_listview_choose_device) {

            @Override
            public void setNewData(@Nullable List<Device> data) {
                super.setNewData(data);
                if (data == null) {
                    this.mData = new ArrayList();
                } else {
                    this.mData = data;
                }
                this.notifyItemRangeChanged(0, getItemCount(), new ArrayList<Integer>(1));
            }

            @Override
            protected void convertPayloads(@NonNull BaseViewHolder helper, Device item, @NonNull List<Object> payloads) {
                convert(helper, item);
            }

            @Override
            protected void convert(@NonNull BaseViewHolder baseViewHolder, Device device) {
                baseViewHolder.setText(R.id.tv_device_name, device.getName());
                baseViewHolder.setText(R.id.tv_device_ip, device.getVip());
                CheckBox checkBox = baseViewHolder.getView(R.id.select_box);
                boolean selectedSn = model.getBaseInfo().hadSelectedSn(device.getId());
                checkBox.setChecked(selectedSn);
                ImageView iconView = baseViewHolder.getView(R.id.iv_device);
                if (baseViewHolder.itemView.getTag() != device.getId()) {
                    iconView.setTag(null);
                    baseViewHolder.itemView.setTag(device.getId());
                }
                int iconByeDevClass = DeviceBean.getIcon(new DeviceBean(device));
                if (iconView.getTag() == null) iconView.setImageResource(iconByeDevClass);
                LibApp.Companion.getInstance().getBriefDelegete().loadDeviceBrief(device.getId(),  BriefRepo.getBrief(device.getId(),BriefRepo.FOR_DEVICE),
                        iconView, null, 0, null, 0);
            }

        };
        View emptyView = LayoutInflater.from(recyclerView.getContext()).inflate(R.layout.pager_empty_text, null);
        ((TextView) emptyView.findViewById(R.id.tv_tips)).setText(R.string.tips_this_net_no_dev);
        adapter.setEmptyView(emptyView);
        adapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter baseQuickAdapter, View view, int i) {
                Device o = (Device) baseQuickAdapter.getData().get(i);

                addDisposable(Observable.create(new ObservableOnSubscribe<Result>() {
                    @Override
                    public void subscribe(ObservableEmitter<Result> emitter) throws Exception {
                        int code = model.selectSmartNode(o.getId());
                        if (code == Constants.CE_SUCC) {
                            emitter.onNext(new Result<String>(o.getId()));
                        } else {
                            emitter.onNext(new Result(code, ""));
                        }
                    }
                }).subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(result -> {
                            if (result.isSuccess()) {
                                baseQuickAdapter.notifyDataSetChanged();
                                ToastHelper.showLongToast(R.string.success);
                            } else {
                                ToastHelper.showLongToast(ErrorCode.error2String(result.code));
                            }

                        }));

            }
        });
        recyclerView.setAdapter(adapter);
        addDisposable(Observable.create(new ObservableOnSubscribe<Result>() {
            @Override
            public void subscribe(ObservableEmitter<Result> emitter) throws Exception {
                int baseInfoCode = model.refreshRemoteBaseInfo();
                int code = model.refreshRemoteDevices();
                if (baseInfoCode == Constants.CE_SUCC && code == Constants.CE_SUCC) {
                    List<Device> devices = model.getDevices();
                    if (devices != null) {
                        Iterator<Device> iterator = devices.iterator();
                        while (iterator.hasNext()) {
                            Device next = iterator.next();
                            if (!next.isOnline() || !next.getSelectable()) {
                                iterator.remove();
                            }
                        }
                    }
                    emitter.onNext(new Result<List<Device>>(devices));
                } else {
                    emitter.onNext(new Result(code, ""));
                }
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(result -> {
                    if (result.isSuccess()) {
                        adapter.setNewData((List<Device>) result.data);
                    } else {
                        ToastHelper.showLongToast(ErrorCode.error2String(result.code));
                    }

                }));
    }

    private void showRemoteNetworks(RecyclerView recyclerView, TextView tvTitle, DevicePrivateModel model) {
        tvTitle.setText(R.string.switch_network);
        HomeNetRVAdapter adapter = new HomeNetRVAdapter(null);
        recyclerView.setAdapter(adapter);

        adapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter baseQuickAdapter, View view, int i) {
                Network o = (Network) baseQuickAdapter.getData().get(i);
                addDisposable(Observable.create(new ObservableOnSubscribe<Result>() {
                    @Override
                    public void subscribe(ObservableEmitter<Result> emitter) throws Exception {
                        int code = model.switchNetwork(o.getId());
                        if (code == Constants.CE_SUCC || code == Constants.CE_PENDING) {
                            emitter.onNext(new Result<String>(o.getId()));
                        } else {
                            emitter.onNext(new Result(code, ""));
                        }
                    }
                }).subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(result -> {
                            if (result.isSuccess()) {
                                adapter.setNewData(model.getNetworks());
                                ToastHelper.showLongToast(R.string.switch_success);
                            } else {
                                ToastHelper.showLongToast(ErrorCode.error2String(result.code));
                            }

                        }));

            }
        });
        addDisposable(Observable.create(new ObservableOnSubscribe<Result>() {
            @Override
            public void subscribe(ObservableEmitter<Result> emitter) throws Exception {
                int code = model.refreshNetworks();
                if (code == Constants.CE_SUCC) {
                    emitter.onNext(new Result<List<Network>>(model.getNetworks()));
                } else {
                    emitter.onNext(new Result(code, ""));
                }
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(result -> {
                    if (result.isSuccess()) {
                        adapter.setNewData((List<Network>) result.data);
                    } else {
                        ToastHelper.showLongToast(ErrorCode.error2String(result.code));
                    }

                }));
    }

    public void dismiss() {
        if (mM3UpdateViewModel != null) {
            mM3UpdateViewModel.onCleared();
            mM3UpdateViewModel = null;
        }
        if (detailDialog != null) {
            detailDialog.dismiss();
        }
        dispose();
    }

    private void initView(View view) {
        sv = view.findViewById(R.id.device_des_sv);
        switchSnEnable = view.findViewById(R.id.switch_sn_enable);
        loadingView = view.findViewById(R.id.layout_loading);
        accessView = view.findViewById(R.id.device_des_btn_by_browser);
        m3UpgradeBtn = view.findViewById(R.id.device_des_upgrade);
        locationNetworks = view.findViewById(R.id.device_des_btn_networks);
        subnet = view.findViewById(R.id.device_des_btn_subnet);
        btnGetScore = view.findViewById(R.id.device_des_btn_get_score);
        btnDevFlowMng = view.findViewById(R.id.device_des_btn_flow);
        btnDevMng = view.findViewById(R.id.device_des_btn_mng);
        devMngLayout = view.findViewById(R.id.layout_dev_mng);
        remoteManage = view.findViewById(R.id.device_des_btn_remote_manage);
        btnDelete = view.findViewById(R.id.device_mng_des_btn_delete);

        btnDevFlowMng.setVisibility(View.GONE);
        btnDevFlowMng.setOnClickListener(listener);
        btnDevMng.setVisibility(View.GONE);
        btnDevMng.setOnClickListener(listener);
        devMngBack = view.findViewById(R.id.ldm_iv_back);
        devMngBack.setOnClickListener(listener);

        view.findViewById(R.id.device_des_iv_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        dilName = view.findViewById(R.id.des_dil_name);
        dilMarkName = view.findViewById(R.id.des_dil_mark_name);
        dilVip = view.findViewById(R.id.des_dil_vip);
        dilLip = view.findViewById(R.id.des_dil_lip);
        dilOwner = view.findViewById(R.id.des_dil_owner);
        dilTrafficPrice = view.findViewById(R.id.des_dil_unit_price);
        dilVersion = view.findViewById(R.id.des_dil_version);
        dilDomain = view.findViewById(R.id.des_dil_domain);
        dilLocation = view.findViewById(R.id.des_dil_location);
        dilAddTime = view.findViewById(R.id.device_mng_dil_add_time);
//        dilId = view.findViewById(R.id.device_mng_dil_id);
        dilSn = view.findViewById(R.id.device_mng_dil_sn);
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
                case R.id.des_dil_name:
                    clipString(dilName.mTvData.getText().toString().trim());
                    break;
                case R.id.des_dil_mark_name:
                    clipString(dilMarkName.mTvData.getText().toString().trim());
                    break;
                case R.id.des_dil_unit_price:
                    clipString(dilTrafficPrice.mTvData.getText().toString().trim());
                    break;
                case R.id.des_dil_vip:
                    clipString(dilVip.mTvData.getText().toString().trim());
                    break;
                case R.id.des_dil_lip:
                    clipString(dilLip.mTvData.getText().toString().trim());
                    break;
                case R.id.des_dil_owner:
                    clipString(dilOwner.mTvData.getText().toString().trim());
                    break;
                case R.id.des_dil_version:
                    clipString(dilVersion.mTvData.getText().toString().trim());
                    break;
                case R.id.des_dil_domain:
                    clipString(dilDomain.mTvData.getText().toString().trim());
                    break;
                case R.id.des_dil_location:
                    clipString(dilLocation.mTvData.getText().toString().trim());
                    break;
                case R.id.device_mng_dil_add_time:
                    clipString(dilAddTime.mTvData.getText().toString().trim());
                    break;
                case R.id.device_mng_dil_id:
//                    clipString(dilId.mTvData.getText().toString().trim());
                    break;
                case R.id.device_mng_dil_sn:
                    clipString(dilSn.mTvData.getText().toString().trim());
                    break;
                case R.id.des_ll_expand:
                    devInfoExpand();
                    break;
            }
        };
        dilName.setDataOnClickListener(listener);
        dilMarkName.setDataOnClickListener(listener);
        dilTrafficPrice.setDataOnClickListener(listener);
        dilVip.setDataOnClickListener(listener);
        dilLip.setDataOnClickListener(listener);
        dilOwner.setDataOnClickListener(listener);
        dilVersion.setDataOnClickListener(listener);
        dilDomain.setDataOnClickListener(listener);
        dilLocation.setDataOnClickListener(listener);
        dilAddTime.setDataOnClickListener(listener);
//        dilId.setDataOnClickListener(listener);
        dilSn.setDataOnClickListener(listener);

        llExtrlDil = view.findViewById(R.id.des_ll_extrl_dil);
        llExpand = view.findViewById(R.id.des_ll_expand);
        tvExpand = view.findViewById(R.id.des_tv_expand);
        ivExpand = view.findViewById(R.id.des_iv_expand);
        llExpand.setOnClickListener(listener);
        llExpand.setVisibility(View.GONE);
        llExtrlDil.setVisibility(View.VISIBLE);
    }

    private void clipString(String content) {
        if (context != null) {
            ClipboardUtils.copyToClipboard(context, content);
            ToastUtils.showToast(context.getString(R.string.Copied) + content);
        }
    }

    private void devInfoExpand() {
        if (llExtrlDil.getVisibility() == View.VISIBLE) {
            llExtrlDil.setVisibility(View.GONE);
            tvExpand.setText(R.string.expand);
            ivExpand.setRotation(0);
        } else {
            llExtrlDil.setVisibility(View.VISIBLE);
            tvExpand.setText(R.string.shrink);
            ivExpand.setRotation(180);
        }
    }

    private void rename() {
        DialogUtil.showEditDialog(context, context.getString(R.string.rename), bean.getName(), "",
                context.getString(R.string.confirm), new DialogUtil.OnDialogButtonClickListener() {

                    @Override
                    public void onClick(View v, final String strEdit, final Dialog dialog, boolean isCheck) {
                        if (TextUtils.isEmpty(strEdit)) {
                            AnimUtils.sharkEditText(context, v);
                        } else {
                            SetDeviceNameHttpLoader loader = new SetDeviceNameHttpLoader(GsonBaseProtocol.class);
                            loader.setHttpLoaderStateListener(DeviceDialogUtil.this);
                            loader.setParams(bean.getId(), strEdit);
                            loader.executor(new MyOkHttpListener() {
                                @Override
                                public void success(Object tag, GsonBaseProtocol gsonBaseProtocol) {
                                    bean.setName(strEdit);
                                    DevManager.getInstance().notifyDeviceStateChanged();
                                    dilName.setText(strEdit);
                                    dialog.dismiss();
                                }
                            });
                        }
                    }
                },
                context.getString(R.string.cancel), null);
    }

    private void editMarkName() {
        if ((!bean.isOnline())) {
            ToastUtils.showToast(R.string.device_offline);
            return;
        }
        ViewModelProviders.of(detailDialog.requireActivity()).get(DeviceViewModel.class)
                .showDeviceName(context, bean.getId(), dilMarkName.mTvData.getText().toString().trim(),
                        newName -> {
                            dilMarkName.setText(newName);
                            DevManager.getInstance().notifyDeviceStateChanged();
                        });
    }

    private void initShareView(View view) {
        share = view.findViewById(R.id.device_des_btn_share);
        shareLayout = view.findViewById(R.id.layout_share_code);
        shareBack = view.findViewById(R.id.lsc_iv_back);
        switchShare = view.findViewById(R.id.lsc_switch_share);
        shareImageContainer = view.findViewById(R.id.lsc_container);
        imgShareQR = view.findViewById(R.id.lsc_iv_qr);
        tvShareCode = view.findViewById(R.id.lsc_tv_share_code);
        tvShareTips = view.findViewById(R.id.lsc_tv_tips);
        switchShareNeedAuth = view.findViewById(R.id.lsc_switch_share_need_Auth);
        shareBtn = view.findViewById(R.id.lsc_btn_share);


        share.setVisibility(View.VISIBLE);
        share.setOnClickListener(listener);
        shareBack.setOnClickListener(listener);

        switchShare.setChecked(bean.getHardData() != null && bean.getHardData().getEnableshare());
        switchShareNeedAuth.setChecked(bean.getHardData() != null && bean.getHardData().isScanconfirm());
        refreshShareView(bean.getHardData() != null && bean.getHardData().getEnableshare());

        switchShare.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, final boolean isChecked) {
                ShareUtil.savedEnableShareState(bean.getId(), isChecked,
                        DeviceDialogUtil.this, new ResultListener() {
                            @Override
                            public void success(Object tag, GsonBaseProtocol data) {
                                if (bean.getHardData() != null)
                                    bean.getHardData().setEnableshare(isChecked);
                                refreshShareView(isChecked);
                                initShareViewDate();
                            }

                            @Override
                            public void error(Object tag, GsonBaseProtocol baseProtocol) {
                                switchShare.setChecked(bean.getHardData() != null && bean.getHardData().getEnableshare());
                            }
                        });
            }
        });
        switchShareNeedAuth.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, final boolean isChecked) {
                ShareUtil.savedScanConfirmState(bean.getId(), isChecked,
                        DeviceDialogUtil.this, new ResultListener() {
                            @Override
                            public void success(Object tag, GsonBaseProtocol data) {
                                if (bean.getHardData() != null)
                                    bean.getHardData().setScanconfirm(isChecked);
                            }

                            @Override
                            public void error(Object tag, GsonBaseProtocol baseProtocol) {
                                switchShareNeedAuth.setChecked(bean.getHardData() != null && bean.getHardData().isScanconfirm());
                            }
                        });
            }
        });
        shareBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //保存二维码到本地
                ShareUtil.saveAndShareImg(shareImageContainer, shareCode, null);
            }
        });
    }

    private String shareCode;

    private void initShareViewDate() {
        if (bean.getHardData() != null && bean.getHardData().getEnableshare()) {
            //获取分享吗
            ShareUtil.getDeviceShareCode(bean.getId(), this,
                    new MyOkHttpListener<ShareCode>() {
                        @Override
                        public void success(Object tag, final ShareCode data) {
                            //生成二维码
                            shareCode = data.sharecode;
                            ShareUtil.generateQRCode(imgShareQR, MyConstants.EVENT_CODE_HARDWAER_DEVICE, shareCode,
                                    new ShareUtil.QRCodeResult() {
                                        @Override
                                        public void onGenerated(Bitmap bitmap, String tips) {
                                            imgShareQR.setImageBitmap(bitmap);
                                            tvShareCode.setText(shareCode);
                                            tvShareTips.setText(tips);
                                            MessageManager.getInstance().quickDelay();
                                        }
                                    });
                        }
                    });
        }
    }

    private void refreshShareView(boolean shareable) {
        if (shareable) {
            shareImageContainer.setVisibility(View.VISIBLE);
            switchShareNeedAuth.setVisibility(View.VISIBLE);
            shareBtn.setEnabled(true);
        } else {
            shareImageContainer.setVisibility(View.GONE);
            switchShareNeedAuth.setVisibility(View.GONE);
            shareBtn.setEnabled(false);
        }
    }

    private void initUserView(View view) {
        userMng = view.findViewById(R.id.device_mng_des_btn_user_mng);
        userMngLayout = view.findViewById(R.id.layout_user_mng);
        userMngBack = view.findViewById(R.id.lum_iv_back);
//        userMngCheckAll = view.findViewById(R.id.lum_btn_check_all);
        lumRv = view.findViewById(R.id.lum_rv);
//        userMngDelete = view.findViewById(R.id.lum_btn_cancel_share);

        userMng.setVisibility(View.VISIBLE);
        userMng.setOnClickListener(listener);
        userMngBack.setOnClickListener(listener);
//        userMngCheckAll.setOnClickListener(listener);
//        userMngDelete.setOnClickListener(listener);
        initUserMngLayout(context);
    }

    private void initUserMngLayout(Context context) {
        userMngAdapter = new DevUserMngRVAdapter();
        lumRv.setLayoutManager(new LinearLayoutManager(context, RecyclerView.VERTICAL, false));
        lumRv.setItemAnimator(null);
        lumRv.setAdapter(userMngAdapter);
        userMngAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter baseQuickAdapter, View view, int i) {
                ShareUser user = (ShareUser) baseQuickAdapter.getData().get(i);
                showUserMngDialog(user);
            }
        });
    }

    private void showNasUserManager(Context context) {
        Intent intent = new Intent(context, UserManageActivity.class);
        intent.putExtra(AppConstants.SP_FIELD_DEVICE_ID, bean.getId());
        context.startActivity(intent);
        dismiss();
    }

    private void initUserMngData() {
        DeviceUserUtil.shareUsers(bean.getId(), DeviceDialogUtil.this,
                new MyOkHttpListener<SharedUserList>() {
                    @Override
                    public void success(Object tag, SharedUserList data) {
//                        isCheckAll = false;
//                        userMngCheckAll.setText(R.string.check_all);
                        Collections.sort(data.users, new Comparator<ShareUser>() {
                            @Override
                            public int compare(ShareUser o1, ShareUser o2) {
                                if (o1.mgrlevel != o2.mgrlevel)
                                    return o1.mgrlevel - o2.mgrlevel;
                                return o1.datetime.compareTo(o2.datetime);
                            }
                        });
                        userMngAdapter.setNewData(data.users);
                    }
                });
    }

    private void showUserMngDialog(ShareUser user) {
        View view = LayoutInflater.from(context).inflate(R.layout.layout_dialog_nas_user_manage, null);
        DataItemLayout dataItemAccount = view.findViewById(R.id.nmg_des_dil_account);
        DataItemLayout dataItemName = view.findViewById(R.id.nmg_des_dil_name);
        DataItemLayout dataItemSpace = view.findViewById(R.id.nmg_des_dil_space);
        View tvTransOwner = view.findViewById(R.id.nmg_des_btn_transfer_owner);
        TextView tvMdfLevel = view.findViewById(R.id.nmg_des_btn_up);
        View tvDelete = view.findViewById(R.id.nmg_des_btn_delete);
        View ivBack = view.findViewById(R.id.iv_back);
        Dialog dialog = new Dialog(context, R.style.DialogTheme);
        dialog.setContentView(view);
        dialog.setCancelable(false);
        dialog.show();
        dataItemAccount.setText(user.username);
        dataItemName.setVisibility(View.GONE);
        dataItemSpace.setVisibility(View.GONE);
        if (bean.getMnglevel() == 0
                && !CMAPI.getInstance().getBaseInfo().getUserId().equals(user.userid)) {
            //如果是所有者，并且选的不是自己
            tvMdfLevel.setVisibility(View.VISIBLE);
            if (user.mgrlevel == 1) {
                tvMdfLevel.setText(context.getString(R.string.Downgrad_to_a_common_user));
            }
            tvMdfLevel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (user.mgrlevel == 1) {
                        showModifyUserLevelDialog(user, 2);
                    } else {
                        showModifyUserLevelDialog(user, 1);
                    }
                    dialog.dismiss();
                }
            });
            tvTransOwner.setVisibility(View.VISIBLE);
            tvTransOwner.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showModifyUserLevelDialog(user, 0);
                    dialog.dismiss();
                }
            });
        }
        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        tvDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteUser(user);
                dialog.dismiss();
            }
        });
    }

    private void showModifyUserLevelDialog(ShareUser user, int mgrlevel) {
        int titleResId;
        switch (mgrlevel) {
            case 0:
                titleResId = R.string.transfer_ownership_of_this_device;
                break;
            case 1:
                titleResId = R.string.upgrade_to_administrator;
                break;
            default:
                titleResId = R.string.Downgrad_to_a_common_user;
                break;
        }
        DialogUtils.showWarningDialog(context,
                titleResId,
                -1,
                R.string.confirm, R.string.cancel, new DialogUtils.OnDialogClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, @NonNull boolean isPositiveBtn) {
                        dialog.dismiss();
                        if (isPositiveBtn) {
                            modifyUserLevel(user, mgrlevel);
                        }
                    }
                });
    }

    private void modifyUserLevel(ShareUser user, int mgrlevel) {
        GradeBindDeviceHttpLoader loader = new GradeBindDeviceHttpLoader(GsonBaseProtocol.class);
        loader.setParams(user.userid, bean.getId(), mgrlevel);
        loader.executor(new MyOkHttpListener<GsonBaseProtocol>() {
            @Override
            public void success(Object tag, GsonBaseProtocol data) {
                ToastHelper.showToast(R.string.success);
                initUserMngData();
                if (mgrlevel == 0) {
                    dismiss();
                    DevManager.getInstance().initHardWareList(null);
                }
            }
        });
    }

    private void deleteUser(ShareUser user) {
        String curUid = CMAPI.getInstance().getBaseInfo().getUserId();
        if (bean.getMnglevel() == 0) {
            if (curUid.equals(user.userid)) { //所有者删除自己，要先转移所有者权限
                DialogUtils.showNotifyDialog(context,
                        R.string.permission_denied, R.string.tip_please_change_admin, R.string.ok, null);
            } else { //所有者删除其他用户
                showDeleteUserDialog(user);
            }
        } else if (curUid.equals(user.userid)) { //用户删除自己账号
            showDeleteUserDialog(user);
        } else if (bean.getMnglevel() == 1 && user.mgrlevel != 0 && user.mgrlevel != 1) { //管理员删除其他用户
            showDeleteUserDialog(user);
        } else { //非所有者用户删除其他普通用户
            DialogUtils.showNotifyDialog(context,
                    R.string.permission_denied, R.string.please_login_onespace_with_admin, R.string.ok, null);
        }
    }

    private void showDeleteUserDialog(ShareUser user) {
        DialogUtils.showWarningDialog(context,
                R.string.delete_user,
                R.string.warning_delete_user,
                R.string.delete, R.string.cancel, new DialogUtils.OnDialogClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, @NonNull boolean isPositiveBtn) {
                        dialog.dismiss();
                        if (isPositiveBtn) {
                            if (!TextUtils.isEmpty(user.userid)) {
                                unbind(user.userid);
                            }
                        }
                    }
                });
    }

    private void unbind(String userId) {
        //用户解绑
        UnbindDeviceHttpLoader unbindDeviceHttpLoader = new UnbindDeviceHttpLoader();

        unbindDeviceHttpLoader.unbindSingle(bean.getId(), userId, new ResultListener<UnbindDeviceResult>() {
            @Override
            public void success(Object tag, UnbindDeviceResult data) {
                String curUid = CMAPI.getInstance().getBaseInfo().getUserId();
                ToastHelper.showToast(R.string.remove_success);
                if (curUid.equals(userId)) {
                    dismiss();
                    DevManager.getInstance().initHardWareList(null);
                }
            }

            @Override
            public void error(Object tag, GsonBaseProtocol baseProtocol) {
                ToastHelper.showToast(R.string.remove_device_failed);
            }
        });
    }

//    private boolean isCheckAll;

//    private void checkAllUsers() {
//        isCheckAll = !isCheckAll;
//        if (isCheckAll) {
//            boolean isChanged = false;
//            for (ShareUser user : userMngAdapter.getData()) {
//                if (user.mgrlevel != 0 && user.mgrlevel != 1) {
//                    user.isSelected = true;
//                    isChanged = true;
//                }
//            }
//            if (isChanged) {
//                userMngCheckAll.setText(R.string.cancel);
//            }
//        } else {
//            for (ShareUser user : userMngAdapter.getData()) {
//                if (user.mgrlevel != 0 && user.mgrlevel != 1)
//                    user.isSelected = false;
//            }
//            userMngCheckAll.setText(R.string.check_all);
//        }
//        userMngAdapter.notifyDataSetChanged();
//    }

//    private void deleteUsers() {
//        final List<String> userids = new ArrayList<>();
//        for (ShareUser user : userMngAdapter.getData()) {
//            if (user.isSelected)
//                userids.add(user.userid);
//        }
//        if (userids.size() > 0)
//            DialogUtil.showSelectDialog(context, context.getString(R.string.cancel_share_to_these_users),
//                    context.getString(R.string.yes), new DialogUtil.OnDialogButtonClickListener() {
//                        @Override
//                        public void onClick(View v, String strEdit, final Dialog dialog, boolean isCheck) {
//                            dialog.dismiss();
//                            DeviceUserUtil.unbindDevice(bean.getDeviceId(), userids, DeviceDialogUtil.this,
//                                    new ListResultListener<UnbindDeviceResult>() {
//                                        @Override
//                                        public void success(Object tag, List<UnbindDeviceResult> results) {
//                                            initUserMngData();
//                                        }
//
//                                        @Override
//                                        public void error(Object tag, List<UnbindDeviceResult> mErrorResults) {
//
//                                        }
//
//                                        @Override
//                                        public void error(Object tag, GsonBaseProtocol baseProtocol) {
//                                            ToastUtils.showError(baseProtocol.result);
//                                        }
//                                    });
//                        }
//                    },
//                    context.getString(R.string.no), null);
//    }

    private void initSnView(final View view) {
        snLayout = view.findViewById(R.id.layout_node_setting);
        snBack = view.findViewById(R.id.lns_iv_back);
        snSetting = view.findViewById(R.id.device_des_btn_sn);
        snSubmit = view.findViewById(R.id.lns_btn_submit);
        internetSwitch = view.findViewById(R.id.lns_switch_access_internet);
        dnsSwitch = view.findViewById(R.id.lns_switch_dns);
        dnsLayout = view.findViewById(R.id.lns_layout_dns);
        dnsEdit1 = view.findViewById(R.id.lns_edit_dns_1);
        dnsEdit2 = view.findViewById(R.id.lns_edit_dns_2);
        subnetSwitch = view.findViewById(R.id.lns_switch_subnet);
        subnetLayout = view.findViewById(R.id.lns_layout_subnet);
        addSubnet = view.findViewById(R.id.lns_btn_add_subnet);

        snSetting.setVisibility(View.VISIBLE);
        snSetting.setOnClickListener(listener);
        snBack.setOnClickListener(listener);
        addSubnet.setOnClickListener(listener);
        snSubmit.setOnClickListener(listener);


        dnsEdit1.setOnFocusChangeListener(focusChangeListener);
        dnsEdit2.setOnFocusChangeListener(focusChangeListener);

        refreshSNView();
        internetSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                refreshSNView();
            }
        });
        dnsSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                refreshSNView();
            }
        });
        subnetSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                refreshSNView();
            }
        });
    }

    private List<SubnetEntity> mSubnets = new ArrayList<>();

    private void initSnViewDate() {
        snInternetEnable = SmartNodeUtil.isAccessInternet(bean);
        snDnsEnable = false;
        snSubnetEnable = SmartNodeUtil.isAccessSubnet(bean);

        String mDns = bean.getDns();
        if (!TextUtils.isEmpty(mDns)) {
            String[] split = mDns.split(",");
            if (split.length > 0) {
                dnsEdit1.setText(split[0]);
                if (snInternetEnable) {
                    dnsLayout.setVisibility(View.VISIBLE);
                    snDnsEnable = true;
                }
            }
            if (split.length > 1)
                dnsEdit2.setText(split[1]);
        }

        internetSwitch.setChecked(snInternetEnable);
        dnsSwitch.setChecked(snDnsEnable);
        subnetSwitch.setChecked(snSubnetEnable);

        SmartNodeUtil.getSubnet(bean, this,
                new MyOkHttpListener<SubnetList>() {
                    @Override
                    public void success(Object tag, final SubnetList subnetList) {
                        if (subnetList != null) {
                            while (subnetLayout.getChildCount() > 1) {
                                subnetLayout.removeViewAt(0);
                            }
                            mSubnets.clear();
                            mSubnets.addAll(subnetList.getSubnet());
                            if (mSubnets != null) {
                                if (mSubnets.size() > 0) {
                                    for (SubnetEntity entity : mSubnets) {
                                        if (!TextUtils.isEmpty(entity.getNet()) ||
                                                !TextUtils.isEmpty(entity.getMask())) {
                                            addSubnetView(entity);
                                        }
                                    }
                                } else {
                                    addSubnetView(null);
                                }
                            }
                        }
                    }

                    @Override
                    public void error(Object tag, GsonBaseProtocol baseProtocol) {
                        super.error(tag, baseProtocol);
                        onBackPress();
                    }
                });
    }

    private void addSubnetView(SubnetEntity entity) {
        final SubnetLayout subnet = new SubnetLayout(context);
        if (entity != null) {
            subnet.getEtIp().setText(entity.getNet());
            subnet.getEtNetmask().setText(entity.getMask());
        }
        subnet.setRemoveListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (subnetLayout.getChildCount() == 2) {
                    SubnetLayout child = (SubnetLayout) subnetLayout.getChildAt(0);
                    child.getEtIp().setText("");
                    child.getEtNetmask().setText("");

                    child.getEtIp().setOnFocusChangeListener(focusChangeListener);
                    child.getEtNetmask().setOnFocusChangeListener(focusChangeListener);
                } else {
                    subnetLayout.removeView(subnet);
                }
            }
        });
        subnetLayout.addView(subnet, subnetLayout.getChildCount() - 1);
    }

    private void submitSnSetting() {
        final boolean accessInternet = internetSwitch.isChecked();
        final boolean accessSubnet = subnetSwitch.isChecked();
        final boolean isUsedDns = dnsSwitch.isChecked();
        final StringBuffer dnsBuff = new StringBuffer();
        if (accessInternet && isUsedDns) {
            String dns1 = dnsEdit1.getText().toString().trim();
            String dns2 = dnsEdit2.getText().toString().trim();
            if (!TextUtils.isEmpty(dns1) || !TextUtils.isEmpty(dns2)) {
                String dnsRegex = "^(((\\d{1,2})|(1\\d{2})|(2[0-4]\\d)|(25[0-5]))\\.){3}((\\d{1,2})|(1\\d{2})|(2[0-4]\\d)|(25[0-5]))$";
                if (dns1.matches(dnsRegex)) {
                    dnsBuff.append(dns1);
                } else {
                    ToastUtils.showToast(R.string.pls_enter_right_dns);
                    dnsEdit1.requestFocus();
                    return;
                }
                if (dns2.matches(dnsRegex)) {
                    dnsBuff.append(",").append(dns2);
                } else if (!TextUtils.isEmpty(dns2)) {
                    ToastUtils.showToast(R.string.pls_enter_right_dns);
                    dnsEdit2.requestFocus();
                    return;
                }
            }
        }
        final List<SubnetEntity> subnet = new ArrayList<>();
        if (accessSubnet) {
            for (int i = 0; i < subnetLayout.getChildCount() - 1; i++) {
                SubnetLayout child = (SubnetLayout) subnetLayout.getChildAt(i);
                if (TextUtils.isEmpty(child.getEtIp().getText().toString().trim()) &&
                        TextUtils.isEmpty(child.getEtNetmask().getText().toString().trim())) {
                    continue;
                }
                String ip = child.getIp();
                if (ip == null) {
                    child.getEtIp().requestFocus();
                    ToastUtils.showToast(R.string.pls_enter_right_subnet_ip);
                    return;
                }
                child.getEtIp().setText(ip);
                String netmask = child.getNetmask();
                if (netmask == null) {
                    child.getEtNetmask().requestFocus();
                    ToastUtils.showToast(R.string.pls_enter_right_subnet_mask);
                    return;
                }
                child.getEtNetmask().setText(netmask);
                SubnetEntity entity = new SubnetEntity(ip, netmask);
                if (!subnet.contains(entity)) {
                    subnet.add(entity);
                } else {
                    child.getEtNetmask().requestFocus();
                    ToastUtils.showToast(R.string.repeated_subnet);
                    return;
                }
            }
            if (subnet.size() == 0) {
                ToastUtils.showToast(R.string.pls_configure_at_least_one_subnet);
                return;
            }
        }
        if (snInternetEnable != accessInternet || snSubnetEnable != accessSubnet) {
            SmartNodeUtil.submitAccessFlag(bean, accessInternet, accessSubnet, this, new MyOkHttpListener() {
                @Override
                public void success(Object tag, GsonBaseProtocol data) {
                    commitDnsAndSubnet(true, accessInternet, accessSubnet, isUsedDns, dnsBuff.toString(), subnet);
                }
            });
        } else {
            commitDnsAndSubnet(false, accessInternet, accessSubnet, isUsedDns, dnsBuff.toString(), subnet);
        }
    }

    boolean submitDns;
    boolean submitSubnet;

    private void commitDnsAndSubnet(boolean isSubmitFlag, final boolean accessInternet, final boolean accessSubnet, boolean isUsedDns, String dns, List<SubnetEntity> subnet) {
        boolean isCommit = false;
        submitDns = false;
        submitSubnet = false;
        if (accessInternet) {
            if (!bean.getDns().equals(dns)) {
                SmartNodeUtil.submitDns(bean, dns, this, new MyOkHttpListener() {
                    @Override
                    public void success(Object tag, GsonBaseProtocol data) {
                        submitDns = true;
                        if (!accessSubnet || submitSubnet)
                            dismiss();
                    }
                });
                isCommit = true;
            } else {
                submitDns = true;
            }
        }
        if (accessSubnet) {
            if (!mSubnets.containsAll(subnet) || !subnet.containsAll(mSubnets)) {
                SmartNodeUtil.submitSubnet(bean, subnet, this, new MyOkHttpListener() {
                    @Override
                    public void success(Object tag, GsonBaseProtocol data) {
                        submitSubnet = true;
                        if (!accessInternet || submitDns)
                            dismiss();
                    }
                });
                isCommit = true;
            } else {
                submitSubnet = true;
            }
        }
        if (!isCommit) {
            if (isSubmitFlag)
                dismiss();
            else
                onBackPress();
        }
    }

    private void refreshSNView() {
        internetSwitch.setVisibility(View.VISIBLE);
        dnsSwitch.setVisibility(View.GONE);
        dnsLayout.setVisibility(View.GONE);
        subnetSwitch.setVisibility(View.VISIBLE);
        subnetLayout.setVisibility(View.GONE);
        if (internetSwitch.isChecked()) {
            dnsSwitch.setVisibility(View.VISIBLE);
            if (dnsSwitch.isChecked()) {
                dnsLayout.setVisibility(View.VISIBLE);
            }
        }
        if (subnetSwitch.isChecked()) {
            subnetLayout.setVisibility(View.VISIBLE);
        }
    }

    private void showSubnet(final View view) {
        subLayout = view.findViewById(R.id.layout_subnet);
        subLayout.setVisibility(View.VISIBLE);
        View subBack = subLayout.findViewById(R.id.ls_iv_back);
        RecyclerView subRv = subLayout.findViewById(R.id.ls_rv);
        TextView tvTitle = subLayout.findViewById(R.id.ls_tv_title);
        tvTitle.setText(R.string.subnet);
        subBack.setOnClickListener(listener);
        SubnetRVAdapter adapter = new SubnetRVAdapter();
        subRv.setLayoutManager(new LinearLayoutManager(context, RecyclerView.VERTICAL, false));
        subRv.setItemAnimator(null);
        subRv.setAdapter(adapter);
        List<Device.SubNet> subNets = new ArrayList<>(bean.subNets);
        Device.SubNet title = new Device.SubNet();
        title.net = "IP";
        title.mask = "Mask";
        subNets.add(0, title);
        adapter.setNewData(subNets);
    }

    private void initM3View(View view) {
        m3Layout = view.findViewById(R.id.device_mng_des_ll_m3);
        m3Space = view.findViewById(R.id.device_mng_des_btn_space);
        m3SpaceBack = view.findViewById(R.id.lds_iv_back);
        m3SpaceLayout = view.findViewById(R.id.layout_dev_space);
        m3SpacePb = view.findViewById(R.id.lds_progressbar);
        m3SpaceTvProgress = view.findViewById(R.id.lds_tv_progress);
        m3SpaceTvTotal = view.findViewById(R.id.lds_tv_total);
        m3SpaceTvAviliable = view.findViewById(R.id.lds_tv_aviliable);
        m3SpaceTvUsed = view.findViewById(R.id.lds_tv_used);
        m3SpaceIvHd = view.findViewById(R.id.lds_iv_hd);
        m3App = view.findViewById(R.id.device_mng_des_btn_app);
        accessView = view.findViewById(R.id.device_des_btn_by_browser);
        m3Reboot = view.findViewById(R.id.device_mng_des_btn_reboot);
        m3Shutdown = view.findViewById(R.id.device_mng_des_btn_shutdown);
        m3Layout.setVisibility(View.VISIBLE);
        if (UiUtils.isM8(bean.getDevClass())) {
            m3App.setVisibility(View.GONE);
            accessView.setVisibility(View.GONE);
        }
        if (UiUtils.isAndroidTV(bean.getDevClass())) {
            m3App.setVisibility(View.GONE);
            accessView.setVisibility(View.GONE);
            m3Shutdown.setVisibility(View.GONE);
            m3Space.setVisibility(View.GONE);
        }

        m3Space.setOnClickListener(listener);
        m3SpaceBack.setOnClickListener(listener);
        m3SpaceIvHd.setOnClickListener(listener);
        m3App.setOnClickListener(listener);
        m3Reboot.setOnClickListener(listener);
        m3Shutdown.setOnClickListener(listener);
    }

    @Nullable
    private OneOSHardDisk oneOSHardDisk1, oneOSHardDisk2;

    private void initM3Space() {
        loadingView.setVisibility(View.VISIBLE);
        SessionManager.getInstance().getLoginSession(bean.getId(), new GetSessionListener(false) {
            @Override
            public void onSuccess(String url, LoginSession loginSession) {
                OneOSSpaceAPI spaceAPI2 = new OneOSSpaceAPI(loginSession);
                spaceAPI2.setOnSpaceListener(new OneOSSpaceAPI.OnSpaceListener() {
                    @Override
                    public void onStart(String url) {
                    }

                    @Override
                    public void onSuccess(String url, boolean isOneOSSpace, @NonNull OneOSHardDisk hd1, OneOSHardDisk hd2) {
                        final long total = hd1.getTotal();
                        final long free = hd1.getFree();
                        final long used = hd1.getUsed();
                        oneOSHardDisk1 = hd1;
                        oneOSHardDisk2 = hd2;
                        String totalInfo = FileUtils.fmtFileSize(total);
                        String freeInfo = FileUtils.fmtFileSize(free);
                        String usedInfo = FileUtils.fmtFileSize(total - free);
                        float ratio = 100 - (free * 100f / total);
                        ratio = ratio > 100 ? 100 : ratio;
                        startProgressAnim((int) (ratio + .5f));
                        if (UiUtils.isM8(bean.getDevClass())) {
                            if (mView != null) {
                                TextView space2 = mView.findViewById(R.id.space2);
                                space2.setText(R.string.system_space);
                                TextView space3 = mView.findViewById(R.id.space3);
                                space3.setText(R.string.user_space);
                            }
                            usedInfo = String.format("%s/%s", freeInfo, totalInfo);
                            totalInfo = FileUtils.fmtFileSize(total + AppConstants.M8_SYSTEM_SPACE);
                            freeInfo = FileUtils.fmtFileSize(AppConstants.M8_SYSTEM_SPACE);
                        }
                        m3SpaceTvTotal.setText(totalInfo);
                        m3SpaceTvAviliable.setText(freeInfo);
                        m3SpaceTvUsed.setText(usedInfo);
                        ratio = ((ratio > 100) ? 100 : ratio);
                        m3SpaceTvProgress.setText(String.format("%.2f", ratio));
                        loadingView.setVisibility(View.GONE);
                        queryHDInfo(oneOSHardDisk1, oneOSHardDisk2);
                    }

                    @Override
                    public void onFailure(String url, int errorNo, String errorMsg) {
                        queryFailure();
                    }
                });
                spaceAPI2.query(true);

            }

            private void queryFailure() {
                loadingView.setVisibility(View.GONE);
                m3SpaceTvProgress.setText(R.string.query_space_failure);
                m3SpaceTvTotal.setText(R.string.query_space_failure);
                m3SpaceTvAviliable.setText(R.string.query_space_failure);
                m3SpaceTvUsed.setText(R.string.query_space_failure);
            }

            @Override
            public void onFailure(String url, int errorNo, String errorMsg) {
                super.onFailure(url, errorNo, errorMsg);
                queryHDInfo(null, null);
                queryFailure();
            }

            private void startProgressAnim(int progress) {
                float r = (float) progress / (float) 50;
                int durTime = (int) (r * 800);
                if (m3SpacePb != null) {
                    m3SpacePb.setAnimParameter(progress);
                    m3SpacePb.startCartoom();
                }
            }

            private void queryHDInfo(final OneOSHardDisk hardDisk1, final OneOSHardDisk hardDisk2) {
                OneOSHardDiskInfoAPI.OnHDInfoListener mListener = new OneOSHardDiskInfoAPI.OnHDInfoListener() {
                    @Override
                    public void onStart(String url) {
                    }

                    @Override
                    public void onSuccess(String url, String model, @Nullable OneOSHardDisk hd1, @Nullable OneOSHardDisk hd2) {
                        if (hd1 != null || hd2 != null) {
                            oneOSHardDisk1 = hd1;
                            oneOSHardDisk2 = hd2;
                            m3SpaceIvHd.setVisibility(View.VISIBLE);
                        }
                    }

                    @Override
                    public void onFailure(String url, int errorNo, String errorMsg) {
                        m3SpaceIvHd.setVisibility(View.GONE);
                    }
                };
                OneOSHardDiskInfoAPI hdInfoAPI = new OneOSHardDiskInfoAPI(bean.getVip());
                V5Observer observer = new V5Observer<Object>(bean.getId()) {

                    @Override
                    public void isNotV5() {//调用旧API

                        hdInfoAPI.setOnHDInfoListener(mListener);
                        hdInfoAPI.query(hardDisk1, hardDisk2);
                    }

                    @Override
                    public void fail(@NotNull BaseProtocol<Object> result) {
                        mListener.onFailure("", result.getError().getCode(), result.getError().getMsg());
                    }

                    @Override
                    public void success(@NotNull BaseProtocol<Object> result) {
                        String mode = hdInfoAPI.getHDInfor(new Gson().toJson(result.getData()), hardDisk1, hardDisk2);
                        mListener.onSuccess("", mode, hardDisk1, hardDisk2);
                    }

                    @Override
                    public boolean retry() {
                        V5Repository.Companion.INSTANCE().clearUser(bean.getId(), bean.getVip(), LoginTokenUtil.getToken(), this);
                        return true;
                    }
                };
                V5Repository.Companion.INSTANCE().getHDInforSystem(bean.getId(), bean.getVip(), LoginTokenUtil.getToken(), observer);
            }
        });


    }

    private void showHdView(Context context) {
        int countNum = 0;
        if (oneOSHardDisk1 != null && oneOSHardDisk1.getSerial() != null) countNum++;
        if (oneOSHardDisk2 != null && oneOSHardDisk2.getSerial() != null) countNum++;
        if (countNum > 0) {
            Intent intent = new Intent(context, HdManageActivity.class);
            intent.putExtra("count", String.valueOf(countNum));
            intent.putExtra(AppConstants.SP_FIELD_DEVICE_ID, bean.getId());
            context.startActivity(intent);
            dismiss();
        } else {
            ToastHelper.showToast(context.getString(R.string.tip_no_sata), Toast.LENGTH_SHORT);
        }
    }

    private void checkUpdate(DeviceModel deviceModel) {
        final String devId = deviceModel.getDevId();
        if (deviceModel.isOnline() && deviceModel.isOwner()) {
            FragmentActivity activity = UIUtils.getActivity(mView, FragmentActivity.class);
            if (activity != null) {
                mM3UpdateViewModel = ViewModelProviders.of(activity).get(M3UpdateViewModel.class);
            } else {
                mM3UpdateViewModel = new M3UpdateViewModel();
            }
            Observer<Resource<UpdateInfo>> observer = new Observer<Resource<UpdateInfo>>() {
                @Override
                public void onChanged(Resource<UpdateInfo> updateInfoResource) {
                    if (updateInfoResource.getStatus() == Status.SUCCESS && updateInfoResource.getData() != null) {
                        m3UpgradeBtn.setVisibility(View.VISIBLE);
                        m3UpgradeBtn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                mM3UpdateViewModel.update(v.getContext(), devId, updateInfoResource.getData(), null).observeForever(new Observer<Resource<Boolean>>() {
                                    @Override
                                    public void onChanged(Resource<Boolean> booleanResource) {
                                        if (booleanResource.getStatus() == Status.SUCCESS) {
                                            loadingView.setVisibility(View.GONE);
                                            mLayoutUpgradeProgress.setVisibility(View.VISIBLE);
                                            mM3UpdateViewModel.subUpgradeProgress().observeForever(new Observer<Resource<UpgradeProgress>>() {
                                                @Override
                                                public void onChanged(Resource<UpgradeProgress> upgradeProgressResource) {
                                                    if (upgradeProgressResource.getStatus() == Status.SUCCESS) {
                                                        final UpgradeProgress upgradeProgress = upgradeProgressResource.getData();

                                                        if (upgradeProgress != null && "download".equalsIgnoreCase(upgradeProgress.getName())) {
                                                            if (upgradeProgress.getPercent() >= 0)
                                                                mProgressBarDownload.setProgress(upgradeProgress.getPercent());
                                                            else {
                                                                mLayoutUpgradeProgress.setVisibility(View.GONE);
                                                                ToastHelper.showToast(R.string.device_upgrade_failed_by_download);
                                                            }
                                                        }
                                                        if (upgradeProgress != null && "install".equalsIgnoreCase(upgradeProgress.getName())) {
                                                            if (upgradeProgress.getPercent() >= 0) {
                                                                mProgressBarInstall.setProgress(upgradeProgress.getPercent());
                                                                if (upgradeProgress.getPercent() == 100) {
                                                                    mM3UpdateViewModel.showPowerDialog(v.getContext(), devId, false);
                                                                    if (deviceModel != null) {
                                                                        LoginSession loginSession = deviceModel.getLoginSession();
                                                                        if (loginSession != null) {
                                                                            OneOSInfo oneOSInfo = loginSession.getOneOSInfo();
                                                                            if (oneOSInfo != null) {
                                                                                if (deviceModel.isOnline() && deviceModel.isOwner()) {
                                                                                    oneOSInfo.setNeedsUp(false);
                                                                                }
                                                                            }
                                                                        }
                                                                    }
                                                                    dismiss();
                                                                }
                                                            } else {
                                                                mLayoutUpgradeProgress.setVisibility(View.GONE);
                                                                ToastHelper.showToast(R.string.device_upgrade_failed_by_install);
                                                            }
                                                        }
                                                    }
                                                }
                                            });
                                        } else if (booleanResource.getStatus() == Status.ERROR) {
                                            loadingView.setVisibility(View.GONE);
                                        } else if (booleanResource.getStatus() == Status.LOADING) {
                                            loadingView.setVisibility(View.VISIBLE);
                                        }
                                    }
                                });
                            }
                        });
                    }
                }
            };
            mM3UpdateViewModel.getUpdateInfo(deviceModel).observe(detailDialog, observer);
        }
    }

    private void showNetworks(View view) {
        subLayout = view.findViewById(R.id.layout_subnet);
        subLayout.setVisibility(View.VISIBLE);
        View subBack = subLayout.findViewById(R.id.ls_iv_back);
        RecyclerView subRv = subLayout.findViewById(R.id.ls_rv);
        TextView tvTitle = subLayout.findViewById(R.id.ls_tv_title);
        tvTitle.setText(R.string.network_location);
        subBack.setOnClickListener(listener);
        HomeNetRVAdapter adapter = new HomeNetRVAdapter(null);
        subRv.setLayoutManager(new LinearLayoutManager(context, RecyclerView.VERTICAL, false));
        subRv.setItemAnimator(null);
        subRv.setAdapter(adapter);
        List<Network> networks = new ArrayList<>();
        List<String> strings = bean.getNetworks();
        List<Network> list = NetManager.getInstance().getNetBeans();
        if (strings != null) {
            for (Network network : list) {
                for (String next : strings) {
                    if (Objects.equals(network.getId(), next)) {
                        networks.add(network);
                        break;
                    }
                }
                if (networks.size() == strings.size()) {
                    break;
                }
            }
        }
        adapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter baseQuickAdapter, View view, int i) {
                Network o = (Network) baseQuickAdapter.getData().get(i);
                EventBus.getDefault().post(o);
                dismiss();
            }
        });
        adapter.setNewData(networks);
    }

    private void showPowerDialog(final boolean isPowerOff) {
        int contentRes = isPowerOff ? R.string.confirm_power_off_device : R.string.confirm_reboot_device;
        DialogUtils.showConfirmDialog(context, R.string.tips, contentRes, R.string.confirm, R.string.cancel, new DialogUtils.OnDialogClickListener() {
            @Override
            public void onClick(DialogInterface dialog, boolean isPositiveBtn) {
                if (isPositiveBtn) {
                    doPowerOffOrRebootDevice(isPowerOff);
                }
            }
        });
    }


    private void doPowerOffOrRebootDevice(final boolean isPowerOff) {
        SessionManager.getInstance().getLoginSession(bean.getId(), new GetSessionListener() {
            @Override
            public void onStart(String url) {
                if (isPowerOff)
                    ToastUtils.showToast(R.string.power_off_device);
                else
                    ToastUtils.showToast(R.string.rebooting_device);
            }

            @Override
            public void onSuccess(String url, LoginSession data) {
                OneOSPowerAPI.OnPowerListener listener = new OneOSPowerAPI.OnPowerListener() {
                    @Override
                    public void onStart(String url) {
                    }

                    @Override
                    public void onSuccess(String url, boolean isPowerOff) {
                        if (isPowerOff)
                            ToastUtils.showToast(R.string.success_power_off_device);
                        else
                            ToastUtils.showToast(R.string.success_reboot_device);
                        dismiss();
                    }

                    @Override
                    public void onFailure(String url, int errorNo, String errorMsg) {
                        ToastUtils.showToast(HttpErrorNo.getResultMsg(false, errorNo, errorMsg));
                        if (!isPowerOff) {
                            if (rebootDevice()) {
                                dismiss();
                                ToastUtils.showToast(R.string.success_reboot_device);
                            }
                        }
                    }
                };


                V5Observer observer = new V5Observer<Object>(data.getId()) {

                    @Override
                    public void isNotV5() {
                        OneOSPowerAPI oneOSPowerAPI = new OneOSPowerAPI(data);
                        oneOSPowerAPI.setOnPowerListener(listener);
                        oneOSPowerAPI.power(isPowerOff);
                    }

                    @Override
                    public void fail(@NotNull BaseProtocol<Object> result) {
                        listener.onFailure("", result.getError().getCode(), result.getError().getMsg());
                    }

                    @Override
                    public void success(@NotNull BaseProtocol<Object> result) {
                        listener.onSuccess("", isPowerOff);

                    }

                    @Override
                    public boolean retry() {
                        V5Repository.Companion.INSTANCE().rebootOrHaltSystem(data.getId(), data.getIp(), LoginTokenUtil.getToken(), isPowerOff, this);
                        return true;
                    }
                };
                V5Repository.Companion.INSTANCE().rebootOrHaltSystem(data.getId(), data.getIp(), LoginTokenUtil.getToken(), isPowerOff, observer);
            }

            @Override
            public void onFailure(String url, int errorNo, String errorMsg) {
                if (!isPowerOff) {
                    if (rebootDevice()) {
                        dismiss();
                        ToastUtils.showToast(R.string.success_reboot_device);
                        return;
                    }
                }
                super.onFailure(url, errorNo, errorMsg);
            }
        });
    }

    private boolean rebootDevice() {
        int result = CMAPI.getInstance().rebootDevice(bean.getVip());
        if (result == Constants.CE_SUCC) {
            return true;
        } else {
            Timber.d("reboot result : %s", result);
        }
        return false;
    }

    private void deleteThisDevice() {
        if (bean.getHardData() != null) {
            if (bean.getHardData().isOwner()) {
                DialogUtil.showExtraSelectDialog(context, context.getString(R.string.msg_admin_remove_device)
                        , context.getString(R.string.clear_all), new DialogUtil.OnDialogButtonClickListener() {
                            @Override
                            public void onClick(View v, String strEdit, Dialog dialog, boolean isCheck) {
                                dialog.dismiss();
                                //如果是nas显示是否删除设备数据.
                                DialogUtil.showSelectDialog(context, context.getString(R.string.msg_admin_clear_all),
                                        context.getString(R.string.confirm), new DialogUtil.OnDialogButtonClickListener() {
                                            @Override
                                            public void onClick(View v, String strEdit, final Dialog dialog, boolean isCheck) {
                                                if (isCheck) {
                                                    if (bean.isDevDisable()) {
                                                        ScoreHelper.showNeedMBPointDialog(context);
                                                        dialog.dismiss();
                                                    } else {
                                                        dialog.hide();
                                                        clearNasUser(dialog);
                                                    }
                                                } else {
                                                    dialog.hide();
                                                    clearDeviceUser(dialog);
                                                }
                                            }
                                        },
                                        context.getString(R.string.cancel), null, UiUtils.isNas(bean.getDevClass()) ? context.getString(R.string.delete_personal_data) : null);
                            }
                        }, context.getString(R.string.cancel), null, null, null, null);
            } else {
                DialogUtil.showSelectDialog(context, context.getString(R.string.unbind_device_prompt),
                        context.getString(R.string.confirm), new DialogUtil.OnDialogButtonClickListener() {
                            @Override
                            public void onClick(View v, String strEdit, final Dialog dialog, boolean isCheck) {
                                if (isCheck) {
                                    if (bean.isDevDisable()) {
                                        dialog.dismiss();
                                        ScoreHelper.showNeedMBPointDialog(context);
                                    } else {
                                        dialog.hide();
                                        deleteNasUser(dialog);
                                    }
                                } else {
                                    dialog.hide();
                                    deleteThisDevice(dialog);
                                }
                            }
                        },
                        context.getString(R.string.cancel), null, UiUtils.isNas(bean.getDevClass()) ? context.getString(R.string.delete_personal_data) : null);
            }
        }
    }

    private void clearNasUser(final Dialog dialog) {
        loadingView.setVisibility(View.VISIBLE);
        SessionManager.getInstance().getLoginSession(bean.getId(), new GetSessionListener() {
            @Override
            public void onSuccess(String url, LoginSession data) {
                OneOSClearUsersAPI.ClearUserListener listener = new OneOSClearUsersAPI.ClearUserListener<String>() {
                    @Override
                    public void onStart(String url) {
                    }

                    @Override
                    public void onSuccess(String url) {
                        OneOSUserManageAPI.OnUserManageListener mListener = new OneOSUserManageAPI.OnUserManageListener() {
                            @Override
                            public void onStart(String url) {
                            }

                            @Override
                            public void onSuccess(String url, String cmd) {
                                clearDeviceUser(dialog);
                            }

                            @Override
                            public void onFailure(String url, int errorNo, String errorMsg) {
                                loadingView.setVisibility(View.GONE);
                                dialog.show();
                                if (context != null) {
                                    String msg = HttpErrorNo.getResultMsg(true, errorNo, errorMsg);
                                    String s = context.getString(R.string.remove_device_failed);
                                    ToastHelper.showToast(String.format("%s! %s", s, msg));
                                }
                            }
                        };
                        if (SessionCache.Companion.getInstance().isV5(data.getId())) {
                            mListener.onSuccess("", "");
                        } else {
                            OneOSUserManageAPI manageAPI = new OneOSUserManageAPI(data);
                            manageAPI.setOnUserManageListener(mListener);
                            manageAPI.chpwd(AppConstants.DEFAULT_USERNAME_ADMIN, AppConstants.DEFAULT_USERNAME_PWD);
                        }

//                        V5Observer observer = new V5Observer<Object>(data.getId()){
//
//                            @Override
//                            public void isNotV5() {
//                                OneOSUserManageAPI manageAPI = new OneOSUserManageAPI(data);
//                                manageAPI.setOnUserManageListener(mListener);
//                                manageAPI.chpwd(AppConstants.DEFAULT_USERNAME_ADMIN, AppConstants.DEFAULT_USERNAME_PWD);
//                            }
//
//                            @Override
//                            public void fail(@NotNull BaseProtocol<Object> result) {
//                                mListener.onFailure("",result.getError().getCode(),result.getError().getMsg());
//                            }
//
//                            @Override
//                            public void success(@NotNull BaseProtocol<Object> result) {
//                                mListener.onSuccess("","");
//
//                            }
//
//                            @Override
//                            public boolean retry() {
//                                V5Repository.Companion.INSTANCE().updateUserPassword(data.getId(),data.getIp(),LoginTokenUtil.getToken(),AppConstants.DEFAULT_USERNAME_ADMIN, AppConstants.DEFAULT_USERNAME_PWD,this);
//                                return true;
//                            }
//                        };
//                        V5Repository.Companion.INSTANCE().updateUserPassword(data.getId(),data.getIp(),LoginTokenUtil.getToken(),AppConstants.DEFAULT_USERNAME_ADMIN, AppConstants.DEFAULT_USERNAME_PWD,observer);
                    }

                    @Override
                    public void onFailure(String url, int errorNo, String errorMsg) {
                        loadingView.setVisibility(View.GONE);
                        dialog.show();
                        if (context != null) {
                            String msg = HttpErrorNo.getResultMsg(true, errorNo, errorMsg);
                            String s = context.getString(R.string.remove_device_failed);
                            ToastHelper.showToast(String.format("%s! %s", s, msg));
                        }
                    }
                };

                V5Observer observer = new V5Observer<Object>(data.getId()) {

                    @Override
                    public void isNotV5() {
                        OneOSClearUsersAPI osClearUsersAPI = new OneOSClearUsersAPI(data);
                        osClearUsersAPI.setClearUserListener(listener);
                        osClearUsersAPI.clear();
                    }

                    @Override
                    public void fail(@NotNull BaseProtocol<Object> result) {
                        listener.onFailure("", result.getError().getCode(), result.getError().getMsg());
                    }

                    @Override
                    public void success(@NotNull BaseProtocol<Object> result) {
                        listener.onSuccess("");
                    }

                    @Override
                    public boolean retry() {
                        V5Repository.Companion.INSTANCE().clearUser(data.getId(), data.getIp(), LoginTokenUtil.getToken(), this);
                        return true;
                    }
                };
                V5Repository.Companion.INSTANCE().clearUser(data.getId(), data.getIp(), LoginTokenUtil.getToken(), observer);

            }

            @Override
            public void onFailure(String url, int errorNo, String errorMsg) {
                loadingView.setVisibility(View.GONE);
                if (context != null) {
                    String msg = HttpErrorNo.getResultMsg(true, errorNo, errorMsg);
                    String s = context.getString(R.string.remove_device_failed);
                    ToastHelper.showToast(String.format("%s! %s", s, msg));
                }
            }
        });
    }

    private void clearDeviceUser(final Dialog dialog) {
        loadingView.setVisibility(View.VISIBLE);
        DeviceUserUtil.deviceClearBinds(bean.getId(), DeviceDialogUtil.this,
                new MyOkHttpListener() {
                    @Override
                    public void success(Object tag, GsonBaseProtocol data) {
                        loadingView.setVisibility(View.GONE);
                        DevManager.getInstance().initHardWareList(null);
                        dialog.dismiss();
                        dismiss();
                    }

                    @Override
                    public void error(Object tag, GsonBaseProtocol baseProtocol) {
                        super.error(tag, baseProtocol);
                        dialog.show();
                        loadingView.setVisibility(View.GONE);
                    }
                });
    }

    private void deleteNasUser(Dialog dialog) {
        loadingView.setVisibility(View.VISIBLE);
        SessionManager.getInstance().getLoginSession(bean.getId(), new GetSessionListener() {

            @Override
            public void onSuccess(String url, LoginSession data) {

                OneOSUserManageAPI manageAPI = new OneOSUserManageAPI(data);
                OneOSUserManageAPI.OnUserManageListener listener = new OneOSUserManageAPI.OnUserManageListener() {
                    @Override
                    public void onStart(String url) {
                    }

                    @Override
                    public void onSuccess(String url, String cmd) {
                        if (bean.getMnglevel() == 0 || bean.getMnglevel() == 1) {
                            if (SessionCache.Companion.getInstance().isV5(data.getId()) || Objects.equals(cmd, "update")) {
                                deleteThisDevice(dialog);
                            } else {
                                manageAPI.chpwd(AppConstants.DEFAULT_USERNAME_ADMIN, AppConstants.DEFAULT_USERNAME_PWD);
                            }
//                            V5Observer observer = new V5Observer<Object>(data.getId()) {
//
//                                @Override
//                                public void isNotV5() {
//                                    manageAPI.chpwd(AppConstants.DEFAULT_USERNAME_ADMIN, AppConstants.DEFAULT_USERNAME_PWD);
//                                }
//
//                                @Override
//                                public void fail(@NotNull BaseProtocol<Object> result) {
//                                    int errorNo = result.getError().getCode();
//                                    String errorMsg = result.getError().getMsg();
//                                    loadingView.setVisibility(View.GONE);
//                                    if (errorNo == -40000 && Objects.equals("Delete system user failed", errorMsg)) {
//                                        deleteThisDevice(dialog);
//                                    } else if (context != null) {
//                                        dialog.show();
//                                        String msg = HttpErrorNo.getResultMsg(true, errorNo, errorMsg);
//                                        String s = context.getString(R.string.remove_device_failed);
//                                        ToastHelper.showToast(String.format("%s! %s", s, msg));
//                                    }
//                                }
//
//                                @Override
//                                public void success(@NotNull BaseProtocol<Object> result) {
//                                    deleteThisDevice(dialog);
//                                }
//
//                                @Override
//                                public boolean retry() {
//                                    V5Repository.Companion.INSTANCE().updateUserPassword(data.getId(), data.getIp(), LoginTokenUtil.getToken(), AppConstants.DEFAULT_USERNAME_ADMIN, AppConstants.DEFAULT_USERNAME_PWD, this);
//                                    return true;
//                                }
//                            };
//                            V5Repository.Companion.INSTANCE().updateUserPassword(data.getId(), data.getIp(), LoginTokenUtil.getToken(), AppConstants.DEFAULT_USERNAME_ADMIN, AppConstants.DEFAULT_USERNAME_PWD, observer);


                        } else
                            deleteThisDevice(dialog);
                    }

                    @Override
                    public void onFailure(String url, int errorNo, String errorMsg) {
                        loadingView.setVisibility(View.GONE);
                        if (errorNo == -40000 && Objects.equals("Delete system user failed", errorMsg)) {
                            deleteThisDevice(dialog);
                        } else if (context != null) {
                            dialog.show();
                            String msg = HttpErrorNo.getResultMsg(true, errorNo, errorMsg);
                            String s = context.getString(R.string.remove_device_failed);
                            ToastHelper.showToast(String.format("%s! %s", s, msg));
                        }
                    }
                };

                V5Observer observer = new V5Observer<Object>(data.getId()) {

                    @Override
                    public void isNotV5() {
                        manageAPI.setOnUserManageListener(listener);
                        manageAPI.delete(SessionManager.getInstance().getUsername());
                    }

                    @Override
                    public void fail(@NotNull BaseProtocol<Object> result) {
                        listener.onFailure("", result.getError().getCode(), result.getError().getMsg());
                    }

                    @Override
                    public void success(@NotNull BaseProtocol<Object> result) {
                        listener.onSuccess("", "delete");
                    }

                    @Override
                    public boolean retry() {
                        V5Repository.Companion.INSTANCE().deleteUser(data.getId(), data.getIp(), LoginTokenUtil.getToken(),
                                SessionManager.getInstance().getUsername(), this);
                        return true;
                    }
                };
                V5Repository.Companion.INSTANCE().deleteUser(data.getId(), data.getIp(), LoginTokenUtil.getToken(),
                        SessionManager.getInstance().getUsername(), observer);

            }

            @Override
            public void onFailure(String url, int errorNo, String errorMsg) {
                loadingView.setVisibility(View.GONE);
                if (context != null) {
                    dialog.show();
                    String msg = HttpErrorNo.getResultMsg(true, errorNo, errorMsg);
                    String s = context.getString(R.string.remove_device_failed);
                    ToastHelper.showToast(String.format("%s! (%s)", s, errorNo));
                }
            }
        });
    }

    private void deleteThisDevice(final Dialog dialog) {
        loadingView.setVisibility(View.VISIBLE);
        DeviceUserUtil.deleteThisDevice(bean.getId(), DeviceDialogUtil.this,
                new ListResultListener<UnbindDeviceResult>() {

                    @Override
                    public void error(Object tag, GsonBaseProtocol baseProtocol) {
                        loadingView.setVisibility(View.GONE);
                        dialog.show();
                        ToastUtils.showError(baseProtocol.result);
                    }

                    @Override
                    public void success(Object tag, List<UnbindDeviceResult> results) {
                        loadingView.setVisibility(View.GONE);
                        DevManager.getInstance().initHardWareList(null);
                        dialog.dismiss();
                        dismiss();
                    }

                    @Override
                    public void error(Object tag, List<UnbindDeviceResult> mErrorResults) {
                        dialog.show();
                        loadingView.setVisibility(View.GONE);
                    }
                });
    }

    @Override
    public void onLoadStart(Disposable disposable) {
        if (loadingView != null)
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
}
