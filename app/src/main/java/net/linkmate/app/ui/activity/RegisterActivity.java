package net.linkmate.app.ui.activity;

import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.hbb20.CountryCodePicker;

import net.linkmate.app.R;
import net.linkmate.app.base.BaseActivity;
import net.linkmate.app.base.MyConstants;
import net.linkmate.app.base.MyOkHttpListener;
import net.linkmate.app.poster.database.AppDatabase;
import net.linkmate.app.poster.model.UserModel;
import net.linkmate.app.util.ToastUtils;
import net.linkmate.app.util.UIUtils;
import net.linkmate.app.util.business.RegisterUtil;
import net.linkmate.app.view.CountDownTextView;
import net.linkmate.app.view.TipsBar;
import net.sdvn.common.internet.core.GsonBaseProtocol;
import net.sdvn.common.internet.core.HttpLoader;
import net.sdvn.nascommon.constant.AppConstants;
import net.sdvn.nascommon.utils.Utils;
import net.sdvn.nascommon.widget.CheckableImageButton;

import org.view.libwidget.MagicTextViewUtil;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.reactivex.disposables.Disposable;
import kotlin.Unit;

import static net.linkmate.app.util.ToastUtils.showToast;

public class RegisterActivity extends BaseActivity implements HttpLoader.HttpLoaderStateListener {
    Pattern compile = Pattern.compile(MyConstants.regNumLetterAndChar);
    private Button btnRegister;
    private ImageView registerImgBack;
    private EditText etUser;
    private EditText etCode;
    private CountDownTextView mCountDownTV;
    private CountryCodePicker ccp;
    private TextView tvCcp;
    private TextView tvUse;
    private TextView tvAccountTips;
    private EditText etAccount;
    //    @BindView(R.id.register_et_firstname)
//    EditText etFirstname;
//    @BindView(R.id.register_et_lastname)
//    EditText etLastname;
    private
    TextView tvNickTips;
    private EditText etNickname;
    private ImageView ivPwd;
    private TextView tvPwdTips;
    private EditText etPwd;
    private ImageView ivConfirmPwd;
    private TextView tvConfirmPwdTips;
    private EditText etConfirmPwd;
    private CheckableImageButton cb_agreement;
    private TextView tvSubAgreement;
    private int registerWay = SING_UP_BY_PHONE;
    private static final int SING_UP_BY_PHONE = 1;
    private static final int SING_UP_BY_EMAIL = 2;
    private String user;
    private String countryCode;
    //    private String auxCode;
    private String mAccount;
    private View mRegisterBtnRegister;
    private View mRegisterImgBack;
    private View mTvCcp;
    private View mRegisterCdtv;
    private View mRegisterTvUseEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initFullScreen();
        setContentView(R.layout.activity_register);
        final String username = getIntent().getStringExtra(AppConstants.SP_FIELD_USERNAME);
        if (!TextUtils.isEmpty(username)) {
            mAccount = username;
            String pattern = MyConstants.REGEX_EMAIL;
            Pattern r = Pattern.compile(pattern);
            Matcher m = r.matcher(mAccount);
            if (m.matches()) {
                registerWay = SING_UP_BY_EMAIL;
            } else {
                registerWay = SING_UP_BY_PHONE;
            }
        }
        initView();
    }

    @Nullable
    @Override
    protected TipsBar getTipsBar() {
        return null;
    }

    @Override
    protected View getTopView() {
        return findViewById(R.id.root_layout);
    }

    private void initView() {
        bindView();
        ccp.setAutoDetectedCountry(true);
        ccp.setOnCountryChangeListener(new CountryCodePicker.OnCountryChangeListener() {
            @Override
            public void onCountrySelected() {
                tvCcp.setText("+" + ccp.getSelectedCountryCode());
            }
        });

        mCountDownTV.setNormalText(getString(R.string.get_verify_code))
                .setCountDownText(getString(R.string.get_verify_code) + "(", ")")
                .setCountDownClickable(false)
                .setCloseKeepCountDown(true)
                .setOnCountDownFinishListener(new CountDownTextView.OnCountDownFinishListener() {
                    @Override
                    public void onFinish() {
                        mCountDownTV.setEnabled(true);
                    }
                });
        findViewById(R.id.iv_visible).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UIUtils.togglePasswordStatus(v,etPwd, etConfirmPwd);
            }
        });
        initEditEvent();

        switchRegisterWay(registerWay);
        etUser.setText(mAccount);
        String agreeAgreement = getString(R.string.agree_user_agreement);
        String agreement = getString(R.string.sub_agreement);
        String agreementUrl = getString(R.string.subscriber_agreement_url);
        String privatePolicy = getString(R.string.privacy_policy);
        String privatePolicyUrl = getString(R.string.privacy_policy_url);
        int color = getResources().getColor(R.color.link_blue);
