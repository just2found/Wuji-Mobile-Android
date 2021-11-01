package net.linkmate.app.util.business;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.google.gson.Gson;

import net.linkmate.app.R;
import net.linkmate.app.base.MyConstants;
import net.linkmate.app.base.MyOkHttpListener;
import net.linkmate.app.bean.DeviceBean;
import net.linkmate.app.manager.DevManager;
import net.linkmate.app.manager.MessageManager;
import net.linkmate.app.manager.NetManager;
import net.linkmate.app.ui.activity.nasApp.NasAppsActivity;
import net.linkmate.app.ui.nas.helper.HdManageActivity;
import net.linkmate.app.ui.nas.user.UserManageActivity;
import net.linkmate.app.util.DialogUtil;
import net.linkmate.app.util.ToastUtils;
import net.linkmate.app.view.DataItemLayout;
import net.linkmate.app.view.SubnetLayout;
import net.linkmate.app.view.adapter.DevUserMngRVAdapter;
import net.linkmate.app.view.adapter.HomeNetRVAdapter;
import net.sdvn.cmapi.CMAPI;
import net.sdvn.cmapi.Network;
import net.sdvn.cmapi.util.ClipboardUtils;
import net.sdvn.common.internet.core.GsonBaseProtocol;
import net.sdvn.common.internet.core.HttpLoader;
import net.sdvn.common.internet.listener.ListResultListener;
import net.sdvn.common.internet.listener.ResultListener;
import net.sdvn.common.internet.loader.SetDeviceNameHttpLoader;
import net.sdvn.common.internet.protocol.ShareCode;
import net.sdvn.common.internet.protocol.SharedUserList;
import net.sdvn.common.internet.protocol.SubnetList;
import net.sdvn.common.internet.protocol.UnbindDeviceResult;
import net.sdvn.common.internet.protocol.entity.ShareUser;
import net.sdvn.common.internet.protocol.entity.SubnetEntity;
import net.sdvn.common.internet.utils.LoginTokenUtil;
import net.sdvn.nascommon.SessionManager;
import net.sdvn.nascommon.constant.AppConstants;
import net.sdvn.nascommon.constant.HttpErrorNo;
import net.sdvn.nascommon.iface.GetSessionListener;
import net.sdvn.nascommon.model.DeviceModel;
import net.sdvn.nascommon.model.oneos.OneOSHardDisk;
import net.sdvn.nascommon.model.oneos.api.sys.OneOSHardDiskInfoAPI;
import net.sdvn.nascommon.model.oneos.api.sys.OneOSPowerAPI;
import net.sdvn.nascommon.model.oneos.api.sys.OneOSSpaceAPI;
import net.sdvn.nascommon.model.oneos.api.user.OneOSClearUsersAPI;
import net.sdvn.nascommon.model.oneos.api.user.OneOSUserManageAPI;
import net.sdvn.nascommon.model.oneos.user.LoginSession;
import net.sdvn.nascommon.utils.AnimUtils;
import net.sdvn.nascommon.utils.DialogUtils;
import net.sdvn.nascommon.utils.FileUtils;
import net.sdvn.nascommon.utils.ToastHelper;
import net.sdvn.nascommon.utils.Utils;
import net.sdvn.nascommon.viewmodel.DeviceViewModel;
import net.sdvn.nascommon.widget.AnimCircleProgressBar;

import org.greenrobot.eventbus.EventBus;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.weline.repo.SessionCache;
import io.weline.repo.data.model.BaseProtocol;
import io.weline.repo.net.V5Observer;
import io.weline.repo.repository.V5Repository;

public class DeviceManagerDialogUtil implements HttpLoader.HttpLoaderStateListener {

    private static final String TAG = DeviceManagerDialogUtil.class.getSimpleName();
    private final Context context;
    private final DeviceBean bean;
    private final int position;
    @Nullable
    private OneOSHardDisk hardDisk1, hardDisk2;


    private Dialog mngDialog;
    private View sv;
    private View share;
    private View snSetting;
    private View btnDelete;
    private View loadingView;

