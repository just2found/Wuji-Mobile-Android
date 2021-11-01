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
import net.linkmate.app.util.ToastUtils;
import net.linkmate.app.util.UIUtils;
import net.linkmate.app.util.business.ResetPasswordUtil;
import net.linkmate.app.view.CountDownTextView;
import net.linkmate.app.view.TipsBar;
import net.sdvn.common.internet.core.GsonBaseProtocol;
import net.sdvn.common.internet.core.HttpLoader;
import net.sdvn.nascommon.constant.AppConstants;
import net.sdvn.nascommon.utils.Utils;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.reactivex.disposables.Disposable;

import static net.linkmate.app.base.MyConstants.regNumLetterAndChar;
import static net.linkmate.app.util.ToastUtils.showToast;

public class ForgetPasswordActivity extends BaseActivity implements HttpLoader.HttpLoaderStateListener {
    Pattern compile = Pattern.compile(regNumLetterAndChar);

    Button btnReset;

    ImageView ImgBack;

    EditText etUser;

    EditText etCode;

    CountDownTextView mCountDownTV;

    CountryCodePicker ccp;

    TextView tvCcp;

    TextView tvUse;

    ImageView ivPwd;

    TextView tvPwdTips;

    EditText etPwd;

    ImageView ivConfirmPwd;

    TextView tvConfirmPwdTips;