//        //通过TextView里面的类html标签来实现显示效果
//        String text = "<font color='#f9f9f9'>%s</font><font color='blue'><a href='%s'>%s</a></font>" +
//                "<font color='#f9f9f9'> & </font><font color='blue'><a href='%s'>%s</a></font>";
//
//        tvSubAgreement.setText(Html.fromHtml(String.format(text, agreeAgreement, agreementUrl,
//                agreement, privatePolicyUrl, privatePolicy)));
//        //设置鼠标移动事件，产生链接显示,没有这句话，进不去
//        tvSubAgreement.setMovementMethod(LinkMovementMethod.getInstance());
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

    private void initEditEvent() {
//        etCode.addTextChangedListener(new TextWatcher() {
//            @Override
//            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//
//            }
//
//            @Override
//            public void onTextChanged(final CharSequence code, int start, int before, int count) {
//                if (code.length() == 6) {
//                    MyOkHttpListener listener = new MyOkHttpListener() {
//                        @Override
//                        public void success(Object tag, GsonBaseProtocol data) {
//                            mCountDownTV.setEnabled(false);
//                            auxCode = code.toString();
//                        }
//                    };
//                    if (registerWay == SING_UP_BY_PHONE) {
//                        RegisterUtil.authAuxCode(countryCode, user, code.toString(), RegisterActivity.this, listener);
//                    } else if (registerWay == SING_UP_BY_EMAIL) {
//                        RegisterUtil.authAuxCode(user, code.toString(), RegisterActivity.this, listener);
//                    }
//                }
//            }
//
//            @Override
//            public void afterTextChanged(Editable s) {
//
//            }
//        });
        cb_agreement.setOnCheckedChangeListener((buttonView, isChecked) -> {
            btnRegister.setEnabled(isChecked);
        });
        etAccount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(final CharSequence code, int start, int before, int count) {
                String account = etAccount.getText().toString().trim();
                if (MyConstants.regExInputLoginName(account)) {
                    tvAccountTips.setTextColor(getResources().getColor(R.color.theme_body_text_hint));
                }
                etAccount.setTextColor(getResources().getColor(R.color.theme_body_text));
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        etAccount.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    String account = etAccount.getText().toString().trim();
                    if (!MyConstants.regExInputLoginName(account)) {
//                        tvAccountTips.setText(R.string.no_special_characters_in_the_loginname);
//                        tvAccountTips.setTextColor(getResources().getColor(R.color.text_orange));
                        tvAccountTips.setVisibility(View.GONE);
                        etAccount.setTextColor(getResources().getColor(R.color.text_orange));
                        return;
                    }
                    tvAccountTips.setVisibility(View.GONE);
                    etAccount.setTextColor(getResources().getColor(R.color.theme_body_text));
                } else {
//                    tvAccountTips.setText(R.string.no_special_characters_in_the_loginname);
//                    tvAccountTips.setTextColor(getResources().getColor(R.color.theme_body_text_hint));
                    tvAccountTips.setVisibility(View.GONE);
                }
            }
        });
        etNickname.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(final CharSequence code, int start, int before, int count) {
                String nick = etNickname.getText().toString().trim();
                if (!nick.isEmpty() && nick.length() <= 24) {
                    tvNickTips.setTextColor(getResources().getColor(R.color.theme_body_text_hint));
                }
                etNickname.setTextColor(getResources().getColor(R.color.theme_body_text));
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        etNickname.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    String nick = etNickname.getText().toString().trim();
                    if (nick.isEmpty() || nick.length() > 24) {
//                        tvNickTips.setText(R.string.error_string_no_nickname);
//                        tvNickTips.setTextColor(getResources().getColor(R.color.text_orange));
                        tvNickTips.setVisibility(View.GONE);
                        etNickname.setTextColor(getResources().getColor(R.color.text_orange));
                        return;
                    }
                    tvNickTips.setVisibility(View.GONE);
                    etNickname.setTextColor(getResources().getColor(R.color.theme_body_text));
                } else {
//                    tvNickTips.setText(R.string.error_string_no_nickname);
//                    tvNickTips.setTextColor(getResources().getColor(R.color.theme_body_text_hint));
                    tvNickTips.setVisibility(View.GONE);
                }
            }
        });
        etPwd.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(final CharSequence code, int start, int before, int count) {
                String pwd = etPwd.getText().toString().trim();
                if (!pwd.isEmpty() && compile.matcher(pwd).matches()) {
                    tvPwdTips.setTextColor(getResources().getColor(R.color.theme_body_text_hint));
                }
                pwd = null;
                etPwd.setTextColor(getResources().getColor(R.color.theme_body_text));
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        etPwd.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    String pwd = etPwd.getText().toString().trim();
                    if (pwd.isEmpty() || !compile.matcher(pwd).matches()) {
                        tvPwdTips.setVisibility(View.VISIBLE);
                        tvPwdTips.setText(R.string.password_must_contains_num_letter_char);
                        tvPwdTips.setTextColor(getResources().getColor(R.color.text_orange));
                        etPwd.setTextColor(getResources().getColor(R.color.text_orange));
                        return;
                    }
                    pwd = null;
                    tvPwdTips.setVisibility(View.GONE);
                    etPwd.setTextColor(getResources().getColor(R.color.theme_body_text));
                } else {
                    tvPwdTips.setVisibility(View.VISIBLE);
                    tvPwdTips.setText(R.string.password_must_contains_num_letter_char);
                    tvPwdTips.setTextColor(getResources().getColor(R.color.theme_body_text_hint));
                }
            }
        });
        etConfirmPwd.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(final CharSequence code, int start, int before, int count) {
                String pwd = etPwd.getText().toString().trim();
                String confPwd = etConfirmPwd.getText().toString().trim();
                if (Objects.equals(pwd, confPwd)) {
                    tvConfirmPwdTips.setVisibility(View.GONE);
                }
                pwd = null;
                confPwd = null;
                etConfirmPwd.setTextColor(getResources().getColor(R.color.theme_body_text));
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        etConfirmPwd.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    String pwd = etPwd.getText().toString().trim();
                    String confPwd = etConfirmPwd.getText().toString().trim();
                    if (!Objects.equals(pwd, confPwd)) {
                        tvConfirmPwdTips.setVisibility(View.VISIBLE);
                        tvConfirmPwdTips.setText(R.string.error_string_pwd_confirm);
                        tvConfirmPwdTips.setTextColor(getResources().getColor(R.color.text_orange));
                        etConfirmPwd.setTextColor(getResources().getColor(R.color.text_orange));
                        return;
                    }
                    pwd = null;
                    confPwd = null;
                    tvConfirmPwdTips.setVisibility(View.GONE);
                    etConfirmPwd.setTextColor(getResources().getColor(R.color.theme_body_text));
                }
            }
        });
