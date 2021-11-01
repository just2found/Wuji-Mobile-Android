package net.linkmate.app.ui.activity.mine;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import net.linkmate.app.R;
import net.linkmate.app.base.BaseActivity;
import net.linkmate.app.base.MyOkHttpListener;
import net.linkmate.app.manager.LoginManager;
import net.linkmate.app.manager.UserInfoManager;
import net.linkmate.app.ui.activity.LoginActivity;
import net.linkmate.app.util.DialogUtil;
import net.linkmate.app.util.ToastUtils;
import net.linkmate.app.view.ActivityItemLayout;
import net.linkmate.app.view.CusTextView;
import net.linkmate.app.view.TipsBar;
import net.sdvn.cmapi.CMAPI;
import net.sdvn.cmapi.global.Constants;
import net.sdvn.cmapi.protocal.ResultListener;
import net.sdvn.cmapi.util.CommonUtils;
import net.sdvn.common.ErrorCode;
import net.sdvn.common.Local;
import net.sdvn.common.internet.core.GsonBaseProtocol;
import net.sdvn.common.internet.core.HttpLoader;
import net.sdvn.common.internet.loader.SetNickHttpLoader;
import net.sdvn.common.internet.protocol.GetUserInfoResultBean;
import net.sdvn.nascommon.utils.AnimUtils;
import net.sdvn.nascommon.utils.ToastHelper;

import io.reactivex.disposables.Disposable;

public class AccountActivity extends BaseActivity implements HttpLoader.HttpLoaderStateListener {
    private ImageView itbIvLeft;
    private TextView itbTvTitle;
    private RelativeLayout itbRl;

