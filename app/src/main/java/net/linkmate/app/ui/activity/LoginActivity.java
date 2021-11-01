package net.linkmate.app.ui.activity;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.chad.library.adapter.base.BaseQuickAdapter;

import net.linkmate.app.BuildConfig;
import net.linkmate.app.R;
import net.linkmate.app.base.BaseActivity;
import net.linkmate.app.base.MyConstants;
import net.linkmate.app.net.RetrofitSingleton;
import net.linkmate.app.poster.database.AppDatabase;
import net.linkmate.app.poster.database.UserDao;
import net.linkmate.app.poster.model.UserModel;
import net.linkmate.app.util.DialogUtil;
import net.linkmate.app.util.NetworkUtils;
import net.linkmate.app.util.ToastUtils;
import net.linkmate.app.util.UIUtils;
import net.linkmate.app.view.TipsBar;
import net.linkmate.app.view.adapter.LoginUsersRVAdapter;
import net.sdvn.cmapi.BaseInfo;
import net.sdvn.cmapi.CMAPI;
import net.sdvn.cmapi.global.Constants;
import net.sdvn.cmapi.protocal.ResultListener;
import net.sdvn.common.ErrorCode;
import net.sdvn.nascommon.constant.AppConstants;
import net.sdvn.nascommon.model.UiUtils;
import net.sdvn.nascommon.utils.AnimUtils;
import net.sdvn.nascommon.utils.InputMethodUtils;
import net.sdvn.nascommon.utils.Utils;
import net.sdvn.nascommon.widget.CheckableImageButton;

import org.view.libwidget.MagicTextViewUtil;

import java.util.List;
import java.util.Objects;

import io.weline.repo.SessionCache;
import kotlin.Unit;

import static net.linkmate.app.util.ToastUtils.showToast;

public class LoginActivity extends BaseActivity {

    private Button loginBtnLogin;
    private ImageView loginImgBack;
    private EditText mETAccount;
    private EditText mETPassword;
    private EditText loginEtCode;
    private TextView loginBtnTvCode;
    private TextView loginTvForget;
    private TextView loginTvRegister;
    private final String EmptyPassword = "WELINE%PASSBY";

    private LinearLayout llContent;
    private LinearLayout llUser;
    private ImageView ivClearAccount;
    private ImageView ivSelectAccount;
    private PopupWindow popView;
    private List<String> mUsers;
    private String mAccount;
    private String mPasswd;
    private View mLoginBtnLogin;
    private View pwdVisible;
    @Nullable
    private CheckableImageButton checkableImageButton;


    private void initView() {
        ((TextView) findViewById(R.id.tv_version_name)).setText(BuildConfig.VERSION_NAME);
        TextView tvSubAgreement = findViewById(R.id.tv_sub_agreement);
        String agreeAgreement = getString(R.string.agree_user_agreement);
        String agreement = getString(R.string.sub_agreement);
        String privatePolicy = getString(R.string.privacy_policy);
        int color = getResources().getColor(R.color.link_blue);
//        //通过TextView里面的类html标签来实现显示效果
//        String text = "<font color='#f9f9f9'>%s</font><font color='blue'><a href='%s'>%s</a></font>" +
//                "<font color='#f9f9f9'> & </font><font color='blue'><a href='%s'>%s</a></font>";
//
//        tvSubAgreement.setText(Html.fromHtml(String.format(text, agreeAgreement, agreementUrl,
//                agreement, privatePolicyUrl, privatePolicy)));
//        //设置鼠标移动事件，产生链接显示,没有这句话，进不去
//        tvSubAgreement.setMovementMethod(LinkMovementMethod.getInstance());
        if (tvSubAgreement != null) {
            MagicTextViewUtil.Companion.getInstance(tvSubAgreement)
                    .append(agreeAgreement)
                    .append(agreement, color, true, s ->
                            showTermsAndConditions()
                    )
                    .append("&")
                    .append(privatePolicy, color, true, s ->
                            showPrivacyPolicy())
                    .append(".")
                    .show();
        }
    }

    private Unit showPrivacyPolicy() {
        String privacy_policy = getString(R.string.privacy_policy);
        String privacy_policy_url = MyConstants.getPrivacyUrlByLanguage(this);//getString(R.string.privacy_policy_url);
        WebViewActivity.open(this, privacy_policy, privacy_policy_url);
        return null;
    }

