package net.linkmate.app.util.business;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
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

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import net.linkmate.app.R;
import net.linkmate.app.bean.DeviceBean;
import net.linkmate.app.bean.VNodeBean;
import net.linkmate.app.manager.DevManager;
import net.linkmate.app.util.FormatUtils;
import net.linkmate.app.util.ToastUtils;
import net.linkmate.app.view.DataItemLayout;
import net.linkmate.app.view.adapter.VNodeRVAdapter;
import net.sdvn.cmapi.CMAPI;
import net.sdvn.cmapi.Device;
import net.sdvn.cmapi.util.ClipboardUtils;
import net.sdvn.common.internet.core.GsonBaseProtocol;
import net.sdvn.common.internet.core.HttpLoader;
import net.sdvn.common.internet.listener.ResultListener;
import net.sdvn.common.internet.protocol.AccountPrivilegeInfo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import io.reactivex.disposables.Disposable;

public class DeviceVNodeDialogUtil implements HttpLoader.HttpLoaderStateListener {

    private final Context context;
    private final DeviceBean bean;
    private final int position;
    private VNodeRVAdapter adapter;

    public DeviceVNodeDialogUtil(Context context, DeviceBean bean, int position) {
        this.context = context;
        this.bean = bean;
        this.position = position;
    }

    private Dialog detailDialog;
    private View sv;
    private View progressLayout;
    private Switch switchSnEnable;
    private View loadingView;
    private View vNodeBtn;

    private DataItemLayout dilName;
    private DataItemLayout dilVip;
    private DataItemLayout dilLip;
    private DataItemLayout dilOwner;
    private DataItemLayout dilVersion;
    private DataItemLayout dilDomain;

    private DataItemLayout dilTime;
    private DataItemLayout dilTotal;
    private DataItemLayout dilRemaining;

    //subnet view
    private View vnodeLayout;
    private View vnodeBack;
    private RecyclerView vnodeRv;


    //sn开关
    private boolean snInternetEnable;
    private boolean snDnsEnable;
    private boolean snSubnetEnable;

    private int dialogLevel;

    private boolean onBackPress() {
        return onBackPress(sv, vnodeLayout);
    }

    private boolean onBackPress(View visibleView, View... goneViews) {
        if (dialogLevel <= 0 || detailDialog == null || !detailDialog.isShowing()) {
            dialogLevel = 0;
            return false;
        } else {
            if (dialogLevel == 2) {
                for (View view : goneViews) {
                    if (view != null)
                        view.setVisibility(View.GONE);
                }
                if (visibleView != null)
                    visibleView.setVisibility(View.VISIBLE);
            } else if (dialogLevel == 1) {
                detailDialog.dismiss();
            }
            dialogLevel--;
            return true;
        }
    }