    private SwipeRefreshLayout mSrlAccount;
    private ActivityItemLayout mAilAccount;
    private ActivityItemLayout mAilName;
    private ActivityItemLayout mAilModifyNick;
    private ActivityItemLayout mAilBindPhone;
    private ActivityItemLayout mAilModifyPassword;
    private ActivityItemLayout mQRCodeAccount;
    private String userName;
    private boolean isSwitch;
    private boolean isModifyName;
    @Nullable
    private String switchAccount;
    @Nullable
    private String pwd;
    private GetUserInfoResultBean.DataBean bean;
    private View mAccountAilName;
    private View mAccountAilModifyNick;
    private View mAccountAilBindPhone;
    private View mAccountAilModifyPassword;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);
        bindView();
        itbTvTitle.setText(R.string.title_account);
        itbTvTitle.setTextColor(getResources().getColor(R.color.title_text_color));
        itbIvLeft.setVisibility(View.VISIBLE);
        itbIvLeft.setImageResource(R.drawable.icon_return);

        itbIvLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        mSrlAccount.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshData();
            }
        });

        if (UserInfoManager.getInstance().isInitting()) {
            mSrlAccount.setRefreshing(true);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshData();
    }

    private void refreshData() {
        mSrlAccount.setRefreshing(true);
        //请求新数据
        UserInfoManager.getInstance().initUserInfo(new net.sdvn.common.internet.listener.ResultListener<GetUserInfoResultBean>() {
            @Override
            public void success(Object tag, GetUserInfoResultBean data) {
                initBeans();
                if (mSrlAccount != null)
                    mSrlAccount.setRefreshing(false);
            }

            @Override
            public void error(Object tag, GsonBaseProtocol baseProtocol) {
                if (mSrlAccount != null)
                    mSrlAccount.setRefreshing(false);
            }
        });
    }

    private void initBeans() {
        bean = UserInfoManager.getInstance().getUserInfoBean();
        if (bean != null) {
            mAilAccount.setTips(bean.loginname);
            userName = bean.nickname;
            String name;
            String firstname = TextUtils.isEmpty(bean.firstname) ? "" : bean.firstname;
            String lastname = TextUtils.isEmpty(bean.lastname) ? "" : bean.lastname;
//            if (!UiUtils.isEn()) {
//                name = lastname + firstname;
//            } else {
//                name = firstname + " " + lastname;
//            }
            name = Local.getLocalName(lastname, firstname);
            mAilName.setTips(name);
            mAilModifyNick.setTips(userName);
            if (!TextUtils.isEmpty(bean.phone)) {
                mAilBindPhone.setTips(bean.phone);
            }
        }
    }

    @Override
    protected View getTopView() {
        return itbRl;
    }

    @Nullable
    @Override
    protected TipsBar getTipsBar() {
        return findViewById(R.id.tipsBar);
    }

    @Override
    protected void onEstablished() {
        if (isSwitch) setStatus(LoadingStatus.LOGIN_AGIN);
        refreshData();
    }

    @Override
    public void onDisconnected() {
        if (isSwitch) {
            isSwitch = false;
            mSdvnStatusViewModel.toLogin(switchAccount,
                    pwd, new ResultListener() {
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
                                startActivity(new Intent(AccountActivity.this, LoginActivity.class));
                                overridePendingTransition(R.anim.slide_left_in, R.anim.slide_right_out);
                                finish();
                            }
                        }
                    });
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1 && resultCode == 1) {
            setStatus(LoadingStatus.LOGIN_AGIN);
            ToastUtils.showToast(R.string.modify_succ);
            //以切换账号的逻辑重新登陆，实际使用的是原账号，只为更新密码
            isSwitch = true;
            switchAccount = data.getStringExtra("account");
            pwd = data.getStringExtra("pwd");
            CMAPI.getInstance().disconnect();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.account_ail_name:
                editname();
                break;
            case R.id.account_ail_modify_nick:
                editnick();
                break;
            case R.id.account_ail_bind_phone:
//                if (bean == null || TextUtils.isEmpty(bean.phone)) {
                startActivity(new Intent(this, BindPhoneActivity.class));
//                }
                break;
            case R.id.account_ail_modify_password:
                startActivityForResult(new Intent(this, ModifyPwdActivity.class), 1);
                break;
        }
    }

    private void editname() {
        final View dialogView = View.inflate(this, R.layout.dialog_edit, null);
        final Dialog mDialog = new Dialog(this, R.style.DialogTheme);
        final TextView tvTitle = dialogView.findViewById(R.id.txt_title);
        final EditText etContent = dialogView.findViewById(R.id.et_content);
        final EditText etExtra = dialogView.findViewById(R.id.et_extra);
        final TextView tvContent = dialogView.findViewById(R.id.tv_content);
        final TextView tvExtra = dialogView.findViewById(R.id.tv_extra);
        tvTitle.setText(getString(R.string.rename));
        dialogView.findViewById(R.id.fl_content).setVisibility(View.VISIBLE);

        dialogView.findViewById(R.id.fl_extra).setVisibility(View.VISIBLE);

        etContent.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                tvContent.setVisibility(etContent.getText().length() > 0 ? View.GONE : View.VISIBLE);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        etExtra.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                tvExtra.setVisibility(etExtra.getText().length() > 0 ? View.GONE : View.VISIBLE);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        etContent.setText(bean.firstname);
        //光标定位到最后
        etContent.setSelection(etContent.getText().length());
        etContent.requestFocus();
        tvContent.setText(R.string.firstname);
        etExtra.setText(bean.lastname);
        tvExtra.setText(R.string.lastname);

        TextView positiveBtn = dialogView.findViewById(R.id.positive);
        positiveBtn.setText(getString(R.string.confirm));
        positiveBtn.setVisibility(View.VISIBLE);
        positiveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newFirst = etContent.getText().toString().trim();
                String newLast = etExtra.getText().toString().trim();
                if (TextUtils.isEmpty(newFirst) || newFirst.length() > 16) {
                    AnimUtils.sharkEditText(AccountActivity.this, etContent);
                } else if (TextUtils.isEmpty(newLast) || newLast.length() > 16) {
                    AnimUtils.sharkEditText(AccountActivity.this, etExtra);
                } else {
                    SetNickHttpLoader loader = new SetNickHttpLoader(GsonBaseProtocol.class);
                    String ticket = CMAPI.getInstance().getBaseInfo().getTicket();
                    if (CommonUtils.isEmpty(ticket) || !LoginManager.getInstance().isLogined()) {
                        ToastHelper.showLongToast(R.string.tip_wait_for_service_connect);
                        return;
                    }
                    loader.setParams(ticket, newFirst, newLast, null);
                    loader.executor(new MyOkHttpListener() {
                        @Override
                        public void success(Object tag, GsonBaseProtocol data) {
                            dismissLoading();
                            ToastUtils.showToast(R.string.modify_succ);
                            refreshData();
                        }

                        @Override
                        public void error(Object tag, GsonBaseProtocol baseProtocol) {
                            dismissLoading();
                            super.error(tag, baseProtocol);
                        }
                    });
                    showLoading(R.string.save_settings);
                    mDialog.dismiss();
                }
            }
        });

        TextView negativeBtn = dialogView.findViewById(R.id.negative);
        negativeBtn.setText(getString(R.string.cancel));
        negativeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDialog.dismiss();
            }
        });

        mDialog.setContentView(dialogView);
        mDialog.setCancelable(true);
        mDialog.setCanceledOnTouchOutside(true);
        mDialog.show();
    }

    private void editnick() {
        DialogUtil.showEditDialog(this, getString(R.string.modify_nick),
                TextUtils.isEmpty(userName) ? "" : userName.trim(), "",
                getString(R.string.confirm), new DialogUtil.OnDialogButtonClickListener() {
                    @Override
                    public void onClick(View v, final String newName, final Dialog dialog, boolean isCheck) {
                        if (TextUtils.isEmpty(newName) || newName.length() > 24) {
                            AnimUtils.sharkEditText(AccountActivity.this, v);
                        } else {
                            SetNickHttpLoader loader = new SetNickHttpLoader(GsonBaseProtocol.class);
                            String ticket = CMAPI.getInstance().getBaseInfo().getTicket();
                            if (CommonUtils.isEmpty(ticket) || !LoginManager.getInstance().isLogined()) {
                                ToastHelper.showLongToast(R.string.tip_wait_for_service_connect);
                                return;
                            }
                            loader.setParams(ticket, null, null, newName);
                            loader.executor(new MyOkHttpListener() {
                                @Override
                                public void success(Object tag, GsonBaseProtocol data) {
                                    dismissLoading();
                                    ToastUtils.showToast(R.string.modify_succ);
                                    refreshData();
                                }

                                @Override
                                public void error(Object tag, GsonBaseProtocol baseProtocol) {
                                    dismissLoading();
                                    super.error(tag, baseProtocol);
                                }
                            });
                            showLoading(R.string.save_settings);
                            dialog.dismiss();
                        }
                    }
                },
                getString(R.string.cancel), null);
    }

    @Override
    public void onLoadStart(Disposable disposable) {
        addDisposable(disposable);
        showLoading();
    }

    @Override
    public void onLoadComplete() {
        dismissLoading();
    }

    @Override
    public void onLoadError() {
        dismissLoading();
    }

    private void bindView() {
        itbIvLeft = findViewById(R.id.itb_iv_left);
        itbTvTitle = findViewById(R.id.itb_tv_title);
        itbRl = findViewById(R.id.itb_rl);
        mSrlAccount = findViewById(R.id.account_srl);
        mAilAccount = findViewById(R.id.account_ail_account);
        mAilName = findViewById(R.id.account_ail_name);
        mAilModifyNick = findViewById(R.id.account_ail_modify_nick);
        mAilBindPhone = findViewById(R.id.account_ail_bind_phone);
        mAilModifyPassword = findViewById(R.id.account_ail_modify_password);
        mAccountAilName = findViewById(R.id.account_ail_name);
        mAccountAilModifyNick = findViewById(R.id.account_ail_modify_nick);
        mAccountAilBindPhone = findViewById(R.id.account_ail_bind_phone);
        mAccountAilModifyPassword = findViewById(R.id.account_ail_modify_password);

        //我的标识码
        mQRCodeAccount = findViewById(R.id.account_qrCode);
        CusTextView tvQrCodeTips = mQRCodeAccount.getTvTips();
        tvQrCodeTips.setVisibility(View.VISIBLE);
        tvQrCodeTips.setEndDrawable(getDrawable(R.drawable.icon_qr_code));

        mAccountAilName.setOnClickListener(v -> {
            onViewClicked(v);
        });
        mAccountAilModifyNick.setOnClickListener(v -> {
            onViewClicked(v);
        });
        mAccountAilBindPhone.setOnClickListener(v -> {
            onViewClicked(v);
        });
        mAccountAilModifyPassword.setOnClickListener(v -> {
            onViewClicked(v);
        });
        mQRCodeAccount.setOnClickListener(v -> {
            IdentifyCodeActivity.Companion.startMyIdCode(this);
        });
    }
}