    private Unit showTermsAndConditions() {
        String agreement = getString(R.string.sub_agreement);
        String agreementUrl = MyConstants.getAgreementUrlByLanguage(this);//getString(R.string.subscriber_agreement_url);
        WebViewActivity.open(this, agreement, agreementUrl);
        return null;
    }

    private void initEvent() {
        mETAccount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                findViewById(R.id.login_tv_user).setVisibility(mETAccount.getText().length() > 0 ? View.GONE : View.VISIBLE);
                initClearBtn();
                lastSavePassword = null;
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        mETPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                findViewById(R.id.login_tv_pw).setVisibility(mETPassword.getText().length() > 0 ? View.GONE : View.VISIBLE);
                pwdVisible.setVisibility(mETPassword.getText().length() > 0 ? View.VISIBLE : View.GONE);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        mETPassword.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                login();
                return true;
            }
            return false;
        });
        pwdVisible.setOnClickListener(v -> {
            UIUtils.togglePasswordStatus(v, mETPassword);
            if (Objects.equals(mETPassword.getText().toString(), EmptyPassword)) {
                mETPassword.setText("");
            }
        });
//        if (checkableImageButton != null) {
//            loginBtnEnable();
//            checkableImageButton.setOnCheckedChangeListener((buttonView, isChecked) -> {
//                loginBtnLogin.setEnabled(isChecked);
//            });
//        }
    }

    private void initClearBtn() {
        if (TextUtils.isEmpty(mETAccount.getText().toString().trim())) {
            ivClearAccount.setVisibility(View.GONE);
        } else {
            ivClearAccount.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onStart() {
        loginBtnEnable();
        setStatus(LoadingStatus.LOGIN);
        super.onStart();
        initData();
    }

    private void loginBtnEnable() {
       // loginBtnLogin.setEnabled(true);
      //  loginBtnLogin.setEnabled(checkableImageButton == null || checkableImageButton.isChecked());
    }

    private View mLoginImgBack;


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Nullable
    @Override
    protected TipsBar getTipsBar() {
        return null;
    }

    private View mLoginBtnTvCode;

    private void showUserDropdown() {
        View contentView = LayoutInflater.from(this).inflate(R.layout.popup_login_users, null, false);

        final PopupWindow popupWindow = new PopupWindow(contentView, llUser.getWidth(), LinearLayout.LayoutParams.WRAP_CONTENT, true);
        popupWindow.setOutsideTouchable(true);
        popupWindow.setTouchable(true);

        RecyclerView rv = contentView.findViewById(R.id.pop_account_rv);
        LoginUsersRVAdapter adapter = new LoginUsersRVAdapter(mUsers);
        adapter.setOnItemChildClickListener(new BaseQuickAdapter.OnItemChildClickListener() {
            @Override
            public void onItemChildClick(BaseQuickAdapter adapter, View view, int position) {
                if (view.getId() == R.id.item_account_ib_delete) {
                    DialogUtil.showSelectDialog(LoginActivity.this, getString(R.string.delete_this_account),
                            getString(R.string.ok), new DialogUtil.OnDialogButtonClickListener() {
                                @Override
                                public void onClick(View v, String strEdit, Dialog dialog, boolean isCheck) {
                                    String deleteName = mUsers.get(position);
                                    String account = CMAPI.getInstance().getBaseInfo().getAccount();
                                    CMAPI.getInstance().removeUser(deleteName);
                                    if (Objects.equals(deleteName, account)) {
                                        mETAccount.setText("");
                                        mETPassword.setText("");
                                    }
                                    initData();
                                    dialog.dismiss();
                                    popupWindow.dismiss();
                                    mETAccount.requestFocus();
                                }
                            },
                            getString(R.string.cancel), null);
                } else if (view.getId() == R.id.item_account_tv_id) {
                    mETAccount.setText(mUsers.get(position));
                    mETPassword.setText(EmptyPassword);
                    popupWindow.dismiss();
                }
            }
        });
        LinearLayoutManager layout = new LinearLayoutManager(this, RecyclerView.VERTICAL, false);
        rv.setLayoutManager(layout);
        rv.setItemAnimator(null);
        rv.setAdapter(adapter);

        popupWindow.showAsDropDown(llUser);
    }

    private View mLoginTvForget;

    private String lastSavePassword;

    //onEstablished会调用两次，用来标记唯一启动主页
    private boolean isLoginSuccessed = false;
    private View mLoginTvRegister;
    private View mLoginIvClearAccount;
    private View mLoginIvSelectAccount;
    private View mLoginEtUser;
    private View mLoginEtPw;
    private View mLoginEtCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SessionCache.Companion.getInstance().clear();
        RetrofitSingleton.Companion.getInstance().clear();
        initFullScreen();
//        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
//        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
//                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_login);
        bindView(this.getWindow().getDecorView());
        Intent intent = getIntent();
        if (intent != null) {
            int reason = intent.getIntExtra("dr", -1);
            if (reason != -1) {
                ToastUtils.showToast(ErrorCode.dr2String(reason));
                final String username = intent.getStringExtra(AppConstants.SP_FIELD_USERNAME);
                if (!TextUtils.isEmpty(username)) {
                    mAccount = username;
                    mPasswd = "";
                }
            }
        }
        initView();
        initEvent();

    }

    private void initData() {
        BaseInfo baseInfo = CMAPI.getInstance().getBaseInfo();
        mUsers = baseInfo.getUserList();

        String account = mETAccount.getText().toString().trim();
        String password = mETPassword.getText().toString().trim();
        BaseInfo baseinfo = CMAPI.getInstance().getBaseInfo();
        if (TextUtils.isEmpty(account) && TextUtils.isEmpty(password)) {
            if (!TextUtils.isEmpty(mAccount)) {
                mETAccount.setText(mAccount);
                mETPassword.setText(mPasswd);
            } else if (baseinfo != null && !baseinfo.getAccount().isEmpty()) {
                mETAccount.setText(baseinfo.getAccount());
                mETPassword.setText(EmptyPassword);
            } else {
                mETAccount.setText("");
                mETPassword.setText("");
            }
        }
        password = null;
    }

    private void onViewClicked(View view) {
        if (Utils.isFastClick(view)) {
            return;
        }
        if (view instanceof EditText) {
            InputMethodUtils.showKeyboard(view.getContext(), (EditText) view);
            return;
        }
        switch (view.getId()) {
            case R.id.login_iv_clear_account:
                mETAccount.setText("");
                mETPassword.setText("");
                mETAccount.requestFocus();
                break;
            case R.id.login_iv_select_account:
                showUserDropdown();
                break;
            case R.id.login_btn_login:
                setStatus(LoadingStatus.LOGIN);
                login();
                break;
            case R.id.login_img_back:
                onBackPressed();
                break;
            case R.id.login_btn_tv_code:
                break;
            case R.id.login_tv_forget:
                final Intent intentForget = new Intent(this, ForgetPasswordActivity.class);
                final String account = mETAccount.getText().toString().trim();
                intentForget.putExtra(AppConstants.SP_FIELD_USERNAME, account);
                startActivity(intentForget);
                break;
            case R.id.login_tv_register:
                final Intent intentRegister = new Intent(this, RegisterActivity.class);
                final String account2 = mETAccount.getText().toString().trim();
                intentRegister.putExtra(AppConstants.SP_FIELD_USERNAME, account2);
                startActivity(intentRegister);
                break;
        }
    }

    private void login() {
        /*if (checkableImageButton != null && !checkableImageButton.isChecked()) {
            ToastUtils.showToast(getString(R.string.read_and_agree_user_agreement_and_privacy));
            InputMethodUtils.hideKeyboard(this);
            AnimUtils.sharkEditText(checkableImageButton);
            return;
        }*/
        if (!NetworkUtils.checkNetwork(this)) {
            showToast(R.string.network_not_available);
            return;
        }

        String account = mETAccount.getText().toString().trim();
        String password = mETPassword.getText().toString().trim();
        if (account.isEmpty() || password.isEmpty()) {
            showToast(getString(R.string.please_enter_your_account_and_password));
            if (account.isEmpty()) {
                mETAccount.requestFocus();
                mETAccount.setEnabled(true);
                return;
            }
            mETPassword.requestFocus();
            mETPassword.setEnabled(true);
            return;
        }
        if (EmptyPassword.equalsIgnoreCase(password))
            password = "";
        updateUser(account, password);
        mSdvnStatusViewModel.toLogin(account, password, new ResultListener() {
            @Override
            public void onError(int reason) {
                updateUser(account,pwdOld);
                loginBtnEnable();
                if (Constants.DR_BY_USER != reason &&
                        Constants.DR_MISSING_INFO != reason &&
                        Constants.DR_UNSET != reason
                        && Constants.DR_CONNECTED != reason) {
                    ToastUtils.showToast(UiUtils.formatWithError(ErrorCode.dr2String(reason), reason));
                }
            }
        });
    }

    private String pwdOld = "";
    private void updateUser(String account, String password){
        if(!password.isEmpty()){
            new Thread(new Runnable() {
                @Override
                public void run()
                {
                    UserDao dao = AppDatabase.Companion.getInstance(getApplicationContext()).getUserDao();
                    UserModel userModel = dao.getUser(account.toLowerCase());
                    if(userModel == null){
                        userModel = new UserModel(account.toLowerCase(),password,password);
                        userModel.setPwdNew(password);
                        dao.insert(userModel);
                    }
                    else {
                        pwdOld = userModel.getPwdNew();
                        if(!password.equals(pwdOld)){
                            userModel.setPwdNew(password);
                            dao.insert(userModel);
                        }
                    }
                }
            }).start();
        }
    }

    @Override
    public void onBackPressed() {
//        ThemeActivity.enterMainInstance(this);
        super.onBackPressed();
    }

    @Override
    public void onEstablished() {
//        LoginManager.getInstance().notifyLogin(true);
//        loginBtnLogin.setEnabled(true);
        if (!isLoginSuccessed) {
            isLoginSuccessed = true;
            String password = mETPassword.getText().toString().trim();
            // if (!TextUtils.isEmpty(password) && !Objects.equals(EmptyPassword, password) &&
            //         !Objects.equals(password, lastSavePassword)) {
            //     lastSavePassword = password;
            // }
            password = null;
//            ThemeActivity.enterMainInstance(this);
            //回到调用的Activity
            finish();
        }
    }

    @Override
    protected void onDisconnected() {
        super.onDisconnected();
        loginBtnEnable();
    }

    private void bindView(View bindSource) {
        loginBtnLogin = bindSource.findViewById(R.id.login_btn_login);
        loginBtnLogin.setEnabled(true);
        loginImgBack = bindSource.findViewById(R.id.login_img_back);
        mETAccount = bindSource.findViewById(R.id.login_et_user);
        mETPassword = bindSource.findViewById(R.id.login_et_pw);
        loginEtCode = bindSource.findViewById(R.id.login_et_code);
        loginBtnTvCode = bindSource.findViewById(R.id.login_btn_tv_code);
        loginTvForget = bindSource.findViewById(R.id.login_tv_forget);
        loginTvRegister = bindSource.findViewById(R.id.login_tv_register);
        llContent = bindSource.findViewById(R.id.login_ll_content);
        llUser = bindSource.findViewById(R.id.login_ll_user);
        ivClearAccount = bindSource.findViewById(R.id.login_iv_clear_account);
        ivSelectAccount = bindSource.findViewById(R.id.login_iv_select_account);
        mLoginBtnLogin = bindSource.findViewById(R.id.login_btn_login);
        mLoginImgBack = bindSource.findViewById(R.id.login_img_back);
        mLoginBtnTvCode = bindSource.findViewById(R.id.login_btn_tv_code);
        mLoginTvForget = bindSource.findViewById(R.id.login_tv_forget);
        mLoginTvRegister = bindSource.findViewById(R.id.login_tv_register);
        mLoginIvClearAccount = bindSource.findViewById(R.id.login_iv_clear_account);
        mLoginIvSelectAccount = bindSource.findViewById(R.id.login_iv_select_account);
        mLoginEtUser = bindSource.findViewById(R.id.login_et_user);
        mLoginEtPw = bindSource.findViewById(R.id.login_et_pw);
        mLoginEtCode = bindSource.findViewById(R.id.login_et_code);
        pwdVisible = findViewById(R.id.login_iv_visible);
        checkableImageButton = findViewById(R.id.checkBox_agreement);

        mLoginBtnLogin.setOnClickListener(v -> {
            onViewClicked(v);
        });
        mLoginImgBack.setOnClickListener(v -> {
            onViewClicked(v);
        });
        mLoginBtnTvCode.setOnClickListener(v -> {
            onViewClicked(v);
        });
        mLoginTvForget.setOnClickListener(v -> {
            onViewClicked(v);
        });
        mLoginTvRegister.setOnClickListener(v -> {
            onViewClicked(v);
        });
        mLoginIvClearAccount.setOnClickListener(v -> {
            onViewClicked(v);
        });
        mLoginIvSelectAccount.setOnClickListener(v -> {
            onViewClicked(v);
        });
        mLoginEtUser.setOnClickListener(v -> {
            onViewClicked(v);
        });
        mLoginEtPw.setOnClickListener(v -> {
            onViewClicked(v);
        });
        mLoginEtCode.setOnClickListener(v -> {
            onViewClicked(v);
        });
    }
}