//        etPwd.addTextChangedListener(new PasswdTextWatcher(etPwd, compile));
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

    @Override
    protected void onResume() {
        super.onResume();
//        btnRegister.setEnabled(cb_agreement.isChecked());
    }

    private void onViewClicked(View view) {
        if (Utils.isFastClick(view)) {
            return;
        }
        switch (view.getId()) {
            case R.id.register_btn_register:
                register();
                break;
            case R.id.register_img_back:
                onBackPressed();
                break;
            case R.id.register_tv_use_email:
                switchRegisterWay();
                break;
            case R.id.tv_ccp:
                ccp.launchCountrySelectionDialog();
                break;
            case R.id.register_cdtv:
                requestAuxCode();
                break;
        }
    }

    private void switchRegisterWay() {
        switchRegisterWay(-1);
    }

    private void switchRegisterWay(int signUpType) {
        ImageView ivLoginType = findViewById(R.id.icon_login_type);
        if (signUpType == SING_UP_BY_EMAIL || signUpType == SING_UP_BY_PHONE) {
            registerWay = signUpType;
        } else {
            if (registerWay == SING_UP_BY_EMAIL) {
                registerWay = SING_UP_BY_PHONE;
            } else {
                registerWay = SING_UP_BY_EMAIL;
            }
        }
        if (registerWay == SING_UP_BY_PHONE) {
            tvUse.setText(R.string.use_email);
            etUser.setHint(R.string.phone);
            tvCcp.setVisibility(View.VISIBLE);
            etUser.setInputType(InputType.TYPE_CLASS_PHONE);
            String userName = etUser.getText().toString().trim();
            if (!userName.matches("\\d+")) {
                etUser.setText("");
            }
            ivLoginType.setImageResource(R.drawable.icon_logo_user);
        } else if (registerWay == SING_UP_BY_EMAIL) {
            ivLoginType.setImageResource(R.drawable.icon_mail);
            tvUse.setText(R.string.use_phone);
            etUser.setHint(R.string.email);
            tvCcp.setVisibility(View.GONE);
            etUser.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        }
    }

    private void requestAuxCode() {
        user = etUser.getText().toString().trim();
        MyOkHttpListener listener = new MyOkHttpListener() {
            @Override
            public void error(Object tag, GsonBaseProtocol baseProtocol) {
                super.error(tag, baseProtocol);
                mCountDownTV.setEnabled(true);
            }

            @Override
            public void success(Object tag, GsonBaseProtocol data) {
                mCountDownTV.startCountDown(60);
                etCode.setEnabled(true);
                etCode.requestFocus();
            }
        };
        if (registerWay == SING_UP_BY_PHONE) {
            if (TextUtils.isEmpty(user)) {
                ToastUtils.showToast(R.string.error_phone_number_tips);
                return;
            }
            countryCode = ccp.getSelectedCountryCode();
            RegisterUtil.requestAuxCode(countryCode, user, this, listener);
        } else if (registerWay == SING_UP_BY_EMAIL) {
            if (TextUtils.isEmpty(user) || !user.matches(MyConstants.REGEX_EMAIL)) {
                ToastUtils.showToast(R.string.error_phone_email_tips);
                return;
            }
            RegisterUtil.requestAuxCode(user, this, listener);
        }
        mCountDownTV.setEnabled(false);
    }

    private void register() {
        String countryCode = ccp.getSelectedCountryCode();
        String user = etUser.getText().toString().trim();
        String code = etCode.getText().toString().trim();
        String pwd = etPwd.getText().toString().trim();
        String confPwd = etConfirmPwd.getText().toString().trim();
        String account = etAccount.getText().toString().trim();
//        String firstname = etFirstname.getText().toString().trim();
//        String lastname = etLastname.getText().toString().trim();
        String nick = etNickname.getText().toString().trim();

        if (registerWay == SING_UP_BY_PHONE) {
            if (TextUtils.isEmpty(user)) {
                ToastUtils.showToast(R.string.error_phone_number_tips);
                etUser.requestFocus();
                return;
            }
        } else if (registerWay == SING_UP_BY_EMAIL) {
            if (TextUtils.isEmpty(user) || !user.matches(MyConstants.REGEX_EMAIL)) {
                ToastUtils.showToast(R.string.error_phone_email_tips);
                etUser.requestFocus();
                return;
            }
        }
        if (code.isEmpty()) {
            showToast(getString(R.string.error_string_no_auxcode));
            etCode.requestFocus();
            return;
        }
        if (account.isEmpty()) {
            showToast(getString(R.string.error_string_empty_account));
            etAccount.requestFocus();
            tvAccountTips.setVisibility(View.VISIBLE);
            tvAccountTips.setText(R.string.error_string_empty_account);
            etAccount.setTextColor(getResources().getColor(R.color.text_orange));
            return;
        }
        if (!MyConstants.regExInputLoginName(account)) {
            showToast(R.string.no_special_characters_in_the_loginname);
            etAccount.requestFocus();
            tvAccountTips.setVisibility(View.VISIBLE);
            tvAccountTips.setText(R.string.no_special_characters_in_the_loginname);
            tvAccountTips.setTextColor(getResources().getColor(R.color.text_orange));
            etAccount.setTextColor(getResources().getColor(R.color.text_orange));
            return;
        }
//        if (firstname.isEmpty() || firstname.length() > 32) {
//            showToast(getString(R.string.error_string_no_firstname));
//            etFirstname.requestFocus();
//            return;
//        }
//        if (!MyConstants.regExInput(firstname)) {
//            showToast(R.string.no_special_characters_in_the_name);
//            etFirstname.requestFocus();
//            return;
//        }
//        if (lastname.isEmpty() || lastname.length() > 32) {
//            showToast(getString(R.string.error_string_no_lastname));
//            etLastname.requestFocus();
//            return;
//        }
//        if (!MyConstants.regExInput(lastname)) {
//            showToast(R.string.no_special_characters_in_the_name);
//            etLastname.requestFocus();
//            return;
//        }
        if (nick.isEmpty() || nick.length() > 24) {
            showToast(getString(R.string.error_string_no_nickname));
            etNickname.requestFocus();
            tvNickTips.setVisibility(View.VISIBLE);
            tvNickTips.setText(R.string.error_string_no_nickname);
            tvNickTips.setTextColor(getResources().getColor(R.color.text_orange));
            etNickname.setTextColor(getResources().getColor(R.color.text_orange));
            return;
        }
//        if (!MyConstants.regExInput(nick)) {
//            showToast(R.string.no_special_characters_in_the_name);
//            etNickname.requestFocus();
//            return;
//        }
        if (pwd.isEmpty()) {
            showToast(getString(R.string.password_must_contains_num_letter_char));
            etPwd.requestFocus();
            tvPwdTips.setVisibility(View.VISIBLE);
            tvPwdTips.setText(R.string.password_must_contains_num_letter_char);
            tvPwdTips.setTextColor(getResources().getColor(R.color.text_orange));
            etPwd.setTextColor(getResources().getColor(R.color.text_orange));
            return;
        }
        if (!compile.matcher(pwd).matches()) {
            showToast(R.string.password_must_contains_num_letter_char);
            etPwd.requestFocus();
            tvPwdTips.setVisibility(View.VISIBLE);
            tvPwdTips.setText(R.string.password_must_contains_num_letter_char);
            tvPwdTips.setTextColor(getResources().getColor(R.color.text_orange));
            etPwd.setTextColor(getResources().getColor(R.color.text_orange));
            return;
        }
        if (!Objects.equals(pwd, confPwd)) {
            showToast(getString(R.string.error_string_pwd_confirm));
            etConfirmPwd.requestFocus();
            tvConfirmPwdTips.setVisibility(View.VISIBLE);
            tvConfirmPwdTips.setText(R.string.error_string_pwd_confirm);
            tvConfirmPwdTips.setTextColor(getResources().getColor(R.color.text_orange));
            etConfirmPwd.setTextColor(getResources().getColor(R.color.text_orange));
            return;
        }
        MyOkHttpListener listener = new MyOkHttpListener() {
            @Override
            public void success(Object tag, GsonBaseProtocol data) {
                updateUser(account,pwd);
                ToastUtils.showToast(R.string.register_succ);
                RegisterActivity.this.finish();
            }
        };
        if (registerWay == SING_UP_BY_PHONE) {
            RegisterUtil.register(countryCode, account, user, code, nick, pwd,
                    "", ""/*firstname, lastname*/, this, listener);
        } else if (registerWay == SING_UP_BY_EMAIL) {
            RegisterUtil.register(account, user, code, nick, pwd,
                    "", ""/*firstname, lastname*/, this, listener);
        }

    }

    private void updateUser(String account, String password){
        new Thread(new Runnable() {
            @Override
            public void run()
            {
                UserModel userModel = new UserModel(account.toLowerCase(),password,password);
                AppDatabase.Companion.getInstance(getApplicationContext()).getUserDao().insert(userModel);
            }
        }).start();
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
        btnRegister =  findViewById(R.id.register_btn_register);
        registerImgBack =  findViewById(R.id.register_img_back);
        etUser =  findViewById(R.id.register_et_user);
        etCode =  findViewById(R.id.register_et_code);
        mCountDownTV =  findViewById(R.id.register_cdtv);
        ccp =  findViewById(R.id.ccp);
        tvCcp =  findViewById(R.id.tv_ccp);
        tvUse =  findViewById(R.id.register_tv_use_email);
        tvAccountTips =  findViewById(R.id.tv_account_tips);
        etAccount =  findViewById(R.id.register_et_loginname);
        tvNickTips =  findViewById(R.id.tv_nick_tips);
        etNickname =  findViewById(R.id.register_et_nickname);
        ivPwd =  findViewById(R.id.register_iv_pw);
        tvPwdTips =  findViewById(R.id.tv_pwd_tips);
        etPwd =  findViewById(R.id.register_et_pw);
        ivConfirmPwd =  findViewById(R.id.register_iv_confirm_pw);
        tvConfirmPwdTips =  findViewById(R.id.tv_confirm_pwd_tips);
        etConfirmPwd =  findViewById(R.id.register_et_confirm_pw);
        cb_agreement =  findViewById(R.id.checkBox_agreement);
        tvSubAgreement =  findViewById(R.id.tv_sub_agreement);
        mRegisterBtnRegister =  findViewById(R.id.register_btn_register);
        mRegisterImgBack =  findViewById(R.id.register_img_back);
        mTvCcp =  findViewById(R.id.tv_ccp);
        mRegisterCdtv =  findViewById(R.id.register_cdtv);
        mRegisterTvUseEmail =  findViewById(R.id.register_tv_use_email);
        mRegisterBtnRegister.setOnClickListener(v -> {
            onViewClicked(v);
        });
        mRegisterImgBack.setOnClickListener(v -> {
            onViewClicked(v);
        });
        mTvCcp.setOnClickListener(v -> {
            onViewClicked(v);
        });
        mRegisterCdtv.setOnClickListener(v -> {
            onViewClicked(v);
        });
        mRegisterTvUseEmail.setOnClickListener(v -> {
            onViewClicked(v);
        });
    }

}