    EditText etConfirmPwd;
    private int resetWay = SING_UP_BY_PHONE;
    private static final int SING_UP_BY_PHONE = 1;
    private static final int SING_UP_BY_EMAIL = 2;
    private String countryCode;
    private String mAccount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initFullScreen();
        setContentView(R.layout.activity_forget);
        btnReset = (Button) findViewById(R.id.forget_pwd_btn_reset);
        ImgBack = (ImageView) findViewById(R.id.forget_pwd_img_back);
        etUser = (EditText) findViewById(R.id.forget_pwd_et_user);
        etCode = (EditText) findViewById(R.id.forget_pwd_et_code);
        mCountDownTV = (CountDownTextView) findViewById(R.id.forget_pwd_cdtv);
        ccp = (CountryCodePicker) findViewById(R.id.ccp);
        tvCcp = (TextView) findViewById(R.id.tv_ccp);
        tvUse = (TextView) findViewById(R.id.forget_pwd_tv_use_email);
        ivPwd = (ImageView) findViewById(R.id.register_iv_pw);
        tvPwdTips = (TextView) findViewById(R.id.tv_pwd_tips);
        etPwd = (EditText) findViewById(R.id.register_et_pw);
        ivConfirmPwd = (ImageView) findViewById(R.id.register_iv_confirm_pw);
        tvConfirmPwdTips = (TextView) findViewById(R.id.tv_confirm_pwd_tips);
        etConfirmPwd = (EditText) findViewById(R.id.register_et_confirm_pw);
        findViewById(R.id.forget_pwd_tv_use_email).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onViewClicked((View) v);
            }
        });
        findViewById(R.id.forget_pwd_cdtv).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onViewClicked((View) v);
            }
        });
        findViewById(R.id.tv_ccp).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onViewClicked((View) v);
            }
        });
        findViewById(R.id.forget_pwd_img_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onViewClicked((View) v);
            }
        });
        findViewById(R.id.forget_pwd_btn_reset).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onViewClicked((View) v);
            }
        });

        final String username = getIntent().getStringExtra(AppConstants.SP_FIELD_USERNAME);
        if (!TextUtils.isEmpty(username)) {
            mAccount = username;
            String pattern = MyConstants.REGEX_EMAIL;
            Pattern r = Pattern.compile(pattern);
            Matcher m = r.matcher(mAccount);
            if (m.matches()) {
                resetWay = SING_UP_BY_EMAIL;
            } else {
                resetWay = SING_UP_BY_PHONE;
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
        showLayoutContent(true);
        ccp.setAutoDetectedCountry(true);
        ccp.setOnCountryChangeListener(new CountryCodePicker.OnCountryChangeListener() {
            @Override
            public void onCountrySelected() {
                tvCcp.setText(String.format("+%s", ccp.getSelectedCountryCode()));
            }
        });

        mCountDownTV.setNormalText(getString(R.string.get_verify_code))
                .setCountDownText(getString(R.string.get_verify_code) + "(", ")")
                .setCountDownClickable(false)
                .setCloseKeepCountDown(true)
                .setOnCountDownFinishListener(new CountDownTextView.OnCountDownFinishListener() {
                    @Override
                    public void onFinish() {
                        tvUse.setVisibility(View.VISIBLE);
                    }
                });
        etCode.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(final CharSequence code, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
//        etPwd.addTextChangedListener(new PasswdTextWatcher(etPwd, compile));
        switchResetWay(resetWay);
        etUser.setText(mAccount);
        findViewById(R.id.iv_visible).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UIUtils.togglePasswordStatus(v, etPwd, etConfirmPwd);
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
                etPwd.setTextColor(getResources().getColor(R.color.theme_body_text));
                pwd = null;
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
    }

    private void showLayoutContent(boolean isShow) {
        if (isShow) {
            ivPwd.setImageResource(R.drawable.icon_pw);
            ivConfirmPwd.setImageResource(R.drawable.icon_pw);
        } else {
            ivPwd.setImageResource(R.drawable.icon_pw_disable);
            ivConfirmPwd.setImageResource(R.drawable.icon_pw_disable);
        }
        etPwd.setEnabled(isShow);
        etConfirmPwd.setEnabled(isShow);
    }


    public void onViewClicked(View view) {
        if (Utils.isFastClick(view)) {
            return;
        }
        switch (view.getId()) {
            case R.id.forget_pwd_btn_reset:
                reset();
                break;
            case R.id.forget_pwd_img_back:
                onBackPressed();
                break;
            case R.id.forget_pwd_tv_use_email:
                switchResetWay();
                break;
            case R.id.tv_ccp:
                ccp.launchCountrySelectionDialog();
                break;
            case R.id.forget_pwd_cdtv:
                requestAuxCode();
                break;
        }
    }

    private void switchResetWay() {
        switchResetWay(-1);
    }

    private void switchResetWay(int signUpType) {
        ImageView ivLoginType = findViewById(R.id.icon_login_type);
        if (signUpType == SING_UP_BY_EMAIL || signUpType == SING_UP_BY_PHONE) {
            resetWay = signUpType;
        } else {
            if (resetWay == SING_UP_BY_EMAIL) {
                resetWay = SING_UP_BY_PHONE;
            } else {
                resetWay = SING_UP_BY_EMAIL;
            }
        }
        if (resetWay == SING_UP_BY_PHONE) {
            tvUse.setText(R.string.use_email);
            etUser.setHint(R.string.phone);
            tvCcp.setVisibility(View.VISIBLE);
            etUser.setInputType(InputType.TYPE_CLASS_PHONE);
            String userName = etUser.getText().toString().trim();
            if (!userName.matches("\\d+")) {
                etUser.setText("");
            }
            ivLoginType.setImageResource(R.drawable.icon_logo_user);
        } else if (resetWay == SING_UP_BY_EMAIL) {
            ivLoginType.setImageResource(R.drawable.icon_mail);
            tvUse.setText(R.string.use_phone);
            etUser.setHint(R.string.email);
            tvCcp.setVisibility(View.GONE);
            etUser.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        }
    }

    private void requestAuxCode() {
        String user = etUser.getText().toString().trim();
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
        if (resetWay == SING_UP_BY_PHONE) {
            if (TextUtils.isEmpty(user)) {
                ToastUtils.showToast(R.string.error_phone_number_tips);
                return;
            }
            countryCode = ccp.getSelectedCountryCode();
            ResetPasswordUtil.requestAuxCode(countryCode, user, this, listener);
        } else if (resetWay == SING_UP_BY_EMAIL) {
            if (TextUtils.isEmpty(user) || !user.matches(MyConstants.REGEX_EMAIL)) {
                ToastUtils.showToast(R.string.error_phone_email_tips);
                return;
            }
            ResetPasswordUtil.requestAuxCode(user, this, listener);
        }
        mCountDownTV.setEnabled(false);
    }

    private void reset() {
        String account = etUser.getText().toString().trim();
        String code = etCode.getText().toString().trim();
        String pwd = etPwd.getText().toString().trim();
        String confPwd = etConfirmPwd.getText().toString().trim();

        if (resetWay == SING_UP_BY_PHONE) {
            if (TextUtils.isEmpty(account)) {
                ToastUtils.showToast(R.string.error_phone_number_tips);
                etUser.requestFocus();
                return;
            }
        } else if (resetWay == SING_UP_BY_EMAIL) {
            if (TextUtils.isEmpty(account) || !account.matches(MyConstants.REGEX_EMAIL)) {
                ToastUtils.showToast(R.string.error_phone_email_tips);
                etUser.requestFocus();
                return;
            }
        }
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
        ResetPasswordUtil.reset(account,account, code, pwd, this, new MyOkHttpListener() {
            @Override
            public void success(Object tag, GsonBaseProtocol data) {
                ToastUtils.showToast(R.string.reset_pwd_succ);
                ForgetPasswordActivity.this.finish();
            }
        });
        pwd = null;
        confPwd = null;
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
}