    private View.OnClickListener mlistener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.device_des_btn_vnode:
                    sv.setVisibility(View.GONE);
                    refreshSubnode();
                    vnodeLayout.setVisibility(View.VISIBLE);
                    dialogLevel++;
                    break;
                case R.id.lv_iv_back:
                    onBackPress();
                    break;
            }
        }
    };

    public void showDetailDialog() {
        if (detailDialog != null && detailDialog.isShowing()) {
            return;
        }
        final View view = LayoutInflater.from(context).inflate(R.layout.layout_dialog_device_des, null);
        detailDialog = new AppCompatDialog(context, R.style.DialogTheme);
        detailDialog.setContentView(view);

        initView(view);

        dilName.setText(bean.getName());
        List<String> vnodeid = new ArrayList<>();
        vnodeid.add(bean.getId());
        PrivilegeUtil.getVNodeInfo(vnodeid, this, new ResultListener<AccountPrivilegeInfo>() {
            @Override
            public void success(Object tag, AccountPrivilegeInfo data) {
                AccountPrivilegeInfo.VnodesBean vnodesBean = data.data.vnodes.get(0);
                dilTime.setText(VIPDialogUtil.getDateString(vnodesBean.getExpired()));
                boolean isBit = false;
                if (vnodesBean.getUnits() != null) {
                    if (vnodesBean.getUnits().endsWith("b")) {
                        isBit = true;
                    }
                    dilTotal.setText(FormatUtils.getSizeFormat(vnodesBean.getFlowUsable(), isBit));
                    dilRemaining.setText(FormatUtils.getSizeFormat(vnodesBean.getFlowUsed(), isBit));
                }
            }

            @Override
            public void error(Object tag, GsonBaseProtocol baseProtocol) {

            }
        });

        switchSnEnable.setVisibility(View.VISIBLE);
        switchSnEnable.setChecked(CMAPI.getInstance().getBaseInfo().hadSelectedSn(bean.getId()));
        switchSnEnable.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
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
                if ((isChecked && !CMAPI.getInstance().getBaseInfo().hadSelectedSn(bean.getId()))
                        || (!isChecked && CMAPI.getInstance().getBaseInfo().hadSelectedSn(bean.getId())))

                    if (CMAPI.getInstance().getBaseInfo().hadSelectedSn(bean.getId())) {
                        if (CMAPI.getInstance().getConfig().isNetBlock()
                                && CMAPI.getInstance().getBaseInfo().getSnIds().size() == 1) {
                            new AlertDialog.Builder(context)
                                    .setMessage(R.string.tips_internet_access_sn_cancel)
                                    .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            runnable.run();
                                            if (CMAPI.getInstance().removeSmartNode(bean.getId())) {
                                                switchSnEnable.setChecked(false);
                                                dialog.dismiss();
                                                DevManager.getInstance().notifyDeviceStateChanged();
                                            }
                                        }
                                    })
                                    .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                        }
                                    })
                                    .show();
                        } else {
                            if (CMAPI.getInstance().removeSmartNode(bean.getId())) {
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

        dilVip.setVisibility(View.GONE);
        dilLip.setVisibility(View.GONE);
        dilVersion.setVisibility(View.GONE);
        dilDomain.setVisibility(View.GONE);
        dilOwner.setVisibility(View.GONE);
        dilTime.setVisibility(View.VISIBLE);
        dilTotal.setVisibility(View.VISIBLE);
        dilRemaining.setVisibility(View.VISIBLE);
        vNodeBtn.setVisibility(View.VISIBLE);

        detailDialog.show();
        sv.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                DisplayMetrics metrics = context.getResources().getDisplayMetrics();
                final Window window = detailDialog.getWindow();
                if (window != null) {
                    WindowManager.LayoutParams params = window.getAttributes();
                    params.width = (int) (metrics.widthPixels * 0.80);
                    window.setAttributes(params);
                    sv.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                }
            }
        });
        dialogLevel = 1;
    }

    private void initView(View view) {
        sv = view.findViewById(R.id.device_des_sv);
        switchSnEnable = view.findViewById(R.id.switch_sn_enable);
        loadingView = view.findViewById(R.id.layout_loading);
        vNodeBtn = view.findViewById(R.id.device_des_btn_vnode);

        view.findViewById(R.id.device_des_iv_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                detailDialog.dismiss();
            }
        });

        dilName = view.findViewById(R.id.des_dil_name);
        dilVip = view.findViewById(R.id.des_dil_vip);
        dilLip = view.findViewById(R.id.des_dil_lip);
        dilOwner = view.findViewById(R.id.des_dil_owner);
        dilVersion = view.findViewById(R.id.des_dil_version);
        dilDomain = view.findViewById(R.id.des_dil_domain);
        dilTime = view.findViewById(R.id.des_dil_time);
        dilTotal = view.findViewById(R.id.des_dil_total_traffic);
        dilRemaining = view.findViewById(R.id.des_dil_remaining_traffic);
        vnodeLayout = view.findViewById(R.id.layout_vnode);
        vnodeBack = view.findViewById(R.id.lv_iv_back);
        vnodeRv = view.findViewById(R.id.lv_rv);

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
                case R.id.des_dil_time:
                    clipString(dilTime.mTvData.getText().toString().trim());
                case R.id.des_dil_total_traffic:
                    clipString(dilTotal.mTvData.getText().toString().trim());
                    break;
                case R.id.des_dil_remaining_traffic:
                    clipString(dilRemaining.mTvData.getText().toString().trim());
                    break;
            }
        };
        dilName.setDataOnClickListener(listener);
        dilTime.setDataOnClickListener(listener);
        dilTotal.setDataOnClickListener(listener);
        dilRemaining.setDataOnClickListener(listener);
        vNodeBtn.setOnClickListener(mlistener);
        vnodeBack.setOnClickListener(mlistener);

        adapter = new VNodeRVAdapter(new ArrayList<>());
        View emptyView = LayoutInflater.from(context).inflate(R.layout.pager_empty_text, null);
        ((TextView) emptyView.findViewById(R.id.tv_tips)).setText(R.string.tips_no_dev);
        adapter.setEmptyView(emptyView);
        vnodeRv.setLayoutManager(new LinearLayoutManager(context, RecyclerView.VERTICAL, false));
        vnodeRv.setItemAnimator(null);
        vnodeRv.setAdapter(adapter);
    }

    private void refreshSubnode() {
        CMAPI.getInstance().refreshBaseInfo();
        // TODO: 2019/01/20  vnode test
        CMAPI.getInstance().refreshDevices();
        List<VNodeBean> beans = new ArrayList<>();
        List<Device.VNode> vNodes = new ArrayList<>(bean.vNode);
        Collections.sort(vNodes, new Comparator<Device.VNode>() {
            @Override
            public int compare(Device.VNode o1, Device.VNode o2) {
                if (o1.needPaid == o2.needPaid)
                    return o1.groupName.compareTo(o2.groupName);
                else if (o1.needPaid)
                    return -1;
                else
                    return 1;
            }
        });
        for (Device.VNode vNode : vNodes) {
            if (vNode.deviceIds.size() <= 0)
                continue;
            VNodeBean title = new VNodeBean();
            title.setName(vNode.groupName);
            title.setType(-1);
            beans.add(title);
            String usableSnid = CMAPI.getInstance().getBaseInfo().getUsableSnid();
            for (Device.VNode.GroupDevices gd : vNode.deviceIds) {
                VNodeBean bean = new VNodeBean();
                if (Objects.equals(usableSnid, gd.id)) {
                    bean.setSelected(true);
                }
                if (!TextUtils.isEmpty(gd.name)) {
                    bean.setName(gd.name);
                } else
                    for (Device device : CMAPI.getInstance().getDevices()) {
                        if (Objects.equals(device.getId(), gd.id)) {
                            bean.setName(device.getName());
                            bean.setOnline(true);
                            break;
                        }
                    }
                bean.setType(0);
                beans.add(bean);
            }
        }
        adapter.setNewData(beans);
    }

    private void clipString(String content) {
        if (context != null) {
            ClipboardUtils.copyToClipboard(context, content);
            ToastUtils.showToast(context.getString(R.string.Copied) + content);
        }
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