    private DataItemLayout dilName;
    private DataItemLayout dilMarkName;
    private DataItemLayout dilOwner;
    private DataItemLayout dilAddTime;
    private DataItemLayout dilVersion;
    private DataItemLayout dilId;
    private DataItemLayout dilSn;

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
    private TextView userMngCheckAll;
    private RecyclerView lumRv;
    private DevUserMngRVAdapter userMngAdapter;
    private View userMngDelete;

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

    //networks 所处网络
    private View locationNetworks;

    //subnet view
    private View subLayout;

    //sn开关
    private boolean snInternetEnable;
    private boolean snDnsEnable;
    private boolean snSubnetEnable;

    private int dialogLevel;
    private final String devId;
    private View mView;

    public DeviceManagerDialogUtil(Context context, DeviceBean bean, int position) {
        this.context = context;
        this.bean = bean;
        this.position = position;
        devId = bean.getId();
    }

    private boolean onBackPress() {
        return onBackPress(sv, shareLayout, userMngLayout, m3SpaceLayout, snLayout, subLayout);
    }

    private boolean onBackPress(View visibView, View... goneViews) {
        if (dialogLevel <= 0 || mngDialog == null || !mngDialog.isShowing()) {
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
                mngDialog.dismiss();
            }
            dialogLevel--;
            return true;
        }
    }

    private View.OnClickListener listener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (Utils.isFastClick(v)) {
                return;
            }
            switch (v.getId()) {
                case R.id.device_mng_des_btn_share:
                    sv.setVisibility(View.GONE);
                    shareLayout.setVisibility(View.VISIBLE);
                    dialogLevel++;
                    initShareViewDate();
                    break;
                case R.id.lsc_iv_back:
                    onBackPress();
                    break;
                case R.id.device_mng_des_btn_user_mng:
                    if (bean.isNas()) {
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
                case R.id.lum_btn_check_all:
                    checkAllUsers();
                    break;
                case R.id.lum_btn_cancel_share:
                    deleteUsers();
                    break;
                case R.id.device_mng_des_btn_space:
                    sv.setVisibility(View.GONE);
                    m3SpaceLayout.setVisibility(View.VISIBLE);
                    dialogLevel++;
                    initM3Space();
                    break;
                case R.id.lds_iv_hd:
                    showHdView(v.getContext());
                    break;
                case R.id.device_mng_des_btn_app:
                    Intent intent = new Intent(context, NasAppsActivity.class);
                    intent.putExtra(AppConstants.SP_FIELD_DEVICE_ID, bean.getId());
                    context.startActivity(intent);
                    break;
                case R.id.device_mng_des_btn_reboot:
                    showPowerDialog(false);
                    break;
                case R.id.device_mng_des_btn_shutdown:
                    showPowerDialog(true);
                    break;
                case R.id.lds_iv_back:
                    onBackPress();
                    break;
                case R.id.device_mng_des_btn_sn:
                    sv.setVisibility(View.GONE);
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
                case R.id.device_mng_btn_networks:
                    showNetworks();
                    sv.setVisibility(View.GONE);
                    dialogLevel++;
                    break;
                case R.id.ls_iv_back:
                    onBackPress();
                    break;
                case R.id.device_mng_des_btn_delete:
                    deleteThisDevice();
                    break;
            }
        }
    };

    private void showNasUserManager(Context context) {
        Intent intent = new Intent(context, UserManageActivity.class);
        intent.putExtra(AppConstants.SP_FIELD_DEVICE_ID, devId);
        context.startActivity(intent);
    }

    private void showHdView(Context context) {
        int countNum = 0;
        if (hardDisk1 != null && hardDisk1.getSerial() != null) countNum++;
        if (hardDisk2 != null && hardDisk2.getSerial() != null) countNum++;
        if (countNum > 0) {
            Intent intent = new Intent(context, HdManageActivity.class);
            intent.putExtra("count", String.valueOf(countNum));
            intent.putExtra(AppConstants.SP_FIELD_DEVICE_ID, devId);
            context.startActivity(intent);
        } else {
            ToastHelper.showToast(context.getString(R.string.tip_no_sata), Toast.LENGTH_SHORT);
        }
    }

    public void showDetailDialog() {
        if (mngDialog != null && mngDialog.isShowing()) {
            return;
        }
        mView = LayoutInflater.from(context).inflate(R.layout.layout_dialog_device_mng, null);
        mngDialog = new Dialog(context, R.style.DialogTheme);
        mngDialog.setContentView(mView);

        initView(mView);

        dilOwner.setText(bean.getOwnerName());
        dilVersion.setText(bean.getAppVersion());
        if (bean.getHardData() != null) {
            dilAddTime.setText(bean.getHardData().getDatetime());
            dilName.setText(bean.getHardData().getDevicename());
            dilId.setText(bean.getHardData().getDeviceid());
            dilSn.setText(bean.getHardData().getDevicesn());
        }
        if (bean.isOnline()) {
            //管理权限
            if ((bean.getMnglevel() == 0 || bean.getMnglevel() == 1)) {
                dilName.mIv.setVisibility(View.VISIBLE);
                dilName.mIv.setImageResource(R.drawable.icon_edit);
                dilName.mIv.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        rename();
                    }
                });

                //分享
                initShareView(mView);

                //用户管理
                initUserView(mView);

                //M3设备
                if (bean.isNas()) {
                    final DeviceModel deviceModel = SessionManager.getInstance().getDeviceModel(bean.getId());
                    if (deviceModel != null) {
                        initM3View(mView);
                    } else {
                        dilMarkName.setVisibility(View.GONE);
                    }
                }
                //SN设备
                if (bean.isSN) {
                    initSnView(mView);
                }
            }
            //M3设备
            if (bean.isNas()) {
                final DeviceModel deviceModel = SessionManager.getInstance().getDeviceModel(bean.getId());
                if (deviceModel != null) {
                    dilMarkName.setVisibility(View.VISIBLE);
                    dilMarkName.mIv.setVisibility(View.VISIBLE);
                    dilMarkName.mIv.setImageResource(R.drawable.icon_edit);
                    dilMarkName.mIv.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            editMarkName();
                        }
                    });
                    deviceModel.getDevNameFromDB().subscribe(new Consumer<String>() {
                        @Override
                        public void accept(String s) {
                            if (dilMarkName != null)
                                dilMarkName.setText(s);
                        }
                    });
                } else {
                    dilMarkName.setVisibility(View.GONE);
                }
            }
        }

        btnDelete.setVisibility(View.VISIBLE);
        btnDelete.setOnClickListener(listener);

        List<String> networks = bean.getNetworks();
        if (networks != null && networks.size() > 0) {
            locationNetworks.setVisibility(View.VISIBLE);
            locationNetworks.setOnClickListener(listener);
        } else {
            locationNetworks.setVisibility(View.GONE);
        }

        mngDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                if (userMngAdapter != null)
                    userMngAdapter = null;
            }
        });

        mngDialog.show();
        sv.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                DisplayMetrics metrics = context.getResources().getDisplayMetrics();
                WindowManager.LayoutParams params = mngDialog.getWindow().getAttributes();
                params.width = (int) (metrics.widthPixels * 0.80);
                mngDialog.getWindow().setAttributes(params);
                sv.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });
        dialogLevel = 1;
    }

    private void initView(View view) {
        sv = view.findViewById(R.id.device_mng_sv);
        btnDelete = view.findViewById(R.id.device_mng_des_btn_delete);
        locationNetworks = view.findViewById(R.id.device_mng_btn_networks);
        loadingView = view.findViewById(R.id.layout_loading);
        view.findViewById(R.id.device_des_iv_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mngDialog.dismiss();
            }
        });

        dilName = view.findViewById(R.id.device_mng_dil_name);
        dilMarkName = view.findViewById(R.id.device_mng_des_dil_remark_name);
        dilOwner = view.findViewById(R.id.device_mng_dil_owner);
        dilAddTime = view.findViewById(R.id.device_mng_dil_add_time);
        dilVersion = view.findViewById(R.id.device_mng_dil_version);
        dilId = view.findViewById(R.id.device_mng_dil_id);
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
                case R.id.device_mng_dil_name:
                    clipString(dilName.mTvData.getText().toString().trim());
                    break;
                case R.id.device_mng_des_dil_remark_name:
                    clipString(dilMarkName.mTvData.getText().toString().trim());
                    break;
                case R.id.device_mng_dil_owner:
                    clipString(dilOwner.mTvData.getText().toString().trim());
                    break;
                case R.id.device_mng_dil_add_time:
                    clipString(dilAddTime.mTvData.getText().toString().trim());
                    break;
                case R.id.device_mng_dil_version:
                    clipString(dilVersion.mTvData.getText().toString().trim());
                    break;
                case R.id.device_mng_dil_id:
                    clipString(dilId.mTvData.getText().toString().trim());
                    break;
                case R.id.device_mng_dil_sn:
                    clipString(dilSn.mTvData.getText().toString().trim());
                    break;
            }
        };
        dilName.setDataOnClickListener(listener);
        dilMarkName.setDataOnClickListener(listener);
        dilOwner.setDataOnClickListener(listener);
        dilAddTime.setDataOnClickListener(listener);
        dilVersion.setDataOnClickListener(listener);
        dilId.setDataOnClickListener(listener);
        dilSn.setDataOnClickListener(listener);
    }

    private void clipString(String content) {
        if (context != null) {
            ClipboardUtils.copyToClipboard(context, content);
            ToastUtils.showToast(context.getString(R.string.Copied) + content);
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
                            if (!MyConstants.regExInput(strEdit)) {
                                ToastHelper.showToast(R.string.no_special_characters_in_the_name);
                                AnimUtils.sharkEditText(context, v);
                                return;
                            }
                            SetDeviceNameHttpLoader loader = new SetDeviceNameHttpLoader(GsonBaseProtocol.class);
                            loader.setHttpLoaderStateListener(DeviceManagerDialogUtil.this);
                            loader.setParams(bean.getId(), strEdit);
                            loader.executor(new MyOkHttpListener() {

                                @Override
                                public void success(Object tag, GsonBaseProtocol gsonBaseProtocol) {
                                    DevManager.getInstance().notifyDeviceStateChanged();
                                    bean.setName(strEdit);
                                    dilName.setText(strEdit);
                                    dialog.dismiss();
                                    ToastHelper.showLongToastSafe(R.string.modify_succeed);
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
        final DeviceViewModel deviceViewModel = new DeviceViewModel();
        deviceViewModel.showDeviceName(context, bean.getId(), dilMarkName.mTvData.getText().toString().trim(),
                newName -> {
                    dilMarkName.setText(newName);
                    DevManager.getInstance().notifyDeviceStateChanged();
                });

    }

    private void initShareView(View view) {
        share = view.findViewById(R.id.device_mng_des_btn_share);
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

        switchShare.setChecked(bean.getHardData().getEnableshare());
        switchShareNeedAuth.setChecked(bean.getHardData().isScanconfirm());
        refreshShareView(bean.getHardData().getEnableshare());

        switchShare.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, final boolean isChecked) {
                ShareUtil.savedEnableShareState(bean.getId(), isChecked,
                        DeviceManagerDialogUtil.this, new ResultListener() {
                            @Override
                            public void success(Object tag, GsonBaseProtocol data) {
                                bean.getHardData().setEnableshare(isChecked);
                                refreshShareView(isChecked);
                                initShareViewDate();
                            }

                            @Override
                            public void error(Object tag, GsonBaseProtocol baseProtocol) {
                                switchShare.setChecked(bean.getHardData().getEnableshare());
                            }
                        });
            }
        });
        switchShareNeedAuth.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, final boolean isChecked) {
                ShareUtil.savedScanConfirmState(bean.getHardData().getDeviceid(), isChecked,
                        DeviceManagerDialogUtil.this, new ResultListener() {
                            @Override
                            public void success(Object tag, GsonBaseProtocol data) {
                                bean.getHardData().setScanconfirm(isChecked);
                            }

                            @Override
                            public void error(Object tag, GsonBaseProtocol baseProtocol) {
                                switchShareNeedAuth.setChecked(bean.getHardData().isScanconfirm());
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
        if (bean.getHardData().getEnableshare()) {
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
        userMngCheckAll = view.findViewById(R.id.lum_btn_check_all);
        lumRv = view.findViewById(R.id.lum_rv);
        userMngDelete = view.findViewById(R.id.lum_btn_cancel_share);

        userMng.setVisibility(View.VISIBLE);
        userMng.setOnClickListener(listener);
        userMngBack.setOnClickListener(listener);
        userMngCheckAll.setOnClickListener(listener);
        userMngDelete.setOnClickListener(listener);
        initUserMngLayout(context);
    }

    private void initUserMngLayout(Context context) {
        userMngAdapter = new DevUserMngRVAdapter();
        lumRv.setLayoutManager(new LinearLayoutManager(context, RecyclerView.VERTICAL, false));
        lumRv.setItemAnimator(null);
        lumRv.setAdapter(userMngAdapter);
    }

    private void initUserMngData() {
        DeviceUserUtil.shareUsers(bean.getId(), DeviceManagerDialogUtil.this,
                new MyOkHttpListener<SharedUserList>() {
                    @Override
                    public void success(Object tag, SharedUserList data) {
                        isCheckAll = false;
                        userMngCheckAll.setText(R.string.check_all);

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

    private boolean isCheckAll;

    private void checkAllUsers() {
        isCheckAll = !isCheckAll;
        if (isCheckAll) {
            boolean isChanged = false;
            for (ShareUser user : userMngAdapter.getData()) {
                if (user.mgrlevel != 0 && user.mgrlevel != 1) {
                    user.isSelected = true;
                    isChanged = true;
                }
            }
            if (isChanged) {
                userMngCheckAll.setText(R.string.cancel);
            }
        } else {
            for (ShareUser user : userMngAdapter.getData()) {
                if (user.mgrlevel != 0 && user.mgrlevel != 1)
                    user.isSelected = false;
            }
            userMngCheckAll.setText(R.string.check_all);
        }
        userMngAdapter.notifyDataSetChanged();
    }

    private void deleteUsers() {
        final List<String> userids = new ArrayList<>();
        for (ShareUser user : userMngAdapter.getData()) {
            if (user.isSelected)
                userids.add(user.userid);
        }
        if (userids.size() > 0)
            DialogUtil.showSelectDialog(context, context.getString(R.string.cancel_share_to_these_users),
                    context.getString(R.string.yes), new DialogUtil.OnDialogButtonClickListener() {
                        @Override
                        public void onClick(View v, String strEdit, final Dialog dialog, boolean isCheck) {
                            dialog.dismiss();
                            DeviceUserUtil.unbindDevice(bean.getId(), userids, DeviceManagerDialogUtil.this,
                                    new ListResultListener<UnbindDeviceResult>() {
                                        @Override
                                        public void success(Object tag, List<UnbindDeviceResult> results) {
                                            initUserMngData();
                                        }

                                        @Override
                                        public void error(Object tag, List<UnbindDeviceResult> mErrorResults) {

                                        }

                                        @Override
                                        public void error(Object tag, GsonBaseProtocol baseProtocol) {
                                            ToastUtils.showError(baseProtocol.result);
                                        }
                                    });
                        }
                    },
                    context.getString(R.string.no), null);
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
        m3Reboot = view.findViewById(R.id.device_mng_des_btn_reboot);
        m3Shutdown = view.findViewById(R.id.device_mng_des_btn_shutdown);
        m3Layout.setVisibility(View.VISIBLE);

        m3Space.setOnClickListener(listener);
        m3SpaceBack.setOnClickListener(listener);
        m3SpaceIvHd.setOnClickListener(listener);
        m3App.setOnClickListener(listener);
        m3Reboot.setOnClickListener(listener);
        m3Shutdown.setOnClickListener(listener);

    }

    private void initM3Space() {
        loadingView.setVisibility(View.VISIBLE);
        SessionManager.getInstance().getLoginSession(bean.getId(), new GetSessionListener() {

            @Override
            public void onSuccess(String url, final LoginSession data) {
                OneOSSpaceAPI spaceAPI2 = new OneOSSpaceAPI(data);
                spaceAPI2.setOnSpaceListener(new OneOSSpaceAPI.OnSpaceListener() {
                    @Override
                    public void onStart(String url) {
                    }

                    @Override
                    public void onSuccess(String url, boolean isOneOSSpace, @NonNull OneOSHardDisk hd1, OneOSHardDisk hd2) {
                        final long total = hd1.getTotal();
                        final long free = hd1.getFree();
                        final long used = hd1.getUsed();
                        hardDisk1 = hd1;
                        hardDisk2 = hd2;
                        String totalInfo = FileUtils.fmtFileSize(total);
                        String freeInfo = FileUtils.fmtFileSize(free);
                        String usedInfo = FileUtils.fmtFileSize(total - free);
                        float ratio = 100 - (free * 100f / total);
                        ratio = ratio > 100 ? 100 : ratio;
                        startProgressAnim((int) (ratio + .5f));
                        m3SpaceTvTotal.setText(totalInfo);
                        m3SpaceTvAviliable.setText(freeInfo);
                        m3SpaceTvUsed.setText(usedInfo);
                        ratio = ((ratio > 100) ? 100 : ratio);
                        m3SpaceTvProgress.setText(String.format("%.2f", ratio));
                        loadingView.setVisibility(View.GONE);
                        queryHDInfo(hardDisk1, hardDisk2);
                    }

                    @Override
                    public void onFailure(String url, int errorNo, String errorMsg) {
                        queryFailure();
                    }
                });
                spaceAPI2.query(true);
            }

            @Override
            public void onFailure(String url, int errorNo, String errorMsg) {
                queryFailure();
            }

            private void queryFailure() {
                loadingView.setVisibility(View.GONE);
                m3SpaceTvProgress.setText(R.string.query_space_failure);
                m3SpaceTvTotal.setText(R.string.query_space_failure);
                m3SpaceTvAviliable.setText(R.string.query_space_failure);
                m3SpaceTvUsed.setText(R.string.query_space_failure);
            }
        });
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

    private void initSnView(final View view) {
        snLayout = view.findViewById(R.id.layout_node_setting);
        snBack = view.findViewById(R.id.lns_iv_back);
        snSetting = view.findViewById(R.id.device_mng_des_btn_sn);
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
                            mngDialog.dismiss();
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
                            mngDialog.dismiss();
                    }
                });
                isCommit = true;
            } else {
                submitSubnet = true;
            }
        }
        if (!isCommit) {
            if (isSubmitFlag)
                mngDialog.dismiss();
            else
                onBackPress();
        }
    }

    private void showNetworks() {
        subLayout = mView.findViewById(R.id.layout_subnet);
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
                mngDialog.dismiss();
            }
        });
        adapter.setNewData(networks);
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

    private void deleteThisDevice() {
        if (bean.getHardData() != null) {
            if (bean.getHardData().isOwner()) {
                DialogUtil.showExtraSelectDialog(context, context.getString(R.string.msg_admin_remove_device),
                        context.getString(R.string.everyone), new DialogUtil.OnDialogButtonClickListener() {
                            @Override
                            public void onClick(View v, String strEdit, final Dialog dialog, boolean isCheck) {
                                loadingView.setVisibility(View.VISIBLE);
                                dialog.dismiss();
                                if (isCheck) {
                                    clearNasUser(dialog);
                                } else {
                                    clearDeviceUser(dialog);
                                }
                            }
                        },
                        context.getString(R.string.cancel), null
                        , context.getString(R.string.self), new DialogUtil.OnDialogButtonClickListener() {
                            @Override
                            public void onClick(View v, String strEdit, Dialog dialog, boolean isCheck) {
                                loadingView.setVisibility(View.VISIBLE);
                                dialog.dismiss();
                                if (isCheck) {
                                    deleteNasUser(dialog);
                                } else {
                                    deleteThisDevice(dialog);
                                }
                            }
                        }, context.getString(R.string.delete_personal_data));
            } else {
                DialogUtil.showSelectDialog(context, context.getString(R.string.unbind_device_prompt),
                        context.getString(R.string.confirm), new DialogUtil.OnDialogButtonClickListener() {
                            @Override
                            public void onClick(View v, String strEdit, final Dialog dialog, boolean isCheck) {
                                loadingView.setVisibility(View.VISIBLE);
                                dialog.dismiss();
                                if (isCheck) {
                                    deleteNasUser(dialog);
                                } else {
                                    deleteThisDevice(dialog);
                                }
                            }
                        },
                        context.getString(R.string.cancel), null, context.getString(R.string.delete_personal_data));
            }
        }
    }

    private void clearNasUser(final Dialog dialog) {
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
                                if (context != null) {
                                    String msg = HttpErrorNo.getResultMsg(true, errorNo, errorMsg);
                                    String s = context.getString(R.string.remove_device_failed);
                                    ToastHelper.showToast(String.format("%s! %s", s, msg));
                                }
                            }
                        };
                        if (SessionCache.Companion.getInstance().isV5(data.getId())) {
                            mListener.onSuccess("","");
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
        loadingView.setVisibility(View.GONE);
        DeviceUserUtil.deviceClearBinds(bean.getHardData().getDeviceid(), DeviceManagerDialogUtil.this,
                new MyOkHttpListener() {
                    @Override
                    public void success(Object tag, GsonBaseProtocol data) {
                        DevManager.getInstance().initHardWareList(null);
                        dialog.dismiss();
                        mngDialog.dismiss();
                    }
                });
    }

    private void deleteNasUser(Dialog dialog) {
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
                            if (SessionCache.Companion.getInstance().isV5(data.getId())) {
                                deleteThisDevice(dialog);
                            }else{
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

                            if (Objects.equals(cmd, "update")) {
                                deleteThisDevice(dialog);
                            }
                        } else
                            deleteThisDevice(dialog);
                    }

                    @Override
                    public void onFailure(String url, int errorNo, String errorMsg) {
                        loadingView.setVisibility(View.GONE);
                        if (errorNo == -40000 && Objects.equals("Delete system user failed", errorMsg)) {
                            deleteThisDevice(dialog);
                        } else if (context != null) {
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
                        manageAPI.delete(CMAPI.getInstance().getBaseInfo().getAccount());
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
                                CMAPI.getInstance().getBaseInfo().getAccount(), this);
                        return true;
                    }
                };
                V5Repository.Companion.INSTANCE().deleteUser(data.getId(), data.getIp(), LoginTokenUtil.getToken(),
                        CMAPI.getInstance().getBaseInfo().getAccount(), observer);

            }

            @Override
            public void onFailure(String url, int errorNo, String errorMsg) {
                loadingView.setVisibility(View.GONE);
                if (context != null) {
                    String msg = HttpErrorNo.getResultMsg(true, errorNo, errorMsg);
                    String s = context.getString(R.string.remove_device_failed);
                    ToastHelper.showToast(String.format("%s! (%s)", s, errorNo));
                }
            }
        });
    }

    private void deleteThisDevice(final Dialog dialog) {
        DeviceUserUtil.deleteThisDevice(bean.getHardData().getDeviceid(), DeviceManagerDialogUtil.this,
                new ListResultListener<UnbindDeviceResult>() {

                    @Override
                    public void error(Object tag, GsonBaseProtocol baseProtocol) {
                        loadingView.setVisibility(View.GONE);
                        ToastUtils.showError(baseProtocol.result);
                    }

                    @Override
                    public void success(Object tag, List<UnbindDeviceResult> results) {
                        loadingView.setVisibility(View.GONE);
                        DevManager.getInstance().initHardWareList(null);
                        dialog.dismiss();
                        mngDialog.dismiss();
                    }

                    @Override
                    public void error(Object tag, List<UnbindDeviceResult> mErrorResults) {
                        loadingView.setVisibility(View.GONE);
                    }
                });
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
                        mngDialog.dismiss();
                    }

                    @Override
                    public void onFailure(String url, int errorNo, String errorMsg) {
                        ToastUtils.showToast(HttpErrorNo.getResultMsg(false, errorNo, errorMsg));
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
}
