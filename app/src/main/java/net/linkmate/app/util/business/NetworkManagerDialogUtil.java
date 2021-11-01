package net.linkmate.app.util.business;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import net.linkmate.app.R;
import net.linkmate.app.base.MyConstants;
import net.linkmate.app.base.MyOkHttpListener;
import net.linkmate.app.manager.MessageManager;
import net.linkmate.app.util.DialogUtil;
import net.linkmate.app.util.ToastUtils;
import net.linkmate.app.view.DataItemLayout;
import net.linkmate.app.view.adapter.NetUserMngRVAdapter;
import net.sdvn.cmapi.CMAPI;
import net.sdvn.cmapi.Network;
import net.sdvn.cmapi.util.ClipboardUtils;
import net.sdvn.common.internet.core.GsonBaseProtocol;
import net.sdvn.common.internet.core.HttpLoader;
import net.sdvn.common.internet.listener.ResultListener;
import net.sdvn.common.internet.loader.ModifyNetWorkHttpLoader;
import net.sdvn.common.internet.protocol.NetMembersList;
import net.sdvn.common.internet.protocol.ShareCode;
import net.sdvn.common.internet.protocol.entity.NetMember;
import net.sdvn.nascommon.utils.AnimUtils;
import net.sdvn.nascommon.utils.ToastHelper;
import net.sdvn.nascommon.utils.Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import io.reactivex.disposables.Disposable;

public class NetworkManagerDialogUtil implements HttpLoader.HttpLoaderStateListener {

    private final Context context;
    private final Network bean;
    private final int position;

    public NetworkManagerDialogUtil(Context context, Network bean, int position) {
        this.context = context;
        this.bean = bean;
        this.position = position;
    }

    private Dialog mngDialog;
    private View sv;
    private View btnDelete;
    @NonNull
    private View loadingView;

    private DataItemLayout dilName;
    private DataItemLayout dilId;
    private DataItemLayout dilOwner;

    //share view
    private View share;
    private View shareLayout;
    private View shareBack;
    private Switch switchShare;
    private ImageView imgShareQR;
    private TextView tvShareCode;
    private TextView tvShareTips;
    private View shareImageContainer;
    private Switch switchShareNeedAuth;
    private Button shareBtn;

    //invite view
    private View invite;

    //user manager view
    private View userMng;
    private View userMngLayout;
    private View userMngBack;
    private TextView userMngCheckAll;
    private RecyclerView lumRv;
    private NetUserMngRVAdapter userMngAdapter;
    private View userMngDelete;

    private int dialogLevel;

    private boolean onBackPress() {
        return onBackPress(sv, shareLayout, userMngLayout);
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
                return true;
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
                case R.id.net_nmg_des_btn_share:
                    sv.setVisibility(View.GONE);
                    shareLayout.setVisibility(View.VISIBLE);
                    dialogLevel++;
                    initShareViewDate();
                    break;
                case R.id.lsc_iv_back:
                    onBackPress();
                    break;
                case R.id.net_nmg_des_btn_invite:
                    invite();
                    break;
                case R.id.net_nmg_des_btn_user_mng:
                    sv.setVisibility(View.GONE);
                    userMngLayout.setVisibility(View.VISIBLE);
                    dialogLevel++;
                    initUserMngData();
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
                case R.id.net_nmg_des_btn_delete:
                    deleteThisNet();
                    break;
                case R.id.net_iv_back:
                    onBackPress();
                    break;
            }
        }
    };

    public void showDetailDialog() {
        if (mngDialog != null && mngDialog.isShowing()) {
            return;
        }
        final View view = LayoutInflater.from(context).inflate(R.layout.layout_dialog_network_manage, null);
        mngDialog = new Dialog(context, R.style.DialogTheme);
        mngDialog.setContentView(view);
        initView(view);

        View viewById = view.findViewById(R.id.net_iv_back);
        viewById.setOnClickListener(listener);

        dilName.setText(bean.getName());
        dilId.setText(bean.getId());
        dilOwner.setText(bean.getOwner());

        dilName.mIv.setVisibility(View.GONE);
        //管理权限
        if (isAdmin()) {
            dilName.mIv.setVisibility(View.VISIBLE);
            dilName.mIv.setImageResource(R.drawable.icon_edit);
            dilName.mIv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    rename();
                }
            });

            //分享
            initShareView(view);

            //邀请
            initInviteView(view);

            //用户管理
            initUserView(view);
        }

        btnDelete.setVisibility(View.VISIBLE);
        btnDelete.setOnClickListener(listener);


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

    private boolean isAdmin() {
        return isAdmin(bean.getUid());
    }

    private boolean isAdmin(String userid) {
        return Objects.equals(CMAPI.getInstance().getBaseInfo().getUserId(), userid);
    }

    private void initView(View view) {
        sv = view.findViewById(R.id.net_nmg_des_sv);
        btnDelete = view.findViewById(R.id.net_nmg_des_btn_delete);
        loadingView = view.findViewById(R.id.layout_loading);

        dilName = view.findViewById(R.id.net_nmg_des_dil_name);
        dilId = view.findViewById(R.id.net_nmg_des_dil_id);
        dilOwner = view.findViewById(R.id.net_nmg_des_dil_owner);

//        view.setFocusableInTouchMode(true);
        view.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
//                if (v.getId() == R.id.net_iv_back) {
//                    return false;
//                }
                if (event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_BACK) {
                    return onBackPress();
                }
                return false;
            }
        });
        View.OnClickListener listener = v -> {
            switch (v.getId()) {
                case R.id.net_nmg_des_dil_name:
                    clipString(dilName.mTvData.getText().toString().trim());
                    break;
                case R.id.net_nmg_des_dil_id:
                    clipString(dilId.mTvData.getText().toString().trim());
                    break;
                case R.id.net_nmg_des_dil_owner:
                    clipString(dilOwner.mTvData.getText().toString().trim());
                    break;
            }
        };
        dilName.setDataOnClickListener(listener);
        dilId.setDataOnClickListener(listener);
        dilOwner.setDataOnClickListener(listener);
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
                            ModifyNetWorkHttpLoader loader = new ModifyNetWorkHttpLoader(GsonBaseProtocol.class);
                            loader.setHttpLoaderStateListener(NetworkManagerDialogUtil.this);
                            loader.setParams(bean.getId(), strEdit);
                            loader.executor(new MyOkHttpListener() {

                                @Override
                                public void success(Object tag, GsonBaseProtocol gsonBaseProtocol) {
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

    private void initShareView(View view) {
        share = view.findViewById(R.id.net_nmg_des_btn_share);
        shareLayout = view.findViewById(R.id.layout_share_code);
        shareBack = view.findViewById(R.id.lsc_iv_back);
        switchShare = view.findViewById(R.id.lsc_switch_share);
        shareImageContainer = view.findViewById(R.id.lsc_container);
        imgShareQR = view.findViewById(R.id.lsc_iv_qr);
        tvShareCode = view.findViewById(R.id.lsc_tv_share_code);
        tvShareTips = view.findViewById(R.id.lsc_tv_tips);
        switchShareNeedAuth = view.findViewById(R.id.lsc_switch_share_need_Auth);
        shareBtn = view.findViewById(R.id.lsc_btn_share);

        switchShare.setVisibility(View.GONE);
        switchShareNeedAuth.setVisibility(View.GONE);

        share.setVisibility(View.VISIBLE);
        share.setOnClickListener(listener);
        shareBack.setOnClickListener(listener);

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
        //获取分享码
        ShareUtil.showNetworkShareCode(bean.getId(), this,
                new ResultListener<ShareCode>() {
                    @Override
                    public void success(Object tag, final ShareCode data) {
                        //生成二维码
                        shareCode = data.sharecode;
                        ShareUtil.generateQRCode(imgShareQR, MyConstants.EVENT_CODE_NETWORK, shareCode,
                                new ShareUtil.QRCodeResult() {
                                    @Override
                                    public void onGenerated(Bitmap bitmap, String tips) {
                                        shareImageContainer.setVisibility(View.VISIBLE);
                                        shareBtn.setEnabled(true);
                                        imgShareQR.setImageBitmap(bitmap);
                                        tvShareCode.setText(shareCode);
                                        tvShareTips.setText(tips);
                                        MessageManager.getInstance().quickDelay();
                                    }
                                });
                    }

                    @Override
                    public void error(Object tag, GsonBaseProtocol baseProtocol) {
                        shareImageContainer.setVisibility(View.GONE);
                        shareBtn.setEnabled(false);
                        ToastUtils.showError(baseProtocol.result);
                    }
                });
    }

    private void initInviteView(View view) {
        invite = view.findViewById(R.id.net_nmg_des_btn_invite);
        invite.setVisibility(View.VISIBLE);
        invite.setOnClickListener(listener);
    }

    private void invite() {
        DialogUtil.showEditDialog(context, context.getString(R.string.invite), "",
                context.getString(R.string.pls_input_invite_username),
                context.getString(R.string.confirm), new DialogUtil.OnDialogButtonClickListener() {
                    @Override
                    public void onClick(View v, final String strEdit, final Dialog dialog, boolean isCheck) {
                        if (TextUtils.isEmpty(strEdit)) {
                            AnimUtils.sharkEditText(context, v);
                        } else {
                            NetManagerUtil.addNetMember(bean.getId(), strEdit, NetworkManagerDialogUtil.this,
                                    new MyOkHttpListener() {
                                        @Override
                                        public void success(Object tag, GsonBaseProtocol data) {
                                            ToastUtils.showToast(R.string.wait_for_consent);
                                            dialog.dismiss();
                                        }
                                    });
                        }
                    }
                },
                context.getString(R.string.cancel), null);
    }

    private void initUserView(View view) {
        userMng = view.findViewById(R.id.net_nmg_des_btn_user_mng);
        userMngLayout = view.findViewById(R.id.layout_user_mng);
        userMngBack = view.findViewById(R.id.lum_iv_back);
        userMngCheckAll = view.findViewById(R.id.lum_btn_check_all);
        lumRv = view.findViewById(R.id.lum_rv);
        userMngDelete = view.findViewById(R.id.lum_btn_cancel_share);
        userMngDelete.setVisibility(View.VISIBLE);
        userMng.setVisibility(View.VISIBLE);
        userMng.setOnClickListener(listener);
        userMngBack.setOnClickListener(listener);
        userMngCheckAll.setOnClickListener(listener);
        userMngDelete.setOnClickListener(listener);
        initUserMngLayout(context);
    }

    private void initUserMngLayout(Context context) {
        userMngAdapter = new NetUserMngRVAdapter();
        lumRv.setLayoutManager(new LinearLayoutManager(context, RecyclerView.VERTICAL, false));
        lumRv.setItemAnimator(null);
        lumRv.setAdapter(userMngAdapter);
    }

    private void initUserMngData() {
        NetManagerUtil.getMembers(bean.getId(), NetworkManagerDialogUtil.this,
                new MyOkHttpListener<NetMembersList>() {
                    @Override
                    public void success(Object tag, NetMembersList data) {
                        isCheckAll = false;
                        userMngCheckAll.setText(R.string.check_all);
                        if (data != null && data.data != null && data.data.members != null) {
                            Collections.sort(data.data.members, new Comparator<NetMember>() {
                                @Override
                                public int compare(NetMember o1, NetMember o2) {
                                    if (isAdmin(o1.userid))
                                        return -1;
                                    if (isAdmin(o2.userid))
                                        return 1;
                                    return o1.userid.compareTo(o2.userid);
                                }
                            });
                        }
                        userMngAdapter.setNewData(data.data.members);
                    }
                });
    }

    private boolean isCheckAll;

    private void checkAllUsers() {
        isCheckAll = !isCheckAll;
        if (isCheckAll) {
            for (NetMember user : userMngAdapter.getData()) {
                if (!isAdmin(user.userid))
                    user.isSelected = true;
            }
            userMngCheckAll.setText(R.string.cancel);
        } else {
            for (NetMember user : userMngAdapter.getData()) {
                if (!isAdmin(user.userid))
                    user.isSelected = false;
            }
            userMngCheckAll.setText(R.string.check_all);
        }
        userMngAdapter.notifyDataSetChanged();
    }

    private void deleteUsers() {
        final List<String> userids = new ArrayList<>();
        for (NetMember user : userMngAdapter.getData()) {
            if (user.isSelected)
                userids.add(user.userid);
        }
        if (userids.size() > 0) {
            DialogUtil.showSelectDialog(context, context.getString(R.string.cancel_share_to_these_users),
                    context.getString(R.string.yes), new DialogUtil.OnDialogButtonClickListener() {
                        @Override
                        public void onClick(View v, String strEdit, final Dialog dialog, boolean isCheck) {
                            dialog.dismiss();
                            NetManagerUtil.removeMembers(bean.getId(), userids, NetworkManagerDialogUtil.this,
                                    new MyOkHttpListener() {
                                        @Override
                                        public void success(Object tag, GsonBaseProtocol data) {
                                            initUserMngData();
                                            ToastHelper.showLongToastSafe(R.string.remove_success);
                                        }
                                    });
                        }
                    },
                    context.getString(R.string.no), null);
        } else {
            ToastHelper.showLongToastSafe(R.string.pls_notify_select_one);
        }
    }

    private void deleteThisNet() {
        DialogUtil.showSelectDialog(context, context.getString(R.string.unbind_network_prompt),
                context.getString(R.string.yes), new DialogUtil.OnDialogButtonClickListener() {
                    @Override
                    public void onClick(View v, String strEdit, final Dialog dialog, boolean isCheck) {
                        deleteThisNet(dialog);
                    }
                },
                context.getString(R.string.no), null);
    }

    private void deleteThisNet(final Dialog dialog) {
        if (isAdmin()) {
            NetManagerUtil.removeNet(bean.getId(), NetworkManagerDialogUtil.this,
                    new MyOkHttpListener() {
                        @Override
                        public void success(Object tag, GsonBaseProtocol data) {
                            dialog.dismiss();
                            mngDialog.dismiss();
                            ToastHelper.showLongToastSafe(R.string.remove_success);
                        }
                    });
        } else {
            List<String> userids = new ArrayList<>();
            userids.add(CMAPI.getInstance().getBaseInfo().getUserId());
            NetManagerUtil.removeMembers(bean.getId(), userids, NetworkManagerDialogUtil.this,
                    new MyOkHttpListener() {
                        @Override
                        public void success(Object tag, GsonBaseProtocol data) {
                            dialog.dismiss();
                            mngDialog.dismiss();
                            ToastHelper.showLongToastSafe(R.string.remove_success);
                        }
                    });
        }
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
